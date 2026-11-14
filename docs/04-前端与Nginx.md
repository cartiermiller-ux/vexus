# 04 - 前端与 Nginx（PC + WAP 静态部署、域名补丁、反代与 HTTPS）

> 适用范围：Vexus Java 交易所「源码版」部署（Tomcat 三应用：`/admin`、`/web`、`/data`）。
> 对接文档：`01-部署总览.md`（整体顺序）、`05-安全加固与账号.md`（后台安全）。
> 配套文件：
> - 前端补丁脚本：[patch-frontend.ps1](../deploy/frontend/patch-frontend.ps1) / [patch-frontend.sh](../deploy/frontend/patch-frontend.sh)
> - 补丁变更与 0 命中复验：[CHANGES.md](../deploy/frontend/CHANGES.md)
> - Nginx 配置：[vexus-single-domain.conf](../deploy/nginx/vexus-single-domain.conf)、[vexus-https-ssl.conf](../deploy/nginx/vexus-https-ssl.conf)、[vexus-multi-domain.conf](../deploy/nginx/vexus-multi-domain.conf)、[README.md](../deploy/nginx/README.md)

---

## 0. TL;DR（最短路径）

```bash
# ① 本地生成补丁前端（占位域名先看效果；正式发布前换真实域名！）
cd deploy/frontend
powershell -ExecutionPolicy Bypass -File .\patch-frontend.ps1 -Domain 你的域名     # Windows
bash patch-frontend.sh -d 你的域名                                                # Linux

# ② 上传到服务器
rsync -av deploy/frontend/zcx/ root@服务器:/www/wwwroot/vexus/site/

# ③ 改 Nginx 占位符并启用（详见第 4 章）
sed -i 's#your-domain.com#你的域名#g' deploy/nginx/vexus-single-domain.conf
cp deploy/nginx/vexus-single-domain.conf /etc/nginx/conf.d/vexus.conf
nginx -t && systemctl reload nginx

# ④ 申请证书（详见 4.3）
certbot --nginx -d 你的域名 -d www.你的域名 --redirect

# ⑤ 验证（详见第 6 章）
curl -I https://你的域名/
curl -s 'https://你的域名/api/item!list.action' | head -c 200
```

---

## 1. 目录规划与权限

### 1.1 服务器目录

```
/www/wwwroot/vexus/
├── site/                  # ← deploy/frontend/zcx 的内容（PC 站根 + WAP 子目录）
│   ├── index.html         #    PC 入口（补丁后标题 Vexus）
│   ├── js/app.*.js        #    PC 入口脚本（补丁后 WS/跳转均为新域名）
│   ├── css/ img/ fonts/ symbol/ ...
│   ├── wap/               #    WAP 站（index.html、js、css、img、desk/manifest.json、symbol/）
│   ├── app.html           #    App 下载落地页（APK/mobileconfig 默认不发布，下载按钮已摘除）
│   └── 404.html           #    zcx 自带 404 页
├── images/                # ← 后台/前端上传的图片（= system.properties 的 images.dir）
├── logs/                  # ← nginx access.log / error.log
└── ...
```

- **`site` 里放的是 `zcx` 的“内容”**，不是 `zcx` 目录本身。
  正确：`/www/wwwroot/vexus/site/index.html`；错误：`/www/wwwroot/vexus/site/zcx/index.html`（会整站 404）。
- WAP 静态资源在 `site/wap/`，webpack `publicPath=/wap/`，因此 Nginx 必须保留 `/wap/` 这个路径层级。
- `images` 必须在**站点根目录之外**（否则 `/images/xxx` 会被 SPA fallback 抓走），由 Nginx `location ^~ /images/` 单独 alias。
- 后台上传接口 `UploadImgController`/`ShowImgController` 的 basePath 是 `/public/`；
  对外访问图片用 `https://域名/images/...`（对应 `http.server.host` + `images.dir`）。

### 1.2 权限

| 目录/文件 | 属主 | 权限 | 说明 |
| --- | --- | --- | --- |
| `/www/wwwroot/vexus` | `www:www`（宝塔）或 `nginx:nginx` | 755 | 按实际运行 Nginx 的用户调整 |
| `site/` 及静态文件 | `www:www` | 目录 755 / 文件 644 | 静态只读即可；**不要** chmod 777 |
| `images/` | `www:www`，或 Tomcat 运行用户 | 775（组可写） | 后台上传需要可写；若 Tomcat 与 Nginx 不同用户，可让 images 属组同时包含二者 |
| `logs/` | `www:www`（或 nginx 用户） | 755 | Nginx 写日志需要权限 |
| 证书私钥 | root | 600 | certbot 目录默认就是 600，不要放宽 |

命令示例（宝塔面板，Nginx/Tomcat 均以 `www` 运行）：

```bash
mkdir -p /www/wwwroot/vexus/{site,images,logs}
rsync -av deploy/frontend/zcx/ /www/wwwroot/vexus/site/
chown -R www:www /www/wwwroot/vexus
find /www/wwwroot/vexus/site -type d -exec chmod 755 {} \;
find /www/wwwroot/vexus/site -type f -exec chmod 644 {} \;
chmod -R 775 /www/wwwroot/vexus/images
chmod 755 /www/wwwroot/vexus/logs
```

---

## 2. 前端现状与补丁说明（为什么必须打补丁）

原始 `前端wap+pc编译后/zcx`（41MB / 844 文件）是卖家编译好的 PC+WAP 站点，**不能直接上线**：

| 问题 | 说明 |
| --- | --- |
| 硬编码卖家域名 | 3 个 JS 里写了 `wss://kelubit.id/data/websocket`（PC/WAP 行情/交易 WS）、`https://kelubit.id/wap/#/login`、`https://vexus.kelubit.id/wap/#/register`，共 5 处；不改会连卖家服务器（用户 IP/交易行为外泄） |
| 卖家广告注入 | `index.html` / `wap/index.html` 标题为“365ym源码网 365ym.com”、`365ym.com` WhatsApp 悬浮按钮、cdnjs page-accelerator 脚本、base64 伪 SEO meta |
| 防调试脚本 | PC 根 `index.html` 引入 `/block.js`（禁 F12/右键/复制），WAP 里被注释；文件本体也带 365ym 注释 |
| 第三方后门 | WAP 入口 `getConditionUrl()` 在接口异常时把机器码/用户名回传 `www.anedhaude.xyz` 并接受远端下发的接口地址；PC `/app` 下载页 iOS 按钮指向 `down.uchajq0.com` |
| 假数据目录 | `wap/api/` 下 ~200 个卖家保存的接口响应快照（`assets!getAll.action` 等），部署后会被当静态文件返回，后台 JSP `base + "wap/api/realtime!execute.action"` 会读到过期假数据 |
| 可疑安装包 | `Vexus.apk`（2.65MB，未签名）、`Vexus.mobileconfig`（iOS 描述文件），来路不明，默认不发布 |

补丁脚本会：复制 → 清注入/后门 → 排除 `wap/api` → 全量替换域名 → **断言 0 残留** → 写 `patch-report.json`。
详细改动清单见 [CHANGES.md](../deploy/frontend/CHANGES.md)。

### 2.1 路由模式（决定 try_files 怎么写）

在 PC/WAP 的 `app.*.js` 与 `chunk-vendors.*.js` 里**都找不到** `mode:"history"` / `mode:"hash"`：

- PC：`new VueRouter({routes:[...]})`；WAP：`new VueRouter({scrollBehavior:..., routes:[...]})`；
- vue-router 不传 `mode` 时**默认 hash 模式** ⇒ PC 地址形如 `https://域名/#/index`，WAP 形如 `https://域名/wap/#/login`。

因此 Nginx 用兼容写法即可：

```nginx
location /      { try_files $uri $uri/ /index.html; }      # PC
location ^~ /wap/ { try_files $uri $uri/ /wap/index.html; } # WAP
```

- hash 模式下这两个 fallback 基本不会触发，**没有副作用**；
- 若以后前端改成 history 模式（`/order/exchangeHistory` 这类无 `#` 地址），这两行就是必需的。

**最终确认方法（部署后 1 分钟）**：

1. 浏览器打开首页，看地址栏：带 `#` = hash 模式；切换菜单后变成 `/order/exchangeHistory` 而无 `#` = history 模式；
2. 在 `site/js/app.*.js` 里搜 `createWebHistory`：搜不到 = 不是 history 模式（本项目搜不到）；
3. 直接访问一个深链，如 `https://域名/#/order/exchangeHistory` 能打开、`https://域名/order/exchangeHistory` 若也返回首页则 fallback 生效（两种模式都兼容）。

---

## 3. 前端补丁完整步骤

### 3.1 方式 A：本地（Windows）生成 → 上传（推荐）

```powershell
# 在仓库根目录
cd deploy\frontend

# 首次/每次换域名都重新生成（幂等：先删旧副本、再重新复制，可反复执行）
powershell -ExecutionPolicy Bypass -File .\patch-frontend.ps1 -Domain vexus.io
# 或（PowerShell 7）
pwsh -File .\patch-frontend.ps1 -Domain vexus.io

# 保留 APK/描述文件（不推荐，安全风险自负）
powershell -ExecutionPolicy Bypass -File .\patch-frontend.ps1 -Domain vexus.io -KeepAppPackages

# 原始目录不在默认位置时显式指定（默认按项目相对路径自动定位）
powershell -ExecutionPolicy Bypass -File .\patch-frontend.ps1 -Domain vexus.io `
  -SourceDir "D:\源码\前端wap+pc编译后\zcx" -OutDir "D:\deploy\zcx"
```

脚本输出示例（节选）：

```
[2/6] 排除 wap\api\ 接口快照目录 ...  已排除 200 个文件，1.45 MB
[3/6] 清理 PC/WAP index.html 注入内容 ...  index.html: 删除 35 行 ... 标题已改
[5/6] 替换硬编码域名 kelubit.id ...  替换 5 处（vexus子域 1 / wss 2 / https 2）
      [断言通过] kelubit.id 全量 0 命中
      [断言通过] 所有 WebSocket 地址均为新域名 wss://vexus.io
[6/6] 部署目录 : 640 个文件 / 36.74 MB
```

然后上传：

```bash
# Windows 可用 WinSCP / scp；服务器需先建好 site 目录
scp -r deploy/frontend/zcx/* root@服务器:/www/wwwroot/vexus/site/
# 或 rsync（删掉服务器上旧的多余文件，保证与产物一致）
rsync -av --delete deploy/frontend/zcx/ root@服务器:/www/wwwroot/vexus/site/
```

### 3.2 方式 B：源码整体上传服务器后，直接在服务器打补丁（Linux）

```bash
# 把整个项目目录（或至少 前端wap+pc编译后/zcx + deploy/frontend 两个目录）上传到服务器
cd /path/to/repo/deploy/frontend
chmod +x patch-frontend.sh
bash patch-frontend.sh -d vexus.io
# 默认输出到 deploy/frontend/zcx；也可指定输出到站点目录（省一次拷贝）：
bash patch-frontend.sh -d vexus.io -o /www/wwwroot/vexus/site
```

> Linux 版与 Windows 版改动一致；`patch-frontend.sh -h` 查看全部参数。
> 若服务器上没有原始 `zcx`，用 `-s /path/to/zcx` 或环境变量 `VEXUS_SRC=/path/to/zcx` 指定。

### 3.3 补丁后必须同步的 3 个后端配置项（**最容易漏**）

前端域名改完后，后台 `system.properties` 里的地址如果还是卖家的，会导致后台生成的链接、分享图、上传图地址、支付回调跳转全部指向旧站。
**每个后端应用的 `WEB-INF/classes/config/system.properties` 都要改**（源码版/预编译版目录不同，见下）：

```properties
# 以 https://vexus.io 为例
admin_url=https://vexus.io/admin          # 后台访问地址（web3 预编译版是 https://.../schw）
web_url=https://vexus.io/wap              # 前端 WAP 地址
http.server.host=https://vexus.io/images/ # 图片/上传文件对外前缀（末尾带 /images/）
images.dir=/www/wwwroot/vexus/images/     # 服务器图片实际目录（Linux 用 /，Windows 用 \\ 或 /）
```

涉及文件（改完重启 Tomcat）：

| 位置 | 说明 |
| --- | --- |
| `web3_project/admin_u/WEB-INF/classes/config/system.properties` | 预编译版后台（当前是 `schw.xyz` / `/schw`） |
| `web3_project/data_u/WEB-INF/classes/config/system.properties` | 预编译版数据/WS 应用（同样有 admin_url/web_url） |
| `源代码/admin/src/main/java/config/system.properties`、`源代码/admin/target/classes/...` | 源码版后台（默认 `127.0.0.1:8080/admin`） |
| `源代码/data/src/main/java/config/...`、`.../src/main/webapp/WEB-INF/classes/...`、`target/classes/...` | 源码版数据应用 |
| `源代码/api/src/main/webapp/WEB-INF/classes/...`、`.../target/classes/...` | 源码版 api（主要是 web_url/http.server.host） |

### 3.4 域名一致性对照表（部署前逐项核对）

| 项 | 取值要求 | 在哪里配 |
| --- | --- | --- |
| 站点域名（PC/WAP/API/WS 同域） | `https://vexus.io` | Nginx `server_name` + 证书 |
| PC 前端 WebSocket | `wss://vexus.io/data/websocket` | 补丁脚本自动改（`js/app.*.js`） |
| WAP 前端 WebSocket | `wss://vexus.io/data/websocket` | 补丁脚本自动改（`wap/js/app.*.js`） |
| 前端接口 | 相对路径 `/api/...`（前端不用改） | Nginx `/api/` → Tomcat `/web/api/` |
| 后端 `admin_url` | `https://vexus.io/admin`（源码版）/ `https://vexus.io/schw`（web3 版） | `system.properties` |
| 后端 `web_url` | `https://vexus.io/wap` | `system.properties` |
| 后端 `http.server.host` | `https://vexus.io/images/` | `system.properties` |
| 后端 `images.dir` | `/www/wwwroot/vexus/images/` | `system.properties` |
| 证书 | `vexus.io` + `www.vexus.io`（或通配符） | certbot/宝塔 |

> 口诀：**Nginx 的域名 = 前端补丁的域名 = system.properties 的域名**，三者必须一致。
> 补丁脚本默认占位域名 `your-domain.com`，正式发布前必须用真实域名重跑并重新上传，否则 WS 连不上、注册跳转 404。

### 3.5 部署后强制刷新（浏览器 / CDN）

- 前端入口文件名是 webpack contenthash（如 `app.71218f6a.js`），但我们**改内容没改名**，浏览器/CDN 可能仍缓存旧文件（旧文件里还是 `kelubit.id`）。
- 处理顺序：
  1. 服务器端 `rsync` 完成后，**刷新 CDN**（宝塔 CDN / Cloudflare 清缓存 / 阿里云 CDN 刷新目录 `https://域名/js/`、`https://域名/wap/js/`）；
  2. 浏览器**强刷**：Windows `Ctrl+F5` / `Ctrl+Shift+R`，macOS `Cmd+Shift+R`；或开无痕窗口验证；
  3. 若仍不生效，查看 Network 面板 `app.*.js` 状态是否为 `304/from disk cache`，确认响应头 `Cache-Control`；
  4. 长期建议：每次部署后在 `index.html` / `wap/index.html` 的入口脚本引用加版本号（无需改文件名）：
     ```bash
     sed -i 's#/js/app\.\([a-f0-9]*\)\.js#/js/app.\1.js?v=20261114#; s#/js/chunk-vendors\.\([a-f0-9]*\)\.js#/js/chunk-vendors.\1.js?v=20261114#' \
       /www/wwwroot/vexus/site/index.html
     sed -i 's#/wap/js/app\.\([a-f0-9]*\)\.js#/wap/js/app.\1.js?v=20261114#; s#/wap/js/chunk-vendors\.\([a-f0-9]*\)\.js#/wap/js/chunk-vendors.\1.js?v=20261114#' \
       /www/wwwroot/vexus/site/wap/index.html
     ```
     （`vexus-single-domain.conf` 里已对入口 js/css 先设 `expires 1h`，确认线上生效后可删除该段恢复 30d 长缓存。）
- 同理：换域名/重新打补丁后，除了重新上传 `zcx`，还要提醒用户清缓存，否则老用户仍拿着旧 JS 连卖家 WS。

---

## 4. Nginx 安装 / 启用 / HTTPS

### 4.1 安装与基础检查

```bash
# Ubuntu/Debian
apt update && apt install -y nginx
# CentOS/Rocky（dnf）
dnf install -y nginx
# 宝塔面板：软件商店 → 安装 Nginx（编译版），以下命令同样适用

nginx -v            # 版本（建议 >= 1.20；1.25+ 的 http2 写法见 4.2 注释）
systemctl enable --now nginx
```

### 4.2 启用站点配置（单域名主方案）

1. 用 [vexus-single-domain.conf](../deploy/nginx/vexus-single-domain.conf)，把其中占位符替换成真实值：
   - `your-domain.com` → 你的域名（`server_name`、证书路径）
   - `/www/wwwroot/vexus/site` → 前端静态目录
   - `/www/wwwroot/vexus/images` → 上传目录
   - `/www/wwwroot/vexus/logs` → 日志目录
2. 放置：
   - 源码 nginx：`cp vexus-single-domain.conf /etc/nginx/conf.d/vexus.conf`（`nginx.conf` 的 `http{}` 会 include `conf.d/*.conf`，文件顶部的 `map` 才能生效）；
   - 宝塔：面板 → 网站 → 设置 → 配置文件，用该文件的两个 `server{}` 块替换默认内容（宝塔站点配置本身就在 `http{}` 内，`map` 可用）。
3. `nginx -t` 通过后 `systemctl reload nginx`（宝塔：面板 → 软件商店 → Nginx → 重载配置）。

> nginx ≥ 1.25.1：`listen 443 ssl http2;` 会提示 deprecated，改成 `listen 443 ssl;` + `http2 on;` 即可（配置注释里已写）。

### 4.3 HTTPS 证书

**方式 1：certbot（Let's Encrypt，推荐）**

```bash
apt install -y certbot python3-certbot-nginx

# 自动改 nginx 并加 80→443 跳转（会读走你的 server_name）
certbot --nginx -d vexus.io -d www.vexus.io --redirect

# 或 webroot 模式（不想让 certbot 改配置）：
certbot certonly --webroot -w /www/wwwroot/vexus/site -d vexus.io -d www.vexus.io

# 续期检查 + 续期（certbot 自带 systemd timer，一般无需手工）
certbot renew --dry-run
certbot renew --deploy-hook "systemctl reload nginx"
```

证书路径：`/etc/letsencrypt/live/vexus.io/fullchain.pem`、`privkey.pem`（写进 443 server 块）。
完整 SSL 参数（TLS1.2/1.3、套件、会话缓存、OCSP、HSTS）见 [vexus-https-ssl.conf](../deploy/nginx/vexus-https-ssl.conf)，可直接粘到 443 server 或用 `include`。

**方式 2：宝塔面板**

1. 网站 → 添加站点（域名填 `vexus.io`、`www.vexus.io`，根目录 `/www/wwwroot/vexus/site`，PHP 选“纯静态”）；
2. 网站 → 设置 → SSL → Let's Encrypt → 勾选域名 → 申请（自动续期）；
3. 网站 → 设置 → 配置文件，把默认内容替换为 `vexus-single-domain.conf` 的 server 块（证书路径改宝塔路径 `/www/server/panel/vhost/cert/vexus.io/...`）；
4. 保存后宝塔会自动 `nginx -t && reload`。

**方式 3：acme.sh**

```bash
curl https://get.acme.sh | sh -s email=admin@vexus.io
acme.sh --issue -d vexus.io -d www.vexus.io -w /www/wwwroot/vexus/site
acme.sh --install-cert -d vexus.io \
  --key-file       /etc/nginx/certs/vexus.io/key.pem \
  --fullchain-file /etc/nginx/certs/vexus.io/fullchain.pem \
  --reloadcmd      "systemctl reload nginx"
```

### 4.4 反向代理映射（与后端 context 对应，务必理解）

| 外部 URL | Nginx location | 反代到（同一 Tomcat :8080） | 后端应用 context |
| --- | --- | --- | --- |
| `/api/item!list.action` | `^~ /api/` | `http://127.0.0.1:8080/web/api/item!list.action` | api = `/web` |
| `/wap/api/realtime!execute.action` | `^~ /wap/api/` | `http://127.0.0.1:8080/web/api/realtime!execute.action` | api = `/web` |
| `/wap/public/uploadimg!execute.action`、`/public/...` | `^~ /wap/public/`、`^~ /public/` | `http://127.0.0.1:8080/web/public/...` | api = `/web` |
| `wss://域名/data/websocket/1/BTC_USDT` | `^~ /data/` | `http://127.0.0.1:8080/data/websocket/1/BTC_USDT`（带 Upgrade 头） | data = `/data` |
| `/admin/login.jsp` | `^~ /admin/` | `http://127.0.0.1:8080/admin/login.jsp` | admin = `/admin` |
| `/images/xxx.png` | `^~ /images/` | 本地 alias `/www/wwwroot/vexus/images/` | — |
| `/symbol/btc.png` | `^~ /symbol/` | 本地 `site/symbol/` | — |

要点：
- **api 应用 context 必须是 `/web`**（旧版 RMI 要求非空 context），控制器写死 `/api/...`，所以 `proxy_pass` 带 `/web/api/`；把 api 改成 ROOT 需要改控制器或去 RMI，不推荐。
- `/wap/api/`、`/wap/public/`、`/public/` 的 `^~` 优先级高于 `/wap/` 静态，保证不会被静态文件抢走；同时前端补丁已把 `zcx/wap/api/` 假数据目录整体排除。
- 后台两种版本：**源码版 `/admin`**（本文主方案）；**web3 预编译版**把 `system.properties` 的 `admin_url` 设为 `https://域名/schw`，Nginx 用主配置里注释的 `/schw/` 段，二选一不要同时开。
- 高并发时建议把 4 处 `proxy_pass` 改成 `upstream vexus_tomcat { server 127.0.0.1:8080; keepalive 32; }`（主配置注释里有模板）并配 `proxy_set_header Connection ""`。

多域名/子域分离（后台独立域、API 子域、WS 子域）见 [vexus-multi-domain.conf](../deploy/nginx/vexus-multi-domain.conf)：
- 后台独立子域最推荐（前端没有指向后台的硬编码）；
- api 子域只能给外部集成用，主站仍要保留 `/api/`（前端接口是相对路径）；
- WS 拆子域需要手工把 2 个 js 里的 `wss://` 改成子域，或改补丁参数后重打。

---

## 5. 后台与图片目录

- 后台入口：源码版 `https://域名/admin/login.jsp`；预编译版 `https://域名/schw/login.jsp`。
- 上传图片实际落在 `images.dir`（Linux 必须用绝对路径 `/www/wwwroot/vexus/images/`，末尾带 `/`），
  对外 URL 前缀是 `http.server.host`（`https://域名/images/`）。
- 安全检查（强烈建议，详见 `05-安全加固与账号.md`）：
  - 后台 location 加 IP 白名单：`allow 你的IP; deny all;`；
  - 不要对公网开放 `/admin` 的搜索引擎收录，建议加 `X-Robots-Tag: noindex`；
  - 修改默认管理员账号口令、数据库口令、`encryption.key`；
  - Tomcat 隐藏版本号（`server.xml` 的 `Connector server` 属性）。

---

## 6. 验证清单（部署后逐条执行）

### 6.1 前端静态

```bash
# PC 首页 200；HTML 里标题是 Vexus，且不含卖家广告/域名
curl -I  https://vexus.io/
curl -s  https://vexus.io/ | grep -nE '<title>|365ym|kelubit|cdnjs|block\.js'
#   ↑ 期望只看到 <title>Vexus</title>，其余 0 行

# WAP 首页
curl -I  https://vexus.io/wap/

# 入口 js 里 WS 已改成新域名
curl -s https://vexus.io/js/app.71218f6a.js | grep -o 'wss://[^"]*'
curl -s https://vexus.io/wap/js/app.b1f9f5578f88f56a.js | grep -o 'wss://[^"]*'
#   ↑ 期望 wss://vexus.io/data/websocket
```

### 6.2 接口 / 上传 / 后台

```bash
# 对外 API（注意 .action 保留、返回 JSON 而不是 404/502）
curl -s  'https://vexus.io/api/item!list.action' | head -c 300; echo

# 后台 JSP 专用路径
curl -s  'https://vexus.io/wap/api/item!list.action' | head -c 300; echo

# 上传接口存在性（GET 一般会提示方法/参数错误，但不应 404/502）
curl -I 'https://vexus.io/wap/public/uploadimg!execute.action'

# 后台登录页 200
curl -I  https://vexus.io/admin/login.jsp
#   ↑ 期望 HTTP/1.1 200（302 到登录页也算正常）；502 说明 Tomcat 未起或 context 不对

# 图片
curl -I  https://vexus.io/images/已存在的图片.png
```

### 6.3 WebSocket 握手（重点）

```bash
# 期望：HTTP/1.1 101 Switching Protocols（若返回 400/200/403/502，见第 7 章排错）
curl -i -N --max-time 5 \
  -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  -H "Sec-WebSocket-Version: 13" \
  -H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" \
  -H "Host: vexus.io" \
  "https://vexus.io/data/websocket/1/BTC_USDT"
```

浏览器验证：打开 PC 首页 → F12 → Network → WS → 应看到 `wss://vexus.io/data/websocket/1/BTC_USDT` 状态 101；
下单/行情自动刷新正常即说明 WS 通。

### 6.4 HTTPS / 证书

```bash
openssl s_client -connect vexus.io:443 -servername vexus.io </dev/null 2>/dev/null \
  | openssl x509 -noout -issuer -subject -dates
# 期望 issuer=Let's Encrypt / 宝塔证书，日期未过期
curl -I http://vexus.io/    # 期望 301 到 https
```

### 6.5 前端 0 命中验收（发布前必做，含 WAP 的 wss）

在仓库根目录执行，**期望无输出 / 0**：

```bash
grep -rIl -E 'kelubit|anedhaude|hfjdshkd|cdhvjfgbs' deploy/frontend/zcx   # 无输出
grep -RIs -o kelubit deploy/frontend/zcx | wc -l                          # 0
grep -RIn 'wss://' deploy/frontend/zcx/js deploy/frontend/zcx/wap/js | grep -v vexus.io   # 无输出
grep -rIl -E '365ym\.com|cdnjs\.cloudflare\.com|/block\.js' deploy/frontend/zcx           # 无输出
test -d deploy/frontend/zcx/wap/api && echo 'wap/api 仍存在!' || echo 'OK'
```

> 换真实域名后务必重跑一次补丁并复验；`patch-report.json` 里 `kelubitZeroHit=true`、`wsAssertionPassed=true` 才算通过。

---

## 7. 常见问题排查

### 7.1 首页 404 / 静态资源 404

| 原因 | 处理 |
| --- | --- |
| `root` 指到 `zcx` 目录本身（`site/zcx/index.html`） | `root /www/wwwroot/vexus/site;`，把 zcx 的**内容**放进去 |
| 上传时多套了一层目录 | `ls /www/wwwroot/vexus/site` 必须直接看到 `index.html`、`wap/`、`js/` |
| 权限不足（403/404 混淆） | `namei -l /www/wwwroot/vexus/site/index.html`，确保每级目录对 nginx 用户可执行（x） |
| 静态资源 404 但首页正常 | 检查是否被 SPA fallback 吃了：`/images/` 必须 alias 到站点外目录；`/symbol/` 用 `^~` |
| 中文文件名/特殊字符 404 | 本前端产物无中文文件名；自行加图片建议英文名。若必须中文，Nginx 保证 `charset utf-8;` 并确认文件系统编码 |
| `location /` 把 `/wap/` 抢走 | 必须保留 `location ^~ /wap/`（前缀比 `/` 长，天然优先；但 `/wap/api/` 要用 `^~` 单独反代） |

### 7.2 接口 404 / 502

- **404**：先确认 api 应用 context 是 `/web`：本机 `curl -I http://127.0.0.1:8080/web/api/item!list.action`。
  - 本机通、外网 404 → `location ^~ /api/` 的 `proxy_pass http://127.0.0.1:8080/web/api/;` 是否漏了 `/web` 或多了/少了斜杠；
  - 本机也 404 → Tomcat 里 api 应用 context 配置不对（看 `server.xml`/`Catalina/localhost/web.xml`）。
- **502**：Tomcat 没起 → `systemctl status tomcat`、`ss -lntp | grep 8080`；
  - 端口被防火墙挡（Nginx 与 Tomcat 同机一般不需要开 8080 公网）；
  - `proxy_pass` 指向 127.0.0.1 但 Tomcat 只监听 IPv6/其他地址；
  - Tomcat 应用启动失败（看 `catalina.out`，常见是数据库/Redis 连接失败）。
- **504**：接口慢，调 `proxy_read_timeout`；同时排查后端慢 SQL。
- **`.action` 被吃掉**：`proxy_pass` 末尾带 `/web/api/` 时路径会被拼接，不会丢 `.action`；若用了 `rewrite ... break` 反而可能丢，建议保持本配置写法。

### 7.3 WebSocket 101 失败 / 连上就断

| 现象 | 排查 |
| --- | --- |
| `curl` 返回 200/400 而不是 101 | `/data/` 是否 `proxy_http_version 1.1;` + `Upgrade`/`Connection` 头；`map` 是否生效（`Connection` 应为 `upgrade`） |
| 101 后 60 秒断开 | `proxy_read_timeout`（默认 60s）→ 本配置 3600s；同时 CDN/WAF 的空闲超时也要调大 |
| 502 on WS | Tomcat 的 `javax.websocket` 支持是否完整（`tomcat-embed-websocket`/`javax.websocket-api`）；`ServerEndpointExporter` 是否被 Spring 扫描到 |
| 行情不动但 WS 101 | `wss://` 地址是否为新域名（`curl -s .../app.*.js | grep wss`）；浏览器 Console 是否报 mixed content / 证书错误 |
| 连接数暴涨 | 前端断线重连逻辑 + `proxy_read_timeout` 配合；检查是否有多个页面同时建 WS |

### 7.4 跨域（CORS）

- 单域名方案：前端所有请求都是相对路径 `/api/...`，**同源，不需要 CORS**；若浏览器报 CORS，说明请求被打到了别的域名/端口（检查 `HOST_URL` 配置、反向代理是否带错 Host）。
- 只有「前端直连 api 子域」或「独立管理后台前端」才需要 CORS：在对应 server 块加 `Access-Control-Allow-Origin` 等并处理 OPTIONS（见 `vexus-multi-domain.conf` 注释）。
- WS 不需要 CORS，但 `Origin` 校验由后端控制，异常时看后端日志。

### 7.5 缓存旧 JS（改了没生效）

见 3.5：CDN 刷新 + 浏览器强刷 + 入口加 `?v=`；Nginx 对入口 js 先 `expires 1h`。
若用户反馈「还是旧页面/连不上行情」，多半就是他本地缓存了旧 `app.*.js`——让用户强刷即可，也可以临时把入口 js 的 `Cache-Control` 改为 `no-store` 一两天。

### 7.6 上传 413 / 大文件失败

- Nginx：`client_max_body_size 20m;`（本配置已加在 server 级，`/public/`、`/wap/public/`、`/admin/` 也单独写了）；
- 宝塔面板：网站 → 设置 → 配置文件同样要有；面板「上传限制」也要放开；
- Tomcat：`server.xml` 的 `Connector maxPostSize`（默认 2MB，可能需调大）、`maxSwallowSize`；
- 后端 Spring：`MultipartResolver` 的 `maxUploadSize`；
- 前端 KYC 图片较大时，建议 20m~50m，并注意磁盘空间。

### 7.7 其它

| 现象 | 处理 |
| --- | --- |
| `nginx -t` 报 `unknown directive "map"` | 配置没被 include 在 `http{}` 内（放错位置），或文件被某个 server 块内 include |
| 宝塔保存后 403 | 宝塔默认配置有 `location ~ ^/\.user.ini` 等规则，替换成我们配置时保留宝塔的 `.user.ini` 禁读段即可 |
| 页面白屏、Console 报加载 chunk 失败 | 上传不完整（`rsync --delete` 重传）；或 `publicPath` 被改动；或 CDN 回源路径错 |
| 静态文件 403 | 权限/属主不对（644/755），SELinux（`getenforce`）需 `chcon -R -t httpd_sys_content_t /www/wwwroot/vexus/site` |
| 时间/时区不对 | 容器/服务器时区，`timedatectl set-timezone Asia/Shanghai`；前端时间格式化依赖浏览器时区 |

---

## 8. 上线检查表（前端 & Nginx 部分）

- [ ] 用**真实域名**重跑补丁脚本（`-Domain 真实域名`），`patch-report.json` 中 `kelubitZeroHit=true`、`wsAssertionPassed=true`
- [ ] 第 6.5 节 0 命中命令全部通过（含 WAP 的 wss）
- [ ] `deploy/frontend/zcx` 内容已 rsync 到 `/www/wwwroot/vexus/site`（`site/index.html`、`site/wap/index.html` 直接存在）
- [ ] `site`、`images`、`logs` 权限正确；`images/` 可写；上传目录不在 site 内
- [ ] `system.properties` 三处（`admin_url`/`web_url`/`http.server.host`/`images.dir`）已改成新域名，Tomcat 已重启
- [ ] Nginx 占位符全部替换，`nginx -t` 通过，`systemctl reload nginx`
- [ ] 证书申请成功且自动续期；`http → https` 301；HSTS 按需开启
- [ ] `curl -I https://域名/` = 200；`/wap/` = 200；`/api/item!list.action` 返回 JSON
- [ ] `/admin/login.jsp`（源码版）或 `/schw/login.jsp`（预编译版）= 200/302
- [ ] WS 握手返回 `101 Switching Protocols`，PC/WAP 行情与深度正常刷新
- [ ] 浏览器强刷/无痕验证：标题 Vexus、无 365ym 广告、注册跳转新域名、WS 连新域名
- [ ] CDN（如有）已刷新 `js/`、`wap/js/` 目录
- [ ] 后台 `/admin` 已加 IP 白名单（按 `05-安全加固与账号.md`）

---

## 9. 附：文件与路径速查

| 用途 | 路径 |
| --- | --- |
| PC 入口 HTML | `/www/wwwroot/vexus/site/index.html` |
| WAP 入口 HTML | `/www/wwwroot/vexus/site/wap/index.html` |
| PC 入口 JS（含 WS） | `site/js/app.71218f6a.js` |
| WAP 入口 JS（含 WS） | `site/wap/js/app.b1f9f5578f88f56a.js` |
| PC 注册跳转 JS | `site/js/chunk-91e00e56.ea6ad434.js` |
| 后端配置（源码版） | `源代码/admin|api|data/src/main/java/config/system.properties` 及 `target/classes` / `src/main/webapp/WEB-INF/classes` |
| 后端配置（预编译版） | `web3_project/admin_u/WEB-INF/classes/config/system.properties`、`web3_project/data_u/...` |
| 前端补丁 | `deploy/frontend/patch-frontend.ps1` / `.sh` |
| 补丁报告 | `deploy/frontend/patch-report.json`、`deploy/frontend/CHANGES.md` |
| Nginx 配置 | `deploy/nginx/vexus-single-domain.conf`、`vexus-https-ssl.conf`、`vexus-multi-domain.conf` |

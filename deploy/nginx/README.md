# Nginx 配置目录说明（Vexus 交易所）

本目录给的是**可直接改域名使用**的 Nginx 配置，覆盖前端静态 + API/上传/WS/后台反代 + HTTPS。
所有配置里只出现占位符 `your-domain.com`，**不含任何卖家/第三方真实域名**（补丁前的旧域名一律以占位符替代）。

| 文件 | 用途 |
| --- | --- |
| `vexus-single-domain.conf` | **主方案**：单域名。80→443、PC/WAP 静态、`/api/`→`/web/api/`、`/wap/api/`、`/wap/public/`、`/public/`、`/data/` WebSocket、`/admin/`、`/images/`、`/symbol/`、缓存/gzip/安全头 |
| `vexus-https-ssl.conf` | HTTPS 参数片段：证书路径（certbot/宝塔）、TLS 协议套件、会话缓存、OCSP、HSTS |
| `vexus-multi-domain.conf` | 可选：把后台/API/WS 拆到独立子域；含与单域方案的差异说明 |
| `README.md` | 本文件：使用步骤、后端 context 约定、验证命令 |

## 1. 后端 Tomcat 部署约定（非常关键）

三个应用在**同一个 Tomcat**（默认 `127.0.0.1:8080`）：

| 应用 | context path | 对外入口 | Nginx 映射 |
| --- | --- | --- | --- |
| admin（后台） | `/admin` | `https://your-domain.com/admin/login.jsp` | `/admin/` → `/admin/` |
| api（接口） | `/web` | `https://your-domain.com/api/item!list.action` | `/api/` → `/web/api/`（保留 `.action`） |
| data（行情/WS） | `/data` | `wss://your-domain.com/data/websocket/1/BTC_USDT` | `/data/` → `/data/` + Upgrade 头 |

- api 用 `/web`（不是 ROOT）的原因：旧版 RMI 要求 api webapp 的 context path 非空；
  api 控制器 `@RequestMapping` 写死为 `/api/xxx!yyy.action`，所以外部 `/api/` 必须替换成 Tomcat 的 `/web/api/`。
- 如果把 api 改成 ROOT context，需要同步改控制器 mapping 或去掉 RMI，**不推荐**。
- web3 预编译版的配置是 `admin_url=https://<旧域名>/schw`，本目录按**源码版 `/admin`** 编写；
  若部署预编译版，请启用主配置里注释掉的 `/schw/` 段并同步改 `system.properties`。

## 2. 使用步骤（单域名）

```bash
# 0) 准备目录
mkdir -p /www/wwwroot/vexus/{site,images,logs}
chown -R www:www /www/wwwroot/vexus            # 宝塔用户一般是 www:www；nginx 裸装机是 nginx:nginx
chmod -R 755 /www/wwwroot/vexus/site
chmod -R 775 /www/wwwroot/vexus/images          # 后台上传目录需要可写（或属主给 Tomcat 运行用户）

# 1) 上传前端产物（deploy/frontend/zcx 的内容）到 /www/wwwroot/vexus/site
#    注意是“内容”，即 site/index.html、site/wap/index.html、site/js/... 这样
rsync -av --delete deploy/frontend/zcx/ /www/wwwroot/vexus/site/

# 2) 修改配置里的占位符（3 处）
sed -i 's#your-domain.com#你的真实域名#g' vexus-single-domain.conf

# 3) 放到 nginx（二选一）
cp vexus-single-domain.conf /etc/nginx/conf.d/vexus.conf                   # 源码 nginx
# 宝塔：面板 → 网站 → 设置 → 配置文件，用本文件的两个 server 块替换默认内容

# 4) 申请证书（二选一，详见 vexus-https-ssl.conf）
certbot --nginx -d 你的真实域名 -d www.你的真实域名 --redirect
# 或宝塔：网站 → SSL → Let's Encrypt

# 5) 语法检查 + 重载
nginx -t && systemctl reload nginx
```

> HTTPS 首次申请前，80 server 块的 `/.well-known/acme-challenge/` 已经指向 `site` 目录，
> 因此 certbot 也可以用 webroot 模式：`certbot certonly --webroot -w /www/wwwroot/vexus/site -d 域名`。

## 3. 验证命令（部署后逐条执行）

```bash
# 首页 200 且返回的是补丁后的 HTML（标题 Vexus，不含卖家广告/旧域名）
curl -I  https://your-domain.com/
curl -s  https://your-domain.com/ | grep -nE '<title>'
# 完整的“0 命中”检查命令见 docs/04-前端与Nginx.md §6.5 与 deploy/frontend/CHANGES.md §8

# WAP 首页
curl -I  https://your-domain.com/wap/

# 接口（api context=/web 映射是否生效），期望返回 JSON 而不是 404/502
curl -s  'https://your-domain.com/api/item!list.action' | head -c 300; echo
curl -s  'https://your-domain.com/wap/api/item!list.action' | head -c 300; echo

# WebSocket 握手：期望 HTTP/1.1 101 Switching Protocols（不带 101 说明 Upgrade 头/反代有问题）
curl -i -N \
  -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  -H "Sec-WebSocket-Version: 13" \
  -H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" \
  -H "Host: your-domain.com" \
  "https://your-domain.com/data/websocket/1/BTC_USDT" --max-time 5

# 后台（源码版 /admin；预编译版换 /schw/login.jsp）
curl -I https://your-domain.com/admin/login.jsp

# 上传目录（如果后台已经传过图片）
curl -I https://your-domain.com/images/某个已存在的图片.png

# 证书与 TLS
openssl s_client -connect your-domain.com:443 -servername your-domain.com </dev/null 2>/dev/null | openssl x509 -noout -issuer -dates
```

## 4. 常见问题

| 现象 | 排查 |
| --- | --- |
| 首页 404 | `root` 是否指到 site 目录；产物是否把 `zcx` 目录整体放进去了（错误：`site/zcx/index.html`） |
| `/api/xxx!action` 404 | api 应用 context 是否是 `/web`；`proxy_pass` 是否带 `/web/api/`（丢了 `/web` 会 404） |
| 登录/注册跳回卖家域名 | 前端补丁没重跑或没换真实域名：重新跑 `patch-frontend.ps1 -Domain 真实域名` 并重新上传 |
| 502 | Tomcat 未启动 / 端口不是 8080 / `systemctl status tomcat`；`curl -I http://127.0.0.1:8080/admin/login.jsp` 在本机确认 |
| WS 101 失败（连接被关/超时） | `/data/` 是否配了 `Upgrade`/`Connection`；`proxy_read_timeout` 是否过短；Tomcat 是否支持 JSR-356（`javax.websocket-api`） |
| 行情 60s 后断开 | `proxy_read_timeout 3600s`（本配置已给）；CDN/WAF 也有空闲超时，需一起调大 |
| 上传 413 | `client_max_body_size 20m` 位于 server 级，确认没有被 location 覆盖；PHP/网关层另有大小限制也要调 |
| 新 JS 不生效/仍是旧域名 | 入口 js 文件名未变：清 CDN 缓存 + 浏览器强刷（Ctrl/Cmd+Shift+R），或按 `docs/04` 给入口 js 加 `?v=` 版本号 |
| 静态资源中文/特殊字符 404 | 确认文件名 UTF-8；本前端产物内无中文文件名，若自行添加图片建议用英文名 |
| 跨域报错 | 单域名方案前端全部相对路径，不需要 CORS；只有拆 api 子域才需要，见 `vexus-multi-domain.conf` 注释 |
| `nginx -t` 报 `duplicate listen options` / `http2` 警告 | nginx ≥1.25.1 用 `listen 443 ssl;` + `http2 on;` 替代 `listen 443 ssl http2;` |

## 5. 与文档的关系

- 完整部署流程、目录规划、证书申请、验证与排错：`docs/04-前端与Nginx.md`
- 前端补丁做了什么、0 命中复验：`deploy/frontend/CHANGES.md`
- 安全加固（后台 IP 白名单、Tomcat 隐藏版本、账号口令等）：`docs/05-安全加固与账号.md`

# deploy/ 交付物索引

> 配套主文档：`../部署教程.md`；安全审计：`../docs/05-安全加固与账号.md`；
> **后门清除记录：`../docs/06-后门清除记录.md`**。
> **默认约定**：域名 `your-domain.com`；Tomcat 三应用 context = `admin` / `web`（api）/ `data`；
> Nginx 外网 `/api/` → Tomcat `/web/api/`。
>
> ✅ 2026-11-15：`war/`、`frontend/zcx`、`源代码`、`数据库/dkewl.sql` 均已清除已知后门；
> `web3_project` 已移出交付包。使用旧备份重新构建/导库会复活后门，见 docs/06。

| 目录/文件 | 用途 | 使用方式 |
|---|---|---|
| `war/admin.war` | 管理后台 WAR（含 normal 主题、清理后配置） | 改名/解压到 `webapps/admin` |
| `war/api.war` | 前台 API WAR | **必须部署为 context `/web`**：`webapps/web` |
| `war/data.war` | 行情/WebSocket/定时任务 WAR | `webapps/data` |
| `war/MANIFEST.md` | 大小、条目数、校验命令、0 命中复验结果、未验证项 | 上传前先读 |
| `build/build-war.ps1` | Windows 下从 `源代码/` 重建 3 个 WAR（含后门排除、配置脱敏、basePath 补丁） | `pwsh .\deploy\build\build-war.ps1` |
| `build/build-war.sh` | Linux 下重建（服务器上执行） | `bash deploy/build/build-war.sh` |
| `config/` | 三应用配置模板（jdbc/redis/域名/图片/日志） | 与 `WEB-INF/classes` 对照修改 |
| `sql/import-db.sh` | MySQL 导入 + 校验脚本 | `bash deploy/sql/import-db.sh /root/dkewl.sql` |
| `sql/security-cleanup.sql` | **上线前必执行**：删隐藏超管、清万能 2FA/外部通道/私钥行 | 导库后、首次启动前，先备份 |
| `sql/schema-compat-report.md` | 旧源码版 JDBC 实体/手写 SQL 与 `dkewl.sql` 的兼容性报告 | 决定是否需要 ALTER |
| `tools/DbPassword.ps1` / `db_password.py` / `db_password.sh` | DB 密码 3DES 加解密（三平台） | `... encrypt '你的密码'`，把输出填进 `jdbc.password` |
| `tools/README.md` | 工具用法与自测样例 | 先自测解密 `Err;2[eoGFwdgr...` |
| `frontend/zcx/` | 已打补丁的前端静态产物（PC + WAP） | 原样上传到 `/www/wwwroot/vexus/site` |
| `frontend/patch-frontend.ps1` / `.sh` | 从原始 zcx 重新生成补丁包（含 0 命中断言） | 改域名时重跑 |
| `frontend/CHANGES.md` | 补丁改动清单与复验命令 | 发布前复验 |
| `nginx/vexus-single-domain.conf` | 单域名 Nginx 主配置（80/443、静态、反代、WS） | 替换域名/证书路径后启用 |
| `nginx/vexus-https-ssl.conf` | SSL 片段（certbot 用，可选） | include |
| `nginx/vexus-multi-domain.conf` | 多子域方案（可选） | 参考 |

## 最短路径（已有 Linux 服务器时）

```bash
# 0) 本机把整个项目目录上传到服务器 /root/vexus-src
# 1) 按 部署教程.md 第 2 节安装 JDK8/Tomcat9/MySQL/Redis/Nginx
#    ⚠️ MySQL 必须先设 lower_case_table_names=1（MySQL8 要在 initialize 前设，见教程 2.3）
# 2) 导库
bash deploy/sql/import-db.sh /root/dkewl.sql
# 2.1) 源码版补 schema（缺 1 表 9 列，不补会报 Unknown column）
mysql -uroot --default-character-set=utf8 jiaoyipan < deploy/sql/patch-old-source-schema.sql
# 2.2) 建自己的超管（不要用 root，SQL 见 部署教程.md 10.1），再执行安全清理（先备份！）
mysql -uroot --default-character-set=utf8 jiaoyipan < deploy/sql/security-cleanup.sql
# 3) 生成 DB 密码密文（填写到三套 config.properties）
python3 deploy/tools/db_password.py encrypt '你的强密码'
# 4) 部署 WAR（解压成目录；server.xml 用显式 <Context> 固定顺序 admin→web→data）
mkdir -p /opt/tomcat9/webapps
unzip -q deploy/war/admin.war -d /opt/tomcat9/webapps/admin
unzip -q deploy/war/api.war   -d /opt/tomcat9/webapps/web
unzip -q deploy/war/data.war  -d /opt/tomcat9/webapps/data
# 5) 改三套 WEB-INF/classes 配置（docs/03、部署教程.md 第 5 节）
# 6) 启动
systemctl restart vexus-tomcat
# 7) 前端 + Nginx
rsync -a --delete deploy/frontend/zcx/ /www/wwwroot/vexus/site/
cp deploy/nginx/vexus-single-domain.conf /etc/nginx/sites-available/vexus.conf  # 改域名
# 8) 验收（部署教程.md 第 9 节）
```

> **两个必须遵守的部署约束**（本机实测确认）：
> 1. `server.xml` 的 `<Host>` 要关闭自动部署，显式写 3 个 `<Context>`，顺序固定
>    `admin → web → data`；否则 data 启动时会报
>    `RMI-Client: Not Implemented Remote Interface: project.c2c.C2cTranslateService`。
> 2. api 的 context 必须是 `/web`，不能是 `/api`（URL 会变 `/api/api/...`）或 ROOT（旧 RMI NPE）；
>    Nginx 把外网 `/api/` 反代到 `/web/api/`。

> 上传 WAR 前务必用 `war/MANIFEST.md` 里的“0 命中”命令复验，确认没有后门类。

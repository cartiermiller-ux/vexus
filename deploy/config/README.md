# deploy/config —— 三端配置文件模板与映射说明

Vexus 的配置分散在 **两个层次**，且 **admin / data / api 三端各有一份**：

1. **`classes/config.properties`（根）**：Spring `<context:property-placeholder location="classpath:config.properties"/>`
   加载，包含 `jdbc.*`、`redis.address`、`dubbo.*`、`images.dir`、`hibernate.*`、`email.*`。
   → **数据库/Redis/Dubbo 连接就看这一份**。
2. **`classes/config/*.properties`**：由各自的 `PropertiesUtil*` 类用 `classpath:config/xxx.properties` 读取，
   包括 `system.properties`（域名/图片/前后台地址）、`redis.properties`（**真正生效的 Redis 配置**）、
   `db.properties`（数据库备份模块）、`wallet.properties`、`email.properties`、`sms253.properties`、
   `cms.properties`、`auto_monitor.properties`、`autoe2due.properties`。

| 模板文件 | 目标文件名 | 目标位置（每端一份） | 必改？ |
| --- | --- | --- | --- |
| `config.properties.template` | `config.properties` | `web3_project/{admin_u,data_u}/WEB-INF/classes/`（根）<br>`源代码/{admin,api,data}/src/main/resources/`（+ `target/classes/`） | ✅ jdbc.url/username/password、dubbo.registry.address、images.dir |
| `system.properties.template` | `config/system.properties` | 同上各端 `classes/config/`（api 为 `WEB-INF/classes/config/`） | ✅ http.server.host、images.dir、admin_url、web_url |
| `redis.properties.template` | `config/redis.properties` | 同上 | ✅ redis.address |
| `db.properties.template` | `config/db.properties` | 同上 | ⭕ 用后台备份/还原才必改（db.password 也是密文） |
| `wallet.properties.template` | `config/wallet.properties` | 同上 | ⭕ 默认 USD |
| `email.properties.template` | `config/email.properties` | 同上 | ⭕ 发邮件才改 |
| `sms253.properties.template` | `config/sms253.properties` | 同上 | ⭕ 发短信才改（实际通道由 `t_syspara.send_code_type` 决定） |
| `cms.properties.template` | `config/cms.properties` | `admin_u/.../config/`（仅后台有） | ⭕ 默认中文 |
| `auto_monitor.properties.template` | `config/auto_monitor.properties` | `data_u/.../config/`、`dapp/src/config/` | ⭕ 链上监控按机器调线程池 |
| `autoe2due.properties.template` | `config/autoe2due.properties` | 同上 | ⚪ 保持默认 |

## 使用步骤

```bash
# 以 web3 预编译版 admin_u 为例（data_u 同理）
cd web3_project/admin_u/WEB-INF/classes

# 1) 备份原配置（回滚用）
cp -r config.properties config.properties.bak
cp -r config config.bak

# 2) 复制模板并改占位符（<...> 的位置）
#    macOS/Linux: 用 sed 或手工改；Windows: 直接编辑
cp /path/to/deploy/config/config.properties.template config.properties
cp /path/to/deploy/config/system.properties.template config/system.properties
cp /path/to/deploy/config/redis.properties.template config/redis.properties
cp /path/to/deploy/config/db.properties.template config/db.properties

# 3) 生成 jdbc.password 密文（不是明文！）
python3 /path/to/deploy/tools/db_password.py encrypt '你的数据库密码'
# 把输出粘到 config.properties 的 jdbc.password = 后面

# 4) 重启 Tomcat，验证 catalina.out 无连接失败
```

## 注意事项

1. **`jdbc.password` / `db.password` 必须是 3DES 密文**（`LocalDruidDataSource.setPassword()` 启动时会解密），
   明文会连不上；生成工具见 `../tools/`。
2. **Redis 要改 `config/redis.properties`**：`project.redis.interal.Redis` 只从
   `PropertiesUtilRedis`（`classpath:config/redis.properties`）读 `redis.address`；
   根 `config.properties` 里的 `redis.address` 在当前版本无代码读取。
3. **`images.dir` 出现两处**（根 config.properties 与 config/system.properties），两处必须一致且目录可写。
4. **`admin`/`data` 的 `dubbo.application.name` 必须不同**（本包已分别是 `admin` / `data`），
   `data_u` 还要注意 `dubbo.application.port.root` 不能与 admin 冲突。
5. `hibernate.hbm2ddl.auto` 在本版本**不生效**（SessionFactory 里该项被注释、旧源码无 SessionFactory），
   缺表/缺列必须手工 DDL（见 `../sql/schema-compat-report.md`）。
6. 改完 **必须重启** Tomcat（数据源/Redis/Dubbo 都在启动时初始化）。
7. 生产环境别把 `deploy/`、`web3_project/` 暴露在 Web 根目录；配置目录权限 `chmod 600`。

# deploy/sql —— 数据库导入、校验、备份与旧版 schema 兼容补丁

本目录提供「Vexus 交易所」数据库落地的全部 SQL 侧工具与结论，**不修改**原始
`数据库/dkewl.sql`（131,302,296 字节 / 125.2 MiB，MySQL 5.7.34 dump，库名 `jiaoyipan`，129 张表）。

| 文件 | 作用 |
| --- | --- |
| `import-db.sh` | 一键导入 / 校验 / 备份 / 回滚（bash，Git Bash / Linux 均可） |
| `patch-old-source-schema.sql` | 「旧源码版」搭配该 SQL 时需要补的列/表（只跑一次） |
| `security-cleanup.sql` | **上线前必跑**：删隐藏超管 root、万能 2FA、外发通道、遗留私钥、测试账号、卖家域名残留（配套 `docs/05-安全加固与账号.md`） |
| `schema-compat-report.md` | schema 兼容性分析报告（结论：旧源码版不能直接跑，需要打补丁） |
| `README.md` | 本文件 |

> 相关工具：`../tools/`（DB 密码 3DES 加解密）、`../../docs/03-数据库与配置.md`（完整安装与配置说明）。

---

## 1. 快速开始

```bash
cd deploy/sql
chmod +x import-db.sh

# 0) 先看一眼会执行什么（不需要 mysql 客户端也能跑）
./import-db.sh --dry-run

# 1) 正式导入（会先检查客户端/端口/dump/max_allowed_packet，然后建库导入并校验）
DB_PASS='你的root密码' ./import-db.sh

# 2) 只做校验
DB_PASS='...' ./import-db.sh --verify-only

# 3) 备份 / 回滚
DB_PASS='...' ./import-db.sh --backup ./backup/jiaoyipan-before.sql.gz
DB_PASS='...' ./import-db.sh --restore ./backup/jiaoyipan-before.sql.gz --force
```

也可以用环境变量或同目录 `import-db.env` 固定参数：

```bash
# deploy/sql/import-db.env（可选，注意别提交真实密码到公开仓库）
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=jiaoyipan
DB_USER=vexus
DB_PASS='强密码'
```

---

## 2. 手工导入（等价命令，便于排错）

```bash
# 2.1 建库（字符集必须 utf8，与 dump 里的 SET NAMES utf8 / DEFAULT CHARSET=utf8 对齐）
mysql -uroot -p -e "
CREATE DATABASE IF NOT EXISTS \`jiaoyipan\`
  DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"

# 2.2 导入（131MB，约 3~15 分钟；--default-character-set=utf8 不能省）
mysql -uroot -p --default-character-set=utf8 --max-allowed-packet=256M \
      jiaoyipan < "/path/to/数据库/dkewl.sql"

# 2.3 校验
mysql -uroot -p --default-character-set=utf8 -e "
SELECT COUNT(*) AS tables_cnt FROM information_schema.TABLES
 WHERE TABLE_SCHEMA='jiaoyipan';
SELECT COUNT(*) FROM jiaoyipan.t_user;
SELECT COUNT(*) FROM jiaoyipan.t_syspara;
SELECT UUID,USERNAME,ENABLED FROM jiaoyipan.sct_user;"
```

### 导入前服务端参数（`my.cnf` 的 `[mysqld]`，改完重启 MySQL）

```ini
[mysqld]
max_allowed_packet = 256M      # 关键：dump 单条 INSERT 很大，默认 4M/16M 会报错
wait_timeout = 600
interactive_timeout = 600
net_read_timeout = 600
net_write_timeout = 600
innodb_buffer_pool_size = 1G   # 按机器内存调整，建议物理内存的 50%~70%
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2   # 导入期可临时放宽，导完改回 1
character-set-server = utf8
collation-server = utf8_general_ci
```

命令行临时生效（无需重启，重启后失效）：

```sql
SET GLOBAL max_allowed_packet = 268435456;
SET GLOBAL net_read_timeout = 600;
SET GLOBAL net_write_timeout = 600;
```

> MySQL 8.0 也可以导入该 5.7 dump（`int(11)`、`double`、`utf8` 均兼容），
> 但**不要**把库默认字符集改成 `utf8mb4` 后直接导入：dump 内是 `utf8`，
> 混用会出现 4 字节字符/索引长度问题。确实要升 utf8mb4，请导入后单独做字符集迁移。

---

## 3. 校验标准（导入成功的判据）

| 项 | 期望值（本 dump 基线） | 说明 |
| --- | --- | --- |
| 表数量 | **129** | `information_schema.TABLES` |
| 总行数 | 931,140 行左右 | 66 张表有数据，63 张空表 |
| `t_realtime` | 647,082 | 行情快照，最大表 |
| `t_kline` | 271,375 | K 线 |
| `t_contract_order` | 6,310 | 合约订单 |
| `t_user` | 783 | 用户主表 |
| `t_syspara` | 207 | 系统参数 |
| `t_cms` | 164 | CMS 内容 |
| `t_item` | 55 | 交易对/币种 |
| `t_banner` | 41 | 轮播 |
| `t_wallet` / `t_wallet_log` | 12 / 15 | 钱包/流水 |
| `t_exchange_apply_order` | 20 | 币币委托 |
| `pat_party` | 12 | 用户资料 |
| `sct_user` / `sct_role` / `sct_user_role` | 6 / 13 / 14 | 后台账号/角色 |
| `t_withdraw_order` | 6 | 提现单 |
| 管理员 | 存在 `root`（UUID=`SROOT`，角色 `SECURITY_ROLE_ROOT`） | 见 `docs/03` 重置密码章节 |

字符集校验（全部应为 `utf8` / `utf8_general_ci`）：

```sql
SELECT TABLE_COLLATION, COUNT(*) FROM information_schema.TABLES
 WHERE TABLE_SCHEMA='jiaoyipan' GROUP BY TABLE_COLLATION;
```

---

## 4. 失败重试与回滚

**导入中断（客户端断开 / 报错）**——最重要原则：**不要在半成品库上继续导入**，
dump 每条 `CREATE TABLE` 前都有 `DROP TABLE IF EXISTS`，直接重跑通常也能成功，但更稳妥的做法是：

```sql
DROP DATABASE IF EXISTS jiaoyipan;
CREATE DATABASE jiaoyipan DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
```

然后重新执行导入。常见错误与处理：

| 报错 | 原因 | 处理 |
| --- | --- | --- |
| `ERROR 2006 (HY000): MySQL server has gone away` | `max_allowed_packet` 太小 / 超时 | 调大 `max_allowed_packet`、`net_read_timeout` 后重导 |
| `ERROR 1153 (08S01): Got a packet bigger than 'max_allowed_packet' bytes` | 同上 | 同上 |
| `ERROR 1064` / 语法错误 | 客户端字符集不对、dump 被截断 | 用 `--default-character-set=utf8`，确认文件大小 131,302,296 字节 |
| `ERROR 1118` 行太大 | MySQL 8 严格模式 + `utf8mb4` | 库保持 `utf8`；或调 `innodb_strict_mode=OFF` 仅导入期 |
| `ERROR 1045` | 账号密码错 / `root@localhost` 与 `root@127.0.0.1` 授权不同 | 用 `-h127.0.0.1` 或补授权 |
| 中文变成 `????` / 乱码 | 客户端字符集不是 utf8 | 导入命令加 `--default-character-set=utf8`，导入后只能重导修复 |

**回滚**：导入前先备份数据库目录或 `--backup` 出一份；回滚用
`./import-db.sh --restore <备份> --force`。没有备份又导错库时，
只能 `DROP DATABASE jiaoyipan` 后重新导入 dump（dump 本身即基线）。

---

## 5. 与旧源码版的兼容性

见 `schema-compat-report.md`。一句话结论：

> **旧源码版（`源代码/`，Spring JDBC + JPA 注解）不能直接跑在该 SQL 上**：
> 缺 1 张表（`T_USER_WALLET_ADDRESS`）+ 9 个列（`t_wallet.LOCK_MONEY/FREEZE_MONEY`、
> `t_wallet_extend.LOCK_AMOUNT`、`t_withdraw_order.DEVICE_IP/REMARKS`、
> `pat_party.BALANCE_INCOME_CONFIG`、`t_money_log.SHOW_STATUS`、
> `t_userdata.RECHARGE_USDC`、`t_auto_monitor_auto_transfer_from_config.ENABLED_GRANT_OTHER`），
> 另有 2 张 dapp 监控表（`T_LOAN_PARAM`、`T_SIMPLE_LOAN_ORDER`）也不存在。
> 执行一次 `patch-old-source-schema.sql` 后即可启动，核心链路可用（建议回归测试）。

web3 预编译版（`web3_project/admin_u`、`data_u`，Hibernate hbm 映射）与该 SQL
**基本完全匹配**：97 张映射表里 96 张表/列齐全，仅 `T_USER_WALLET_ADDRESS` 缺失（旧功能，可忽略）。

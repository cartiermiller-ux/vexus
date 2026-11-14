# Schema 兼容性报告：旧源码版 / web3 预编译版 × `dkewl.sql`

> 目标：回答「拿到的 `数据库/dkewl.sql`（jiaoyipan 库）能不能直接配这两套代码跑」。
> 结论先行，明细在后。所有列名/行号均来自本工程实际文件，可复核。
>
> - 分析对象 1（旧源码版）：`源代码/`（Spring MVC + **Spring JdbcTemplate 手写 SQL** + JPA `@Table/@Column` 注解做结果集映射）
> - 分析对象 2（web3 预编译版）：`web3_project/admin_u`、`web3_project/data_u`（**Hibernate `*.hbm.xml` 映射** + Spring）
> - 数据库：`数据库/dkewl.sql`，131,302,296 字节，MySQL 5.7.34 dump，库 `jiaoyipan`，**129 张表**、931,140 行数据
>
> 生成时间：2026-11-14 · 分析脚本：PowerShell/正则 + Python（流式解析 131MB dump，未整文件读入内存）

---

## 0. 结论摘要（TL;DR）

### 0.1 直接结论（一句话）

> **旧源码版（`源代码/`：Spring JDBC 手写 SQL + JPA `@Table/@Column` 注解实体，无 hbm）与 `dkewl.sql` 不兼容，不能直接启动。**
> 缺 **1 张核心表 + 9 个列**（另有 2 张 dapp 监控表可选），会导致后台提现列表、用户注册/改资料、钱包锁定/冻结、
> 充值地址、资金流水等功能报 `Unknown column` / `Table doesn't exist`。执行一次
> `patch-old-source-schema.sql`（建 1 表 + 补 9 列）后可用，需回归测试。
>
> web3 预编译版（`web3_project/*`，Hibernate `*.hbm.xml`）**基本完全兼容**：97 张映射表里 96 张表/列齐全，
> 仅 `T_USER_WALLET_ADDRESS` 一张旧功能表缺失。

### 0.2 判定依据

- 旧源码版持久层：`JdbcTemplate` 手写 SQL（1076 个 Java 文件中 72 张 SQL 表被引用）+ `ApplicationUtil.getInsertStatement/getUpdateStatement`
  按实体 `@Column` 反射生成 INSERT/UPDATE（所以实体上多出的列会直接变成非法列名）。
- 逐项比对：93 张 JPA 实体表 / 全部列 × 129 张 `CREATE TABLE` 表 / 列，再对「实体有列、SQL 无列」逐条回查手写 SQL 引用。
- **不兼容清单（完整，共 3 表 + 9 列）**：

| 类型 | 表 | 缺失内容 | 影响功能（有代码引用证据） |
| --- | --- | --- | --- |
| 缺表 | `T_USER_WALLET_ADDRESS` | 整表 | 用户充值地址（`UserWalletAddressServiceImpl`，核心） |
| 缺表 | `T_SIMPLE_LOAN_ORDER` | 整表 | dapp 监控借贷（`LoanServiceImpl`，可选部署） |
| 缺表 | `T_LOAN_PARAM` | 整表 | dapp 借贷参数（可选部署） |
| 缺列 | `t_wallet` | `LOCK_MONEY`、`FREEZE_MONEY` | 钱包锁定/冻结金额（`WalletDaoImpl` 手写 UPDATE） |
| 缺列 | `t_wallet_extend` | `LOCK_AMOUNT` | 钱包扩展锁定金额（`WalletDaoImpl` 手写 UPDATE） |
| 缺列 | `t_withdraw_order` | `DEVICE_IP`、`REMARKS` | 后台提现列表（`AdminWithdrawServiceImpl` 手写 SELECT） |
| 缺列 | `pat_party` | `BALANCE_INCOME_CONFIG` | 用户注册/资料保存（`PartyServiceImpl` 用 ApplicationUtil 生成 INSERT/UPDATE） |
| 缺列 | `t_money_log` | `SHOW_STATUS` | 资金流水记录（`MoneyLog` 实体 + ApplicationUtil） |
| 缺列 | `t_userdata` | `RECHARGE_USDC` | 用户充值统计（`UserData` 实体） |
| 缺列 | `t_auto_monitor_auto_transfer_from_config` | `ENABLED_GRANT_OTHER` | dapp 自动归集配置（可选部署） |

补丁：`patch-old-source-schema.sql`（含执行后自检 SQL）；执行顺序与冒烟测试见第 5 节。

### 0.3 两个版本总览

| 组合 | 结论 | 需要的改动 |
| --- | --- | --- |
| **旧源码版（`源代码/`）+ `dkewl.sql`** | ❌ **不能直接跑**（会报 `Unknown column` / `Table doesn't exist`） | 打补丁：**建 1 张表 + 补 9 个列**（另 2 张 dapp 监控表可选）→ 见 `patch-old-source-schema.sql` |
| web3 预编译版（`web3_project/*`）+ `dkewl.sql` | ✅ **可直接跑**（97 张 hbm 映射表中 96 张完全匹配） | 仅 `T_USER_WALLET_ADDRESS` 表缺失（旧功能；不用该功能可忽略） |

**旧源码版需要的补丁量（详细）**

- 缺失表 **3** 张：
  - `T_USER_WALLET_ADDRESS`（核心，`comm/User` 充值地址功能）
  - `T_LOAN_PARAM`、`T_SIMPLE_LOAN_ORDER`（`dapp` 监控模块，仅 dapp 部署才需要）
- 缺失列 **9** 个 / **7** 张表：`t_wallet`(2)、`t_wallet_extend`(1)、`t_withdraw_order`(2)、`pat_party`(1)、`t_money_log`(1)、`t_userdata`(1)、`t_auto_monitor_auto_transfer_from_config`(1)
- 其中 **3 处手写 SQL 会立即报错**（不用等功能触发、页面一打开就炸）：
  1. 后台「提现列表」`AdminWithdrawServiceImpl` 直接 select `withdraw.REMARKS`、`withdraw.DEVICE_IP`
  2. 钱包变更 `WalletDaoImpl.updateLockAndFreeze()` update `t_wallet.LOCK_MONEY/FREEZE_MONEY`
  3. 用户充值地址 `UserWalletAddressServiceImpl` 直接操作 `T_USER_WALLET_ADDRESS`
- 另有 **2 处实体持久化必踩**：`PartyServiceImpl.save()/update()`（`pat_party.BALANCE_INCOME_CONFIG`）、钱包实体 `ApplicationUtil.getInsertStatement(Wallet)`（`t_wallet.LOCK_MONEY/FREEZE_MONEY`）

补齐上述 DDL 后，旧源码版可以启动并跑通核心链路（用户/钱包/提现/币币/合约），但**仍需回归测试**（见第 6 节「未覆盖项」）。

---

## 1. 两个版本持久层实现完全不同（这是兼容性问题的根因）

| 对比项 | 旧源码版 `源代码/` | web3 预编译版 `web3_project/` |
| --- | --- | --- |
| ORM | **无 Hibernate SessionFactory**；`JdbcTemplate` 手写 SQL | Hibernate 5（`LocalSessionFactoryBean`），`*.hbm.xml` |
| 实体映射 | JPA 注解 `@Table(name=...)` / `@Column(name=...)`，由 `kernel.bo.RecordObjectMapper`（结果集→Bean）、`kernel.web.ApplicationUtil`（Bean→INSERT/UPDATE 语句）反射使用 | `classpath:/project`、`/security`、`/systemuser` 下的 **193 个 hbm.xml**（admin_u + data_u 各一份） |
| 配置位置 | `源代码/*/src/main/resources/spring/applicationContext.xml`（`classpath:config.properties`），无 `mappingDirectoryLocations` | `web3_project/*/WEB-INF/classes/spring/applicationContext.xml`，`mappingDirectoryLocations` 指向上面的 hbm 目录 |
| 自动建表 | 无（`hibernate.hbm2ddl.auto=update` 是残留配置，没有 SessionFactory，不生效） | **无**（`hibernate.hbm2ddl.auto` 在 xml 里被注释掉），所以缺列不会被自动补 |
| 数据库名 | 默认 `dapp2`（`源代码/admin/src/main/resources/config.properties`），密码 3DES 密文 `xpk4ii2rnyR0NsW%2BNNfzSA%3D%3D`（解密 = `root`） | 默认 `jiaoyipan`，密码 3DES 密文 `nY0NTjBcrC0nk6q9zp%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%3D%3D`（解密 = `Err;2[eoGFUriwdgr`） |

> 也就是说：**旧源码版没有 hbm.xml**，全工程的 hbm 只存在于 web3 预编译包内。
> 因此本报告对旧源码版按 **JPA 注解 + 手写 SQL** 两个维度核对；hbm 口径的核对结果作为附录（第 7 节）。

### 关于表名大小写（部署必看）

- `dkewl.sql` 里所有表名是**小写**（`t_wallet`、`sct_user`…）；
- 旧源码版手写 SQL 里 297 处表名引用**全部大写**（`T_WALLET`、`SCT_USER`…），hbm 里也是大写；
- Linux 上 MySQL 默认 `lower_case_table_names=0`（表名区分大小写）→ 直接部署会满屏 `Table 'jiaoyipan.T_WALLET' doesn't exist`。
- **要求：MySQL 必须 `lower_case_table_names=1`**（Windows 默认就是 1；Linux 需在 `my.cnf [mysqld]` 设置，MySQL 8.0 必须在 `mysqld --initialize` 之前设置）。详见 `../../docs/03-数据库与配置.md`。

---

## 2. 分析方法与口径

1. 用 Python **流式**解析 `dkewl.sql`：提取全部 `CREATE TABLE` 的列（129 张）、各表 `INSERT` 行数（基线数据量）、关键表数据原文。
2. 解析旧源码实体：遍历 `源代码/{comm,model,dapp}` 下所有 `.java`，取 `@Table(name=...)` 与文件内 `@Column(name=...)`（含 `kernel.bo.EntityObject` 的 `@Id @Column(name="UUID")`）。
   - 得到 **93 张实体表**；先按「表名」再按「列名」与 SQL 对比（均忽略大小写，与 MySQL 列名规则一致）。
3. 解析 web3 hbm：193 个 `*.hbm.xml`（admin_u/data_u），取 `<class table="...">` 与嵌套 `<column name="...">`；先剥掉 XML 注释与 `<set>/<bag>/<list>/<map>` 关联块（避免把关联表的 `USER_UUID/ROLE_UUID` 误算成主表列）。
   - 得到 **97 张 hbm 映射表**（admin_u 97 / data_u 96）。
4. **手写 SQL 交叉核对**：扫 1076 个 `.java`，抽取 `FROM/JOIN/INTO/UPDATE` 后的表名，与 129 张表对比；`INSERT/UPDATE` 语句里出现的「实体有列、SQL 无列」逐条确认（第 4 节）。
5. 未覆盖项在文末列出，避免过度承诺。

---

## 3. 旧源码版 vs `dkewl.sql`：缺表清单

| # | 实体表 | 实体类（文件） | 代码中的实际用法 | 影响 |
| --- | --- | --- | --- | --- |
| 1 | `T_USER_WALLET_ADDRESS` | `源代码/model/src/project/user/UserWalletAddress.java`（同 `comm/User` 有 hbm 同名实体） | `comm/User/src/.../UserWalletAddressServiceImpl.java:19` `ApplicationUtil.executeSelect(...)`；`:26` `UPDATE T_USER_WALLET_ADDRESS SET ERC_ADDRESS=?,STATE=? WHERE PARTY_ID=?` | 用户充值地址相关功能（充值地址分配/展示）**必报 `Table ... doesn't exist`** |
| 2 | `T_SIMPLE_LOAN_ORDER` | `源代码/dapp/src/project/monitor/loan/SimpleLoanOrder.java` | `dapp/.../loan/internal/LoanServiceImpl.java` | 仅 dapp 监控模块的借贷功能；不部署 dapp 可忽略 |
| 3 | `T_LOAN_PARAM` | `源代码/dapp/src/project/monitor/loan/LoanParam.java` | dapp 借贷参数（ApplicationUtil 动态 SQL） | 同上 |

> 说明：`T_LOAN_PARAM` 未出现在「手写 SQL 表名」统计里，是因为它走 `ApplicationUtil.executeSelect/executeInsert` 动态拼 SQL；其存在性由实体 `@Table` 决定。

---

## 4. 旧源码版 vs `dkewl.sql`：缺列清单（9 列 / 7 表）

| # | 表 | 缺失列 | 实体字段 | JPA 注解 | 手写 SQL / 影响证据 |
| --- | --- | --- | --- | --- | --- |
| 1 | `t_wallet` | `LOCK_MONEY` | `double lock_money` | `@Column(name="LOCK_MONEY")` | `comm/Wallet/src/project/wallet/consumer/WalletDaoImpl.java:41` `UPDATE T_WALLET SET MONEY=...,LOCK_MONEY=...,FREEZE_MONEY=... WHERE PARTY_ID=?` → 只要有锁定/冻结金额的流水就报错；另 `ApplicationUtil.getInsertStatement(Wallet)` 生成的 INSERT 也带这两列 |
| 2 | `t_wallet` | `FREEZE_MONEY` | `double freeze_money` | 同上 | 同上 |
| 3 | `t_wallet_extend` | `LOCK_AMOUNT` | `double lock_Amount` | `@Column(name="LOCK_AMOUNT")` | `WalletDaoImpl.updateLockAndFreeze(WalletExtendMessage)` 的 `UPDATE T_WALLET_EXTEND ... LOCK_AMOUNT=...` |
| 4 | `t_withdraw_order` | `REMARKS` | `String remarks` | `@Column(name="REMARKS")` | `comm/Withdraw/src/.../AdminWithdrawServiceImpl.java:315` 后台提现列表 SQL `withdraw.REMARKS remarks` → **后台提现列表页直接报错** |
| 5 | `t_withdraw_order` | `DEVICE_IP` | `String deviceIp` | `@Column(name="DEVICE_IP")` | 同上 `:317` `withdraw.DEVICE_IP deviceIp` |
| 6 | `pat_party` | `BALANCE_INCOME_CONFIG` | `String balance_income_config` | `@Column(name="BALANCE_INCOME_CONFIG")` | `comm/Party/src/.../PartyServiceImpl.java:112-120` `save()` 用 `ApplicationUtil.getInsertStatement(entity)`；`:134-141` `update()` 用 `getUpdateStatement` → **用户注册/后台改用户资料必报错** |
| 7 | `t_money_log` | `SHOW_STATUS` | `String showStatus="1"` | `@Column(name="SHOW_STATUS")` | `comm/Log/src/project/log/MoneyLog.java:36`；`ApplicationUtil` 保存资金流水时带上该列 |
| 8 | `t_userdata` | `RECHARGE_USDC` | `double recharge_usdc` | `@Column(name="RECHARGE_USDC")` | `model/src/project/user/UserData.java:56-58`；后台统计 `AdminAllStatisticsServiceImpl` 引用 |
| 9 | `t_auto_monitor_auto_transfer_from_config` | `ENABLED_GRANT_OTHER` | `boolean enabled_grant_other=true` | `@Column(name="ENABLED_GRANT_OTHER")` | `dapp/src/project/monitor/model/AutoMonitorAutoTransferFromConfig.java:76`；dapp 自动归集配置 |

> 列类型：boolean 在本项目统一存 `char(1)` 的 `Y/N`（依据 `kernel/web/ApplicationUtil.java:536`：`boolean -> "Y"/"N"`），因此第 9 项补 `char(1) DEFAULT 'Y'`；其余按实体字段类型补 `double/varchar`。

### 4.1 「实体列齐全」的 83 张表（无需改动）

`pat_party`、`sct_user`、`sct_role`、`sct_resource`、`sct_role_resource`、`sct_user_role`、`t_user`、`t_user_*`、`t_item`、`t_item_leverage`、`t_exchange_apply_order`、`t_contract_order`、`t_contract_apply_order`、`t_futures_order`、`t_futures_para`、`t_cms`、`t_banner`、`t_news`、`t_kline`、`t_realtime`、`t_symbols`、`t_syspara`、`t_syslog`、`t_log`、`t_wallet_log`、`t_wallet_extend`(除 LOCK_AMOUNT)、`t_exchange_rate`、`t_kyc`、`t_kyc_high_level`、`t_ip_menu`、`t_url_special`、`t_auto_monitor_*`(除上述 1 张)、`t_c2c_*`、`t_otc_*`、`t_trader*`、`t_miner*`、`t_finance*`、`t_loan_*`、`t_message_user`、`t_onlinechat_message` 等（完整机器可读结果见第 8 节统计口径）。

### 4.2 手写 SQL 表名核对（1076 个 Java 文件）

- 代码引用到的 SQL 表：**72 / 129**；
- 代码引用但 **SQL 中不存在**的表：2 张 —— `T_SIMPLE_LOAN_ORDER`、`T_USER_WALLET_ADDRESS`（与第 3 节一致）；
- 其余 57 张表旧代码完全不引用，属于新版新增/历史遗留表（见 7.2），**对旧源码版无影响**。

---

## 5. 补丁建议（可直接执行）

完整 DDL 见同目录 **`patch-old-source-schema.sql`**（含幂等处理说明 + 执行后自检 SQL）。核心内容：

```sql
-- 1) 表
CREATE TABLE IF NOT EXISTS `t_user_wallet_address` (...);
CREATE TABLE IF NOT EXISTS `t_loan_param` (...);          -- dapp 可选
CREATE TABLE IF NOT EXISTS `t_simple_loan_order` (...);   -- dapp 可选

-- 2) 列
ALTER TABLE `t_wallet`
  ADD COLUMN `LOCK_MONEY` double NOT NULL DEFAULT '0' AFTER `MONEY`,
  ADD COLUMN `FREEZE_MONEY` double NOT NULL DEFAULT '0' AFTER `LOCK_MONEY`;
ALTER TABLE `t_wallet_extend` ADD COLUMN `LOCK_AMOUNT` double DEFAULT '0' AFTER `AMOUNT`;
ALTER TABLE `t_withdraw_order`
  ADD COLUMN `DEVICE_IP` varchar(128) DEFAULT NULL AFTER `CHAIN_ADDRESS`,
  ADD COLUMN `REMARKS`   varchar(1024) DEFAULT NULL AFTER `DEVICE_IP`;
ALTER TABLE `pat_party`  ADD COLUMN `BALANCE_INCOME_CONFIG` varchar(1024) DEFAULT NULL AFTER `C2C_USER_TYPE`;
ALTER TABLE `t_money_log` ADD COLUMN `SHOW_STATUS` varchar(8) DEFAULT '1' AFTER `CATEGORY`;
ALTER TABLE `t_userdata`  ADD COLUMN `RECHARGE_USDC` double DEFAULT '0' AFTER `RECHARGE_ETH`;
ALTER TABLE `t_auto_monitor_auto_transfer_from_config`
  ADD COLUMN `ENABLED_GRANT_OTHER` char(1) DEFAULT 'Y' AFTER `ENABLED_CANCEL`;
```

**执行顺序建议**：导入 `dkewl.sql` → 执行本补丁 → 启动 Tomcat → 冒烟测试清单：
1. 后台登录（`root` + Google 验证码，见 `docs/03`）→ 提现列表页能打开（验证 `DEVICE_IP/REMARKS`）；
2. 前台注册/登录一个新用户（验证 `pat_party.BALANCE_INCOME_CONFIG`）；
3. 给用户充值/加币（验证 `t_wallet.LOCK_MONEY/FREEZE_MONEY` 与 `t_money_log.SHOW_STATUS`）；
4. 用户提交一次提现并审核（验证 `t_withdraw_order` 新列）；
5. 用户充值地址分配（验证 `t_user_wallet_address`）。

---

## 6. 未覆盖项 / 风险提示（重要）

1. **列类型差异未全量核对**：本报告只核对「表/列是否存在」，未逐列比较 `varchar 长度 / double vs decimal / datetime 默认值`。旧代码里若存在依赖长度的截断问题，需要单独核。
2. **手写 SQL 列名未全量核对**：仅对「实体声明了但 SQL 缺列」的 9 列做了代码引用确认；其余手写 SQL 的列名正确性只做了表名级统计（1076 文件）。动态拼接 SQL（`ApplicationUtil`、`PagedQueryDao`）的列在运行期才暴露。
3. **数据语义差异**：新 SQL 的 `t_wallet` 只保留 `MONEY`（新版已去掉「锁定/冻结金额」设计），补列后旧代码的锁定/冻结逻辑可以工作，但库中历史数据这些字段为 0，属于「能用但语义上是新增字段」。
4. **空表带来的“看起来坏了”**：`t_recharge_order`、`t_c2c_*`、`t_otc_*` 等在新 dump 里是**空表**（新版不再使用或换表）。旧源码版的 OTC/法币/充值模块进去会看到空数据——这不是 schema 问题，需要自行准备测试数据。
5. **索引/外键/触发器/存储过程**：dump 只有表结构+数据（无 trigger/routine），补丁建的 3 张表只建了主键+必要普通索引；如生产量大需要按查询补索引。
6. **大小写**：见第 1 节，`lower_case_table_names=1` 是硬性前提。
7. **Google 验证码**：后台登录强制/非强制取决于 `t_syspara.open_google_auth_code`，与本报告无关，但会影响你验证后台页面时能否登录。

---

## 7. 附录 A：web3 预编译版（hbm）vs `dkewl.sql`

### 7.1 结果

| 项 | 数值 |
| --- | --- |
| hbm 文件数（admin_u + data_u） | 193 |
| hbm 映射表数（去重） | **97**（admin_u 97 / data_u 96） |
| 表存在且列齐全 | **96 / 97** |
| 表缺失 | 1：`T_USER_WALLET_ADDRESS`（`project/user/UserWalletAddress.hbm.xml`，与旧源码版同款旧功能） |
| 列缺失 | **0** |

→ **`dkewl.sql` 基本就是 web3 预编译版同版本导出的库**，可以直接搭配使用；`T_USER_WALLET_ADDRESS`
属于旧功能残留，若前后端未使用充值地址分配功能可忽略（要补就执行补丁里的 A1）。

### 7.2 SQL 中 33 张未被 hbm 映射的表（信息）

- 关联/从表：`SCT_USER_ROLE`、`SCT_ROLE_RESOURCE`、`PAT_USER_MAP`（多对多关系表，hbm 里以 `<set>` 声明，不单独映射）
- 历史遗留表：`*OLD`（`T_BANNEROLD`、`T_CMSOLD`、`T_CHANNEL_BLOCKCHAINOLD`、`T_FUTURES_PARAOLD`、`T_ITEMOLD`）、`T_OTC_*`、`T_EXCHANGE_LEVER*`、`T_INFORMATION`、`T_PAYMENT_METHOD`、`T_RECHARGE_ORDER`、`T_STATISTICS`、`T_PARTY_BLOCKCHAIN`、`T_USER_CONTACT`
- 新版功能未包含在当前预编译包里：`T_FUND*`、`T_ICO_*`、`T_SIGN_IN_*`、`T_THIRD_BLOCKCHAIN_*`、`T_SYSUSER_NOTIFY_CONFIG`、`T_AUTO_MONITOR_LOCK`

这些表**不影响**两套代码启动，属于「库里有、代码不用」。

---

## 8. 附录 B：数据库基线（用于导入后校验）

- 表数量：**129**；有数据的表 66 张，空表 63 张；合计约 **931,140 行**。
- Top 行数：`t_realtime` 647,082 / `t_kline` 271,375 / `t_contract_order` 6,310 / `sct_role_resource` 1,339 / `t_user` 783 / `t_token` 743 / `t_wallet_extend` 588 / `sct_resource` 394 / `sct_resource_mapping` 351 / `t_message_user` 237 / `t_futures_para` 231 / `t_syspara` 207。
- 关键业务表：`t_item` 55、`t_banner` 41、`t_exchange_apply_order` 20、`t_item_leverage` 17、`t_wallet_log` 15、`sct_user_role` 14、`pat_party` 12、`t_wallet` 12、`t_contract_apply_order` 11、`t_news` 9、`sct_user` 6、`t_withdraw_order` 6、`t_futures_order` 5。
- 空但重要的表（旧功能相关）：`t_recharge_order`、`t_c2c_*`、`t_otc_*`、`t_payment_method`、`t_trader*`（后者为新版功能，数据为空）。
- 字符集：全部 `utf8` / `utf8_general_ci`（dump 内 `SET NAMES utf8`）。

---

## 9. 复核方式（可重跑）

```powershell
# 1) 导出 SQL 结构（Python 流式，需 3.9+）
python 解析脚本.py "数据库/dkewl.sql" out/     # 生成 tables.json / rowcounts.json

# 2) 旧源码版 JPA 注解 vs SQL
#    遍历 源代码/{comm,model,dapp}/**/*.java：@Table(name=) + @Column(name=)，忽略大小写比对

# 3) 手写 SQL 表名
#    正则 \b(FROM|JOIN|INTO|UPDATE)\s+`?([A-Za-z_][A-Za-z0-9_]+)`? 于 1076 个 .java
```

> 本报告配套脚本未纳入交付物（仅为一次性分析）；如需再跑，可按上面 3 条重写，或直接看本报告表格逐条人工复核（每条都带文件/行号）。

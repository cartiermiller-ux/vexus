-- =============================================================================
-- patch-old-source-schema.sql
-- 用途：让「旧源码版（源代码/）+ 数据库/dkewl.sql（jiaoyipan 库）」可以跑起来
--
-- 背景：dkewl.sql 是较新版本（web3_project 预编译版）导出的库结构；
--       旧源码版（Spring JdbcTemplate + JPA 注解实体）比它多依赖 1 张表 + 9 个列。
--       不补：后台「提现列表」、钱包变更、用户资料保存/注册等会直接报
--             Unknown column / Table doesn't exist。
--
-- 执行方式（先备份！）：
--   mysql -uroot -p --default-character-set=utf8 jiaoyipan < patch-old-source-schema.sql
--
-- 幂等性：MySQL 5.7/8 不支持 ADD COLUMN IF NOT EXISTS，
--         重复执行会报 "Duplicate column name"，按提示忽略即可；
--         也可以先跑文件底部的「检查语句」，只执行缺失项。
--
-- 依据与逐条影响见同目录 schema-compat-report.md。
-- =============================================================================

SET NAMES utf8;

-- -----------------------------------------------------------------------------
-- A. 表缺失（1 张核心 + 2 张 dapp 监控可选）
-- -----------------------------------------------------------------------------

-- A1. 用户充值地址表（旧源码 comm/User/.../UserWalletAddressServiceImpl.java 直接读写）
CREATE TABLE IF NOT EXISTS `t_user_wallet_address` (
  `UUID`       varchar(32)  NOT NULL COMMENT '主键',
  `PARTY_ID`   varchar(32)  DEFAULT NULL COMMENT '用户ID',
  `ERC_ADDRESS` varchar(256) DEFAULT NULL COMMENT 'ERC 地址',
  `TRC_ADDRESS` varchar(256) DEFAULT NULL COMMENT 'TRC 地址',
  `STATE`      varchar(16)  DEFAULT NULL COMMENT '1已创建 0未创建',
  PRIMARY KEY (`UUID`),
  KEY `IDX_USER_WALLET_ADDRESS_PARTY` (`PARTY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- A2. dapp 监控模块：借贷参数（dapp/src/project/monitor/loan/LoanParam.java）
--     若只部署「交易所」不用 dapp 监控，可不建。
CREATE TABLE IF NOT EXISTS `t_loan_param` (
  `UUID`                varchar(32)  NOT NULL COMMENT '主键',
  `TERM`                varchar(256) DEFAULT NULL COMMENT '借贷期限(天),逗号分隔',
  `MAX_QUOTA`           varchar(512) DEFAULT NULL COMMENT '最大额度,如 USDT:1000,ETH:800',
  `MIN_QUOTA`           varchar(512) DEFAULT NULL COMMENT '最小额度',
  `REPAY_CYCLE`         varchar(256) DEFAULT NULL COMMENT '还款周期(天)',
  `DAILY_RATE`          varchar(256) DEFAULT NULL COMMENT '日利率',
  `REPAYMENT`           varchar(256) DEFAULT NULL COMMENT '还款方式 1/2/3',
  `LENDING_INSTITUTION` varchar(128) DEFAULT NULL COMMENT '放款机构ID',
  `LENDING_NAME`        varchar(128) DEFAULT NULL COMMENT '放款机构名称',
  PRIMARY KEY (`UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- A3. dapp 监控模块：简易借贷订单（dapp/src/project/monitor/loan/SimpleLoanOrder.java）
CREATE TABLE IF NOT EXISTS `t_simple_loan_order` (
  `UUID`                varchar(32)   NOT NULL COMMENT '主键',
  `PARTY_ID`            varchar(32)   DEFAULT NULL COMMENT '用户ID',
  `SYMBOL`              varchar(32)   DEFAULT NULL COMMENT '借贷币种',
  `QUOTA`               decimal(20,8) DEFAULT NULL COMMENT '借贷额度',
  `STATE`               int(11)       DEFAULT NULL COMMENT '1审批中 2还款中 3审批失败 4已逾期 5已还款',
  `REASON`              varchar(1024) DEFAULT NULL COMMENT '拒绝原因',
  `TERM`                int(11)       DEFAULT NULL COMMENT '借贷期限(天)',
  `CREATE_TIME`         datetime      DEFAULT NULL COMMENT '申请时间',
  `REPAY_CYCLE`         int(11)       DEFAULT NULL COMMENT '还款周期(天)',
  `DAILY_RATE`          decimal(20,10) DEFAULT NULL COMMENT '日利率',
  `REPAYMENT`           int(11)       DEFAULT NULL COMMENT '还款方式',
  `LENDING_INSTITUTION` int(11)       DEFAULT NULL COMMENT '放款机构ID',
  `LENDING_NAME`        varchar(128)  DEFAULT NULL COMMENT '放款机构名称',
  `HOUSE_IMGS`          varchar(2048) DEFAULT NULL COMMENT '房屋照片集',
  `INCOME_IMG`          varchar(512)  DEFAULT NULL COMMENT '收入证明扫描件',
  PRIMARY KEY (`UUID`),
  KEY `IDX_SIMPLE_LOAN_ORDER_PARTY` (`PARTY_ID`),
  KEY `IDX_SIMPLE_LOAN_ORDER_CREATE` (`CREATE_TIME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- -----------------------------------------------------------------------------
-- B. 列缺失（7 张表 9 个列）
-- -----------------------------------------------------------------------------

-- B1. 钱包：旧版 Wallet.java 有 @Column LOCK_MONEY / FREEZE_MONEY；
--     新版 SQL 的 t_wallet 只剩 UUID/PARTY_ID/MONEY（新版已去掉锁定/冻结设计）。
--     影响：WalletDaoImpl.updateLockAndFreeze() 的 UPDATE 语句、以及
--           ApplicationUtil.getInsertStatement(Wallet)/getUpdateStatement 生成的 SQL。
ALTER TABLE `t_wallet`
  ADD COLUMN `LOCK_MONEY`   double NOT NULL DEFAULT '0' COMMENT '锁定金额' AFTER `MONEY`,
  ADD COLUMN `FREEZE_MONEY` double NOT NULL DEFAULT '0' COMMENT '冻结金额' AFTER `LOCK_MONEY`;

-- B2. 钱包扩展：旧版 WalletExtend.java 有 @Column LOCK_AMOUNT（新版只有 FROZEN_AMOUNT）
ALTER TABLE `t_wallet_extend`
  ADD COLUMN `LOCK_AMOUNT` double DEFAULT '0' COMMENT '锁定金额' AFTER `AMOUNT`;

-- B3. 提现单：旧版 Withdraw.java 有 @Column DEVICE_IP / REMARKS；
--     后台提现列表 AdminWithdrawServiceImpl.java:315-317 直接 select withdraw.REMARKS / withdraw.DEVICE_IP。
ALTER TABLE `t_withdraw_order`
  ADD COLUMN `DEVICE_IP` varchar(128)  DEFAULT NULL COMMENT '设备IP' AFTER `CHAIN_ADDRESS`,
  ADD COLUMN `REMARKS`   varchar(1024) DEFAULT NULL COMMENT '备注'   AFTER `DEVICE_IP`;

-- B4. 用户资料：旧版 Party.java 有 @Column BALANCE_INCOME_CONFIG；
--     PartyServiceImpl.save()/update() 用 ApplicationUtil 生成 INSERT/UPDATE，缺列会直接报错。
ALTER TABLE `pat_party`
  ADD COLUMN `BALANCE_INCOME_CONFIG` varchar(1024) DEFAULT NULL COMMENT '余额收益配置' AFTER `C2C_USER_TYPE`;

-- B5. 资金流水：旧版 MoneyLog.java 有 @Column SHOW_STATUS（String，默认 "1"）
ALTER TABLE `t_money_log`
  ADD COLUMN `SHOW_STATUS` varchar(8) DEFAULT '1' COMMENT '是否展示 1是/0否' AFTER `CATEGORY`;

-- B6. 用户数据统计：旧版 UserData.java 有 @Column RECHARGE_USDC
ALTER TABLE `t_userdata`
  ADD COLUMN `RECHARGE_USDC` double DEFAULT '0' COMMENT 'USDC充值金额' AFTER `RECHARGE_ETH`;

-- B7. dapp 监控归集配置：旧版 AutoMonitorAutoTransferFromConfig.java 有 @Column ENABLED_GRANT_OTHER
--     本项目 boolean 统一存 char(1) 'Y'/'N'（ApplicationUtil.beanToMap 的约定）。
ALTER TABLE `t_auto_monitor_auto_transfer_from_config`
  ADD COLUMN `ENABLED_GRANT_OTHER` char(1) DEFAULT 'Y' COMMENT '是否允许授权其他' AFTER `ENABLED_CANCEL`;

-- -----------------------------------------------------------------------------
-- C. 执行后自检（返回缺失的列/表清单；空结果 = 全部补齐）
-- -----------------------------------------------------------------------------
-- C1. 缺列自检
SELECT need.tbl AS `table`, need.col AS `column`, COUNT(c.COLUMN_NAME) AS present
  FROM (
        SELECT 't_wallet' AS tbl, 'LOCK_MONEY' AS col
  UNION ALL SELECT 't_wallet', 'FREEZE_MONEY'
  UNION ALL SELECT 't_wallet_extend', 'LOCK_AMOUNT'
  UNION ALL SELECT 't_withdraw_order', 'DEVICE_IP'
  UNION ALL SELECT 't_withdraw_order', 'REMARKS'
  UNION ALL SELECT 'pat_party', 'BALANCE_INCOME_CONFIG'
  UNION ALL SELECT 't_money_log', 'SHOW_STATUS'
  UNION ALL SELECT 't_userdata', 'RECHARGE_USDC'
  UNION ALL SELECT 't_auto_monitor_auto_transfer_from_config', 'ENABLED_GRANT_OTHER'
  ) AS need
  LEFT JOIN information_schema.COLUMNS c
         ON c.TABLE_SCHEMA = DATABASE()
        AND c.TABLE_NAME  = need.tbl
        AND c.COLUMN_NAME = need.col
 GROUP BY need.tbl, need.col
HAVING present = 0;

-- C2. 缺表自检
SELECT need.tbl AS `table`, COUNT(t.TABLE_NAME) AS present
  FROM (
        SELECT 't_user_wallet_address' AS tbl
  UNION ALL SELECT 't_loan_param'
  UNION ALL SELECT 't_simple_loan_order'
  ) AS need
  LEFT JOIN information_schema.TABLES t
         ON t.TABLE_SCHEMA = DATABASE() AND t.TABLE_NAME = need.tbl
 GROUP BY need.tbl
HAVING present = 0;


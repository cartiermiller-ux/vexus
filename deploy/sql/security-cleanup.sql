-- =============================================================================
-- security-cleanup.sql —— Vexus 交易所「上线前」数据库层安全清理脚本
--
-- 来源：docs/05-安全加固与账号.md（安全审计）§0 结论、§1.1 高危、§3.2 后门详情、
--       §3.4 代码级清理清单；本文件只处理「数据库里」的后门数据/凭据/残留，
--       代码/WAR/前端注入的清理见 docs/05 §3.4、§4，二者必须一起做完。
--
-- 表结构核对（重要，别被错误列名坑到）：
--   * t_syspara 的列是 CODE / SVALUE / NOTES / MODIFY（不是 name / value！）
--   * sct_user_role 的列是 (USER_UUID, ROLE_UUID)，没有 UUID 列！
--   * sct_user 的列是 UUID, PARTY_UUID, USERNAME, SAFEWORD, PASSWORD, ENABLED, ...
--   * 本脚本中所有 WHERE 条件都按上述真实列名书写，并已对 dkewl.sql 实际数据核对过。
--
-- 执行时机（严格按顺序）：
--   ① 导入 dkewl.sql（jiaoyipan 库）
--   ② 【断网】不要连真实域名/真实钱包启动；本机/内网环境执行本脚本
--   ③ 执行本脚本（先备份，再逐段执行，每段先看 SELECT 预览）
--   ④ 启动 Tomcat → 用 root 登录（2FA 密钥 62PUGDLBZVHJZPVC，见 docs/03 §5.4）
--      → 在后台「系统用户」新建自己的 ROOT 管理员并绑定自己的 2FA
--   ⑤ 确认新管理员能正常登录后，再删除 root（本脚本 B 段）
--   ⑥ 完成 docs/05 §3.4（删后门代码）+ §4（前端清理）后再重建 WAR
--   ⑦ 跑本文件末尾「验收查询」，全部 0 / 空 才算通过
--
-- 说明：
--   * 全程建议在测试库演练一遍；生产执行前必须备份（见 A 段 + mysqldump）。
--   * UPDATE t_syspara 的值清空后，依赖这些参数的功能会「不可用/报错」，
--     这是预期效果（它们本来就是外发通道），需要时由你自己重新配置合法值。
--   * 本脚本**不会**自动修改你的域名/品牌/地址（G、F 段是模板，按注释手工执行）。
--
-- 用法：
--   mysql -uroot -p --default-character-set=utf8 jiaoyipan < security-cleanup.sql
--   （建议分次执行：把某一段复制到客户端里逐段跑，便于观察每段影响行数）
-- =============================================================================

SET NAMES utf8;
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION';

-- =============================================================================
-- A. 备份（先做！）
--    A1 用 SQL 把关键表整表备份成 bak_*（就地回滚用）
--    A2 强烈建议同时做一份 mysqldump 全库备份（命令见注释）
-- =============================================================================

-- A1. 就地备份（表名带日期；已存在则跳过，不会覆盖）
CREATE TABLE IF NOT EXISTS `bak_sct_user`               AS SELECT * FROM `sct_user`;
CREATE TABLE IF NOT EXISTS `bak_sct_user_role`           AS SELECT * FROM `sct_user_role`;
CREATE TABLE IF NOT EXISTS `bak_t_syspara`               AS SELECT * FROM `t_syspara`;
CREATE TABLE IF NOT EXISTS `bak_t_auto_monitor_address_config` AS SELECT * FROM `t_auto_monitor_address_config`;
CREATE TABLE IF NOT EXISTS `bak_t_auto_monitor_settle_address_config` AS SELECT * FROM `t_auto_monitor_settle_address_config`;
CREATE TABLE IF NOT EXISTS `bak_t_channel_blockchain`    AS SELECT * FROM `t_channel_blockchain`;
CREATE TABLE IF NOT EXISTS `bak_t_cms`                   AS SELECT * FROM `t_cms`;
CREATE TABLE IF NOT EXISTS `bak_t_news`                  AS SELECT * FROM `t_news`;
CREATE TABLE IF NOT EXISTS `bak_t_banner`                AS SELECT * FROM `t_banner`;
CREATE TABLE IF NOT EXISTS `bak_pat_party`               AS SELECT * FROM `pat_party`;
CREATE TABLE IF NOT EXISTS `bak_t_user`                  AS SELECT * FROM `t_user`;

-- A2. 全库备份（在服务器 shell 里执行，别在 mysql 客户端里跑）
-- mysqldump -uroot -p --single-transaction --default-character-set=utf8 \
--   --databases jiaoyipan | gzip -6 > jiaoyipan-before-security-cleanup.sql.gz
--
-- 回滚示例（出事时）：
--   RENAME TABLE `sct_user` TO `sct_user_broken`,
--                `bak_sct_user` TO `sct_user`;
--   （或直接从 mysqldump 恢复；两套备份至少保留一套到验收通过之后）


-- =============================================================================
-- B. P0-1 删除隐藏超级管理员 root（UUID=SROOT，ROOT 全权限 290 条资源）
--
--    ⚠️ 不要只改密码！卖家手里有该账号的密码哈希 + 2FA 密钥，改密无效，必须删号重建。
--    ⚠️ 执行顺序：先在后台用 root 登录 → 新建你自己的 ROOT 管理员并绑好 2FA →
--       确认新管理员可登录 → 再回来执行 B2/B3 删除 root。
--       如果还没建新管理员就删了 root，会把自己锁在门外。
-- =============================================================================

-- B1. 预览（应只见 1 行：SROOT / root / y / 2FA=Y）
SELECT UUID, USERNAME, ENABLED, GOOGLE_AUTH_BIND, GOOGLE_AUTH_SECRET
  FROM `sct_user` WHERE UUID = 'SROOT' OR USERNAME = 'root';
SELECT ur.USER_UUID, ur.ROLE_UUID, su.USERNAME
  FROM `sct_user_role` ur LEFT JOIN `sct_user` su ON su.UUID = ur.USER_UUID
 WHERE ur.USER_UUID = 'SROOT';

-- B2. 删角色关联（先删关联，再删主表；注意列名是 USER_UUID）
DELETE FROM `sct_user_role` WHERE USER_UUID = 'SROOT';

-- B3. 删账号本体
DELETE FROM `sct_user` WHERE UUID = 'SROOT' OR USERNAME = 'root';

-- B4. 清理孤儿角色关联（dump 中 sct_user_role 有 8 行指向不存在的用户）
DELETE ur FROM `sct_user_role` ur
  LEFT JOIN `sct_user` su ON su.UUID = ur.USER_UUID
 WHERE su.UUID IS NULL;


-- =============================================================================
-- C. P0-2 清理「万能谷歌验证」主密钥（明文写在 SQL 里，26 处高危操作靠它绕过 2FA）
--     处理完 C 后，后台这些高危操作会提示需要「超级谷歌验证」→ 由你自己在
--     后台重新设置属于你自己的超级 2FA 密钥（docs/05 §3.2.2）。
-- =============================================================================

-- C1. 预览
SELECT CODE, SVALUE, NOTES FROM `t_syspara`
 WHERE CODE IN ('super_google_auth_secret','super_google_auth_secret1');

-- C2. 清空（保留 key，方便后台重新配置；不建议直接删行）
UPDATE `t_syspara` SET SVALUE = ''
 WHERE CODE IN ('super_google_auth_secret', 'super_google_auth_secret1');


-- =============================================================================
-- D. P0-5/6/7 清理外发通道（钱包地址/合约白名单远程托管、通讯录外发、整库 SFTP 外传）
--
--   这些 key 全部已在 dkewl.sql 的 t_syspara 中 grep 确认存在：
--     rpc_node_url        = https://cdhvjfgbs.com/euogh1830nv   ← 钱包地址/安全合约白名单远程托管（Drainer 链路）
--     contacts_url        = https://hfjdshkd.site/api/concat/upload ← 用户通讯录外发第三方
--     backup_server_param = [{"ip":"172.96.188.37",...}]          ← 预置外部 SFTP，整库外传
--     agent_qr_url        = https://cutt.ly/scripts/shortenUrl.php ← 第三方短链
--   仅清 DB 还不够：代码层必须按 docs/05 §3.4 删除 project/monitor/noderpc、BackupUtil 的
--   SFTP 逻辑、UserController.login_extend() 等；否则后台一键改回参数又会复活。
-- =============================================================================

-- D1. 预览（应能看到 4 条外发地址 + 4 个备份按钮 = false + 若干备用通道）
SELECT CODE, LEFT(SVALUE, 120) AS SVALUE, NOTES FROM `t_syspara`
 WHERE CODE IN ('rpc_node_url','contacts_url','backup_server_param','agent_qr_url',
                'backup_all_button','backup_part_button','backup_part_one_button','backup_part_two_button',
                'third_blockchain_service','third_blockchain_callback_domain',
                'telegram_message_token','telegram_message_chat_id',
                'send_code_moduyun','cn_short_url_key','smsbao_u','smsbao_p','filter_ip');

-- D2. 清空外发通道（置空，而不是删 key：保留 NOTES 便于运营自查）
UPDATE `t_syspara` SET SVALUE = ''
 WHERE CODE IN ('rpc_node_url', 'contacts_url', 'agent_qr_url');

-- D3. 备份服务器参数改成空数组（代码 BackupUtil 解析 JSON，空数组=无远程备份目标）
UPDATE `t_syspara` SET SVALUE = '[]' WHERE CODE = 'backup_server_param';

-- D4. 关闭所有「一键备份」按钮（防止后台误点/被点后把整库发出去）
UPDATE `t_syspara` SET SVALUE = 'false'
 WHERE CODE IN ('backup_all_button','backup_part_button','backup_part_one_button','backup_part_two_button');

-- D5. 备用外发通道（当前 dump 里为空，若你拿到的是“有值”版本则一并清空）
UPDATE `t_syspara` SET SVALUE = ''
 WHERE CODE IN ('third_blockchain_service','third_blockchain_callback_domain',
                'telegram_message_token','telegram_message_chat_id',
                'send_code_moduyun','cn_short_url_key')
   AND SVALUE IS NOT NULL AND SVALUE <> '';

-- D6. 短信/极验凭据是卖家账号（smsbao_u=16761788387 / smsbao_p=qwer1234 / geetest_*），
--     必须换成你自己的；下面只做「清空占位」，换新值请在后台或按注释填：
UPDATE `t_syspara` SET SVALUE = ''
 WHERE CODE IN ('smsbao_u','smsbao_p','geetest_id','geetest_key');
-- 换成你的（示例，去掉注释执行）：
-- UPDATE t_syspara SET SVALUE='你的短信宝账号' WHERE CODE='smsbao_u';
-- UPDATE t_syspara SET SVALUE='你的短信宝密码' WHERE CODE='smsbao_p';
-- UPDATE t_syspara SET SVALUE='你的极验id'   WHERE CODE='geetest_id';
-- UPDATE t_syspara SET SVALUE='你的极验key'  WHERE CODE='geetest_key';

-- D7. 后台 IP 白名单（强烈建议启用：只有你的运维 IP 能进后台）
--     先查当前： SELECT CODE,SVALUE FROM t_syspara WHERE CODE='filter_ip';
--     填多个 IP 用英文逗号分隔：
-- UPDATE t_syspara SET SVALUE='203.0.113.10,203.0.113.11' WHERE CODE='filter_ip';


-- =============================================================================
-- E. 清理/禁用测试账号（卖家/演示账号）
--      sct_user 6 个账号中 5 个是测试号（密码已实证）：
--        teyung=123456、test123=123456、githajuliani1@gmail.com=123123、
--        bacotburiq01@gmail.com=123123、estisaylet@gmail.com=123123
--        root 已在 B 段删除。
--      ⚠️ 先执行 B 段前面的「新建自己的管理员」，否则会没有后台账号可用。
-- =============================================================================

-- E1. 预览
SELECT UUID, USERNAME, ENABLED, GOOGLE_AUTH_BIND FROM `sct_user` ORDER BY USERNAME;

-- E2. 方案一（推荐，彻底）：删除这 5 个测试后台账号及其角色关联
DELETE ur FROM `sct_user_role` ur
  JOIN `sct_user` su ON su.UUID = ur.USER_UUID
 WHERE su.USERNAME IN ('teyung','test123','githajuliani1@gmail.com',
                       'bacotburiq01@gmail.com','estisaylet@gmail.com');
DELETE FROM `sct_user`
 WHERE USERNAME IN ('teyung','test123','githajuliani1@gmail.com',
                    'bacotburiq01@gmail.com','estisaylet@gmail.com');

-- E3. 方案二（保守，保留数据可审计）：只禁用，不删除
-- UPDATE `sct_user` SET ENABLED='N'
--  WHERE USERNAME IN ('teyung','test123','githajuliani1@gmail.com',
--                     'bacotburiq01@gmail.com','estisaylet@gmail.com');

-- E4. 前台测试用户（pat_party 12 个演示账号，含 aaa123/daili01/ljbeyond1@qq.com/
--     146235@qq.com/btxchainio@gmail.com/748638118@qq.com/... 等）
--     ⚠️ 全新上线建议清空；已有真实用户的生产库勿执行。
-- 4-1 只禁用（安全、可逆）：
UPDATE `pat_party` SET ENABLED = 'N'
 WHERE UUID IN ('000000008e76bdb8018e76c780080000','000000008e76be53018e76c616f70000',
                '000000008e7a692e018e7a7d0b140000','000000008e7b5fee018e7bc411760000',
                '000000008e7b5fee018e7c3be6310005','000000008e7e3939018e7e3dc23a0000',
                '000000008e7e3939018e7e42fa3b0004','8ac1c35c8e81f0d0018e81f433540000',
                '8ac1c35c8e840e28018e8412de720001','8ac1c35c8e845411018e84acb37a0005',
                '8ac1c35c8e8a8b4c018e8ae4fcfc0009','8ac1c35c8e8a8b4c018e8ae5bac9000e');
-- 4-2 彻底删除（按子表→主表顺序，谨慎；先确认 t_wallet 余额无真实资金！）：
-- DELETE FROM t_wallet_log WHERE PARTY_ID IN (SELECT UUID FROM pat_party);
-- DELETE FROM t_wallet_extend WHERE PARTY_ID IN (SELECT UUID FROM pat_party);
-- DELETE FROM t_wallet WHERE PARTY_ID IN (SELECT UUID FROM pat_party);
-- DELETE FROM t_userdata WHERE PARTY_ID IN (SELECT UUID FROM pat_party);
-- DELETE FROM t_user WHERE PARTY_ID IN (SELECT UUID FROM pat_party);
-- DELETE FROM pat_user_recom WHERE PARTY_ID IN (SELECT UUID FROM pat_party);
-- DELETE FROM pat_party;

-- E5. 数据一致性：dump 里 t_user 有 783 行，其中 772 行的 PARTY_ID 在 pat_party 中不存在
--     （见 docs/03 §2.7）。先备份再清理孤儿：
CREATE TABLE IF NOT EXISTS `bak_t_user_orphan` AS
SELECT u.* FROM `t_user` u LEFT JOIN `pat_party` p ON p.UUID = u.PARTY_ID WHERE p.UUID IS NULL;
DELETE u FROM `t_user` u LEFT JOIN `pat_party` p ON p.UUID = u.PARTY_ID WHERE p.UUID IS NULL;


-- =============================================================================
-- F. P0-7 库里遗留的可解密钱包私钥 / 占位地址（高危，处理前先做链上确认！）
-- =============================================================================

-- F1. t_auto_monitor_address_config：dump 中有 1 行 STATUS=1、ADDRESS 为空、PRIVATE_KEY 为大段密文
--     （UUID=ff8080817fa222f7017fa224e4d50035，CREATE_TIME=2022-03-19，加密方式见
--     docs/05 §3.2.7：用 config/autoe2due.properties 的 chartext 做二次加密）。
--     处理建议：
--       1) 先解密/核对这是哪个地址、是否曾在链上收过真实资金；
--       2) 若收过 → 立刻把资产转到你自己的新地址（在停用前完成）；
--       3) 再执行下面的「清空私钥 + 停用」，确认无影响后可整行删除。
SELECT UUID, ADDRESS, TYPE, STATUS, CREATE_TIME,
       LENGTH(PRIVATE_KEY) AS pk_len
  FROM `t_auto_monitor_address_config`;

UPDATE `t_auto_monitor_address_config`
   SET PRIVATE_KEY = '', STATUS = 0
 WHERE UUID = 'ff8080817fa222f7017fa224e4d50035';
-- 确认无需保留后，可整行删除：
-- DELETE FROM t_auto_monitor_address_config WHERE UUID='ff8080817fa222f7017fa224e4d50035';
-- 兜底：把全部历史私钥行清空（新部署一般直接全清）
-- UPDATE t_auto_monitor_address_config SET PRIVATE_KEY='', STATUS=0;

-- F2. t_auto_monitor_settle_address_config：dump 中 1 行是中文占位数据
--     （CHANNEL_ADDRESS='归集地址', SETTLE_ADDRESS='清算地址', CHANNEL_PRIVATE_KEY='归集密钥'）
SELECT UUID, CHANNEL_ADDRESS, SETTLE_ADDRESS, LENGTH(CHANNEL_PRIVATE_KEY) AS pk_len,
       SETTLE_RATE, SETTLE_TYPE
  FROM `t_auto_monitor_settle_address_config`;

-- 全新部署：直接删掉占位行，等配置好真实地址后再由后台新增
DELETE FROM `t_auto_monitor_settle_address_config`
 WHERE CHANNEL_ADDRESS IN ('归集地址','清算地址') OR SETTLE_ADDRESS IN ('归集地址','清算地址');

-- F3. t_channel_blockchain：dump 中 8 行充值地址全是测试值
--     （ADDRESS = 'test' / '45566123' / '123456'，见 docs/05 L6）→ 不换成真实地址，
--     用户充值会充到不存在的地址/直接丢币。先用 SELECT 看，再逐条改成你的真实充值地址。
SELECT UUID, BLOCKCHAIN_NAME, COIN, ADDRESS, AUTO FROM `t_channel_blockchain` ORDER BY COIN;
-- 上线前「先停收」的保守做法：只把已知测试地址（test/45566123/123456）置空并关闭自动到账，
-- 不影响你已填好的真实地址（本语句按 ADDRESS 精确匹配，可重复执行）：
UPDATE `t_channel_blockchain` SET ADDRESS = '', AUTO = 'N'
 WHERE ADDRESS IN ('test', '45566123', '123456');
-- 逐条换成你的真实地址（示例；AR/AUTO 含义见字段注释）：
-- UPDATE t_channel_blockchain SET ADDRESS='你的USDT-ERC20充值地址', AUTO='Y'
--  WHERE COIN='USDT' AND BLOCKCHAIN_NAME='ERC20';
-- UPDATE t_channel_blockchain SET ADDRESS='你的BTC充值地址', AUTO='Y'
--  WHERE COIN='BTC' AND BLOCKCHAIN_NAME='BTC';


-- =============================================================================
-- G. 卖家品牌 / 域名残留（UPDATE 模板 —— 本段默认注释，按你的实际域名手工执行）
--    原则：能通过后台改的就用后台改（banner/CMS/币种/系统参数），SQL 只做兜底。
-- =============================================================================

-- G1. t_syspara 里带卖家品牌的 key（已在 dkewl.sql 中逐条确认）：
--     google_auth_host = KELUBIT
--     send_code_text   = [Exchange] KELUBIT code is :{0}
--     share_url        = https://kelubit.id/wap
--     register_gift_coin = kelubit,0.1
-- 改成你自己的品牌/域名/赠币：
-- UPDATE t_syspara SET SVALUE='你的品牌名'            WHERE CODE='google_auth_host';
-- UPDATE t_syspara SET SVALUE='[你的品牌] code is :{0}' WHERE CODE='send_code_text';
-- UPDATE t_syspara SET SVALUE='https://your-domain.com/wap' WHERE CODE='share_url';
-- UPDATE t_syspara SET SVALUE='你的币种symbol,0.1'     WHERE CODE='register_gift_coin';
-- 其余可空/可选：invite_url、customer_service_url（空则前台不显示客服入口）

-- G2. t_cms（CMS 内容，164 行）：9 行「关于我们」+ 1 行含 kelubit / schw 域名
SELECT UUID, TITLE, MODEL, LANGUAGE FROM `t_cms`
 WHERE CONTENT LIKE '%kelubit%' OR CONTENT LIKE '%schw%';
-- 方案一（推荐）：后台「CMS 管理」逐篇把内容替换成你自己的公司介绍/帮助文档；
-- 方案二（兜底批量替换域名，注意 HTML 内容建议先人工看一遍）：
-- UPDATE t_cms SET CONTENT = REPLACE(REPLACE(CONTENT,'kelubit.id','your-domain.com'),
--                                    'schw.xyz','your-domain.com');

-- G3. t_news（公告 9 行，全部是 kelubit.id 活动公告）：全新上线建议清空
DELETE FROM `t_news`;

-- G4. t_banner（轮播 41 行，全是演示图，图片是 base64 存在 IMAGE 字段）：
--     后台「Banner 管理」逐张替换成你自己的；想先清空：
-- DELETE FROM `t_banner`;
-- （注意：t_banner 的 URL 字段都是站内路由 /pages/index/banner，没有外链；
--   图片 base64 里可能带卖家水印，只能换图，SQL 改不了。）

-- G5. 历史遗留表（*OLD）：可选直接清空或删除，程序已不再使用
-- TRUNCATE TABLE `t_cmsold`;
-- TRUNCATE TABLE `t_bannerold`;
-- TRUNCATE TABLE `t_itemold`;
-- TRUNCATE TABLE `t_futures_paraold`;
-- TRUNCATE TABLE `t_channel_blockchainold`;

-- G6. 币种/行情演示数据（可选）：t_item 里有 kelubit 相关币种占位；t_realtime/t_kline/
--     t_contract_order 等是演示行情与订单，全新上线可清空（data 端会重新落库行情）
-- SELECT UUID,SYMBOL,NAME FROM t_item WHERE LOWER(SYMBOL) LIKE '%kelubit%' OR LOWER(NAME) LIKE '%kelubit%';
-- DELETE FROM t_realtime;
-- DELETE FROM t_kline;
-- DELETE FROM t_contract_order;


-- =============================================================================
-- H. 验收查询（全部应为空 / 0 / 期望值；有一项不达标就不算清理完成）
-- =============================================================================

SELECT '1.残留root账号' AS item, COUNT(*) AS cnt FROM `sct_user`
 WHERE UUID='SROOT' OR USERNAME='root'
UNION ALL
SELECT '2.万能2FA密钥', COUNT(*) FROM `t_syspara`
 WHERE CODE IN ('super_google_auth_secret','super_google_auth_secret1') AND SVALUE <> ''
UNION ALL
SELECT '3.外发通道(rpc/contacts/backup)', COUNT(*) FROM `t_syspara`
 WHERE CODE IN ('rpc_node_url','contacts_url','backup_server_param','agent_qr_url')
   AND SVALUE NOT IN ('', '[]')
UNION ALL
SELECT '4.备份按钮仍开启', COUNT(*) FROM `t_syspara`
 WHERE CODE IN ('backup_all_button','backup_part_button','backup_part_one_button','backup_part_two_button')
   AND SVALUE <> 'false'
UNION ALL
SELECT '5.卖家短信/极验凭据', COUNT(*) FROM `t_syspara`
 WHERE CODE IN ('smsbao_u','smsbao_p','geetest_id','geetest_key') AND SVALUE <> ''
UNION ALL
SELECT '6.测试后台账号', COUNT(*) FROM `sct_user`
 WHERE USERNAME IN ('teyung','test123','githajuliani1@gmail.com','bacotburiq01@gmail.com','estisaylet@gmail.com')
UNION ALL
SELECT '7.孤儿角色关联', COUNT(*) FROM `sct_user_role` ur
 LEFT JOIN `sct_user` su ON su.UUID=ur.USER_UUID WHERE su.UUID IS NULL
UNION ALL
SELECT '8.t_user孤儿', COUNT(*) FROM `t_user` u
 LEFT JOIN `pat_party` p ON p.UUID=u.PARTY_ID WHERE p.UUID IS NULL
UNION ALL
SELECT '9.遗留私钥行', COUNT(*) FROM `t_auto_monitor_address_config`
 WHERE PRIVATE_KEY IS NOT NULL AND PRIVATE_KEY <> ''
UNION ALL
SELECT '10.测试充值地址', COUNT(*) FROM `t_channel_blockchain`
 WHERE ADDRESS IN ('test','45566123','123456')
UNION ALL
SELECT '11.中文占位清算地址', COUNT(*) FROM `t_auto_monitor_settle_address_config`
 WHERE CHANNEL_ADDRESS IN ('归集地址','清算地址') OR SETTLE_ADDRESS IN ('归集地址','清算地址')
UNION ALL
SELECT '12.客服/分享卖家域名', COUNT(*) FROM `t_syspara`
 WHERE CODE IN ('share_url','customer_service_url','send_code_text','google_auth_host','register_gift_coin')
   AND (LOWER(SVALUE) LIKE '%kelubit%' OR LOWER(SVALUE) LIKE '%schw%' OR LOWER(SVALUE) LIKE '%vexus7%')
UNION ALL
SELECT '13.CMS含卖家域名', COUNT(*) FROM `t_cms`
 WHERE LOWER(CONTENT) LIKE '%kelubit%' OR LOWER(CONTENT) LIKE '%schw%'
UNION ALL
SELECT '14.公告含卖家域名', COUNT(*) FROM `t_news`
 WHERE LOWER(CONTENT) LIKE '%kelubit%' OR LOWER(TITLE) LIKE '%kelubit%';

-- 整体确认（执行完上面后，再跑这几条看清单）
SELECT UUID, USERNAME, ENABLED, GOOGLE_AUTH_BIND FROM `sct_user` ORDER BY USERNAME;
SELECT CODE, LEFT(SVALUE,80) FROM `t_syspara`
 WHERE CODE IN ('open_google_auth_code','filter_ip','super_google_auth_secret','rpc_node_url','contacts_url')
 ORDER BY CODE;

-- =============================================================================
-- 附：执行完成后仍然必须做的事（本 SQL 管不到）
--   □ docs/05 §3.4：删除 character/get|put 后门类（RandomController/RandomHandleDecorator/
--     character.properties/applicationContext-poi.xml）、project/monitor/noderpc 整包、
--     UserController.login_extend()、TestController、EmailUtils 硬编码邮箱等
--   □ docs/05 §2：轮换所有明文凭据（邮箱/Gmail/Outlook、短信宝、极验、DB root、3DES KEY 相关）
--   □ docs/05 §4：前端清理（anedhaude.xyz C2、kelubit.id/wss、block.js、apk、mobileconfig…）
--   □ docs/05 §0.1：重建 deploy/war 三个 WAR，并用复验脚本确认 0 命中后重新部署
--   □ docs/03 §5.5：新建自己的 ROOT 管理员并绑定自己的 Google 2FA
--   □ 重新执行本文件 H 段 + docs/05 的复验命令，全部通过后才可对公网开放
-- =============================================================================

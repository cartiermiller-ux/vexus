# VEXUS Exchange

VEXUS 加密货币交易所项目源码与部署配置。

## 目录结构

```
source/             Java 源码（admin / api / data / comm / dapp / model）
frontend/zcx/       PC + WAP 编译后前端
deploy/             部署配置、Nginx、SQL 补丁、构建脚本
patches/            部署过程中新增/修改的后端类源码
docs/               部署与安全文档
部署教程.md          快速部署入口
```

## 环境

- JDK 8
- Maven 3.x（源码构建）
- Tomcat 9
- MySQL 5.7
- Redis 5.x
- Nginx

## 部署

参考：

- `部署教程.md`
- `docs/01-部署总览.md`
- `docs/02-后端部署.md`
- `docs/03-数据库与配置.md`
- `docs/04-前端与Nginx.md`
- `docs/05-安全加固与账号.md`

## 重要说明

- 仓库不包含 WAR、数据库全量 dump、服务器证书、日志和运行时数据。
- 数据库、邮箱、短信、DeepSeek、Alpaca 等凭据必须放在部署环境配置或 `t_syspara` 中，不要提交到仓库。
- 源码中的 `REPLACE_ME` 为占位符，部署前必须修改。
- 生产环境请更换管理员密码、开启后台二次验证、限制面板/SSH 来源 IP，并配置备份。
- `patches/` 是部署时对源码的补充修改，合并回源码模块后重新构建 WAR。

## 新增功能

- 股票行情 / 全球股票 / TradFi / 资讯 / 模拟交易 / 活动 / 储备金证明页面
- 币种图标、Lucide 导航图标
- AI 在线客服（DeepSeek 代理 + 在线客服自动回复）
- Alpaca IEX 行情与 SSE 逐笔推送（服务端单连接桥接）
- 顶部导航下拉、互斥、hover、间距优化

服务端额外依赖与密钥不在此仓库中，部署时按文档配置。

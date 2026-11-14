# deploy/tools —— DB 密码加解密工具（3DES + Base64 + URLEncode）

Vexus 交易所的 `jdbc.password` / `db.password` **不是明文**，而是被 Java 端固定 KEY 加密后的字符串。
换数据库密码 / 换库 / 排错时，必须用本目录工具生成新密文（或解密现有密文）。

| 文件 | 语言/依赖 | 用途 |
| --- | --- | --- |
| `db_password.py` | Python 3（**纯标准库**，自带 DES/3DES 实现） | 跨平台首选；含已知样例自测 |
| `DbPassword.ps1` | Windows PowerShell 5.1+（.NET TripleDES） | Windows 服务器/本机快速加解密 |
| `db_password.sh` | bash + openssl + md5sum + iconv | Linux/Git Bash；部署服务器上直接用 |
| `README.md` | — | 本文件 |

---

## 1. 算法（与 Java 源码逐行对齐）

实现来源：`源代码/comm/Kernel/src/kernel/util/Endecrypt.java`、
`源代码/comm/Kernel/src/kernel/druid/LocalDruidDataSource.java`

```text
KEY          = "Roj6#@08SDF87323FG00%jjsd"          （写死在 LocalDruidDataSource.java:13）
密钥         = MD5( KEY 的 GBK 字节 ) 的 16 字节 + 8 个 0x00 = 24 字节
明文编码     = UTF-16LE
加密算法     = DESede / ECB / PKCS5Padding（PKCS5 在 8 字节块上等价 PKCS7）
密文编码     = Base64（标准字母表、无换行）→ URLEncode（Java URLEncoder，UTF-8）
解密顺序     = URLDecode → Base64Decode → DESede 解密 → UTF-16LE 解码
```

Java 端使用方式：`LocalDruidDataSource.setPassword()` 在 Druid 初始化前把配置里的
密文解密成真实密码（`super.setPassword(endecrypt.get3DESDecrypt(password, KEY))`），
所以 **`config.properties` 里必须填密文**，填明文会导致连接池初始化失败（解密出乱码）。

### 已知样例（三份实现都必须通过）

```text
密文: nY0NTjBcrC0nk6q9zp%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%3D%3D
明文: Err;2[eoGFUriwdgr
```

另有旧源码版 `源代码/admin/src/main/resources/config.properties` 的样例：
`xpk4ii2rnyR0NsW%2BNNfzSA%3D%3D` → 明文 `root`（即旧默认库密码就是 root）。

---

## 2. 用法

### 2.1 Python（推荐，纯标准库）

```bash
# 解密现有配置里的密码
python3 db_password.py decrypt "nY0NTjBcrC0nk6q9zp%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%3D%3D"
# -> Err;2[eoGFUriwdgr

# 把新密码加密（输出直接粘到 config.properties 的 jdbc.password）
python3 db_password.py encrypt 'MyStr0ng#Pass'
# -> 生成的密文（形如 xxxxx%%2Fxx%%3D%%3D）

# 自测（4 项断言，含已知密文/已知明文双向校验）
python3 db_password.py selftest
# -> selftest: PASS

# 换 KEY（一般不需要）
python3 db_password.py encrypt 'pwd' --key 'OtherKey'
```

### 2.2 Windows PowerShell

> ⚠️ `DbPassword.ps1` 含中文注释，**必须保存为 UTF-8 with BOM**（Windows PowerShell 5.1 默认按 ANSI/GBK 解析无 BOM 的 UTF-8，会报 `在此对象上找不到属性"Mode"` 之类的诡异错误）。本文件已是 UTF-8 BOM；若你用编辑器另存后报错，请重新加 BOM：
> `powershell -c "$p='DbPassword.ps1'; [IO.File]::WriteAllText($p,[IO.File]::ReadAllText($p,[Text.Encoding]::UTF8),[Text.UTF8Encoding]::new($true))"`

```powershell
cd deploy\tools

# 解密
.\DbPassword.ps1 -Action decrypt -CipherText "nY0NTjBcrC0nk6q9zp%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%3D%3D"

# 加密
.\DbPassword.ps1 -Action encrypt -PlainText "MyStr0ng#Pass"

# 交互式输入（明文不留在命令历史里）
.\DbPassword.ps1 -Action encrypt -Prompt

# 自测
.\DbPassword.ps1 -Action selftest
```

若系统禁用了脚本执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\DbPassword.ps1 -Action decrypt -CipherText "..."
```

### 2.3 Linux / Git Bash（openssl）

```bash
cd deploy/tools
chmod +x db_password.sh

./db_password.sh decrypt 'nY0NTjBcrC0nk6q9zp%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%3D%3D'
./db_password.sh encrypt 'MyStr0ng#Pass'
./db_password.sh selftest          # -> selftest: PASS
./db_password.sh encrypt 'pwd' --key 'OtherKey'
```

依赖：`openssl`（需支持 `des-ede3`）、`md5sum`（或 macOS `md5`）、`base64`、`iconv`。
- Windows 可在 **Git Bash** 里直接跑（openssl 位于 `C:\Program Files\Git\usr\bin\openssl.exe`，Git Bash 已加入 PATH）。
- 校验 openssl 支持 3DES：`openssl enc -ciphers | grep des-ede3`。

---

## 3. 三份实现的互验结果（本次实测）

以 `Test@123456` 为明文，三份工具输出**完全一致**，且与 Java 端 `Endecrypt.get3DESEncrypt`
（同为 DESede/ECB/PKCS5 + UTF-16LE + Base64 + URLEncode）结果一致：

| 工具 | 已知密文解密 | 加密 `Test@123456` 结果 |
| --- | --- | --- |
| `db_password.py` | `Err;2[eoGFUriwdgr` ✅ | `NOyIeShWtE9E9Owj2hrAHHW5f1%2Blg7lT` |
| `DbPassword.ps1` | `Err;2[eoGFUriwdgr` ✅ | `NOyIeShWtE9E9Owj2hrAHHW5f1%2Blg7lT` |
| `db_password.sh` | `Err;2[eoGFUriwdgr` ✅ | `NOyIeShWtE9E9Owj2hrAHHW5f1%2Blg7lT` |

反向校验（用已知明文重新加密必须得到已知密文）三份工具也全部通过：

```text
encrypt("Err;2[eoGFUriwdgr") == "nY0NTjBcrC0nk6q9zp%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%3D%3D"   ✅
```

---

## 4. 修改数据库密码的标准流程

1. 在 MySQL 里改密码：
   ```sql
   ALTER USER 'vexus'@'127.0.0.1' IDENTIFIED BY 'MyStr0ng#Pass';
   FLUSH PRIVILEGES;
   ```
2. 用工具生成密文：`python3 db_password.py encrypt 'MyStr0ng#Pass'`
3. 把密文填到 **所有** 用到该库的配置里（改一处漏一处会连不上）：
   - `web3_project/admin_u/WEB-INF/classes/config.properties` → `jdbc.password`
   - `web3_project/data_u/WEB-INF/classes/config.properties` → `jdbc.password`
   - `web3_project/admin_u/WEB-INF/classes/config/config.properties` → `jdbc.password`（该文件是副本，若存在一并改）
   - `web3_project/data_u/WEB-INF/classes/config/db.properties` → `db.password`（DB 备份/恢复模块用）
   - 旧源码版对应 `源代码/{admin,api,data}/src/main/resources/...` 与 `.../target/classes/...`
4. 重启 Tomcat（Druid 连接池启动时解密，热改配置不生效）。
5. 验证：看 `catalina.out` 无 `Access denied for user` / 解密异常，后台能正常登录。

> 注意：`%` 在 `.properties` 里是普通字符，**直接原样粘贴**即可（不需要转义成 `%%`；
> 上面示例里的 `%%` 只是 bash/printf 场景的写法）。
> 但如果你用 `sed -i` 批量替换，注意 `&`、`/` 需转义。

---

## 5. 安全注意事项

1. **KEY 是写死在源码里的**（`Roj6#@08SDF87323FG00%jjsd`），拿到源码就能解密数据库密码。
   因此：不要把源码包/配置包公开；生产环境数据库账号要限权（见 `docs/03` 第 7 节），
   仅靠密文并不安全，**核心是数据库只监听 127.0.0.1 + 低权限账号**。
2. 生成的密文等同于密码，**不要**提交到 Git、不要发在群里；本目录文件权限建议 `chmod 600`。
3. 明文尽量用 `-Prompt`/交互输入，避免留在 `~/.bash_history`、PowerShell `PSReadLine` 历史里
   （PowerShell 可用 `Clear-History`；bash 可 `history -c`）。
4. 换 KEY 属于「破坏性变更」：必须同时改 `LocalDruidDataSource.java` 的 `KEY` 并重新编译所有应用，
   否则配置与代码不一致，连接池全部起不来。除非有明确需求，**不要换 KEY**。
5. 本工具只处理数据库密码；`t_syspara.backup_server_param` 里的备份服务器密码也用同一套 3DES，
   可用同样命令解密（JSON 里那串 `...%3D%3D`）。

---

## 6. 常见问题

| 现象 | 原因 | 处理 |
| --- | --- | --- |
| `解密失败：PKCS#5/7 填充非法` / 解密出乱码 | KEY 不对、密文被截断、或把明文当密文解 | 确认用的是本项目 KEY；密文里的 `%2F`、`%3D` 不要手工改 |
| `解密失败：密文长度不是 8 的整数倍` | URL 编码被破坏（例如 `+` 被转成空格） | 用单引号包住密文，避免 shell 处理 `%`；用文件读取 |
| PowerShell 报找不到属性 `Mode` | 脚本被存成无 BOM 的 UTF-8 | 按 2.2 节说明加 BOM |
| openssl 报 `unknown cipher des-ede3` | 系统 openssl 编译时裁掉了 3DES | 用 Python 版本，或安装完整 openssl |
| 配置改了但启动仍连旧库 | 改的是 `config/config.properties` 副本，实际加载的是 `classes/config.properties` | 以 `applicationContext.xml` 的 `property-placeholder location="classpath:config.properties"` 为准（root 那份），两个都改最稳 |
| 密文里出现换行 | 手工从终端复制被折行 | 密文是单行，不要带空格/换行 |

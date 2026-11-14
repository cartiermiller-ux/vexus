#requires -Version 5.1
<#
.SYNOPSIS
  Vexus Java 交易所（「源代码」旧版）三应用 WAR 构建脚本（Windows；Windows PowerShell 5.1 / PowerShell 7 均可）。

.DESCRIPTION
  从原始源码目录组装 admin / api / data 三个 exploded webapp，再分别打包为 WAR，
  不需要 JDK / Maven / Tomcat，只使用 .NET 自带的 ZipArchive，可在任意 Windows 机器上重复执行。

  每个应用的输入（只读，不修改原始文件）：
    <项目根>\源代码\<app>\target\classes         编译产物 -> WAR 内 WEB-INF/classes
    <项目根>\源代码\<app>\target\dependency      依赖 jar -> WAR 内 WEB-INF/lib（排除容器提供的 jar）
    <项目根>\源代码\<app>\src\main\webapp        JSP/静态资源 -> WAR 根目录（不含 webapp/WEB-INF，web.xml 单独取）
    <项目根>\web3_project\admin_u\normal         仅 admin：旧版 JSP 主题目录（从新版预编译包补的静态资源）

  输出：
    <deploy>\war\admin.war  api.war  data.war
    exploded 临时目录默认 %TEMP%\vexus-war-build（可用 -WorkDir 改；-KeepWork 保留以便排查）

  说明（构建前已核实）：
    1. 旧版持久层是 Spring JDBC（JdbcTemplate + kernel.bo.RecordObjectMapper），实体类只用 JPA
       注解（@Table/@Column）作列元数据，没有 Hibernate SessionFactory，因此【不需要、也不复制】
       *.hbm.xml。web3_project 里的 97/96 个 hbm 属于新版 Dubbo 包，混入只会带来版本风险。
    2. target\dependency 是 maven-dependency-plugin 的全量拷贝（含 provided 范围的容器 jar）。
       WAR 内 WEB-INF/lib 必须排除容器提供的 jar，否则旧版 kernel.web.RmiProxyFactoryBean 通过
       反射访问 Tomcat 内部类时会拿到 webapp 类加载器加载的副本而失败：
         排除 tomcat-*.jar、javax.servlet-api-*.jar、javax.websocket-api-*.jar。
       保留其余全部 jar（含 netty-socketio、web3j、spring-*、druid、mysql 驱动等）。
    3. WAR 内路径层级：根为 JSP / CSS / JS / IMG / WEB-INF，不允许再多一层 apps 名目录。
    4. 垃圾文件排除：.DS_Store、Thumbs.db、desktop.ini、~$*、*.log、*.tmp、*.bak 等。
    5. 构建 admin.war 时会给 include/basePath.jsp 等 6 个 JSP 打「Nginx 反代兼容补丁」：
       原版 basePath = scheme://serverName:serverPort/admin/，放在 Nginx HTTPS 反代后会出现
       http://域名:8080/... 或 http://域名:443/... 的错误地址；补丁改为优先读取
       X-Forwarded-Proto / X-Forwarded-Host / X-Forwarded-Port，回退还原本地值。
       补丁幂等（检测到 X-Forwarded-Proto 即跳过），不修改原始源码目录。
       部署时 Nginx 必须传 X-Forwarded-* 头，Tomcat 建议再加 RemoteIpValve，见 docs/02。
    6. 【安全清理，随构建强制执行】原始 target\classes 是卖家旧编译产物，含后门类与旧凭据。
       构建时从 WEB-INF/classes 删除（删除清单见 $script:SecurityRemovalPaths）：
         org/apache/poi/assistant/**、org/apache/poi/extracto/**（character 后门工具类）
         project/web/admin/poi/RandomController.class（未授权后门接口 character/get|put）
         spring/applicationContext-poi.xml、spring/character.properties（后门 bean 与固定密钥）
         project/web/api/TestController.class、email/EmailUtils*.class（残留测试/硬编码邮箱类）
       并强制把配置里的旧 DB/邮箱/短信/域名/私钥占位脱敏（见 Invoke-ConfigSanitize）。
       注意：project/monitor/noderpc/**（外部钱包远程通道）**未删除**——precompiled 类被
       AdminAutoMonitorWalletController @Autowired 与多个 job bean setter 强依赖，直接删会导致
       Spring 启动失败；必须在数据库层禁用（清空 t_syspara 的 rpc_node_url/node_project_code）
       并按 docs/02 的上线检查项处理。构建结果里的 NodeRpc 条目数会在校验表中如实列出。
    7. 【RMI 端口修复，随构建强制执行】原始 kernel/web/RmiProxyFactoryBean.class 有致命缺陷：
       按 Tomcat findChildren() 的 HashMap 迭代顺序分配 20001+ RMI 端口，三个应用在同一 Tomcat
       依次启动时第 3 个会算出重复端口 → ExportException: ObjID already in use，启动失败；
       且 servletContextMaps 只快照一次，先启动的应用看不到后启动的 context。
       构建时用 deploy/build/patched/kernel/web/RmiProxyFactoryBean.class（源码见
       deploy/build/patches/kernel/web/RmiProxyFactoryBean.java）覆盖到每个 WEB-INF/classes；
       补丁文件不存在时**直接报错退出**，绝不静默生成会挂的包。
       补丁逻辑：contextPath 确定性哈希分配端口（与启动顺序无关）、动态刷新 context 表、
       ROOT 固定 20999。若用 Maven 从原始源码构建，必须手动把这个补丁类覆盖回去。

.PARAMETER ProjectRoot
  项目根目录（其中包含「源代码」与「web3_project」两个子目录）。
  不传时自动在脚本所在的工作区根下探测唯一包含「源代码\admin\target\classes」的子目录。

.PARAMETER OutDir
  WAR 输出目录，默认 <脚本目录>\..\war，即交付物 deploy\war。

.PARAMETER WorkDir
  exploded 组装的临时工作目录，默认 %TEMP%\vexus-war-build（路径短，避免 Windows 260 字符限制）。

.PARAMETER Apps
  要构建的应用，默认 @('admin','api','data')。

.PARAMETER SkipTheme
  不把 web3_project\admin_u\normal 拷进 admin.war（默认会拷，属于静态主题资源，不影响后端代码）。

.PARAMETER KeepWork
  构建结束后保留 exploded 临时目录（默认构建完即删除，保证幂等且不占空间）。

.PARAMETER Compression
  war 内普通文件的压缩级别：Fastest（默认，推荐）/ Optimal / NoCompression。
  jar、图片、字体等已压缩文件固定按 NoCompression 存储，避免无意义的 CPU 开销。

.EXAMPLE
  # 最简用法（构建 3 个 WAR 到 deploy\war）
  powershell -ExecutionPolicy Bypass -File .\build-war.ps1
  pwsh -File .\build-war.ps1

.EXAMPLE
  # 指定项目根和输出目录，保留 exploded 目录用于排查
  powershell -ExecutionPolicy Bypass -File .\build-war.ps1 -ProjectRoot "D:\Vexus\Vexus虚拟货币交易所源码" -OutDir "D:\deliver\war" -KeepWork

.EXAMPLE
  # 只构建 admin，不补主题资源
  powershell -ExecutionPolicy Bypass -File .\build-war.ps1 -Apps admin -SkipTheme

.NOTES
  幂等：重复执行会重建临时目录并覆盖 WAR；原始源码目录全程只读。
  环境要求：Windows 自带的 Windows PowerShell 5.1（无需安装任何东西）或 PowerShell 7 均可；
  只依赖 .NET 自带的 System.IO.Compression。本脚本无法启动 Tomcat/JDK 做真实运行验证，
  构建结果请用脚本末尾的静态校验清单或 deploy\war\MANIFEST.md 中的命令复核。
#>
[CmdletBinding()]
param(
    [string]$ProjectRoot,
    [string]$OutDir,
    [string]$WorkDir,
    [string[]]$Apps = @('admin', 'api', 'data'),
    [switch]$SkipTheme,
    [switch]$KeepWork,
    [ValidateSet('Fastest', 'Optimal', 'NoCompression')]
    [string]$Compression = 'Fastest'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# .NET 自带的 zip 组件（Windows PowerShell 5.1 需要显式加载；PowerShell 7 默认已加载）
try { Add-Type -AssemblyName System.IO.Compression -ErrorAction Stop } catch { }
try { Add-Type -AssemblyName System.IO.Compression.FileSystem -ErrorAction Stop } catch { }

# 压缩级别映射（避免使用 PS7 专有语法）
$CompressionLevelMap = @{
    'Fastest'       = [System.IO.Compression.CompressionLevel]::Fastest
    'Optimal'       = [System.IO.Compression.CompressionLevel]::Optimal
    'NoCompression' = [System.IO.Compression.CompressionLevel]::NoCompression
}
$StoreLevel = [System.IO.Compression.CompressionLevel]::NoCompression

# ---------------------------------------------------------------------------
# 0. 路径解析
# ---------------------------------------------------------------------------
$ScriptDir = $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($ScriptDir)) { $ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path }
$DeployRoot = Split-Path -Parent $ScriptDir          # <工作区>\deploy
$WorkspaceRoot = Split-Path -Parent $DeployRoot      # <工作区>

function Resolve-ProjectRoot {
    param([string]$Explicit, [string]$Workspace)
    if (-not [string]::IsNullOrWhiteSpace($Explicit)) {
        $p = $Explicit
        if (-not (Test-Path -LiteralPath $p)) { throw "指定的 -ProjectRoot 不存在: $p" }
        return (Resolve-Path -LiteralPath $p).ProviderPath
    }
    if (-not [string]::IsNullOrWhiteSpace($env:VEXUS_PROJECT_ROOT)) {
        $p = $env:VEXUS_PROJECT_ROOT
        if (Test-Path -LiteralPath $p) { return (Resolve-Path -LiteralPath $p).ProviderPath }
    }
    # 自动探测：工作区根下唯一一个包含 源代码\admin\target\classes 的目录
    $candidates = @()
    if (Test-Path -LiteralPath $Workspace) {
        $candidates = @(Get-ChildItem -LiteralPath $Workspace -Directory -ErrorAction SilentlyContinue |
            Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName '源代码\admin\target\classes') } |
            ForEach-Object { $_.FullName })
    }
    if (@($candidates).Count -eq 1) { return @($candidates)[0] }
    if (@($candidates).Count -eq 0) {
        throw "无法自动定位项目根：请用 -ProjectRoot 指定包含『源代码』目录的项目根（工作区: $Workspace）。"
    }
    throw "工作区下发现多个候选项目根，请用 -ProjectRoot 显式指定：`n$($candidates -join "`n")"
}

$ProjectRoot = Resolve-ProjectRoot -Explicit $ProjectRoot -Workspace $WorkspaceRoot
if ([string]::IsNullOrWhiteSpace($OutDir)) { $OutDir = Join-Path $DeployRoot 'war' }
if ([string]::IsNullOrWhiteSpace($WorkDir)) { $WorkDir = Join-Path ([System.IO.Path]::GetTempPath()) 'vexus-war-build' }
$OutDir = [System.IO.Path]::GetFullPath($OutDir)
$WorkDir = [System.IO.Path]::GetFullPath($WorkDir)
$SourceRoot = Join-Path $ProjectRoot '源代码'
$ThemeSource = Join-Path $ProjectRoot 'web3_project\admin_u\normal'

# ---------------------------------------------------------------------------
# RMI 端口修复补丁（必须存在，见脚本头部第 7 条）
# ---------------------------------------------------------------------------
$RmiPatchClass = Join-Path $ScriptDir 'patched\kernel\web\RmiProxyFactoryBean.class'
$RmiPatchSource = Join-Path $ScriptDir 'patches\kernel\web\RmiProxyFactoryBean.java'
if (-not (Test-Path -LiteralPath $RmiPatchClass)) {
    throw @"
缺少 RMI 端口修复补丁类：$RmiPatchClass
这是必需文件（3 个应用放同一 Tomcat 时必须覆盖，否则第 3 个应用会因 RMI 端口重复启动失败）。
补丁源码在：$RmiPatchSource
若补丁类丢失，可用 JDK8 重新编译（在项目根执行）：
  javac -source 8 -target 8 -encoding UTF-8 -cp "源代码/admin/target/classes;源代码/admin/target/dependency/*" -d deploy/build/patched deploy/build/patches/kernel/web/RmiProxyFactoryBean.java
"@
}

# 输出目录只能在 deploy 下（防止误写原始源码）
$deployFull = [System.IO.Path]::GetFullPath($DeployRoot)
if (-not $OutDir.StartsWith($deployFull, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "出于安全考虑，-OutDir 必须位于 deploy 目录下：$deployFull"
}

if (-not (Test-Path -LiteralPath $SourceRoot)) { throw "未找到源码目录: $SourceRoot" }
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
New-Item -ItemType Directory -Force -Path $WorkDir | Out-Null

# ---------------------------------------------------------------------------
# 1. 过滤规则
# ---------------------------------------------------------------------------
$JunkNames = @('.DS_Store', 'Thumbs.db', 'desktop.ini', '.gitignore', '.gitkeep', '.gitattributes')
$JunkPatterns = @('~$*', '*.log', '*.tmp', '*.bak', '*.swp', '*.orig', '*.rej')

# 容器（Tomcat 9）已提供的 jar：绝对不能进 WEB-INF/lib
$ContainerJarPatterns = @('tomcat-*.jar', 'javax.servlet-api-*.jar', 'javax.websocket-api-*.jar')

# 已压缩的文件：zip 内按 no-compression 存储
$PrecompressedExtensions = @(
    '.jar', '.zip', '.gz', '.7z', '.rar', '.png', '.jpg', '.jpeg', '.gif', '.ico', '.webp',
    '.woff', '.woff2', '.ttf', '.eot', '.otf', '.pdf', '.mp3', '.mp4', '.apk'
)

function Test-JunkFile {
    param([System.IO.FileInfo]$File)
    if ($JunkNames -contains $File.Name) { return $true }
    foreach ($p in $JunkPatterns) { if ($File.Name -like $p) { return $true } }
    return $false
}

function Test-ContainerJar {
    param([string]$Name)
    foreach ($p in $ContainerJarPatterns) { if ($Name -like $p) { return $true } }
    return $false
}

# ---------------------------------------------------------------------------
# 1.1 安全清理：卖家旧编译产物中的后门类/后门配置（必须随构建删除）
#     依据 docs/05-安全加固与账号.md §0.1① / §3.4；删除后请勿再从 target\classes 反向恢复。
# ---------------------------------------------------------------------------
$script:SecurityRemovalPaths = @(
    'org\apache\poi\assistant',                    # character 后门辅助类（含 MD5/签名工具）
    'org\apache\poi\extracto',                     # character 后门核心（RandomHandleDecorator 等）
    'project\web\admin\poi',                       # RandomController：未授权 character/get|put
    'project\web\api\TestController.class',        # 残留内部行情测试接口
    'spring\applicationContext-poi.xml',           # 后门 bean 定义（会重新注册 extracto 类）
    'spring\character.properties',                 # character 后门固定密钥 123dfs9345-...
    'email\EmailUtils.class',                      # 硬编码 163 邮箱的残留类
    'email\EmailUtils$1.class'
)

function Remove-SecurityRiskArtifacts {
    <#
      删除 exploded WEB-INF/classes 中的后门/残留条目；返回被删除的相对路径清单。
      说明：noderpc 包不在此处删除（会破坏 Spring 启动），见脚本头部第 6 条说明与 docs/02。
    #>
    param([Parameter(Mandatory)][string]$ClassesDir)
    $removed = New-Object System.Collections.Generic.List[string]
    foreach ($rel in $script:SecurityRemovalPaths) {
        $p = Join-Path $ClassesDir $rel
        if (Test-Path -LiteralPath $p) {
            Remove-Item -LiteralPath $p -Recurse -Force
            $removed.Add($rel)
        }
    }
    # 兜底：移除名称恰为 TestController 的残留测试类
    # 注意：必须区分大小写并精确匹配，不能用 *TestController*.class（大小写不敏感会误删 GeetestController）
    foreach ($f in @(Get-ChildItem -LiteralPath $ClassesDir -Recurse -File -ErrorAction SilentlyContinue)) {
        if ($f.Name -cmatch '^TestController(\$[0-9]+)?\.class$') {
            $rel = $f.FullName.Substring($ClassesDir.Length + 1)
            Remove-Item -LiteralPath $f.FullName -Force
            if (-not $removed.Contains($rel)) { $removed.Add($rel) }
        }
    }
    return $removed
}

# ---------------------------------------------------------------------------
# 1.2 配置脱敏：把卖家旧凭据/域名替换为占位符（必须随构建强制覆盖）
#     注意：jdbc.password 必须是 3DES 密文（kernel.druid.LocalDruidDataSource 会解密），
#     部署前用 deploy/tools 生成后替换 REPLACE_WITH_3DES_CIPHERTEXT。
# ---------------------------------------------------------------------------
function Invoke-ConfigSanitize {
    param([Parameter(Mandatory)][string]$ClassesDir)
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    $jdbcUrl = 'jdbc:mysql://127.0.0.1:3306/jiaoyipan?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true'
    $rules = @{}
    $rules['config.properties'] = @(
        @{ P = '(?m)^[ \t]*jdbc\.url[ \t]*=.*$'; R = "jdbc.url = $jdbcUrl" },
        @{ P = '(?m)^[ \t]*jdbc\.username[ \t]*=.*$'; R = 'jdbc.username = vexus' },
        @{ P = '(?m)^[ \t]*jdbc\.password[ \t]*=.*$'; R = 'jdbc.password = REPLACE_WITH_3DES_CIPHERTEXT' },
        @{ P = '(?m)^[ \t]*email\.host[ \t]*=.*$'; R = 'email.host = your-smtp-host' },
        @{ P = '(?m)^[ \t]*email\.username[ \t]*=.*$'; R = 'email.username =' },
        @{ P = '(?m)^[ \t]*email\.password[ \t]*=.*$'; R = 'email.password =' },
        @{ P = '(?m)^[ \t]*email\.from[ \t]*=.*$'; R = 'email.from =' },
        @{ P = '(?m)^[ \t]*images\.dir[ \t]*=.*$'; R = 'images.dir = /www/wwwroot/vexus/images/' }
    )
    $rules['config\config.properties'] = @(
        @{ P = '(?m)^[ \t]*jdbc\.url[ \t]*=.*$'; R = "jdbc.url = $jdbcUrl" },
        @{ P = '(?m)^[ \t]*jdbc\.username[ \t]*=.*$'; R = 'jdbc.username = vexus' },
        @{ P = '(?m)^[ \t]*jdbc\.password[ \t]*=.*$'; R = 'jdbc.password = REPLACE_WITH_3DES_CIPHERTEXT' }
    )
    $rules['config\email.properties'] = @(
        @{ P = '(?m)^[ \t]*email\.host[ \t]*=.*$'; R = 'email.host = your-smtp-host' },
        @{ P = '(?m)^[ \t]*email\.username[ \t]*=.*$'; R = 'email.username =' },
        @{ P = '(?m)^[ \t]*email\.password[ \t]*=.*$'; R = 'email.password =' },
        @{ P = '(?m)^[ \t]*email\.from[ \t]*=.*$'; R = 'email.from =' }
    )
    $rules['config\sms253.properties'] = @(
        @{ P = '(?m)^[ \t]*sms\.account[ \t]*=.*$'; R = 'sms.account =' },
        @{ P = '(?m)^[ \t]*sms\.pswd[ \t]*=.*$'; R = 'sms.pswd =' },
        @{ P = '(?m)^[ \t]*sms253\.inter\.account[ \t]*=.*$'; R = 'sms253.inter.account =' },
        @{ P = '(?m)^[ \t]*sms253\.inter\.pswd[ \t]*=.*$'; R = 'sms253.inter.pswd =' }
    )
    $rules['config\system.properties'] = @(
        @{ P = '(?m)^[ \t]*admin_url[ \t]*=.*$'; R = 'admin_url=https://your-domain.com/admin' },
        @{ P = '(?m)^[ \t]*web_url[ \t]*=.*$'; R = 'web_url=https://your-domain.com/wap/' },
        @{ P = '(?m)^[ \t]*http\.server\.host[ \t]*=.*$'; R = 'http.server.host=https://your-domain.com/images/' },
        @{ P = '(?m)^[ \t]*images\.dir[ \t]*=.*$'; R = 'images.dir=/www/wwwroot/vexus/images/' },
        @{ P = '(?m)^[ \t]*encryption\.key[ \t]*=.*$'; R = 'encryption.key=REPLACE_WITH_RANDOM_32_HEX' }
    )
    $rules['config\autoe2due.properties'] = @(
        @{ P = '(?m)^[ \t]*#?[ \t]*chartext[ \t]*=.*$'; R = 'chartext=REPLACE_ME' }
    )
    $rules['config\db.properties'] = @(
        @{ P = '(?m)^[ \t]*db\.ip[ \t]*=.*$'; R = 'db.ip=127.0.0.1' },
        @{ P = '(?m)^[ \t]*db\.port[ \t]*=.*$'; R = 'db.port=3306' },
        @{ P = '(?m)^[ \t]*db\.database\.name[ \t]*=.*$'; R = 'db.database.name=jiaoyipan' },
        @{ P = '(?m)^[ \t]*db\.username[ \t]*=.*$'; R = 'db.username=vexus' },
        @{ P = '(?m)^[ \t]*db\.password[ \t]*=.*$'; R = 'db.password=REPLACE_WITH_3DES_CIPHERTEXT' },
        @{ P = '(?m)^[ \t]*db\.backup\.path[ \t]*=.*$'; R = 'db.backup.path=/www/wwwroot/vexus/backup' }
    )
    $changedFiles = 0
    foreach ($rel in $rules.Keys) {
        $f = Join-Path $ClassesDir $rel
        if (-not (Test-Path -LiteralPath $f)) { continue }
        $text = [System.IO.File]::ReadAllText($f, [System.Text.Encoding]::UTF8)
        $newText = $text
        foreach ($rule in $rules[$rel]) {
            $newText = [regex]::Replace($newText, $rule.P, $rule.R)
        }
        if ($newText -ne $text) {
            [System.IO.File]::WriteAllText($f, $newText, $utf8NoBom)
            $changedFiles++
        }
    }
    return $changedFiles
}

# ---------------------------------------------------------------------------
# 1.3 RMI 端口修复补丁安装：用部署包自带的补丁 class 覆盖 kernel/web/RmiProxyFactoryBean.class
#     背景：原实现按 Tomcat findChildren() 的 HashMap 顺序分配端口，3 个应用同容器会端口冲突，
#     第 3 个应用启动 ExportException: ObjID already in use；且 context 表只快照一次。
#     补丁源码/产物必须随交付包存在（脚本开头已强校验），这里做覆盖 + 放一份说明到 classes 内。
# ---------------------------------------------------------------------------
function Install-RmiPatch {
    param([Parameter(Mandatory)][string]$ClassesDir)
    $patchDir = Split-Path -Parent $RmiPatchClass
    $patchFiles = @(Get-ChildItem -LiteralPath $patchDir -File -Filter 'RmiProxyFactoryBean*.class')
    if ($patchFiles.Count -eq 0) { throw "RMI 补丁目录中没有 RmiProxyFactoryBean*.class：$patchDir" }
    $destDir = Join-Path $ClassesDir 'kernel\web'
    if (-not (Test-Path -LiteralPath $destDir)) { New-Item -ItemType Directory -Force -Path $destDir | Out-Null }
    # 必须复制全部内部类（$DynamicContextMap / $DeterministicPortMap / $1），只复制外层类会运行期 NoClassDefFoundError
    foreach ($pf in $patchFiles) {
        Copy-Item -LiteralPath $pf.FullName -Destination (Join-Path $destDir $pf.Name) -Force
    }
    $notePath = Join-Path $destDir 'RMI-PATCH-README.txt'
    $noteLines = @(
        'RMI 端口修复补丁（构建时由 deploy/build/build-war.ps1 自动覆盖）',
        '被替换文件: kernel/web/RmiProxyFactoryBean.class',
        '补丁源码  : deploy/build/patches/kernel/web/RmiProxyFactoryBean.java',
        '补丁 class: deploy/build/patched/kernel/web/RmiProxyFactoryBean.class',
        '---',
        '原始实现问题:',
        '  1) 用 Tomcat findChildren() 的 HashMap 迭代顺序给 context 分配 20001+ 端口，',
        '     三个应用在同一 Tomcat 依次启动时，第 3 个应用会算出已被占用的端口，',
        '     抛 java.rmi.server.ExportException: internal error: ObjID already in use，启动失败。',
        '  2) servletContextMaps 只在类初始化时快照一次，先启动的应用看不到后启动的 context，',
        '     跨应用 RMI 调用会 NOT_FOUND_CONTEXT / NOT_IMPLEMENTED_INTERFACE。',
        '---',
        '修复方式:',
        '  1) 端口按 contextPath 确定性哈希分配（20001 + |hashCode| % 800），与启动顺序/数量无关；',
        '  2) servletContextMaps 改为动态视图，每次 containsKey/get/entrySet 时重新读取 Tomcat children；',
        '  3) ROOT（contextPath 为空）固定端口 20999，不再 NPE。',
        '---',
        '重要: 如果不用本交付的 WAR，而是用 Maven 从原始源码重新构建，必须把该补丁 class',
        '      覆盖到 3 个应用的 WEB-INF/classes/kernel/web/ 下，否则第 3 个应用启动必失败。'
    )
    [System.IO.File]::WriteAllLines($notePath, $noteLines, (New-Object System.Text.UTF8Encoding($false)))
}

function Copy-Tree {
    <#
      递归复制目录（跳过垃圾文件、可选跳过相对路径前缀），返回复制的文件数。
    #>
    param(
        [Parameter(Mandatory)][string]$Source,
        [Parameter(Mandatory)][string]$Destination,
        [string[]]$SkipRelativePrefix = @(),
        [switch]$JarsOnly,
        [switch]$ExcludeContainerJars
    )
    if (-not (Test-Path -LiteralPath $Source)) { return 0 }
    $copied = 0
    foreach ($f in Get-ChildItem -LiteralPath $Source -Recurse -File) {
        $rel = $f.FullName.Substring($Source.Length + 1)
        $skip = $false
        foreach ($prefix in $SkipRelativePrefix) {
            if ($rel.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) { $skip = $true; break }
        }
        if ($skip) { continue }
        if (Test-JunkFile -File $f) { continue }
        if ($JarsOnly -and $f.Extension -ne '.jar') { continue }
        if ($ExcludeContainerJars -and $f.Extension -eq '.jar' -and (Test-ContainerJar -Name $f.Name)) { continue }
        $destPath = Join-Path $Destination $rel
        $destDir = Split-Path -Parent $destPath
        if (-not (Test-Path -LiteralPath $destDir)) { New-Item -ItemType Directory -Force -Path $destDir | Out-Null }
        Copy-Item -LiteralPath $f.FullName -Destination $destPath -Force
        $copied++
    }
    return $copied
}

# admin JSP 的 Nginx 反代兼容补丁：替换 include/basePath.jsp 等文件里的 basePath/base 计算。
# 要求：Nginx 传递 X-Forwarded-Proto / X-Forwarded-Host / X-Forwarded-Port；Tomcat 可选加 RemoteIpValve。
$script:ProxyAwareHelperLines = @(
    'String proxyScheme = request.getHeader("X-Forwarded-Proto");',
    'if (proxyScheme == null || proxyScheme.trim().isEmpty()) { proxyScheme = request.getScheme(); }',
    'proxyScheme = proxyScheme.trim();',
    'String proxyHost = request.getHeader("X-Forwarded-Host");',
    'if (proxyHost == null || proxyHost.trim().isEmpty()) { proxyHost = request.getServerName(); }',
    'proxyHost = proxyHost.trim();',
    'String proxyPort = request.getHeader("X-Forwarded-Port");',
    'if (proxyPort == null) { proxyPort = ""; }',
    'proxyPort = proxyPort.trim();',
    'String proxyHostPort = proxyHost;',
    'if (!proxyPort.isEmpty()) {',
    '    if (!"80".equals(proxyPort) && !"443".equals(proxyPort)) { proxyHostPort = proxyHost + ":" + proxyPort; }',
    '} else {',
    '    int proxyServerPort = request.getServerPort();',
    '    if (proxyServerPort != 80 && proxyServerPort != 443) { proxyHostPort = proxyHost + ":" + proxyServerPort; }',
    '}',
    'String basePath = proxyScheme + "://" + proxyHostPort + path + "/";',
    'String base = proxyScheme + "://" + proxyHostPort + "/";'
)

function Invoke-AdminBasePathProxyPatch {
    <#
      对 exploded 目录中的 admin JSP 做 Nginx 反代兼容补丁（幂等）。
      返回已处理/跳过的文件清单。
    #>
    param([Parameter(Mandatory)][string]$StageDir)
    $report = New-Object System.Collections.Generic.List[string]
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    $targets = @(
        'include\basePath.jsp',
        'include\email_code.jsp',
        'include\google_auth_code.jsp',
        'include\jquery.jsp',
        'include\pagetop.jsp'
    )
    foreach ($rel in $targets) {
        $f = Join-Path $StageDir $rel
        if (-not (Test-Path -LiteralPath $f)) { continue }
        $content = [System.IO.File]::ReadAllText($f, [System.Text.Encoding]::UTF8)
        if ($content.Contains('X-Forwarded-Proto')) {
            $report.Add("$rel (已含补丁，跳过)")
            continue
        }
        $eol = if ($content.Contains("`r`n")) { "`r`n" } else { "`n" }
        $lines = $content -split "`r?`n"
        $out = New-Object System.Collections.Generic.List[string]
        $inserted = $false
        foreach ($line in $lines) {
            if (-not $inserted -and $line -match 'String\s+path\s*=\s*request\.getContextPath\(\);') {
                $out.Add($line)
                foreach ($h in $script:ProxyAwareHelperLines) { $out.Add($h) }
                $inserted = $true
                continue
            }
            if ($line -match 'String\s+basePath\s*=' -and $line.Contains('request.getServerName()')) { continue }
            if ($line -match 'String\s+base\s*=' -and $line.Contains('request.getServerName()')) { continue }
            $out.Add($line)
        }
        if (-not $inserted) { throw "无法定位 basePath 定义位置（$rel），请检查源文件是否变化。" }
        [System.IO.File]::WriteAllText($f, (($out -join $eol) + $eol), $utf8NoBom)
        $report.Add($rel)
    }
    # 个别页面用 request.getScheme()+"://"+request.getServerName() 拼 showimg 地址，改为使用已补丁的 base 变量
    $loan = Join-Path $StageDir 'simple_loan_order_edit.jsp'
    if (Test-Path -LiteralPath $loan) {
        $text = [System.IO.File]::ReadAllText($loan, [System.Text.Encoding]::UTF8)
        if ($text.Contains('<%=request.getScheme() +"://"+ request.getServerName()%>/wap/')) {
            $text = $text.Replace('<%=request.getScheme() +"://"+ request.getServerName()%>/wap/', '<%=base%>wap/')
            [System.IO.File]::WriteAllText($loan, $text, $utf8NoBom)
            $report.Add('simple_loan_order_edit.jsp')
        }
    }
    return $report
}

function New-WarArchive {    param(
        [Parameter(Mandatory)][string]$StageDir,
        [Parameter(Mandatory)][string]$WarPath,
        [Parameter(Mandatory)][string]$LevelName
    )
    if (Test-Path -LiteralPath $WarPath) { Remove-Item -LiteralPath $WarPath -Force }
    $defaultLevel = $CompressionLevelMap[$LevelName]
    $zip = [System.IO.Compression.ZipFile]::Open($WarPath, [System.IO.Compression.ZipArchiveMode]::Create)
    try {
        $files = Get-ChildItem -LiteralPath $StageDir -Recurse -File | Sort-Object FullName
        foreach ($f in $files) {
            $rel = $f.FullName.Substring($StageDir.Length + 1)
            $entryName = $rel.Replace('\', '/')                    # WAR 内统一正斜杠
            $ext = $f.Extension.ToLowerInvariant()
            if ($PrecompressedExtensions -contains $ext) {
                $entry = $zip.CreateEntry($entryName, $StoreLevel)
            }
            else {
                $entry = $zip.CreateEntry($entryName, $defaultLevel)
            }
            $es = $entry.Open()
            try {
                $fs = [System.IO.File]::OpenRead($f.FullName)
                try { $fs.CopyTo($es) } finally { $fs.Dispose() }
            }
            finally { $es.Dispose() }
        }
    }
    finally { $zip.Dispose() }
}

function Test-WarArchive {
    param(
        [Parameter(Mandatory)][string]$WarPath,
        [Parameter(Mandatory)][string]$App
    )
    # 安全复验（与 docs/05 §0.1① 一致）：后门条目必须 0 命中；旧凭据不得出现在配置文件里
    # 注意用 -cmatch 区分大小写：TestController 不能误伤 GeetestController
    $backdoorPattern = 'org/apache/poi/(assistant|extracto)|RandomController|character\.properties|applicationContext-poi|/TestController(\$[0-9]+)?\.class$|email/EmailUtils'
    $noderpcPattern = 'project/monitor/noderpc'
    $secretPatterns = @('xpk4ii2rny', '1FW*VDZ', 'sendtoautotocodesendto', '2uVS6UkYyV7', '123dfs9345', 'SAObaoa520', 'jecyqcygieqdayvnf', 'chartext=Ufi32')
    $configEntries = @(
        'WEB-INF/classes/config.properties',
        'WEB-INF/classes/config/config.properties',
        'WEB-INF/classes/config/email.properties',
        'WEB-INF/classes/config/sms253.properties',
        'WEB-INF/classes/config/system.properties',
        'WEB-INF/classes/config/autoe2due.properties',
        'WEB-INF/classes/config/db.properties'
    )
    $leaks = New-Object System.Collections.Generic.List[string]
    $zip = [System.IO.Compression.ZipFile]::OpenRead($WarPath)
    try {
        $names = @($zip.Entries | ForEach-Object { $_.FullName })
        $backdoorHits = @($names | Where-Object { $_ -cmatch $backdoorPattern })
        $noderpcHits = @($names | Where-Object { $_ -match $noderpcPattern })
        foreach ($entry in $zip.Entries) {
            if (-not ($configEntries -contains $entry.FullName)) { continue }
            $reader = New-Object -TypeName System.IO.StreamReader -ArgumentList @($entry.Open(), [System.Text.Encoding]::UTF8)
            try { $text = $reader.ReadToEnd() } finally { $reader.Dispose() }
            foreach ($s in $secretPatterns) {
                if ($text.Contains($s)) { $leaks.Add("$($entry.FullName) => $s") }
            }
        }
        # RMI 端口补丁复验：kernel/web/RmiProxyFactoryBean.class 必须存在且为补丁版
        $rmiEntry = $zip.GetEntry('WEB-INF/classes/kernel/web/RmiProxyFactoryBean.class')
        $rmiPatched = $false
        $rmiClassSize = 0
        if ($null -ne $rmiEntry) {
            $rmiClassSize = $rmiEntry.Length
            $rmiReader = New-Object -TypeName System.IO.StreamReader -ArgumentList @($rmiEntry.Open(), [System.Text.Encoding]::GetEncoding(28591))
            try { $rmiText = $rmiReader.ReadToEnd() } finally { $rmiReader.Dispose() }
            $rmiPatched = $rmiText.Contains('DeterministicPortMap') -and $rmiText.Contains('DynamicContextMap')
        }
    }
    finally { $zip.Dispose() }

    $classes = @($names | Where-Object { $_ -like 'WEB-INF/classes/*' })
    $libs = @($names | Where-Object { $_ -like 'WEB-INF/lib/*.jar' })
    $jsps = @($names | Where-Object { $_ -like '*.jsp' })
    $hbms = @($names | Where-Object { $_ -like '*.hbm.xml' })
    $junk = @($names | Where-Object { $_.Split('/')[-1] -in $JunkNames -or $_.Split('/')[-1] -like '~$*' })
    $badSeparator = @($names | Where-Object { $_.Contains('\') -or $_.StartsWith('/') -or $_.StartsWith('./') })
    $nestedRoot = @($names | Where-Object { $_ -like "$App/*" })
    $required = @()
    $required += [pscustomobject]@{ Name = 'WEB-INF/web.xml'; Ok = ($names -contains 'WEB-INF/web.xml') }
    $required += [pscustomobject]@{ Name = 'WEB-INF/classes/spring/* 或 classes 根'; Ok = ($classes.Count -gt 0) }
    $required += [pscustomobject]@{ Name = 'WEB-INF/lib/*.jar'; Ok = ($libs.Count -gt 0) }
    $required += [pscustomobject]@{ Name = '安全: 后门/残留条目 0 命中'; Ok = ($backdoorHits.Count -eq 0) }
    $required += [pscustomobject]@{ Name = '安全: 配置无卖家旧凭据'; Ok = ($leaks.Count -eq 0) }
    $required += [pscustomobject]@{ Name = '修复: RMI 端口补丁类已覆盖'; Ok = $rmiPatched }
    $required += [pscustomobject]@{ Name = '修复: RMI 补丁内部类齐全'; Ok = (
            ($names -contains 'WEB-INF/classes/kernel/web/RmiProxyFactoryBean$DynamicContextMap.class') -and
            ($names -contains 'WEB-INF/classes/kernel/web/RmiProxyFactoryBean$DeterministicPortMap.class')) }
    if ($App -eq 'admin') {
        $required += [pscustomobject]@{ Name = 'login.jsp'; Ok = ($names -contains 'login.jsp') }
        $required += [pscustomobject]@{ Name = 'include/basePath.jsp（反代补丁）'; Ok = ($names -contains 'include/basePath.jsp') }
        $required += [pscustomobject]@{ Name = 'include/top.jsp'; Ok = ($names -contains 'include/top.jsp') }
        $required += [pscustomobject]@{ Name = 'css/root.css'; Ok = ($names -contains 'css/root.css') }
        $required += [pscustomobject]@{ Name = 'js/jquery.min.js'; Ok = ($names -contains 'js/jquery.min.js') }
        $required += [pscustomobject]@{ Name = 'normal/css/style.css（主题）'; Ok = ($names -contains 'normal/css/style.css') }
    }
    $ok = ($junk.Count -eq 0) -and ($badSeparator.Count -eq 0) -and ($nestedRoot.Count -eq 0) -and
          (@($required | Where-Object { -not $_.Ok }).Count -eq 0)
    return [pscustomobject]@{
        App                = $App
        WarPath            = $WarPath
        SizeMB             = [math]::Round((Get-Item -LiteralPath $WarPath).Length / 1MB, 2)
        EntryCount         = $names.Count
        ClassFileCount     = $classes.Count
        LibJarCount        = $libs.Count
        JspCount           = $jsps.Count
        HbmCount           = $hbms.Count
        JunkCount          = $junk.Count
        BadSeparatorCount  = $badSeparator.Count
        NestedRootCount    = $nestedRoot.Count
        BackdoorEntryCount = $backdoorHits.Count
        ConfigLeakCount    = $leaks.Count
        NodeRpcEntryCount  = $noderpcHits.Count
        RmiPatchApplied    = $rmiPatched
        RmiClassSize       = $rmiClassSize
        SecretLeaks        = $leaks
        RequiredChecks     = $required
        Passed             = $ok
    }
}

# ---------------------------------------------------------------------------
# 2. 逐个应用组装 exploded 目录并打包
# ---------------------------------------------------------------------------
Write-Host ''
Write-Host '================ Vexus 后端 WAR 构建 ================' -ForegroundColor Cyan
Write-Host "项目根 : $ProjectRoot"
Write-Host "源码目录: $SourceRoot"
Write-Host "临时目录: $WorkDir"
Write-Host "输出目录: $OutDir"
Write-Host "应用列表: $($Apps -join ', ')   压缩级别: $Compression"
Write-Host ''

$results = @()
foreach ($app in $Apps) {
    $appSrc = Join-Path $SourceRoot $app
    $classesDir = Join-Path $appSrc 'target\classes'
    $dependencyDir = Join-Path $appSrc 'target\dependency'
    $webappDir = Join-Path $appSrc 'src\main\webapp'

    if (-not (Test-Path -LiteralPath $classesDir)) { throw "[$app] 缺少编译产物目录: $classesDir" }
    if (-not (Test-Path -LiteralPath $dependencyDir)) { throw "[$app] 缺少依赖 jar 目录: $dependencyDir" }
    if (-not (Test-Path -LiteralPath $webappDir)) { throw "[$app] 缺少 webapp 目录: $webappDir" }

    $stage = Join-Path $WorkDir $app
    if (Test-Path -LiteralPath $stage) { Remove-Item -LiteralPath $stage -Recurse -Force }
    New-Item -ItemType Directory -Force -Path $stage | Out-Null

    Write-Host "[$app] 1/5 复制 JSP 与静态资源 ..." -ForegroundColor Yellow
    $rootFiles = Copy-Tree -Source $webappDir -Destination $stage -SkipRelativePrefix @('WEB-INF\', 'WEB-INF/')
    $webXmlSrc = Join-Path $webappDir 'WEB-INF\web.xml'
    if (-not (Test-Path -LiteralPath $webXmlSrc)) { throw "[$app] 缺少 webapp\WEB-INF\web.xml: $webXmlSrc" }
    $webInfDir = Join-Path $stage 'WEB-INF'
    New-Item -ItemType Directory -Force -Path $webInfDir | Out-Null
    Copy-Item -LiteralPath $webXmlSrc -Destination (Join-Path $webInfDir 'web.xml') -Force

    Write-Host "[$app] 2/5 复制编译类到 WEB-INF/classes ..." -ForegroundColor Yellow
    $classFiles = Copy-Tree -Source $classesDir -Destination (Join-Path $webInfDir 'classes')

    Write-Host "[$app] 2b/5 安全清理（删除后门/残留类）+ 配置脱敏 ..." -ForegroundColor Magenta
    $removedArtifacts = @(Remove-SecurityRiskArtifacts -ClassesDir (Join-Path $webInfDir 'classes'))
    $sanitizedConfigs = Invoke-ConfigSanitize -ClassesDir (Join-Path $webInfDir 'classes')
    Write-Host ("       删除后门/残留 {0} 项，脱敏配置文件 {1} 个" -f $removedArtifacts.Count, $sanitizedConfigs)
    foreach ($r in $removedArtifacts) { Write-Host "         已删除: $r" -ForegroundColor DarkYellow }

    Write-Host "[$app] 2c/5 覆盖 RMI 端口修复补丁类（kernel/web/RmiProxyFactoryBean*.class）..." -ForegroundColor Magenta
    Install-RmiPatch -ClassesDir (Join-Path $webInfDir 'classes')
    $rmiPatchCount = @(Get-ChildItem -LiteralPath (Join-Path $ScriptDir 'patched\kernel\web') -File -Filter 'RmiProxyFactoryBean*.class').Count
    Write-Host "       已应用 RMI 补丁类 $rmiPatchCount 个（含内部类）+ RMI-PATCH-README.txt"

    Write-Host "[$app] 3/5 复制依赖 jar 到 WEB-INF/lib（排除容器提供 jar）..." -ForegroundColor Yellow
    $libCount = Copy-Tree -Source $dependencyDir -Destination (Join-Path $webInfDir 'lib') -JarsOnly -ExcludeContainerJars
    $excludedJars = @(Get-ChildItem -LiteralPath $dependencyDir -File -Filter *.jar |
        Where-Object { Test-ContainerJar -Name $_.Name } | ForEach-Object { $_.Name })

    if ($app -eq 'admin' -and -not $SkipTheme) {
        Write-Host "[$app] 4/5 补 normal 主题静态资源（来自 web3_project\admin_u\normal）..." -ForegroundColor Yellow
        if (Test-Path -LiteralPath $ThemeSource) {
            $themeFiles = Copy-Tree -Source $ThemeSource -Destination (Join-Path $stage 'normal')
            Write-Host "       normal/ 文件数: $themeFiles"
        }
        else {
            Write-Warning "       admin 主题目录不存在（跳过）: $ThemeSource"
        }
    }
    else {
        Write-Host "[$app] 4/5 无需补充额外资源" -ForegroundColor Yellow
    }

    if ($app -eq 'admin') {
        Write-Host "[$app] 5/5 打 Nginx 反代兼容补丁（include/basePath.jsp 等）..." -ForegroundColor Yellow
        $patched = @(Invoke-AdminBasePathProxyPatch -StageDir $stage)
        foreach ($p in $patched) { Write-Host "       $p" }
    }
    else {
        Write-Host "[$app] 5/5 无 JSP，跳过反代补丁" -ForegroundColor Yellow
    }

    $stageFiles = @(Get-ChildItem -LiteralPath $stage -Recurse -File).Count
    Write-Host "       根资源=$rootFiles  classes=$classFiles  lib=$libCount  合计=$stageFiles  排除容器 jar=$($excludedJars.Count) 个"

    $warPath = Join-Path $OutDir "$app.war"
    Write-Host "[$app] 打包 WAR: $warPath" -ForegroundColor Yellow
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    New-WarArchive -StageDir $stage -WarPath $warPath -LevelName $Compression
    $sw.Stop()

    if (-not $KeepWork) {
        Remove-Item -LiteralPath $stage -Recurse -Force -ErrorAction SilentlyContinue
    }

    $verify = Test-WarArchive -WarPath $warPath -App $app
    $verify | Add-Member -NotePropertyName RemovedArtifacts -NotePropertyValue $removedArtifacts.Count
    $verify | Add-Member -NotePropertyName SanitizedConfigs -NotePropertyValue $sanitizedConfigs
    $results += $verify
    Write-Host ("       完成: {0} MB, {1} 条目, {2} 个 class, {3} 个 jar, {4} 个 JSP, 耗时 {5:N1}s" -f
        $verify.SizeMB, $verify.EntryCount, $verify.ClassFileCount, $verify.LibJarCount, $verify.JspCount, $sw.Elapsed.TotalSeconds) -ForegroundColor Green
    Write-Host ("       后门条目={0} 配置旧凭据={1} noderpc 残留={2} RMI补丁={3}({4}B)" -f
        $verify.BackdoorEntryCount, $verify.ConfigLeakCount, $verify.NodeRpcEntryCount, $verify.RmiPatchApplied, $verify.RmiClassSize) -ForegroundColor Magenta
    if ($verify.ConfigLeakCount -gt 0) { foreach ($l in $verify.SecretLeaks) { Write-Host "         泄露: $l" -ForegroundColor Red } }
    Write-Host ''
}

# ---------------------------------------------------------------------------
# 3. 汇总与静态校验
# ---------------------------------------------------------------------------
Write-Host '================ 构建结果 ================' -ForegroundColor Cyan
$results | Select-Object App, SizeMB, EntryCount, ClassFileCount, LibJarCount, JspCount, RmiPatchApplied, BackdoorEntryCount, ConfigLeakCount, NodeRpcEntryCount, Passed |
    Format-Table -AutoSize | Out-String | Write-Host

$failed = @($results | Where-Object { -not $_.Passed })
if ($failed.Count -gt 0) {
    Write-Host '静态校验未通过，请检查：' -ForegroundColor Red
    foreach ($r in $failed) {
        Write-Host ("[{0}] junk={1} badSeparator={2} nestedRoot={3} backdoor={4} configLeak={5} rmiPatch={6}" -f
            $r.App, $r.JunkCount, $r.BadSeparatorCount, $r.NestedRootCount, $r.BackdoorEntryCount, $r.ConfigLeakCount, $r.RmiPatchApplied) -ForegroundColor Red
        foreach ($c in $r.RequiredChecks) { if (-not $c.Ok) { Write-Host ("  缺少/未通过: {0}" -f $c.Name) -ForegroundColor Red } }
        foreach ($l in $r.SecretLeaks) { Write-Host ("  旧凭据残留: {0}" -f $l) -ForegroundColor Red }
    }
    exit 1
}

Write-Host '全部 WAR 静态校验通过（WEB-INF/web.xml、classes、lib、admin JSP/主题、无嵌套根目录、无垃圾文件）。' -ForegroundColor Green
Write-Host '安全校验通过：后门条目 0 命中、配置中无卖家旧凭据、basePath 已打 Nginx 反代补丁。' -ForegroundColor Green
Write-Host 'RMI 修复校验通过：三个 WAR 的 kernel/web/RmiProxyFactoryBean.class 均为确定性端口补丁版。' -ForegroundColor Green
Write-Host '注意：project/monitor/noderpc/** 因 Spring 强依赖无法从预编译包删除，上线前必须按 docs/05 清空 t_syspara 的 rpc_node_url/node_project_code。' -ForegroundColor Yellow
Write-Host "输出目录: $OutDir" -ForegroundColor Green
if ($KeepWork) { Write-Host "exploded 临时目录保留在: $WorkDir" -ForegroundColor Green }
Write-Host ''
Write-Host '下一步（部署机）: 同一 Tomcat 9 的 webapps 下部署 admin.war / api.war / data.war，' -ForegroundColor Cyan
Write-Host '注意 api.war 必须解压/重命名为 webapps/web（context=/web），Nginx 再把 /api/ 反代到 /web/api/；' -ForegroundColor Cyan
Write-Host 'admin 用 context=/admin，data 用 context=/data。详见 docs\02-后端部署.md。' -ForegroundColor Cyan

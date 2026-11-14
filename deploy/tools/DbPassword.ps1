#Requires -Version 5.1
<#
.SYNOPSIS
    Vexus 交易所数据库密码加解密工具（Windows / .NET TripleDES）

.DESCRIPTION
    与 Java 端 kernel.util.Endecrypt / kernel.druid.LocalDruidDataSource 完全一致：
      密钥   : MD5( KEY 的 GBK 字节 ) 16 字节 + 8 个 0x00 -> 24 字节
      明文   : UTF-16LE 字节
      加密   : TripleDES / ECB / PKCS7（等价 Java DESede/ECB/PKCS5Padding）
      编码   : Base64 -> URL 百分号编码
      解密   : URL 解码 -> Base64 解码 -> TripleDES 解密 -> UTF-16LE 解码

    固定 KEY: Roj6#@08SDF87323FG00%jjsd

.PARAMETER Action
    encrypt 或 decrypt

.PARAMETER PlainText
    加密时的明文（如新的数据库密码）

.PARAMETER CipherText
    解密时的密文（配置文件里 jdbc.password / db.password 的原样值，含 %2F %3D 等转义）

.PARAMETER Key
    可选，默认使用源码里的固定 KEY；换 KEY 时才需要传

.EXAMPLE
    # 解密现有配置里的密码
    .\DbPassword.ps1 -Action decrypt -CipherText "nY0NTjBcrC0nk6q9zp%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%3D%3D"

.EXAMPLE
    # 把新密码加密后填进 config.properties
    .\DbPassword.ps1 -Action encrypt -PlainText "MyStr0ng#Pass"

.EXAMPLE
    # 自测（会跑已知密文/已知明文两组校验）
    .\DbPassword.ps1 -Action selftest

.EXAMPLE
    # 交互式加密（不把明文留在命令行历史里）
    .\DbPassword.ps1 -Action encrypt -Prompt
#>
[CmdletBinding(DefaultParameterSetName = 'Decrypt')]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet('encrypt', 'decrypt', 'selftest')]
    [string]$Action,

    [Parameter(ParameterSetName = 'Encrypt')]
    [string]$PlainText,

    [Parameter(ParameterSetName = 'Decrypt')]
    [string]$CipherText,

    [Parameter(ParameterSetName = 'Encrypt')]
    [switch]$Prompt,

    [string]$Key = 'Roj6#@08SDF87323FG00%jjsd'
)

$ErrorActionPreference = 'Stop'

# 已知样例（来自 dkewl.sql / config.properties）
$script:KnownCipher = 'nY0NTjBcrC0nk6q9zp%2FJRCVu0GmCAI2kBNPDWppUOY3XuBTmq4d4gA%3D%3D'
$script:KnownPlain = 'Err;2[eoGFUriwdgr'

function Get-GbkEncoding {
    # PowerShell 7 (.NET Core) 需要先注册代码页提供程序才能拿到 GBK；KEY 为 ASCII 时退化为 ASCII/UTF8 也无差别
    try {
        if (-not ('System.Text.CodePagesEncodingProvider' -as [type])) {
            Add-Type -AssemblyName System.Text.Encoding.CodePages -ErrorAction Stop
        }
        [System.Text.CodePagesEncodingProvider]::Instance | Out-Null
        [System.Text.Encoding]::RegisterProvider([System.Text.CodePagesEncodingProvider]::Instance)
    }
    catch { }
    try { return [System.Text.Encoding]::GetEncoding(936) } catch { return [System.Text.Encoding]::UTF8 }
}

function Get-DesKey {
    param([string]$KeyString)
    $md5 = [System.Security.Cryptography.MD5]::Create()
    try {
        $bytes = Get-GbkEncoding
        $digest = $md5.ComputeHash($bytes.GetBytes($KeyString))
    }
    finally { $md5.Dispose() }
    $key24 = New-Object byte[] 24
    [Array]::Copy($digest, $key24, 16)   # 后 8 字节保持 0x00
    return $key24
}

function New-TripleDes {
    param([byte[]]$Key24, [bool]$Encrypt)
    $tdes = [System.Security.Cryptography.TripleDES]::Create()
    $tdes.Mode = [System.Security.Cryptography.CipherMode]::ECB
    $tdes.Padding = [System.Security.Cryptography.PaddingMode]::PKCS7
    $tdes.Key = $Key24
    return $tdes
}

function Invoke-UrlEncode {
    # 等价 Java URLEncoder.encode(s, "UTF-8")：保留 A-Za-z0-9 -_.*，空格为 +
    param([string]$Text)
    $sb = New-Object System.Text.StringBuilder
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($Text)
    foreach ($b in $bytes) {
        $c = [char]$b
        if ($b -eq 0x20) { [void]$sb.Append('+') }
        elseif (($b -ge 0x41 -and $b -le 0x5A) -or ($b -ge 0x61 -and $b -le 0x7A) -or
                ($b -ge 0x30 -and $b -le 0x39) -or
                $c -eq '-' -or $c -eq '_' -or $c -eq '.' -or $c -eq '*') {
            [void]$sb.Append($c)
        }
        else { [void]$sb.AppendFormat('%{0:X2}', $b) }
    }
    return $sb.ToString()
}

function Invoke-UrlDecode {
    param([string]$Text)
    $t = $Text.Replace('+', ' ')
    $ms = New-Object System.IO.MemoryStream
    $i = 0
    while ($i -lt $t.Length) {
        $ch = $t[$i]
        if ($ch -eq '%' -and ($i + 2) -lt $t.Length) {
            $hex = $t.Substring($i + 1, 2)
            $val = 0
            if ([int]::TryParse($hex, [System.Globalization.NumberStyles]::HexNumber, $null, [ref]$val)) {
                $ms.WriteByte([byte]$val)
                $i += 3
                continue
            }
        }
        $one = [System.Text.Encoding]::UTF8.GetBytes([string]$ch)
        $ms.Write($one, 0, $one.Length)
        $i++
    }
    return [System.Text.Encoding]::UTF8.GetString($ms.ToArray())
}

function Invoke-Encrypt {
    param([string]$Text, [string]$KeyString)
    $key24 = Get-DesKey -KeyString $KeyString
    $plain = [System.Text.Encoding]::Unicode.GetBytes($Text)   # UTF-16LE
    $tdes = New-TripleDes -Key24 $key24 -Encrypt $true
    try { $cipher = $tdes.CreateEncryptor().TransformFinalBlock($plain, 0, $plain.Length) }
    finally { $tdes.Dispose() }
    $b64 = [Convert]::ToBase64String($cipher)
    return (Invoke-UrlEncode -Text $b64)
}

function Invoke-Decrypt {
    param([string]$Text, [string]$KeyString)
    $key24 = Get-DesKey -KeyString $KeyString
    $b64 = Invoke-UrlDecode -Text $Text.Trim()
    $raw = [Convert]::FromBase64String($b64)
    $tdes = New-TripleDes -Key24 $key24 -Encrypt $false
    try { $plain = $tdes.CreateDecryptor().TransformFinalBlock($raw, 0, $raw.Length) }
    finally { $tdes.Dispose() }
    return [System.Text.Encoding]::Unicode.GetString($plain)
}

function Invoke-SelfTest {
    $ok = $true

    # (a) 标准 DES 测试向量（验证 .NET 底层与 Java DESede 一致的基本前提）
    $des = [System.Security.Cryptography.DES]::Create()
    $des.Mode = [System.Security.Cryptography.CipherMode]::ECB
    $des.Padding = [System.Security.Cryptography.PaddingMode]::None
    $des.Key = [byte[]]@(0x13, 0x34, 0x57, 0x79, 0x9B, 0xBC, 0xDF, 0xF1)
    $pt = [byte[]]@(0x01, 0x23, 0x45, 0x67, 0x89, 0xAB, 0xCD, 0xEF)
    $desCt = $des.CreateEncryptor().TransformFinalBlock($pt, 0, 8)
    $hex = ($desCt | ForEach-Object { $_.ToString('X2') }) -join ''
    $des.Dispose()
    "[1] DES standard vector : $hex  " + $(if ($hex -eq '85E813540F0AB405') { 'OK' } else { 'FAIL' }) | Write-Host
    if ($hex -ne '85E813540F0AB405') { $ok = $false }

    # (b) 已知密文解密
    try {
        $ptText = Invoke-Decrypt -Text $script:KnownCipher -KeyString $Key
        $good = ($ptText -eq $script:KnownPlain)
        "[2] known cipher decrypt: '$ptText'  " + $(if ($good) { 'OK' } else { "FAIL (expect $script:KnownPlain)" }) | Write-Host
        if (-not $good) { $ok = $false }
    }
    catch {
        "[2] known cipher decrypt: ERROR $_" | Write-Host
        $ok = $false
    }

    # (c) 往返
    $sample = 'Test@123456'
    $ct = Invoke-Encrypt -Text $sample -KeyString $Key
    $rt = Invoke-Decrypt -Text $ct -KeyString $Key
    "[3] round trip          : $sample -> $ct -> $rt  " + $(if ($rt -eq $sample) { 'OK' } else { 'FAIL' }) | Write-Host
    if ($rt -ne $sample) { $ok = $false }

    # (d) 已知明文重新加密必须得到已知密文（强校验：与 Java 结果逐字节一致）
    $ctKnown = Invoke-Encrypt -Text $script:KnownPlain -KeyString $Key
    $good = ($ctKnown -eq $script:KnownCipher)
    "[4] known plain encrypt : $ctKnown  " + $(if ($good) { 'OK' } else { "FAIL (expect $script:KnownCipher)" }) | Write-Host
    if (-not $good) { $ok = $false }

    if ($ok) { Write-Host 'selftest: PASS' -ForegroundColor Green; return 0 }
    else { Write-Host 'selftest: FAIL' -ForegroundColor Red; return 1 }
}

# ------------------------------- main -------------------------------------
switch ($Action) {
    'selftest' {
        exit (Invoke-SelfTest)
    }
    'encrypt' {
        if ($Prompt -or [string]::IsNullOrEmpty($PlainText)) {
            $secure = Read-Host -Prompt '请输入要加密的数据库密码明文' -AsSecureString
            $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
            try { $PlainText = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr) }
            finally { [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) }
        }
        if ([string]::IsNullOrEmpty($PlainText)) { Write-Error '必须提供 -PlainText 或使用 -Prompt'; exit 2 }
        Invoke-Encrypt -Text $PlainText -KeyString $Key
    }
    'decrypt' {
        if ([string]::IsNullOrEmpty($CipherText)) { Write-Error '必须提供 -CipherText'; exit 2 }
        try { Invoke-Decrypt -Text $CipherText -KeyString $Key }
        catch { Write-Error "解密失败: $_"; exit 1 }
    }
}

$ErrorActionPreference = 'Stop'
$root = 'C:\vexus-test\android-app'
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
$bt = Join-Path $sdk 'build-tools\25.0.3'
$aapt = Join-Path $bt 'aapt.exe'
$zipalign = Join-Path $sdk 'build-tools\36.0.0\zipalign.exe'
$d8 = Join-Path $sdk 'build-tools\36.0.0\d8.bat'
$apksigner = Join-Path $sdk 'build-tools\36.0.0\apksigner.bat'
$androidJar = Join-Path $sdk 'platforms\android-27\android.jar'
$jbr = 'C:\Program Files\Android\Android Studio\jbr\bin'
$javac = Join-Path $jbr 'javac.exe'
$keytool = Join-Path $jbr 'keytool.exe'

$gen = Join-Path $root 'gen'
$obj = Join-Path $root 'obj'
$bin = Join-Path $root 'bin'
$dist = Join-Path $root 'dist'
foreach ($d in @($gen, $obj, $bin, $dist)) {
    if (Test-Path $d) { Remove-Item $d -Recurse -Force }
    New-Item -ItemType Directory -Force -Path $d | Out-Null
}

Write-Host '== aapt generate R.java =='
& $aapt package -f -m -J $gen -M (Join-Path $root 'AndroidManifest.xml') -S (Join-Path $root 'res') -I $androidJar
if ($LASTEXITCODE -ne 0) { throw 'aapt R.java failed' }

Write-Host '== javac =='
$srcFiles = Get-ChildItem (Join-Path $root 'src') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
$rJava = Get-ChildItem $gen -Recurse -Filter 'R.java' | Select-Object -First 1 -ExpandProperty FullName
& $javac -source 8 -target 8 -bootclasspath $androidJar -classpath $androidJar -d $obj $srcFiles $rJava
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }

Write-Host '== d8 =='
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:PATH = $env:JAVA_HOME + '\bin;' + $env:PATH
$classFiles = Get-ChildItem $obj -Recurse -Filter '*.class' | ForEach-Object { $_.FullName }
$classArgs = ($classFiles | ForEach-Object { '"' + $_ + '"' }) -join ' '
cmd /c "`"$d8`" --lib `"$androidJar`" --output `"$bin`" $classArgs"
if ($LASTEXITCODE -ne 0) { throw 'd8 failed' }

Write-Host '== aapt package =='
$unsigned = Join-Path $dist 'Vexus-unsigned.apk'
& $aapt package -f -M (Join-Path $root 'AndroidManifest.xml') -S (Join-Path $root 'res') -I $androidJar -F $unsigned
if ($LASTEXITCODE -ne 0) { throw 'aapt package failed' }

Write-Host '== add classes.dex =='
$py = 'C:\Users\Admin\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe'
$addDex = Join-Path $root 'add_dex.py'
@'
import zipfile, sys
apk, dex = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(apk, "a", zipfile.ZIP_DEFLATED) as z:
    z.write(dex, "classes.dex")
print("dex added")
'@ | Set-Content $addDex -Encoding UTF8
& $py $addDex $unsigned (Join-Path $bin 'classes.dex')
if ($LASTEXITCODE -ne 0) { throw 'add dex failed' }

Write-Host '== zipalign =='
$aligned = Join-Path $dist 'Vexus-aligned.apk'
& $zipalign -f -p 4 $unsigned $aligned
if ($LASTEXITCODE -ne 0) { throw 'zipalign failed' }

Write-Host '== sign =='
$ks = Join-Path $root 'vexus.keystore'
if (-not (Test-Path $ks)) {
    & $keytool -genkeypair -v -keystore $ks -alias vexus -keyalg RSA -keysize 2048 -validity 10000 -storepass vexus2026 -keypass vexus2026 -dname "CN=Vexus, OU=Mobile, O=Vexus, L=Singapore, S=Singapore, C=SG" 2>&1 | Select-Object -Last 3
}
$final = Join-Path $dist 'Vexus.apk'
& $apksigner sign --ks $ks --ks-key-alias vexus --ks-pass pass:vexus2026 --key-pass pass:vexus2026 --out $final $aligned
if ($LASTEXITCODE -ne 0) { throw 'apksigner failed' }

Write-Host '== verify =='
& $aapt dump badging $final | Select-Object -First 8
& $apksigner verify --verbose $final | Select-Object -First 8
Get-Item $final | Select-Object FullName, Length

# Shared Windows tooling. No machine-specific SDK, drive or user paths.
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
function Resolve-RuiSdk([string]$SdkPath) {
    if (-not $SdkPath) { $SdkPath = $env:ANDROID_HOME }
    if (-not $SdkPath) { $SdkPath = $env:ANDROID_SDK_ROOT }
    if (-not $SdkPath) { $SdkPath = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
    if (-not (Test-Path -LiteralPath (Join-Path $SdkPath 'platform-tools\adb.exe'))) { throw "Android SDK not found: $SdkPath. Pass -SdkPath or set ANDROID_HOME." }
    return (Resolve-Path -LiteralPath $SdkPath).Path
}
function Invoke-RuiTool {
    param([string]$Tool, [string[]]$Arguments)
    & $Tool @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Tool failed (exit $LASTEXITCODE): $Tool" }
}
function Get-RuiKey {
    $key = Join-Path $env:LOCALAPPDATA 'RuiRuiOS\debug.keystore'
    if (-not (Test-Path -LiteralPath $key)) {
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $key) | Out-Null
        Invoke-RuiTool 'keytool' @('-genkeypair','-keystore',$key,'-storepass','android','-keypass','android','-alias','androiddebugkey','-keyalg','RSA','-keysize','2048','-validity','10000','-dname','CN=RuiRui OS Development')
    }
    return $key
}
function Wait-RuiAndroid([string]$Adb,[string]$Serial) {
    $deadline=(Get-Date).AddMinutes(5)
    while ((Get-Date) -lt $deadline) {
        $boot= & $Adb -s $Serial shell getprop sys.boot_completed 2>$null
        if ("$boot".Trim() -eq '1') { return }
        Start-Sleep -Seconds 2
    }
    throw "Android did not finish booting on $Serial within five minutes."
}

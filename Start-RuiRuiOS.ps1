#Requires -Version 7.0
param([string]$SdkPath,[string]$AvdName='RuiruiOS_API34',[string]$AvdHome,[int]$Port=5554,[string]$Gpu='swiftshader_indirect')
. "$PSScriptRoot\Tools.Common.ps1"
$sdk=Resolve-RuiSdk $SdkPath
$adb=Join-Path $sdk 'platform-tools\adb.exe'
$emulator=Join-Path $sdk 'emulator\emulator.exe'
if ($Port -lt 5554 -or $Port -gt 5682 -or $Port%2) { throw 'Port must be an even emulator console port (5554–5682).' }
if ($AvdHome) { $env:ANDROID_AVD_HOME=(Resolve-Path -LiteralPath $AvdHome).Path }
$serial="emulator-$Port"
$devices=(& $adb devices) -join "`n"
if ($devices -notmatch "(?m)^$serial\s+device") {
    $available=@(& $emulator -list-avds)
    if ($available -notcontains $AvdName) { throw "AVD '$AvdName' not found. Follow README to create it or pass -AvdName." }
    # This visible window is the requested interactive Android device.
    Start-Process -FilePath $emulator -ArgumentList @('-avd',$AvdName,'-port',$Port,'-writable-system','-no-snapshot','-gpu',$Gpu,'-memory','2048','-cores','2') -WindowStyle Normal
}
Wait-RuiAndroid $adb $serial
$avdReply=@(& $adb -s $serial emu avd name)
if ($avdReply -notcontains $AvdName) { throw "$serial belongs to a different AVD. Choose another -Port to avoid modifying it." }
$installed=(& $adb -s $serial shell pm path com.ruirui.os) -join ''
if (-not $installed.StartsWith('package:')) { Write-Host 'Emulator ready. Run Install-RuiRuiOS.ps1 next, then run this script again.'; return }
$listener=Get-NetTCPConnection -LocalAddress 127.0.0.1 -LocalPort 18765 -State Listen -ErrorAction SilentlyContinue
if (-not $listener) {
    $bridge=Join-Path $PSScriptRoot 'RuiRuiOS-ControlBridge.ps1'
    $pwsh=(Get-Command pwsh -ErrorAction Stop).Source
    Start-Process -FilePath $pwsh -ArgumentList @('-NoProfile','-File',('"'+$bridge+'"'),'-SdkPath',('"'+$sdk+'"'),'-Serial',$serial) -WindowStyle Hidden
    Start-Sleep -Seconds 2
    if (-not (Get-NetTCPConnection -LocalAddress 127.0.0.1 -LocalPort 18765 -State Listen -ErrorAction SilentlyContinue)) { throw 'Bridge failed to start. Run RuiRuiOS-ControlBridge.ps1 in a terminal to see the error.' }
} else { Write-Host 'Using existing localhost:18765 bridge; it must target the same emulator.' }
# Do not reinstall, reboot or force-stop an active AI session on ordinary startup.
Invoke-RuiTool $adb @('-s',$serial,'shell','am','start','-a','android.intent.action.MAIN','-c','android.intent.category.HOME')

#Requires -Version 7.0
param([string]$SdkPath,[string]$Serial='emulator-5554',[switch]$KeepGoogleApps)
. "$PSScriptRoot\Tools.Common.ps1"
$sdk=Resolve-RuiSdk $SdkPath
$adb=Join-Path $sdk 'platform-tools\adb.exe'
if ($Serial -notmatch '^emulator-\d+$') { throw 'This installer supports Android Emulator targets only.' }
Wait-RuiAndroid $adb $Serial
$active=(& $adb -s $Serial shell dumpsys activity services com.ruirui.os) -join "`n"
if ($active -match 'ServiceRecord[^\r\n]*AiActivityService') { throw 'An AI session is running. Finish or stop it inside the app before installing an update.' }
$apk=Join-Path $PSScriptRoot 'RuiRuiOS-alpha.apk'
if (-not (Test-Path -LiteralPath $apk)) { throw 'Download the APK from Releases into this directory or run Build-RuiRuiOS.ps1.' }
foreach ($name in @('RuiRuiOS-alpha.apk','RuiRuiSystemUI.apk','RuiRuiFramework.apk','bootanimation.zip')) {
    if (-not (Test-Path -LiteralPath (Join-Path $PSScriptRoot $name))) { throw "Missing release component: $name. Download the complete runtime package first." }
}
Invoke-RuiTool $adb @('-s',$Serial,'install','-r',$apk)
Invoke-RuiTool $adb @('-s',$Serial,'shell','cmd','package','set-home-activity','com.ruirui.os/.HomeActivity')
& {
    foreach ($name in @('RuiRuiSystemUI.apk','RuiRuiFramework.apk','bootanimation.zip')) { if (-not (Test-Path -LiteralPath (Join-Path $PSScriptRoot $name))) { throw "Missing $name. Download release assets or build the overlays first." } }
    Invoke-RuiTool $adb @('-s',$Serial,'root')
    Start-Sleep -Seconds 2
    $uid=(& $adb -s $Serial shell id) -join ''
    if ($uid -notmatch 'uid=0') { throw 'Root required. Use a Google APIs image, not a Google Play image.' }
    Invoke-RuiTool $adb @('-s',$Serial,'disable-verity')
    Invoke-RuiTool $adb @('-s',$Serial,'reboot')
    Wait-RuiAndroid $adb $Serial
    Invoke-RuiTool $adb @('-s',$Serial,'root')
    Start-Sleep -Seconds 2
    Invoke-RuiTool $adb @('-s',$Serial,'remount')
    Invoke-RuiTool $adb @('-s',$Serial,'shell','mkdir','-p','/product/overlay','/product/media')
    foreach ($name in @('RuiRuiSystemUI.apk','RuiRuiFramework.apk')) {
        Invoke-RuiTool $adb @('-s',$Serial,'push',(Join-Path $PSScriptRoot $name),"/product/overlay/$name")
        Invoke-RuiTool $adb @('-s',$Serial,'shell','chmod','0644',"/product/overlay/$name")
    }
    Invoke-RuiTool $adb @('-s',$Serial,'push',"$PSScriptRoot\bootanimation.zip",'/product/media/bootanimation.zip')
    Invoke-RuiTool $adb @('-s',$Serial,'shell','chmod','0644','/product/media/bootanimation.zip')
    Invoke-RuiTool $adb @('-s',$Serial,'shell','restorecon','-R','/product/overlay','/product/media')
}
if (-not $KeepGoogleApps) {
    foreach ($package in @('com.google.android.youtube','com.google.android.apps.youtube.music','com.google.android.apps.docs','com.google.android.apps.maps','com.google.android.gm','com.google.android.googlequicksearchbox')) {
        & $adb -s $Serial shell pm uninstall --user 0 $package
    }
}
Invoke-RuiTool $adb @('-s',$Serial,'shell','settings','put','secure','assistant','com.ruirui.os/.RuiVoiceInteractionService')
Invoke-RuiTool $adb @('-s',$Serial,'shell','settings','put','secure','voice_interaction_service','com.ruirui.os/.RuiVoiceInteractionService')
Invoke-RuiTool $adb @('-s',$Serial,'shell','settings','put','global','power_button_long_press','5')
& { Invoke-RuiTool $adb @('-s',$Serial,'reboot'); Wait-RuiAndroid $adb $Serial }
Write-Host 'Installed. Run Start-RuiRuiOS.ps1 to start the bridge, then configure your model in Settings > RuiRui AI.'


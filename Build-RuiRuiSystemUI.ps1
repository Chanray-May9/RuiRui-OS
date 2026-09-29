#Requires -Version 7.0
param([string]$SdkPath,[string]$Serial='emulator-5554',[string]$BuildTools='36.0.0',[int]$CompileApi=35)
. "$PSScriptRoot\Tools.Common.ps1"
$sdk=Resolve-RuiSdk $SdkPath
$bt=Join-Path $sdk "build-tools\$BuildTools"
$android=Join-Path $sdk "platforms\android-$CompileApi\android.jar"
$adb=Join-Path $sdk 'platform-tools\adb.exe'
$build=Join-Path $PSScriptRoot ('.build\overlays-'+[guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path $build | Out-Null
$remote=@(& $adb -s $Serial shell pm path com.android.systemui) | Where-Object { $_ -like 'package:*' } | Select-Object -First 1
if (-not $remote) { throw 'Start the target Google APIs API 34 emulator before building SystemUI overlays.' }
Invoke-RuiTool $adb @('-s',$Serial,'pull',$remote.Substring(8).Trim(),"$build\SystemUI.apk")
$key=Get-RuiKey
foreach ($item in @(@('systemui-overlay','RuiRuiSystemUI'),@('framework-overlay','RuiRuiFramework'))) {
    $source=$item[0];$name=$item[1]
    Invoke-RuiTool "$bt\aapt2.exe" @('compile','--dir',"$PSScriptRoot\$source\res",'-o',"$build\$name-res.zip")
    $link=@('link','-o',"$build\$name-unsigned.apk",'--manifest',"$PSScriptRoot\$source\AndroidManifest.xml",'-I',$android,'--auto-add-overlay')
    if ($name -eq 'RuiRuiSystemUI') { $link+=@('-I',"$build\SystemUI.apk",'--package-id','0x7e','--allow-reserved-package-id') }
    Invoke-RuiTool "$bt\aapt2.exe" ($link+@("$build\$name-res.zip"))
    Invoke-RuiTool "$bt\zipalign.exe" @('-f','4',"$build\$name-unsigned.apk","$build\$name-aligned.apk")
    Invoke-RuiTool "$bt\apksigner.bat" @('sign','--ks',$key,'--ks-pass','pass:android','--key-pass','pass:android','--out',"$PSScriptRoot\$name.apk","$build\$name-aligned.apk")
}

#Requires -Version 7.0
param([string]$SdkPath,[string]$BuildTools='36.0.0',[int]$CompileApi=35)
. "$PSScriptRoot\Tools.Common.ps1"
$sdk=Resolve-RuiSdk $SdkPath
$bt=Join-Path $sdk "build-tools\$BuildTools"
$android=Join-Path $sdk "platforms\android-$CompileApi\android.jar"
if (-not (Test-Path -LiteralPath $android)) { throw "Install SDK Platform $CompileApi first." }
$build=Join-Path $PSScriptRoot ('.build\app-'+[guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path "$build\classes","$build\dex" | Out-Null
$sources=@(Get-ChildItem "$PSScriptRoot\android-app\src" -Recurse -Filter *.java | ForEach-Object FullName)
Invoke-RuiTool 'javac' (@('-encoding','UTF-8','--release','17','-cp',$android,'-d',"$build\classes")+$sources)
Invoke-RuiTool "$bt\aapt.exe" @('package','-f','-M',"$PSScriptRoot\android-app\AndroidManifest.xml",'-S',"$PSScriptRoot\android-app\res",'-A',"$PSScriptRoot\android-app\assets",'-I',$android,'-F',"$build\base.apk")
$classes=@(Get-ChildItem "$build\classes" -Recurse -Filter *.class | ForEach-Object FullName)
Invoke-RuiTool "$bt\d8.bat" (@('--lib',$android,'--min-api','26','--output',"$build\dex")+$classes)
Invoke-RuiTool 'jar' @('uf',"$build\base.apk",'-C',"$build\dex",'classes.dex')
Invoke-RuiTool "$bt\zipalign.exe" @('-f','4',"$build\base.apk","$build\aligned.apk")
$key=Get-RuiKey
Invoke-RuiTool "$bt\apksigner.bat" @('sign','--ks',$key,'--ks-pass','pass:android','--key-pass','pass:android','--out',"$PSScriptRoot\RuiRuiOS-alpha.apk","$build\aligned.apk")
Get-Item "$PSScriptRoot\RuiRuiOS-alpha.apk" | Select-Object Name,Length

#Requires -Version 7.0
param([string]$SdkPath,[string]$Serial='emulator-5554')
. "$PSScriptRoot\Tools.Common.ps1"
$sdk = Resolve-RuiSdk $SdkPath
$adb = Join-Path $sdk 'platform-tools\adb.exe'
if ($Serial -notmatch '^emulator-\d+$') { throw 'Only Android Emulator targets are supported.' }
$port = 18765
$authorized = $false

function Invoke-EmulatorShell([string]$command, [switch]$Detailed) {
    if ([string]::IsNullOrWhiteSpace($command) -or $command.Length -gt 12000) { throw 'Invalid command length.' }
    $encoded = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($command))
    $info = [Diagnostics.ProcessStartInfo]::new()
    $info.FileName = $adb
    $info.UseShellExecute = $false
    $info.RedirectStandardOutput = $true
    $info.RedirectStandardError = $true
    $info.StandardOutputEncoding = [Text.Encoding]::UTF8
    $info.StandardErrorEncoding = [Text.Encoding]::UTF8
    foreach ($arg in @('-s',$Serial,'shell',"echo $encoded | base64 -d | timeout 30 sh")) { [void]$info.ArgumentList.Add($arg) }
    $proc = [Diagnostics.Process]::Start($info)
    $stdoutTask = $proc.StandardOutput.ReadToEndAsync()
    $stderrTask = $proc.StandardError.ReadToEndAsync()
    if (-not $proc.WaitForExit(35000)) { $proc.Kill(); throw 'Command timed out; check device state before retrying.' }
    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()
    $exit = $proc.ExitCode
    $proc.Dispose()
    if ($stdout.Length -gt 16000) { $stdout = $stdout.Substring(0,16000) + "`n[output truncated]" }
    if ($stderr.Length -gt 4000) { $stderr = $stderr.Substring(0,4000) }
    if ($Detailed) { return @{stdout=$stdout;stderr=$stderr;exitCode=$exit;output=$stdout} }
    if ($exit -ne 0) { throw "Android command failed ($exit): $stderr $stdout" }
    return $stdout
}
function Write-HttpResponse($stream, [int]$status, [string]$body) {
    $bytes = [Text.Encoding]::UTF8.GetBytes($body)
    $phrase = if ($status -eq 200) { 'OK' } elseif ($status -eq 403) { 'Forbidden' } else { 'Bad Request' }
    $header = "HTTP/1.1 $status $phrase`r`nContent-Type: application/json; charset=utf-8`r`nContent-Length: $($bytes.Length)`r`nConnection: close`r`n`r`n"
    $headBytes = [Text.Encoding]::ASCII.GetBytes($header)
    $stream.Write($headBytes,0,$headBytes.Length)
    $stream.Write($bytes,0,$bytes.Length)
    $stream.Flush()
}

$null = & $adb -s $Serial root 2>&1
Start-Sleep -Milliseconds 800
$null = Invoke-EmulatorShell 'id'
$listener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback,$port)
$listener.Start()
while ($true) {
    $client = $listener.AcceptTcpClient()
    try {
        $stream = $null; $memory = $null
        $stream = $client.GetStream(); $stream.ReadTimeout = 10000; $stream.WriteTimeout = 10000
        $memory = [IO.MemoryStream]::new()
        $buffer = [byte[]]::new(4096)
        $boundary = -1
        while ($boundary -lt 0) {
            $n = $stream.Read($buffer,0,$buffer.Length)
            if ($n -le 0) { break }
            $memory.Write($buffer,0,$n)
            $sofar = [Text.Encoding]::ASCII.GetString($memory.ToArray())
            $boundary = $sofar.IndexOf("`r`n`r`n")
        }
        $raw = [Text.Encoding]::ASCII.GetString($memory.ToArray())
        $line = ($raw -split "`r`n",2)[0]
        $parts = $line.Split(' ')
        $method = $parts[0]
        $path = $parts[1]
        if ($boundary -lt 0) { throw "Invalid HTTP request" }; $headerText = $raw.Substring(0,$boundary)
        $lengthMatch = [regex]::Match($headerText,'(?im)^Content-Length:\s*(\d+)')
        $contentLength = if ($lengthMatch.Success) { [int]$lengthMatch.Groups[1].Value } else { 0 }
        if ($contentLength -gt 1048576) { throw "Request too large" }; $prefixLength = $boundary + 4
        $bodyBytes = $memory.ToArray()
        while ($bodyBytes.Length - $prefixLength -lt $contentLength) {
            $n = $stream.Read($buffer,0,$buffer.Length)
            if ($n -le 0) { break }
            $memory.Write($buffer,0,$n)
            $bodyBytes = $memory.ToArray()
        }
        $body = if ($contentLength -gt 0) { [Text.Encoding]::UTF8.GetString($bodyBytes,$prefixLength,$contentLength) } else { '{}' }
        if ($method -eq 'GET' -and $path -eq '/health') {
            $id = Invoke-EmulatorShell 'id'
            Write-HttpResponse $stream 200 (@{root=($id -match 'uid=0'); authorized=$authorized} | ConvertTo-Json -Compress)
        } elseif ($method -eq 'GET' -and $path.StartsWith('/state?')) {
            $uri = [Uri]::new("http://127.0.0.1:$port$path")
            $key = [System.Web.HttpUtility]::ParseQueryString($uri.Query).Get('key')
            $command = switch ($key) {
                'wifi' { 'cmd wifi status' }
                'bluetooth' { 'dumpsys bluetooth_manager' }
                'airplane' { 'settings get global airplane_mode_on' }
                'brightness' { 'settings get system screen_brightness' }
                'font' { 'settings get system font_scale' }
                'night' { 'settings get secure ui_night_mode' }
                'dnd' { 'settings get global zen_mode' }
                'volume' { 'cmd media_session volume --stream 3 --get' }
                default { throw 'Unknown settings key.' }
            }
            $value = Invoke-EmulatorShell $command
            if ($key -eq 'wifi') { $value = if ($value -match 'Wifi is enabled') { '1' } else { '0' } }
            elseif ($key -eq 'bluetooth') { $value = if ($value -match '(?i)state\s*[:=]\s*(ON|STATE_ON)|mEnabled=true') { '1' } else { '0' } }
            elseif ($key -eq 'night') { $value = if ($value -match '2') { '1' } else { '0' } }
            Write-HttpResponse $stream 200 (@{value="$value".Trim()} | ConvertTo-Json -Compress)
        } elseif ($method -eq 'POST' -and $path -eq '/authorize') {
            $authorized = $true
            Write-HttpResponse $stream 200 '{"ok":true}'
        } elseif ($method -eq 'POST' -and $path -eq '/deauthorize') {
            $authorized = $false
            Write-HttpResponse $stream 200 '{"ok":true}'
        } elseif ($method -eq 'POST' -and $path -in @('/run','/run-once')) {
            $request = $body | ConvertFrom-Json
            $once = $path -eq '/run-once' -and $request.approved -eq $true
            if (-not $authorized -and -not $once) { Write-HttpResponse $stream 403 '{"error":"需要本次操作确认，或在设置中开启完全控制。"}' }
            else {
                $result = Invoke-EmulatorShell ([string]$request.command) -Detailed
                Write-HttpResponse $stream 200 ($result | ConvertTo-Json -Compress)
            }        } elseif ($method -eq 'POST' -and $path -eq '/setting') {
            $request = $body | ConvertFrom-Json
            $value = [string]$request.value
            $command = switch ([string]$request.key) {
                'wifi' { if ($value -eq '1') { 'cmd wifi set-wifi-enabled enabled' } else { 'cmd wifi set-wifi-enabled disabled' } }
                'bluetooth' { if ($value -eq '1') { 'cmd bluetooth_manager enable' } else { 'cmd bluetooth_manager disable' } }
                'airplane' { if ($value -eq '1') { 'settings put global airplane_mode_on 1; am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true' } else { 'settings put global airplane_mode_on 0; am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false' } }
                'brightness' { if ($value -match '^\d{1,3}$' -and [int]$value -le 255) { "settings put system screen_brightness_mode 0; settings put system screen_brightness $value" } else { throw 'Brightness must be from 0 to 255.' } }
                'font' { if ($value -in @('0.9','1.0','1.1','1.2')) { "settings put system font_scale $value" } else { throw 'Unsupported font scale.' } }
                'night' { if ($value -eq '1') { 'cmd uimode night yes' } else { 'cmd uimode night no' } }
                'dnd' { if ($value -eq '1') { 'cmd notification set_dnd on' } else { 'cmd notification set_dnd off' } }
                'volume' { if ($value -match '^\d{1,2}$' -and [int]$value -le 15) { "cmd media_session volume --stream 3 --set $value" } else { throw 'Volume must be from 0 to 15.' } }
                default { throw 'Unknown settings action.' }
            }
            $result = Invoke-EmulatorShell $command
            Write-HttpResponse $stream 200 (@{ok=$true;output=$result} | ConvertTo-Json -Compress)
        } else { Write-HttpResponse $stream 400 '{"error":"Unknown endpoint."}' }
    } catch {
        try { Write-HttpResponse $stream 400 (@{error=$_.Exception.Message} | ConvertTo-Json -Compress) } catch {}
    } finally {
        if ($stream) { $stream.Dispose() }
        if ($client) { $client.Dispose() }
        if ($memory) { $memory.Dispose() }
    }
}


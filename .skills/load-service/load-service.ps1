param(
    [switch]$SkipBuild,
    [switch]$SkipTunnel,
    [switch]$Dev
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")

function Write-Status($icon, $msg) { Write-Host "$icon $msg" }
function Write-Done($msg) { Write-Status "OK" $msg }
function Write-Fail($msg) { Write-Status "FAIL" $msg; exit 1 }
function Write-Info($msg) { Write-Status ">>" $msg }
function Write-Step($n, $msg) { Write-Host "`n--- Step ${n}: $msg ---" -ForegroundColor Cyan }

function Parse-YamlConfig {
    param([Parameter(Mandatory=$true)][string]$Content)
    $script:cfgData = @{}
    $pathStack = New-Object System.Collections.Stack
    $lines = $Content -split "`r?`n"

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $rawLine = $lines[$i]
        if ($rawLine -match '^\s*#' -or $rawLine.Trim() -eq '') { continue }

        $trimmed = $rawLine.TrimStart()
        $indent = $rawLine.Length - $trimmed.Length

        if ($trimmed.StartsWith('- ')) {
            $item = $trimmed.Substring(2).Trim()
            if ($item.Length -ge 2 -and $item[0] -eq [char]34 -and $item[-1] -eq [char]34) { $item = $item.Substring(1, $item.Length - 2) }
            elseif ($item.Length -ge 2 -and $item[0] -eq 39 -and $item[-1] -eq 39) { $item = $item.Substring(1, $item.Length - 2) }
            $parent = if ($pathStack.Count -gt 0) { ($pathStack.Peek() -split '\|')[-2] } else { "" }
            if (-not $script:cfgData.ContainsKey($parent)) { $script:cfgData[$parent] = New-Object System.Collections.ArrayList }
            [void]$script:cfgData[$parent].Add($item)
            continue
        }

        $colonIdx = $trimmed.IndexOf(':')
        if ($colonIdx -lt 1) { continue }

        $k = $trimmed.Substring(0, $colonIdx).Trim()
        $v = $trimmed.Substring($colonIdx + 1).Trim()

        while ($pathStack.Count -gt 0) {
            $peekIndent = [int]($pathStack.Peek() -split '\|')[-1]
            if ($peekIndent -ge $indent) { [void]$pathStack.Pop() } else { break }
        }
        $parent = if ($pathStack.Count -gt 0) { ($pathStack.Peek() -split '\|')[-2] } else { "" }
        $fullPath = if ($parent) { "$parent.$k" } else { $k }

        if ($v -ne '') {
            if ($v.Length -ge 2 -and $v[0] -eq [char]34 -and $v[-1] -eq [char]34) {
                $v = $v.Substring(1, $v.Length - 2)
            } elseif ($v.Length -ge 2 -and $v[0] -eq 39 -and $v[-1] -eq 39) {
                $v = $v.Substring(1, $v.Length - 2)
            }
            $script:cfgData[$fullPath] = $v
        }
        [void]$pathStack.Push("${fullPath}|${indent}")
    }
}

Write-Host ""
Write-Host "CoffeeCookie HomePage - Load Service" -ForegroundColor Yellow
Write-Host "========================================="

Write-Step 1 "Read config"
$configPath = Join-Path $ProjectRoot ".config\service-config.yml"
if (-not (Test-Path $configPath)) { Write-Fail "Config not found: $configPath" }
Parse-YamlConfig -Content (Get-Content $configPath -Raw -Encoding UTF8)
$cfg = $script:cfgData

$svcPort = $cfg['service.port']
$svcDomain = $cfg['service.domain']
$tunnelMethod = $cfg['start.tunnel.method']
$healthUrl = $cfg['start.backend.health_check.url']
$healthTimeout = [int]$cfg['start.backend.health_check.timeout_ms'] / 1000
$healthInterval = [int]$cfg['start.backend.health_check.interval_ms'] / 1000
$publicUrl = $cfg['access.public']
$localUrl = $cfg['access.local']
$username = $cfg['access.default_account.username']
$password = $cfg['access.default_account.password']

Write-Done "Service: $($cfg['service.name']) | Port: $svcPort | Domain: $svcDomain"
Write-Done "Tunnel: $tunnelMethod | Public: $publicUrl"

Write-Step 2 "Env check"
$mavenDir = "C:\Tools\apache-maven-3.9.14"
if ((Test-Path "$mavenDir\bin\mvn.cmd") -and ($env:Path -notlike "*$mavenDir*")) {
    $env:Path = "$mavenDir\bin;$env:Path"
}

$envTools = $cfg['env_check.required']
if ($envTools -is [string]) { $envTools = @($envTools) }

$toolChecks = @{
    'java'       = 'java'
    'mvn'        = 'mvn'
    'node'       = 'node'
    'npm'        = 'npm'
    'cloudflared'= 'cloudflared'
}

foreach ($tool in $envTools) {
    $exe = $toolChecks[$tool]
    $found = $null -ne (Get-Command $exe -ErrorAction SilentlyContinue)
    if ($found) {
        try {
            if ($tool -eq 'java') {
                $ver = (java -version 2>&1 | ForEach-Object { $_.ToString() }) | Select-Object -First 1
            } else {
                $ver = & $exe --version 2>&1 | Select-Object -First 1
            }
            $verStr = ($ver -join " ").Trim()
            Write-Done "$tool -> $verStr"
        } catch {
            Write-Done "$tool -> installed"
        }
    } else {
        Write-Fail "$tool not installed"
    }
}

if (-not $SkipBuild) {
    Write-Step 3 "Build frontend"
    Push-Location (Join-Path $ProjectRoot "frontend")
    try {
        if (-not (Test-Path "node_modules")) {
            Write-Info "npm install..."
            npm install 2>&1 | ForEach-Object { Write-Host $_ }
            if ($LASTEXITCODE -ne 0) { Write-Fail "npm install failed" }
        }
        Write-Info "npm run build..."
        npm run build 2>&1 | ForEach-Object { Write-Host $_ }
        if ($LASTEXITCODE -ne 0) { Write-Fail "npm run build failed" }
        Write-Done "Frontend built -> frontend/dist/"
    } finally {
        Pop-Location
    }
} else {
    Write-Step 3 "Build frontend (skipped)"
}

Write-Step 4 "Start backend (port $svcPort)"

$backendWorkdir = Join-Path $ProjectRoot "backend"
$logFile = Join-Path $ProjectRoot "backend.log"
$errFile = Join-Path $ProjectRoot "backend-error.log"

$javaProcs = Get-Process -Name "java" -ErrorAction SilentlyContinue
$backendRunning = $false
if ($javaProcs) {
    try {
        $null = Invoke-WebRequest -Uri $healthUrl -TimeoutSec 3 -ErrorAction Stop
        $backendRunning = $true
        Write-Done "Backend already running"
    } catch {
        $backendRunning = $false
    }
}

if (-not $backendRunning) {
    Write-Info "Starting Spring Boot..."
    Push-Location $backendWorkdir
    Start-Process -FilePath "cmd.exe" `
        -ArgumentList "/c mvn spring-boot:run > `"$logFile`" 2> `"$errFile`"" `
        -WindowStyle Hidden
    Pop-Location

    Write-Info "Waiting for backend (timeout ${healthTimeout}s)..."
    $elapsed = 0
    while ($elapsed -lt $healthTimeout) {
        try {
            $null = Invoke-WebRequest -Uri $healthUrl -TimeoutSec 3 -ErrorAction Stop
            Write-Done "Backend ready (${elapsed}s)"
            break
        } catch {
            Start-Sleep -Seconds $healthInterval
            $elapsed += $healthInterval
            Write-Host "." -NoNewline
        }
    }
    if ($elapsed -ge $healthTimeout) {
        Write-Fail "Backend timeout, check $errFile"
    }
}

if (-not $SkipTunnel) {
    Write-Step 5 "Start Cloudflare Tunnel ($tunnelMethod)"

    if ($tunnelMethod -eq 'existing-domain') {
        $configFile = Join-Path $ProjectRoot $cfg['start.tunnel.config_file']
        if (-not (Test-Path $configFile)) { Write-Fail "Tunnel config not found: $configFile" }

        Write-Info "Config: $configFile"
        Write-Info "Public: $publicUrl (fixed domain)"
        Write-Info "Ensuring DNS route..."
        $prevEAP = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        $dnsOutput = cloudflared tunnel route dns --overwrite-dns $cfg['start.tunnel.tunnel_name'] $svcDomain 2>&1
        $ErrorActionPreference = $prevEAP
        $dnsOutput | Where-Object { $_ -match 'INF|already' } | ForEach-Object { Write-Host "   $_" }
        Write-Host ""
        Start-Process -FilePath "cloudflared" `
            -ArgumentList "tunnel --config `"$configFile`" run" `
            -WindowStyle Minimized
        Start-Sleep -Seconds 5
        Write-Done "Tunnel started -> $publicUrl"
    } else {
        Write-Info "Quick Tunnel mode..."
        $tunnelLog = Join-Path $ProjectRoot "tunnel.log"
        Start-Process -FilePath "cloudflared" `
            -ArgumentList "tunnel --url http://localhost:$svcPort" `
            -RedirectStandardOutput $tunnelLog `
            -WindowStyle Hidden
        Start-Sleep -Seconds 15
        $quickUrl = (Select-String -Path $tunnelLog -Pattern "https://.*trycloudflare.com" | Select-Object -First 1).Matches.Value
        if ($quickUrl) {
            Write-Done "Tunnel started -> $quickUrl"
            $publicUrl = $quickUrl
        } else {
            Write-Host "WARN: Quick URL not found, check tunnel.log"
        }
    }
} else {
    Write-Step 5 "Cloudflare Tunnel (skipped)"
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host "  DEPLOY SUCCESS" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Backend:  $localUrl"
Write-Host "  Frontend: built"
if (-not $SkipTunnel) { Write-Host "  Tunnel:   running" }
Write-Host ""
Write-Host "  Public:   $publicUrl"
Write-Host ""
Write-Host "  Login:    $username / $password"
Write-Host ""
Write-Host "  Logs:"
Write-Host "    Backend:  Get-Content $logFile -Tail 50 -Wait"
Write-Host "    Error:    Get-Content $errFile -Tail 50"
Write-Host ""
Write-Host "  Stop:"
Write-Host "    Backend:  Stop-Process -Name java -Force"
Write-Host "    Tunnel:   Stop-Process -Name cloudflared -Force"
Write-Host ""

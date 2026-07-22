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

function Test-UrlReady([string]$Url, [int]$TimeoutSec = 3) {
    try {
        $null = Invoke-WebRequest -Uri $Url -TimeoutSec $TimeoutSec -UseBasicParsing -ErrorAction Stop
        return $true
    } catch {
        return $false
    }
}

function Wait-UrlReady([string]$Url, [int]$TimeoutSec, [int]$IntervalSec, [string]$Label) {
    Write-Info "Waiting for $Label (timeout ${TimeoutSec}s)..."
    $elapsed = 0
    while ($elapsed -lt $TimeoutSec) {
        if (Test-UrlReady -Url $Url) {
            Write-Done "$Label ready (${elapsed}s)"
            return
        }
        Start-Sleep -Seconds $IntervalSec
        $elapsed += $IntervalSec
        Write-Host "." -NoNewline
    }
    Write-Host ""
    Write-Fail "$Label timeout: $Url"
}

Write-Host ""
Write-Host "CoffeeCookie HomePage - Load Service" -ForegroundColor Yellow
Write-Host "  FastAPI :8000 + Vite :3000 + Tunnel" -ForegroundColor Yellow
Write-Host "========================================="

Write-Step 1 "Read config"
$configPath = Join-Path $ProjectRoot ".config\service-config.yml"
$examplePath = Join-Path $PSScriptRoot "service-config.example.yml"
if (-not (Test-Path $configPath)) {
    Write-Fail "Config not found: $configPath`nCopy example: copy `"$examplePath`" `"$configPath`""
}
Parse-YamlConfig -Content (Get-Content $configPath -Raw -Encoding UTF8)
$cfg = $script:cfgData

$edgePort = if ($cfg['service.port']) { $cfg['service.port'] } else { "3000" }
$svcDomain = $cfg['service.domain']
$tunnelMethod = $cfg['start.tunnel.method']
$apiHealthUrl = if ($cfg['start.backend.health_check.url']) { $cfg['start.backend.health_check.url'] } else { "http://localhost:8000/api/health" }
$webHealthUrl = if ($cfg['start.frontend.health_check.url']) { $cfg['start.frontend.health_check.url'] } else { "http://localhost:3000" }
$apiHealthTimeout = if ($cfg['start.backend.health_check.timeout_ms']) { [int]$cfg['start.backend.health_check.timeout_ms'] / 1000 } else { 120 }
$apiHealthInterval = if ($cfg['start.backend.health_check.interval_ms']) { [int]$cfg['start.backend.health_check.interval_ms'] / 1000 } else { 2 }
$webHealthTimeout = if ($cfg['start.frontend.health_check.timeout_ms']) { [int]$cfg['start.frontend.health_check.timeout_ms'] / 1000 } else { 90 }
$webHealthInterval = if ($cfg['start.frontend.health_check.interval_ms']) { [int]$cfg['start.frontend.health_check.interval_ms'] / 1000 } else { 2 }
$publicUrl = $cfg['access.public']
$localUrl = if ($cfg['access.local']) { $cfg['access.local'] } else { "http://localhost:3000" }
$username = $cfg['access.default_account.username']
$password = $cfg['access.default_account.password']

Write-Done "Service: $($cfg['service.name']) | Edge port: $edgePort | Domain: $svcDomain"
Write-Done "Tunnel: $tunnelMethod | Public: $publicUrl"
if ($Dev) { Write-Info "Dev switch accepted (stack is always FastAPI + Vite)" }

Write-Step 2 "Env check"
$envTools = $cfg['env_check.required']
if ($envTools -is [string]) { $envTools = @($envTools) }
if (-not $envTools) { $envTools = @('python', 'node', 'npm', 'cloudflared') }

$toolChecks = @{
    'python'     = 'python'
    'node'       = 'node'
    'npm'        = 'npm'
    'cloudflared'= 'cloudflared'
    # legacy names in old configs — map away from Java stack
    'java'       = $null
    'mvn'        = $null
}

foreach ($tool in $envTools) {
    if ($tool -eq 'java' -or $tool -eq 'mvn') {
        Write-Info "Skipping obsolete tool check: $tool (FastAPI stack)"
        continue
    }
    $exe = $toolChecks[$tool]
    if (-not $exe) { $exe = $tool }
    $found = $null -ne (Get-Command $exe -ErrorAction SilentlyContinue)
    if ($found) {
        try {
            $ver = & $exe --version 2>&1 | Select-Object -First 1
            $verStr = ($ver -join " ").Trim()
            Write-Done "$tool -> $verStr"
        } catch {
            Write-Done "$tool -> installed"
        }
    } else {
        Write-Fail "$tool not installed"
    }
}

$apiDir = Join-Path $ProjectRoot "api"
$frontendDir = Join-Path $ProjectRoot "frontend"
$venvPython = Join-Path $apiDir ".venv\Scripts\python.exe"
$venvUvicorn = Join-Path $apiDir ".venv\Scripts\uvicorn.exe"
$apiLog = Join-Path $ProjectRoot "api.log"
$apiErr = Join-Path $ProjectRoot "api-error.log"
$webLog = Join-Path $ProjectRoot "frontend.log"
$webErr = Join-Path $ProjectRoot "frontend-error.log"

if (-not $SkipBuild) {
    Write-Step 3 "Ensure dependencies"
    if (-not (Test-Path $venvPython) -or -not (Test-Path $venvUvicorn)) {
        Write-Info "Creating api/.venv and installing requirements..."
        $py = Get-Command python -ErrorAction SilentlyContinue
        if (-not $py) { $py = Get-Command py -ErrorAction SilentlyContinue }
        if (-not $py) { Write-Fail "python not found" }
        & $py.Source -m venv (Join-Path $apiDir ".venv")
        & (Join-Path $apiDir ".venv\Scripts\pip.exe") install -r (Join-Path $apiDir "requirements.txt")
        if ($LASTEXITCODE -ne 0) { Write-Fail "pip install failed" }
    } else {
        Write-Done "api/.venv ready"
    }
    Push-Location $frontendDir
    try {
        if (-not (Test-Path "node_modules")) {
            Write-Info "npm install..."
            npm install 2>&1 | ForEach-Object { Write-Host $_ }
            if ($LASTEXITCODE -ne 0) { Write-Fail "npm install failed" }
        } else {
            Write-Done "frontend/node_modules ready"
        }
    } finally {
        Pop-Location
    }
} else {
    Write-Step 3 "Dependencies (skipped)"
    if (-not (Test-Path $venvUvicorn)) { Write-Fail "Missing $venvUvicorn (run without -SkipBuild once)" }
}

Write-Step 4 "Start FastAPI (:8000)"
if (Test-UrlReady -Url $apiHealthUrl) {
    Write-Done "API already running"
} else {
    Write-Info "Starting uvicorn..."
    $apiCmd = "`"$venvUvicorn`" app.main:app --host 127.0.0.1 --port 8000 > `"$apiLog`" 2> `"$apiErr`""
    Start-Process -FilePath "cmd.exe" -ArgumentList "/c $apiCmd" -WorkingDirectory $apiDir -WindowStyle Hidden
    Wait-UrlReady -Url $apiHealthUrl -TimeoutSec ([int]$apiHealthTimeout) -IntervalSec ([int]$apiHealthInterval) -Label "API"
}

Write-Step 5 "Start Vite (:3000)"
if (Test-UrlReady -Url $webHealthUrl) {
    Write-Done "Vite already running"
} else {
    Write-Info "Starting npm run dev..."
    $webCmd = "npm run dev > `"$webLog`" 2> `"$webErr`""
    Start-Process -FilePath "cmd.exe" -ArgumentList "/c $webCmd" -WorkingDirectory $frontendDir -WindowStyle Hidden
    Wait-UrlReady -Url $webHealthUrl -TimeoutSec ([int]$webHealthTimeout) -IntervalSec ([int]$webHealthInterval) -Label "Vite"
}

if (-not $SkipTunnel) {
    Write-Step 6 "Start Cloudflare Tunnel ($tunnelMethod)"

    if ($tunnelMethod -eq 'existing-domain') {
        $configFile = Join-Path $ProjectRoot $cfg['start.tunnel.config_file']
        if (-not (Test-Path $configFile)) { Write-Fail "Tunnel config not found: $configFile" }

        Write-Info "Config: $configFile"
        Write-Info "Public: $publicUrl (fixed domain → localhost:$edgePort)"
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
        Write-Info "Quick Tunnel mode (edge http://localhost:$edgePort)..."
        $tunnelLog = Join-Path $ProjectRoot "tunnel.log"
        Start-Process -FilePath "cloudflared" `
            -ArgumentList "tunnel --url http://localhost:$edgePort" `
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
    Write-Step 6 "Cloudflare Tunnel (skipped)"
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host "  DEPLOY SUCCESS" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Frontend: $localUrl"
Write-Host "  API:      $apiHealthUrl"
if (-not $SkipTunnel) { Write-Host "  Tunnel:   running" }
Write-Host ""
Write-Host "  Public:   $publicUrl"
Write-Host ""
Write-Host "  Login:    $username / $password"
Write-Host ""
Write-Host "  Logs:"
Write-Host "    API:      Get-Content $apiLog -Tail 50 -Wait"
Write-Host "    API err:  Get-Content $apiErr -Tail 50"
Write-Host "    Frontend: Get-Content $webLog -Tail 50 -Wait"
Write-Host ""
Write-Host "  Stop:"
Write-Host "    stop.bat   (or stop uvicorn / node / cloudflared processes)"
Write-Host ""

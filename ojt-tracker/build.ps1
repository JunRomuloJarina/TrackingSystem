param(
    [switch]$Portable
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$distDirectory = Join-Path $projectRoot "dist"
$appDirectory = Join-Path $distDirectory "OJT-Tracker"

Push-Location $projectRoot
try {
    if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
        throw "Maven was not found on PATH. Install Maven or open a terminal where mvn is available."
    }

    if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
        throw "jpackage was not found on PATH. Install a JDK 17 or newer and add its bin folder to PATH."
    }

    if (-not $Portable) {
        $wixV3Available = (Get-Command candle.exe -ErrorAction SilentlyContinue) -and
            (Get-Command light.exe -ErrorAction SilentlyContinue)
        $wixV4Available = Get-Command wix.exe -ErrorAction SilentlyContinue
        $wixAvailable = $wixV3Available -or $wixV4Available
        if (-not $wixAvailable) {
            throw "WiX Toolset is required for the installer. Install WiX 3.x or 4.x from https://wixtoolset.org in an elevated terminal, then reopen this terminal. Use -Portable for a self-contained app without an installer."
        }
    }

    Write-Host "Running tests and creating the executable JAR..."
    & mvn clean package
    if ($LASTEXITCODE -ne 0) {
        throw "Maven packaging failed."
    }

    if (Test-Path $distDirectory) {
        Remove-Item $distDirectory -Recurse -Force
    }
    New-Item -ItemType Directory -Path $distDirectory | Out-Null

    $packageType = if ($Portable) { "app-image" } else { "exe" }
    $packageArguments = @(
        "--type", $packageType,
        "--input", (Join-Path $projectRoot "target"),
        "--main-jar", "ojt-tracker.jar",
        "--main-class", "com.ojttracker.Main",
        "--name", "OJT-Tracker",
        "--app-version", "1.0.0",
        "--vendor", "OJT Tracker",
        "--dest", $distDirectory
    )

    if (-not $Portable) {
        $packageArguments += @("--win-menu", "--win-shortcut", "--win-per-user-install")
        Write-Host "Creating the Windows installer..."
    } else {
        Write-Host "Creating the portable Windows executable..."
    }

    & jpackage @packageArguments
    if ($LASTEXITCODE -ne 0) {
        if (-not $Portable) {
            throw "Installer creation failed. The default installer build requires WiX Toolset 3.x on PATH. Use -Portable for a self-contained app folder."
        }
        throw "Executable packaging failed."
    }

    if (-not $Portable) {
        Write-Host "Ready: $distDirectory\OJT-Tracker-1.0.0.exe"
    } else {
        Write-Host "Ready: $appDirectory\OJT-Tracker.exe"
    }
}
finally {
    Pop-Location
}
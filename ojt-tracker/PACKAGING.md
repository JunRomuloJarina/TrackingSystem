# Windows packaging

## Create the installable Windows app

From the `ojt-tracker` folder, run:

```powershell
.\build.ps1
```

The installer is created at `dist\OJT-Tracker-1.0.0.exe`.
Double-clicking `build.bat` runs the same installer build. It includes a Java runtime, so users do not need Java installed.

## Create the portable executable

Use this when WiX Toolset is not installed:

```powershell
.\build.ps1 -Portable
```

The portable application is created at `dist\OJT-Tracker\OJT-Tracker.exe`.

The installer build requires WiX Toolset 3.x (`candle.exe` and `light.exe`) or WiX 4.x (`wix.exe`) available on `PATH`.
Install it from the WiX website or run `winget install --id WiXToolset.WiXToolset --exact` from **Run as administrator**, then reopen PowerShell before building. The generated installer is configured for per-user installation.

The application stores its SQLite database in the current user's Windows app-data folder, not beside the executable.

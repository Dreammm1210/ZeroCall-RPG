$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
Set-Location $PSScriptRoot
try { $v = ((& java -version 2>&1) | Out-String) } catch { throw 'Chua cai Java JDK 21.' }
if ($v -notmatch 'version "21\.') { throw "Can JDK 21. Tim thay: $v" }
$gradleVersion = '9.2.1'
$homeDir = Join-Path $PSScriptRoot '.gradle-bootstrap'
$gradleCmd = Join-Path $homeDir "gradle-$gradleVersion\bin\gradle.bat"
if (!(Test-Path $gradleCmd)) {
  New-Item -ItemType Directory -Force -Path $homeDir | Out-Null
  $zip = Join-Path $homeDir "gradle-$gradleVersion-bin.zip"
  Invoke-WebRequest "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip" -OutFile $zip
  Expand-Archive -LiteralPath $zip -DestinationPath $homeDir -Force
  Remove-Item $zip
}
& $gradleCmd --no-daemon clean build
if ($LASTEXITCODE -ne 0) { throw 'Build fail. Gui log loi.' }
Get-ChildItem (Join-Path $PSScriptRoot 'build\libs') -Filter '*.jar' | Format-Table Name,Length

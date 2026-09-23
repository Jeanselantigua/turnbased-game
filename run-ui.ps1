$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
New-Item -ItemType Directory -Force -Path bin\main | Out-Null
$jars = (Get-ChildItem lib\javafx\*.jar | ForEach-Object { $_.FullName }) -join ";"
$argfile = Join-Path $root "bin\ui-sources.txt"
Get-ChildItem -Recurse -Filter *.java src\main\java | ForEach-Object { $_.FullName } |
    Set-Content -Encoding ascii $argfile
& javac --module-path $jars --add-modules javafx.controls -d bin\main -sourcepath src\main\java "@$argfile"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
New-Item -ItemType Directory -Force -Path bin\main\com\battlesim\ui | Out-Null
Copy-Item src\main\resources\com\battlesim\ui\game.css bin\main\com\battlesim\ui\game.css -Force
& java --module-path $jars --add-modules javafx.controls --enable-native-access=javafx.graphics -cp bin\main com.battlesim.ui.GuiMain

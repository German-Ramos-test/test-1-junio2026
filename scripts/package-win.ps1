param(
    [string]$AppVersion = "0.1.0",
    [string]$AppName = "MiniJuego2D"
)

$ErrorActionPreference = "Stop"

Write-Host "[1/3] Ejecutando build y quality gates..."
mvn clean verify
if ($LASTEXITCODE -ne 0) {
    throw "mvn clean verify fallo con codigo $LASTEXITCODE"
}

Write-Host "[2/3] Empaquetando runtime con jpackage (app-image)..."
$jar = "target/test-1-junio2026-$AppVersion.jar"
if (-not (Test-Path $jar)) {
    throw "No se encontro el JAR esperado: $jar"
}

$outDir = "dist"
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

$appDir = Join-Path $outDir $AppName
if (Test-Path $appDir) {
    Remove-Item -Recurse -Force $appDir
}

jpackage `
  --type app-image `
  --name $AppName `
  --input target `
  --main-jar (Split-Path $jar -Leaf) `
  --main-class com.example.App `
  --dest $outDir
if ($LASTEXITCODE -ne 0) {
        throw "jpackage fallo con codigo $LASTEXITCODE"
}

Write-Host "[3/3] Empaquetado completado en: $outDir/$AppName"

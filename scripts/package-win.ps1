param(
    [string]$AppVersion = "0.1.0",
    [string]$AppName = "MiniJuego2D"
)

$ErrorActionPreference = "Stop"

Write-Host "[1/3] Ejecutando build y quality gates..."
mvn clean verify

Write-Host "[2/3] Empaquetando runtime con jpackage (app-image)..."
$jar = "target/test-1-junio2026-$AppVersion.jar"
if (-not (Test-Path $jar)) {
    throw "No se encontro el JAR esperado: $jar"
}

$outDir = "dist"
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

jpackage `
  --type app-image `
  --name $AppName `
  --input target `
  --main-jar (Split-Path $jar -Leaf) `
  --main-class com.example.App `
  --dest $outDir

Write-Host "[3/3] Empaquetado completado en: $outDir/$AppName"

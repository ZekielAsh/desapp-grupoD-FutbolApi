# Script de Verificación Pre-Commit
# PowerShell

Write-Host "Verificación Pre-Commit - Tests de Integración" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Paso 1: Verificar archivos modificados
Write-Host "Paso 1: Verificando archivos clave..." -ForegroundColor Yellow

$files = @(
    "src\main\kotlin\com\example\demo\config\DataSourceConfig.kt",
    "src\test\kotlin\com\example\demo\integration\TeamControllerE2ETest.kt",
    "src\test\kotlin\com\example\demo\unitTests\authentication\AuthIntegrationTest.kt"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "  ✓ $file" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $file NO ENCONTRADO" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""

# Paso 2: Verificar @Profile("prod") en DataSourceConfig
Write-Host "Paso 2: Verificando @Profile('prod') en DataSourceConfig..." -ForegroundColor Yellow

$dataSourceContent = Get-Content "src\main\kotlin\com\example\demo\config\DataSourceConfig.kt" -Raw
if ($dataSourceContent -match '@Profile\("prod"\)') {
    Write-Host "  ✓ @Profile('prod') encontrado" -ForegroundColor Green
} else {
    Write-Host "  ✗ @Profile('prod') NO encontrado" -ForegroundColor Red
    Write-Host "    Agrega @Profile('prod') a DataSourceConfig" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Paso 3: Verificar @ActiveProfiles("test") en tests
Write-Host "Paso 3: Verificando @ActiveProfiles('test') en tests..." -ForegroundColor Yellow

$testFiles = @(
    "src\test\kotlin\com\example\demo\integration\TeamControllerE2ETest.kt",
    "src\test\kotlin\com\example\demo\unitTests\authentication\AuthIntegrationTest.kt"
)

foreach ($testFile in $testFiles) {
    $content = Get-Content $testFile -Raw
    if ($content -match '@ActiveProfiles\("test"\)') {
        Write-Host "  ✓ $(Split-Path $testFile -Leaf)" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $(Split-Path $testFile -Leaf) - Falta @ActiveProfiles('test')" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""

# Paso 4: Limpiar build anterior
Write-Host "Paso 4: Limpiando build anterior..." -ForegroundColor Yellow
.\gradlew.bat clean | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Clean exitoso" -ForegroundColor Green
} else {
    Write-Host "  ✗ Clean falló" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Paso 5: Ejecutar tests
Write-Host "Paso 5: Ejecutando tests..." -ForegroundColor Yellow
Write-Host "  (Esto puede tardar 2-4 minutos)" -ForegroundColor Gray
Write-Host ""

.\gradlew.bat test --no-daemon

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "TODOS LOS TESTS PASARON" -ForegroundColor Green
    Write-Host ""
    Write-Host "Próximos pasos:" -ForegroundColor Cyan
    Write-Host "  1. git add ." -ForegroundColor White
    Write-Host "  2. git commit -m 'fix: Configure profiles for tests and production DataSource'" -ForegroundColor White
    Write-Host "  3. git push origin main" -ForegroundColor White
    Write-Host ""
    Write-Host "GitHub Actions debería pasar ahora" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "ALGUNOS TESTS FALLARON" -ForegroundColor Red
    Write-Host ""
    Write-Host "Revisa el reporte en:" -ForegroundColor Yellow
    Write-Host "  build\reports\tests\test\index.html" -ForegroundColor White
    Write-Host ""
    Write-Host "O ejecuta con más detalle:" -ForegroundColor Yellow
    Write-Host "  .\gradlew.bat test --info" -ForegroundColor White
    exit 1
}


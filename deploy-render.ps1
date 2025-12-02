# Deploy Render - Quick Start Script
# PowerShell version

Write-Host "🚀 Quick Start - Deploy a Render" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Paso 1: Verificar archivos
Write-Host "Paso 1: Verificando archivos..." -ForegroundColor Yellow
if (Test-Path "Dockerfile") {
    Write-Host "✓ Dockerfile encontrado" -ForegroundColor Green
} else {
    Write-Host "✗ Dockerfile NO encontrado" -ForegroundColor Red
    exit 1
}

if (Test-Path "render.yaml") {
    Write-Host "✓ render.yaml encontrado" -ForegroundColor Green
} else {
    Write-Host "✗ render.yaml NO encontrado" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Paso 2: Generar JWT Secret
Write-Host "Paso 2: Generando JWT Secret..." -ForegroundColor Yellow
$bytes = New-Object byte[] 64
$rng = [System.Security.Cryptography.RNGCryptoServiceProvider]::Create()
$rng.GetBytes($bytes)
$JWT_SECRET = [Convert]::ToBase64String($bytes)

Write-Host "JWT_SECRET generado:" -ForegroundColor Green
Write-Host $JWT_SECRET
Write-Host ""
Write-Host "⚠️  GUARDA ESTE SECRET - Lo necesitarás en Render" -ForegroundColor Yellow
Write-Host ""

# Paso 3: Test Docker (opcional)
Write-Host "Paso 3: Test local con Docker..." -ForegroundColor Yellow
$testDocker = Read-Host "¿Quieres probar el build localmente? (y/n)"

if ($testDocker -eq "y") {
    Write-Host "Construyendo imagen Docker..." -ForegroundColor Cyan
    docker build -t futbol-api-test .

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Build exitoso" -ForegroundColor Green
        Write-Host ""
        $runDocker = Read-Host "¿Ejecutar container? (y/n)"

        if ($runDocker -eq "y") {
            Write-Host "Ejecutando container en puerto 8080..." -ForegroundColor Cyan
            docker run -p 8080:8080 `
                -e JWT_SECRET="$JWT_SECRET" `
                -e SPRING_PROFILES_ACTIVE=prod `
                futbol-api-test
        }
    } else {
        Write-Host "✗ Build falló - revisa los errores" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""

# Paso 4: Git commands
Write-Host "Paso 4: Commit y Push" -ForegroundColor Yellow
Write-Host "Ejecuta estos comandos:" -ForegroundColor White
Write-Host ""
Write-Host "  git add ." -ForegroundColor Gray
Write-Host "  git commit -m 'feat: Add Render deployment configuration'" -ForegroundColor Gray
Write-Host "  git push origin main" -ForegroundColor Gray
Write-Host ""

# Paso 5: Instrucciones Render
Write-Host "Paso 5: Configurar en Render" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Ve a https://dashboard.render.com" -ForegroundColor White
Write-Host "2. Crea PostgreSQL Database:" -ForegroundColor White
Write-Host "   - Name: futbol-db" -ForegroundColor Gray
Write-Host "   - Region: Oregon (o el más cercano)" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Crea Web Service:" -ForegroundColor White
Write-Host "   - Conecta tu repo de GitHub/GitLab" -ForegroundColor Gray
Write-Host "   - Environment: Docker" -ForegroundColor Gray
Write-Host "   - Agrega estas variables:" -ForegroundColor Gray
Write-Host ""
Write-Host "   SPRING_PROFILES_ACTIVE=prod" -ForegroundColor Cyan
Write-Host "   PORT=8080" -ForegroundColor Cyan
Write-Host "   JWT_SECRET=$JWT_SECRET" -ForegroundColor Cyan
Write-Host "   JWT_EXPIRATION=86400000" -ForegroundColor Cyan
Write-Host "   DATABASE_URL=[copiar de Render PostgreSQL]" -ForegroundColor Cyan
Write-Host "   DATABASE_USERNAME=futboluser" -ForegroundColor Cyan
Write-Host "   DATABASE_PASSWORD=[copiar de Render PostgreSQL]" -ForegroundColor Cyan
Write-Host "   DATABASE_DRIVER=org.postgresql.Driver" -ForegroundColor Cyan
Write-Host "   DDL_AUTO=update" -ForegroundColor Cyan
Write-Host ""
Write-Host "4. Health Check Path: /actuator/health" -ForegroundColor White
Write-Host ""
Write-Host "5. Click 'Create Web Service'" -ForegroundColor White
Write-Host ""
Write-Host "¡Listo! Tu API estará disponible en unos minutos" -ForegroundColor Green
Write-Host ""
Write-Host "📚 Lee RENDER_DEPLOYMENT_GUIDE.md para más detalles" -ForegroundColor Cyan

# Pause
Write-Host ""
Write-Host "Presiona cualquier tecla para salir..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")


# ============================================
# TaskFlow - Script de Setup PostgreSQL 17
# ============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " TaskFlow - Setup PostgreSQL 17" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se Docker está instalado
try {
    $dockerVersion = docker --version
    Write-Host "✓ Docker encontrado: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker não encontrado. Instale o Docker Desktop." -ForegroundColor Red
    exit 1
}

# Verificar se Docker Compose está disponível
try {
    $composeVersion = docker-compose --version
    Write-Host "✓ Docker Compose encontrado: $composeVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker Compose não encontrado." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Iniciando PostgreSQL 17..." -ForegroundColor Yellow

# Parar containers existentes
docker-compose down -v 2>$null

# Iniciar PostgreSQL
docker-compose up -d

Write-Host ""
Write-Host "Aguardando PostgreSQL estar pronto..." -ForegroundColor Yellow

# Aguardar PostgreSQL estar saudável
$maxAttempts = 30
$attempt = 0
do {
    $attempt++
    $health = docker inspect --format='{{.State.Health.Status}}' taskflow-postgres 2>$null
    if ($health -eq "healthy") {
        Write-Host "✓ PostgreSQL está pronto!" -ForegroundColor Green
        break
    }
    Write-Host "  Tentativa $attempt/$maxAttempts..." -ForegroundColor Gray
    Start-Sleep -Seconds 2
} while ($attempt -lt $maxAttempts)

if ($attempt -eq $maxAttempts) {
    Write-Host "✗ Timeout aguardando PostgreSQL" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Configuração concluída!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Credenciais:" -ForegroundColor Yellow
Write-Host "  Host: localhost" -ForegroundColor White
Write-Host "  Port: 5432" -ForegroundColor White
Write-Host "  Database: datatarefas" -ForegroundColor White
Write-Host "  User: postgres" -ForegroundColor White
Write-Host "  Password: 123456" -ForegroundColor White
Write-Host ""
Write-Host "Para conectar via psql:" -ForegroundColor Yellow
Write-Host "  psql -h localhost -U postgres -d datatarefas" -ForegroundColor White
Write-Host ""
Write-Host "Para executar a aplicação com PostgreSQL:" -ForegroundColor Yellow
Write-Host "  cd backend" -ForegroundColor White
Write-Host "  mvnw spring-boot:run -Dspring-boot.run.profiles=postgres" -ForegroundColor White
Write-Host ""
Write-Host "Para parar o PostgreSQL:" -ForegroundColor Yellow
Write-Host "  docker-compose down" -ForegroundColor White
Write-Host ""

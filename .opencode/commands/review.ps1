param([string]$Path = ".")

if (-not (Test-Path $Path)) {
  Write-Host "Erro: caminho não encontrado: $Path" -ForegroundColor Red
  exit 1
}

Write-Host "============================================" -ForegroundColor Blue
Write-Host "  REVISÃO DE CÓDIGO" -ForegroundColor Blue
Write-Host "============================================" -ForegroundColor Blue
Write-Host ""
Write-Host "Alvo: $Path" -ForegroundColor Cyan
Write-Host ""

# Coleta arquivos
if (Test-Path -Path $Path -PathType Container) {
  $files = Get-ChildItem -Path $Path -Recurse -Include "*.ts", "*.java", "*.html", "*.css", "*.json", "*.properties", "*.sh", "*.yml", "*.yaml" -ErrorAction SilentlyContinue | Select-Object -First 30
} else {
  $files = Get-Item -Path $Path
}

foreach ($file in $files) {
  Write-Host ">> $($file.FullName)" -ForegroundColor Yellow
  $lines = (Get-Content $file.FullName -ErrorAction SilentlyContinue | Measure-Object).Count
  Write-Host "   $lines linhas"
}

Write-Host ""
Write-Host "Verificações gerais" -ForegroundColor Yellow

# console.log em não-teste
$consoleLogs = Select-String -Path $files.FullName -Pattern "console\.log" -SimpleMatch -ErrorAction SilentlyContinue |
               Where-Object { $_ -notmatch "spec\.ts|e2e\\" } | Select-Object -First 5
if ($consoleLogs) {
  Write-Host "  ⚠️  console.log encontrados:" -ForegroundColor Yellow
  $consoleLogs | ForEach-Object { Write-Host "    - $($_.Path):$($_.LineNumber)" }
}

# TODOs
$todos = Select-String -Path $files.FullName -Pattern "TODO|FIXME|HACK|XXX" -ErrorAction SilentlyContinue | Select-Object -First 5
if ($todos) {
  Write-Host "  💡 TODOs/FIXME pendentes:" -ForegroundColor Cyan
  $todos | ForEach-Object { Write-Host "    - $($_.Path):$($_.LineNumber)" }
}

Write-Host ""
Write-Host "Revisão concluída." -ForegroundColor Blue

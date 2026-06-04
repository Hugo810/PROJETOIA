#!/bin/bash
# ============================================================
# analisar-bug.sh — Análise de Causa Raiz de Bug
# ============================================================
# Uso: ./analisar-bug.sh "descrição do bug"
# ============================================================

set -euo pipefail

BG_BOLD='\033[1;34m'
BG_STEP='\033[1;33m'
BG_OK='\033[1;32m'
BG_ERRO='\033[1;31m'
BG_RESET='\033[0m'

BUG_DESC="${1:-}"

if [ -z "$BUG_DESC" ]; then
  echo -e "${BG_ERRO}Uso:${BG_RESET} ./analisar-bug.sh \"descrição do bug\""
  exit 1
fi

echo -e "${BG_BOLD}============================================${BG_RESET}"
echo -e "${BG_BOLD}  ANALISADOR DE CAUSA RAIZ${BG_RESET}"
echo -e "${BG_BOLD}============================================${BG_RESET}"
echo ""
echo -e "Bug: ${BG_STEP}$BUG_DESC${BG_RESET}"
echo ""

# -----------------------------------------------------------
# Passo 1 — Perguntas de clarificação
# -----------------------------------------------------------
echo -e "${BG_STEP}[1/5] Perguntas de clarificação${BG_RESET}"

read -r -p "Q1: Qual o comportamento esperado? " ESPERADO
read -r -p "Q2: Qual o comportamento atual? " ATUAL
read -r -p "Q3: Como reproduzir? (passos ou URL) " REPRO

echo ""

# -----------------------------------------------------------
# Passo 2 — Coleta de evidências
# -----------------------------------------------------------
echo -e "${BG_STEP}[2/5] Coleta de evidências${BG_RESET}"

# Busca no código por arquivos relacionados ao bug
echo "  Buscando arquivos relacionados..."

# Extrai palavras-chave do bug description + respostas
KEYWORDS=$(echo "$BUG_DESC $ESPERADO $ATUAL" | tr ' ' '\n' | sort -u | tr '\n' ' ')

# Monta lista de possíveis localizações
CANDIDATOS=""
for kw in $BUG_DESC $ESPERADO $ATUAL; do
  kw_clean=$(echo "$kw" | tr -cd '[:alnum:]_-')
  if [ ${#kw_clean} -ge 3 ]; then
    RESULT=$(grep -rl --include="*.ts" --include="*.java" --include="*.html" -i "$kw_clean" . 2>/dev/null | head -5)
    if [ -n "$RESULT" ]; then
      CANDIDATOS="$CANDIDATOS $RESULT"
    fi
  fi
done

CANDIDATOS=$(echo "$CANDIDATOS" | tr ' ' '\n' | sort -u | head -10)
if [ -n "$CANDIDATOS" ]; then
  echo "  Arquivos potencialmente relevantes:"
  echo "$CANDIDATOS" | while read -r f; do
    echo "    - $f"
  done
else
  echo "  (Nenhum arquivo encontrado por palavra-chave)"
fi

# Últimos commits (se repo git)
if git rev-parse --git-dir > /dev/null 2>&1; then
  echo ""
  echo "  Últimos 5 commits:"
  git log --oneline -5 2>/dev/null | while read -r line; do
    echo "    $line"
  done
fi

echo ""

# -----------------------------------------------------------
# Passo 3 — Análise de logs (backend + frontend)
# -----------------------------------------------------------
echo -e "${BG_STEP}[3/5] Análise de logs disponíveis${BG_RESET}"

# Verifica se backend está rodando
if curl -sf http://localhost:8080/api/health > /dev/null 2>&1; then
  echo -e "  ${BG_OK}Backend OK${BG_RESET} (porta 8080)"
else
  echo "  Backend: não respondeu em 8080"
fi

# Verifica se frontend está rodando
if curl -sf http://localhost:4200 > /dev/null 2>&1; then
  echo -e "  ${BG_OK}Frontend OK${BG_RESET} (porta 4200)"
else
  echo "  Frontend: não respondeu em 4200"
fi

# Verifica se há logs de erro no backend
if [ -d "backend/logs" ]; then
  tail -50 backend/logs/*.log 2>/dev/null || echo "  (sem logs)"
fi

echo ""

# -----------------------------------------------------------
# Passo 4 — Identificação da causa raiz
# -----------------------------------------------------------
echo -e "${BG_STEP}[4/5] Identificação da causa raiz${BG_RESET}"

echo "  Bug: $BUG_DESC"
echo "  Esperado: $ESPERADO"
echo "  Atual: $ATUAL"
echo "  Reprodução: $REPRO"
echo ""

echo "  Causa raiz hipotética:"
echo "    Gap entre comportamento esperado e atual"
echo "    Fatores contribuintes:"
echo "      - Código não reflete a especificação"
echo "      - Testes insuficientes na camada afetada"
echo "      - Side effect não previsto na alteração recente"

# Tenta encontrar o diff relevante
if git rev-parse --git-dir > /dev/null 2>&1; then
  echo ""
  echo "  Arquivos modificados recentemente:"
  git diff --name-only HEAD~1 2>/dev/null | while read -r f; do
    echo "    - $f"
  done
fi

echo ""

# -----------------------------------------------------------
# Passo 5 — Sugestão de correção
# -----------------------------------------------------------
echo -e "${BG_STEP}[5/5] Sugestão de correção${BG_RESET}"

echo "  Arquivos afetados:"
echo "$CANDIDATOS" | while read -r f; do
  if [ -n "$f" ]; then
    echo "    - $f"
  fi
done
echo ""

read -r -p "Deseja iniciar /corrigir-bug automaticamente? (s/N) " AUTO_FIX

if [ "$AUTO_FIX" = "s" ] || [ "$AUTO_FIX" = "S" ]; then
  echo ""
  echo -e "${BG_OK}Invocando /corrigir-bug...${BG_RESET}"
  echo "  Bug: $BUG_DESC"
  echo "  Causa raiz: gap entre esperado ($ESPERADO) e atual ($ATUAL)"
  echo "  Reprodução: $REPRO"
  echo "  Arquivos: $CANDIDATOS"
  echo ""
  echo "  (Modo automatizado — execute os comandos sugeridos manualmente)"
fi

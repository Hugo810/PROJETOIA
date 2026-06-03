#!/bin/bash
# ============================================================
# review.sh — Revisão de Código
# ============================================================
# Uso: ./review.sh [caminho-do-arquivo]
# ============================================================

set -euo pipefail

BG_TITLE='\033[1;34m'
BG_STEP='\033[1;33m'
BG_RESET='\033[0m'

TARGET="${1:-.}"

if [ ! -e "$TARGET" ]; then
  echo "Erro: caminho não encontrado: $TARGET"
  exit 1
fi

echo -e "${BG_TITLE}============================================${BG_RESET}"
echo -e "${BG_TITLE}  REVISÃO DE CÓDIGO${BG_RESET}"
echo -e "${BG_TITLE}============================================${BG_RESET}"
echo ""
echo "Alvo: $TARGET"
echo ""

# Coleta arquivos
if [ -d "$TARGET" ]; then
  FILES=$(find "$TARGET" -type f \( -name "*.ts" -o -name "*.java" -o -name "*.html" -o -name "*.css" -o -name "*.json" -o -name "*.sh" -o -name "*.yml" -o -name "*.yaml" -o -name "*.properties" \) | head -30)
else
  FILES="$TARGET"
fi

# Análise de cada arquivo
for FILE in $FILES; do
  echo -e "${BG_STEP}>> $FILE${BG_RESET}"
  lines=$(wc -l < "$FILE" 2>/dev/null || echo "0")
  echo "   $lines linhas"
  echo ""
done

# Verificações gerais
echo -e "${BG_STEP}Verificações gerais${BG_RESET}"

# Procura por console.log (deixados em produção)
CONSOLE_LOGS=$(grep -rn "console\.log" $FILES 2>/dev/null | grep -v "node_modules" | grep -v "\.spec\.ts" | grep -v "e2e/" | head -5)
if [ -n "$CONSOLE_LOGS" ]; then
  echo "  ⚠️  console.log encontrados:"
  echo "$CONSOLE_LOGS" | while read -r line; do
    echo "    - $line"
  done
fi

# Procura por TODOs/FIXME
TODOS=$(grep -rn "TODO\|FIXME\|HACK\|XXX" $FILES 2>/dev/null | grep -v "node_modules" | head -5)
if [ -n "$TODOS" ]; then
  echo "  💡 TODOs/FIXME pendentes:"
  echo "$TODOS" | while read -r line; do
    echo "    - $line"
  done
fi

# Procura por @ts-expect-error ou @ts-ignore
TS_IGNORE=$(grep -rn "@ts-expect-error\|@ts-ignore" $FILES 2>/dev/null | head -5)
if [ -n "$TS_IGNORE" ]; then
  echo "  ⚠️  @ts-expect-error / @ts-ignore:"
  echo "$TS_IGNORE" | while read -r line; do
    echo "    - $line"
  done
fi

# Verifica arquivos sem cobertura de teste
echo ""
echo -e "${BG_STEP}Verificação de cobertura de teste${BG_RESET}"
for FILE in $FILES; do
  basename=$(basename "$FILE" .ts)
  dirname=$(dirname "$FILE")
  test_file="$dirname/$basename.spec.ts"
  if [ ! -f "$test_file" ]; then
    test_file="$dirname/../__tests__/$basename.spec.ts"
  fi
  if [ ! -f "$test_file" ]; then
    echo "  💡 $basename — sem teste unitário encontrado"
  fi
done

# Arquivos no .gitignore (se git)
if git rev-parse --git-dir > /dev/null 2>&1; then
  GITIGNORE_CHECK=$(git check-ignore $FILES 2>/dev/null | head -3)
  if [ -n "$GITIGNORE_CHECK" ]; then
    echo ""
    echo "  ⚠️  Arquivos no .gitignore que estão sendo revisados:"
    echo "$GITIGNORE_CHECK"
  fi
fi

echo ""
echo -e "${BG_TITLE}Revisão concluída.${BG_RESET}"

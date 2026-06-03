#!/bin/bash
# ============================================================
# implementar.sh — Decompõe task em subtasks com TDD
# ============================================================
# Uso: ./implementar.sh "descrição da task"
# ============================================================

set -euo pipefail

BG_TITLE='\033[1;34m'
BG_STEP='\033[1;33m'
BG_TASK='\033[1;35m'
BG_OK='\033[1;32m'
BG_RESET='\033[0m'

TASK_DESC="${1:-}"

if [ -z "$TASK_DESC" ]; then
  echo -e "\033[1;31mUso:\033[0m ./implementar.sh \"descrição da task\""
  exit 1
fi

echo -e "${BG_TITLE}============================================${BG_RESET}"
echo -e "${BG_TITLE}  DECOMPOSIÇÃO DE TASK (TDD)${BG_RESET}"
echo -e "${BG_TITLE}============================================${BG_RESET}"
echo ""
echo -e "Task: ${BG_STEP}$TASK_DESC${BG_RESET}"
echo ""

# -----------------------------------------------------------
# Fase 1 — Clarificação (até 3 perguntas)
# -----------------------------------------------------------
echo -e "${BG_STEP}[Fase 1] Perguntas de clarificação${BG_RESET}"

read -r -p "P1: Qual o escopo? (backend/frontend/ambos) " ESCOPO
read -r -p "P2: Quais os critérios de aceitação? " CRITERIOS
read -r -p "P3: Existe interface de usuário envolvida? (s/N) " UI

echo ""

# -----------------------------------------------------------
# Fase 2 — Geração do plano
# -----------------------------------------------------------
echo -e "${BG_STEP}[Fase 2] Plano de implementação${BG_RESET}"

# Define os componentes afetados com base no escopo
SPRINTS=()
TASKS=()
TESTS=()

if [ "$ESCOPO" = "frontend" ] || [ "$ESCOPO" = "ambos" ]; then
  SPRINTS+=("Sprint FE: Componente + Serviço")
  TASKS+=("Task 1 - Criar modelo/interface no frontend")
  TESTS+=("  Teste 1: modelo possui os campos corretos")
  TESTS+=("  Teste 2: instância default tem valores esperados")
  TASKS+=("Task 2 - Criar serviço HTTP")
  TESTS+=("  Teste 1: método listar chama GET /api/...")
  TESTS+=("  Teste 2: método criar chama POST /api/...")
  TASKS+=("Task 3 - Criar componente com template")
  TESTS+=("  Teste 1: componente renderiza dados mockados")
  TESTS+=("  Teste 2: evento de clique propaga corretamente")
  if [ "$UI" = "s" ] || [ "$UI" = "S" ]; then
    TASKS+=("Task 4 - Teste E2E do fluxo completo")
    TESTS+=("  Teste 1: Playwright — criar registro via UI")
    TESTS+=("  Teste 2: Playwright — editar e confirmar alteração")
  fi
fi

if [ "$ESCOPO" = "backend" ] || [ "$ESCOPO" = "ambos" ]; then
  SPRINTS+=("Sprint BE: Controller + Service + Repository")
  TASKS+=("Task 1 - Criar entidade JPA")
  TESTS+=("  Teste 1: @DataJpaTest — save e findById")
  TESTS+=("  Teste 2: @DataJpaTest — derived query funciona")
  TASKS+=("Task 2 - Criar DTO e Service")
  TESTS+=("  Teste 1: Mockito — criar retorna DTO esperado")
  TESTS+=("  Teste 2: Mockito — erro 404 se id inexistente")
  TASKS+=("Task 3 - Criar Controller REST")
  TESTS+=("  Teste 1: MockMvc — POST retorna 201 + Location header")
  TESTS+=("  Teste 2: MockMvc — GET lista retorna 200 + JSON")
fi

# Exibe o plano
echo ""
echo -e "${BG_TITLE}Estrutura do plano:${BG_RESET}"
for sprint in "${SPRINTS[@]}"; do
  echo ""
  echo -e "${BG_TASK}$sprint${BG_RESET}"
done

echo ""
echo -e "${BG_TITLE}Tasks:${BG_RESET}"
for i in "${!TASKS[@]}"; do
  echo ""
  echo "  ${TASKS[$i]}"
  test_idx=$((i * 2))
  if [ $test_idx -lt ${#TESTS[@]} ]; then
    echo "    ${TESTS[$test_idx]}"
  fi
  test_idx=$((i * 2 + 1))
  if [ $test_idx -lt ${#TESTS[@]} ]; then
    echo "    ${TESTS[$test_idx]}"
  fi
done

echo ""
echo -e "${BG_TITLE}Pré-requisitos para cada task:${BG_RESET}"
echo "  - Mínimo 2 testes definidos ANTES do código"
echo "  - Testes devem falhar no início (Red)"
echo "  - Implementar até passar (Green)"
echo "  - Refatorar mantendo testes verdes (Refactor)"
echo ""

# -----------------------------------------------------------
# Fase 3 — Verificação
# -----------------------------------------------------------
echo -e "${BG_STEP}[Fase 3] Verificação pós-implementação${BG_RESET}"

echo "  Comandos para validar:"
if [ "$ESCOPO" = "frontend" ] || [ "$ESCOPO" = "ambos" ]; then
  echo "    cd frontend && npm run test"
  if [ "$UI" = "s" ] || [ "$UI" = "S" ]; then
    echo "    cd frontend && npx playwright test"
  fi
fi
if [ "$ESCOPO" = "backend" ] || [ "$ESCOPO" = "ambos" ]; then
  echo "    cd backend && mvnw test"
fi
echo ""
echo "  Revisão final: ./review.sh [caminho]"

echo ""
echo -e "${BG_OK}Plano gerado. Para começar, execute a primeira task.${BG_RESET}"

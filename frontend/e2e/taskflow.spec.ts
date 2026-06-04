import { test, expect, Page } from '@playwright/test';

async function login(page: Page) {
  await page.goto('/login');
  await page.fill('input[type="email"]', 'admin@taskflow.com');
  await page.fill('input[type="password"]', 'admin');
  await page.click('button[type="submit"]');
  await page.waitForURL('**/tarefas');
}

async function findTaskCard(page: Page, title: string) {
  for (let attempt = 0; attempt < 10; attempt++) {
    const card = page.locator('.task-card').filter({ hasText: title });
    if (await card.count() > 0) return card;
    const proximaBtn = page.locator('.paginacao button:last-child');
    if (await proximaBtn.isEnabled()) {
      await proximaBtn.click();
      await page.waitForTimeout(500);
    } else {
      break;
    }
  }
  return page.locator('.task-card').filter({ hasText: title });
}

test.describe('TaskFlow E2E', () => {

  test('deve carregar a página inicial após login', async ({ page }) => {
    await login(page);
    await expect(page.locator('h2')).toHaveText('Tarefas');
    await expect(page.locator('.btn-primary')).toHaveText('Nova Tarefa');
  });

  test('deve exibir opções de filtro após login', async ({ page }) => {
    await login(page);
    const selects = page.locator('select');
    await expect(selects).toHaveCount(2);
  });

  test('deve navegar para o formulário de nova tarefa', async ({ page }) => {
    await login(page);
    await page.click('text=Nova Tarefa');
    await expect(page.locator('h1')).toHaveText('Nova Tarefa');
    await expect(page.locator('form')).toBeVisible();
  });

  test('deve validar campos obrigatórios no formulário', async ({ page }) => {
    await login(page);
    await page.goto('/nova');
    const tituloInput = page.locator('#titulo');
    const prazoInput = page.locator('#prazo');

    await tituloInput.focus();
    await tituloInput.blur();
    await expect(page.locator('.field-error')).toHaveText('Título é obrigatório.');
  });

  test('fluxo completo: criar, editar, concluir e excluir tarefa', async ({ page }) => {
    const uniqueTitle = `Tarefa E2E ${Date.now()}`;

    await login(page);
    await page.goto('/nova');
    await expect(page.locator('h1')).toHaveText('Nova Tarefa');

    await page.fill('#titulo', uniqueTitle);
    await page.fill('#descricao', 'Descrição da tarefa de teste');
    await page.selectOption('#categoria', { label: 'TRABALHO' });
    await page.fill('#prazo', '2026-12-31');

    const responsePromise = page.waitForResponse(resp => resp.url().includes('/api/tarefas') && resp.request().method() === 'POST');
    await page.locator('button[type="submit"]').click();
    const response = await responsePromise;
    expect(response.status()).toBe(201);

    await page.waitForURL('**/tarefas');
    await page.waitForTimeout(500);

    await expect(page.locator('h2')).toHaveText('Tarefas');

    const taskCard = await findTaskCard(page, uniqueTitle);
    await expect(taskCard).toBeVisible({ timeout: 5000 });

    await taskCard.getByTitle('Editar').click();
    await expect(page.locator('h1')).toHaveText('Editar Tarefa');
    await page.fill('#titulo', `${uniqueTitle} Editada`);
    await page.click('button[type="submit"]');

    await page.waitForURL('**/tarefas');
    await page.waitForTimeout(500);

    const updatedCard = await findTaskCard(page, `${uniqueTitle} Editada`);
    await expect(updatedCard.locator('h3')).toContainText(new RegExp(`${uniqueTitle} Editada`, 'i'));

    await updatedCard.locator('.card-body').click();
    await page.waitForTimeout(500);
    await expect(updatedCard.locator('.status-concluida')).toContainText('Concluída');

    await updatedCard.getByTitle('Excluir').click();
    await expect(page.locator('.modal-content')).toBeVisible();
    await page.getByRole('button', { name: 'Confirmar' }).click();
    await page.waitForTimeout(1500);
    await expect(page.locator('.task-card').filter({ hasText: `${uniqueTitle} Editada` })).toHaveCount(0);
  });
});

import { test, expect } from '@playwright/test';

test.describe('TaskFlow E2E', () => {

  test('deve carregar a página inicial', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('h1')).toHaveText('TaskFlow');
    await expect(page.locator('.btn-primary')).toHaveText('Nova Tarefa');
  });

  test('deve exibir opções de filtro', async ({ page }) => {
    await page.goto('/');
    const selects = page.locator('select');
    await expect(selects).toHaveCount(2);
  });

  test('deve navegar para o formulário de nova tarefa', async ({ page }) => {
    await page.goto('/');
    await page.click('text=Nova Tarefa');
    await expect(page.locator('h1')).toHaveText('Nova Tarefa');
    await expect(page.locator('form')).toBeVisible();
  });

  test('deve validar campos obrigatórios no formulário', async ({ page }) => {
    await page.goto('/nova');
    const tituloInput = page.locator('#titulo');
    const prazoInput = page.locator('#prazo');

    await tituloInput.focus();
    await tituloInput.blur();
    await expect(page.locator('.field-error')).toHaveText('Título é obrigatório.');
  });

  test('fluxo completo: criar, editar, concluir e excluir tarefa', async ({ page }) => {
    const uniqueTitle = `Tarefa E2E ${Date.now()}`;

    await page.goto('/nova');

    await page.fill('#titulo', uniqueTitle);
    await page.fill('#descricao', 'Descrição da tarefa de teste');
    await page.selectOption('#categoria', { label: 'TRABALHO' });
    await page.fill('#prazo', '2026-12-31');

    await page.locator('button[type="submit"]').click();

    await page.waitForURL('http://localhost:4200/');
    await page.waitForTimeout(1500);

    await expect(page.locator('h1')).toHaveText('TaskFlow');

    const taskCard = page.locator('.task-card', { hasText: uniqueTitle });
    await expect(taskCard.locator('h3')).toContainText(uniqueTitle);

    await taskCard.locator('text=Editar').click();
    await expect(page.locator('h1')).toHaveText('Editar Tarefa');
    await page.fill('#titulo', `${uniqueTitle} Editada`);
    await page.click('button[type="submit"]');
    await page.waitForTimeout(500);

    const updatedCard = page.locator('.task-card', { hasText: `${uniqueTitle} Editada` });
    await expect(updatedCard.locator('h3')).toContainText(`${uniqueTitle} Editada`);

    await updatedCard.locator('text=Concluir').click();
    await page.waitForTimeout(500);
    await expect(updatedCard.locator('.status-concluida')).toContainText('Concluída');

    await updatedCard.locator('text=Excluir').click();
    await expect(page.locator('.modal-content')).toBeVisible();
    await page.getByRole('button', { name: 'Confirmar' }).click();
    await page.waitForTimeout(1500);
    await expect(page.locator('.task-card', { hasText: `${uniqueTitle} Editada` })).toHaveCount(0);
  });
});

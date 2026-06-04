import { test, expect } from '@playwright/test';

test.describe('Login', () => {

  test('deve redirecionar para /login ao acessar raiz sem autenticação', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL('/login');
    await expect(page.locator('h1')).toHaveText('TaskFlow');
  });

  test('deve exibir erro para credenciais inválidas', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#email', 'naoexiste@teste.com');
    await page.fill('#senha', '123');
    await page.click('button[type="submit"]');

    await expect(page.locator('.erro')).toBeVisible();
    await expect(page.locator('.erro')).toContainText('Email ou senha invalidos');
  });

  test('deve exibir erro para campos vazios', async ({ page }) => {
    await page.goto('/login');
    await page.click('button[type="submit"]');

    await expect(page.locator('.erro')).toBeVisible();
    await expect(page.locator('.erro')).toContainText('Preencha todos os campos');
  });

  test('deve fazer login com case-insensitive', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#email', 'ADMIN@TASKFLOW.COM');
    await page.fill('#senha', 'admin');
    await page.click('button[type="submit"]');

    await page.waitForURL('**/tarefas');
    await expect(page.locator('h2')).toHaveText('Tarefas');
    await expect(page.locator('.btn-primary')).toHaveText('Nova Tarefa');
  });

  test('deve fazer login com sucesso e acessar /tarefas', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#email', 'admin@taskflow.com');
    await page.fill('#senha', 'admin');
    await page.click('button[type="submit"]');

    await page.waitForURL('**/tarefas');
    await expect(page.locator('h2')).toHaveText('Tarefas');
    await expect(page.locator('.btn-primary')).toHaveText('Nova Tarefa');
  });

  test('deve limpar mensagem de erro ao focar no input', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#email', 'naoexiste@teste.com');
    await page.fill('#senha', '123');
    await page.click('button[type="submit"]');

    await expect(page.locator('.erro')).toBeVisible();

    await page.click('#email');
    await expect(page.locator('.erro')).not.toBeVisible();
  });

  test('deve manter mensagem de erro visivel por pelo menos 30s', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#email', 'naoexiste@teste.com');
    await page.fill('#senha', '123');
    await page.click('button[type="submit"]');

    await expect(page.locator('.erro')).toBeVisible({ timeout: 1000 });
    await expect(page.locator('.erro')).toContainText('Email ou senha invalidos');
  });

});

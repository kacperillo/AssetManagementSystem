import { test, expect } from '@playwright/test';
import { TEST_USERS } from '../fixtures/test-data';

test.describe('Uwierzytelnianie', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.evaluate(() => localStorage.clear());
  });

  test('AUTH-01: Poprawne logowanie jako Admin', async ({ page }) => {
    await page.getByLabel('Email').fill(TEST_USERS.admin.email);
    await page.getByLabel(/Hasło/i).fill(TEST_USERS.admin.password);
    await page.getByRole('button', { name: /zaloguj/i }).click();

    await expect(page).toHaveURL(/\/assets/);
    await expect(page.getByRole('heading', { name: 'Zasoby' })).toBeVisible();
  });

  test('AUTH-02: Poprawne logowanie jako Pracownik', async ({ page }) => {
    await page.getByLabel('Email').fill(TEST_USERS.employee.email);
    await page.getByLabel(/Hasło/i).fill(TEST_USERS.employee.password);
    await page.getByRole('button', { name: /zaloguj/i }).click();

    await expect(page).toHaveURL(/\/my-assets/);
  });

  test('AUTH-03: Błędne dane logowania', async ({ page }) => {
    await page.getByLabel('Email').fill('wrong@email.com');
    await page.getByLabel(/Hasło/i).fill('wrongpassword');
    await page.getByRole('button', { name: /zaloguj/i }).click();

    await expect(page.getByRole('alert')).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
  });

  test('AUTH-04: Wylogowanie', async ({ page }) => {
    // Logowanie
    await page.getByLabel('Email').fill(TEST_USERS.admin.email);
    await page.getByLabel(/Hasło/i).fill(TEST_USERS.admin.password);
    await page.getByRole('button', { name: /zaloguj/i }).click();
    await expect(page).toHaveURL(/\/assets/);

    // Wylogowanie
    await page.getByRole('button', { name: /wyloguj/i }).click();
    await expect(page).toHaveURL(/\/login/);
  });
});

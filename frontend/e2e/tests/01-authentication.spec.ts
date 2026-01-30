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

    // Wait to ensure we stay on login page (wrong credentials)
    await page.waitForTimeout(1000);
    await expect(page).toHaveURL(/\/login/);
    // Check for error message
    await expect(page.getByText(/nieprawidłowy email lub hasło/i)).toBeVisible({ timeout: 5000 });
  });

  test('AUTH-04: Wylogowanie', async ({ page }) => {
    // Logowanie
    await page.getByLabel('Email').fill(TEST_USERS.admin.email);
    await page.getByLabel(/Hasło/i).fill(TEST_USERS.admin.password);
    await page.getByRole('button', { name: /zaloguj/i }).click();
    await expect(page).toHaveURL(/\/assets/);

    // Wylogowanie - click user menu first, then logout
    await page.getByRole('button', { name: TEST_USERS.admin.email }).click();
    await page.getByRole('menuitem', { name: /wyloguj/i }).click();
    await expect(page).toHaveURL(/\/login/);
  });
});

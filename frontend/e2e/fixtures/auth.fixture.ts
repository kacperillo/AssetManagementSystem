import { test as base, Page } from '@playwright/test';
import { TEST_USERS } from './test-data';

export const test = base.extend<{
  adminPage: Page;
  employeePage: Page;
}>({
  adminPage: async ({ page }, use) => {
    await page.goto('/login');
    await page.getByLabel('Email').fill(TEST_USERS.admin.email);
    await page.getByLabel(/Hasło/i).fill(TEST_USERS.admin.password);
    await page.getByRole('button', { name: /zaloguj/i }).click();
    await page.waitForURL('**/assets');
    await use(page);
  },
  employeePage: async ({ page }, use) => {
    await page.goto('/login');
    await page.getByLabel('Email').fill(TEST_USERS.employee.email);
    await page.getByLabel(/Hasło/i).fill(TEST_USERS.employee.password);
    await page.getByRole('button', { name: /zaloguj/i }).click();
    await page.waitForURL('**/my-assets');
    await use(page);
  },
});

export { expect } from '@playwright/test';

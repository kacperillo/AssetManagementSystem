import { test, expect } from '../fixtures/auth.fixture';
import { TEST_ASSETS } from '../fixtures/test-data';

test.describe('Widok pracownika', () => {
  test('VIEW-01: Wyświetlenie własnych zasobów', async ({ employeePage: page }) => {
    await expect(page.getByRole('heading', { name: /moje zasoby/i })).toBeVisible();
    await expect(page.locator('table')).toBeVisible();

    // Pracownik ma przypisany laptop Dell XPS 15
    await expect(page.getByText(TEST_ASSETS.laptop.seriesNumber)).toBeVisible();
  });

  test('VIEW-02: Brak dostępu do stron admina', async ({ employeePage: page }) => {
    // Próba wejścia na stronę admina
    await page.goto('/employees');

    // Powinno przekierować na /my-assets
    await expect(page).not.toHaveURL(/\/employees$/);
    await expect(page).toHaveURL(/\/my-assets/);
  });
});

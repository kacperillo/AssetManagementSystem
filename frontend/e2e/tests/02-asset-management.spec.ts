import { test, expect } from '../fixtures/auth.fixture';

test.describe('Zarządzanie zasobami (Admin)', () => {
  test('Pełny cykl zarządzania zasobem: dodanie, filtrowanie, dezaktywacja', async ({ adminPage: page }) => {
    const uniqueSerial = `SN-E2E-${Date.now()}`;

    // Krok 1-2: Po zalogowaniu jesteśmy na /assets
    await expect(page.getByRole('heading', { name: 'Zasoby' })).toBeVisible();
    await expect(page.locator('table')).toBeVisible();

    // Krok 3: Otwórz modal dodawania zasobu
    await page.getByRole('button', { name: /dodaj zasób/i }).click();
    await expect(page.getByRole('heading', { name: /dodaj zasób/i })).toBeVisible();

    // Krok 4: Wypełnij formularz
    await page.getByLabel('Typ zasobu').click();
    await page.getByRole('option', { name: /tablet/i }).click();
    await page.getByLabel('Producent').fill('Microsoft');
    await page.getByLabel('Model').fill('Surface Pro');
    await page.getByLabel('Numer seryjny').fill(uniqueSerial);

    // Krok 5: Zatwierdź dodanie zasobu
    await page.getByRole('button', { name: /^dodaj$/i }).click();
    await expect(page.getByRole('cell', { name: uniqueSerial })).toBeVisible({ timeout: 5000 });

    // Krok 6-7: Filtruj aktywne i sprawdź wyniki
    await page.getByRole('radio', { name: /aktywne/i }).click();
    await page.waitForTimeout(500);
    await expect(page.getByRole('cell', { name: uniqueSerial })).toBeVisible();
    await expect(page.getByText('SN-HEADPHONES-001')).not.toBeVisible(); // nieaktywny zasób ukryty

    // Krok 8: Dezaktywuj zasób
    const assetRow = page.locator('table tbody tr').filter({ hasText: uniqueSerial });
    await assetRow.getByRole('button', { name: /dezaktywuj/i }).click();

    // Potwierdź w dialogu (jeśli istnieje)
    const confirmButton = page.getByRole('button', { name: /potwierdź|tak/i });
    if (await confirmButton.isVisible({ timeout: 1000 }).catch(() => false)) {
      await confirmButton.click();
    }
    await page.waitForTimeout(1000);

    // Krok 9: Zmień filtr na "Nieaktywne"
    await page.getByRole('radio', { name: /nieaktywne/i }).click();
    await page.waitForTimeout(500);

    // Krok 10: Potwierdź obecność zdezaktywowanego zasobu
    await expect(page.getByRole('cell', { name: uniqueSerial })).toBeVisible({ timeout: 5000 });
  });
});

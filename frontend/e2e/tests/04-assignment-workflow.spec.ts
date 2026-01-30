import { test, expect } from '../fixtures/auth.fixture';

test.describe('Workflow przypisań (Admin)', () => {
  test('Pełny cykl życia przypisania: utworzenie, weryfikacja, zakończenie', async ({ adminPage: page }) => {
    // Krok 1-2: Przejdź do strony przydziałów
    await page.goto('/assignments');
    await expect(page.getByRole('heading', { name: 'Przydziały' })).toBeVisible();
    await expect(page.locator('table')).toBeVisible();
    await expect(page.getByRole('button', { name: /utwórz przydział/i })).toBeVisible();

    // Krok 3: Otwórz modal tworzenia przydziału
    await page.getByRole('button', { name: /utwórz przydział/i }).click();
    await expect(page.getByRole('heading', { name: /utwórz przydział/i })).toBeVisible();

    // Krok 4: Wybierz pracownika i zasób
    await page.getByLabel('Pracownik').click();
    await page.getByRole('option', { name: /jan kowalski/i }).click();
    await page.waitForTimeout(300); // czekaj na zamknięcie poprzedniego dropdownu
    await page.getByLabel('Zasób').click();
    await expect(page.getByRole('listbox')).toBeVisible();
    await page.getByRole('option', { name: /iphone 14/i }).click();

    // Krok 5: Zatwierdź utworzenie przypisania
    await page.getByRole('button', { name: /^utwórz$/i }).click();
    await expect(page.locator('table').getByText('iPhone 14')).toBeVisible({ timeout: 5000 });

    // Krok 6-7: Przejdź do widoku zasobów i zaznacz filtr "Przypisane"
    await page.goto('/assets');
    await expect(page.getByRole('heading', { name: 'Zasoby' })).toBeVisible();
    await page.getByRole('radio', { name: /^przypisane$/i }).click();
    await page.waitForTimeout(500);

    // Krok 8: Potwierdź obecność przypisanego zasobu (iPhone 14)
    await expect(page.getByText('SN-PHONE-001')).toBeVisible();

    // Krok 9: Wróć do przydziałów
    await page.goto('/assignments');
    await expect(page.getByRole('heading', { name: 'Przydziały' })).toBeVisible();

    // Krok 10: Zakończ przydział
    const assignmentRow = page.locator('table tbody tr').filter({ hasText: 'iPhone 14' });
    await assignmentRow.getByRole('button', { name: /zakończ/i }).click();
    const today = new Date().toISOString().split('T')[0];
    await page.getByLabel(/data zakończenia/i).fill(today);
    await page.getByRole('button', { name: /zakończ przydział/i }).click();
    await page.waitForTimeout(1000);

    // Krok 11: Potwierdź zmianę statusu na "Zakończony"
    await page.getByRole('radio', { name: /zakończone/i }).click();
    await page.waitForTimeout(500);
    const endedRow = page.locator('table tbody tr').filter({ hasText: 'iPhone 14' });
    await expect(endedRow).toBeVisible();
    await expect(endedRow.getByText(/zakończon/i)).toBeVisible();

    // Krok 12-13: Przejdź do zasobów i zaznacz filtr "Nieprzypisane"
    await page.goto('/assets');
    await expect(page.getByRole('heading', { name: 'Zasoby' })).toBeVisible();
    await page.getByRole('radio', { name: /^nieprzypisane$/i }).click();
    await page.waitForTimeout(500);

    // Krok 14: Potwierdź obecność zwolnionego zasobu (iPhone 14)
    await expect(page.getByText('SN-PHONE-001')).toBeVisible();
  });
});

import { test, expect } from '../fixtures/auth.fixture';

test.describe('Zarządzanie pracownikami (Admin)', () => {
  test('Pełny cykl zarządzania pracownikiem: wyświetlenie listy i dodanie', async ({ adminPage: page }) => {
    const uniqueEmail = `e2e.user.${Date.now()}@test.com`;

    // Krok 1-2: Przejdź do strony pracowników
    await page.goto('/employees');
    await expect(page.getByRole('heading', { name: 'Pracownicy' })).toBeVisible();
    await expect(page.locator('table')).toBeVisible();
    await expect(page.getByRole('button', { name: /dodaj pracownika/i })).toBeVisible();

    // Krok 3: Otwórz modal dodawania pracownika
    await page.getByRole('button', { name: /dodaj pracownika/i }).click();

    // Krok 4: Wypełnij formularz
    await page.getByLabel(/imię i nazwisko/i).fill('Nowy Pracownik E2E');
    await page.getByLabel('Email').fill(uniqueEmail);
    await page.getByLabel(/hasło/i).fill('testpass123');
    await page.getByLabel('Rola').click();
    await page.getByRole('option', { name: /pracownik/i }).click();
    await page.getByLabel(/data zatrudnienia od/i).fill('2024-01-01');

    // Krok 5: Zatwierdź dodanie pracownika
    await page.getByRole('button', { name: /^dodaj$/i }).click();

    // Krok 6: Weryfikacja obecności nowego pracownika
    await expect(page.getByRole('cell', { name: uniqueEmail })).toBeVisible({ timeout: 5000 });
  });
});

# Dokument wymagań produktu (PRD) - AssetManagementSystem

## 1. Przegląd produktu

AssetManagementSystem to aplikacja webowa zaprojektowana do zarządzania zasobami firmowymi oraz ich przydziałami pracownikom. System umożliwia śledzenie sprzętu takiego jak laptopy, smartfony, tablety, drukarki i słuchawki, a także prowadzenie pełnej historii przydziałów zasobów do pracowników.

Aplikacja zapewnia dwupoziomową kontrolę dostępu:
- Administrator - pełen dostęp do zarządzania pracownikami, zasobami i przydziałami
- Pracownik - dostęp wyłącznie do własnych zasobów i historii przydziałów

System został zaprojektowany jako MVP (Minimum Viable Product) koncentrujący się na podstawowych funkcjonalnościach zarządzania zasobami bez rozbudowanych integracji czy dodatkowych funkcji.

## 2. Problem użytkownika

Firmy zatrudniające pracowników i posiadające zasoby sprzętowe napotykają następujące wyzwania:

- Brak centralnego systemu do śledzenia, który pracownik używa konkretnego sprzętu
- Trudności w identyfikacji dostępnych zasobów do przydzielenia
- Brak historii przydziałów uniemożliwiający weryfikację, kto używał danego sprzętu w przeszłości
- Problemy z kontrolą dostępu - pracownicy nie powinni mieć wglądu w zasoby innych pracowników
- Chaos informacyjny przy przechodzeniu pracowników między stanowiskami lub przy ich odejściu
- Trudności w inwentaryzacji i zarządzaniu cyklem życia sprzętu

AssetManagementSystem rozwiązuje te problemy poprzez:
- Centralizację informacji o zasobach i pracownikach
- Automatyczne śledzenie przydziałów z datami początku i końca
- Pełną historię przydziałów dla każdego pracownika i zasobu
- Kontrolę dostępu zapewniającą prywatność i bezpieczeństwo danych
- Możliwość szybkiego sprawdzenia dostępności zasobów

## 3. Wymagania funkcjonalne

### 3.1 Zarządzanie pracownikami
- System musi umożliwiać administratorowi dodawanie nowych pracowników z danymi: imię i nazwisko, adres email, hasło, rola (ADMIN/EMPLOYEE), data zatrudnienia (od), data zatrudnienia (do - opcjonalna)
- System musi umożliwiać administratorowi wyświetlanie listy wszystkich pracowników
- System musi umożliwiać administratorowi wyświetlanie szczegółów pojedynczego pracownika po ID
- Każdy pracownik musi mieć unikalny identyfikator (ID)
- Każdy pracownik musi posiadać adres email i hasło do logowania
- Każdy pracownik musi mieć przypisaną rolę: ADMIN lub EMPLOYEE

### 3.2 Zarządzanie zasobami
- System musi umożliwiać administratorowi dodawanie nowych zasobów z danymi: typ zasobu, producent, model, numer seryjny
- System musi wspierać następujące typy zasobów: LAPTOP, SMARTPHONE, TABLET, PRINTER, HEADPHONES
- System musi umożliwiać administratorowi oznaczanie zasobów jako nieaktywne (wyłącznie tych, które nie są aktualnie przydzielone)
- System musi umożliwiać administratorowi wyświetlanie listy wszystkich zasobów
- Każdy zasób musi mieć unikalny identyfikator (ID)

### 3.3 Zarządzanie przydziałami
- System musi umożliwiać administratorowi tworzenie przydziału zasobu do pracownika z określeniem daty rozpoczęcia
- System musi umożliwiać administratorowi zakończenie przydziału z określeniem daty zakończenia
- System musi rejestrować wszystkie przydziały w historii z danymi: ID przydziału, ID zasobu, ID pracownika, data rozpoczęcia, data zakończenia, status aktywności
- System musi umożliwiać administratorowi wyświetlanie całej historii przydziałów
- System musi umożliwiać administratorowi filtrowanie historii przydziałów po konkretnym zasobie
- System musi umożliwiać administratorowi filtrowanie historii przydziałów po konkretnym pracowniku

### 3.4 Funkcjonalności pracownika
- System musi umożliwiać pracownikowi wyświetlanie wyłącznie swoich własnych aktywnych zasobów
- System musi umożliwiać pracownikowi wyświetlanie historii wszystkich swoich przydziałów (w tym zasobów już nieaktywnych)
- System musi zapewniać, że pracownik nie ma dostępu do danych innych pracowników

### 3.5 Uwierzytelnianie i autoryzacja
- System musi umożliwiać logowanie użytkowników przy użyciu adresu email i hasła
- System musi zwracać token JWT po pomyślnym zalogowaniu (ważność tokenu: 24 godziny)
- System musi rozróżniać role: Administrator i Pracownik
- System musi egzekwować uprawnienia dostępu zgodnie z rolą użytkownika
- System musi zapewniać bezpieczne przechowywanie haseł (algorytm BCrypt)
- System musi umożliwiać zmianę hasła przez użytkownika po podaniu aktualnego hasła
- System używa bezstanowej (stateless) autoryzacji opartej na tokenach JWT

### 3.6 Walidacja i reguły biznesowe
- System nie może pozwolić na oznaczenie zasobu jako nieaktywny, jeśli jest on aktualnie przydzielony pracownikowi
- System musi zapobiegać przydzieleniu tego samego zasobu wielu pracownikom jednocześnie
- System musi walidować wszystkie wymagane pola przy dodawaniu pracowników i zasobów

## 4. Granice produktu

### 4.1 Co NIE wchodzi w zakres MVP

- Rozbudowane informacje o pracownikach (np. działy, stanowiska, przełożeni)
- Rozbudowane informacje o zasobach (np. cena, data zakupu, stan techniczny)
- Aplikacje mobilne (iOS, Android) - tylko aplikacja webowa
- Hierarchia administratorów - wszyscy administratorzy mają równe uprawnienia
- Hierarchia pracowników i struktura organizacyjna
- Integracja z innymi systemami zarządzania firmą (HR, księgowość)
- Powiadomienia email lub push notifications
- Zarządzanie lokalizacją zasobów
- Procesy zatwierdzania przydziałów
- Rezerwacje zasobów na przyszłość
- Import/export danych z plików zewnętrznych
- Zaawansowane raportowanie i analityka

### 4.2 Techniczne ograniczenia

- Aplikacja działa wyłącznie w środowisku webowym
- Brak wsparcia dla trybu offline
- Brak API publicznego dla integracji zewnętrznych

## 5. Historyjki użytkowników

### US-001: Logowanie do systemu
Jako użytkownik (Administrator lub Pracownik)
Chcę zalogować się do systemu używając mojego adresu email i hasła
Aby uzyskać dostęp do funkcjonalności zgodnych z moją rolą

Kryteria akceptacji:
- Użytkownik może wprowadzić adres email i hasło na stronie logowania
- System weryfikuje poprawność danych logowania
- Po poprawnym zalogowaniu, użytkownik zostaje przekierowany do głównego widoku zgodnego z jego rolą
- Po niepoprawnym logowaniu, system wyświetla komunikat o błędzie
- Hasło jest maskowane podczas wprowadzania
- System nie ujawnia, czy błąd dotyczy emaila czy hasła (ze względów bezpieczeństwa)

### US-002: Wylogowanie z systemu
Jako zalogowany użytkownik
Chcę wylogować się z systemu
Aby zakończyć swoją sesję i zabezpieczyć dostęp do mojego konta

Kryteria akceptacji:
- Użytkownik może kliknąć przycisk wylogowania dostępny w interfejsie
- Po wylogowaniu, sesja użytkownika zostaje zakończona
- Użytkownik zostaje przekierowany do strony logowania
- Próba dostępu do chronionych zasobów po wylogowaniu wymaga ponownego logowania

### US-003: Dodawanie nowego pracownika przez administratora
Jako administrator
Chcę dodać nowego pracownika do systemu
Aby mógł on korzystać z systemu i otrzymywać przydziały zasobów

Kryteria akceptacji:
- Administrator ma dostęp do formularza dodawania pracownika
- Formularz zawiera pola: imię i nazwisko, adres email, hasło, rola (ADMIN/EMPLOYEE), data zatrudnienia (od), data zatrudnienia (do - opcjonalna)
- System waliduje, że wszystkie wymagane pola są wypełnione
- System waliduje poprawność formatu adresu email
- System waliduje, że email nie jest już użyty przez innego pracownika
- Po zapisaniu, nowy pracownik pojawia się na liście pracowników
- Nowo utworzony pracownik może zalogować się używając podanych danych

### US-004: Wyświetlanie listy wszystkich pracowników przez administratora
Jako administrator
Chcę wyświetlić listę wszystkich pracowników
Aby mieć przegląd zatrudnionych osób w firmie

Kryteria akceptacji:
- Administrator ma dostęp do widoku listy pracowników
- Lista zawiera: ID, imię i nazwisko, adres email, rola, datę zatrudnienia (od), datę zatrudnienia (do)
- Lista wyświetla wszystkich pracowników w systemie
- Dane są czytelnie sformatowane i uporządkowane

### US-005: Dodawanie nowego zasobu przez administratora
Jako administrator
Chcę dodać nowy zasób do systemu
Aby móc go przydzielić pracownikom

Kryteria akceptacji:
- Administrator ma dostęp do formularza dodawania zasobu
- Formularz zawiera pola: typ zasobu (LAPTOP, SMARTPHONE, TABLET, PRINTER, HEADPHONES), producent, model, numer seryjny
- System waliduje, że wszystkie wymagane pola są wypełnione
- System waliduje, że numer seryjny jest unikalny w systemie
- Po zapisaniu, nowy zasób pojawia się na liście zasobów ze statusem aktywny
- Nowy zasób jest dostępny do przydzielenia pracownikom

### US-006: Wyświetlanie listy wszystkich zasobów przez administratora
Jako administrator
Chcę wyświetlić listę wszystkich zasobów
Aby mieć przegląd dostępnego sprzętu w firmie

Kryteria akceptacji:
- Administrator ma dostęp do widoku listy zasobów
- Lista zawiera: ID, typ zasobu, producent, model, numer seryjny, status (aktywny/nieaktywny), informacje o aktualnie przypisanym pracowniku (ID, imię i nazwisko, email) jeśli zasób jest przypisany
- Lista wspiera paginację (numer strony, rozmiar strony) oraz sortowanie
- Lista wyświetla wszystkie zasoby w systemie
- Administrator może rozróżnić zasoby aktywne i nieaktywne
- Dane są czytelnie sformatowane i uporządkowane

### US-007: Oznaczanie zasobu jako nieaktywny przez administratora
Jako administrator
Chcę oznaczyć zasób jako nieaktywny
Aby wskazać, że zasób nie jest już dostępny do użytku (np. zepsuty, sprzedany)

Kryteria akceptacji:
- Administrator ma możliwość oznaczenia zasobu jako nieaktywny z poziomu listy zasobów lub szczegółów zasobu
- System sprawdza, czy zasób nie jest obecnie przydzielony żadnemu pracownikowi
- Jeśli zasób jest przydzielony, system wyświetla komunikat błędu i nie pozwala na zmianę statusu
- Jeśli zasób nie jest przydzielony, system zmienia jego status na nieaktywny
- Nieaktywny zasób nadal jest widoczny na liście zasobów, ale oznaczony jako nieaktywny
- Nieaktywny zasób nie może być przydzielony pracownikowi

### US-008: Tworzenie przydziału zasobu do pracownika przez administratora
Jako administrator
Chcę przydzielić zasób konkretnemu pracownikowi
Aby pracownik mógł korzystać z tego sprzętu

Kryteria akceptacji:
- Administrator ma dostęp do formularza tworzenia przydziału
- Formularz umożliwia wybór pracownika z listy
- Formularz umożliwia wybór zasobu z listy dostępnych (aktywnych i nieprzydzielonych) zasobów
- Formularz umożliwia określenie daty rozpoczęcia przydziału
- System waliduje, że wybrany zasób nie jest obecnie przydzielony innemu pracownikowi
- Po zapisaniu, tworzony jest nowy rekord przydziału ze statusem aktywny
- Zasób staje się niedostępny do przydzielenia innym pracownikom
- Pracownik widzi nowy zasób na swojej liście aktywnych zasobów

### US-009: Kończenie przydziału zasobu przez administratora
Jako administrator
Chcę zakończyć przydzielenie zasobu pracownikowi
Aby zwolnić zasób i umożliwić jego przydzielenie innej osobie

Kryteria akceptacji:
- Administrator ma możliwość zakończenia aktywnego przydziału
- Administrator może określić datę zakończenia przydziału
- Po zakończeniu, status przydziału zmienia się na nieaktywny
- Zasób staje się dostępny do przydzielenia innym pracownikom
- Pracownik przestaje widzieć zasób na liście swoich aktywnych zasobów
- Przydzielenie jest nadal widoczne w historii zarówno dla pracownika jak i administratora

### US-010: Wyświetlanie całej historii przydziałów przez administratora
Jako administrator
Chcę wyświetlić całą historię przydziałów w systemie
Aby mieć pełny przegląd wszystkich przydziałów zasobów

Kryteria akceptacji:
- Administrator ma dostęp do widoku historii przydziałów
- Lista zawiera: ID przydziału, ID zasobu, dane zasobu (typ, producent, model, numer seryjny), ID pracownika, imię i nazwisko pracownika, data rozpoczęcia, data zakończenia, status (aktywny/nieaktywny)
- Lista wspiera paginację (numer strony, rozmiar strony) oraz sortowanie
- Lista wyświetla wszystkie przydziały w systemie (zarówno aktywne jak i zakończone)
- Dane są czytelnie sformatowane i uporządkowane chronologicznie
- Administrator może rozróżnić przydziały aktywne od zakończonych

### US-011: Filtrowanie historii przydziałów po zasobie przez administratora
Jako administrator
Chcę przefiltrować historię przydziałów po konkretnym zasobie
Aby zobaczyć wszystkich pracowników, którzy używali tego zasobu

Kryteria akceptacji:
- Widok historii przydziałów zawiera opcję filtrowania po ID zasobu
- Administrator może podać ID zasobu jako parametr filtru
- Po zastosowaniu filtru, lista pokazuje tylko przydziały dotyczące wybranego zasobu (bez paginacji)
- Widoczne są wszystkie przydziały wybranego zasobu (aktywne i zakończone)
- Administrator może wyczyścić filtr i wrócić do pełnej listy z paginacją

### US-012: Filtrowanie historii przydziałów po pracowniku przez administratora
Jako administrator
Chcę przefiltrować historię przydziałów po konkretnym pracowniku
Aby zobaczyć wszystkie zasoby, które były lub są przydzielone temu pracownikowi

Kryteria akceptacji:
- Widok historii przydziałów zawiera opcję filtrowania po ID pracownika
- Administrator może podać ID pracownika jako parametr filtru
- Po zastosowaniu filtru, lista pokazuje tylko przydziały dotyczące wybranego pracownika (bez paginacji)
- Widoczne są wszystkie przydziały wybranego pracownika (aktywne i zakończone)
- Administrator może wyczyścić filtr i wrócić do pełnej listy z paginacją

### US-013: Wyświetlanie swoich aktywnych zasobów przez pracownika
Jako pracownik
Chcę wyświetlić listę moich aktywnych zasobów
Aby wiedzieć, jaki sprzęt jest mi obecnie przydzielony

Kryteria akceptacji:
- Pracownik ma dostęp do widoku swoich aktywnych zasobów
- Lista zawiera wyłącznie zasoby aktualnie przydzielone do zalogowanego pracownika
- Lista zawiera: typ zasobu, producent, model, numer seryjny, data rozpoczęcia przydziału
- Lista nie zawiera zasobów przydzielonych innym pracownikom
- Jeśli pracownik nie ma żadnych aktywnych przydziałów, wyświetla się odpowiedni komunikat
- Dane są czytelnie sformatowane

### US-014: Wyświetlanie historii swoich przydziałów przez pracownika
Jako pracownik
Chcę wyświetlić historię wszystkich moich przydziałów
Aby zobaczyć, jakie zasoby były mi przydzielone w przeszłości

Kryteria akceptacji:
- Pracownik ma dostęp do widoku historii swoich przydziałów
- Lista zawiera wszystkie przydziały zalogowanego pracownika (aktywne i zakończone)
- Lista zawiera: ID przydziału, ID zasobu, typ zasobu, producent, model, numer seryjny, data rozpoczęcia przydziału, data zakończenia przydziału, status (aktywny/nieaktywny)
- Lista nie zawiera przydziałów innych pracowników
- Pracownik może zobaczyć dane zasobów, które posiadał w przeszłości, ale już nie posiada
- Dane są uporządkowane chronologicznie
- Pracownik może rozróżnić przydziały aktywne od zakończonych

### US-015: Zabezpieczenie dostępu do danych innych pracowników
Jako pracownik
Nie mogę zobaczyć danych innych pracowników ani ich zasobów
Aby system zapewniał prywatność i bezpieczeństwo danych

Kryteria akceptacji:
- Pracownik nie ma dostępu do listy innych pracowników
- Pracownik nie ma dostępu do zasobów przydzielonych innym pracownikom
- Pracownik nie ma dostępu do historii przydziałów innych pracowników
- Próba bezpośredniego dostępu do chronionego zasobu (np. przez URL) jest blokowana
- System wyświetla odpowiedni komunikat o braku uprawnień przy próbie nieautoryzowanego dostępu
- Pracownik widzi tylko te elementy interfejsu, do których ma uprawnienia

### US-016: Walidacja unikalności numeru seryjnego zasobu
Jako administrator
Chcę, aby system zapobiegał dodawaniu zasobów z duplikującymi się numerami seryjnymi
Aby zapewnić unikalność identyfikacji każdego zasobu

Kryteria akceptacji:
- System sprawdza unikalność numeru seryjnego podczas dodawania nowego zasobu
- Jeśli numer seryjny już istnieje w systemie, formularz wyświetla komunikat o błędzie
- Komunikat błędu jasno wskazuje, że numer seryjny jest już używany
- Zasób nie zostaje dodany do systemu, dopóki numer seryjny nie będzie unikalny

### US-017: Walidacja unikalności adresu email pracownika
Jako administrator
Chcę, aby system zapobiegał dodawaniu pracowników z duplikującymi się adresami email
Aby każdy pracownik miał unikalny login do systemu

Kryteria akceptacji:
- System sprawdza unikalność adresu email podczas dodawania nowego pracownika
- Jeśli adres email już istnieje w systemie, formularz wyświetla komunikat o błędzie
- Komunikat błędu jasno wskazuje, że adres email jest już używany
- Pracownik nie zostaje dodany do systemu, dopóki adres email nie będzie unikalny

### US-018: Zapobieganie jednoczesnym przydziałom tego samego zasobu
Jako administrator
Chcę, aby system zapobiegał przydzieleniu tego samego zasobu wielu pracownikom jednocześnie
Aby zapewnić, że jeden zasób fizyczny jest przydzielony maksymalnie jednej osobie

Kryteria akceptacji:
- System sprawdza, czy zasób ma aktywny przydział podczas tworzenia nowego przydziału
- Jeśli zasób jest już przydzielony, system wyświetla komunikat o błędzie
- Lista zasobów dostępnych do przydzielenia pokazuje tylko zasoby aktywne i nieprzydzielone
- Komunikat błędu informuje, któremu pracownikowi zasób jest obecnie przydzielony
- Nowy przydział nie zostaje utworzony, dopóki aktualny przydział nie zostanie zakończony

### US-019: Obsługa błędnych danych logowania
Jako użytkownik próbujący się zalogować z nieprawidłowymi danymi
Chcę otrzymać jasny komunikat o błędzie
Aby wiedzieć, że logowanie się nie powiodło i móc spróbować ponownie

Kryteria akceptacji:
- System wyświetla komunikat o błędzie po wprowadzeniu nieprawidłowego emaila lub hasła
- Komunikat jest ogólny i nie wskazuje, czy błąd dotyczy emaila czy hasła (bezpieczeństwo)
- Użytkownik pozostaje na stronie logowania z możliwością ponownej próby
- Pola formularza są wyczyszczone lub zachowują wprowadzone dane (zgodnie z dobrymi praktykami UX)
- Nie ma limitu prób logowania w MVP (ale komunikat może sugerować sprawdzenie danych)

### US-020: Wyświetlanie pustej listy gdy brak danych
Jako użytkownik przeglądający listy w systemie
Chcę zobaczyć odpowiedni komunikat gdy lista jest pusta
Aby wiedzieć, że system działa poprawnie, ale nie ma jeszcze danych do wyświetlenia

Kryteria akceptacji:
- Gdy lista pracowników jest pusta, wyświetla się komunikat "Brak pracowników w systemie"
- Gdy lista zasobów jest pusta, wyświetla się komunikat "Brak zasobów w systemie"
- Gdy lista przydziałów jest pusta, wyświetla się komunikat "Brak przydziałów w systemie"
- Gdy pracownik nie ma aktywnych zasobów, wyświetla się komunikat "Nie masz przydzielonych zasobów"
- Gdy pracownik nie ma historii przydziałów, wyświetla się komunikat "Brak historii przydziałów"
- Komunikaty są czytelne i pomocne dla użytkownika

## 6. Metryki sukcesu

### 6.1 Metryki funkcjonalności
- System poprawnie egzekwuje kontrolę dostępu - 100% prób dostępu pracownika do danych innych pracowników jest blokowanych
- Wszystkie obowiązkowe pola są walidowane - 0 błędów związanych z brakującymi danymi
- Brak duplikatów numerów seryjnych i adresów email w systemie
- Brak przypadków jednoczesnego przydzielenia tego samego zasobu wielu pracownikom

### 6.2 Metryki bezpieczeństwa
- 100% haseł jest bezpiecznie przechowywanych (zahashowanych)
- Każda sesja użytkownika wymaga uwierzytelnienia
- Brak nieautoryzowanego dostępu do danych pracowników lub administratora
- System poprawnie weryfikuje uprawnienia przy każdej operacji

### 6.3 Metryki użyteczności
- Administrator może dodać nowego pracownika w czasie krótszym niż 2 minuty
- Administrator może dodać nowy zasób w czasie krótszym niż 1 minuty
- Administrator może utworzyć przydział w czasie krótszym niż 1 minuty
- Pracownik może zobaczyć swoje aktywne zasoby w czasie krótszym niż 10 sekund od zalogowania
- Wszystkie operacje CRUD (Create, Read, Update, Delete) działają bez błędów

### 6.4 Metryki kompletności danych
- 100% przydziałów ma zarejestrowane daty rozpoczęcia
- 100% zakończonych przydziałów ma zarejestrowane daty zakończenia
- Historia przydziałów zawiera wszystkie dotychczasowe przydziały bez braków
- Każdy zasób i pracownik ma pełne wymagane dane

### 6.5 Metryki adopcji (post-launch)
- Liczba pracowników zarejestrowanych w systemie
- Liczba zasobów zarejestrowanych w systemie
- Liczba aktywnych przydziałów


### 6.6 Kryteria akceptacji systemu
- Administrator może zarządzać pracownikami (dodawanie, wyświetlanie)
- Administrator może zarządzać zasobami (dodawanie, dezaktywacja, wyświetlanie)
- Administrator może zarządzać przydziałami (tworzenie, kończenie, wyświetlanie z filtrowaniem)
- Pracownik może wyświetlać tylko swoje zasoby i historię
- Pracownik NIE MOŻE wyświetlać danych innych pracowników
- Wszystkie operacje są logowane w historii przydziałów
- System działa stabilnie bez krytycznych błędów

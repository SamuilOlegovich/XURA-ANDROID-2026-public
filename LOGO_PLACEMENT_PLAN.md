# План размещения логотипа XURA

**Статус:** Ожидает реализации  
**Ветка:** ui-redesign-2026  
**Дата:** 2026-06-12

---

## Исходная ситуация

Лого (`@layout/logo_xura` / `@drawable/ic_xura_logo`) сейчас присутствует **только** в одном файле:
- `activity_main.xml` — главный экран

Во всех остальных экранах лого отсутствует.

---

## Что такое `logo_xura.xml`

Файл `res/layout/logo_xura.xml` — это переиспользуемый include-компонент:

```xml
<include
    layout="@layout/logo_xura"
    android:id="@+id/logo_xura"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_marginStart="80dp"
    android:layout_marginEnd="80dp" />
```

Именно этот `<include>` нужно вставлять в каждый целевой layout. Не дублировать `ImageView` — использовать include.

---

## Правила размещения лого

- **Позиция:** всегда сверху экрана, над основным контентом
- **Ширина:** `0dp` с горизонтальными отступами `marginStart/End="80dp"`
- **Высота:** `wrap_content`
- **Отступ сверху:** `marginTop="32dp"` от края экрана (или `24dp` если экран компактный)
- **Отступ до контента:** `marginTop="24dp"` от лого до следующего элемента
- **Accessibility:** `importantForAccessibility="no"` (уже задано в logo_xura.xml)
- **Никогда** не добавлять лого на игровые экраны (рулетка, цвет, число)

---

## Этапы реализации

---

### Этап 1 — Splash Screen (высокий приоритет)

**Файл:** `res/layout/flasher.xml`  
**Activity:** `view/Flasher.java`  
**Причина:** Первое, что видит пользователь. Лого по центру экрана — стандарт splash-экранов.

**Что сделать:**
1. Открыть `flasher.xml`
2. Добавить `<include layout="@layout/logo_xura">` по центру экрана
3. Лого должно быть единственным крупным элементом — убрать всё лишнее с splash, если есть
4. Добавить constraint: центр по вертикали и горизонтали (`constraintTop/Bottom/Start/End toParent`)

---

### Этап 2 — Выбор языка (высокий приоритет)

**Файл:** `res/layout/select_language.xml`  
**Activity:** `view/SelectLanguage.java`  
**Причина:** Первый экран при первом запуске приложения. Пользователь ещё не знаком с брендом.

**Что сделать:**
1. Открыть `select_language.xml`
2. Добавить `<include layout="@layout/logo_xura">` как первый элемент, сверху экрана
3. Constraint: `constraintTop_toTopOf="parent"` с `marginTop="32dp"`
4. Кнопки выбора языка — ниже лого

---

### Этап 3 — Восстановить или создать кошелёк (высокий приоритет)

**Файл:** `res/layout/restore_or_create_new_wallet_page.xml`  
**Activity:** `view/RestoreOrCreateNewWallet.java`  
**Причина:** Ключевой экран выбора пути onboarding. Пользователь ещё не внутри приложения.

**Что сделать:**
1. Открыть `restore_or_create_new_wallet_page.xml`
2. Добавить `<include layout="@layout/logo_xura">` вверху
3. Constraint: `constraintTop_toTopOf="parent"` с `marginTop="32dp"`
4. Две кнопки (создать / восстановить) — ниже лого с `marginTop="32dp"`

---

### Этап 4 — Создание нового кошелька (высокий приоритет)

**Файл:** `res/layout/create_new_wallet.xml`  
**Activity:** `view/CreateNewWallet.java`  
**Причина:** Пользователь создаёт кошелёк впервые. Часть onboarding-флоу.

**Что сделать:**
1. Открыть `create_new_wallet.xml`
2. Добавить `<include layout="@layout/logo_xura">` вверху
3. Контент (seed-фраза, инструкции) — ниже лого

---

### Этап 5 — Проверка нового кошелька (высокий приоритет)

**Файл:** `res/layout/checking_new_wallet.xml`  
**Activity:** `view/CheckingNewWallet.java`  
**Причина:** Продолжение создания кошелька — один флоу, единый стиль.

**Что сделать:**
1. Открыть `checking_new_wallet.xml`
2. Добавить `<include layout="@layout/logo_xura">` вверху
3. Форма проверки seed-фразы — ниже лого

---

### Этап 6 — Восстановление кошелька (высокий приоритет)

**Файл:** `res/layout/restore_wallet_page.xml`  
**Activity:** `view/RestoreWallet.java`  
**Причина:** Стрессовый момент — пользователь восстанавливает доступ. Лого создаёт доверие.

**Что сделать:**
1. Открыть `restore_wallet_page.xml`
2. Добавить `<include layout="@layout/logo_xura">` вверху
3. Поле ввода seed-фразы — ниже лого

---

### Этап 7 — Установка пароля (первый запуск) (высокий приоритет)

**Файл:** `res/layout/set_password_for_app_page.xml`  
**Activity:** `view/SetAnAppPassword.java`  
**Причина:** Часть первичной настройки приложения. Пользователь ещё не закончил onboarding.

**Что сделать:**
1. Открыть `set_password_for_app_page.xml`
2. Добавить `<include layout="@layout/logo_xura">` вверху
3. Поле ввода пароля — ниже лого

---

### Этап 8 — Экран блокировки / ввод пароля (высокий приоритет)

**Файл:** `res/layout/enter_application_password.xml`  
**Activity:** `view/EnterApplicationPassword.java`  
**Причина:** Пользователь возвращается после паузы. Лого снимает вопрос "что за приложение".

**Что сделать:**
1. Открыть `enter_application_password.xml`
2. Добавить `<include layout="@layout/logo_xura">` вверху
3. Поле ввода пароля + кнопка биометрии — ниже лого
4. Отступ от лого до поля: `marginTop="40dp"` (больше, чем обычно — экран блокировки должен "дышать")

---

### Этап 9 — Реферальные экраны (средний приоритет)

#### 9а. Стать рефералом
**Файл:** `res/layout/become_referral.xml`  
**Activity:** `view/BecomeReferral.java`

#### 9б. Реферал
**Файл:** `res/layout/referral.xml`  
**Activity:** `view/Referral.java`

#### 9в. Ваш реферал
**Файл:** `res/layout/your_referral_page.xml`  
**Activity:** `view/YourReferral.java`

**Причина для всех трёх:** Реферальные экраны видят потенциально новые пользователи (через скриншоты, шеринг). Лого XURA усиливает бренд при вирусном распространении.

**Что сделать для каждого:**
1. Открыть файл layout
2. Добавить `<include layout="@layout/logo_xura">` вверху
3. Лого компактнее — `marginStart/End="100dp"` вместо `80dp`, если контента много
4. Основной контент — ниже

---

### Этап 10 — Экран победы (средний приоритет)

**Файл:** `res/layout/win_page.xml`  
**Activity:** `view/Win.java`  
**Причина:** Эмоциональный пик — пользователь только что выиграл. Брендовое закрепление в момент радости.

**Что сделать:**
1. Открыть `win_page.xml`
2. Добавить `<include layout="@layout/logo_xura">` вверху или как watermark
3. Вариант А (вверху): лого над суммой выигрыша, стандартный include
4. Вариант Б (watermark): лого с `alpha="0.15"` по центру экрана за контентом — более изощрённо
5. **Рекомендация: Вариант А** — проще и надёжнее читается

---

## Экраны, куда лого НЕ добавляем

| Экран | Файл | Причина |
|---|---|---|
| Рулетка | `roulette_game_page.xml` | Активный геймплей, не отвлекать |
| Угадай цвет | `guess_the_color_game_page.xml` | Активный геймплей |
| Угадай число | `guess_the_number_game_page.xml` | Активный геймплей |
| История транзакций | `transaction_history_page.xml` | Утилитарный экран |
| Настройки (повторные) | `settings_page.xml` | Пользователь уже "дома" |
| Смена пароля в настройках | `settings_set_password_for_app.xml` | Повторное действие, не onboarding |
| Отправка платежа | `send_payment_page.xml` | Рабочий инструмент |
| Получение платежа | `receive_payment.xml` | Рабочий инструмент |
| QR-сканер | `scan_code_page.xml` | Функциональный экран |
| Поражение | `lost.xml` | Не стоит брендировать негативный момент |
| Правила игр | `rules_of_the_game_*.xml` | Информационные, не onboarding |

---

## Порядок выполнения (приоритеты)

```
[P0] Этап 1  — flasher.xml              (splash — самое важное)
[P0] Этап 8  — enter_application_password.xml (блокировка — частое появление)
[P1] Этап 2  — select_language.xml      (первый экран при первом запуске)
[P1] Этап 3  — restore_or_create_new_wallet_page.xml
[P1] Этап 4  — create_new_wallet.xml
[P1] Этап 5  — checking_new_wallet.xml
[P1] Этап 6  — restore_wallet_page.xml
[P1] Этап 7  — set_password_for_app_page.xml
[P2] Этап 9  — become_referral / referral / your_referral_page
[P2] Этап 10 — win_page.xml
```

---

## Шаблон include для вставки

Вставлять сразу после открывающего тега корневого layout (первый дочерний элемент):

```xml
<!-- XURA Logo -->
<include
    layout="@layout/logo_xura"
    android:id="@+id/logo_xura"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_marginStart="80dp"
    android:layout_marginTop="32dp"
    android:layout_marginEnd="80dp"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

Следующий элемент после лого привязывать:
```xml
app:layout_constraintTop_toBottomOf="@+id/logo_xura"
android:layout_marginTop="24dp"
```

---

## Итого

| Приоритет | Кол-во экранов |
|---|---|
| P0 — критично | 2 |
| P1 — onboarding | 6 |
| P2 — бренд/шеринг | 4 |
| **Итого** | **12 экранов** |

Текущее состояние: лого только на главном экране (1 из 13).

# XURA — UI Redesign Report
> Ветка: `ui-redesign-2026` | Дата анализа: 2026-06-12
> Режим: Полный ревью (дизайн + код + реализация)

---

## 📊 Оценка текущего интерфейса

| Критерий | Оценка | Комментарий |
|----------|:------:|-------------|
| Визуальная иерархия | 5/10 | Логотип съедает 20–25% экрана на каждой странице. Между балансом и кнопками — пустая зона. Нет явных visual groups. |
| Типографика | 6/10 | Montserrat — хороший выбор. Но `9sp` для "YOUR BALANCE" на игровых экранах нарушает минимум читаемости MD3 (≥12sp). |
| Цвета и контраст | 7/10 | XURA-палитра стильная. Cyan на чёрном ~7:1 ✓. Проблема: `xura_text_muted` (#4DFFFFFF = 30% белого) — контраст 1.8:1, WCAG fail. |
| Отступы и сетка | 7/10 | Кратность 4dp соблюдена. Непоследовательность: логотип с `marginStart` 60/80/90/100dp на разных экранах — нет единого токена. |
| Компоненты | 3/10 | `TextView` вместо `Button`. `ListView` вместо `RecyclerView`. `EditText` вместо `TextInputLayout`. `Theme.AppCompat` вместо `Theme.Material3`. |
| Состояния | 2/10 | Нет ни одного `loading`, `empty`, `error` состояния в layout-файлах. Win/Lost есть — это хорошо. |
| Доступность | 4/10 | `TextView` без `android:clickable` — неправильная роль для TalkBack. Нет `contentDescription` на кнопках. `9sp` ниже нормы. |
| Навигация | 2/10 | Нет AppBar с Back, нет Bottom Navigation Bar, нет NavComponent. Пользователь не понимает структуру и не может вернуться назад. |

---

## ❌ Главные проблемы

### 🔴 P0 — Критические (ломают UX)

**1. Нет навигационной архитектуры**
Весь навигационный граф построен на кликах по `TextView`. Пользователь не видит:
- где он находится в приложении
- как вернуться назад (нет Back / AppBar)
- структуру разделов (Кошелёк / Игры / Настройки)

**2. `TextView` вместо `Button` на всех 31 layout**
Ломает: TalkBack (неправильная роль accessibility), keyboard navigation,
ripple area, elevation states, pressed state.

**3. `ListView` в transaction_history_page**
Устаревший компонент. Нет view recycling, нет `DiffUtil`,
плохая производительность на длинных списках.

**4. `xura_text_muted` (#4DFFFFFF) используется на UI-тексте**
Контраст 1.8:1 — WCAG fail. Нечитаемо для людей с нарушениями зрения.

---

### 🟡 P1 — Важные (ухудшают качество)

**5. Логотип на каждом экране занимает ~20–25% высоты**
При margin 90dp с каждой стороны логотип крошечный, а пустое
пространство вокруг огромное. Критично на маленьких телефонах.

**6. `EditText` без `TextInputLayout`**
Поля ввода не имеют floating label (метка исчезает при вводе),
нет error-состояния, нет иконок действий.
`scanQR` встроен отдельной ссылкой над полем — нестандартно.

**7. Hardcoded цвета в drawables**
`bg_button_primary.xml`, `bg_game_card_red.xml` содержат hardcoded HEX
вместо `@color/xura_*`. При смене палитры придётся менять и drawables.

**8. `Theme.AppCompat` вместо `Theme.Material3`**
Без Material3: нет Dynamic Color, нет MD3-компонентов,
нет современного elevation system.

---

### 🟢 P2 — Улучшения (повышают polish)

**9. Settings page — пустой экран**
Только 3 кнопки внизу, остальное пространство — пустое. Нет информации о версии,
о подключённом кошельке, о текущем аккаунте.

**10. Win page — тупиковый экран**
60sp "YOU WON!" — эмоционально правильно. Но нет кнопки "Сыграть ещё" или
"Вернуться" — экран не имеет выхода через UI.

**11. Roulette "COMING SOON" — неинформативно**
Показан как бледный tertiary text. Должна быть заблокированная карточка
с тизером / подпиской на уведомление.

---

## ✅ Что сделано хорошо

1. **XURA gradient** `(cyan → purple → gold)` — сильный бренд-айдентити, выглядит дорого
2. **Glass morphism** на secondary кнопках — модно, согласован с dark theme
3. **Montserrat** — правильный выбор для crypto/fintech
4. **Цветовая система с depth**: `xura_black` → `xura_surface` → `xura_card`
5. **Touch targets основных кнопок** — 56dp и 60dp ✓ (превышает минимум 48dp)
6. **Win page** — 60sp "YOU WON!" — эмоционально правильный celebrate UX
7. **`letterSpacing`** проставлен на всех текстах — это редкость и правильно
8. **Только тёмная тема** — для crypto/game это верное решение
9. **Биометрия + PBKDF2** — security продумана
10. **Звуки** — `button.mp3`, `win.mp3`, `lost.mp3` — sound UX присутствует

---

## 🎨 Предложение: Новый дизайн

### Концепция
**"Crypto Casino Premium"** — сохранить XURA gradient identity,
но перейти на полноценную архитектуру с Navigation Component,
Bottom Navigation Bar и Material 3.

**Стиль:** *Dark Fintech Immersive*
Минимализм крипто-кошелька (Phantom, MetaMask) +
казино-атмосфера через ambient glow и цветные акценты только в нужных местах.
Не менять палитру — менять структуру и компоненты.

### Цветовая схема (без изменений, только правильное использование)
```
background  : #000000  (xura_black)   — фон всех экранов
surface     : #08081A  (xura_surface) — карточки, нижняя навигация
card        : #0D0D20  (xura_card)    — вложенные карточки
primary     : #00D4FF  (xura_cyan)    — главный акцент, иконки
secondary   : #9020D0  (xura_purple)  — вторичный акцент
error       : #FF3060  (xura_error)   — ошибки
success     : #00FF88  (xura_success) — успех/выигрыш
onSurface   : #FFFFFF  (text_primary) — основной текст
onSurface60 : #99FFFFFF               — вторичный текст (было 80%, стало 60% = контраст ≥4.5:1)
```

### Типографика
```
Display  : Montserrat Bold   60sp  — Win/Lost hero
Headline : Montserrat Bold   28sp  — заголовки страниц
Title    : Montserrat Bold   22sp  — названия секций
Body     : Montserrat Regular 16sp — основной контент
Label    : Montserrat Medium  13sp — лейблы, капшены
Caption  : Montserrat Regular 12sp — минимум (заменить все 9sp, 10sp, 11sp)
```

### Ключевые изменения
1. Navigation Component + BottomNavigationView (3 вкладки: Wallet / Games / Settings)
2. `MaterialButton` вместо `TextView`-кнопок везде
3. `TextInputLayout` + `TextInputEditText` для всех полей ввода
4. `RecyclerView` + `ListAdapter` в истории транзакций
5. `MaterialToolbar` с Back кнопкой на вложенных экранах
6. Логотип — только на главном экране полный, на остальных — compact wordmark в toolbar
7. `MaterialCardView` для игровых карточек на Select Game
8. `CircularProgressIndicator` для loading состояний
9. `Snackbar` для ошибок вместо Toast
10. `Theme.Material3.DayNight.NoActionBar` как базовая тема

---

## 📐 Визуализация 1 — Главный экран (Кошелёк)

```
┌─────────────────────────────────┐  ← статус-бар (чёрный)
│  ≡  XURA                   [⚙] │  ← MaterialToolbar, cyan wordmark
├─────────────────────────────────┤
│                                 │
│   ░░░░░░░░░░░░░░░░░░░░░░░░░   │  ← фоновый ambient glow (cyan 5%)
│                                 │
│         YOUR BALANCE            │  ← 12sp, text_tertiary, uppercase
│                                 │
│       ╔═══════════════╗         │
│       ║  97.456782    ║         │  ← 40sp bold white (hero number)
│       ║     XRP       ║         │  ← 16sp cyan
│       ╚═══════════════╝         │
│                                 │
│   ≈ $58.23 USD  ▲ +2.3% 24h    │  ← 13sp, muted — USD эквивалент
│                                 │
│  ┌─────────────────────────┐   │
│  │                         │   │  ← MaterialCardView (xura_surface)
│  │  ↑ SEND     ↓ RECEIVE   │   │  ← 2 кнопки в ряд внутри карточки
│  │                         │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │  RECENT TRANSACTIONS    │   │  ← секция с заголовком
│  │  ─────────────────────  │   │
│  │  ↑ Sent  -5.0 XRP   🕐 │   │  ← строка транзакции
│  │  ↓ Recv  +2.1 XRP   🕐 │   │
│  │  ↑ Game  -1.0 XRP   🕐 │   │
│  │  [ See all history ]    │   │  ← текстовая ссылка
│  └─────────────────────────┘   │
│                                 │
├─────────────────────────────────┤
│  [💼 Wallet] [🎮 Games] [⚙️ Set]│  ← BottomNavigationView
└─────────────────────────────────┘
   xura_surface bg, cyan selected
```

---

## 📐 Визуализация 2 — Выбор игры (Games Tab)

```
┌─────────────────────────────────┐
│  XURA  /  Games            [?] │  ← Toolbar, ? = global rules
├─────────────────────────────────┤
│                                 │
│  SELECT GAME                    │  ← 28sp bold
│  Choose your battle             │  ← 13sp muted
│                                 │
│  ┌─────────────────────────┐   │
│  │ ░░░░░░░░░░░░░░░░░░░░░  │   │  ← gradient overlay (cyan→purple)
│  │                         │   │
│  │  🎨  GUESS THE COLOR    │   │  ← MaterialCardView, 96dp tall
│  │      Double your bet    │   │  ← subtitle muted
│  │                    [▶]  │   │  ← стрелка-иконка
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │ ░░░░░░░░░░░░░░░░░░░░░  │   │  ← gradient overlay (purple→gold)
│  │                         │   │
│  │  🔢  GUESS THE NUMBER   │   │  ← MaterialCardView, 96dp tall
│  │      Win ×100           │   │
│  │                    [▶]  │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │ ░░░░░░░░░░░░░░░░░░░░░  │   │  ← серый overlay (locked)
│  │                         │   │
│  │  🎰  ROULETTE      🔒  │   │  ← MaterialCardView, заблокирована
│  │      Coming soon...     │   │
│  │      [Notify me]        │   │  ← кнопка подписки
│  └─────────────────────────┘   │
│                                 │
├─────────────────────────────────┤
│  [💼 Wallet] [🎮 Games] [⚙️ Set]│
└─────────────────────────────────┘
```

---

## 📐 Визуализация 3 — Игра: Угадай цвет

```
┌─────────────────────────────────┐
│  ←  GUESS THE COLOR        [?] │  ← Toolbar + Back
├─────────────────────────────────┤
│                                 │
│  Balance: 97.456782 XRP         │  ← компактно, 14sp cyan, вверху
│                                 │
│  ┌─────────────────────────┐   │
│  │   💰 Place your bet     │   │  ← TextInputLayout OutlinedBox
│  │   [_________________]   │   │     с floating label
│  │        XRP              │   │     суффикс "XRP"
│  └─────────────────────────┘   │
│                                 │
│  Quick amounts:                 │
│  [1 XRP] [5 XRP] [10 XRP]      │  ← ChipGroup для быстрого выбора
│                                 │
│  ╔═════════════════════════╗   │
│  ║                         ║   │
│  ║   🔴   B E T   R E D    ║   │  ← MaterialCardView, gradient
│  ║      ×2 if correct      ║   │     pink→red, 96dp, ripple
│  ║                         ║   │
│  ╚═════════════════════════╝   │
│                                 │
│  ╔═════════════════════════╗   │
│  ║                         ║   │
│  ║   ⚫   B E T  B L A C K  ║   │  ← MaterialCardView, dark glass
│  ║      ×2 if correct      ║   │     xura_surface, border glow
│  ║                         ║   │
│  ╚═════════════════════════╝   │
│                                 │
├─────────────────────────────────┤
│  [💼 Wallet] [🎮 Games] [⚙️ Set]│
└─────────────────────────────────┘
```

---

## 📐 Визуализация 4 — WIN страница

```
┌─────────────────────────────────┐
│              (нет toolbar)      │  ← fullscreen celebrate
├─────────────────────────────────┤
│                                 │
│  ✦  ✦  ✦  ✦  ✦  ✦  ✦  ✦     │  ← particle / confetti эффект
│                                 │
│                                 │
│      🏆  YOU WON!  🏆          │  ← 60sp Bold, white
│                                 │
│   ╔═══════════════════════╗    │
│   ║  + 194.913564  XRP    ║    │  ← cyan glow, 32sp Bold
│   ╚═══════════════════════╝    │
│                                 │
│      CONGRATULATIONS!           │  ← 20sp, cyan
│                                 │
│   New balance: 292.37 XRP       │  ← 14sp, muted
│                                 │
│                                 │
│                                 │
│  ╔═════════════════════════╗   │
│  ║     PLAY AGAIN          ║   │  ← primary gradient button
│  ╚═════════════════════════╝   │
│                                 │
│  ╔═════════════════════════╗   │
│  ║     BACK TO GAMES       ║   │  ← secondary glass button
│  ╚═════════════════════════╝   │
│                                 │
└─────────────────────────────────┘
```

---

## 📐 Визуализация 5 — Send Payment

```
┌─────────────────────────────────┐
│  ←  SEND PAYMENT               │  ← Toolbar + Back
├─────────────────────────────────┤
│                                 │
│  Available: 97.456782 XRP       │  ← 14sp, компактно
│                                 │
│  ┌────────────────────────┐    │
│  │  To (XRP Address)  [📷]│    │  ← TextInputLayout
│  │  [____________________]│    │    trailing icon = scan QR
│  └────────────────────────┘    │  ← один элемент вместо двух отдельных
│                                 │
│  ┌────────────────────────┐    │
│  │  Amount            XRP │    │  ← TextInputLayout
│  │  [____________________]│    │    suffix = "XRP"
│  └────────────────────────┘    │
│                                 │
│  ┌────────────────────────┐    │
│  │  Destination Tag (opt) │    │  ← TextInputLayout, меньший размер
│  │  [____________________]│    │
│  └────────────────────────┘    │
│                                 │
│  ─────────────────────────      │
│  Network fee: ~0.00001 XRP      │  ← информация о комиссии
│  ─────────────────────────      │
│                                 │
│                                 │
│  ╔═════════════════════════╗   │
│  ║        SEND             ║   │  ← MaterialButton, gradient
│  ╚═════════════════════════╝   │
│                                 │
└─────────────────────────────────┘
```

---

## 💻 Реализация — Код изменений

### Шаг 1: Тема — `values/themes.xml`

```xml
<!-- БЫЛО: -->
<style name="Theme.XGW" parent="Theme.AppCompat.NoActionBar">

<!-- СТАЛО: -->
<style name="Theme.XGW" parent="Theme.Material3.DayNight.NoActionBar">
    <item name="colorPrimary">@color/xura_cyan</item>
    <item name="colorOnPrimary">@color/xura_black</item>
    <item name="colorSecondary">@color/xura_purple</item>
    <item name="colorSurface">@color/xura_surface</item>
    <item name="colorOnSurface">@color/xura_text_primary</item>
    <item name="colorError">@color/xura_error</item>
    <item name="android:colorBackground">@color/xura_black</item>
    <item name="android:statusBarColor">@color/xura_black</item>
    <item name="android:navigationBarColor">@color/xura_black</item>
    <item name="android:windowLightStatusBar" tools:targetApi="m">false</item>
</style>
```

### Шаг 2: `MaterialButton` вместо `TextView`

```xml
<!-- БЫЛО: -->
<TextView
    android:id="@+id/next_link"
    android:layout_height="56dp"
    android:background="@drawable/bg_button_primary"
    android:text="@string/send" />

<!-- СТАЛО: -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/next_link"
    android:layout_height="56dp"
    android:backgroundTint="@color/transparent"
    android:background="@drawable/bg_button_primary"
    android:text="@string/send"
    android:textAllCaps="false"
    app:cornerRadius="16dp"
    app:rippleColor="#40FFFFFF" />
```

### Шаг 3: `TextInputLayout` для полей ввода

```xml
<!-- БЫЛО: -->
<EditText
    android:id="@+id/from_field"
    android:background="@drawable/bg_input_xura"
    android:hint="@string/to" />

<!-- СТАЛО: -->
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/til_address"
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
    android:hint="@string/to"
    app:boxBackgroundColor="@color/xura_glass_fill"
    app:boxStrokeColor="@color/xura_glass_border"
    app:boxStrokeErrorColor="@color/xura_error"
    app:hintTextColor="@color/xura_text_tertiary"
    app:endIconMode="custom"
    app:endIconDrawable="@drawable/ic_qr_scan"
    app:endIconContentDescription="@string/scan_qr_code"
    android:layout_height="wrap_content">

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/from_field"
        android:inputType="textPersonName"
        android:textColor="@color/xura_text_primary"
        android:textColorHint="@color/xura_text_muted" />

</com.google.android.material.textfield.TextInputLayout>
```

### Шаг 4: `RecyclerView` в истории транзакций

```xml
<!-- БЫЛО: -->
<ListView
    android:id="@+id/list_of_history"
    android:stackFromBottom="true"
    android:transcriptMode="alwaysScroll" />

<!-- СТАЛО: -->
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/list_of_history"
    android:layout_width="0dp"
    android:layout_height="0dp"
    android:layout_marginStart="12dp"
    android:layout_marginTop="16dp"
    android:layout_marginEnd="12dp"
    android:layout_marginBottom="16dp"
    android:clipToPadding="false"
    android:paddingBottom="8dp"
    app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toBottomOf="@+id/transaction_history_text_view" />
```

### Шаг 5: Bottom Navigation — `activity_main.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/xura_black">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/xura_black"
        app:elevation="0dp">

        <com.google.android.material.appbar.MaterialToolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="@color/xura_black"
            app:title="XURA"
            app:titleTextColor="@color/xura_cyan"
            app:navigationIconTint="@color/xura_text_primary" />

    </com.google.android.material.appbar.AppBarLayout>

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/nav_host_fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginBottom="56dp"
        app:layout_behavior="@string/appbar_scrolling_view_behavior"
        app:defaultNavHost="true"
        app:navGraph="@navigation/nav_graph" />

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_nav"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:layout_gravity="bottom"
        android:background="@color/xura_surface"
        app:itemIconTint="@color/bottom_nav_selector"
        app:itemTextColor="@color/bottom_nav_selector"
        app:menu="@menu/bottom_nav_menu"
        app:labelVisibilityMode="labeled" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### Шаг 6: `bottom_nav_menu.xml` (новый файл `res/menu/`)

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/nav_wallet"
        android:icon="@drawable/ic_wallet"
        android:title="@string/nav_wallet" />
    <item
        android:id="@+id/nav_games"
        android:icon="@drawable/ic_games"
        android:title="@string/nav_games" />
    <item
        android:id="@+id/nav_settings"
        android:icon="@drawable/ic_settings"
        android:title="@string/nav_settings" />
</menu>
```

---

## 🗺️ Дорожная карта — Приоритеты

| Приоритет | Задача | Что меняем | Оценка |
|:---------:|--------|------------|:------:|
| ✅ P0 | **Bottom Nav + Navigation Component** | BaseActivity + 3 layouts + 3 Activities | DONE |
| ✅ P0 | **`TextView` → `MaterialButton` везде** | все 18 layout с кнопками | DONE |
| ✅ P0 | **Исправить контраст `text_muted`** | colors.xml: #4D→#80FFFFFF (1.8→5.3:1) | DONE |
| ✅ P1 | **`EditText` → `TextInputLayout`** | send, receive, game, password экраны | DONE |
| ✅ P1 | **`ListView` → `RecyclerView`** | transaction_history_page.xml + adapter | DONE |
| ✅ P1 | **`Theme.Material3`** | themes.xml | DONE |
| ✅ P1 | **Логотип только на главном** | убрать из 28 из 31 layout | DONE |
| ✅ P2 | **Loading/Error состояния** | все экраны | DONE |
| ✅ P2 | **Win page — кнопки навигации** | win_page.xml | DONE |
| ✅ P2 | **Roulette — карточка с тизером** | roulette_game_page.xml | DONE |
| ✅ P2 | **ChipGroup для быстрых ставок** | guess_the_color, guess_the_number | DONE |
| ✅ P2 | **Settings — добавить контент** | settings_page.xml | DONE |

---

## 📝 Как работать с этим файлом

```
# Начать задачу по пункту из дорожной карты:
"Возьми P0 задачу №1 — сделай Bottom Nav + Navigation Component"

# Работа с конкретным экраном:
"Переделай send_payment_page.xml по стандартам из этого отчёта"

# Быстрая оценка нового экрана:
"Вот новый экран [код/скриншот]. Оцени по таблице из отчёта."

# Проверить прогресс:
"Какие пункты из дорожной карты уже выполнены?"
```

---

*Файл создан: 2026-06-12 | Ветка: `ui-redesign-2026`*
*Обновлять этот файл при каждом завершении пункта из дорожной карты.*

# XURA — Полная спецификация дизайна и UI

> Документ описывает каждый экран приложения: все элементы, тексты, цвета, размеры, поведение кнопок.  
> Версия приложения: **26.7.1** · Шрифт везде: **Montserrat** · Фон всех экранов: **#000000**

---

## Содержание

1. [Цветовая система](#1-цветовая-система)
2. [Универсальные компоненты](#2-универсальные-компоненты)
3. [Логотип XURA](#3-логотип-xura)
4. [Нижняя навигация](#4-нижняя-навигация)
5. [Сплэш-экран](#5-сплэш-экран)
6. [Онбординг (4 страницы)](#6-онбординг-4-страницы)
7. [Создать или восстановить кошелёк](#7-создать-или-восстановить-кошелёк)
8. [Создание нового кошелька — показ seed](#8-создание-нового-кошелька--показ-seed)
9. [Проверка seed-фразы](#9-проверка-seed-фразы)
10. [Восстановление кошелька](#10-восстановление-кошелька)
11. [Установка пароля](#11-установка-пароля)
12. [Ввод пароля (блокировка)](#12-ввод-пароля-блокировка)
13. [Смена пароля (настройки)](#13-смена-пароля-настройки)
14. [Главный экран — кошелёк](#14-главный-экран--кошелёк)
15. [Отправка XRP](#15-отправка-xrp)
16. [Получение XRP (QR)](#16-получение-xrp-qr)
17. [Сканирование QR](#17-сканирование-qr)
18. [История транзакций](#18-история-транзакций)
19. [Детали транзакции (BottomSheet)](#19-детали-транзакции-bottomsheet)
20. [Выбор игры](#20-выбор-игры)
21. [Игра — Угадай цвет](#21-игра--угадай-цвет)
22. [Игра — Угадай число](#22-игра--угадай-число)
23. [Игра — Рулетка](#23-игра--рулетка)
24. [Flasher — ожидание результата](#24-flasher--ожидание-результата)
25. [Экран победы](#25-экран-победы)
26. [Экран проигрыша](#26-экран-проигрыша)
27. [Правила — Угадай цвет](#27-правила--угадай-цвет)
28. [Правила — Угадай число](#28-правила--угадай-число)
29. [Правила — Рулетка](#29-правила--рулетка)
30. [Настройки](#30-настройки)
31. [Выбор языка](#31-выбор-языка)
32. [Реферальный раздел](#32-реферальный-раздел)
33. [Стать рефералом](#33-стать-рефералом)
34. [Ваш реферальный код](#34-ваш-реферальный-код)
35. [Информация о реферальной программе](#35-информация-о-реферальной-программе)
36. [О приложении](#36-о-приложении)

---

## 1. Цветовая система

| Переменная | HEX | Использование |
|---|---|---|
| `xura_black` | `#000000` | Фон всех экранов |
| `xura_surface` | `#08081A` | BottomSheet, поверхность |
| `xura_card` | `#0D0D20` | Тёмный фон карточек |
| `xura_cyan` | `#00D4FF` | Получение, кнопки действий, акцент |
| `xura_blue` | `#4080FF` | Вторичный акцент |
| `xura_indigo` | `#6040FF` | Градиент |
| `xura_purple` | `#9020D0` | DEV-панель, кнопка «продолжить» |
| `xura_magenta` | `#D020A0` | Градиент |
| `xura_pink` | `#FF2080` | Отправка, иконка X (проигрыш) |
| `xura_orange` | `#FF5020` | Градиент |
| `xura_gold` | `#FFB000` | Победа, история, кнопки К Играм |
| `xura_text_primary` | `#FFFFFF` | Основной текст |
| `xura_text_secondary` | `#CCFFFFFF` | Вторичный текст (80% белый) |
| `xura_text_tertiary` | `#80FFFFFF` | Подписи, мелкий текст (50% белый) |
| `xura_text_muted` | `#80FFFFFF` | Приглушённый текст |
| `xura_glass_fill` | `#1AFFFFFF` | Фон glass-карточек (10% белый) |
| `xura_glass_border` | `#33FFFFFF` | Рамка glass-карточек (20% белый) |
| `xura_success` | `#00FF88` | Успешные операции |
| `xura_error` | `#FF3060` | Ошибки |
| `xura_warning` | `#FFB000` | Предупреждения (= gold) |

### XURA Signature Gradient
Направление: слева направо  
Цвета: `#00D4FF` → `#4080FF` → `#6040FF` → `#9020D0` → `#D020A0` → `#FF2080` → `#FF5020` → `#FFB000`  
Используется: логотип X, заголовок «ПОБЕДА»

---

## 2. Универсальные компоненты

### Кнопка-карточка (стандарт)
Используется на большинстве экранов как основной интерактивный элемент.

| Параметр | Значение |
|---|---|
| Высота | 80dp |
| Горизонтальный margin | 24dp с каждой стороны |
| Внутренний padding | 16dp слева, 16dp справа |
| Радиус скругления | 20dp |
| Заголовок | 15sp, bold, letterSpacing 0.03 |
| Подпись | 11sp, regular, letterSpacing 0.02, цвет `xura_text_tertiary` |
| Иконка | 36×36dp, справа, marginEnd 16dp |
| Вертикальная цепочка | packed (заголовок + подпись по центру) |

**Стили рамки по цвету:**
| Drawable | Цвет рамки | Цвет заголовка | Применение |
|---|---|---|---|
| `bg_card_action_primary` | `#00D4FF` (cyan) | cyan | Главное действие, биометрия ON |
| `bg_card_gold` | `#FFB000` (gold) | gold | Кнопки «К играм», победа |
| `bg_card_send` | `#FF2080` (pink) | pink | Отправка XRP, пароль |
| `bg_card_request` | `#00D4FF` (cyan) | cyan | Получение XRP |
| `bg_card_history` | `#FFB000` (gold) | gold | История транзакций |
| `bg_card_glass_clickable` | `#33FFFFFF` (glass) | white | Настройки, вторичные кнопки |
| `bg_card_purple` | `#9020D0` (purple) | purple | «Continue Browsing» на Flasher |
| `bg_card_action_error` | `#FF3060` (error) | error | Ошибки (создан в этой версии) |

### Glass-карточка (не кликабельная)
Фон: `#1AFFFFFF`, рамка: `#33FFFFFF`, радиус: 20dp  
Используется для отображения информации (сообщения на экранах Win/Lost убраны в v26.7.1)

---

## 3. Логотип XURA

Файл: `logo_xura.xml` — включается через `<include>` на многих экранах.

| Элемент | Описание |
|---|---|
| Иконка X | `ic_xura_logo`, signature gradient, занимает верхнюю часть блока |
| Текст «XURA» | Montserrat Bold, белый `#FFFFFF`, letterSpacing ~0.16 |
| Расположение | По центру, отступ от краёв 80dp с каждой стороны |
| Кликабельность | На MainActivity — кликабелен → SelectGame |

---

## 4. Нижняя навигация

Присутствует на: MainActivity, SelectGame, Settings и всех игровых экранах.

| Вкладка | Иконка | Действие |
|---|---|---|
| 1 | `ic_nav_wallet` | → MainActivity (кошелёк) |
| 2 | `ic_nav_games` | → SelectGame (игры) |
| 3 | `ic_nav_settings` | → Settings (настройки) |

- Подписей нет (`labelVisibilityMode="unlabeled"`)
- Фон: `bg_bottom_nav` — pill-форма, тёмный
- Расположена внизу экрана поверх контента

---

## 5. Сплэш-экран

**Файл:** `splash_screen.xml`  
**Activity:** `SplashActivity`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` (#000000) |
| Логотип | `ic_xura_logo` по центру экрана |
| Анимация | scaleX/scaleY: 0→1, alpha: 0→1, длительность 850ms, OvershootInterpolator(1.3) |
| Пауза | 1800ms после анимации |
| Переход | → логика определения следующего экрана (онбординг / пароль / кошелёк / главная) |

---

## 6. Онбординг (4 страницы)

**Файл:** `activity_onboarding.xml` + `item_onboarding_page.xml`  
**Activity:** `OnboardingActivity`  
**Показывается:** только при первом запуске

**Структура экрана:**
- ViewPager2 с 4 страницами
- Точки-индикаторы внизу (`ic_dot_active` / `ic_dot_inactive`)
- Кнопка «SKIP» — пропустить онбординг → следующий экран
- Кнопка «NEXT» (на 1–3 странице) / «GET STARTED» (на 4-й)

**Каждая страница (`item_onboarding_page.xml`):**
- Иллюстрация (ImageView) по центру
- Заголовок: Montserrat Bold, белый, крупный
- Описание: Montserrat, `xura_text_secondary`, средний размер

| Страница | Заголовок | Описание |
|---|---|---|
| 1 | Welcome to XURA | Non-custodial XRP wallet + blockchain gaming |
| 2 | Send & Receive | Send XRP to any address, receive via QR |
| 3 | Play & Win XRP | Blockchain-powered games with real XRP |
| 4 | Secure by Design | Your keys, your funds — always |

---

## 7. Создать или восстановить кошелёк

**Файл:** `restore_or_create_new_wallet_page.xml`  
**Activity:** `RestoreOrCreateNewWallet`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Логотип XURA | вверху по центру |
| Заголовок | «XURA WALLET», Montserrat Bold, белый |
| Кнопка 1 | **«СОЗДАТЬ НОВЫЙ КОШЕЛЁК»** · стиль `bg_card_action_primary` (cyan) · иконка `ic_add_wallet` · подпись: «Сгенерировать новый XRP-адрес» → CreateNewWallet |
| Кнопка 2 | **«ВОССТАНОВИТЬ КОШЕЛЁК»** · стиль `bg_card_glass_clickable` (glass) · иконка `ic_restore` · подпись: «Ввести существующую seed-фразу» → RestoreWallet |
| Отступ между кнопками | 12dp |
| Кнопки внизу экрана | marginBottom 32dp |

---

## 8. Создание нового кошелька — показ seed

**Файл:** `create_new_wallet.xml`  
**Activity:** `CreateNewWallet`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «ВАША SEED-ФРАЗА», Montserrat Bold 20sp, белый, по центру, marginTop 32dp |
| Предупреждение | Текст: «Запишите эти слова в правильном порядке. Без них невозможно восстановить кошелёк.» · 14sp · `xura_text_secondary` · по центру · padding 16dp |
| Блок seed | MaterialCardView, фон `xura_card` (#0D0D20), радиус 16dp, padding 16dp · 24 слова в виде пронумерованного списка · Montserrat 14sp, белый |
| Кнопка «Копировать» | Иконка `ic_copy`, рядом с блоком seed · при нажатии — копирует seed + диалог предупреждения об автоочистке буфера |
| Кнопка «ПРОДОЛЖИТЬ» | `bg_card_action_primary` (cyan) · «Я записал seed-фразу» · → CheckingNewWallet |
| Безопасность | FLAG_SECURE активен — скриншоты заблокированы |

---

## 9. Проверка seed-фразы

**Файл:** `checking_new_wallet.xml`  
**Activity:** `CheckingNewWallet`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «ПОДТВЕРДИТЕ SEED-ФРАЗУ», Montserrat Bold 20sp |
| Инструкция | «Введите слова в том же порядке», 14sp, `xura_text_secondary` |
| Поля ввода | 24 поля (по одному на каждое слово) · TextInputLayout · фон `xura_card` · рамка cyan при фокусе · Montserrat 14sp |
| Индикатор прогресса | Линейный, cyan, вверху |
| Кнопка «ПОДТВЕРДИТЬ» | `bg_card_action_primary` (cyan) · активна только когда все поля заполнены → если верно: SetAnAppPassword |
| При ошибке | Подсветка неверных полей красным (`xura_error`), сообщение об ошибке |

---

## 10. Восстановление кошелька

**Файл:** `restore_wallet_page.xml`  
**Activity:** `RestoreWallet`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Логотип XURA | вверху |
| Заголовок | «ВОССТАНОВЛЕНИЕ КОШЕЛЬКА», Montserrat Bold 20sp |
| Поле ввода | Большое многострочное поле · placeholder: «Введите seed-фразу (слова через пробел)» · Montserrat 14sp · фон `xura_card` · рамка cyan при фокусе |
| Кнопка «ВОССТАНОВИТЬ» | `bg_card_action_primary` (cyan) · активна при наличии текста |
| При ошибке | Toast или SnackBar: «Неверная seed-фраза» |

---

## 11. Установка пароля

**Файл:** `set_password_for_app_page.xml`  
**Activity:** `SetAnAppPassword`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Иконка замка | `ic_lock`, 64dp, `xura_cyan`, по центру вверху, marginTop 48dp |
| Заголовок | «УСТАНОВИТЕ ПАРОЛЬ», Montserrat Bold 24sp, белый, по центру |
| Подзаголовок | «Пароль защищает доступ к приложению», 14sp, `xura_text_secondary` |
| Поле «Пароль» | TextInputLayout · тип password · иконка показать/скрыть · рамка cyan · Montserrat 14sp |
| Поле «Повторите» | Аналогично первому |
| Отступ между полями | 16dp |
| Кнопка «УСТАНОВИТЬ» | `bg_card_action_primary` (cyan) · активна когда оба поля совпадают и не пусты |
| Безопасность | FLAG_SECURE |

---

## 12. Ввод пароля (блокировка)

**Файл:** `enter_application_password.xml`  
**Activity:** `EnterApplicationPassword`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Иконка | `ic_lock` или `ic_face_scan`, 64dp, cyan, по центру |
| Заголовок | «ВВЕДИТЕ ПАРОЛЬ», Montserrat Bold 24sp |
| Поле ввода | `enter_application_password_field` · тип password · рамка cyan |
| Биометрия | Предлагается автоматически при onResume (если включена в настройках) · `BiometricPrompt` с кнопкой «USE PASSWORD» |
| Кнопка «ВОЙТИ» | `bg_card_action_primary` (cyan) |
| Кнопка «назад» | **Полностью заблокирована** (OnBackPressedCallback) — нельзя уйти без ввода пароля |
| Безопасность | FLAG_SECURE |

---

## 13. Смена пароля (настройки)

**Файл:** `settings_set_password_for_app.xml`  
**Activity:** `SettingsSetPasswordForApp`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «СМЕНА ПАРОЛЯ», Montserrat Bold 24sp |
| Поле «Текущий пароль» | TextInputLayout, тип password |
| Поле «Новый пароль» | TextInputLayout, тип password |
| Поле «Подтвердить новый» | TextInputLayout, тип password |
| Кнопка «СОХРАНИТЬ» | `bg_card_action_primary` (cyan) |

---

## 14. Главный экран — кошелёк

**Файл:** `activity_main.xml`  
**Activity:** `MainActivity`

Структура сверху вниз:

| Элемент | id | Описание |
|---|---|---|
| Логотип XURA | `logo_xura` | include logo_xura.xml · кликабелен → SelectGame · marginTop 32dp, horizontal margin 80dp |
| Метка баланса | — | «ВАШ БАЛАНС», 11sp, `xura_text_tertiary`, letterSpacing 0.16, caps, по центру |
| Бейдж TESTNET | `tv_testnet_badge` | Виден только в testnet · фон `bg_chip_testnet` (purple) · текст «TESTNET» · 11sp |
| Спиннер загрузки | — | `CircularProgressIndicator`, 44dp, cyan · виден пока загружается баланс |
| Баланс | `balance_linc` | Montserrat Bold 40sp · белый · по центру · Pull-to-refresh обновляет |
| Карточка ОТПРАВИТЬ | `btn_send` | 80dp · `bg_card_send` (pink border) · заголовок: «ОТПРАВИТЬ» pink · подпись: «Перевести XRP на любой адрес» · иконка: `ic_send` pink → SendPayment |
| Карточка ПОЛУЧИТЬ | `btn_receive` | 80dp · `bg_card_request` (cyan border) · заголовок: «ПОЛУЧИТЬ» cyan · подпись: «Получить XRP · Поделиться QR-кодом» · иконка: `ic_receive` cyan → ReceivePayment |
| Карточка ИСТОРИЯ | `btn_history` | 80dp · `bg_card_history` (gold border) · заголовок: «ИСТОРИЯ» gold · подпись: «Все ваши транзакции» · иконка: `ic_history` gold → TransactionHistory |
| Нижняя навигация | — | BottomNavigationView, pill-форма |
| Pull-to-refresh | — | SwipeRefreshLayout, цвет индикатора cyan |

**Расстояние между карточками:** 12dp  
**Карточки внизу:** marginBottom 32dp над навигацией

---

## 15. Отправка XRP

**Файл:** `send_payment_page.xml`  
**Activity:** `SendPayment`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «ОТПРАВИТЬ XRP», Montserrat Bold 24sp, белый |
| Поле «Адрес получателя» | TextInputLayout · placeholder: «XRP-адрес» · справа кнопка QR-сканера (`ic_qr_scan`) → ScanQrCode |
| Поле «Сумма» | TextInputLayout · тип decimal · placeholder: «0.000000 XRP» · справа метка «XRP» |
| Поле «Destination Tag» | TextInputLayout · тип number · placeholder: «Destination Tag (необязательно)» |
| Доступный баланс | Текст: «Доступно: X.XX XRP», 13sp, `xura_text_tertiary` |
| Кнопка «ОТПРАВИТЬ» | `bg_card_send` (pink border) · заголовок: «ОТПРАВИТЬ» pink · подпись: сумма и адрес · иконка `ic_send` pink |
| Диалог подтверждения | AlertDialog перед отправкой: адрес, сумма, тег · кнопки «ПОДТВЕРДИТЬ» / «ОТМЕНА» |

---

## 16. Получение XRP (QR)

**Файл:** `receive_payment.xml`  
**Activity:** `ReceivePayment`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «ПОЛУЧИТЬ XRP», Montserrat Bold 24sp |
| QR-код | ImageView, ~240dp, по центру · генерируется из адреса кошелька (ZXing) · белый на чёрном фоне |
| Адрес | Montserrat 13sp, `xura_text_secondary`, по центру, под QR · полный XRP-адрес |
| Кнопка «СКОПИРОВАТЬ» | `bg_card_action_primary` (cyan) · иконка `ic_copy` · копирует адрес в буфер + диалог предупреждения |

---

## 17. Сканирование QR

**Файл:** `scan_code_page.xml`  
**Activity:** `ScanQrCode`

| Элемент | Описание |
|---|---|
| Фон | Полноэкранный Camera Preview (CameraX) |
| Рамка | Квадратная рамка-прицел по центру экрана |
| Инструкция | «Наведите камеру на QR-код», белый текст внизу |
| Результат | При успешном сканировании → возврат на SendPayment с заполненным адресом |

---

## 18. История транзакций

**Файл:** `transaction_history_page.xml`  
**Activity:** `TransactionHistory`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «ИСТОРИЯ ТРАНЗАКЦИЙ», Montserrat Bold 24sp |
| Нижняя навигация | присутствует |
| Список | RecyclerView · каждая строка: тип операции (иконка), сумма, дата, статус |
| Иконки типов | `ic_send` pink (исходящие) · `ic_receive` cyan (входящие) · `ic_game` gold (игровые) |
| Сумма | Montserrat Bold 15sp · исходящие: красный · входящие: зелёный (`xura_success`) |
| Дата | Montserrat 11sp, `xura_text_tertiary` |
| Нажатие на строку | → TxDetailSheet (BottomSheet) |
| Пустое состояние | Текст: «Транзакций пока нет», по центру, `xura_text_muted` |

---

## 19. Детали транзакции (BottomSheet)

**Файл:** `sheet_tx_detail.xml`

| Элемент | Описание |
|---|---|
| Фон | `xura_surface` (#08081A), радиус верхних углов 24dp |
| Хэндл | Серая полоска вверху 4dp × 40dp |
| Тип операции | Иконка + текст (ОТПРАВЛЕНО / ПОЛУЧЕНО / ИГРА), Montserrat Bold 18sp |
| Сумма | Montserrat Bold 32sp, цвет по типу |
| Адрес | «От:» / «Кому:» + адрес, Montserrat 13sp, `xura_text_secondary` |
| Дата и время | Montserrat 13sp, `xura_text_tertiary` |
| Хэш транзакции | Монospaced 12sp, `xura_text_tertiary` · кнопка копировать |
| Memo | Если есть — отображается декодированный текст, 12sp |
| Кнопка «EXPLORER» | glass-карточка · открывает XRPL Explorer в браузере |

---

## 20. Выбор игры

**Файл:** `select_game_page.xml`  
**Activity:** `SelectGame`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Логотип XURA | вверху, marginTop 32dp |
| Заголовок | «SELECT GAME», Montserrat Bold 24sp, белый, по центру |
| Бейдж режима | TRIAL (cyan, `bg_chip_trial`) или LIVE (gold, `bg_chip_live`) |
| Нижняя навигация | присутствует |
| Фоновая музыка | `flour_of_choice.mp3`, volume 0.5, loop |

**Три карточки игр (снизу вверх):**

| Карточка | Стиль фона | Цвет заголовка | Иконка | Подпись | Действие |
|---|---|---|---|---|---|
| «УГАДАЙ ЦВЕТ» | `bg_card_color_game` | `xura_pink` | две карты (красная + чёрная) | «Красное или Чёрное · ×2» | → GuessTheColorGame |
| «УГАДАЙ ЧИСЛО» | `bg_card_number_game` | `xura_cyan` | цифры + «?» | «Числа 1–36 · ×36» | → GuessTheNumberGame |
| «РУЛЕТКА» | `bg_card_roulette_game` | `xura_gold` | `ic_roulette_wheel` | «Европейская рулетка · ×2–36» | → RouletteGame |

**Анимация «волна»:** каждые 3.5 сек карточки последовательно подпрыгивают на 20dp (рулетка → число → цвет) с задержкой 100ms между ними.

---

## 21. Игра — Угадай цвет

**Файл:** `guess_the_color_game_page.xml`  
**Activity:** `GuessTheColorGame`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «УГАДАЙ ЦВЕТ», Montserrat Bold 24sp |
| Бейдж TRIAL/LIVE | вверху справа |
| Нижняя навигация | присутствует |
| Две кнопки выбора | **КРАСНОЕ** (фон красный, текст белый) и **ЧЁРНОЕ** (фон чёрный с рамкой, текст белый) · при выборе — подсвечиваются |
| Поле ставки | TextInputLayout «Сумма ставки XRP» · min 0.1, max 100 · стиль chips или slider (из настроек) |
| Chips-стиль | Быстрые кнопки: 0.1 / 0.5 / 1 / 5 / 10 XRP |
| Поле реферала | TextInputLayout «Реферальный код (необязательно)» · 13sp · `xura_text_tertiary` |
| Кнопка «ПОСТАВИТЬ» | `bg_card_color_game` (pink) · активна при выбранном цвете и сумме → Flasher |
| Иконка правил | кнопка «?» в AppBar → RulesOfTheGameGuessTheColor |

---

## 22. Игра — Угадай число

**Файл:** `guess_the_number_game_page.xml`  
**Activity:** `GuessTheNumberGame`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «УГАДАЙ ЧИСЛО», Montserrat Bold 24sp |
| Сетка чисел | Кнопки 1–36 в сетке (6 × 6) · каждая 48dp · при нажатии выделяется cyan |
| Поле ставки | TextInputLayout · chips или slider |
| Chips | 0.1 / 0.5 / 1 / 5 / 10 XRP |
| Реферал | поле ввода кода |
| Кнопка «ПОСТАВИТЬ» | `bg_card_number_game` (cyan) → Flasher |
| Иконка правил | «?» → RulesOfTheGameGuessTheNumber |

---

## 23. Игра — Рулетка

**Файл:** `roulette_game_page.xml`  
**Activity:** `RouletteGame`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «РУЛЕТКА», Montserrat Bold 24sp |
| Таблица ставок | Полная сетка европейской рулетки: числа 0–36 + внешние ставки |
| Числа 0–36 | В сетке · 0 = зелёный, красные и чёрные согласно стандарту |
| Внешние ставки | `r` (красные), `b` (чёрные), `o` (нечёт), `e` (чёт), `l` (1–18), `h` (19–36), `d1`/`d2`/`d3` (дюжины), `c1`/`c2`/`c3` (колонки) |
| При нажатии на ячейку | Появляется чип с суммой ставки |
| Поле суммы | TextInputLayout · сумма для одной ячейки |
| Итого | «Итого: X.XX XRP», `xura_text_secondary` |
| Кнопка «SPIN» | `bg_card_roulette_game` (gold) · активна при наличии хотя бы одной ставки → Flasher |
| Сброс ставок | Кнопка «✕» очищает все ставки |
| Правила | «?» → RulesOfTheGameRoulette |

---

## 24. Flasher — ожидание результата

**Файл:** `flasher.xml`  
**Activity:** `Flasher`

Экран появляется сразу после отправки ставки и остаётся до получения ответа от сервера.

| Элемент | id | Описание |
|---|---|---|
| Фон | — | `xura_black` |
| Заголовок результата | `last_text_view` | 48sp, autoSize, bold · «BET WON» (gold gradient) или «BET LOST» (red gradient) · виден после результата |
| Колесо рулетки | `RouletteWheelView` | Кастомный View Canvas · квадратный · заполняет пространство · непрерывно крутится до результата |
| Число в центре | `number_info_text` | 68sp bold, по центру колеса · показывает выигравшее число после остановки |
| Сумма | `last_text_view_two` | 18sp, bold, `xura_cyan` · «+X.XX XRP» (win) или «–X.XX XRP» (lose) |
| Поздравление | `last_text_view_tree` | 26sp, bold, `xura_gold` · «CONGRATULATIONS» или «DONT GIVE UP» |
| Обратный отсчёт | `tv_countdown` | 13sp, `xura_text_muted` · «Возврат через 10 сек...» |
| Кнопка «Continue» | `ll_continue_bet` | `bg_card_purple` (фиолетовая) · «ПРОДОЛЖИТЬ ПРОСМОТР» · видна ПОКА крутится колесо → остаётся на экране |
| Кнопка «Вернуться» | `btn_back_to_game` | `bg_card_action_primary` (cyan) · видна ПОСЛЕ результата → возврат в игру |
| Звук | — | `roulette_spin.mp3` loop во время вращения |
| Таймаут | — | 120 сек (настраивается) · при истечении — нейтральный gold-текст |

**Градиенты текста:**
- WIN: `#FFE040` → `#FFB000` → `#FF6A00` → `#FFB000` → `#FFE040` (золотой пульс)
- LOSE: `#FF3060` → `#990022` → `#666688` → `#333355` (красно-серый затухающий)

---

## 25. Экран победы

**Файл:** `win_page.xml`  
**Activity:** `Win`

| Элемент | id | Описание |
|---|---|---|
| Фон | — | `xura_black` |
| Логотип XURA | `logo_xura` | include · horizontal margin 80dp · marginTop 32dp |
| Заголовок | `win_page_text_view` | «ПОБЕДА» · Montserrat Bold **44sp** · signature gradient (cyan→gold) · letterSpacing 0.02 · по центру · marginTop 12dp |
| Сумма | `win_page_text_view_tow` | «ВЫИГРЫШ — X.X XRP» · Montserrat **14sp** · `xura_text_tertiary` · по центру · без рамки · marginTop 12dp |
| Кнопка | `btn_back_to_games` | **«К ИГРАМ»** · высота 80dp · `bg_card_gold` (gold border #FFB000) · заголовок: «К ИГРАМ» gold 15sp bold · подпись: «Вернуться к выбору игры» tertiary 11sp · иконка: `ic_gamepad` gold 36dp · marginBottom 32dp → SelectGame |
| Звук | — | `win.mp3` при открытии |

---

## 26. Экран проигрыша

**Файл:** `lost.xml`  
**Activity:** `Lost`

| Элемент | id | Описание |
|---|---|---|
| Фон | — | `xura_black` |
| Иконка X | `ic_lost_icon` | `ic_lost_x` · 120×120dp · цвет: **`xura_pink` #FF2080** (без tint-overrride) · marginTop 48dp · по центру |
| Заголовок | `last_text_view` | «ПРОИГРЫШ» · Montserrat Bold **36sp** · gradient: `#9020D0`→`#D02090`→`#FF2040` · letterSpacing 0.02 · по центру · marginTop 16dp |
| Сообщение | `last_text_view_two` | «СТАВКА НЕ ПРОШЛА» (или пусто если нет данных) · Montserrat **14sp** · `xura_text_tertiary` · по центру · **без рамки** · marginTop 20dp · horizontal margin 32dp |
| Кнопка | `btn_back_to_games` | **«К ИГРАМ»** · высота 72dp · `bg_card_gold` (gold border #FFB000) · заголовок: «К ИГРАМ» gold 15sp bold · подпись: «Вернуться к выбору игры» tertiary 11sp · иконка: `ic_gamepad` gold 36dp · marginBottom 32dp → SelectGame |
| Звук | — | `lost.mp3` при открытии |

---

## 27. Правила — Угадай цвет

**Файл:** `rules_of_the_game_guess_the_color_page.xml`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «КАК ИГРАТЬ», Montserrat Bold, `xura_gold` |
| Контент | HTML-текст с форматированием · красный/чёрный, множитель ×2, лото-фонд, лимиты |
| Ставка на красное / чёрное | ×2 от суммы ставки |
| Зеро | Проигрыш для всех стандартных ставок |
| Лото | Выигрывается при выпадении зеро · любой игрок |
| Мини-лото | 1/10 от лото |
| Лимиты | мин. 0.1 XRP · макс. 100 XRP |
| Кнопка назад | AppBar back button |

---

## 28. Правила — Угадай число

**Файл:** `rules_of_the_game_guess_the_number_page.xml`

| Элемент | Описание |
|---|---|
| Заголовок | «КАК ИГРАТЬ», gold |
| Механика | Выбрать число 1–36 · угадал → ×36 |
| Зеро | Проигрыш |
| Лото / Мини-лото | аналогично цвету |
| Лимиты | мин. 0.1 XRP · макс. 100 XRP |

---

## 29. Правила — Рулетка

**Файл:** `rules_of_the_game_roulette_page.xml`

Полная таблица ставок европейской рулетки (HTML-текст gold + cyan):

| Тип ставки | Множитель |
|---|---|
| Прямая (0–36) | ×36 |
| Красное / Чёрное | ×2 |
| Нечётное / Чётное | ×2 |
| 1–18 / 19–36 | ×2 |
| Дюжина (1–12, 13–24, 25–36) | ×3 |
| Колонка | ×3 |

Мультиставка, зеро, лото, лимиты описаны аналогично.

---

## 30. Настройки

**Файл:** `settings_page.xml`  
**Activity:** `Settings`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «SETTINGS», Montserrat Bold 28sp, белый · при 7 нажатиях за 3 сек → DEV-панель |
| Scroll | NestedScrollView + LinearLayout |
| Нижняя навигация | присутствует |

**10 пунктов настроек (каждый 80dp, `bg_card_glass_clickable`):**

| № | Заголовок | Подпись | Иконка | Поведение |
|---|---|---|---|---|
| 1 | **РЕЖИМ ИГРЫ** | TRIAL (тест) / LIVE (реальный XRP) | `ic_shield_outline` (TRIAL) / `ic_flask` (LIVE) | AlertDialog «Подтвердить?» → переключает режим. TRIAL: cyan. LIVE: pink→gold |
| 2 | **ПАРОЛЬ** | Установить / Изменить пароль | `ic_lock_open` (не установлен, pink) / `ic_lock` (установлен, cyan) | → SettingsSetPasswordForApp |
| 3 | **БИОМЕТРИЯ** | Вход по отпечатку / Face ID | `ic_face_scan` cyan (ON) / pink (OFF) | ON→BiometricPrompt · OFF→AlertDialog «Отключить?» |
| 4 | **АВТОБЛОКИРОВКА** | 30 сек / 1 / 2 / 5 / 15 мин | `ic_timer` | AlertDialog со списком вариантов |
| 5 | **ЗВУК** | Включён / Выключен | `ic_volume_up` / `ic_volume_off` | Переключение ON/OFF |
| 6 | **ЯЗЫК** | Текущий язык | `ic_language` | → SelectLanguage |
| 7 | **РЕФЕРАЛЫ** | Реферальная программа | `ic_referral` | → Referral |
| 8 | **СТИЛЬ СТАВКИ** | Chips / Slider | `ic_apps_grid` | AlertDialog: «Chips» / «Slider +/-» |
| 9 | **ТАЙМАУТ СТАВКИ** | 1 / 2 / 3 / 5 мин | `ic_alarm` | AlertDialog со списком |
| 10 | **О ПРИЛОЖЕНИИ** | Информация и документация | `ic_info` | → InfoMain |

**Карточка тестового режима** (видна только в TRIAL):
- Фон: MaterialCardView, `xura_card`
- Показывает текущий тестовый баланс, cyan
- Кнопка «СБРОСИТЬ ТЕСТОВЫЙ БАЛАНС»

**DEV-панель** (скрытая):
- SwitchMaterial: Mainnet ↔ Testnet
- Поле адреса сервера рулетки
- Кнопки: Сохранить, Faucet (только testnet), TX History
- Генератор тестового кошелька: адрес + seed + кнопки копирования

**Футер:**
- «XURA v26.7.1», Montserrat, `xura_text_muted`
- «© 2026 Samuil Olegovich», Montserrat, `xura_text_muted`

---

## 31. Выбор языка

**Файл:** `select_language.xml`  
**Activity:** `SelectLanguage`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «ЯЗЫК», Montserrat Bold 24sp |
| Список | 10 строк · каждая: флаг-иконка + название языка |

| Язык | Иконка |
|---|---|
| English | `ic_flag_en` |
| Русский | `ic_flag_ru` |
| 中文 | `ic_flag_zh` |
| हिन्दी | `ic_flag_hi` |
| Español | `ic_flag_es` |
| Français | `ic_flag_fr` |
| Deutsch | `ic_flag_de` |
| العربية | `ic_flag_ar` |
| Português | `ic_flag_pt` |
| বাংলা | `ic_flag_bn` |

При выборе: сохраняется в прefs, приложение перезапускает UI с новым языком.

---

## 32. Реферальный раздел

**Файл:** `referral.xml`  
**Activity:** `Referral`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «РЕФЕРАЛЫ», Montserrat Bold 24sp |
| Кнопка 1 | **«СТАТЬ РЕФЕРАЛОМ»** · `bg_card_action_primary` (cyan) · иконка `ic_referral` · подпись: «Зарегистрироваться (66 XRP)» → BecomeReferral |
| Кнопка 2 | **«ВАШ КОД»** · `bg_card_glass_clickable` · иконка `ic_share` · видна только если код уже есть → YourReferral |
| Кнопка 3 | **«О ПРОГРАММЕ»** · `bg_card_glass_clickable` · иконка `ic_info` → InfoReferral |

---

## 33. Стать рефералом

**Файл:** `become_referral.xml`  
**Activity:** `BecomeReferral`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «СТАТЬ РЕФЕРАЛОМ», Montserrat Bold 22sp |
| Описание | Текст о программе · 14sp · `xura_text_secondary` |
| Стоимость | «66 XRP», Montserrat Bold 32sp, `xura_gold`, по центру |
| Кнопка «ЗАРЕГИСТРИРОВАТЬСЯ» | `bg_card_action_primary` (cyan) · отправляет 66 XRP на сервер с memo «REF» |
| Разделитель | Или |
| Кнопка «ВОССТАНОВИТЬ» | `bg_card_glass_clickable` · «13 XRP» · memo «REF:REC» |
| Поле кода | TextInputLayout · «Введите код для проверки» · необязательно |

---

## 34. Ваш реферальный код

**Файл:** `your_referral_page.xml`  
**Activity:** `YourReferral`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «ВАШ КОД», Montserrat Bold 24sp |
| Код | Montserrat Bold 36sp, `xura_gold`, по центру, в glass-карточке |
| Кнопка «ПОДЕЛИТЬСЯ» | `bg_card_action_primary` (cyan) · иконка `ic_share` · открывает системный share sheet |
| Кнопка «СКОПИРОВАТЬ» | `bg_card_glass_clickable` · иконка `ic_copy` |
| Статистика | Количество рефералов, суммарный бонус |

---

## 35. Информация о реферальной программе

**Файл:** `info_referral_page.xml`  
**Activity:** `InfoReferral`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Заголовок | «О ПРОГРАММЕ», Montserrat Bold 22sp |
| Контент | ScrollView с описанием: как работают рефералы, сколько платят, как получить код, условия |
| Текст | Montserrat 14sp, `xura_text_secondary`, padding 16dp |

---

## 36. О приложении

**Файл:** `info_main_page.xml`  
**Activity:** `InfoMain`

| Элемент | Описание |
|---|---|
| Фон | `xura_black` |
| Логотип | XURA, вверху |
| Версия | «XURA v26.7.1», Montserrat Bold 18sp, белый |
| Разделы | «О приложении» · «Преимущества» · «Наши цели» · каждый в glass-карточке |
| Текст разделов | Montserrat 14sp, `xura_text_secondary`, bulleted list |
| О приложении | Описание: некастодиальный XRP-кошелёк + блокчейн-игры |
| Преимущества | 7 пунктов: прозрачность, безопасность, скорость, XRP Ledger, развитие и т.д. |
| Цели | Доступность, честность, расширение |

---

## Общие правила дизайна

| Правило | Значение |
|---|---|
| Шрифт | Montserrat везде (Regular / Bold) |
| Фон всех экранов | `#000000` |
| Минимальный touch target | 48dp |
| Отступы кратны | 4dp (предпочтительно 8, 16, 24, 32dp) |
| Радиус скругления карточек | 20dp |
| Толщина рамки карточек | 1.5dp |
| Расстояние между карточками | 12dp |
| Горизонтальный margin карточек | 24dp |
| Нижний отступ последней карточки | 32dp |
| Межбуквенный интервал заголовков кнопок | 0.03 |
| Межбуквенный интервал подписей | 0.02 |

---

*Документ сгенерирован из исходного кода XURA-ANDROID-2026 · Версия 26.7.1*

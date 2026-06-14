# XURA — Ревью всех текстов приложения

> Режим: Дизайнер · Дата: 14.06.2026 · Файлы: strings.xml (EN + RU)

---

## Общая оценка

| Критерий | EN | RU |
|----------|----|----|
| Грамматика | 5/10 | 6/10 |
| Консистентность стиля | 4/10 | 5/10 |
| Пользовательский тон | 5/10 | 6/10 |
| Полнота перевода | — | 4/10 |
| Дублирование строк | ❌ Много | ❌ Много |
| Технические строки в UI | ❌ Есть | ❌ Есть |

---

## 1. Критические ошибки

### 1.1 Опечатки в ключах ресурсов (техдолг)

Эти опечатки в `name=` атрибутах видны разработчикам, но влияют на поддержку кода:

| Ключ | Опечатка | Правильно |
|------|----------|-----------|
| `congratulations_yoyr_bet_is_won` | `yoyr` | `your` |
| `congratulations_yoyr_bet_is_won_loto` | `yoyr` | `your` |
| `requesr_xrp` | `requesr` | `request` |
| `get_becom_referral_enum` | `becom` | `become` |

### 1.2 Опечатки в тексте для пользователя (видны в приложении)

| Файл | Ключ | Текущий текст | Проблема | Правильно |
|------|------|---------------|----------|-----------|
| EN | `last_text_two` | `DONT WORRY - PLAY FURTHER AND YOU WILL BE LUCKY` | Нет апострофа | `DON'T WORRY — KEEP PLAYING AND LUCK WILL COME!` |
| EN | `play...` | `PLAY AND KEEP WINING!` | "WINING" = победа в суде | `PLAY AND KEEP WINNING!` |
| EN | `our_goal_1` | `earn in our system..` | Двойная точка | `earn in our system.` |
| EN | `about_benefits_9` | `played that day..` | Двойная точка | `played that day.` |
| RU | `select_game_mode_text_test` | `TECTОВЫЙ` | Латинская "C" вместо кириллической "С" | `ТЕСТОВЫЙ` |
| RU | `referral_recovery_history` | `восстановление реферрала` | "реферрал" (2 р) | `восстановление реферала` |
| RU | `referral_order_history` | `заказ реферрала` | "реферрала" (2 р) | `заказ реферала` |
| RU | `referral_history` | `реферрал:` | "реферрал" (2 р) | `реферал:` |
| RU | `lead_the_seed` | `Ведите ключ` | Опечатка "Ведите" вместо "Введите" | `Введите ключ` |

---

## 2. Непереведённые строки в RU-файле

В файле `values-ru/strings.xml` следующие строки **остались на английском**:

```
about_benefits_1 ... about_benefits_9
about_benefits (заголовок)
it_s_important_to_know_1 ... it_s_important_to_know_12
our_advantages (заголовок + 1..11)
our_goal (заголовок + 1..2)
btn_sub_* (все 24 подписи к кнопкам — только в EN файле)
```

Исключение: `it_s_important_to_know_12` = `"Другие комиссии или сборы: отсутствуют"` — единственная переведённая строка в этом блоке, что выглядит случайностью.

**Итог:** ~40 строк (≈30% контента) не переведены на русский.

---

## 3. Несоответствия между EN и RU версиями

### 3.1 Разная степень детализации

| Ключ | EN | RU | Проблема |
|------|----|----|----------|
| `transaction_history` | `TRANSACTION HISTORY` | `ИСТОРИЯ` | EN информативнее |
| `send_payment` | `SEND PAYMENT` | `ОТПРАВИТЬ` | EN информативнее |
| `requesr_xrp` | `REQUEST XRP` | `ПОЛУЧИТЬ` | EN информативнее |
| `info` | `INFO` | `ОПИСАНИЕ` | Разные слова по смыслу |
| `select_game_mode_text` | `SELECT GAME MODE` | `ВЫБЕРИТЕ РЕЖИМ` | EN информативнее |

### 3.2 Концептуальные расхождения

| Ключ | EN | RU | Проблема |
|------|----|----|----------|
| `bet_lost` | `BET LOST` | `ПЕЧАЛЬКА` | RU чрезмерно неформальный/детский |
| `info_main_text` | `INFORMATION` | `ОПИСАНИЕ` | Разные слова |
| `become_referral_main_text` | `INFORMATION` | `ИНФОРМАЦИЯ` | Наконец совпадает |

---

## 4. Противоречия в контенте (разные экраны — разные данные)

### 4.1 Стоимость реферальной программы — **критично**

Приложение показывает **разные цены** на одно и то же действие:

| Источник | Строка | Цена |
|----------|--------|------|
| `become_referral_text_1` | "вложив 66 XRP" | **66 XRP** |
| `about_benefits_5` | "get a number - 10 XRP" | **10 XRP** |

Пользователь видит 66 XRP на одном экране и 10 XRP на другом. Нужно убрать устаревший блок `about_benefits_*` или синхронизировать цены.

### 4.2 Стоимость восстановления реферала — **критично**

| Источник | Строка | Цена |
|----------|--------|------|
| `referral_recovery_text_1` | "за 13 XRP" | **13 XRP** |
| `about_benefits_6` | "Referral number recovery - 5 XRP" | **5 XRP** |

Разница в 8 XRP — серьёзное противоречие.

### 4.3 "И ПОЛУЧИТЕ В 36 РАЗ БОЛЬШЕ" vs ключ

| Ключ | Значение |
|------|----------|
| `and_get_a_hundred_times_more` | "AND GET A 36 TIMES MORE" |

Ключ говорит "hundred times" (сотня), значение — "36 times". Ключ надо переименовать: `and_get_a_thirtysix_times_more`.

---

## 5. Дублирование строк

### 5.1 Строки со значением "SETTINGS"

Следующие 4 ключа имеют идентичное значение:

```xml
settings_text_linc  →  НАСТРОЙКИ / SETTINGS
settings_main       →  НАСТРОЙКИ / SETTINGS
settings_settings   →  НАСТРОЙКИ / SETTINGS
settings_text       →  НАСТРОЙКИ / SETTINGS
```

**Рекомендация:** Оставить один ключ `settings_title`, остальные удалить.

### 5.2 Строки со значением "ROULETTE"

```xml
roulette_game  →  РУЛЕТКА / ROULETTE
name_roulette  →  РУЛЕТКА / ROULETTE
```

### 5.3 Правила игр — двойные строки

Для каждой игры существует **две версии правил** — "компактная" и "полная" — с идентичным содержанием но разным форматированием:

```xml
rules_double_your_bet     ← без \n
rules_guess_the_number_text ← с \n\n между абзацами

rules_guess_the_color     ← без \n
rules_guess_the_color_text ← с \n\n между абзацами
```

**Рекомендация:** Оставить только версии с `\n\n`. Использовать одну строку в обоих местах.

### 5.4 Строки со значением "INFORMATION"

```xml
info_main_text          →  INFORMATION / ОПИСАНИЕ  (разные значения!)
become_referral_main_text →  INFORMATION / ИНФОРМАЦИЯ
info_referral           →  INFORMATION / ИНФОРМАЦИЯ
```

Три ключа, два разных перевода. Нужна стандартизация.

### 5.5 Дубликат SELECT GAME MODE

```xml
select_game_mode       →  SELECT MODE GAME   ← неправильный порядок слов!
select_game_mode_text  →  SELECT GAME MODE   ← правильно
```

---

## 6. Стиль и тон: проблемные строки

### 6.1 Все-Caps как основной стиль — нарушение MD3

Большинство кнопочных строк написаны в ALL CAPS прямо в strings.xml. По MD3 и Android best practices, трансформацию в верхний регистр должен делать `android:textAllCaps="true"` на уровне layout, а строки должны быть в нормальном регистре.

Это также влияет на читаемость строк в TalkBack (screenreader читает их по буквам).

**Примеры строк, которые нужно перевести в нормальный регистр:**
- `GUESS THE NUMBER` → `Guess the Number`
- `RULES OF THE GAME` → `Rules of the Game`
- `BECOME A REFERRAL` → `Become a Referral`

### 6.2 Разделитель " - " вместо " — "

Почти все составные сообщения используют ` - ` (пробел-дефис-пробел) вместо типографски корректного ` — ` (em dash):

```
BET CANNOT BE MORE THAN -        →  Bet cannot exceed
BET IS MADE - EXPECT THE RESULT  →  Bet placed — awaiting result
ERROR - CHECK THE SEED           →  Error: check your seed phrase
```

### 6.3 Технический язык в пользовательских сообщениях

| Ключ | Текст | Проблема |
|------|-------|----------|
| `it_is_not_possible_to_send_null` | `IT IS NOT POSSIBLE TO SEND NULL` | "NULL" — разработческий термин |
| `tag_knowledge_cannot_be_more` | `TAG KNOWLEDGE CANNOT BE MORE - 2147483647` | "2147483647" (INT_MAX) — технический предел |
| `check_the_seed_and_try_again` | `ERROR - check the seed and try again.` | Смешанный регистр + developer tone |
| `wrong_restart_please` | `Wrong, restart please.` | Слишком грубо и неинформативно |

### 6.4 Непоследовательный регистр в одном файле

В одном файле сосуществуют:
- `payment_sent` → `"Payment sent"` (Sentence case)
- `biometrics_not_set_up` → `"Biometrics not set up on this device"` (Sentence case)
- `wrong_restart_please` → `"Wrong, restart please."` (Sentence case)
- `WANT TO WIN 386301 XRP` (ALL CAPS)
- `Enter referral code` (Sentence case)
- `SCAN QR CODE` (ALL CAPS)

**Нет единого правила регистра.** Рекомендую: все строки в Sentence case в strings.xml, применять `textAllCaps` через XML/style.

### 6.5 Агрессивные обращения

| Ключ | Текст | Проблема |
|------|-------|----------|
| `want_to_win` | `ХОТИТЕ ВЫИГРАТЬ 386301 XRP - НАЖМИТЕ И НАЧНИТЕ ИГРАТЬ!` | Агрессивный casino-маркетинг |
| `referral_text` | `ЕСЛИ У ВАС ЕСТЬ КОД РЕФЕРАЛА - ОБЯЗАТЕЛЬНО ВВЕДИТЕ ЕГО` | Директивный тон ("ОБЯЗАТЕЛЬНО") |
| `go_to_the_dark_side_find_the_secret_button` | `GO TO THE DARK SIDE - FIND THE SECRET BUTTON!` | Easter egg — ок, но предупреждаю |

### 6.6 Грамматически неправильные EN строки

| Ключ | Текст | Правильно |
|------|-------|-----------|
| `select_game_mode` | `SELECT MODE GAME` | `SELECT GAME MODE` |
| `lead_the_seed` | `LEAD THE SEED` | `ENTER YOUR SEED PHRASE` |
| `collect_and_renew_bet` | `COLLECT AND RENEW BET` | `COLLECT AND RESET BET` |
| `guessed_number_should_not_be_less_than` | `GUESSED NUMBER SHOULD NOT BE LESS THAN - 1 - AND MORE THAN - 36` | `Number must be between 1 and 36` |
| `next_one` | `NEXT >>` | `NEXT` (стрелки лишние) |
| `about_benefits_5` | `Become a referral and get a number - 10 XRP` | "get a number" не имеет смысла |
| `it_is_important_to_know_4` | `activating a new wallet costs - 10 XRP` | Тире не нужно перед суммой |
| `want_to_win` | `WANT TO WIN 386301 XRP - PRESS AND START PLAYING!` | Хардкод суммы 386301 — нужно брать с сервера |

---

## 7. Незначительные но систематические проблемы

### 7.1 "БУФФЕР" в RU строках

```xml
addres_copied_to_phone_buffer → АДРЕС СКОПИРОВАН В БУФФЕР ТЕЛЕФОНА
code_copied_to_phone_buffer   → КОД СКОПИРОВАН В БУФФЕР ТЕЛЕФОНА
```

"Буффер" (2 Ф) — орфографическая ошибка. Правильно: **"буфер"** (1 Ф).

Также: "БУФЕР ТЕЛЕФОНА" — устаревшая формулировка. Лучше: "скопировано в буфер обмена" / "copied to clipboard".

### 7.2 Форматирование процентного знака

```xml
our_advantages_11 → "Any user can become our referral and earn 5% of..."
```

В iOS/Android строках символ `%` должен быть экранирован как `%%` если строка передаётся через `String.format()`. Проверить, не вызывает ли это краш.

### 7.3 Пробелы в числах

```xml
guessed_number_should_not_be_less_than → "from - 1 to - 36"
```

Лишние пробелы вокруг чисел создают визуальный шум. Лучше: "from 1 to 36" или "1–36".

### 7.4 "YOUR ADDRESS:" с двоеточием

```xml
your_address → "YOUR ADDRESS:" / "ВАШ АДРЕС:"
```

Двоеточие в строке-ключе нормально, но если за ним следует другой TextView, то между ними двойная пунктуация. Проверить в layout.

---

## 8. Рекомендованные правки по приоритету

### 🔴 Критично (влияет на доверие и юридическую чистоту)

| # | Строка | Текущее | Предложение |
|---|--------|---------|-------------|
| 1 | `become_referral_text_1` vs `about_benefits_5` | 66 XRP vs 10 XRP | Синхронизировать или удалить `about_benefits_*` |
| 2 | `referral_recovery_text_1` vs `about_benefits_6` | 13 XRP vs 5 XRP | Синхронизировать |
| 3 | `select_game_mode_text_test` (RU) | `TECTОВЫЙ` | `ТЕСТОВЫЙ` |
| 4 | `lead_the_seed` (RU) | `Ведите ключ` | `Введите ключ` |

### 🟠 Важно (UX, граммотность)

| # | Строка | Текущее | Предложение |
|---|--------|---------|-------------|
| 5 | `last_text_two` (EN) | `DONT WORRY - PLAY FURTHER` | `DON'T WORRY — KEEP PLAYING!` |
| 6 | `...KEEP WINING!` | `WINING` | `WINNING` |
| 7 | `addres_copied_to_phone_buffer` (RU) | `БУФФЕР` | `буфер обмена` |
| 8 | `code_copied_to_phone_buffer` (RU) | `БУФФЕР` | `буфер обмена` |
| 9 | `it_is_not_possible_to_send_null` | `SEND NULL` | `Amount cannot be empty` |
| 10 | `tag_knowledge_cannot_be_more` | `2147483647` | `Destination tag is too large` |
| 11 | `wrong_restart_please` | `Wrong, restart please.` | `Incorrect seed phrase. Please try again.` |
| 12 | `select_game_mode` (EN) | `SELECT MODE GAME` | `SELECT GAME MODE` |
| 13 | `bet_lost` (RU) | `ПЕЧАЛЬКА` | `ПРОИГРЫШ` |
| 14 | `referral_*_history` (RU) | `реферрала` | `реферала` |

### 🟡 Желательно (стиль и консистентность)

| # | Действие |
|---|----------|
| 15 | Перевести все непереведённые блоки в values-ru/strings.xml |
| 16 | Перевести все `btn_sub_*` строки на русский |
| 17 | Удалить дублирующиеся строки SETTINGS (оставить один ключ) |
| 18 | Удалить дублирующиеся строки ROULETTE |
| 19 | Удалить дублирующиеся блоки правил игры (оставить с `\n\n`) |
| 20 | Убрать ALL CAPS из strings.xml → применять через layout |
| 21 | Заменить ` - ` на ` — ` или `: ` в составных сообщениях |
| 22 | Переименовать `and_get_a_hundred_times_more` → `and_get_36_times_more` |
| 23 | Унифицировать INFO/INFORMATION/ОПИСАНИЕ (один ключ `section_title_info`) |

### 🟢 Косметика (когда будет время)

| # | Действие |
|---|----------|
| 24 | Заменить `>>` в `next_one` на просто `NEXT` |
| 25 | Убрать `"YOUR ADDRESS:"` с двоеточием — проверить layout |
| 26 | Удалить `our_goal_2` двойную точку |
| 27 | Удалить устаревший блок `about_benefits_*` (заменён на `it_is_important_to_know_*`) |
| 28 | Переименовать ключи с опечатками: `yoyr`→`your`, `requesr`→`request` и т.д. |

---

## 9. Предложение: единый стиль написания строк

Рекомендую принять следующий стандарт для всех новых строк:

```
Кнопки:           Sentence case,  без !,   без ALL CAPS
Заголовки:        Title Case,     без !
Сообщения об ошибках: Sentence case. Начинается с контекста: "Address is invalid."
Тосты/уведомления: Sentence case. Краткие: "Copied to clipboard."
Правила/описания: Sentence case. Нормальная пунктуация.
Маркетинговые CTA: Title Case или Sentence case (без ALL CAPS даже для кнопок)
Цены:             "66 XRP" (без лишних пробелов, без дефисов)
```

---

## 10. Блок-список для быстрого поиска и замены

Ниже перечислены строки, которые нужно найти и заменить во всех файлах:

| Найти | Заменить на |
|-------|-------------|
| `WINING!` | `WINNING!` |
| `DONT WORRY` | `DON'T WORRY` |
| `БУФФЕР` | `буфер обмена` |
| `реферрал` | `реферал` (везде в RU) |
| `TECTОВЫЙ` | `ТЕСТОВЫЙ` |
| `Ведите ключ` | `Введите ключ` |
| ` - 2147483647` | ` is too large` |
| `SEND NULL` | `Amount cannot be empty` |
| `play..` (двойная точка) | `play.` |
| `system..` (двойная точка) | `system.` |

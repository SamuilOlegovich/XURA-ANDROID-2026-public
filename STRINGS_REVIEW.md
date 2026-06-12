# Ревью текстов приложения XURA

**Файлы:** `values/strings.xml`, `values-ru/strings.xml`  
**Дата:** 2026-06-12  
**Статус:** Ожидает исправлений

---

## 🔴 Критические ошибки

### 1. Орфография — русский

| Строка | Было | Должно быть |
|---|---|---|
| `addres_copied_to_phone_buffer` (ru) | АДРЕС СКОПИРОВАН В **БУФФЕР** | АДРЕС СКОПИРОВАН В **БУФЕР** |
| `code_copied_to_phone_buffer` (ru) | КОД СКОПИРОВАН В **БУФФЕР** | КОД СКОПИРОВАН В **БУФЕР** |
| `lead_the_seed` (ru) | **Ведите** ключ | **Введите** ключ |
| `select_game_mode_text_test` (ru) | **TECTОВЫЙ** (латинская T в начале) | **ТЕСТОВЫЙ** |
| `future_plans_1` (ru) | **Pасширение** (латинская P) | **Расширение** |
| `your_account_is_not_enough_to_send` (ru) | НА ВАШЕМ СЧЕТУ НЕ **ДОСТАТОЧНО** | НА ВАШЕМ СЧЕТУ **НЕДОСТАТОЧНО** |
| `benefits_for_referrals_and_their_partners_1` (ru) | а **так же** их приглашенные | а **также** их приглашенные |

### 2. Орфография — английский

| Строка | Было | Должно быть |
|---|---|---|
| `congratulations_yoyr_bet_is_won` (en) | KEEP **WINING** | KEEP **WINNING** |
| `congratulations_yoyr_bet_is_won_loto` (en) | KEEP **WINING** | KEEP **WINNING** |
| `last_text_two` (en) | **DONT** WORRY | **DON'T** WORRY |
| `select_game_mode` (en) | SELECT **MODE GAME** | SELECT **GAME MODE** |

### 3. Непереведённые блоки в русской локали

В `values-ru/strings.xml` строки 197–236 полностью на английском:
- Весь блок `about_benefits_*`
- Весь блок `it_s_important_to_know_*`
- Весь блок `our_advantages_*`, `our_goal_*`

Плюс единственная русская строка внутри английского блока:
```
it_s_important_to_know_12: "• Другие комиссии или сборы: отсутствуют"
```
— очевидно случайно оставленная при переводе остального.

Также не переведены на русский:
- `play_again` — "PLAY AGAIN"
- `back_to_games` — "BACK TO GAMES"
- `no_transactions_yet`, `no_transactions_desc`
- `settings_tap_to_copy`, `settings_address_copied`
- `quick_amounts`, `chip_1_xrp`, `chip_5_xrp`, `chip_10_xrp`
- `notify_me`, `roulette_coming_soon_desc`, `roulette_notify_toast`

---

## 🟡 Смысловые и логические проблемы

### 4. Конфликт цен на реферала — 4 разные цифры в одном приложении

| Строка | Содержимое |
|---|---|
| `about_benefits_5` | "Become a referral and get a number - **10 XRP**" |
| `become_referral_text_1` | "investing **66 XRP**" |
| `about_benefits_6` | "Referral number recovery - **5 XRP**" |
| `referral_recovery_text_1` | "recovered for **13 XRP**" |

Пользователь видит четыре разные цены за одно и то же действие. Нужно определить актуальные цифры и синхронизировать.

### 5. Дублирующиеся строки (мусор в ресурсах)

Следующие пары строк используются параллельно, содержат одно и то же:
- `become_a_referral` и `become_referral` — одинаковый текст "BECOME A REFERRAL"
- `settings_main`, `settings_text`, `settings_settings`, `settings_text_linc` — все = "SETTINGS"
- `rules_double_your_bet` и `rules_guess_the_number_text` — почти идентичные правила
- `rules_guess_the_color` и `rules_guess_the_color_text` — почти идентичные
- Блок `it_is_important_to_know_*` и `it_s_important_to_know_*` — два набора похожих строк

### 6. Несоответствие ключа и значения

```xml
<!-- Ключ говорит "hundred times", значение говорит "36 times" -->
<string name="and_get_a_hundred_times_more">AND GET A 36 TIMES MORE</string>
```

### 7. Технический термин NULL в сообщении для пользователя

```xml
<string name="it_is_not_possible_to_send_null">IT IS NOT POSSIBLE TO SEND NULL</string>
```
Пользователь не знает что такое NULL. Нужно: "Enter an amount" / "Введите сумму"

### 8. Техническое сообщение про тег

```xml
<string name="tag_knowledge_cannot_be_more">TAG KNOWLEDGE CANNOT BE MORE - 2147483647</string>
```
"knowledge" — не то слово. Нужно: "Tag value cannot exceed 2,147,483,647"

### 9. Несогласованность написания ЛОТО / ЛОТТО

В русских строках встречается и "ЛОТО" и "ЛОТТО" (два Т).  
Правильно по-русски: **ЛОТО** (одно Т). Унифицировать везде.

### 10. Несогласованность написания реферал / реферрал

В истории транзакций (ru): "реферрал", "реферрала" — два Р.  
Везде остальде: "реферал" — одно Р.  
Нужно унифицировать → **реферал** (одно Р, это устоявшаяся норма).

---

## 🟠 Качество текстов — читабельность

### 11. CAPS LOCK везде

Все системные сообщения об ошибках написаны ЗАГЛАВНЫМИ буквами:
```
YOUR ACCOUNT IS NOT ENOUGH TO SEND
IT IS NOT POSSIBLE TO SEND NULL
TAG KNOWLEDGE CANNOT BE MORE - 2147483647
WRONG DESTINATION ADDRESS
```

CAPS LOCK воспринимается как крик. В MD3 ошибки пишутся обычным регистром.  
Эти строки — кандидаты на замену при реализации Snackbar/inline errors.

### 12. Грамматически сломанные фразы (английский)

| Было | Должно быть |
|---|---|
| "YOUR ACCOUNT IS NOT ENOUGH TO SEND" | "Insufficient balance" |
| "LEAD THE SEED" | "Enter your seed phrase" |
| "ADDRESS COPIED TO PHONE BUFFER" | "Copied to clipboard" |
| "CODE COPIED TO PHONE BUFFER" | "Copied to clipboard" |
| "WRONG, RESTART PLEASE" | "Incorrect seed. Please try again." |
| "PLAY FURTHER AND YOU WILL BE LUCKY" | "Keep playing — luck is on your way!" |
| "GET RECOVERY BECOME REFERRAL SUCCESSFULLY SENT" | "Recovery request sent. Awaiting confirmation." |
| "ORDER FOR REFERRAL SUCCESSFULLY SENT - PLEASE WAIT FOR REPLY FROM THE SERVER" | "Request sent. Awaiting confirmation." |

### 13. Подчёркивание на кнопках — стилистически устарело

Все кнопки обёрнуты в `<u>...</u>`:
```xml
<string name="send"><u>SEND</u></string>
<string name="next"><u>NEXT</u></string>
```

Подчёркивание — признак гиперссылки, не кнопки. MaterialButton не нуждается в нём.  
После перехода на MaterialButton повсеместно — убрать `<u>` теги из всех строк-кнопок.

### 14. Тикер с жёсткой суммой

```xml
<string name="want_to_win">WANT TO WIN 386301 XRP - PRESS AND START PLAYING!</string>
```

Число "386301" выглядит как случайное, вызывает недоверие.  
Лучше: динамическое значение из сервера, или округлённое "386,000 XRP".  
Русский вариант: "ХОТИТЕ ВЫИГРАТЬ 386301 XRP" — та же проблема.

---

## ✅ Что сделано хорошо

- Русские переводы смысловых блоков (it_is_important_to_know, about_the_application, advantages, target, advice) — качественные, читаемые
- Биометрические строки — чистые и понятные на обоих языках
- История транзакций — лаконично и понятно (кроме двойных Р в реферрал)
- `roulette_coming_soon_desc` — профессионально написан

---

## Сводная таблица приоритетов

| Приоритет | Кол-во проблем | Действие |
|---|---|---|
| 🔴 Критично | 7 орфографических + 1 латиница в кириллице | Исправить в strings.xml |
| 🔴 Непереведено | ~15 строк в ru-локали | Перевести |
| 🟡 Смысловые | 7 проблем | Согласовать и исправить |
| 🟠 Читабельность | 8 фраз | Переписать при работе со Snackbar |
| ⚪ Стиль | Теги `<u>` на кнопках | Убрать при следующем проходе по layout |

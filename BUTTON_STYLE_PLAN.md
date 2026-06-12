# План: форма и цвета кнопок

**Статус:** Ожидает реализации  
**Ветка:** ui-redesign-2026  
**Дата:** 2026-06-12

---

## Текущее состояние

В приложении три типа кнопок:

| Тип | Файл | Скругление | Где используется |
|---|---|---|---|
| Primary (градиент) | `bg_button_primary.xml` | 16dp | 18 layout |
| Secondary (стекло) | `bg_button_secondary.xml` | 16dp | 12 layout |
| Num (игровые цифры) | `bg_num_button.xml` | 6dp | игровые экраны |

Плюс в двух layout задан `app:cornerRadius="16dp"` напрямую:
- `win_page.xml` — строки 92, 115
- `roulette_game_page.xml` — строка 153

---

## Что менять и почему

### Изменение 1 — Скругление: 16dp → 100dp (полный pill)

**Затрагивает:** `bg_button_primary.xml`, `bg_button_secondary.xml`

16dp — серая зона без визуальной идентичности. Не прямоугольник и не pill.  
100dp даёт полное скругление независимо от высоты кнопки (56dp).

Это согласуется с floating pill Bottom Nav (`28dp` снаружи = фактически полное скругление для его высоты).

Топовые крипто/финтех приложения используют полный pill для primary CTA:
Revolut, Binance, Crypto.com, Coinbase — все на полном pill.

```xml
<!-- было -->
<corners android:radius="16dp" />

<!-- станет -->
<corners android:radius="100dp" />
```

### Изменение 2 — Градиент: угол 135° → 0°

**Затрагивает:** `bg_button_primary.xml`

Угол 135° — диагональный, на горизонтальной кнопке цвета распределяются непредсказуемо.  
Угол 0° — строго слева направо: Cyan → Purple → Gold. Чистое, читаемое, предсказуемое.

```xml
<!-- было -->
android:angle="135"

<!-- станет -->
android:angle="0"
```

### Изменение 3 — Проверить контраст текста на primary

Левый край градиента — `#00D4FF` (яркий голубой).  
Белый текст (`#FFFFFF`) на голубом фоне — контраст ~1.7:1, что **не проходит** норму AA (4.5:1).

**Решение:** изменить цвет текста на primary кнопках с белого на тёмный `#000000` или `#0D0D20`.  
Либо сдвинуть `startColor` градиента темнее, например `#0090CC` вместо `#00D4FF`.

Нужно визуально проверить на устройстве и выбрать вариант.

### Изменение 4 — `app:cornerRadius` в layout-файлах

В двух файлах задан `app:cornerRadius="16dp"` прямо на MaterialButton — это перекрывает форму drawable.  
Нужно привести в соответствие:

```
win_page.xml         строки 92, 115   → app:cornerRadius="100dp"
roulette_game_page.xml  строка 153    → app:cornerRadius="100dp"
```

### Изменение 5 — Num кнопки (игровые)

**Файл:** `bg_num_button.xml` — оставить `6dp`.  
Это маленькие квадратные кнопки цифр в играх — pill здесь неуместен и сломает UI.  
Можно увеличить до `8dp` для кратности 4dp, но это минорно.

---

## Список файлов

### Drawable (основные изменения):

| Файл | Что менять |
|---|---|
| `drawable/bg_button_primary.xml` | `radius 16→100dp`, `angle 135→0` |
| `drawable/bg_button_secondary.xml` | `radius 16→100dp` |

### Layout (только `app:cornerRadius`):

| Файл | Строка | Что менять |
|---|---|---|
| `layout/win_page.xml` | 92, 115 | `cornerRadius 16→100dp` |
| `layout/roulette_game_page.xml` | 153 | `cornerRadius 16→100dp` |

### Layout с `bg_button_primary` / `bg_button_secondary` (не требуют изменений в XML — форма берётся из drawable автоматически):

```
activity_main.xml, select_game_page.xml, referral.xml,
settings_set_password_for_app.xml, become_referral.xml,
receive_payment.xml, create_new_wallet.xml,
enter_application_password.xml, settings_page.xml,
restore_wallet_page.xml, select_language.xml,
guess_the_number_game_page.xml, send_payment_page.xml,
select_game_mode_page.xml, checking_new_wallet.xml,
set_password_for_app_page.xml, restore_or_create_new_wallet_page.xml,
roulette_game_page.xml
```

---

## Порядок выполнения

```
[1] bg_button_primary.xml   — radius + angle
[2] bg_button_secondary.xml — radius
[3] win_page.xml            — app:cornerRadius на двух кнопках
[4] roulette_game_page.xml  — app:cornerRadius
[5] Проверить на устройстве контраст текста на primary
[6] При необходимости — скорректировать startColor или цвет текста
```

---

## Итог изменений

| До | После |
|---|---|
| `cornerRadius 16dp` — серая зона | `cornerRadius 100dp` — полный pill |
| Диагональный градиент 135° | Горизонтальный градиент 0° — чище |
| Несоответствие с Bottom Nav | Единый язык форм по всему приложению |
| Потенциально низкий контраст | Проверено и исправлено |
| 2 drawable + 2 layout правки | Всё остальное подхватывается автоматически |

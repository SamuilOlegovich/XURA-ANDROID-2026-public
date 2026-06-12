# План: замена Toast → Snackbar + inline errors

**Статус:** Ожидает реализации  
**Ветка:** ui-redesign-2026  
**Дата:** 2026-06-12

---

## Принцип замены

| Тип сообщения | Было | Станет |
|---|---|---|
| Ошибка валидации поля | Toast | `TextInputLayout.setError(...)` под полем |
| Ошибка уровня экрана | Toast | Snackbar (красный акцент) |
| Успех операции | Toast | Snackbar (cyan акцент) |
| Информация / подтверждение | Toast | Snackbar (нейтральный) |

---

## Этап 0 — Общая инфраструктура (сделать первым)

**Файл:** `BaseActivity.java`

Добавить единый метод `showSnackbar(View root, String message, SnackbarType type)`,
чтобы все Activity получили его бесплатно через наследование.

```
enum SnackbarType { SUCCESS, ERROR, INFO }
```

- `SUCCESS` → иконка ✓, текст `xura_cyan`
- `ERROR`   → иконка ✗, текст `xura_error`
- `INFO`    → без иконки, текст `xura_text_primary`

Для `root` использовать `findViewById(android.R.id.content)` — работает в любом Activity.

После реализации этого этапа — все остальные этапы используют `showSnackbar(...)`.

---

## Этап 1 — SendPayment (приоритет: высокий, уже частично сделано)

**Файл:** `view/SendPayment.java`  
**Layout:** `send_payment_page.xml`

Текущее состояние: `showToast` вызывается из observers. Нужно:

### 1а. Добавить TIL-поля в `setButtons()`

```java
// Добавить поля:
private TextInputLayout tilAddress;   // scan_linc
private TextInputLayout tilAmount;    // til_amount_field
private TextInputLayout tilTag;       // til_tag_field
```

### 1б. Очищать ошибки при вводе

Добавить `TextWatcher` на каждое поле:
```java
address.addTextChangedListener → tilAddress.setError(null)
amount.addTextChangedListener  → tilAmount.setError(null)
tag.addTextChangedListener     → tilTag.setError(null)
```

### 1в. Заменить switch в observers

```
WRONG_ADDRESS        → tilAddress.setError(WRONG_DESTINATION_ADDRESS)
INVALID_AMOUNT       → tilAmount.setError(PAYMENT_AMOUNT_IS_INCORRECT)
AMOUNT_IS_ZERO       → tilAmount.setError(IT_IS_NOT_POSSIBLE_TO_SEND_NULL)
TAG_TOO_LONG/LARGE   → tilTag.setError(TAG_KNOWLEDGE_CANNOT_BE_MORE)
INSUFFICIENT_BALANCE → showSnackbar(root, YOUR_ACCOUNT_IS_NOT_ENOUGH_TO_SEND, ERROR)
PAYMENT_FAILED       → showSnackbar(root, WRONG_DESTINATION_ADDRESS, ERROR)
```

### 1г. Успех

```
"PAYMENT SENT" → showSnackbar(root, getString(R.string.payment_sent), SUCCESS)
```

Строку `payment_sent` добавить в `strings.xml` и `values-ru/strings.xml`.

### 1д. Удалить

- метод `showToast(String message)`
- `import android.widget.Toast`

---

## Этап 2 — CheckingNewWallet

**Файл:** `view/CheckingNewWallet.java`  
**Layout:** `checking_new_wallet.xml`  
**TIL:** `til_password_field` (уже существует)

```
Добавить: private TextInputLayout tilSeed; → findViewById(R.id.til_password_field)
Заменить: makeToast(SEED_DOES_NOT_MATCH) → tilSeed.setError(SEED_DOES_NOT_MATCH)
Добавить: TextWatcher на seed → tilSeed.setError(null)
Удалить:  makeToast(), import Toast
```

---

## Этап 3 — EnterApplicationPassword

**Файл:** `view/EnterApplicationPassword.java`  
**Layout:** `enter_application_password.xml`  
**TIL:** `til_enter_application_password_field` (уже существует)

```
Добавить: private TextInputLayout tilPassword; → findViewById(R.id.til_enter_application_password_field)
Заменить: makeToast(PASSWORD_DOES_NOT_MATCH) → tilPassword.setError(PASSWORD_DOES_NOT_MATCH)
Добавить: TextWatcher на password → tilPassword.setError(null)
Удалить:  makeToast(), import Toast
```

---

## Этап 4 — SetAnAppPassword

**Файл:** `view/SetAnAppPassword.java`  
**Layout:** `set_password_for_app_page.xml`  
**TIL:** `til_settings_set_password_app_field_tow` (второе поле — где несовпадение)

```
Добавить: private TextInputLayout tilPasswordTwo; → findViewById(R.id.til_settings_set_password_app_field_tow)
Заменить: makeToast(PASSWORD_DOES_NOT_MATCH) → tilPasswordTwo.setError(PASSWORD_DOES_NOT_MATCH)
Добавить: TextWatcher на passwordTwo → tilPasswordTwo.setError(null)
Удалить:  makeToast(), import Toast
```

---

## Этап 5 — SettingsSetPasswordForApp

**Файл:** `view/SettingsSetPasswordForApp.java`  
**Layout:** `settings_set_password_for_app.xml`  
**TIL:** `til_edit_text_passport_tow` (второе поле — где несовпадение)

```
Добавить: private TextInputLayout tilPasswordTwo; → findViewById(R.id.til_edit_text_passport_tow)
Заменить: makeToast(PASSWORD_DOES_NOT_MATCH) → tilPasswordTwo.setError(PASSWORD_DOES_NOT_MATCH)
Добавить: TextWatcher на второе поле → tilPasswordTwo.setError(null)
Удалить:  makeToast(), import Toast
```

---

## Этап 6 — RestoreWallet

**Файл:** `view/RestoreWallet.java`  
**Layout:** `restore_wallet_page.xml`  
**TIL:** `til_restore_wallet_seed_field` (уже существует)

```
Добавить: private TextInputLayout tilSeed; → findViewById(R.id.til_restore_wallet_seed_field)
Заменить: makeToast(ERROR_CHECK_THE_SEED) → tilSeed.setError(ERROR_CHECK_THE_SEED)
Добавить: TextWatcher на seed → tilSeed.setError(null)
Удалить:  makeToast(), import Toast
```

---

## Этап 7 — Referral

**Файл:** `view/Referral.java`  
**Layout:** `referral.xml`  
**TIL:** `til_referral_code_field` (уже существует)

```
Добавить: private TextInputLayout tilReferralCode; → findViewById(R.id.til_referral_code_field)
Заменить: makeToast(REFERRAL_DOES_NOT_MATCH) → tilReferralCode.setError(REFERRAL_DOES_NOT_MATCH)
Добавить: TextWatcher на enterReferralCode → tilReferralCode.setError(null)
Удалить:  makeToast(), import Toast
```

---

## Этап 8 — BecomeReferral

**Файл:** `view/BecomeReferral.java`  
**Layout:** `become_referral.xml`  
**Нет полей ввода** → все сообщения через Snackbar

```
makeToast(GET_BECOME_REFERRAL)          → showSnackbar(root, GET_BECOME_REFERRAL, SUCCESS)
makeToast(GET_RECOVERY_BECOME_REFERRAL) → showSnackbar(root, GET_RECOVERY_BECOME_REFERRAL, INFO)
makeToast(PAYMENT_AMOUNT_IS_INCORRECT)  → showSnackbar(root, PAYMENT_AMOUNT_IS_INCORRECT, ERROR)
makeToast(YOUR_ACCOUNT_IS_NOT_ENOUGH)   → showSnackbar(root, YOUR_ACCOUNT_IS_NOT_ENOUGH, ERROR)
makeToast(WRONG_DESTINATION_ADDRESS)    → showSnackbar(root, WRONG_DESTINATION_ADDRESS, ERROR)
Удалить:  makeToast(), import Toast
```

---

## Этап 9 — Clipboard confirmations (ReceivePayment, CreateNewWallet, YourReferral)

Все три — подтверждение копирования в буфер. Одинаковая замена:

### ReceivePayment.java
```
makeToast(ADDRESS_COPIED_TO_PHONE_BUFFER) → showSnackbar(root, ADDRESS_COPIED_TO_PHONE_BUFFER, INFO)
Удалить: makeToast(), import Toast
```

### CreateNewWallet.java
```
makeToast(ADDRESS_COPIED_TO_PHONE_BUFFER) → showSnackbar(root, ADDRESS_COPIED_TO_PHONE_BUFFER, INFO)
Удалить: makeToast(), import Toast
```

### YourReferral.java
```
makeToast(CODE_COPIED_TO_PHONE_BUFFER) → showSnackbar(root, CODE_COPIED_TO_PHONE_BUFFER, INFO)
Удалить: makeToast(), import Toast
```

---

## Этап 10 — Settings

**Файл:** `view/Settings.java`

```
Toast(settings_address_copied)          → showSnackbar(root, getString(R.string.settings_address_copied), INFO)
Toast("Biometrics not set up...")       → showSnackbar(root, getString(R.string.biometrics_not_set_up), ERROR)
```

Строку `biometrics_not_set_up` добавить в `strings.xml` и `values-ru/strings.xml`  
(сейчас это hardcoded английская строка).

---

## Этап 11 — Игровые экраны

### GuessTheColorGame.java и GuessTheNumberGame.java

Оба файла уже используют `tilBetField.setError()` для ошибок ставки — это правильно, оставить.  
Только Toast с сообщением "ставка принята, ждите" заменить:

```
showToast(BET_IS_MADE_EXPECT_THE_RESULT) → showSnackbar(root, BET_IS_MADE_EXPECT_THE_RESULT, INFO)
Удалить: showToast(), import Toast
```

### RouletteGame.java

```
Toast(roulette_notify_toast) → showSnackbar(root, getString(R.string.roulette_notify_toast), INFO)
Удалить: import Toast
```

---

## Этап 12 — ScanQrCode

**Файл:** `view/ScanQrCode.java`

Особый случай: Toast показывается из фонового потока через `runOnUiThread`.  
Snackbar тоже нужно вызывать на UI-потоке — логика остаётся.

```
runOnUiThread(() -> Toast(...qrCodeText...)) 
→ runOnUiThread(() -> showSnackbar(root, qrCodeText, INFO))
Удалить: import Toast
```

---

## Строки для добавления в strings.xml

```xml
<string name="payment_sent">Платёж отправлен</string>
<string name="biometrics_not_set_up">Биометрия не настроена на этом устройстве</string>
```

То же самое в `values-ru/strings.xml`.

---

## Порядок выполнения

```
[0] BaseActivity    — инфраструктура showSnackbar (всё остальное зависит от этого)
[1] SendPayment     — самый сложный, inline + snackbar
[2] CheckingNewWallet, EnterApplicationPassword, SetAnAppPassword,
    SettingsSetPasswordForApp, RestoreWallet, Referral — inline errors (однотипные)
[3] BecomeReferral, ReceivePayment, CreateNewWallet, YourReferral, Settings — только Snackbar
[4] GuessTheColorGame, GuessTheNumberGame, RouletteGame, ScanQrCode — только Snackbar
[5] strings.xml     — добавить новые строки
```

---

## Итого

| Файлов затронуто | Toast вызовов к замене |
|---|---|
| 16 Java-файлов | ~35 вызовов |
| 2 strings.xml | 2 новые строки |
| 0 layout-файлов | TIL уже везде есть |

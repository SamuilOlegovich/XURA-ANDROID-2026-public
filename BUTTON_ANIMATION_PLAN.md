# План: замена анимации кнопок

**Статус:** Ожидает реализации  
**Ветка:** ui-redesign-2026  
**Дата:** 2026-06-12

---

## Проблема с текущей анимацией

Файл: `res/anim/anim_translate.xml`

```xml
<translate
    android:fromXDelta="0"
    android:toXDelta="100%p"     <!-- кнопка улетает на ширину экрана вправо -->
    android:duration="1000"      <!-- 1 секунда туда + 1 секунда обратно = 2 сек -->
    android:repeatCount="1"
    android:repeatMode="reverse"
    android:interpolator="@android:anim/linear_interpolator" />  <!-- без замедления -->
```

| Проблема | Почему плохо |
|---|---|
| 2 секунды на одно нажатие | Пользователь думает, что приложение зависло |
| Кнопка улетает за экран | Дезориентирует, ломает визуальную иерархию |
| Linear interpolator | Движение роботообразное, без физики |
| Дублируется в 20 файлах | `animTranslate` поле + loadAnimation в каждом Activity |
| Конфликт с loading-состоянием | На Send: кнопка улетает пока виден спиннер |

---

## Что делают топовые приложения

**Revolut, Binance, 1Password, Telegram:**

Scale pulse — кнопка чуть уменьшается при нажатии и возвращается.  
Ощущение: физическое, как настоящая кнопка. Мгновенное. Ненавязчивое.

```
Нажал → scale 1.0 → 0.95 (80ms, decelerate)
Отпустил → scale 0.95 → 1.0 (80ms, decelerate)
Итого: 160ms вместо 2000ms
```

MaterialButton уже имеет встроенный **ripple** — он срабатывает сам по себе.  
Scale pulse — единственное дополнение, которое оправдано поверх ripple.

---

## Решение

### Шаг 1 — Новый файл анимации

Создать `res/anim/anim_scale_pulse.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <scale
        android:fromXScale="1.0"
        android:toXScale="0.95"
        android:fromYScale="1.0"
        android:toYScale="0.95"
        android:pivotX="50%"
        android:pivotY="50%"
        android:duration="80"
        android:repeatCount="1"
        android:repeatMode="reverse"
        android:interpolator="@android:anim/decelerate_interpolator" />
</set>
```

### Шаг 2 — Хелпер в BaseActivity

Добавить один метод в `BaseActivity.java`:

```java
protected void pulse(View v) {
    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_scale_pulse));
}
```

Это устраняет дублирование: каждый Activity больше не держит своё поле `animTranslate`
и не вызывает `loadAnimation` самостоятельно.

### Шаг 3 — Замена в каждом файле

В каждом из 20 файлов:
1. Удалить поле `private Animation animTranslate;`
2. Удалить `animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);`
3. Заменить `v.startAnimation(animTranslate)` → `pulse(v)`
4. Удалить `import android.view.animation.Animation;`
5. Удалить `import android.view.animation.AnimationUtils;` (если не используется больше нигде)

### Шаг 4 — Удалить старый файл

Удалить `res/anim/anim_translate.xml` — он больше не нужен нигде.

---

## Список файлов для изменения (20 штук)

| Файл | Кол-во вызовов startAnimation |
|---|---|
| `MainActivity.java` | 4 |
| `SelectGame.java` | 5 |
| `GuessTheColorGame.java` | 3 |
| `GuessTheNumberGame.java` | 2 |
| `RouletteGame.java` | 2 |
| `Settings.java` | 3 |
| `SetAnAppPassword.java` | 2 |
| `SendPayment.java` | 1 |
| `CreateNewWallet.java` | 2 |
| `RestoreOrCreateNewWallet.java` | 2 |
| `RestoreWallet.java` | 1 |
| `CheckingNewWallet.java` | 1 |
| `EnterApplicationPassword.java` | 1 |
| `BecomeReferral.java` | 2 |
| `Referral.java` | 2 |
| `SettingsSetPasswordForApp.java` | 1 |
| `SelectLanguage.java` | 2 |
| `SelectGameMode.java` | 1 |
| `ReceivePayment.java` | 1 |
| `YourReferral.java` | 1 |
| **Итого** | **~38 вызовов** |

---

## Порядок выполнения

```
[1] Создать res/anim/anim_scale_pulse.xml
[2] Добавить pulse(View v) в BaseActivity.java
[3] Заменить во всех 20 файлах (можно делать по одному)
[4] Удалить res/anim/anim_translate.xml
```

---

## Результат

| До | После |
|---|---|
| 2000ms на нажатие | 160ms |
| Кнопка улетает с экрана | Лёгкое уменьшение на месте |
| Linear, без физики | Decelerate — естественное замедление |
| Код дублируется в 20 файлах | Один метод `pulse()` в BaseActivity |
| Конфликт со спиннером в SendPayment | Нет конфликта — анимация локальная |

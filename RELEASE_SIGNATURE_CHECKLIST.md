# Чеклист: SignatureVerifier перед релизом

`SignatureVerifier.java` (`app/src/main/java/com/samuilolegovich/utils/SignatureVerifier.java`)
проверяет в runtime, что APK подписан ожидаемым сертификатом — это защита от
пересобранных/клонированных копий приложения. Сейчас в `ALLOWED_SHA256`
зашит только отпечаток **локального debug-keystore**:

```java
private static final Set<String> ALLOWED_SHA256 = new HashSet<>(Arrays.asList(
        "A2E753A3C2AC8BDAEAB08C72DB5912E5FDCF330DE0771F63CC17B9B153C847CF" // локальный debug-keystore
));
```

Релизных сборок пока нет, поэтому отпечаток release-сертификата отсутствует.
Если выпустить релизный APK/AAB без этого шага — `isSignatureValid()` вернёт
`false`, и каждый легитимный пользователь увидит экран
«Целостность приложения нарушена» вместо рабочего приложения.

## Шаги перед первым релизом

### 1. Завести release-keystore (если ещё не создан)

```bash
keytool -genkeypair -v \
  -keystore release.jks \
  -alias xura-release \
  -keyalg RSA -keysize 2048 -validity 10000
```

Хранить `release.jks` и пароли **вне репозитория** (менеджер паролей / CI-секреты),
никогда не коммитить.

### 2. Подключить signingConfig в `app/build.gradle`

Сейчас `signingConfigs { }` в проекте не настроен — release собирается без
подписи или дефолтной. Нужно добавить блок `signingConfigs.release` со
ссылкой на `release.jks` (через `gradle.properties`/переменные окружения,
не хардкодом) и привязать его к `buildTypes.release.signingConfig`.

### 3. Решить — Play App Signing или свой ключ

**Рекомендуется Play App Signing** (стандарт Google с 2021 года для новых
приложений): вы загружаете AAB, подписанный **upload-ключом**, а Google
переподписывает финальный APK, который получают пользователи,
**собственным app signing key**. Это два разных сертификата с разными
отпечатками.

⚠️ Важно: если используется Play App Signing, в `ALLOWED_SHA256` нужно
зашить отпечаток именно **App signing key certificate**, а не
upload-сертификата — иначе проверка будет ломаться у всех пользователей,
скачавших APK из Play Store.

### 4. Получить SHA-256 отпечаток правильного сертификата

- **Play App Signing**: Play Console → выбрать приложение → "Настройка" →
  "Целостность приложения" (Setup → App integrity) → раздел
  "Сертификат подписи приложения" (App signing key certificate) → скопировать
  значение SHA-256.
- **Свой ключ без Play App Signing**:
  ```bash
  keytool -list -v -keystore release.jks -alias xura-release
  ```
  взять поле `SHA256:` из вывода.

Формат для проекта — без двоеточий, без пробелов, заглавными буквами
(как уже сделано для debug-отпечатка), например:
`A2E753A3C2AC8BDAEAB08C72DB5912E5FDCF330DE0771F63CC17B9B153C847CF`.

### 5. Добавить отпечаток в `SignatureVerifier.ALLOWED_SHA256`

```java
private static final Set<String> ALLOWED_SHA256 = new HashSet<>(Arrays.asList(
        "<SHA256_RELEASE_ОТПЕЧАТОК>",                                       // release (Play App Signing / свой ключ)
        "A2E753A3C2AC8BDAEAB08C72DB5912E5FDCF330DE0771F63CC17B9B153C847CF"   // debug-keystore разработчика
));
```

Можно оставить debug-отпечаток рядом — он не даёт атакующему ничего
(debug.keystore генерируется локально у каждого разработчика и не
является секретом, но и не публикуется), а разработчикам не мешает
тестировать debug-сборки после релиза.

Если в команде несколько разработчиков с разными debug-keystore —
у каждого свой SHA-256, тогда либо:
- коммитить общий `debug.keystore` в репозиторий (`app/debug.keystore` +
  `signingConfigs.debug.storeFile`), чтобы у всех был один отпечаток, либо
- добавить отпечаток debug-keystore каждого разработчика в список.

### 6. Проверить, нет ли нескольких подписантов (App Bundle + динамические модули)

Код уже обрабатывает `signingInfo.hasMultipleSigners()` — если в будущем
появятся динамические feature-модули с отдельной подписью, циклы по
`getApkContentsSigners()` уже корректно сработают, отдельных изменений
не требуется.

### 7. Тестирование перед публикацией

```bash
./gradlew bundleRelease   # или assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

Убедиться, что:
- приложение запускается без диалога «Целостность приложения нарушена»;
- при установке APK, пересобранного/подписанного чужим ключом (debug-keystore
  с другой машины, например), диалог корректно появляется.

## Где это в общем плане безопасности

Это TODO относится к пункту 3 общего плана защиты XURA (runtime-проверка
подписи APK). Остальные пункты (TLS pinning — готово, анти-debug/Frida —
в работе, clipboard-защита, ProGuard/Network Security Config) описаны
отдельно по ходу работы и не входят в этот документ.

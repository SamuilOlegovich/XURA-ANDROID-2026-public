# XURA iOS Porting Guide

**Целевой способ установки:** Xcode + бесплатный Apple ID (без оплаты $99)  
**Язык разработки:** Swift 5.9+  
**UI-фреймворк:** SwiftUI  
**Минимальная версия iOS:** 16.0  
**Эталонный Android-проект:** `XURA-ANDROID-2026`

---

## Оглавление

1. [Установка без Apple Developer](#1-установка-без-apple-developer)
2. [Технологический стек](#2-технологический-стек)
3. [Дизайн-система](#3-дизайн-система)
4. [Навигация и экраны](#4-навигация-и-экраны)
5. [Защита приложения](#5-защита-приложения)
6. [Хранение данных и шифрование](#6-хранение-данных-и-шифрование)
7. [XRP / Wallet логика](#7-xrp--wallet-логика)
8. [Игровая логика](#8-игровая-логика)
9. [Сетевой протокол](#9-сетевой-протокол)
10. [Звук](#10-звук)
11. [Настройки](#11-настройки)
12. [Все экраны — детальное описание](#12-все-экраны--детальное-описание)

---

## 1. Установка без Apple Developer

### Что нужно
- Mac (macOS 13+) с установленным **Xcode** (бесплатно из App Store)
- **Бесплатный Apple ID** (не платный Developer аккаунт)
- iPhone подключён по кабелю USB-C / Lightning

### Процесс первой установки
```
1. Открыть проект в Xcode
2. Product → Destination → выбрать подключённый iPhone
3. Signing & Capabilities → Team → войти с Apple ID → выбрать Personal Team
4. Bundle Identifier: com.xura.wallet (уникальный, не менять)
5. Cmd + R → Xcode соберёт и установит
6. На iPhone: Настройки → Основные → VPN и управление устройством
   → Apple ID → Доверять
```

### Ограничения бесплатного аккаунта
| Параметр | Значение |
|---|---|
| Срок действия сертификата | **7 дней** |
| Максимум приложений на устройстве | **3** |
| Переподписание | Каждые 7 дней: подключить iPhone + Cmd+R |
| Нужен Mac рядом | Да, каждый раз |
| TestFlight | Недоступен |

### Переподписание (каждые 7 дней)
```
1. Подключить iPhone по кабелю
2. Открыть Xcode → выбрать устройство
3. Cmd + R (Build & Run)
4. Приложение переустановится, данные сохранятся (Keychain + UserDefaults)
```

---

## 2. Технологический стек

### Аналоги Android → iOS

| Android | iOS (Swift) | Назначение |
|---|---|---|
| Java / Kotlin | **Swift 5.9** | Язык разработки |
| XML Layouts | **SwiftUI** | Декларативный UI |
| ConstraintLayout | `ZStack / VStack / HStack` | Разметка |
| Activity | **View + NavigationStack** | Экран |
| Intent | `NavigationPath` / `@State` | Навигация |
| SharedPreferences (зашифр.) | **Keychain** | Хранение секретов |
| EncryptedSharedPreferences | **Keychain** + `UserDefaults` | Настройки |
| Android Keystore / AES-GCM | **CryptoKit** + **Keychain** | Шифрование |
| BiometricPrompt | **LocalAuthentication** | Биометрия |
| MediaPlayer | **AVAudioPlayer** | Воспроизведение звука |
| SoundPool | **AVAudioPlayer** (несколько инстансов) | Короткие звуки |
| WebSocket (OkHttp) | **URLSessionWebSocketTask** | XRPL WebSocket |
| xrpl4j | **xrpl-swift** или REST XRPL API | XRP-транзакции |
| Canvas / custom View | **Canvas** в SwiftUI | RouletteWheelView |
| LinearGradient (Android) | `LinearGradient` (SwiftUI) | Градиенты |
| ViewModel + LiveData | **@StateObject + ObservableObject** | Реактивный стейт |
| ForegroundService | **Background Tasks** (`BGTaskScheduler`) | Фоновый WebSocket |
| PBKDF2WithHmacSHA256 | `CryptoKit.HKDF` / `CommonCrypto.CCKeyDerivationPBKDF` | Хэш пароля |

### Зависимости (Swift Package Manager)

```swift
// Package.swift dependencies
.package(url: "https://github.com/XRPLF/xrpl-swift", from: "0.1.0"),
// ИЛИ работать с XRPL напрямую через URLSession + JSON-RPC

.package(url: "https://github.com/Alamofire/Alamofire", from: "5.8.0"),
// опционально для HTTP (faucet, testnet)
```

---

## 3. Дизайн-система

### Цвета (XURA Design System → SwiftUI Color)

```swift
// Colors.swift
extension Color {
    // Backgrounds
    static let xuraBlack   = Color(hex: "#000000")   // основной фон
    static let xuraSurface = Color(hex: "#08081A")   // поверхность
    static let xuraCard    = Color(hex: "#0D0D20")   // карточки

    // XURA Signature Gradient
    static let xuraCyan    = Color(hex: "#00D4FF")
    static let xuraBlue    = Color(hex: "#4080FF")
    static let xuraIndigo  = Color(hex: "#6040FF")
    static let xuraPurple  = Color(hex: "#9020D0")
    static let xuraMagenta = Color(hex: "#D020A0")
    static let xuraPink    = Color(hex: "#FF2080")   // цвет иконки X (проигрыш)
    static let xuraOrange  = Color(hex: "#FF5020")
    static let xuraGold    = Color(hex: "#FFB000")   // победа, кнопка К Играм

    // Text hierarchy
    static let xuraTextPrimary   = Color.white
    static let xuraTextSecondary = Color.white.opacity(0.80)
    static let xuraTextTertiary  = Color.white.opacity(0.50)  // подписи, мелкие тексты
    static let xuraTextMuted     = Color.white.opacity(0.50)

    // State
    static let xuraSuccess = Color(hex: "#00FF88")
    static let xuraError   = Color(hex: "#FF3060")
    static let xuraWarning = Color(hex: "#FFB000")   // = xuraGold

    // Glass
    static let xuraGlassFill       = Color.white.opacity(0.10)
    static let xuraGlassBorder     = Color.white.opacity(0.20)
    static let xuraGlassBorderStrong = Color.white.opacity(0.30)
}

// Hex-инициализатор
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: .alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r = Double((int >> 16) & 0xFF) / 255
        let g = Double((int >> 8)  & 0xFF) / 255
        let b = Double(int & 0xFF)         / 255
        self.init(red: r, green: g, blue: b)
    }
}
```

### XURA Signature Gradient

```swift
// Используется в логотипе и заголовке ПОБЕДА
static let xuraSignatureGradient = LinearGradient(
    colors: [.xuraCyan, .xuraBlue, .xuraIndigo, .xuraPurple,
             .xuraMagenta, .xuraPink, .xuraOrange, .xuraGold],
    startPoint: .leading,
    endPoint: .trailing
)

// Градиент ПРОИГРЫШ (фиолетовый → малиновый → красный)
static let xuraLostGradient = LinearGradient(
    colors: [Color(hex: "#9020D0"), Color(hex: "#D02090"), Color(hex: "#FF2040")],
    startPoint: .leading,
    endPoint: .trailing
)

// Градиент WIN (золотой)
static let xuraWinGradient = LinearGradient(
    colors: [Color(hex: "#FFE040"), Color(hex: "#FFB000"),
             Color(hex: "#FF6A00"), Color(hex: "#FFB000"), Color(hex: "#FFE040")],
    startPoint: .leading,
    endPoint: .trailing
)
```

### Типографика

```swift
// Typography.swift
extension Font {
    // Montserrat (добавить в Assets → Fonts)
    static func montserrat(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        .custom("Montserrat-Regular", size: size).weight(weight)
    }

    // Стили по экранам
    static let xuraBalance     = montserrat(40, weight: .bold)    // баланс на главной
    static let xuraHeroTitle   = montserrat(36, weight: .bold)    // ПРОИГРЫШ
    static let xuraWinTitle    = montserrat(44, weight: .bold)    // ПОБЕДА
    static let xuraScreenTitle = montserrat(28, weight: .bold)    // заголовки экранов
    static let xuraCardTitle   = montserrat(15, weight: .bold)    // кнопки
    static let xuraCardSub     = montserrat(11)                   // подписи кнопок
    static let xuraBody        = montserrat(14)                   // основной текст
    static let xuraMicro       = montserrat(11)                   // мелкий
}
```

### Карточки и кнопки

```swift
// CardStyles.swift

// Карточка с рамкой (аналог bg_card_action_primary)
struct XuraActionCard: ViewModifier {
    let borderColor: Color
    func body(content: Content) -> some View {
        content
            .background(Color.xuraCard)
            .overlay(RoundedRectangle(cornerRadius: 20).stroke(borderColor, lineWidth: 1.5))
            .clipShape(RoundedRectangle(cornerRadius: 20))
    }
}

// Стеклянная карточка (аналог bg_card_glass)
struct XuraGlassCard: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(Color.xuraGlassFill)
            .overlay(RoundedRectangle(cornerRadius: 20).stroke(Color.xuraGlassBorder, lineWidth: 1))
            .clipShape(RoundedRectangle(cornerRadius: 20))
    }
}

// Использование:
// .modifier(XuraActionCard(borderColor: .xuraGold))   // золотая кнопка
// .modifier(XuraActionCard(borderColor: .xuraCyan))   // голубая кнопка
// .modifier(XuraGlassCard())                          // стеклянная
```

### Стандартная кнопка-карточка (80dp → 80pt)

```swift
struct XuraCardButton: View {
    let title: String
    let subtitle: String
    let icon: String          // SF Symbol или кастомная иконка
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.xuraCardTitle)
                        .foregroundColor(color)
                    Text(subtitle)
                        .font(.xuraCardSub)
                        .foregroundColor(.xuraTextTertiary)
                }
                Spacer()
                Image(icon)
                    .resizable()
                    .frame(width: 36, height: 36)
                    .foregroundColor(color)
            }
            .padding(.horizontal, 16)
            .frame(height: 80)
            .modifier(XuraActionCard(borderColor: color))
        }
        .buttonStyle(.plain)
        .padding(.horizontal, 24)
    }
}
```

### Логотип XURA

Логотип состоит из двух элементов:
1. **Иконка X** — `ic_xura_logo` (кастомный SVG с signature-градиентом)
2. **Текст "XURA"** — Montserrat Bold, белый, letterSpacing 0.16

```swift
struct XuraLogo: View {
    var body: some View {
        VStack(spacing: 4) {
            Image("ic_xura_logo")
                .resizable()
                .scaledToFit()
            Text("XURA")
                .font(.montserrat(24, weight: .bold))
                .tracking(4)
                .foregroundColor(.white)
        }
    }
}
```

---

## 4. Навигация и экраны

### Структура навигации

```
SplashView
    ↓ (1.8 сек анимация)
    ├─ OnboardingView          (первый запуск)
    ├─ SetPasswordView         (если пароль не установлен)
    ├─ RestoreOrCreateWalletView (если seed не сохранён)
    └─ EnterPasswordView       (каждый запуск / автоблокировка)
            ↓
        MainTabView
            ├─ [Tab 1] WalletView (MainView)
            ├─ [Tab 2] SelectGameView
            └─ [Tab 3] SettingsView
```

### NavigationStack (iOS 16+)

```swift
// AppRouter.swift
enum AppRoute: Hashable {
    case onboarding
    case setPassword
    case restoreOrCreate
    case enterPassword
    case main
    case sendPayment
    case receivePayment
    case transactionHistory
    case selectGame
    case guessColor
    case guessNumber
    case roulette
    case flasher
    case win
    case lost
    case settings
    case selectLanguage
    case referral
    case becomeReferral
    case yourReferral
    case rulesColor
    case rulesNumber
    case rulesRoulette
    case infoMain
}

@MainActor
class AppCoordinator: ObservableObject {
    @Published var path = NavigationPath()
    @Published var currentRoot: AppRoute = .splash

    func startup() {
        // 1. Онбординг не показывался
        guard UserPrefs.onboardingSeen else { currentRoot = .onboarding; return }
        // 2. Пароль не установлен
        guard KeychainHelper.passwordHash != nil else { currentRoot = .setPassword; return }
        // 3. Seed не сохранён
        guard KeychainHelper.seedExists else { currentRoot = .restoreOrCreate; return }
        // 4. Пароль не введён в этой сессии
        guard AppSession.shared.isUnlocked else { currentRoot = .enterPassword; return }
        // 5. Всё ок
        currentRoot = .main
    }
}
```

### Bottom Navigation (Tab Bar)

```swift
struct MainTabView: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            WalletView()
                .tabItem { Image("ic_nav_wallet") }
                .tag(0)
            SelectGameView()
                .tabItem { Image("ic_nav_games") }
                .tag(1)
            SettingsView()
                .tabItem { Image("ic_nav_settings") }
                .tag(2)
        }
        .background(Color.xuraBlack)
        // Кастомный pill-навбар как в Android
    }
}
```

### Список всех 31 экрана

| Android Activity | iOS View | Назначение |
|---|---|---|
| SplashActivity | `SplashView` | Логотип + анимация запуска |
| OnboardingActivity | `OnboardingView` | 4 страницы ViewPager → PageTabView |
| RestoreOrCreateNewWallet | `RestoreOrCreateView` | Выбор: создать / восстановить |
| CreateNewWallet | `CreateWalletView` | Показ seed-фразы (24 слова) |
| CheckingNewWallet | `VerifyWalletView` | Подтверждение seed |
| RestoreWallet | `RestoreWalletView` | Ввод seed для восстановления |
| SetAnAppPassword | `SetPasswordView` | Установка пароля |
| EnterApplicationPassword | `EnterPasswordView` | Ввод пароля / биометрия |
| SettingsSetPasswordForApp | `ChangePasswordView` | Смена пароля |
| MainActivity | `WalletView` | Баланс + кнопки Send/Receive/History |
| SendPayment | `SendPaymentView` | Отправка XRP |
| ReceivePayment | `ReceivePaymentView` | QR-код для получения |
| ScanQrCode | `ScanQRView` | Камера / AVFoundation |
| TransactionHistory | `TransactionHistoryView` | Список транзакций |
| TxDetailSheet | `TxDetailView` | BottomSheet с деталями |
| SelectGame | `SelectGameView` | Выбор игры |
| GuessTheColorGame | `ColorGameView` | Игра «Угадай цвет» |
| GuessTheNumberGame | `NumberGameView` | Игра «Угадай число» |
| RouletteGame | `RouletteGameView` | Рулетка |
| Flasher | `FlasherView` | Ожидание результата + колесо |
| Win | `WinView` | Экран победы |
| Lost | `LostView` | Экран проигрыша |
| RulesOfTheGameGuessTheColor | `RulesColorView` | Правила: цвет |
| RulesOfTheGameGuessTheNumber | `RulesNumberView` | Правила: число |
| RulesOfTheGameRoulette | `RulesRouletteView` | Правила: рулетка |
| Referral | `ReferralView` | Реферальный раздел |
| BecomeReferral | `BecomeReferralView` | Стать рефералом (66 XRP) |
| YourReferral | `YourReferralView` | Ваш реферальный код |
| InfoReferral | `InfoReferralView` | Описание программы |
| Settings | `SettingsView` | Все настройки |
| SelectLanguage | `SelectLanguageView` | 10 языков |
| InfoMain | `InfoView` | О приложении |

---

## 5. Защита приложения

### Биометрия (LocalAuthentication)

Аналог `BiometricHelper.java` → `BiometricManager.swift`:

```swift
import LocalAuthentication

class BiometricManager {
    static func isAvailable() -> Bool {
        let context = LAContext()
        var error: NSError?
        return context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
    }

    static func authenticate(
        reason: String,
        onSuccess: @escaping () -> Void,
        onFallback: @escaping () -> Void,    // «Использовать пароль»
        onError: @escaping (String) -> Void
    ) {
        let context = LAContext()
        context.localizedFallbackTitle = "USE PASSWORD"
        context.localizedCancelTitle = "CANCEL"

        context.evaluatePolicy(
            .deviceOwnerAuthenticationWithBiometrics,
            localizedReason: reason
        ) { success, error in
            DispatchQueue.main.async {
                if success {
                    onSuccess()
                } else if let err = error as? LAError {
                    switch err.code {
                    case .userFallback, .userCancel, .systemCancel:
                        onFallback()
                    default:
                        onError(err.localizedDescription)
                    }
                }
            }
        }
    }
}
```

### Автоблокировка (InactivityGuard)

```swift
// InactivityGuard.swift
class InactivityGuard: ObservableObject {
    private var timer: Timer?
    @Published var isLocked = false
    var timeoutSeconds: TimeInterval = 120  // из настроек

    func resetTimer() {
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: timeoutSeconds, repeats: false) { [weak self] _ in
            DispatchQueue.main.async { self?.isLocked = true }
        }
    }

    func unlock() {
        isLocked = false
        resetTimer()
    }
}
```

### Блокировка кнопки «Назад»

На `EnterPasswordView` нужно полностью убрать жест свайпа назад:

```swift
struct EnterPasswordView: View {
    var body: some View {
        content
            .navigationBarBackButtonHidden(true)
            .interactiveDismissDisabled(true)
            .gesture(DragGesture())  // блокируем свайп
    }
}
```

### Защита от скриншотов (аналог FLAG_SECURE)

```swift
// В AppDelegate или через UIKit overlay
func makeScreenshotProtectedWindow() {
    let field = UITextField()
    field.isSecureTextEntry = true
    // Добавить как подview поверх window
    // При secure=true система блокирует скриншоты в Release
}
```

### Проверка джейлбрейка (RootDetector)

```swift
// JailbreakDetector.swift
struct JailbreakDetector {
    static func isJailbroken() -> Bool {
        #if targetEnvironment(simulator)
        return false
        #else
        let paths = [
            "/Applications/Cydia.app",
            "/private/var/lib/apt/",
            "/usr/sbin/sshd",
            "/etc/apt",
            "/bin/bash"
        ]
        for path in paths {
            if FileManager.default.fileExists(atPath: path) { return true }
        }
        if let _ = try? "jailbreak".write(toFile: "/private/test.txt",
                                          atomically: true, encoding: .utf8) {
            return true
        }
        return false
        #endif
    }
}
```

---

## 6. Хранение данных и шифрование

### Keychain (аналог EncryptedSharedPreferences + Android Keystore)

Весь Keychain в iOS автоматически защищён Secure Enclave:

```swift
// KeychainHelper.swift
struct KeychainHelper {
    static func save(_ value: String, key: String) {
        let data = Data(value.utf8)
        let query: [String: Any] = [
            kSecClass as String:            kSecClassGenericPassword,
            kSecAttrAccount as String:      key,
            kSecAttrAccessible as String:   kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
            kSecValueData as String:        data
        ]
        SecItemDelete(query as CFDictionary)
        SecItemAdd(query as CFDictionary, nil)
    }

    static func load(_ key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String:       kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecReturnData as String:  true,
            kSecMatchLimit as String:  kSecMatchLimitOne
        ]
        var result: AnyObject?
        guard SecItemCopyMatching(query as CFDictionary, &result) == errSecSuccess,
              let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    static func delete(_ key: String) {
        let query: [String: Any] = [
            kSecClass as String:       kSecClassGenericPassword,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(query as CFDictionary)
    }
}
```

### Ключи хранилища (аналог StringEnum.java)

```swift
// PrefsKeys.swift
enum PrefsKeys {
    static let passwordHash      = "xura_password_hash"
    static let passwordSalt      = "xura_password_salt"
    static let encryptedSeed     = "xura_encrypted_seed"
    static let biometricEnabled  = "xura_biometric_enabled"
    static let lockTimeout       = "xura_lock_timeout"
    static let soundEnabled      = "xura_sound_enabled"
    static let gameMode          = "xura_game_mode"         // "true" = LIVE
    static let locale            = "xura_locale"
    static let referralCode      = "xura_referral"
    static let onboardingSeen    = "xura_onboarding_seen"
    static let betInputStyle     = "xura_bet_input_style"   // "chips" / "slider"
    static let betTimeout        = "xura_bet_timeout"       // секунды
}
```

### Хэширование пароля (PBKDF2 — аналог Cipher.java)

```swift
// PasswordCipher.swift
import CommonCrypto

struct PasswordCipher {
    static let iterations: UInt32 = 310_000
    static let keyLength: Int = 32  // 256 бит

    static func generateSalt() -> String {
        var bytes = [UInt8](repeating: 0, count: 16)
        SecRandomCopyBytes(kSecRandomDefault, 16, &bytes)
        return bytes.map { String(format: "%02x", $0) }.joined()
    }

    static func hashPassword(_ password: String, salt: String) -> String {
        let passwordData = Array(password.utf8)
        let saltData     = Array(salt.utf8)
        var derivedKey   = [UInt8](repeating: 0, count: keyLength)

        CCKeyDerivationPBKDF(
            CCPBKDFAlgorithm(kCCPBKDF2),
            password, passwordData.count,
            saltData,  saltData.count,
            CCPseudoRandomAlgorithm(kCCPRFHmacAlgSHA256),
            iterations,
            &derivedKey, keyLength
        )

        return derivedKey.map { String(format: "%02x", $0) }.joined()
    }

    static func verify(_ input: String, hash: String, salt: String) -> Bool {
        return hashPassword(input, salt: salt) == hash
    }
}
```

### Шифрование seed (AES-256-GCM — аналог SecureSeedStorage.java)

```swift
// SeedStorage.swift
import CryptoKit

struct SeedStorage {
    static func encrypt(_ seed: String) throws -> String {
        let key       = SymmetricKey(size: .bits256)  // из Keychain SecureEnclave
        let data      = Data(seed.utf8)
        let sealed    = try AES.GCM.seal(data, using: key)
        let combined  = sealed.combined!
        return combined.base64EncodedString()
    }

    static func decrypt(_ base64: String, key: SymmetricKey) throws -> String {
        let data      = Data(base64Encoded: base64)!
        let box       = try AES.GCM.SealedBox(combined: data)
        let decrypted = try AES.GCM.open(box, using: key)
        return String(data: decrypted, encoding: .utf8)!
    }
}

// Ключ хранится в Keychain с флагом .userPresence
// (требует биометрию / пароль при каждом обращении к seed)
```

---

## 7. XRP / Wallet логика

### Библиотека

Используйте официальный XRPL JSON-RPC через `URLSession` — это проще и надёжнее,
чем зависеть от нативной Swift-библиотеки:

```swift
// XrplClient.swift
class XrplClient {
    let rpcUrl: URL

    init(mainnet: Bool) {
        rpcUrl = mainnet
            ? URL(string: "https://s1.ripple.com:51234")!
            : URL(string: "https://s.altnet.rippletest.net:51234/")!
    }

    func accountInfo(address: String) async throws -> AccountInfo { ... }
    func currentFee() async throws -> String { ... }
    func submit(txBlob: String) async throws -> SubmitResult { ... }
}
```

### Генерация кошелька (ed25519)

```swift
// WalletGenerator.swift
import CryptoKit

struct XRPWallet {
    let privateKey: Curve25519.Signing.PrivateKey
    let address:    String
    let seed:       String   // Base58-кодированный

    static func generate() -> XRPWallet {
        let privateKey = Curve25519.Signing.PrivateKey()
        let publicKey  = privateKey.publicKey
        let address    = deriveXRPLAddress(publicKey: publicKey)
        let seed       = encodeBase58Seed(privateKey.rawRepresentation)
        return XRPWallet(privateKey: privateKey, address: address, seed: seed)
    }

    static func restore(from seed: String) throws -> XRPWallet { ... }
}
```

### Баланс

```swift
func getDisplayBalance(address: String, mainnet: Bool) async -> Decimal {
    let client = XrplClient(mainnet: mainnet)
    guard let info = try? await client.accountInfo(address: address) else {
        return 0
    }
    let raw    = Decimal(string: info.balance)! / 1_000_000  // drops → XRP
    let reserve = Decimal(10)                                  // резерв 10 XRP
    return max(raw - reserve, 0)
}
```

### Отправка платежа

```swift
func sendPayment(to: String, amount: Decimal, memo: String?) async throws {
    let drops   = NSDecimalNumber(decimal: amount * 1_000_000).intValue
    let memoHex = memo.map { toHex($0) }

    let tx = Payment(
        account:          wallet.address,
        destination:      to,
        amount:           String(drops),
        memoData:         memoHex,
        fee:              await client.currentFee(),
        sequence:         await client.sequence(for: wallet.address),
        lastLedgerSequence: await client.lastLedger() + 4
    )
    let signed = try wallet.sign(tx)
    try await client.submit(txBlob: signed.txBlob)
}

// HEX-кодирование мемо (аналог toHex в WalletXRP.java)
func toHex(_ text: String) -> String {
    text.utf8.map { String(format: "%02X", $0) }.joined()
}
```

### WebSocket подписка (аналог XrplSocketService)

```swift
// XrplWebSocket.swift
class XrplWebSocket: NSObject, ObservableObject {
    private var task: URLSessionWebSocketTask?

    func connect(mainnet: Bool, address: String, onTransaction: @escaping (XrplTx) -> Void) {
        let url     = mainnet
            ? URL(string: "wss://xrplcluster.com")!
            : URL(string: "wss://s.altnet.rippletest.net:51233")!
        let session = URLSession(configuration: .default)
        task        = session.webSocketTask(with: url)
        task?.resume()

        // Подписка на транзакции аккаунта
        let subscribe = """
        {"command":"subscribe","accounts":["\(address)"]}
        """
        task?.send(.string(subscribe)) { _ in }
        receiveLoop(onTransaction: onTransaction)
    }

    private func receiveLoop(onTransaction: @escaping (XrplTx) -> Void) {
        task?.receive { [weak self] result in
            switch result {
            case .success(.string(let text)):
                if let tx = parseTransaction(text) {
                    DispatchQueue.main.async { onTransaction(tx) }
                }
            default: break
            }
            self?.receiveLoop(onTransaction: onTransaction)
        }
    }
}
```

---

## 8. Игровая логика

### Режимы игры

```swift
// GameMode.swift
enum GameMode {
    case trial   // локальная симуляция, XRP не тратятся
    case live    // реальные XRPL-транзакции
}
```

### Flasher — RouletteWheelView (Canvas)

Аналог кастомного `RouletteWheelView` на Android Canvas:

```swift
// RouletteWheelView.swift
struct RouletteWheelView: View {
    @State private var rotation: Double = 0
    @State private var isSpinning = false
    var targetNumber: Int?
    var onStopped: (() -> Void)?

    var body: some View {
        TimelineView(.animation(minimumInterval: 1/60)) { _ in
            Canvas { ctx, size in
                drawWheel(ctx: ctx, size: size, rotation: rotation)
            }
        }
        .onAppear { startSpinning() }
    }

    // 37 секторов (0–36), европейская рулетка
    // Чёрные числа: 2,4,6,8,10,11,13,15,17,20,22,24,26,28,29,31,33,35
    // Зелёный: 0

    func startSpinning() { /* withAnimation(.linear.repeatForever) */ }
    func stopAtNumber(_ n: Int) { /* вычислить угол, остановить */ }
    func drawWheel(ctx: GraphicsContext, size: CGSize, rotation: Double) { /* рисуем сектора */ }
}
```

### Логика определения результата (Trial Mode)

```swift
// GameCalculator.swift
struct GameCalculator {

    // Чёрные числа рулетки
    static let blackNumbers: Set<Int> = [2,4,6,8,10,11,13,15,17,20,22,24,26,28,29,31,33,35]

    static func colorResult(bet: String, betBlack: Bool) -> (win: Bool, number: Int) {
        let number = Int.random(in: 0...36)
        if number == 0 { return (false, 0) }    // зеро — всегда проигрыш
        let isBlack = blackNumbers.contains(number)
        return (isBlack == betBlack, number)
    }

    static func numberResult(playerNumber: Int) -> (win: Bool, number: Int) {
        let number = Int.random(in: 1...36)
        return (number == playerNumber, number)
    }

    static func rouletteResult(bets: [String: Decimal]) -> (profit: Decimal, winNumber: Int) {
        let winNumber = Int.random(in: 0...36)
        var payout: Decimal = 0
        var totalBet: Decimal = 0
        for (tag, amount) in bets {
            totalBet += amount
            if betHits(tag: tag, number: winNumber) {
                payout += amount * Decimal(multiplier(for: tag))
            }
        }
        return (payout - totalBet, winNumber)
    }

    static func betHits(tag: String, number: Int) -> Bool {
        switch tag {
        case "r":          return !blackNumbers.contains(number) && number != 0
        case "b":          return blackNumbers.contains(number)
        case "o":          return number % 2 == 1
        case "e":          return number % 2 == 0 && number != 0
        case "l":          return number >= 1 && number <= 18
        case "h":          return number >= 19 && number <= 36
        case "d1":         return number >= 1 && number <= 12
        case "d2":         return number >= 13 && number <= 24
        case "d3":         return number >= 25 && number <= 36
        case "c1":         return number % 3 == 1
        case "c2":         return number % 3 == 2
        case "c3":         return number % 3 == 0 && number != 0
        default:
            if tag.hasPrefix("n"), let n = Int(tag.dropFirst()) { return n == number }
            return false
        }
    }

    static func multiplier(for tag: String) -> Int {
        switch tag {
        case "r", "b", "o", "e", "l", "h": return 2
        case "d1", "d2", "d3", "c1", "c2", "c3": return 3
        default: return tag.hasPrefix("n") ? 36 : 0
        }
    }
}
```

### Парсинг ответа сервера (аналог NotifierRun.java)

```swift
// ServerResponseParser.swift
enum GameResult {
    case win(text: String, number: String)
    case lose(text: String, number: String)
    case lotto(date: String)
    case referralCode(String)
    case referralReward(String)
    case unknown
}

func parseServerMemo(_ hexMemo: String) -> GameResult {
    guard let data = Data(hexString: hexMemo),
          let text = String(data: data, encoding: .utf8)?.uppercased()
    else { return .unknown }

    if text.hasPrefix("RLT:") {
        let parts = text.components(separatedBy: ":")
        switch parts[1] {
        case "LOTTO":
            return .lotto(date: parts.count > 3 ? parts[3] : "")
        case "REF":
            if parts[2] == "CODE"   { return .referralCode(parts[3]) }
            if parts[2] == "REWARD" { return .referralReward(parts[3]) }
        default:
            let number  = parts[1]
            let outcome = parts.count > 2 ? parts[2] : ""
            return outcome == "WIN" ? .win(text: text, number: number)
                                    : .lose(text: text, number: number)
        }
    } else {
        let parts  = text.components(separatedBy: ":", maxSplits: 1)
        let cmd    = parts[0]
        let number = parts.count > 1 ? parts[1] : ""
        switch cmd {
        case "WIN", "JKPT", "LOTO": return .win(text: text, number: number)
        case "LOSE":                 return .lose(text: text, number: number)
        default:                     return .unknown
        }
    }
    return .unknown
}
```

---

## 9. Сетевой протокол

Протокол идентичен Android-версии. Все взаимодействия через XRP-транзакции с полем MemoData.

### Адреса серверов

```swift
// NetworkConfig.swift
struct NetworkConfig {
    static let mainnetRpc = "https://s1.ripple.com:51234"
    static let mainnetWss = "wss://xrplcluster.com"
    static let testnetRpc = "https://s.altnet.rippletest.net:51234/"
    static let testnetWss = "wss://s.altnet.rippletest.net:51233"

    // Адреса серверов игр
    static let serverColorRoulette = "rGrEJZaBFYhPGuyM7NiJbJw2yXVB9vJHah"
    static let serverNumber        = "rfcMxSEz4JP8zj65LU5Nw9hKfpEaD6Ss9"

    // Финансовые константы
    static let xrpReserve: Decimal = 10      // резерв активации
    static let minBet: Decimal     = 0.1
    static let maxBet: Decimal     = 100
    static let referralCost: Decimal    = 66
    static let referralRestore: Decimal = 13
}
```

### Исходящие MemoData

```swift
// BetEncoder.swift
func colorBetMemo(betRed: Bool, refCode: String) -> String {
    let color = betRed ? "RED" : "BLK"
    return "BET:\(color):\(refCode)"
}

func numberBetMemo(number: Int, refCode: String) -> String {
    "BET:N:\(number):\(refCode)"
}

func rouletteBetMemo(bets: [String: Decimal], refCode: String) -> String {
    let encoded = bets.map { "\($0.key)@\($0.value)" }.joined(separator: ",")
    return "BET:R:\(encoded):\(refCode)"
}
```

### Входящие ответы — полная таблица

| MemoData | Тип | Действие |
|---|---|---|
| `WIN:{n}` | Победа | колесо → n, экран Win |
| `LOSE:{n}` | Проигрыш | колесо → n, экран Lost |
| `JKPT:{n}` | Джекпот (=WIN) | экран Win |
| `LOTO:{n}` | Лото (=WIN) | экран Win |
| `RLT:{n}:WIN` | Рулетка победа | колесо → n, экран Win |
| `RLT:{n}:LOSE` | Рулетка проигрыш | колесо → n, экран Lost |
| `RLT:LOTTO:DAILY:{дата}` | Лото победа | экран Win |
| `RLT:REF:CODE:{код}` | Реферальный код | экран YourReferral |
| `RLT:REF:REWARD:{код}` | Награда рефереру | уведомление |

### Фильтр входящих транзакций

```swift
// Принять транзакцию если:
func shouldProcess(_ tx: XrplTx) -> Bool {
    tx.destination == wallet.address
    && tx.account != NetworkConfig.serverColorRoulette
    && tx.account != NetworkConfig.serverNumber
    && tx.transactionResult == "tesSUCCESS"
    && tx.memos != nil
}
```

---

## 10. Звук

### Файлы (скопировать из Android res/raw/)

| Файл | Когда |
|---|---|
| `bet.mp3` | Отправка ставки |
| `error.mp3` | Ошибка |
| `flour_of_choice.mp3` | Фон SelectGame (loop) |
| `in_casino.mp3` | Фон игровых экранов (loop) |
| `lost.mp3` | Экран проигрыша |
| `roulette_spin.mp3` | Вращение колеса Flasher (loop) |
| `win.mp3` | Экран победы |

### AVAudioPlayer (аналог MediaPlayer + AudioHelper)

```swift
// AudioManager.swift
import AVFoundation

class AudioManager: ObservableObject {
    static let shared = AudioManager()

    private var bgPlayer: AVAudioPlayer?
    private var sfxPlayers: [String: AVAudioPlayer] = [:]
    @Published var isSoundEnabled = true

    func playBackground(_ name: String, loop: Bool = true) {
        guard isSoundEnabled else { return }
        try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
        try? AVAudioSession.sharedInstance().setActive(true)
        guard let url = Bundle.main.url(forResource: name, withExtension: "mp3") else { return }
        bgPlayer = try? AVAudioPlayer(contentsOf: url)
        bgPlayer?.numberOfLoops = loop ? -1 : 0
        bgPlayer?.volume = 0.5
        bgPlayer?.play()
    }

    func stopBackground() {
        bgPlayer?.stop()
        bgPlayer = nil
    }

    func playSFX(_ name: String) {
        guard isSoundEnabled else { return }
        guard let url = Bundle.main.url(forResource: name, withExtension: "mp3") else { return }
        let player = try? AVAudioPlayer(contentsOf: url)
        sfxPlayers[name] = player
        player?.play()
    }
}
```

---

## 11. Настройки

### Список пунктов (Settings.java → SettingsView.swift)

```swift
// SettingsView.swift
struct SettingsView: View {
    @State private var gameMode:       Bool   = false     // false=TRIAL, true=LIVE
    @State private var biometricOn:    Bool   = false
    @State private var soundOn:        Bool   = true
    @State private var lockTimeout:    Int    = 120       // секунды
    @State private var betInputStyle:  String = "chips"   // "chips" / "slider"
    @State private var betTimeout:     Int    = 120       // секунды

    // Варианты таймаута автоблокировки (как в Android)
    let lockTimeouts = [30, 60, 120, 300, 900]           // 30с, 1м, 2м, 5м, 15м

    // Варианты таймаута ставки
    let betTimeouts  = [60, 120, 180, 300]               // 1м, 2м, 3м, 5м

    // Скрытая DEV-секция: 7 нажатий на заголовок за 3 секунды
    @State private var tapCount = 0
    @State private var lastTapTime: Date = .distantPast
    @State private var showDevSection = false

    var body: some View {
        // 1. GAME MODE
        // 2. SET/CHANGE PASSWORD
        // 3. BIOMETRICS
        // 4. AUTO-LOCK TIMEOUT
        // 5. SOUND
        // 6. SELECT LANGUAGE (10 языков)
        // 7. BECOME A REFERRAL
        // 8. BET INPUT STYLE
        // 9. SERVER RESPONSE TIMEOUT
        // 10. INFO
        // [DEV] Mainnet/Testnet переключатель
        // Footer: "XURA v{version}" + "© {year} Samuil Olegovich"
    }
}
```

### Версия приложения

```swift
// Автоматически из Info.plist
let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "?"
// Отображать как: "XURA v\(version)"
```

### Языки (10 штук)

| Код | Язык | Флаг-иконка |
|---|---|---|
| en | English | ic_flag_en |
| ru | Русский | ic_flag_ru |
| zh | 中文 | ic_flag_zh |
| hi | हिन्दी | ic_flag_hi |
| es | Español | ic_flag_es |
| fr | Français | ic_flag_fr |
| de | Deutsch | ic_flag_de |
| ar | العربية | ic_flag_ar |
| pt | Português | ic_flag_pt |
| bn | বাংলা | ic_flag_bn |

```swift
// Применить локализацию
func applyLocale(_ code: String) {
    UserDefaults.standard.set([code], forKey: "AppleLanguages")
    UserDefaults.standard.synchronize()
    // Требует перезапуска или Bundle override
}
```

---

## 12. Все экраны — детальное описание

### SplashView

```swift
struct SplashView: View {
    @State private var scale: CGFloat = 0
    @State private var opacity: Double = 0

    var body: some View {
        ZStack {
            Color.xuraBlack.ignoresSafeArea()
            XuraLogo()
                .scaleEffect(scale)
                .opacity(opacity)
        }
        .onAppear {
            withAnimation(.spring(response: 0.85, dampingFraction: 0.6)) {
                scale = 1; opacity = 1
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.8) {
                // coordinator.startup()
            }
        }
    }
}
```

### WalletView (MainActivity)

Структура сверху вниз:
1. Логотип XURA (кликабелен → SelectGameView)
2. "YOUR BALANCE" — 11sp, xuraTextTertiary, letterSpacing 0.16
3. Бейдж TESTNET (видим только в testnet, xuraPurple)
4. ProgressView пока загружается → затем баланс 40sp bold
5. Три карточки: SEND (pink), REQUEST (cyan), TRANSACTION HISTORY (gold)
6. Pull-to-refresh

### LostView

```swift
struct LostView: View {
    let message: String   // "СТАВКА НЕ ПРОШЛА" (из NotifierRun/strings)
    let onBack: () -> Void

    var body: some View {
        ZStack {
            Color.xuraBlack.ignoresSafeArea()
            VStack(spacing: 0) {
                Spacer()
                // X-иконка (xuraPink, без tint)
                Image("ic_lost_x")
                    .resizable()
                    .frame(width: 120, height: 120)
                    .padding(.top, 48)

                // ПРОИГРЫШ — gradient 36sp
                Text(LocalizedStringKey("bet_lost"))
                    .font(.montserrat(36, weight: .bold))
                    .overlay(LinearGradient.xuraLostGradient)
                    .mask(Text(LocalizedStringKey("bet_lost")).font(.montserrat(36, weight: .bold)))
                    .padding(.top, 16)

                // Короткое сообщение без рамки
                if !message.isEmpty {
                    Text(message)
                        .font(.xuraBody)
                        .foregroundColor(.xuraTextTertiary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                        .padding(.top, 20)
                }

                Spacer()

                // Кнопка К ИГРАМ — золото, иконка джойстика
                XuraCardButton(
                    title:    LocalizedStringKey("back_to_games"),
                    subtitle: LocalizedStringKey("btn_sub_back_to_games"),
                    icon:     "ic_gamepad",
                    color:    .xuraGold,
                    action:   onBack
                )
                .padding(.bottom, 32)
            }
        }
        .navigationBarBackButtonHidden(true)
    }
}
```

### WinView

```swift
struct WinView: View {
    let message: String   // "ВЫИГРЫШ — 3.5 XRP"
    let onBack: () -> Void

    var body: some View {
        ZStack {
            Color.xuraBlack.ignoresSafeArea()
            VStack(spacing: 0) {
                // Логотип XURA
                XuraLogo()
                    .padding(.top, 32)
                    .padding(.horizontal, 80)

                // ПОБЕДА — signature gradient 44sp
                Text(LocalizedStringKey("bet_won"))
                    .font(.montserrat(44, weight: .bold))
                    .overlay(LinearGradient.xuraSignatureGradient)
                    .mask(Text(LocalizedStringKey("bet_won")).font(.montserrat(44, weight: .bold)))
                    .padding(.top, 12)

                // Сумма выигрыша — без рамки, серый
                if !message.isEmpty {
                    Text(message)
                        .font(.xuraBody)
                        .foregroundColor(.xuraTextTertiary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                        .padding(.top, 12)
                }

                Spacer()

                // Кнопка К ИГРАМ — золото
                XuraCardButton(
                    title:    LocalizedStringKey("back_to_games"),
                    subtitle: LocalizedStringKey("btn_sub_back_to_games"),
                    icon:     "ic_gamepad",
                    color:    .xuraGold,
                    action:   onBack
                )
                .padding(.bottom, 32)
            }
        }
        .navigationBarBackButtonHidden(true)
    }
}
```

### FlasherView

```swift
struct FlasherView: View {
    @StateObject var vm: FlasherViewModel

    var body: some View {
        ZStack {
            Color.xuraBlack.ignoresSafeArea()
            VStack {
                // Заголовок BET WON / BET LOST (с gradient после результата)
                Text(vm.resultTitle)
                    .font(.montserrat(48, weight: .bold))
                    .foregroundColor(vm.resultColor)

                // Колесо рулетки (кастомный Canvas View)
                RouletteWheelView(
                    targetNumber: vm.winNumber,
                    onStopped:    { vm.onWheelStopped() }
                )
                .aspectRatio(1, contentMode: .fit)

                // Число в центре после остановки
                if let n = vm.winNumber {
                    Text("\(n)")
                        .font(.montserrat(68, weight: .bold))
                }

                // Сумма выигрыша / проигрыша
                Text(vm.amountText)
                    .font(.montserrat(18, weight: .bold))
                    .foregroundColor(.xuraCyan)

                // Обратный отсчёт 10 сек
                Text(vm.countdownText)
                    .font(.xuraMicro)
                    .foregroundColor(.xuraTextMuted)
            }
        }
        .onAppear { vm.startGame() }
    }
}
```

### SelectGameView

Три карточки с анимацией «волна подпрыгивания» (roulette→number→color, stagger 100ms, каждые 3.5с):

```swift
struct SelectGameView: View {
    @State private var bounceOffset: [CGFloat] = [0, 0, 0]

    var body: some View {
        VStack {
            XuraLogo()
            Text("SELECT GAME").font(.xuraScreenTitle)
            // Бейдж TRIAL / LIVE
            gameBadge()

            Spacer()

            // Карточки игр (80pt высота)
            XuraCardButton(title: "GUESS THE COLOR",  ..., color: .xuraPink,  ...)
                .offset(y: bounceOffset[2])
            XuraCardButton(title: "GUESS THE NUMBER", ..., color: .xuraCyan,  ...)
                .offset(y: bounceOffset[1])
            XuraCardButton(title: "ROULETTE",         ..., color: .xuraGold,  ...)
                .offset(y: bounceOffset[0])
        }
        .onAppear {
            startBounceAnimation()
            AudioManager.shared.playBackground("flour_of_choice")
        }
        .onDisappear { AudioManager.shared.stopBackground() }
    }

    func startBounceAnimation() {
        // каждые 3.5 сек: roulette(0) → number(1) → color(2)
        // каждый с задержкой 100ms, прыжок -20pt
    }
}
```

---

## Порядок разработки

1. **Проект** — создать новый SwiftUI App в Xcode, настроить Bundle ID, добавить Montserrat
2. **Дизайн-система** — `Colors.swift`, `Typography.swift`, `CardStyles.swift`
3. **Keychain + Шифрование** — `KeychainHelper`, `PasswordCipher`, `SeedStorage`
4. **Защита** — `BiometricManager`, `InactivityGuard`, `JailbreakDetector`
5. **Навигация** — `AppCoordinator`, `MainTabView`
6. **Онбординг + Wallet setup** — 6 экранов создания/восстановления
7. **Главный экран** — `WalletView`, баланс, Send/Receive/History
8. **XRP клиент** — `XrplClient`, `XrplWebSocket`, `WalletXRP`
9. **Игры** — `SelectGameView`, `ColorGameView`, `NumberGameView`, `RouletteGameView`
10. **Flasher** — `RouletteWheelView` (Canvas), `FlasherViewModel`
11. **Win / Lost** — `WinView`, `LostView`
12. **Настройки** — `SettingsView` + все 10 пунктов
13. **Рефералы** — `ReferralView`, `BecomeReferralView`, `YourReferralView`
14. **Звук** — `AudioManager`, подключить все 7 файлов
15. **Локализация** — 10 языков (`Localizable.strings`)
16. **Тест на устройстве** — Xcode → iPhone по кабелю

---

*Документ основан на реальном коде XURA-ANDROID-2026. Версия Android-приложения: 26.7.1*

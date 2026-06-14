# DEV · Переключение сети (MAINNET / TESTNET)

> Только для разработчиков. В продакшн-сборке не убирать —
> карточка скрыта за 7 тапами и не видна обычному пользователю.

---

## Как открыть DEV-секцию

1. Открыть **Настройки** (нижняя навигация)
2. Тапнуть по заголовку **«НАСТРОЙКИ»** / **«SETTINGS»** **7 раз подряд** в течение 3 секунд
3. Внизу страницы появится фиолетовая карточка **DEV · NETWORK**

---

## Настройки в DEV-карточке

| Элемент | Что делает |
|---------|-----------|
| Тоггл MAINNET / TESTNET | Переключает всю сеть: RPC-URL, WebSocket-URL |
| Поле «Roulette server address» | Адрес кошелька бэка для рулетки |
| Поле «Guess Color server address» | Адрес кошелька бэка для «угадай цвет» |
| Поле «Guess Number server address» | Адрес кошелька бэка для «угадай число» |
| Кнопка **SAVE** | Сохраняет адреса в SharedPreferences |
| Кнопка **GET TESTNET XRP** | Вызывает faucet — зачисляет ~1000 XRP на кошелёк игрока (только в TESTNET) |

> Поля адресов можно оставить пустыми — будут использованы дефолтные значения из `StringEnum`.

---

## Что переключается при смене сети

| Компонент | MAINNET | TESTNET |
|-----------|---------|---------|
| JSON-RPC (транзакции) | `https://s1.ripple.com:51234` | `https://s.altnet.rippletest.net:51234/` |
| WebSocket (события) | `wss://xrplcluster.com` | `wss://s.altnet.rippletest.net:51233` |
| Адреса серверов игр | из `StringEnum` | вводятся вручную в DEV-полях |

Переключение применяется **мгновенно** для новых транзакций.
Для WebSocket-подписки требуется **перезапуск приложения** (сокет переподключается при следующем `restartSocket()`).

---

## Получение тестовых XRP (Faucet)

1. Переключить тоггл в **TESTNET**
2. Нажать **GET TESTNET XRP**
3. Подождать 5–10 секунд
4. Вернуться на экран кошелька — баланс обновится

Faucet зачисляет **1000 XRP** на адрес текущего кошелька приложения.
Endpoint: `POST https://faucet.altnet.rippletest.net/accounts`

> Тестовые XRP не имеют реальной ценности.

---

## Адреса серверов

Каждая игра должна иметь **отдельный кошелёк** на сервере:

- **Рулетка** → `NetworkConfig.SERVER_ROULETTE`
- **Угадай цвет** → `NetworkConfig.SERVER_COLOR`
- **Угадай число** → `NetworkConfig.SERVER_NUMBER`

При запуске приложения адреса загружаются из `SharedPreferences` (`NetworkConfig.load()`).
При сохранении в DEV-карточке записываются обратно (`NetworkConfig.save()`).

---

## Архитектура (для разработчика)

```
NetworkConfig.java          ← единственный источник истины
  IS_TESTNET                ← volatile boolean
  SERVER_ROULETTE/COLOR/NUMBER ← volatile String
  getRpcUrl()               ← возвращает URL в зависимости от IS_TESTNET
  getWssUrl()               ← аналогично

WalletXRP.createConnect()   ← читает NetworkConfig.getRpcUrl()
PaymentAndSocketManagerXRPL
  .createNewSocket()        ← читает NetworkConfig.getWssUrl()

RouletteViewModel           ← использует NetworkConfig.SERVER_ROULETTE
GuessColorViewModel         ← использует NetworkConfig.SERVER_COLOR
GuessNumberViewModel        ← использует NetworkConfig.SERVER_NUMBER

MainActivity.onCreate()     ← NetworkConfig.load(preferences)
Settings (DEV-карточка)     ← NetworkConfig.save(preferences)
```

---

## Правила

- **Не пушить** ветки разработки на GitHub — только `main`
- DEV-карточку **не убирать** из кода перед релизом — она скрыта за 7 тапами
- После переключения сети рекомендуется **перезапустить** приложение
- Перед тестированием игры на testnet убедиться, что введён адрес **testnet-кошелька сервера** (mainnet-адрес на testnet не работает)

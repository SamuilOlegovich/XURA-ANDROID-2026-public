#!/usr/bin/env python3
"""XURA Presentation PDF (RU) — dark style matching the app."""

from reportlab.pdfgen import canvas as rl_canvas
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.colors import HexColor, Color, white
from PIL import Image as PILImage
import os

# ── Cyrillic fonts ────────────────────────────────────────────────────────────
pdfmetrics.registerFont(TTFont('Arial',      '/System/Library/Fonts/Supplemental/Arial.ttf'))
pdfmetrics.registerFont(TTFont('Arial-Bold', '/System/Library/Fonts/Supplemental/Arial Bold.ttf'))

PROJ    = "/Users/samuilolegovich/iDev/XURA-ANDROID-2026"
SCREENS = f"{PROJ}/screenshots"
OUT     = f"{PROJ}/XURA_Presentation_RU.pdf"

W, H = 960, 540   # 16:9 in points

# ── Palette ───────────────────────────────────────────────────────────────────
BG      = HexColor('#0A0A14')
CARD    = HexColor('#13132A')
CARD2   = HexColor('#1C1C3A')
CARD3   = HexColor('#0D1F12')
CYAN    = HexColor('#00E5FF')
MAGENTA = HexColor('#FF006E')
ORANGE  = HexColor('#FF6B35')
PURPLE  = HexColor('#8B3FDE')
BLUE    = HexColor('#1E6FFF')
GREEN   = HexColor('#00E676')
YELLOW  = HexColor('#FFD600')
WHITE   = white
GRAY    = HexColor('#9090A8')
DIM     = HexColor('#404055')


# ── Helpers ───────────────────────────────────────────────────────────────────

def lerp(a, b, t):
    return a + (b - a) * t

def hgrad(c, x, y, w, h, c1, c2):
    steps = 80
    for i in range(steps):
        t = i / (steps - 1)
        col = Color(lerp(c1.red, c2.red, t),
                    lerp(c1.green, c2.green, t),
                    lerp(c1.blue, c2.blue, t))
        c.setFillColor(col)
        c.rect(x + i * w / steps, y, w / steps + 1, h, fill=1, stroke=0)

def vgrad(c, x, y, w, h, c1, c2):
    steps = 60
    for i in range(steps):
        t = i / (steps - 1)
        col = Color(lerp(c1.red, c2.red, t),
                    lerp(c1.green, c2.green, t),
                    lerp(c1.blue, c2.blue, t))
        c.setFillColor(col)
        c.rect(x, y + i * h / steps, w, h / steps + 1, fill=1, stroke=0)

def bg(c):
    c.setFillColor(BG)
    c.rect(0, 0, W, H, fill=1, stroke=0)

def top_bar(c, col1, col2, thickness=6):
    hgrad(c, 0, H - thickness, W, thickness, col1, col2)

def bottom_bar(c, col1=None, col2=None, thickness=4):
    hgrad(c, 0, 0, W, thickness, col1 or ORANGE, col2 or MAGENTA)

def accent_line(c, x, y, w=100, h=3, color=None):
    c.setFillColor(color or CYAN)
    c.rect(x, y, w, h, fill=1, stroke=0)

def rounded_card(c, x, y, w, h, color=None, radius=10, stroke_color=None):
    c.setFillColor(color or CARD)
    if stroke_color:
        c.setStrokeColor(stroke_color)
        c.setLineWidth(1.5)
        c.roundRect(x, y, w, h, radius, fill=1, stroke=1)
        c.setLineWidth(1)
    else:
        c.roundRect(x, y, w, h, radius, fill=1, stroke=0)

def slide_footer(c, n, total):
    c.setFont("Arial", 9)
    c.setFillColor(DIM)
    c.drawString(30, 14, "XURA — XRP Кошелёк и Блокчейн-игровая платформа")
    c.drawRightString(W - 24, 14, f"{n}  /  {total}")

def title_text(c, text, x, y, color=None, size=26):
    c.setFont("Arial-Bold", size)
    c.setFillColor(color or WHITE)
    c.drawString(x, y, text)
    accent_line(c, x, y - 10, min(len(text) * size * 0.52, 200), 3, color or CYAN)

def img(c, filename, x, y, w, frame_color=None):
    path = f"{SCREENS}/{filename}" if not filename.startswith('/') else filename
    if not os.path.exists(path):
        return 0
    pil = PILImage.open(path)
    iw, ih = pil.size
    h = int(w * ih / iw)
    fc = frame_color or CARD2
    rounded_card(c, x - 5, y - 5, w + 10, h + 10, fc, 12, DIM)
    c.drawImage(path, x, y, width=w, height=h, mask='auto')
    return h


# ── СЛАЙД 1 — Титульный ───────────────────────────────────────────────────────

def slide_01_title(c, n, T):
    c.setFillColor(HexColor('#000000'))
    c.rect(0, 0, W, H, fill=1, stroke=0)
    top_bar(c, PURPLE, CYAN, 8)
    bottom_bar(c, ORANGE, MAGENTA, 5)

    hgrad(c, W // 2 - 130, H // 2 - 10, 260, 4, PURPLE, CYAN)

    # Logo (cropped splash)
    logo_path = f"{SCREENS}/01_splash.png"
    if os.path.exists(logo_path):
        pil = PILImage.open(logo_path)
        iw, ih = pil.size
        cropped = pil.crop((int(iw * 0.08), int(ih * 0.27), int(iw * 0.92), int(ih * 0.66)))
        tmp = "/tmp/_xura_logo.png"
        cropped.save(tmp)
        lw = 220
        lh = int(lw * cropped.height / cropped.width)
        c.drawImage(tmp, W // 2 - lw // 2, H // 2 + 10, lw, lh, mask='auto')

    c.setFont("Arial", 18)
    c.setFillColor(CYAN)
    c.drawCentredString(W // 2, H // 2 - 40, "XRP Кошелёк  ·  Блокчейн-игровая платформа")

    c.setFont("Arial", 12)
    c.setFillColor(GRAY)
    c.drawCentredString(W // 2, H // 2 - 62,
        "Некастодиальный  ·  Игры on-chain  ·  10 языков  ·  Android  ·  С 2022 года")

    # Badge
    rounded_card(c, W // 2 - 115, 38, 230, 26, CARD2, 8)
    c.setFont("Arial-Bold", 11)
    c.setFillColor(CYAN)
    c.drawCentredString(W // 2, 49, "v26.6.17  |  minSdk 28  |  Java · MVVM · Hilt")

    slide_footer(c, n, T)


# ── СЛАЙД 2 — Что такое XURA ─────────────────────────────────────────────────

def slide_02_what(c, n, T):
    bg(c)
    top_bar(c, BLUE, CYAN)
    bottom_bar(c)

    title_text(c, "Что такое XURA?", 50, H - 72, CYAN, 26)

    # Left description card
    rounded_card(c, 40, 55, 380, H - 145, CARD, 12)
    desc = [
        "XURA — некастодиальный мобильный кошелёк",
        "для XRP Ledger в сочетании с игровой",
        "платформой с проверкой на блокчейне.",
        "",
        "Каждая ставка — реальная XRP-транзакция.",
        "Выигрыш возвращается прямо на адрес",
        "вашего кошелька — без запросов на вывод,",
        "без скрытых балансов,",
        "без доверия посреднику.",
        "",
        "Кошелёк работает на XRPL Mainnet",
        "прямо сейчас. Игровой сервер — скоро.",
    ]
    y = H - 130
    for line in desc:
        if line:
            c.setFont("Arial", 12.5)
            c.setFillColor(WHITE if line.startswith("XURA") else GRAY)
            c.drawString(58, y, line)
        y -= 22

    # Right: 3 pillar cards
    pillars = [
        (CYAN,    "🔐  XRPL Кошелёк",        "Некастодиальный · Mainnet · Защита seed"),
        (MAGENTA, "🎲  Казино on-chain",       "Рулетка · Угадай цвет · Угадай число"),
        (ORANGE,  "👥  Реферальная система",   "Регистрация on-chain · Бонусы · Розыгрыши"),
    ]
    py = H - 125
    for col, title, sub in pillars:
        rounded_card(c, 445, py - 55, 475, 64, CARD2, 10)
        c.setFillColor(col)
        c.rect(445, py - 55, 5, 64, fill=1, stroke=0)
        c.setFont("Arial-Bold", 14)
        c.setFillColor(col)
        c.drawString(462, py - 18, title)
        c.setFont("Arial", 11.5)
        c.setFillColor(GRAY)
        c.drawString(462, py - 38, sub)
        py -= 80

    # Screenshot preview
    img(c, "13_main_wallet.png", 835, 62, 80)

    slide_footer(c, n, T)


# ── СЛАЙД 3 — Почему XURA (сравнение) ────────────────────────────────────────

def slide_03_why(c, n, T):
    bg(c)
    top_bar(c, PURPLE, MAGENTA)
    bottom_bar(c)

    title_text(c, "Почему XURA?", 50, H - 72, MAGENTA, 26)

    headers = ["Характеристика", "Обычное казино", "XURA ✓"]
    rows = [
        ("Хранение средств",  "Платформа держит ваши деньги",  "Пользователь — некастодиально"),
        ("Депозиты",          "Внутренний баланс (off-chain)",  "Прямая XRPL-транзакция"),
        ("Выводы средств",    "Запрос обязателен, могут заморозить", "Не нужны — средства on-chain"),
        ("Прозрачность",      "Ограничена / непрозрачный RNG",  "Каждая ставка проверяема"),
        ("Приватный ключ",    "Не применимо",                   "Никогда не покидает устройство"),
        ("Риск потери средств","Риск банкротства платформы",    "Ноль — всегда на XRPL"),
    ]

    cx = [50, 270, 520]
    cw = [215, 245, 360]
    row_h = 44
    table_top = H - 105

    # Header
    rounded_card(c, 40, table_top - 28, sum(cw) + 30, 34, HexColor('#1C0A30'), 6)
    for i, (hdr, x) in enumerate(zip(headers, cx)):
        c.setFont("Arial-Bold", 12)
        c.setFillColor(MAGENTA if i == 0 else (GREEN if i == 2 else GRAY))
        c.drawString(x, table_top - 16, hdr)

    for ri, (feat, bad, good) in enumerate(rows):
        ry = table_top - 30 - (ri + 1) * row_h
        bg_col = CARD if ri % 2 == 0 else CARD2
        rounded_card(c, 40, ry, sum(cw) + 30, row_h - 3, bg_col, 5)

        c.setFont("Arial-Bold", 11.5)
        c.setFillColor(WHITE)
        c.drawString(cx[0], ry + 14, feat)

        c.setFont("Arial", 11.5)
        c.setFillColor(HexColor('#FF6666'))
        c.drawString(cx[1], ry + 14, bad)

        c.setFont("Arial-Bold", 11.5)
        c.setFillColor(GREEN)
        c.drawString(cx[2], ry + 14, good)

    # Side badge — right edge 755+165=920
    rounded_card(c, 755, 55, 165, 95, CARD3, 10, GREEN)
    accent_line(c, 755, 55, 165, 4, GREEN)
    c.setFont("Arial-Bold", 11)
    c.setFillColor(GREEN)
    c.drawCentredString(838, 128, "Мини-экосистема")
    c.setFont("Arial", 11)
    c.setFillColor(GRAY)
    for i, t in enumerate(["• XRPL Кошелёк", "• Казино on-chain", "• Реферальная платформа"]):
        c.drawString(770, 108 - i * 17, t)

    slide_footer(c, n, T)


# ── СЛАЙД 4 — Видение ─────────────────────────────────────────────────────────

def slide_04_vision(c, n, T):
    bg(c)
    top_bar(c, BLUE, PURPLE)
    bottom_bar(c)

    title_text(c, "Видение: будущее игр на блокчейне", 50, H - 72, PURPLE, 23)

    # Central statement
    rounded_card(c, 40, H - 165, W - 80, 75, CARD2, 12)
    accent_line(c, 40, H - 90, W - 80, 4, PURPLE)
    c.setFont("Arial-Bold", 15)
    c.setFillColor(WHITE)
    c.drawCentredString(W // 2, H - 120,
        "XURA — это proof-of-concept, а не просто продукт.")
    c.setFont("Arial", 12.5)
    c.setFillColor(GRAY)
    c.drawCentredString(W // 2, H - 142,
        "Это эталонная реализация того, как должны работать игры на блокчейне —")
    c.drawCentredString(W // 2, H - 158,
        "прозрачно, некастодиально и проверяемо каждым на любом блокчейне.")

    # Chains row
    chains = [
        (HexColor('#627EEA'), "Ethereum", "EVM · смарт-контракты"),
        (HexColor('#9945FF'), "Solana",   "SOL · SPL токены"),
        (HexColor('#00B0FF'), "XRPL ★",  "Эталонная реализация"),
        (HexColor('#7B4FFF'), "TON",      "Экосистема Telegram"),
        (HexColor('#E84142'), "Stellar",  "XLM · схожая архитектура"),
    ]
    # n_chains cards × cw_card + (n_chains-1) gaps × 10 = 880 → x=40..920
    cx = 40
    n_chains = len(chains)
    cw_card = (W - 80 - (n_chains - 1) * 10) // n_chains   # = 168
    for col, name, sub in chains:
        is_xrpl = "★" in name
        rounded_card(c, cx, H - 265, cw_card, 85, CARD2, 10,
                     col if is_xrpl else None)
        if is_xrpl:
            rounded_card(c, cx, H - 265, cw_card, 85, HexColor('#0A1830'), 10)
        c.setFillColor(col)
        c.rect(cx, H - 265, cw_card, 5, fill=1, stroke=0)
        c.setFont("Arial-Bold", 13)
        c.setFillColor(col)
        c.drawCentredString(cx + cw_card // 2, H - 225, name)
        c.setFont("Arial", 10)
        c.setFillColor(WHITE if is_xrpl else GRAY)
        c.drawCentredString(cx + cw_card // 2, H - 243, sub)
        cx += cw_card + 10

    # Three principles
    principles = [
        (CYAN,    "Нет аккаунта казино",
                  "Пользователь всегда контролирует средства.\nНет депозита. Нет запросов на вывод."),
        (MAGENTA, "Каждая ставка = реальная tx",
                  "Ставки — нативные транзакции блокчейна\nс проверяемыми мемо."),
        (GREEN,   "Сервер не может смухлевать",
                  "Каждый результат подписан. Итоги\nпроверяемы on-chain любым желающим."),
    ]
    # 3 blocks × 280 + 2 gaps × 20 = 880 → matches central card (x=40..920)
    px = 40
    for col, title, desc in principles:
        rounded_card(c, px, 52, 280, 115, CARD2, 12)
        c.setFillColor(col)
        c.rect(px, 52, 280, 4, fill=1, stroke=0)
        c.setFont("Arial-Bold", 13)
        c.setFillColor(col)
        c.drawCentredString(px + 140, 140, title)
        c.setFont("Arial", 11)
        c.setFillColor(GRAY)
        for i, line in enumerate(desc.split('\n')):
            c.drawCentredString(px + 140, 116 - i * 18, line)
        px += 300

    slide_footer(c, n, T)


# ── СЛАЙД 5 — Возможности кошелька ───────────────────────────────────────────

def slide_04_wallet(c, n, T):
    bg(c)
    top_bar(c, BLUE, CYAN)
    bottom_bar(c)

    title_text(c, "Возможности кошелька", 50, H - 72, CYAN, 26)

    features = [
        (CYAN, "Создать кошелёк",
               "Новая пара ключей XRPL — seed показан один раз, зашифрован в Android Keystore AES-256-GCM"),
        (CYAN, "Восстановить кошелёк",
               "Импорт любого аккаунта XRPL через 16-словную seed-фразу"),
        (CYAN, "Отправить XRP",
               "Ввод вручную или QR-сканирование · тег назначения · диалог подтверждения"),
        (CYAN, "Получить XRP",
               "Ваш адрес как QR-код + копирование одним касанием"),
        (CYAN, "История транзакций",
               "Список в реальном времени через WebSocket к wss://xrplcluster.com"),
        (CYAN, "Баланс в реальном времени",
               "Постоянный WebSocket — баланс обновляется без перезагрузки"),
        (CYAN, "Режим Testnet",
               "Переключение на XRPL Altnet для разработки без реальных средств"),
    ]

    x = 50
    y = H - 120
    for col, title, desc in features:
        rounded_card(c, x - 8, y - 26, 575, 38, CARD2, 7)
        c.setFillColor(col)
        c.rect(x - 8, y - 26, 4, 38, fill=1, stroke=0)
        c.setFont("Arial-Bold", 12)
        c.setFillColor(WHITE)
        c.drawString(x + 6, y - 4, title)
        c.setFont("Arial", 11)
        c.setFillColor(GRAY)
        c.drawString(x + 6, y - 18, desc)
        y -= 48

    # Screenshots column — last frame right edge: 830+85+5=920
    img(c, "14_send_payment.png",  640, 295, 85)
    img(c, "15_receive_qr.png",    735, 295, 85)
    img(c, "16_tx_history.png",    830, 295, 85)
    img(c, "13_main_wallet.png",   680, 55,  120)

    slide_footer(c, n, T)


# ── СЛАЙД 6 — Безопасность ────────────────────────────────────────────────────

def slide_05_security(c, n, T):
    bg(c)
    top_bar(c, PURPLE, BLUE)
    bottom_bar(c)

    title_text(c, "Безопасность", 50, H - 72, PURPLE, 26)

    items = [
        (PURPLE, "Android Keystore AES-256-GCM",
                 "Seed зашифрован в TEE — никогда не хранится в открытом виде"),
        (PURPLE, "Биометрическая разблокировка",
                 "Отпечаток / Face ID через androidx.biometric (только BIOMETRIC_STRONG)"),
        (PURPLE, "Пароль приложения (PBKDF2)",
                 "Опциональный PIN/пароль — соль+хеш, никогда не хранится открытым текстом"),
        (PURPLE, "Автоблокировка по бездействию",
                 "Экран блокируется через настраиваемый период бездействия"),
        (PURPLE, "Обнаружение root и подделки",
                 "Предупреждает, если устройство рутовано или APK переподписан чужим ключом"),
        (PURPLE, "FLAG_SECURE",
                 "Запрещает скриншоты ОС на экранах seed и ввода пароля"),
        (PURPLE, "Безопасность буфера обмена",
                 "Диалог подтверждения перед вставкой — защита от подмены адреса"),
    ]

    x = 50
    y = H - 120
    for col, title, desc in items:
        rounded_card(c, x - 8, y - 26, 575, 38, CARD2, 7)
        c.setFillColor(col)
        c.rect(x - 8, y - 26, 4, 38, fill=1, stroke=0)
        c.setFont("Arial-Bold", 12)
        c.setFillColor(WHITE)
        c.drawString(x + 6, y - 4, title)
        c.setFont("Arial", 11)
        c.setFillColor(GRAY)
        c.drawString(x + 6, y - 18, desc)
        y -= 48

    # Security screenshots — x=655,745,835 w=80; last frame right: 835+80+5=920
    img(c, "32_security_root_warning.png", 655, 80, 80)
    img(c, "10_set_password.png",          745, 80, 80)
    img(c, "11_enter_password.png",        835, 80, 80)

    # Shield decoration — x=650, w=270, right=920; center=785
    rounded_card(c, 650, 360, 270, 120, HexColor('#12082A'), 12, PURPLE)
    c.setFont("Arial-Bold", 36)
    c.setFillColor(PURPLE)
    c.drawCentredString(785, 440, "🔐")
    c.setFont("Arial-Bold", 13)
    c.setFillColor(WHITE)
    c.drawCentredString(785, 408, "Ваши ключи. Ваши монеты.")
    c.setFont("Arial", 11)
    c.setFillColor(GRAY)
    c.drawCentredString(785, 388, "Seed никогда не касается сервера")

    slide_footer(c, n, T)


# ── СЛАЙД 7 — Игры ────────────────────────────────────────────────────────────

def slide_06_games(c, n, T):
    bg(c)
    top_bar(c, ORANGE, MAGENTA)
    bottom_bar(c)

    title_text(c, "Игры  *(бэкенд скоро)*", 50, H - 72, ORANGE, 24)

    c.setFont("Arial", 12)
    c.setFillColor(GRAY)
    c.drawString(50, H - 95,
        "Каждая ставка = реальная транзакция XRPL  ·  Сервер отвечает подписанным платежом  ·  Результат проверяем on-chain")

    # Three game cards
    games = [
        (MAGENTA, "Угадай цвет",        "Красное или чёрное",          "×2  выплата",  "22_guess_color.png"),
        (ORANGE,  "Угадай число",        "Выбери число 1–36",           "×36 выплата",  "23_guess_number.png"),
        (YELLOW,  "Европейская рулетка", "Полный стол: прямая,\nкрасное/чёрное, дюжины…", "×2–×36", "24_roulette.png"),
    ]

    # 3 cards × 280 + 2 gaps × 20 = 880 → x=40..920, margins=40 each side
    gx = 40
    for col, title, desc, payout, scr in games:
        rounded_card(c, gx, 55, 280, H - 165, CARD2, 12, col)
        accent_line(c, gx, H - 112, 280, 4, col)

        c.setFont("Arial-Bold", 14)
        c.setFillColor(col)
        c.drawCentredString(gx + 140, H - 130, title)

        c.setFont("Arial", 11)
        c.setFillColor(GRAY)
        for i, line in enumerate(desc.split('\n')):
            c.drawCentredString(gx + 140, H - 152 - i * 16, line)

        rounded_card(c, gx + 88, H - 195, 105, 26, col, 8)
        c.setFont("Arial-Bold", 14)
        c.setFillColor(BG)
        c.drawCentredString(gx + 140, H - 183, payout)

        img(c, scr, gx + 80, 68, 120)
        gx += 300

    slide_footer(c, n, T)


# ── СЛАЙД 8 — Скриншоты приложения ───────────────────────────────────────────

def slide_07_gallery(c, n, T):
    bg(c)
    top_bar(c, CYAN, GREEN)
    bottom_bar(c)

    title_text(c, "Скриншоты приложения", 50, H - 72, CYAN, 26)

    TOP_W   = 96
    TOP_H   = int(TOP_W * 540 / 249)   # 208
    TOP_GAP = 18
    TOP_X0  = (W - (5 * TOP_W + 4 * TOP_GAP)) // 2
    TOP_Y   = 188

    top_shots = [
        ("01_splash.png",      "Сплэш"),
        ("13_main_wallet.png", "Кошелёк"),
        ("14_send_payment.png","Отправить XRP"),
        ("24_roulette.png",    "Рулетка"),
        ("18_settings.png",    "Настройки"),
    ]
    for i, (fn, lbl) in enumerate(top_shots):
        sx = TOP_X0 + i * (TOP_W + TOP_GAP)
        rounded_card(c, sx - 5, TOP_Y - 5, TOP_W + 10, TOP_H + 10, CARD2, 10, DIM)
        c.drawImage(f"{SCREENS}/{fn}", sx, TOP_Y, width=TOP_W, height=TOP_H, mask='auto')
        c.setFont("Arial", 9)
        c.setFillColor(GRAY)
        c.drawCentredString(sx + TOP_W // 2, TOP_Y - 13, lbl)

    SCR_W  = 52
    SCR_H  = int(SCR_W * 540 / 249)    # 113
    C_W    = 230
    C_H    = SCR_H + 16                 # 129
    C_GAP  = 28
    C_X0   = (W - (3 * C_W + 2 * C_GAP)) // 2
    C_Y    = 38

    game_cards = [
        (ORANGE,  "33_roulette_wheel.png", "Колесо рулетки",    "Ожидание результата"),
        (GREEN,   "34_bet_won.png",        "СТАВКА ВЫИГРАНА",    "Поздравляем!"),
        (MAGENTA, "35_bet_lost.png",       "СТАВКА ПРОИГРАНА",   "Удачи в следующий раз"),
    ]
    for j, (col, fn, title, sub) in enumerate(game_cards):
        cx = C_X0 + j * (C_W + C_GAP)
        rounded_card(c, cx, C_Y, C_W, C_H, CARD2, 10)
        c.setFillColor(col)
        c.rect(cx, C_Y, 4, C_H, fill=1, stroke=0)

        sx = cx + 14
        rounded_card(c, sx - 3, C_Y + 7 - 3, SCR_W + 6, SCR_H + 6, CARD, 8, DIM)
        c.drawImage(f"{SCREENS}/{fn}", sx, C_Y + 7,
                    width=SCR_W, height=SCR_H, mask='auto')

        tx = sx + SCR_W + 12
        c.setFont("Arial-Bold", 12)
        c.setFillColor(col)
        c.drawString(tx, C_Y + C_H - 34, title)
        c.setFont("Arial", 10)
        c.setFillColor(GRAY)
        c.drawString(tx, C_Y + C_H - 52, sub)

    slide_footer(c, n, T)


# ── СЛАЙД 9 — Реферальная система ────────────────────────────────────────────

def slide_08_referral(c, n, T):
    bg(c)
    top_bar(c, GREEN, CYAN)
    bottom_bar(c)

    title_text(c, "Реферальная система", 50, H - 72, GREEN, 26)

    steps = [
        (GREEN,  "1", "Регистрация on-chain",
                       "Оплатите 66 XRP → записано в XRP Ledger.\n13 XRP возвращаются при выходе."),
        (CYAN,   "2", "Поделитесь кодом",
                       "Приглашайте пользователей — мест для приглашённых неограниченно.\nЧисло реферальных позиций ограничено."),
        (YELLOW, "3", "Зарабатывайте вместе",
                       "Вы и рефералы получаете бонусные жизни за ставки.\nУчаствуйте в ежедневном розыгрыше призов."),
        (ORANGE, "4", "Восстановите в любой момент",
                       "Потеряли код? Восстановите за 13 XRP.\nВсё on-chain — прозрачно и проверяемо."),
    ]

    x = 40
    y = H - 115
    for col, num, title, desc in steps:
        rounded_card(c, x, y - 58, 440, 68, CARD2, 10)
        rounded_card(c, x + 8, y - 48, 36, 36, col, 18)
        c.setFont("Arial-Bold", 18)
        c.setFillColor(BG)
        c.drawCentredString(x + 26, y - 27, num)
        c.setFont("Arial-Bold", 13)
        c.setFillColor(col)
        c.drawString(x + 56, y - 18, title)
        c.setFont("Arial", 11)
        c.setFillColor(GRAY)
        for i, line in enumerate(desc.split('\n')):
            c.drawString(x + 56, y - 34 - i * 14, line)
        y -= 80

    # Screenshots
    img(c, "28_referral_enter.png",  520, 230, 90)
    img(c, "29_become_referral.png", 620, 230, 90)
    img(c, "30_your_referral.png",   720, 230, 90)
    img(c, "36_info_referral.png",   820, 230, 90)

    # Fee card — 510+410=920
    rounded_card(c, 510, 55, 410, 165, CARD3, 12, GREEN)
    c.setFont("Arial-Bold", 13)
    c.setFillColor(GREEN)
    c.drawCentredString(715, 200, "Разбивка комиссий")
    table = [
        ("Регистрационный взнос",   "66 XRP",    GREEN),
        ("Возврат при выходе",      "13 XRP",    YELLOW),
        ("Итого расходов",          "53 XRP",    ORANGE),
        ("Реферальных слотов",      "Ограничено", CYAN),
        ("Приглашённых пользователей", "∞",       CYAN),
    ]
    ty = 178
    for label, val, col in table:
        c.setFont("Arial", 11)
        c.setFillColor(GRAY)
        c.drawString(530, ty, label)
        c.setFont("Arial-Bold", 11)
        c.setFillColor(col)
        c.drawRightString(905, ty, val)
        ty -= 20

    slide_footer(c, n, T)


# ── СЛАЙД 10 — Архитектура ────────────────────────────────────────────────────

def slide_09_arch(c, n, T):
    bg(c)
    top_bar(c, BLUE, PURPLE)
    bottom_bar(c)

    title_text(c, "Архитектура и стек", 50, H - 72, BLUE, 26)

    # Left: layers
    layers = [
        (CYAN,   "Представление (Activities)", "31 экран · Java"),
        (BLUE,   "ViewModel + LiveData",        "MVVM · реактивное состояние"),
        (PURPLE, "Dagger Hilt DI",             "AppModule · скоупная инъекция"),
        (ORANGE, "XRPL Клиент",               "xrpl4j-core 3.3.0 · WebSocket · Retrofit"),
        (GREEN,  "Слой безопасности",          "Android Keystore · BiometricHelper · Cipher"),
        (YELLOW, "Async Runnables",            "BalanceRunnable · SubscriberRunnable · Notifier"),
    ]
    y = H - 118
    for col, title, sub in layers:
        rounded_card(c, 40, y - 28, 430, 40, CARD2, 8)
        c.setFillColor(col)
        c.rect(40, y - 28, 4, 40, fill=1, stroke=0)
        c.setFont("Arial-Bold", 12)
        c.setFillColor(WHITE)
        c.drawString(56, y - 4, title)
        c.setFont("Arial", 11)
        c.setFillColor(GRAY)
        c.drawString(56, y - 19, sub)
        y -= 52

    # Right: libs grid
    libs = [
        (CYAN,    "xrpl4j 3.3.0",        "XRPL транзакции"),
        (BLUE,    "Java-WebSocket 1.5.7", "Баланс в реальном времени"),
        (PURPLE,  "Hilt 2.51.1",          "Внедрение зависимостей"),
        (GREEN,   "biometric 1.2.0",      "Отпечаток / FaceID"),
        (ORANGE,  "Retrofit 2.11.0",      "REST + JSON"),
        (YELLOW,  "MLKit barcodes",       "Сканирование QR-кода"),
        (CYAN,    "ZXing 3.3.2",          "Генерация QR-кода"),
        (MAGENTA, "security-crypto",      "EncryptedSharedPrefs"),
    ]
    # Lib grid — step=220, w=210: x=490..700 and 710..920
    lx, ly = 490, H - 118
    for i, (col, lib, desc) in enumerate(libs):
        row, col_idx = i // 2, i % 2
        cx_ = lx + col_idx * 220
        cy_ = ly - row * 60
        rounded_card(c, cx_, cy_ - 30, 210, 44, CARD2, 8)
        c.setFillColor(col)
        c.setFont("Arial-Bold", 11)
        c.drawString(cx_ + 10, cy_ - 8, lib)
        c.setFont("Arial", 10)
        c.setFillColor(GRAY)
        c.drawString(cx_ + 10, cy_ - 22, desc)

    # Test count badge — 490+430=920
    rounded_card(c, 490, 55, 430, 38, CARD3, 8, GREEN)
    c.setFont("Arial-Bold", 13)
    c.setFillColor(GREEN)
    c.drawString(510, 70, "✓  79 автоматических тестов  ·  Java · MVVM · Dagger Hilt · minSdk 28")

    slide_footer(c, n, T)


# ── СЛАЙД 11 — Дорожная карта ─────────────────────────────────────────────────

def slide_10_roadmap(c, n, T):
    bg(c)
    top_bar(c, CYAN, GREEN)
    bottom_bar(c)

    title_text(c, "Дорожная карта", 50, H - 72, GREEN, 26)

    done = [
        "Некастодиальный XRP кошелёк — готов к Mainnet",
        "Биометрия + пароль (PBKDF2 + Keystore AES-256)",
        "QR отправка / получение + сканер камеры",
        "История транзакций в реальном времени (WebSocket)",
        "Европейская рулетка — полный стол UI + протокол",
        "Угадай цвет / Угадай число — UI + протокол",
        "Реферальная система — on-chain регистрация и управление",
        "Локализация на 10 языков (EN, RU, ZH, HI, ES, FR, DE, AR, PT, BN)",
        "79 автоматических тестов",
    ]
    upcoming = [
        "Запуск игрового сервера  ← в ближайшие недели",
        "Push-уведомления для входящих платежей",
        "Публикация в Google Play",
    ]

    # Done column
    rounded_card(c, 40, 52, 450, H - 140, CARD2, 12)
    c.setFont("Arial-Bold", 12)
    c.setFillColor(GREEN)
    c.drawString(58, H - 108, "✓  Выполнено")
    accent_line(c, 58, H - 116, 110, 2, GREEN)
    y = H - 135
    for item in done:
        c.setFont("Arial", 11)
        c.setFillColor(WHITE)
        c.drawString(62, y, "✓")
        c.setFillColor(GRAY)
        c.drawString(78, y, item)
        y -= 28

    # Upcoming column — 510+410=920
    rounded_card(c, 510, 52, 410, H - 140, CARD2, 12)
    c.setFont("Arial-Bold", 12)
    c.setFillColor(CYAN)
    c.drawString(528, H - 108, "⟳  Скоро")
    accent_line(c, 528, H - 116, 80, 2, CYAN)
    y = H - 145
    for item in upcoming:
        rounded_card(c, 520, y - 16, 390, 32, HexColor('#0A1A2A'), 6)
        c.setFont("Arial-Bold", 11)
        c.setFillColor(CYAN if "сервера" in item.lower() else YELLOW)
        c.drawString(534, y - 3, "→")
        c.setFont("Arial", 11)
        c.setFillColor(WHITE)
        c.drawString(550, y - 3, item)
        y -= 46

    slide_footer(c, n, T)


# ── СЛАЙД 12 — Контакты + Отказ от ответственности ───────────────────────────

def slide_11_final(c, n, T):
    c.setFillColor(HexColor('#000000'))
    c.rect(0, 0, W, H, fill=1, stroke=0)
    top_bar(c, ORANGE, MAGENTA)
    bottom_bar(c, PURPLE, CYAN)

    # Logo small
    logo_path = f"{SCREENS}/01_splash.png"
    if os.path.exists(logo_path):
        pil = PILImage.open(logo_path)
        iw, ih = pil.size
        cropped = pil.crop((int(iw * 0.08), int(ih * 0.27), int(iw * 0.92), int(ih * 0.66)))
        tmp = "/tmp/_xura_logo2.png"
        cropped.save(tmp)
        lw = 150
        lh = int(lw * cropped.height / cropped.width)
        c.drawImage(tmp, W // 2 - lw // 2, H // 2 + 35, lw, lh, mask='auto')

    c.setFont("Arial", 13)
    c.setFillColor(CYAN)
    c.drawCentredString(W // 2, H // 2 + 18, "Создано с ♥ на XRP Ledger  ·  С 2022 года")

    # Contact cards
    contacts = [
        (CYAN,    "GitHub",  "SamuilOlegovich"),
        (MAGENTA, "Email",   "samuilolegovich@gmail.com"),
    ]
    cx = W // 2 - 230
    for col, label, val in contacts:
        rounded_card(c, cx, H // 2 - 42, 210, 38, CARD2, 8)
        c.setFont("Arial-Bold", 11)
        c.setFillColor(col)
        c.drawString(cx + 12, H // 2 - 14, label)
        c.setFont("Arial", 11)
        c.setFillColor(WHITE)
        c.drawString(cx + 12, H // 2 - 30, val)
        cx += 250

    # Disclaimer box — x=40, w=880, right=920
    rounded_card(c, 40, 55, W - 80, 80, HexColor('#14080A'), 10, ORANGE)
    c.setFont("Arial-Bold", 10)
    c.setFillColor(ORANGE)
    c.drawString(58, 115, "Отказ от ответственности:")
    disc = ("XURA не предоставляет финансовых или инвестиционных рекомендаций. "
            "Стоимость криптовалют волатильна, все транзакции в XRP Ledger необратимы. "
            "Пользователи несут полную ответственность за соблюдение местного законодательства "
            "в области криптовалют и онлайн-игр.")
    c.setFont("Arial", 9.5)
    c.setFillColor(GRAY)
    words = disc.split()
    line, lines = [], []
    for w in words:
        test = ' '.join(line + [w])
        if c.stringWidth(test, "Arial", 9.5) < W - 120:
            line.append(w)
        else:
            lines.append(' '.join(line))
            line = [w]
    if line:
        lines.append(' '.join(line))
    for i, l in enumerate(lines[:3]):
        c.drawString(58, 100 - i * 14, l)

    # License line
    c.setFont("Arial", 10)
    c.setFillColor(GRAY)
    c.drawCentredString(W // 2, 38,
        "Бесплатно для некоммерческого использования как крипто-кошелёк  ·  Игровой сервер проприетарный  ·  Коммерческие запросы приветствуются")

    slide_footer(c, n, T)


# ── Main ──────────────────────────────────────────────────────────────────────

SLIDES = [
    slide_01_title,
    slide_02_what,
    slide_03_why,
    slide_04_vision,
    slide_04_wallet,
    slide_05_security,
    slide_06_games,
    slide_07_gallery,
    slide_08_referral,
    slide_09_arch,
    slide_10_roadmap,
    slide_11_final,
]

def main():
    c = rl_canvas.Canvas(OUT, pagesize=(W, H))
    c.setTitle("XURA — XRP Кошелёк и Блокчейн-игровая платформа")
    c.setAuthor("SamuilOlegovich")
    c.setSubject("Презентация продукта 2026")
    T = len(SLIDES)
    for i, fn in enumerate(SLIDES, 1):
        fn(c, i, T)
        c.showPage()
    c.save()
    print(f"✓  Сохранено → {OUT}  ({T} слайдов)")

if __name__ == "__main__":
    main()

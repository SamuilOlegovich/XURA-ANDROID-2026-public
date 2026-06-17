#!/usr/bin/env python3
"""XURA Presentation PDF — dark style matching the app."""

from reportlab.pdfgen import canvas as rl_canvas
from reportlab.lib.colors import HexColor, Color, white
from PIL import Image as PILImage
import os

PROJ    = "/Users/samuilolegovich/iDev/XURA-ANDROID-2026"
SCREENS = f"{PROJ}/screenshots"
OUT     = f"{PROJ}/XURA_Presentation.pdf"

W, H = 960, 540   # 16:9 in points

# ── Palette ──────────────────────────────────────────────────────────────────
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
    c.setFont("Helvetica", 9)
    c.setFillColor(DIM)
    c.drawString(30, 14, "XURA — XRP Wallet & Blockchain Gaming Platform")
    c.drawRightString(W - 24, 14, f"{n}  /  {total}")

def title_text(c, text, x, y, color=None, size=26):
    c.setFont("Helvetica-Bold", size)
    c.setFillColor(color or WHITE)
    c.drawString(x, y, text)
    accent_line(c, x, y - 10, min(len(text) * size * 0.52, 200), 3, color or CYAN)

def bullet_item(c, dot, text, x, y, dot_color=None, size=12):
    c.setFillColor(dot_color or CYAN)
    c.setFont("Helvetica-Bold", size + 2)
    c.drawString(x, y, dot)
    c.setFont("Helvetica", size)
    c.setFillColor(WHITE)
    c.drawString(x + 18, y, text)

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


# ── SLIDE 1 — Title ───────────────────────────────────────────────────────────

def slide_01_title(c, n, T):
    bg(c)
    top_bar(c, PURPLE, CYAN, 8)
    bottom_bar(c, ORANGE, MAGENTA, 5)

    # Glow circle behind logo
    c.setFillColor(HexColor('#0D0D2E'))
    c.circle(W // 2, H // 2 + 60, 130, fill=1, stroke=0)
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

    c.setFont("Helvetica", 18)
    c.setFillColor(CYAN)
    c.drawCentredString(W // 2, H // 2 - 22, "XRP Wallet  ·  Blockchain Gaming Platform")

    c.setFont("Helvetica", 12)
    c.setFillColor(GRAY)
    c.drawCentredString(W // 2, H // 2 - 46,
        "Non-custodial  ·  On-chain games  ·  10 languages  ·  Android  ·  Since 2022")

    # Badge
    rounded_card(c, W // 2 - 105, 38, 210, 26, CARD2, 8)
    c.setFont("Helvetica-Bold", 11)
    c.setFillColor(CYAN)
    c.drawCentredString(W // 2, 49, "v26.6.17  |  minSdk 28  |  Java · MVVM · Hilt")

    slide_footer(c, n, T)


# ── SLIDE 2 — What is XURA ────────────────────────────────────────────────────

def slide_02_what(c, n, T):
    bg(c)
    top_bar(c, BLUE, CYAN)
    bottom_bar(c)

    title_text(c, "What is XURA?", 50, H - 72, CYAN, 26)

    # Left description card
    rounded_card(c, 40, 55, 380, H - 145, CARD, 12)
    desc = [
        "XURA is a non-custodial mobile wallet",
        "for the XRP Ledger combined with a",
        "blockchain-verifiable gaming platform.",
        "",
        "Every bet is a real on-chain XRP",
        "transaction. Winnings are paid back",
        "directly to your wallet address —",
        "no withdrawal requests, no hidden",
        "balances, no trust required.",
        "",
        "The wallet works on XRPL Mainnet",
        "right now. Game server coming soon.",
    ]
    y = H - 130
    for line in desc:
        if line:
            c.setFont("Helvetica", 12.5)
            c.setFillColor(GRAY if not line.startswith("XURA") else WHITE)
            c.drawString(58, y, line)
        y -= 22

    # Right: 3 pillar cards
    pillars = [
        (CYAN,    "🔐  XRPL Wallet",      "Non-custodial · Mainnet-ready · Secure seed storage"),
        (MAGENTA, "🎲  On-chain Casino",   "Roulette · Guess Color · Guess Number"),
        (ORANGE,  "👥  Referral System",   "On-chain registration · Bonus lives · Daily draws"),
    ]
    py = H - 125
    for col, title, sub in pillars:
        rounded_card(c, 445, py - 55, 490, 64, CARD2, 10)
        c.setFillColor(col)
        c.rect(445, py - 55, 5, 64, fill=1, stroke=0)
        c.setFont("Helvetica-Bold", 14)
        c.setFillColor(col)
        c.drawString(462, py - 18, title)
        c.setFont("Helvetica", 11.5)
        c.setFillColor(GRAY)
        c.drawString(462, py - 38, sub)
        py -= 80

    # Screenshot preview
    img(c, "13_main_wallet.png", 855, 62, 80)

    slide_footer(c, n, T)


# ── SLIDE 3 — Why XURA (comparison) ──────────────────────────────────────────

def slide_03_why(c, n, T):
    bg(c)
    top_bar(c, PURPLE, MAGENTA)
    bottom_bar(c)

    title_text(c, "Why XURA?", 50, H - 72, MAGENTA, 26)

    headers = ["Feature", "Traditional Casino", "XURA ✓"]
    rows = [
        ("Fund custody",    "Platform holds your funds",    "User — non-custodial"),
        ("Deposits",        "Internal balance (off-chain)", "Direct XRPL transaction"),
        ("Withdrawals",     "Request required, may freeze", "Not needed — on-chain"),
        ("Transparency",    "Limited / opaque RNG",         "Every bet verifiable"),
        ("Private key",     "Not applicable",               "Never leaves your device"),
        ("Funds at risk",   "Platform insolvency risk",     "Zero — always on XRPL"),
    ]

    cx = [50, 270, 520]
    cw = [215, 245, 360]
    row_h = 44
    table_top = H - 105

    # Header
    rounded_card(c, 40, table_top - 28, sum(cw) + 30, 34, HexColor('#1C0A30'), 6)
    for i, (hdr, x) in enumerate(zip(headers, cx)):
        c.setFont("Helvetica-Bold", 12)
        c.setFillColor(MAGENTA if i == 0 else (GREEN if i == 2 else GRAY))
        c.drawString(x, table_top - 16, hdr)

    for ri, (feat, bad, good) in enumerate(rows):
        ry = table_top - 30 - (ri + 1) * row_h
        bg_col = CARD if ri % 2 == 0 else CARD2
        rounded_card(c, 40, ry, sum(cw) + 30, row_h - 3, bg_col, 5)

        c.setFont("Helvetica-Bold", 11.5)
        c.setFillColor(WHITE)
        c.drawString(cx[0], ry + 14, feat)

        c.setFont("Helvetica", 11.5)
        c.setFillColor(HexColor('#FF6666'))
        c.drawString(cx[1], ry + 14, bad)

        c.setFont("Helvetica-Bold", 11.5)
        c.setFillColor(GREEN)
        c.drawString(cx[2], ry + 14, good)

    # Side badge
    rounded_card(c, 775, 55, 165, 95, CARD3, 10, GREEN)
    accent_line(c, 775, 55, 165, 4, GREEN)
    c.setFont("Helvetica-Bold", 11)
    c.setFillColor(GREEN)
    c.drawCentredString(857, 128, "Mini-Ecosystem")
    c.setFont("Helvetica", 11)
    c.setFillColor(GRAY)
    for i, t in enumerate(["• XRPL Wallet", "• On-chain Casino", "• Referral Platform"]):
        c.drawString(790, 108 - i * 17, t)

    slide_footer(c, n, T)


# ── SLIDE 4 — Vision ─────────────────────────────────────────────────────────

def slide_04_vision(c, n, T):
    bg(c)
    top_bar(c, BLUE, PURPLE)
    bottom_bar(c)

    title_text(c, "Vision: The Future of On-Chain Gaming", 50, H - 72, PURPLE, 23)

    # Central statement
    rounded_card(c, 40, H - 165, W - 80, 75, CARD2, 12)
    accent_line(c, 40, H - 90, W - 80, 4, PURPLE)
    c.setFont("Helvetica-Bold", 15)
    c.setFillColor(WHITE)
    c.drawCentredString(W // 2, H - 120,
        "XURA is a proof-of-concept, not just a product.")
    c.setFont("Helvetica", 12.5)
    c.setFillColor(GRAY)
    c.drawCentredString(W // 2, H - 142,
        "This is a reference implementation of how on-chain gaming should work —")
    c.drawCentredString(W // 2, H - 158,
        "transparent, non-custodial, and verifiable by anyone on any chain.")

    # Chains row
    chains = [
        (HexColor('#627EEA'), "Ethereum",  "EVM · smart contracts"),
        (HexColor('#9945FF'), "Solana",    "SOL · SPL tokens"),
        (HexColor('#00B0FF'), "XRPL ★",   "Reference implementation"),
        (HexColor('#7B4FFF'), "TON",       "Telegram ecosystem"),
        (HexColor('#E84142'), "Stellar",   "XLM · similar architecture"),
    ]
    cx = 40
    cw = (W - 80) // len(chains)
    for col, name, sub in chains:
        is_xrpl = "★" in name
        rounded_card(c, cx, H - 265, cw - 10, 85, CARD2, 10,
                     col if is_xrpl else None)
        if is_xrpl:
            rounded_card(c, cx, H - 265, cw - 10, 85, HexColor('#0A1830'), 10)
        c.setFillColor(col)
        c.rect(cx, H - 265, cw - 10, 5, fill=1, stroke=0)
        c.setFont("Helvetica-Bold", 13)
        c.setFillColor(col)
        c.drawCentredString(cx + (cw - 10) // 2, H - 225, name)
        c.setFont("Helvetica", 10)
        c.setFillColor(WHITE if is_xrpl else GRAY)
        c.drawCentredString(cx + (cw - 10) // 2, H - 243, sub)
        cx += cw

    # Three principles
    principles = [
        (CYAN,    "No casino account",
                  "Users keep custody of funds at all times.\nNo deposit. No withdrawal request."),
        (MAGENTA, "Every bet = real tx",
                  "Bets are native blockchain transactions\nwith verifiable memos."),
        (GREEN,   "Server cannot cheat",
                  "Every outcome is signed. Results are\nverifiable on-chain by anyone."),
    ]
    px = 40
    for col, title, desc in principles:
        rounded_card(c, px, 52, 290, 115, CARD2, 12)
        c.setFillColor(col)
        c.rect(px, 52, 290, 4, fill=1, stroke=0)
        c.setFont("Helvetica-Bold", 13)
        c.setFillColor(col)
        c.drawCentredString(px + 145, 140, title)
        c.setFont("Helvetica", 11)
        c.setFillColor(GRAY)
        for i, line in enumerate(desc.split('\n')):
            c.drawCentredString(px + 145, 116 - i * 18, line)
        px += 310

    slide_footer(c, n, T)


# ── SLIDE 5 — Wallet Features ─────────────────────────────────────────────────

def slide_04_wallet(c, n, T):
    bg(c)
    top_bar(c, BLUE, CYAN)
    bottom_bar(c)

    title_text(c, "Wallet Features", 50, H - 72, CYAN, 26)

    features = [
        (CYAN,    "Create Wallet",       "New XRPL keypair — seed shown once, encrypted in Android Keystore AES-256-GCM"),
        (CYAN,    "Restore Wallet",      "Import any XRPL account via 16-word seed phrase"),
        (CYAN,    "Send XRP",            "Hand input or QR scan · destination tag · confirmation dialog"),
        (CYAN,    "Receive XRP",         "Your address as QR code + one-tap copy"),
        (CYAN,    "Transaction History", "Real-time list via WebSocket subscription to wss://xrplcluster.com"),
        (CYAN,    "Live Balance",        "Persistent WebSocket — balance updates without refresh"),
        (CYAN,    "Testnet Mode",        "Switch to XRPL Altnet for development without real funds"),
    ]

    x = 50
    y = H - 120
    for col, title, desc in features:
        rounded_card(c, x - 8, y - 26, 575, 38, CARD2, 7)
        c.setFillColor(col)
        c.rect(x - 8, y - 26, 4, 38, fill=1, stroke=0)
        c.setFont("Helvetica-Bold", 12)
        c.setFillColor(WHITE)
        c.drawString(x + 6, y - 4, title)
        c.setFont("Helvetica", 11)
        c.setFillColor(GRAY)
        c.drawString(x + 6, y - 18, desc)
        y -= 48

    # Screenshots column
    img(c, "14_send_payment.png",  650, 295, 85)
    img(c, "15_receive_qr.png",    745, 295, 85)
    img(c, "16_tx_history.png",    840, 295, 85)
    img(c, "13_main_wallet.png",   690, 55,  120)

    slide_footer(c, n, T)


# ── SLIDE 5 — Security ───────────────────────────────────────────────────────

def slide_05_security(c, n, T):
    bg(c)
    top_bar(c, PURPLE, BLUE)
    bottom_bar(c)

    title_text(c, "Security", 50, H - 72, PURPLE, 26)

    items = [
        (PURPLE, "Android Keystore AES-256-GCM",
                 "Seed encrypted in TEE — never exposed in plaintext"),
        (PURPLE, "Biometric Unlock",
                 "Fingerprint / Face ID via androidx.biometric (BIOMETRIC_STRONG only)"),
        (PURPLE, "App Password (PBKDF2)",
                 "Optional PIN/password — salted hash, never stored in plaintext"),
        (PURPLE, "Inactivity Auto-lock",
                 "Screen locks after configurable idle period"),
        (PURPLE, "Root & Tamper Detection",
                 "Warns if device is rooted or APK is re-signed with unknown key"),
        (PURPLE, "FLAG_SECURE",
                 "Prevents OS screenshots on seed display and password entry screens"),
        (PURPLE, "Clipboard Safety",
                 "Confirmation dialog before paste — guards against address-swap attacks"),
    ]

    x = 50
    y = H - 120
    for col, title, desc in items:
        rounded_card(c, x - 8, y - 26, 575, 38, CARD2, 7)
        c.setFillColor(col)
        c.rect(x - 8, y - 26, 4, 38, fill=1, stroke=0)
        c.setFont("Helvetica-Bold", 12)
        c.setFillColor(WHITE)
        c.drawString(x + 6, y - 4, title)
        c.setFont("Helvetica", 11)
        c.setFillColor(GRAY)
        c.drawString(x + 6, y - 18, desc)
        y -= 48

    # Security icon screenshots
    img(c, "32_security_root_warning.png", 670, 80, 90)
    img(c, "10_set_password.png",          770, 80, 90)
    img(c, "11_enter_password.png",        870, 80, 85)

    # Shield decoration
    rounded_card(c, 655, 360, 310, 120, HexColor('#12082A'), 12, PURPLE)
    c.setFont("Helvetica-Bold", 36)
    c.setFillColor(PURPLE)
    c.drawCentredString(810, 440, "🔐")
    c.setFont("Helvetica-Bold", 13)
    c.setFillColor(WHITE)
    c.drawCentredString(810, 408, "Your keys. Your coins.")
    c.setFont("Helvetica", 11)
    c.setFillColor(GRAY)
    c.drawCentredString(810, 388, "Seed never touches any server")

    slide_footer(c, n, T)


# ── SLIDE 6 — Games ───────────────────────────────────────────────────────────

def slide_06_games(c, n, T):
    bg(c)
    top_bar(c, ORANGE, MAGENTA)
    bottom_bar(c)

    title_text(c, "Games  *(backend coming soon)*", 50, H - 72, ORANGE, 24)

    c.setFont("Helvetica", 12)
    c.setFillColor(GRAY)
    c.drawString(50, H - 95,
        "Every bet = real XRPL transaction  ·  Server responds with signed payment  ·  Result verifiable on-chain")

    # Three game cards
    games = [
        (MAGENTA, "Guess the Color",  "Pick Red or Black",      "×2  payout",  "22_guess_color.png"),
        (ORANGE,  "Guess the Number", "Pick a number 1–36",     "×36 payout",  "23_guess_number.png"),
        (YELLOW,  "European Roulette","Full table: straight,\nred/black, dozens…", "×2–×36", "24_roulette.png"),
    ]

    gx = 50
    for col, title, desc, payout, scr in games:
        rounded_card(c, gx, 55, 265, H - 165, CARD2, 12, col)
        accent_line(c, gx, H - 112, 265, 4, col)

        c.setFont("Helvetica-Bold", 14)
        c.setFillColor(col)
        c.drawCentredString(gx + 132, H - 130, title)

        c.setFont("Helvetica", 11)
        c.setFillColor(GRAY)
        for i, line in enumerate(desc.split('\n')):
            c.drawCentredString(gx + 132, H - 152 - i * 16, line)

        rounded_card(c, gx + 60, H - 195, 105, 26, col, 8)
        c.setFont("Helvetica-Bold", 14)
        c.setFillColor(BG)
        c.drawCentredString(gx + 112, H - 183, payout)

        img(c, scr, gx + 20, 68, 225)
        gx += 295

    slide_footer(c, n, T)


# ── SLIDE 7 — Screenshots Gallery ────────────────────────────────────────────

def slide_07_gallery(c, n, T):
    bg(c)
    top_bar(c, CYAN, GREEN)
    bottom_bar(c)

    title_text(c, "App Screenshots", 50, H - 72, CYAN, 26)

    shots = [
        ("01_splash.png",           "Splash"),
        ("02_onb_1_welcome.png",    "Welcome"),
        ("05_onb_4_get_started.png","Secure by Design"),
        ("06_create_or_restore.png","Setup"),
        ("07_create_new_wallet_seed.png", "New Wallet"),
        ("08_checking_seed.png",    "Verify Seed"),
        ("13_main_wallet.png",      "Main Wallet"),
        ("14_send_payment.png",     "Send XRP"),
        ("15_receive_qr.png",       "Receive"),
    ]

    thumb_w = 85
    gap = 16
    row1_x = 40
    row2_x = 40 + (thumb_w + gap)

    for i, (fn, label) in enumerate(shots):
        col_i = i % 5
        row_i = i // 5
        sx = 40 + col_i * (thumb_w + gap)
        h_pts = img(c, fn, sx, 85 + (1 - row_i) * 200, thumb_w)

        c.setFont("Helvetica", 8.5)
        c.setFillColor(GRAY)
        c.drawCentredString(sx + thumb_w // 2, 73 + (1 - row_i) * 200, label)

    # Win/Lose showcase (right side)
    rounded_card(c, 510, 55, 430, H - 135, CARD2, 14, DIM)
    c.setFont("Helvetica-Bold", 13)
    c.setFillColor(GRAY)
    c.drawCentredString(725, H - 100, "Game Results")

    img(c, "33_roulette_wheel.png", 530, 65, 120)
    img(c, "34_bet_won.png",        665, 65, 120)
    img(c, "35_bet_lost.png",       800, 65, 120)

    c.setFont("Helvetica", 9)
    c.setFillColor(GRAY)
    c.drawCentredString(590, 58, "Wheel")
    c.drawCentredString(725, 58, "BET WON")
    c.drawCentredString(860, 58, "BET LOST")

    slide_footer(c, n, T)


# ── SLIDE 8 — Referral System ─────────────────────────────────────────────────

def slide_08_referral(c, n, T):
    bg(c)
    top_bar(c, GREEN, CYAN)
    bottom_bar(c)

    title_text(c, "Referral System", 50, H - 72, GREEN, 26)

    steps = [
        (GREEN,   "1",  "Register on-chain",
                        "Pay 66 XRP registration fee → recorded on the XRP Ledger.\n13 XRP refunded on exit."),
        (CYAN,    "2",  "Share your code",
                        "Invite users — unlimited slots for invited users.\nNumber of referral positions is limited."),
        (YELLOW,  "3",  "Earn together",
                        "Both you and your referrals get bonus lives per bet.\nParticipate in the daily prize draw."),
        (ORANGE,  "4",  "Recover anytime",
                        "Lost your code? Recover it for 13 XRP.\nAll on-chain — transparent and auditable."),
    ]

    x = 40
    y = H - 115
    for col, num, title, desc in steps:
        rounded_card(c, x, y - 58, 440, 68, CARD2, 10)
        # Number badge
        rounded_card(c, x + 8, y - 48, 36, 36, col, 18)
        c.setFont("Helvetica-Bold", 18)
        c.setFillColor(BG)
        c.drawCentredString(x + 26, y - 27, num)
        # Text
        c.setFont("Helvetica-Bold", 13)
        c.setFillColor(col)
        c.drawString(x + 56, y - 18, title)
        c.setFont("Helvetica", 11)
        c.setFillColor(GRAY)
        for i, line in enumerate(desc.split('\n')):
            c.drawString(x + 56, y - 34 - i * 14, line)
        y -= 80

    # Screenshots
    img(c, "28_referral_enter.png",  520, 230, 90)
    img(c, "29_become_referral.png", 620, 230, 90)
    img(c, "30_your_referral.png",   720, 230, 90)
    img(c, "36_info_referral.png",   820, 230, 90)

    rounded_card(c, 510, 55, 430, 165, CARD3, 12, GREEN)
    c.setFont("Helvetica-Bold", 13)
    c.setFillColor(GREEN)
    c.drawCentredString(725, 200, "Fee breakdown")
    table = [
        ("Registration fee",  "66 XRP",  GREEN),
        ("Exit refund",       "13 XRP",  YELLOW),
        ("Net cost",          "53 XRP",  ORANGE),
        ("Referral slots",    "Limited", CYAN),
        ("Invited users",     "∞",       CYAN),
    ]
    ty = 178
    for label, val, col in table:
        c.setFont("Helvetica", 11)
        c.setFillColor(GRAY)
        c.drawString(530, ty, label)
        c.setFont("Helvetica-Bold", 11)
        c.setFillColor(col)
        c.drawRightString(920, ty, val)
        ty -= 20

    slide_footer(c, n, T)


# ── SLIDE 9 — Architecture ───────────────────────────────────────────────────

def slide_09_arch(c, n, T):
    bg(c)
    top_bar(c, BLUE, PURPLE)
    bottom_bar(c)

    title_text(c, "Architecture & Stack", 50, H - 72, BLUE, 26)

    # Left: layers
    layers = [
        (CYAN,    "View (Activities)",         "31 screens · Java"),
        (BLUE,    "ViewModel + LiveData",       "MVVM · reactive state"),
        (PURPLE,  "Dagger Hilt DI",            "AppModule · scoped injection"),
        (ORANGE,  "XRPL Client",               "xrpl4j-core 3.3.0 · WebSocket · Retrofit"),
        (GREEN,   "Security Layer",            "Android Keystore · BiometricHelper · Cipher"),
        (YELLOW,  "Async Runnables",           "BalanceRunnable · SubscriberRunnable · Notifier"),
    ]
    y = H - 118
    for col, title, sub in layers:
        rounded_card(c, 40, y - 28, 430, 40, CARD2, 8)
        c.setFillColor(col)
        c.rect(40, y - 28, 4, 40, fill=1, stroke=0)
        c.setFont("Helvetica-Bold", 12)
        c.setFillColor(WHITE)
        c.drawString(56, y - 4, title)
        c.setFont("Helvetica", 11)
        c.setFillColor(GRAY)
        c.drawString(56, y - 19, sub)
        y -= 52

    # Right: libs grid
    libs = [
        (CYAN,    "xrpl4j 3.3.0",        "XRPL transactions"),
        (BLUE,    "Java-WebSocket 1.5.7", "Real-time balance"),
        (PURPLE,  "Hilt 2.51.1",          "Dependency injection"),
        (GREEN,   "biometric 1.2.0",      "Fingerprint / FaceID"),
        (ORANGE,  "Retrofit 2.11.0",      "REST + JSON"),
        (YELLOW,  "MLKit barcodes",       "QR code scanning"),
        (CYAN,    "ZXing 3.3.2",          "QR generation"),
        (MAGENTA, "security-crypto",      "EncryptedSharedPrefs"),
    ]
    lx, ly = 490, H - 118
    for i, (col, lib, desc) in enumerate(libs):
        row, col_idx = i // 2, i % 2
        cx_ = lx + col_idx * 230
        cy_ = ly - row * 60
        rounded_card(c, cx_, cy_ - 30, 220, 44, CARD2, 8)
        c.setFillColor(col)
        c.setFont("Helvetica-Bold", 11)
        c.drawString(cx_ + 10, cy_ - 8, lib)
        c.setFont("Helvetica", 10)
        c.setFillColor(GRAY)
        c.drawString(cx_ + 10, cy_ - 22, desc)

    # Test count badge
    rounded_card(c, 490, 55, 460, 38, CARD3, 8, GREEN)
    c.setFont("Helvetica-Bold", 13)
    c.setFillColor(GREEN)
    c.drawString(510, 70, "✓  79 automated tests  ·  Java · MVVM · Dagger Hilt · minSdk 28")

    slide_footer(c, n, T)


# ── SLIDE 10 — Roadmap ───────────────────────────────────────────────────────

def slide_10_roadmap(c, n, T):
    bg(c)
    top_bar(c, CYAN, GREEN)
    bottom_bar(c)

    title_text(c, "Roadmap", 50, H - 72, GREEN, 26)

    done = [
        "Non-custodial XRP wallet — Mainnet ready",
        "Biometric + password security (PBKDF2 + Keystore AES-256)",
        "QR send / receive + camera scanner",
        "Real-time transaction history (WebSocket)",
        "European Roulette — full table UI + protocol",
        "Guess the Color / Guess the Number — UI + protocol",
        "Referral system — on-chain registration & management",
        "10-language localisation (EN, RU, ZH, HI, ES, FR, DE, AR, PT, BN)",
        "79 automated tests",
    ]
    upcoming = [
        "Game server launch  ← ETA: coming weeks",
        "Push notifications for incoming payments",
        "Google Play release",
    ]

    # Done column
    rounded_card(c, 40, 52, 450, H - 140, CARD2, 12)
    c.setFont("Helvetica-Bold", 12)
    c.setFillColor(GREEN)
    c.drawString(58, H - 108, "✓  Completed")
    accent_line(c, 58, H - 116, 130, 2, GREEN)
    y = H - 135
    for item in done:
        c.setFont("Helvetica", 11)
        c.setFillColor(WHITE)
        c.drawString(62, y, "✓")
        c.setFillColor(GRAY)
        c.drawString(78, y, item)
        y -= 28

    # Upcoming column
    rounded_card(c, 510, 52, 430, H - 140, CARD2, 12)
    c.setFont("Helvetica-Bold", 12)
    c.setFillColor(CYAN)
    c.drawString(528, H - 108, "⟳  Coming Soon")
    accent_line(c, 528, H - 116, 140, 2, CYAN)
    y = H - 145
    for item in upcoming:
        rounded_card(c, 520, y - 16, 410, 32, HexColor('#0A1A2A'), 6)
        c.setFont("Helvetica-Bold", 11)
        c.setFillColor(CYAN if "server" in item.lower() else YELLOW)
        c.drawString(534, y - 3, "→")
        c.setFont("Helvetica", 11)
        c.setFillColor(WHITE)
        c.drawString(550, y - 3, item)
        y -= 46

    slide_footer(c, n, T)


# ── SLIDE 11 — Disclaimer + Contact ──────────────────────────────────────────

def slide_11_final(c, n, T):
    bg(c)
    top_bar(c, ORANGE, MAGENTA)
    bottom_bar(c, PURPLE, CYAN)

    # Glow
    c.setFillColor(HexColor('#0D0D22'))
    c.circle(W // 2, H // 2, 200, fill=1, stroke=0)

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

    c.setFont("Helvetica", 13)
    c.setFillColor(CYAN)
    c.drawCentredString(W // 2, H // 2 + 18, "Built with ♥ on the XRP Ledger  ·  Since 2022")

    # Contact cards
    contacts = [
        (CYAN,    "GitHub",  "SamuilOlegovich"),
        (MAGENTA, "Email",   "samuilolegovich@gmail.com"),
    ]
    cx = W // 2 - 230
    for col, label, val in contacts:
        rounded_card(c, cx, H // 2 - 42, 210, 38, CARD2, 8)
        c.setFont("Helvetica-Bold", 11)
        c.setFillColor(col)
        c.drawString(cx + 12, H // 2 - 14, label)
        c.setFont("Helvetica", 11)
        c.setFillColor(WHITE)
        c.drawString(cx + 12, H // 2 - 30, val)
        cx += 250

    # Disclaimer box
    rounded_card(c, 60, 55, W - 120, 80, HexColor('#14080A'), 10, ORANGE)
    c.setFont("Helvetica-Bold", 10)
    c.setFillColor(ORANGE)
    c.drawString(80, 115, "Disclaimer:")
    disc = ("XURA does not provide financial or investment advice. Cryptocurrency values are volatile and "
            "all transactions on the XRP Ledger are irreversible. Users are solely responsible for complying "
            "with local laws and regulations regarding cryptocurrency ownership and online gaming.")
    c.setFont("Helvetica", 9.5)
    c.setFillColor(GRAY)
    # Word-wrap manually
    words = disc.split()
    line, lines = [], []
    for w in words:
        test = ' '.join(line + [w])
        if c.stringWidth(test, "Helvetica", 9.5) < W - 200:
            line.append(w)
        else:
            lines.append(' '.join(line))
            line = [w]
    if line:
        lines.append(' '.join(line))
    for i, l in enumerate(lines[:3]):
        c.drawString(80, 100 - i * 14, l)

    # License line
    c.setFont("Helvetica", 10)
    c.setFillColor(GRAY)
    c.drawCentredString(W // 2, 38, "Free for non-commercial use as a crypto wallet  ·  Game server is proprietary  ·  Commercial inquiries welcome")

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
    c.setTitle("XURA — XRP Wallet & Blockchain Gaming Platform")
    c.setAuthor("SamuilOlegovich")
    c.setSubject("Product Presentation 2026")
    T = len(SLIDES)
    for i, fn in enumerate(SLIDES, 1):
        fn(c, i, T)
        c.showPage()
    c.save()
    print(f"✓  Saved → {OUT}  ({T} slides)")

if __name__ == "__main__":
    main()

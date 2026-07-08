#!/usr/bin/env python3
"""
Генератор звука вращения барабана слот-машины.
Модель: механический трещоточный щелчок (ratchet/detent) —
короткий шумовой импульс с низкочастотным телом, как в реальных механических барабанах.
"""
import wave, struct, math, random

SAMPLE_RATE  = 44100
DURATION_SEC = 5

# ── Параметры одного щелчка ──────────────────────────────────────────────────
CLICK_MS   = 45     # длина ударного импульса (коротко = ощущение удара)
GAP_MS     = 75     # пауза: итого 120 мс = скорость одного символа барабана

# Тело щелчка: низкочастотный резонанс (удар по пластику/металлу)
BODY_FREQ  = 320.0  # Гц — основной тон удара
BODY_AMP   = 0.50   # амплитуда тона
BODY_DECAY = 60.0   # скорость затухания тона

# Атака: высокочастотный шум (механический контакт)
NOISE_AMP  = 0.45   # амплитуда шума
NOISE_DECAY = 200.0 # быстро исчезает

# Фоновый вихрь мотора (полосовой шум ~150 Гц)
WHIRR_FREQ = 150.0
WHIRR_AMP  = 0.07

# ────────────────────────────────────────────────────────────────────────────

click_samples = SAMPLE_RATE * CLICK_MS // 1000
gap_samples   = SAMPLE_RATE * GAP_MS   // 1000
period        = click_samples + gap_samples

# Состояние для простого band-pass фильтра (имитация мотора)
lp1 = lp2 = 0.0
hp  = 0.0
FC  = WHIRR_FREQ / SAMPLE_RATE  # нормированная частота среза

frames = []
total  = SAMPLE_RATE * DURATION_SEC

for i in range(total):
    t   = i / SAMPLE_RATE
    pos = i % period

    # ── Фоновый вихрь: белый шум → low-pass → high-pass (band-pass) ─────────
    raw_noise = random.gauss(0, 1)
    alpha_lp  = 2 * math.pi * FC / (2 * math.pi * FC + 1)
    lp1  += alpha_lp * (raw_noise - lp1)
    lp2  += alpha_lp * (lp1 - lp2)
    alpha_hp = 1 - alpha_lp
    hp   = alpha_hp * (hp + lp2 - (lp2 if i == 0 else lp2))
    whirr = lp2 * WHIRR_AMP

    # ── Щелчок (ударный импульс) ─────────────────────────────────────────────
    click = 0.0
    if pos < click_samples:
        ct = pos / SAMPLE_RATE

        # Низкочастотное тело (затухающий резонанс)
        body = math.sin(2 * math.pi * BODY_FREQ * ct) * math.exp(-ct * BODY_DECAY)

        # Высокочастотный шум атаки (очень быстрое затухание)
        noise = random.gauss(0, 1) * math.exp(-ct * NOISE_DECAY)

        click = body * BODY_AMP + noise * NOISE_AMP

    # ── Итоговый сэмпл ───────────────────────────────────────────────────────
    s = click + whirr
    s = max(-0.95, min(0.95, s))
    frames.append(struct.pack('<h', int(s * 32767)))

with wave.open('slot_spin_preview.wav', 'wb') as f:
    f.setnchannels(1)
    f.setsampwidth(2)
    f.setframerate(SAMPLE_RATE)
    f.writeframes(b''.join(frames))

print("slot_spin_preview.wav — 5 сек, 44100 Hz mono")
print(f"Щелчки: {1000 // (CLICK_MS + GAP_MS)} уд/сек | тело: {BODY_FREQ} Гц | шум атаки + вихрь мотора {WHIRR_FREQ} Гц")

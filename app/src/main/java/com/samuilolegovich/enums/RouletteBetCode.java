package com.samuilolegovich.enums;

/**
 * Compact abbreviation table for all roulette bet types used in XRP memo.
 *
 * Memo format example (multi-bet):
 *   BET:R:n5@1.5,r@2.0,d1@0.5:referralCode
 *
 * Straight number bets N:0–N:36 map to n0–n36 (handled programmatically below).
 *
 * Full abbreviation table:
 * ┌────────────┬──────┬──────────────────────┬──────────┐
 * │ Tag (full) │ Code │ Description          │ Payout   │
 * ├────────────┼──────┼──────────────────────┼──────────┤
 * │ N:0        │ n0   │ Straight: zero       │ 36x      │
 * │ N:1–N:36   │ n1–n36 │ Straight number    │ 36x      │
 * │ RED        │ r    │ Red colour           │ 2x       │
 * │ BLACK      │ b    │ Black colour         │ 2x       │
 * │ ODD        │ o    │ Odd numbers          │ 2x       │
 * │ EVEN       │ e    │ Even numbers         │ 2x       │
 * │ LOW        │ l    │ 1–18                 │ 2x       │
 * │ HIGH       │ h    │ 19–36                │ 2x       │
 * │ D1         │ d1   │ 1st dozen  (1–12)   │ 3x       │
 * │ D2         │ d2   │ 2nd dozen  (13–24)  │ 3x       │
 * │ D3         │ d3   │ 3rd dozen  (25–36)  │ 3x       │
 * │ C1         │ c1   │ 1st column          │ 3x       │
 * │ C2         │ c2   │ 2nd column          │ 3x       │
 * │ C3         │ c3   │ 3rd column          │ 3x       │
 * └────────────┴──────┴──────────────────────┴──────────┘
 */
public enum RouletteBetCode {

    RED  ("RED",   "r",  2),
    BLACK("BLACK", "b",  2),
    ODD  ("ODD",   "o",  2),
    EVEN ("EVEN",  "e",  2),
    LOW  ("LOW",   "l",  2),
    HIGH ("HIGH",  "h",  2),
    D1   ("D1",    "d1", 3),
    D2   ("D2",    "d2", 3),
    D3   ("D3",    "d3", 3),
    C1   ("C1",    "c1", 3),
    C2   ("C2",    "c2", 3),
    C3   ("C3",    "c3", 3);

    /** Full internal tag used throughout the app (e.g. "RED", "D1", "N:5"). */
    public final String fullTag;
    /** Compact code sent in XRP memo (e.g. "r", "d1", "n5"). */
    public final String code;
    /** Win multiplier applied to the bet amount. */
    public final int multiplier;

    RouletteBetCode(String fullTag, String code, int multiplier) {
        this.fullTag = fullTag;
        this.code = code;
        this.multiplier = multiplier;
    }

    // ── Conversion helpers ────────────────────────────────────────────────

    /** Converts a full internal tag to its compact memo code. */
    public static String tagToCode(String tag) {
        if (tag != null && tag.startsWith("N:")) {
            return "n" + tag.substring(2);
        }
        for (RouletteBetCode b : values()) {
            if (b.fullTag.equals(tag)) return b.code;
        }
        return tag != null ? tag.toLowerCase() : "";
    }

    /** Converts a compact memo code back to the full internal tag. */
    public static String codeToTag(String code) {
        if (code != null && code.length() >= 2 && code.charAt(0) == 'n') {
            String numPart = code.substring(1);
            try {
                int n = Integer.parseInt(numPart);
                if (n >= 0 && n <= 36) return "N:" + n;
            } catch (NumberFormatException ignored) {}
        }
        for (RouletteBetCode b : values()) {
            if (b.code.equalsIgnoreCase(code)) return b.fullTag;
        }
        return code != null ? code.toUpperCase() : "";
    }

    /** Returns the win multiplier for a given full internal tag. */
    public static int multiplierForTag(String tag) {
        if (tag != null && tag.startsWith("N:")) return 36;
        for (RouletteBetCode b : values()) {
            if (b.fullTag.equals(tag)) return b.multiplier;
        }
        return 2;
    }
}

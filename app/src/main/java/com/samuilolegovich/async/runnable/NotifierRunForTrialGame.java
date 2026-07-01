package com.samuilolegovich.async.runnable;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.XuraApp;
import com.samuilolegovich.enums.RouletteBetCode;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.view.Flasher;
import com.samuilolegovich.view.SlotFlasher;
import com.samuilolegovich.view.SlotReelView;
import com.samuilolegovich.view.YourReferral;
import com.samuilolegovich.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;



/**
 * Имитирует ответ игрового сервера для "тренировочного" (тестового, не на реальные XRP) режима игр.
 * Сам считает результат ставки локально (без сети), генерирует случайную задержку как у настоящего
 * сервера и при выигрыше начисляет тестовый баланс, чтобы пользователь мог опробовать игру без риска.
 */
public class NotifierRunForTrialGame implements Runnable {
    private TestModeEnum testModeEnum;

    private String YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY;
    private String CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO;
    private String CONGRATULATIONS_YOUR_BET_IS_WON;
    private String YOUR_REFERRAL_CODE;

    private final SecureRandom random = new SecureRandom();



    /** Запоминает, для какой игры имитируется ответ сервера, и подгружает локализованные тексты уведомлений. */
    public NotifierRunForTrialGame(TestModeEnum testModeEnum) {
        this.testModeEnum = testModeEnum;
        setLanguage();
    }

    /** Конструктор для unit-тестов — принимает строки напрямую, не обращается к Android-ресурсам. */
    NotifierRunForTrialGame(TestModeEnum testModeEnum,
                            String lostMsg, String wonMsg, String wonLotoMsg, String referralMsg) {
        this.testModeEnum = testModeEnum;
        YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY = lostMsg;
        CONGRATULATIONS_YOUR_BET_IS_WON                  = wonMsg;
        CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO            = wonLotoMsg;
        YOUR_REFERRAL_CODE                               = referralMsg;
    }



    /** Загружает локализованные строки уведомлений (проигрыш/выигрыш/реферальный код) для текущего языка приложения. */
    private void setLanguage() {
        android.content.res.Resources resources = XuraApp.getLocalizedResources();
        YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY = resources.getString(R.string.your_bet_is_lost_try_again);
        CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO = resources.getString(R.string.congratulations_your_bet_is_won_loto);
        CONGRATULATIONS_YOUR_BET_IS_WON = resources.getString(R.string.congratulations_your_bet_is_won);
        YOUR_REFERRAL_CODE = resources.getString(R.string.your_referral_code);
    }


    /** Выдерживает случайную задержку (как у настоящего сервера), затем считает результат ставки для текущей игры. */
    @Override
    public void run() {
        // Генерация случайного числа в диапазоне от 4000 до 10000
        Long timeSleep = (long) (random.nextInt(10001 - 4000) + 4000);

        try {
            Thread.sleep(timeSleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (testModeEnum.equals(TestModeEnum.GUESS_THE_COLOR_GAME)) {
            calculateForGuessTheColor();
        } else if (testModeEnum.equals(TestModeEnum.ROULETTE_GAME)) {
            calculateForRoulette(random.nextInt(37)); // 0–36
        } else if (testModeEnum.equals(TestModeEnum.SLOT_GAME)) {
            calculateForSlot();
        } else {
            calculateForGuessTheNumber(random.nextInt(36) + 1);
        }
    }


    /** Считает результат тестовой ставки в рулетке: бросает колесо один раз и проверяет каждую
     * ставку игрока против выпавшего числа. Выплата суммируется по всем выигравшим позициям
     * с учётом индивидуального мультипликатора каждой ставки. */
    void calculateForRoulette(int winNumber) {
        Flasher.NUMBER_BET = String.valueOf(winNumber);

        String lotto = String.valueOf(random.nextInt(10001 - 4000) + 4000);
        WalletRepository.getInstance().setLottoNow(lotto);

        LinkedHashMap<String, BigDecimal> allBets = Flasher.ROULETTE_ALL_BETS;
        double totalPayout = 0;
        double totalBet    = 0;
        if (allBets != null) {
            for (Map.Entry<String, BigDecimal> entry : allBets.entrySet()) {
                totalBet += entry.getValue().doubleValue();
                if (evaluateRouletteBet(entry.getKey(), winNumber)) {
                    int mult = RouletteBetCode.multiplierForTag(entry.getKey());
                    totalPayout += entry.getValue().doubleValue() * mult;
                }
            }
        }

        // profit = чистая прибыль; totalPayout уже включает возврат ставки на выигравших позициях,
        // а totalBet — сумма всех ставок (в т.ч. проигравших), которая уже списана с баланса.
        double profit = totalPayout - totalBet;
        boolean win = profit > 0;

        if (win && !Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
            try { WalletRepository.getInstance().creditTestBalance(new BigDecimal(totalPayout)); }
            catch (Exception ignored) {}
        }

        String msg = win
                ? String.format(CONGRATULATIONS_YOUR_BET_IS_WON, String.valueOf(profit))
                : YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY;
        responseToBet(msg, lotto, win ? 2 : 1);
    }

    /** Проверяет, выигрывает ли ставка типа betTag (число, цвет, чёт/нечёт, дюжина, колонка и т.д.) при выпавшем числе n. */
    private boolean evaluateRouletteBet(String betTag, int n) {
        if (betTag.startsWith("N:")) {
            return Integer.parseInt(betTag.substring(2)) == n;
        }
        if (n == 0) return false; // 0 only wins straight-bet on 0
        switch (betTag) {
            case "RED":   return !Lotto.learnTheColorOfNumber(String.valueOf(n));
            case "BLACK": return  Lotto.learnTheColorOfNumber(String.valueOf(n));
            case "ODD":   return n % 2 != 0;
            case "EVEN":  return n % 2 == 0;
            case "LOW":   return n >= 1  && n <= 18;
            case "HIGH":  return n >= 19 && n <= 36;
            case "D1":    return n >= 1  && n <= 12;
            case "D2":    return n >= 13 && n <= 24;
            case "D3":    return n >= 25 && n <= 36;
            case "C1":    return n % 3 == 1; // 1,4,7,...,34
            case "C2":    return n % 3 == 2; // 2,5,8,...,35
            case "C3":    return n % 3 == 0; // 3,6,9,...,36
            default:      return false;
        }
    }

    /** Считает результат тестовой ставки в игре "Угадай число": совпадает ли сгенерированное число со ставкой игрока. */
    void calculateForGuessTheNumber(int i) {
        // NUMBER_BET хранит ставку игрока; сохраняем её до перезаписи
        String playerBet = Flasher.NUMBER_BET;
        // Записываем выпавшее число, чтобы барабан остановился на нём
        Flasher.NUMBER_BET = String.valueOf(i);
        if (playerBet != null && playerBet.equalsIgnoreCase(String.valueOf(i))) {
            responseToBet(StringEnum.BET_WIN_GUESS_THE_COLOR.getValue());
        } else {
            responseToBet(StringEnum.NOT_WIN_GUESS_THE_COLOR.getValue());
        }
    }


    /** Считает результат тестовой ставки в игре "Угадай цвет": генерирует реальное число рулетки,
     * записывает его в Flasher.NUMBER_BET для отображения на колесе, и определяет победу
     * по совпадению цвета выпавшего числа с цветом ставки игрока. */
    void calculateForGuessTheColor() {
        Map<Boolean, String> result = Lotto.genNumberAndColor();
        boolean isBlack = result.containsKey(true);
        String number = isBlack ? result.get(true) : result.get(false);
        Flasher.NUMBER_BET = number;

        int n = Integer.parseInt(number);
        // 0 — зелёное, не красное и не чёрное: всегда проигрыш для ставок на цвет
        if (n == 0) {
            responseToBet(StringEnum.NOT_WIN_GUESS_THE_COLOR.getValue());
            return;
        }

        boolean playerBetBlack = Boolean.TRUE.equals(Flasher.COLOR_BET);
        boolean win = (playerBetBlack == isBlack);
        responseToBet(win
                ? StringEnum.BET_WIN_GUESS_THE_COLOR.getValue()
                : StringEnum.NOT_WIN_GUESS_THE_COLOR.getValue());
    }


    /**
     * Формирует текст результата по тегу исхода ставки (проигрыш/выигрыш/джекпот/реферал),
     * рассчитывает сумму тестового выигрыша по множителю текущей игры и при победе
     * зачисляет её на тестовый баланс (только если включён не "боевой" режим игры).
     */
    private void responseToBet(String tag) {
        boolean isNumber = testModeEnum.equals(TestModeEnum.GUESS_THE_NUMBER_GAME);
        double bet       = Double.parseDouble(Flasher.TEST_SAND_AMOUNT);
        double mult      = isNumber ? 35.0 : 2.0;
        // credit = полный возврат (ставка × множитель); balance корректен т.к. ставка уже списана
        // profit = чистая прибыль (то что видит пользователь в сообщении)
        String credit = String.valueOf(bet * mult);
        String profit = String.valueOf(bet * (mult - 1.0));

        String lotto = (random.nextInt(10001 - 4000) + 4000) + "";
        WalletRepository.getInstance().setLottoNow(lotto);

        if (tag.equals(StringEnum.NOT_WIN_GUESS_THE_COLOR.getValue())) {
            responseToBet(YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY, lotto, 1);

        } else if (tag.equals(StringEnum.BET_WIN_GUESS_THE_COLOR.getValue())) {
            if (!Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
                try { WalletRepository.getInstance().creditTestBalance(new java.math.BigDecimal(credit)); }
                catch (Exception ignored) {}
            }
            responseToBet(String.format(CONGRATULATIONS_YOUR_BET_IS_WON, profit), lotto, 2);

        } else if (tag.equals(StringEnum.LOTTO_WIN_GUESS_THE_COLOR.getValue())) {
            if (!Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
                try { WalletRepository.getInstance().creditTestBalance(new java.math.BigDecimal(credit)); }
                catch (Exception ignored) {}
            }
            responseToBet(String.format(CONGRATULATIONS_YOUR_BET_IS_WON_LOTTO, profit), lotto, 2);

        } else if (tag.equals(StringEnum.BECOME_A_REFERRAL.getValue())) {
            YourReferral.CODE = lotto;
            String s = YOUR_REFERRAL_CODE + " \n" + tag;
            responseToBet(s, lotto, 3);
        }
    }


    /**
     * Симулирует результат слот-машины: генерирует 3×3 матрицу символов с взвешенным RNG,
     * проверяет 5 линий выплат, рассчитывает выигрыш и передаёт результат в SlotFlasher.
     */
    void calculateForSlot() {
        // Веса символов из спецификации: XRP=30, Rocket=20, Moon=15, Diamond=10, Whale=5, Jackpot=1, Wild=3
        int[] weights = { 30, 20, 15, 10, 5, 1, 3 };
        // Множители выплат (Wild не выплачивает сам, только замещает)
        int[] multipliers = { 2, 5, 10, 20, 50, 250, 0 };
        int totalWeight = 0;
        for (int w : weights) totalWeight += w;

        // Генерируем матрицу 3×3
        int[][] matrix = new int[3][3];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                matrix[row][col] = weightedRandom(weights, totalWeight);
            }
        }
        SlotFlasher.RESULT_MATRIX = matrix;

        // 5 линий выплат: средняя, верхняя, нижняя, диагональ вниз, диагональ вверх
        int[][] paylines = {
            { matrix[1][0], matrix[1][1], matrix[1][2] }, // средняя строка (главная)
            { matrix[0][0], matrix[0][1], matrix[0][2] }, // верхняя
            { matrix[2][0], matrix[2][1], matrix[2][2] }, // нижняя
            { matrix[0][0], matrix[1][1], matrix[2][2] }, // диагональ ↘
            { matrix[2][0], matrix[1][1], matrix[0][2] }, // диагональ ↗
        };

        double bet = 0;
        try { bet = Double.parseDouble(SlotFlasher.BET_AMOUNT); } catch (Exception ignored) {}

        double totalPayout = 0;
        for (int[] line : paylines) {
            int sym = resolveLineSymbol(line);
            if (sym >= 0) {
                totalPayout += bet * multipliers[sym];
            }
        }

        boolean win = totalPayout > 0;
        String lotto = String.valueOf(random.nextInt(10001 - 4000) + 4000);
        WalletRepository.getInstance().setLottoNow(lotto);

        if (win && !Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE)) {
            try { WalletRepository.getInstance().creditTestBalance(new java.math.BigDecimal(totalPayout)); }
            catch (Exception ignored) {}
        }

        double profit = totalPayout - bet;
        String msg = win
                ? String.format(CONGRATULATIONS_YOUR_BET_IS_WON, String.valueOf((long) profit))
                : YOUR_BET_IS_LOST_TRY_AGAIN_AND_YOU_WILL_BE_LUCKY;

        responseToBetSlot(msg, lotto, win ? 2 : 1);
    }

    /** Генерирует символ по взвешенному RNG. */
    private int weightedRandom(int[] weights, int total) {
        int r = random.nextInt(total);
        int acc = 0;
        for (int i = 0; i < weights.length; i++) {
            acc += weights[i];
            if (r < acc) return i;
        }
        return weights.length - 1;
    }

    /**
     * Проверяет, выигрывает ли линия из 3 символов.
     * Wild (индекс 6) замещает любой символ.
     * Возвращает индекс выигравшего символа или -1 (нет выигрыша).
     */
    private int resolveLineSymbol(int[] line) {
        // Collect non-wild symbols
        int found = -1;
        for (int s : line) {
            if (s == SlotReelView.SYM_WILD) continue;
            if (found == -1) { found = s; }
            else if (found != s) { return -1; } // two different non-wild → no win
        }
        return found; // -1 if all wild (pays as jackpot? for now no special case)
    }

    /**
     * Доставляет результат слота в SlotFlasher (если он открыт),
     * иначе кладёт в WalletRepository для показа позже.
     */
    private void responseToBetSlot(String text, String lotto, int outcome) {
        com.samuilolegovich.view.SlotFlasher sf = SlotFlasher.SLOT_FLASHER;
        if (SlotFlasher.VISIBLE_ON_SCREEN && sf != null) {
            sf.stopGame(text, outcome == 2);
        } else {
            WalletRepository.getInstance().notifyEvent(text, lotto, outcome);
        }
    }

    /**
     * Доставляет готовый текст результата игроку: если экран соответствующей игры открыт — показывает его
     * прямо там (stopGame), иначе (или для реферального кода) кладёт уведомление в WalletRepository
     * для показа позже.
     */
    private void responseToBet(String text, String lotto, int i) {
        Flasher flasher = Flasher.FLASHER;
        if (Flasher.VISIBLE_ON_SCREEN && flasher != null) {
            switch (i) {
                case 1:
                    flasher.stopGame(text, false);
                    break;
                case 2:
                    flasher.stopGame(text, true);
                    break;
                case 3:
                    WalletRepository.getInstance().notifyEvent(text, lotto, i);
                    break;
            }
        } else {
            WalletRepository.getInstance().notifyEvent(text, lotto, i);
        }
    }
}

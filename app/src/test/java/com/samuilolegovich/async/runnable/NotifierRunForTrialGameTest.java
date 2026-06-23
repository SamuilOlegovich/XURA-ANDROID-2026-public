package com.samuilolegovich.async.runnable;

import com.samuilolegovich.enums.TestModeEnum;
import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.view.Flasher;
import com.samuilolegovich.wallet.repository.WalletRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Регрессионные тесты для NotifierRunForTrialGame.
 *
 * Основной регресс: calculateForGuessTheNumber обязан записывать ВЫПАВШЕЕ число
 * в Flasher.NUMBER_BET (а не оставлять там ставку игрока), иначе барабан
 * останавливается на числе игрока и всегда показывает проигрыш.
 *
 * Инфраструктура:
 *  - WalletRepository.instance инжектируется через reflection (нет setter-а).
 *  - Flasher.VISIBLE_ON_SCREEN = false — responseToBet идёт в notifyEvent (не stopGame).
 *  - package-private конструктор NotifierRunForTrialGame пропускает Android-ресурсы.
 */
public class NotifierRunForTrialGameTest {

    private static final String LOST    = "lost";
    private static final String WON     = "won %s";
    private static final String WON_L   = "wonLoto %s";
    private static final String REF     = "ref";

    private WalletRepository mockRepo;

    private NotifierRunForTrialGame runnerNumber;
    private NotifierRunForTrialGame runnerColor;
    private NotifierRunForTrialGame runnerRoulette;

    @Before
    public void setUp() throws Exception {
        mockRepo = mock(WalletRepository.class);
        injectRepo(mockRepo);

        Flasher.VISIBLE_ON_SCREEN = false;
        Flasher.FLASHER           = null;

        runnerNumber   = new NotifierRunForTrialGame(TestModeEnum.GUESS_THE_NUMBER_GAME,
                LOST, WON, WON_L, REF);
        runnerColor    = new NotifierRunForTrialGame(TestModeEnum.GUESS_THE_COLOR_GAME,
                LOST, WON, WON_L, REF);
        runnerRoulette = new NotifierRunForTrialGame(TestModeEnum.ROULETTE_GAME,
                LOST, WON, WON_L, REF);
    }

    @After
    public void tearDown() throws Exception {
        injectRepo(null);
        Flasher.VISIBLE_ON_SCREEN = false;
        Flasher.FLASHER           = null;
        Flasher.NUMBER_BET        = null;
        Flasher.COLOR_BET         = null;
        Flasher.ROULETTE_ALL_BETS = null;
        Flasher.TEST_SAND_AMOUNT  = null;
    }

    // ── Угадай число ─────────────────────────────────────────────────────────

    /** Регресс: NUMBER_BET должен стать выпавшим числом, а не остаться ставкой игрока. */
    @Test
    public void guessNumber_numberBet_updatedToDrawnNumber() {
        Flasher.NUMBER_BET      = "7";   // ставка игрока
        Flasher.TEST_SAND_AMOUNT = "1";

        runnerNumber.calculateForGuessTheNumber(15);

        assertEquals("15", Flasher.NUMBER_BET);
    }

    @Test
    public void guessNumber_numberBet_notLeftAsPlayerBet_onMismatch() {
        Flasher.NUMBER_BET      = "7";
        Flasher.TEST_SAND_AMOUNT = "1";

        runnerNumber.calculateForGuessTheNumber(22);

        assertNotEquals("7", Flasher.NUMBER_BET);
    }

    @Test
    public void guessNumber_win_creditCalledWhenDrawnMatchesPlayerBet() {
        Flasher.NUMBER_BET      = "7";
        Flasher.TEST_SAND_AMOUNT = "1";

        runnerNumber.calculateForGuessTheNumber(7); // совпадение

        verify(mockRepo).creditTestBalance(any(BigDecimal.class));
    }

    @Test
    public void guessNumber_lose_creditNotCalledWhenDrawnDiffers() {
        Flasher.NUMBER_BET      = "7";
        Flasher.TEST_SAND_AMOUNT = "1";

        runnerNumber.calculateForGuessTheNumber(15); // не совпадает

        verify(mockRepo, never()).creditTestBalance(any());
    }

    // ── Угадай цвет ──────────────────────────────────────────────────────────

    /** NUMBER_BET должен быть заполнен валидным числом рулетки (0–36). */
    @Test
    public void guessColor_numberBet_setToValidDrawnNumber() {
        Flasher.COLOR_BET       = false; // игрок ставит красный
        Flasher.TEST_SAND_AMOUNT = "1";

        runnerColor.calculateForGuessTheColor();

        assertNotNull(Flasher.NUMBER_BET);
        int n = Integer.parseInt(Flasher.NUMBER_BET);
        assertTrue(n >= 0 && n <= 36);
    }

    /** Кредит начисляется тогда и только тогда, когда цвет выпавшего числа совпадает со ставкой игрока. */
    @Test
    public void guessColor_winLossConsistentWithDrawnColor() {
        Flasher.COLOR_BET       = true; // игрок ставит чёрный
        Flasher.TEST_SAND_AMOUNT = "1";

        runnerColor.calculateForGuessTheColor();

        int drawnN = Integer.parseInt(Flasher.NUMBER_BET);
        if (drawnN == 0) {
            // ноль — не красный и не чёрный → всегда проигрыш
            verify(mockRepo, never()).creditTestBalance(any());
        } else {
            boolean drawnIsBlack = Lotto.learnTheColorOfNumber(Flasher.NUMBER_BET);
            if (drawnIsBlack) {                          // ставка чёрный совпала → выигрыш
                verify(mockRepo).creditTestBalance(any(BigDecimal.class));
            } else {                                     // выпал красный → проигрыш
                verify(mockRepo, never()).creditTestBalance(any());
            }
        }
    }

    // ── Рулетка ──────────────────────────────────────────────────────────────

    @Test
    public void roulette_numberBet_updatedToWinNumber() {
        Flasher.ROULETTE_ALL_BETS = bets("RED", "1.0");

        runnerRoulette.calculateForRoulette(7);

        assertEquals("7", Flasher.NUMBER_BET);
    }

    @Test
    public void roulette_numberBet_updatedToZero_onZeroWin() {
        Flasher.ROULETTE_ALL_BETS = bets("N:0", "1.0");

        runnerRoulette.calculateForRoulette(0);

        assertEquals("0", Flasher.NUMBER_BET);
    }

    @Test
    public void roulette_win_onMatchingStraightNumberBet() {
        Flasher.ROULETTE_ALL_BETS = bets("N:7", "1.0"); // ставка прямо на 7

        runnerRoulette.calculateForRoulette(7); // выпало 7

        verify(mockRepo).creditTestBalance(any(BigDecimal.class));
    }

    @Test
    public void roulette_lose_whenStraightNumberNotMatched() {
        Flasher.ROULETTE_ALL_BETS = bets("N:7", "1.0");

        runnerRoulette.calculateForRoulette(15); // выпало другое

        verify(mockRepo, never()).creditTestBalance(any());
    }

    @Test
    public void roulette_lose_colorBetOnZero() {
        // Красный/чёрный проигрывают при зеро
        Flasher.ROULETTE_ALL_BETS = bets("RED", "1.0");

        runnerRoulette.calculateForRoulette(0);

        verify(mockRepo, never()).creditTestBalance(any());
    }

    @Test
    public void roulette_win_colorBetOnMatchingColor() {
        // 7 — красное число в рулетке; ставка RED должна выиграть
        Flasher.ROULETTE_ALL_BETS = bets("RED", "1.0");

        runnerRoulette.calculateForRoulette(7); // 7 — красное

        verify(mockRepo).creditTestBalance(any(BigDecimal.class));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static LinkedHashMap<String, BigDecimal> bets(String tag, String amount) {
        LinkedHashMap<String, BigDecimal> m = new LinkedHashMap<>();
        m.put(tag, new BigDecimal(amount));
        return m;
    }

    private static void injectRepo(WalletRepository repo) throws Exception {
        Field f = WalletRepository.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, repo);
    }
}

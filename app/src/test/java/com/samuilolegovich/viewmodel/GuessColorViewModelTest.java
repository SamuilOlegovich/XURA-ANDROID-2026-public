package com.samuilolegovich.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.config.NetworkConfig;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.wallet.repository.WalletRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для GuessColorViewModel: валидация суммы ставки и ветвление
 * реальный/тестовый режим игры при размещении ставки на цвет.
 *
 * placeBet выполняется на внутреннем фоновом ExecutorService и публикует
 * результат через postValue. InstantTaskExecutorRule делает диспетчер LiveData
 * синхронным (без неё postValue не работает в чистом JVM-тесте — нет реального
 * Android Looper), а CountDownLatch в awaitValue ждёт завершения именно фоновой
 * задачи, на которую тест не имеет прямой ссылки.
 */
@RunWith(MockitoJUnitRunner.class)
public class GuessColorViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private WalletRepository repository;

    private GuessColorViewModel viewModel;

    private static final String RED_TAG = StringEnum.TAG_RED_GUESS_THE_COLOR.getValue();
    private static final String BLACK_TAG = StringEnum.TAG_BLACK_GUESS_THE_NUMBER.getValue();

    @Before
    public void setUp() {
        viewModel = new GuessColorViewModel(repository);
        MainActivity.IS_REAL_GAME_MODE = false;
    }

    @After
    public void resetGameMode() {
        MainActivity.IS_REAL_GAME_MODE = false;
    }

    private void stubBalance(String balance) {
        when(repository.getBalanceLiveData()).thenReturn(new MutableLiveData<>(new BigDecimal(balance)));
    }

    private <T> T awaitValue(LiveData<T> liveData) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> ref = new AtomicReference<>();
        Observer<T> observer = value -> { ref.set(value); latch.countDown(); };
        liveData.observeForever(observer);
        try {
            assertTrue("Не получено значение LiveData за отведённое время", latch.await(2, TimeUnit.SECONDS));
            return ref.get();
        } finally {
            liveData.removeObserver(observer);
        }
    }

    // -------------------------------------------------------------------------
    // Валидация суммы
    // -------------------------------------------------------------------------

    @Test
    public void placeBet_emptyAmount_postsInvalidAmount() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBet("", RED_TAG, "ref");
        assertEquals(GameBetError.INVALID_AMOUNT, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBet_nonNumericAmount_postsInvalidAmount() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBet("abc", RED_TAG, "ref");
        assertEquals(GameBetError.INVALID_AMOUNT, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBet_zeroAmount_postsAmountIsZero() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBet("0", RED_TAG, "ref");
        assertEquals(GameBetError.AMOUNT_IS_ZERO, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBet_amountAboveBalance_postsInsufficientBalance() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBet("600", RED_TAG, "ref");
        assertEquals(GameBetError.INSUFFICIENT_BALANCE, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBet_amountAboveMax_postsBetTooHigh() throws InterruptedException {
        stubBalance("1000");
        viewModel.placeBet("150", RED_TAG, "ref"); // 150 > MAX_BET_GUESS_THE_COLOR (100), но <= баланса
        assertEquals(GameBetError.BET_TOO_HIGH, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBet_amountBelowMin_postsBetTooLow() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBet("0.05", RED_TAG, "ref"); // 0.05 < MIN_BET_GUESS_THE_COLOR (0.1)
        assertEquals(GameBetError.BET_TOO_LOW, awaitValue(viewModel.getError()));
    }

    // -------------------------------------------------------------------------
    // Тестовый режим (без реальных XRP)
    // -------------------------------------------------------------------------

    @Test
    public void placeBet_testMode_validAmount_deductsTestBalanceAndPostsSuccess() throws InterruptedException {
        MainActivity.IS_REAL_GAME_MODE = false;
        stubBalance("500");

        viewModel.placeBet("50", RED_TAG, "ref");

        assertEquals("50", awaitValue(viewModel.getBetSuccess()));
        verify(repository).deductTestBalance(new BigDecimal("50"));
        verify(repository, never()).sendPayment(anyString(), anyString(), any(BigDecimal.class));
    }

    // -------------------------------------------------------------------------
    // Реальный режим (платёж на сервер игры)
    // -------------------------------------------------------------------------

    @Test
    public void placeBet_realMode_redTag_buildsCorrectMemoAndUpdatesBalance() throws InterruptedException {
        MainActivity.IS_REAL_GAME_MODE = true;
        stubBalance("500");
        when(repository.sendPayment(eq(NetworkConfig.SERVER_COLOR), eq("BET:RED:ref"), eq(new BigDecimal("50"))))
                .thenReturn(true);
        when(repository.getBalance()).thenReturn(new BigDecimal("450"));

        viewModel.placeBet("50", RED_TAG, "ref");

        assertEquals("50", awaitValue(viewModel.getBetSuccess()));
        verify(repository).sendPayment(NetworkConfig.SERVER_COLOR, "BET:RED:ref", new BigDecimal("50"));
        verify(repository).updateBalance(new BigDecimal("450"));
    }

    @Test
    public void placeBet_realMode_blackTag_buildsCorrectMemo() throws InterruptedException {
        MainActivity.IS_REAL_GAME_MODE = true;
        stubBalance("500");
        when(repository.sendPayment(eq(NetworkConfig.SERVER_COLOR), eq("BET:BLK:ref"), eq(new BigDecimal("50"))))
                .thenReturn(true);
        when(repository.getBalance()).thenReturn(new BigDecimal("450"));

        viewModel.placeBet("50", BLACK_TAG, "ref");

        assertEquals("50", awaitValue(viewModel.getBetSuccess()));
        verify(repository).sendPayment(NetworkConfig.SERVER_COLOR, "BET:BLK:ref", new BigDecimal("50"));
    }

    @Test
    public void placeBet_realMode_failedPayment_postsPaymentFailed() throws InterruptedException {
        MainActivity.IS_REAL_GAME_MODE = true;
        stubBalance("500");
        when(repository.sendPayment(anyString(), anyString(), any(BigDecimal.class))).thenReturn(false);

        viewModel.placeBet("50", RED_TAG, "ref");

        assertEquals(GameBetError.PAYMENT_FAILED, awaitValue(viewModel.getError()));
        verify(repository, never()).updateBalance(any());
    }
}

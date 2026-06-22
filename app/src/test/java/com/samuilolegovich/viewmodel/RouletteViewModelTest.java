package com.samuilolegovich.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.config.NetworkConfig;
import com.samuilolegovich.wallet.repository.WalletRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для RouletteViewModel: валидация набора ставок (несколько ставок сразу),
 * построение memo через RouletteBetCode и ветвление реальный/тестовый режим.
 * В отличие от GuessColor/GuessNumber, после успеха здесь ВСЕГДА вызывается
 * repository.updateBalance(repository.getBalance()) независимо от режима —
 * отдельной ветки deductTestBalance в этом ViewModel нет.
 */
@RunWith(MockitoJUnitRunner.class)
public class RouletteViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private WalletRepository repository;

    private RouletteViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new RouletteViewModel(repository);
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

    private static LinkedHashMap<String, BigDecimal> betsOf(String tag, String amount) {
        LinkedHashMap<String, BigDecimal> bets = new LinkedHashMap<>();
        bets.put(tag, new BigDecimal(amount));
        return bets;
    }

    // -------------------------------------------------------------------------
    // Валидация набора ставок
    // -------------------------------------------------------------------------

    @Test
    public void placeBets_nullBets_postsNoNumberSelected() throws InterruptedException {
        viewModel.placeBets(null, "ref");
        assertEquals(GameBetError.NO_NUMBER_SELECTED, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBets_emptyBets_postsNoNumberSelected() throws InterruptedException {
        viewModel.placeBets(new LinkedHashMap<>(), "ref");
        assertEquals(GameBetError.NO_NUMBER_SELECTED, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBets_singleBetBelowMin_postsBetTooLow() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBets(betsOf("RED", "0.05"), "ref"); // < MIN_BET_ROULETTE (0.1)
        assertEquals(GameBetError.BET_TOO_LOW, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBets_totalAboveMax_postsBetTooHigh() throws InterruptedException {
        stubBalance("1000");
        LinkedHashMap<String, BigDecimal> bets = new LinkedHashMap<>();
        bets.put("RED", new BigDecimal("60"));
        bets.put("D1", new BigDecimal("60")); // итого 120 > MAX_BET_ROULETTE (100)
        viewModel.placeBets(bets, "ref");
        assertEquals(GameBetError.BET_TOO_HIGH, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBets_totalAboveBalance_postsInsufficientBalance() throws InterruptedException {
        stubBalance("50");
        viewModel.placeBets(betsOf("RED", "80"), "ref"); // 80 <= MAX (100), но > баланса (50)
        assertEquals(GameBetError.INSUFFICIENT_BALANCE, awaitValue(viewModel.getError()));
    }

    // -------------------------------------------------------------------------
    // Тестовый режим
    // -------------------------------------------------------------------------

    @Test
    public void placeBets_testMode_validBets_deductsBalanceAndPostsSuccess() throws InterruptedException {
        MainActivity.IS_REAL_GAME_MODE = false;
        stubBalance("100");

        viewModel.placeBets(betsOf("RED", "30"), "ref");

        assertEquals("30", awaitValue(viewModel.getBetSuccess()));
        verify(repository).deductTestBalance(new BigDecimal("30"));
        verify(repository, never()).sendPayment(anyString(), anyString(), any(BigDecimal.class));
    }

    // -------------------------------------------------------------------------
    // Реальный режим
    // -------------------------------------------------------------------------

    @Test
    public void placeBets_realMode_multipleBets_buildsCorrectMemo() throws InterruptedException {
        MainActivity.IS_REAL_GAME_MODE = true;
        stubBalance("100");
        LinkedHashMap<String, BigDecimal> bets = new LinkedHashMap<>();
        bets.put("RED", new BigDecimal("30"));
        bets.put("D1", new BigDecimal("20"));

        when(repository.sendPayment(eq(NetworkConfig.SERVER_ROULETTE), eq("BET:R:r@30,d1@20:ref123"), eq(new BigDecimal("50"))))
                .thenReturn(true);
        when(repository.getBalance()).thenReturn(new BigDecimal("50"));

        viewModel.placeBets(bets, "ref123");

        assertEquals("50", awaitValue(viewModel.getBetSuccess()));
        verify(repository).sendPayment(NetworkConfig.SERVER_ROULETTE, "BET:R:r@30,d1@20:ref123", new BigDecimal("50"));
        verify(repository).updateBalance(new BigDecimal("50"));
    }

    @Test
    public void placeBets_realMode_failedPayment_postsPaymentFailed() throws InterruptedException {
        MainActivity.IS_REAL_GAME_MODE = true;
        stubBalance("100");
        when(repository.sendPayment(anyString(), anyString(), any(BigDecimal.class))).thenReturn(false);

        viewModel.placeBets(betsOf("RED", "30"), "ref");

        assertEquals(GameBetError.PAYMENT_FAILED, awaitValue(viewModel.getError()));
        verify(repository, never()).updateBalance(any());
    }
}

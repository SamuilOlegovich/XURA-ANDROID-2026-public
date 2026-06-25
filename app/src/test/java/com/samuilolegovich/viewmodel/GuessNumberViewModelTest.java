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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для GuessNumberViewModel: валидация выбранного числа (1-36) и суммы
 * ставки, ветвление реальный/тестовый режим. Сумма ставки в этом ViewModel
 * валидируется по лимитам MAX/MIN_BET_GUESS_THE_COLOR (общий денежный лимит
 * ставки для игр), а не по MAX/MIN_BET_GUESS_THE_NUMBER — те используются
 * только для диапазона самого выбираемого числа. Тесты ниже отражают именно
 * это реальное поведение продакшен-кода.
 */
@RunWith(MockitoJUnitRunner.class)
public class GuessNumberViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private WalletRepository repository;

    private GuessNumberViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new GuessNumberViewModel(repository);
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
    // Валидация выбранного числа (диапазон 1-36)
    // -------------------------------------------------------------------------

    @Test
    public void placeBet_numberZero_postsNoNumberSelected() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBet("10", 0, "ref");
        assertEquals(GameBetError.NO_NUMBER_SELECTED, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBet_negativeNumber_postsNoNumberSelected() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBet("10", -1, "ref");
        assertEquals(GameBetError.NO_NUMBER_SELECTED, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBet_numberAboveRange_postsNoNumberSelected() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBet("10", 37, "ref");
        assertEquals(GameBetError.NO_NUMBER_SELECTED, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBet_boundaryNumbers_passNumberValidation() throws InterruptedException {
        MainActivity.IS_REAL_GAME_MODE = false;
        stubBalance("500");
        viewModel.placeBet("10", 1, "ref");
        assertEquals("10", awaitValue(viewModel.getBetSuccess()));
    }

    // -------------------------------------------------------------------------
    // Валидация суммы ставки (после прохождения проверки числа)
    // -------------------------------------------------------------------------

    @Test
    public void placeBet_emptyAmount_postsInvalidAmount() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBet("", 5, "ref");
        assertEquals(GameBetError.INVALID_AMOUNT, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBet_zeroAmount_postsAmountIsZero() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBet("0", 5, "ref");
        assertEquals(GameBetError.AMOUNT_IS_ZERO, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBet_amountAboveBalance_postsInsufficientBalance() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBet("600", 5, "ref");
        assertEquals(GameBetError.INSUFFICIENT_BALANCE, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBet_amountAboveMax_postsBetTooHigh() throws InterruptedException {
        stubBalance("1000");
        viewModel.placeBet("150", 5, "ref");
        assertEquals(GameBetError.BET_TOO_HIGH, awaitValue(viewModel.getError()));
    }

    @Test
    public void placeBet_amountBelowMin_postsBetTooLow() throws InterruptedException {
        stubBalance("500");
        viewModel.placeBet("0.05", 5, "ref");
        assertEquals(GameBetError.BET_TOO_LOW, awaitValue(viewModel.getError()));
    }

    // -------------------------------------------------------------------------
    // Тестовый режим
    // -------------------------------------------------------------------------

    @Test
    public void placeBet_testMode_validBet_deductsTestBalanceAndPostsSuccess() throws InterruptedException {
        MainActivity.IS_REAL_GAME_MODE = false;
        stubBalance("500");

        viewModel.placeBet("50", 17, "ref");

        assertEquals("50", awaitValue(viewModel.getBetSuccess()));
        verify(repository).deductTestBalance(new BigDecimal("50"));
        verify(repository, never()).sendPayment(anyString(), anyString(), any(BigDecimal.class));
    }

    // -------------------------------------------------------------------------
    // Реальный режим
    // -------------------------------------------------------------------------

    @Test
    public void placeBet_realMode_validBet_sendsPaymentWithNumberMemo() throws InterruptedException {
        MainActivity.IS_REAL_GAME_MODE = true;
        stubBalance("500");
        when(repository.sendPayment(eq(NetworkConfig.SERVER_ROULETTE), eq("BET:R:n17@50:ref"), eq(new BigDecimal("50"))))
                .thenReturn(true);
        when(repository.getBalance()).thenReturn(new BigDecimal("450"));

        viewModel.placeBet("50", 17, "ref");

        assertEquals("50", awaitValue(viewModel.getBetSuccess()));
        verify(repository).sendPayment(NetworkConfig.SERVER_ROULETTE, "BET:R:n17@50:ref", new BigDecimal("50"));
        verify(repository).updateBalance(new BigDecimal("450"));
    }

    @Test
    public void placeBet_realMode_failedPayment_postsPaymentFailed() throws InterruptedException {
        MainActivity.IS_REAL_GAME_MODE = true;
        stubBalance("500");
        when(repository.sendPayment(anyString(), anyString(), any(BigDecimal.class))).thenReturn(false);

        viewModel.placeBet("50", 17, "ref");

        assertEquals(GameBetError.PAYMENT_FAILED, awaitValue(viewModel.getError()));
        verify(repository, never()).updateBalance(any());
    }
}

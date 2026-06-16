package com.samuilolegovich.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.samuilolegovich.wallet.repository.WalletRepository;

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
 * Тесты для SendPaymentViewModel: валидация адреса/суммы/тега и обе перегрузки
 * отправки платежа (с числовым destination tag и без него).
 */
@RunWith(MockitoJUnitRunner.class)
public class SendPaymentViewModelTest {

    // Реальный XRPL classic address (34 символа) — используется только как валидная по длине строка
    private static final String VALID_ADDRESS = "rGrEJZaBFYhPGuyM7NiJbJw2yXVB9vJHah";

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private WalletRepository repository;

    private SendPaymentViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new SendPaymentViewModel(repository);
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
    // Валидация адреса
    // -------------------------------------------------------------------------

    @Test
    public void sendPayment_nullAddress_postsWrongAddress() throws InterruptedException {
        stubBalance("500");
        viewModel.sendPayment(null, "10", null);
        assertEquals(SendError.WRONG_ADDRESS, awaitValue(viewModel.getError()));
    }

    @Test
    public void sendPayment_tooShortAddress_postsWrongAddress() throws InterruptedException {
        stubBalance("500");
        viewModel.sendPayment("short", "10", null);
        assertEquals(SendError.WRONG_ADDRESS, awaitValue(viewModel.getError()));
    }

    // -------------------------------------------------------------------------
    // Валидация суммы
    // -------------------------------------------------------------------------

    @Test
    public void sendPayment_emptyAmount_postsInvalidAmount() throws InterruptedException {
        stubBalance("500");
        viewModel.sendPayment(VALID_ADDRESS, "", null);
        assertEquals(SendError.INVALID_AMOUNT, awaitValue(viewModel.getError()));
    }

    @Test
    public void sendPayment_zeroAmount_postsAmountIsZero() throws InterruptedException {
        stubBalance("500");
        viewModel.sendPayment(VALID_ADDRESS, "0", null);
        assertEquals(SendError.AMOUNT_IS_ZERO, awaitValue(viewModel.getError()));
    }

    @Test
    public void sendPayment_amountAboveBalance_postsInsufficientBalance() throws InterruptedException {
        stubBalance("500");
        viewModel.sendPayment(VALID_ADDRESS, "600", null);
        assertEquals(SendError.INSUFFICIENT_BALANCE, awaitValue(viewModel.getError()));
    }

    // -------------------------------------------------------------------------
    // Валидация destination tag
    // -------------------------------------------------------------------------

    @Test
    public void sendPayment_tagTooLong_postsTagTooLong() throws InterruptedException {
        stubBalance("500");
        viewModel.sendPayment(VALID_ADDRESS, "10", "123456789012"); // 12 символов > 11
        assertEquals(SendError.TAG_TOO_LONG, awaitValue(viewModel.getError()));
    }

    @Test
    public void sendPayment_nonNumericTag_postsTagTooLong() throws InterruptedException {
        stubBalance("500");
        viewModel.sendPayment(VALID_ADDRESS, "10", "abc");
        assertEquals(SendError.TAG_TOO_LONG, awaitValue(viewModel.getError()));
    }

    @Test
    public void sendPayment_tagAtOrAboveIntMax_postsTagTooLarge() throws InterruptedException {
        stubBalance("500");
        viewModel.sendPayment(VALID_ADDRESS, "10", "2147483648"); // Integer.MAX_VALUE + 1
        assertEquals(SendError.TAG_TOO_LARGE, awaitValue(viewModel.getError()));
    }

    // -------------------------------------------------------------------------
    // Успешная отправка
    // -------------------------------------------------------------------------

    @Test
    public void sendPayment_withoutTag_usesPlainPaymentOverload() throws InterruptedException {
        stubBalance("500");
        when(repository.sendPayment(eq(VALID_ADDRESS), eq(new BigDecimal("10")))).thenReturn(true);
        when(repository.getBalance()).thenReturn(new BigDecimal("490"));

        viewModel.sendPayment(VALID_ADDRESS, "10", null);

        assertTrue(awaitValue(viewModel.getPaymentSuccess()));
        verify(repository).sendPayment(VALID_ADDRESS, new BigDecimal("10"));
        verify(repository).updateBalance(new BigDecimal("490"));
    }

    @Test
    public void sendPayment_withTag_usesTaggedPaymentOverload() throws InterruptedException {
        stubBalance("500");
        when(repository.sendPayment(eq(VALID_ADDRESS), eq(Integer.valueOf(42)), eq(new BigDecimal("10"))))
                .thenReturn(true);
        when(repository.getBalance()).thenReturn(new BigDecimal("490"));

        viewModel.sendPayment(VALID_ADDRESS, "10", "42");

        assertTrue(awaitValue(viewModel.getPaymentSuccess()));
        verify(repository).sendPayment(VALID_ADDRESS, Integer.valueOf(42), new BigDecimal("10"));
    }

    @Test
    public void sendPayment_failedPayment_postsPaymentFailed() throws InterruptedException {
        stubBalance("500");
        when(repository.sendPayment(eq(VALID_ADDRESS), any(BigDecimal.class))).thenReturn(false);

        viewModel.sendPayment(VALID_ADDRESS, "10", null);

        assertEquals(SendError.PAYMENT_FAILED, awaitValue(viewModel.getError()));
        verify(repository, never()).updateBalance(any());
    }
}

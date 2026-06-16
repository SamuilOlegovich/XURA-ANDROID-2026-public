package com.samuilolegovich.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.samuilolegovich.wallet.repository.WalletRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для MainViewModel: делегирование LiveData-геттеров в WalletRepository
 * и асинхронная логика restoreAndInit (восстановление кошелька из сид-фразы).
 * Внутренний ExecutorService не инжектируется, поэтому асинхронные вызовы
 * репозитория проверяются через Mockito.verify(..., timeout(...)), который
 * опрашивает мок до получения вызова, а не ждёт фиксированное время.
 */
@RunWith(MockitoJUnitRunner.class)
public class MainViewModelTest {

    @Mock
    private WalletRepository repository;

    private MainViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new MainViewModel(repository);
    }

    // -------------------------------------------------------------------------
    // Делегирование LiveData
    // -------------------------------------------------------------------------

    @Test
    public void getBalance_delegatesToRepository() {
        LiveData<BigDecimal> balanceLiveData = new MutableLiveData<>();
        when(repository.getBalanceLiveData()).thenReturn(balanceLiveData);
        assertSame(balanceLiveData, viewModel.getBalance());
    }

    @Test
    public void getLottoText_delegatesToRepository() {
        LiveData<String> lottoLiveData = new MutableLiveData<>();
        when(repository.getLottoTextLiveData()).thenReturn(lottoLiveData);
        assertSame(lottoLiveData, viewModel.getLottoText());
    }

    @Test
    public void getNavigationEvent_delegatesToRepository() {
        LiveData<NavigationEvent> navigationLiveData = new MutableLiveData<>();
        when(repository.getNavigationEventLiveData()).thenReturn(navigationLiveData);
        assertSame(navigationLiveData, viewModel.getNavigationEvent());
    }

    @Test
    public void getWalletReady_delegatesToRepository() {
        LiveData<Boolean> walletReadyLiveData = new MutableLiveData<>();
        when(repository.getWalletReadyLiveData()).thenReturn(walletReadyLiveData);
        assertSame(walletReadyLiveData, viewModel.getWalletReady());
    }

    // -------------------------------------------------------------------------
    // loadBalance
    // -------------------------------------------------------------------------

    @Test
    public void loadBalance_delegatesToRepository() {
        viewModel.loadBalance();
        verify(repository).loadBalance();
    }

    // -------------------------------------------------------------------------
    // restoreAndInit — асинхронное восстановление кошелька
    // -------------------------------------------------------------------------

    @Test
    public void restoreAndInit_withValidSeed_updatesBalance() {
        Map<String, String> result = new HashMap<>();
        result.put("Classic Address", "rValidAddress");
        when(repository.restoreWallet("valid_seed")).thenReturn(result);
        when(repository.getBalance()).thenReturn(new BigDecimal("123.456789"));

        viewModel.restoreAndInit("valid_seed");

        verify(repository, timeout(2000)).restoreWallet("valid_seed");
        verify(repository, timeout(2000)).updateBalance(new BigDecimal("123.456789"));
    }

    @Test
    public void restoreAndInit_withNullResult_doesNotUpdateBalance() {
        when(repository.restoreWallet("bad_seed")).thenReturn(null);

        viewModel.restoreAndInit("bad_seed");

        verify(repository, timeout(2000)).restoreWallet("bad_seed");
        verify(repository, after(300).never()).getBalance();
        verify(repository, never()).updateBalance(any());
    }

    @Test
    public void restoreAndInit_withResultMissingClassicAddress_doesNotUpdateBalance() {
        Map<String, String> result = new HashMap<>();
        result.put("Some Other Key", "value");
        when(repository.restoreWallet("partial_seed")).thenReturn(result);

        viewModel.restoreAndInit("partial_seed");

        verify(repository, timeout(2000)).restoreWallet("partial_seed");
        verify(repository, after(300).never()).getBalance();
        verify(repository, never()).updateBalance(any());
    }
}

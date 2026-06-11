package com.samuilolegovich.di;

import com.samuilolegovich.wallet.repository.WalletRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;



@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public WalletRepository provideWalletRepository() {
        return WalletRepository.getInstance();
    }
}
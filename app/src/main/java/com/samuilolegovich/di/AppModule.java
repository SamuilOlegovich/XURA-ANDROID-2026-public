package com.samuilolegovich.di;

import com.samuilolegovich.wallet.repository.WalletRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;



/**
 * Hilt-модуль, связывающий ручной Singleton {@link WalletRepository} с DI-графом приложения,
 * чтобы его можно было внедрять через {@code @Inject} в Activity/Service вместо прямых вызовов getInstance().
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    /** Предоставляет единственный экземпляр WalletRepository для всего графа зависимостей приложения. */
    @Provides
    @Singleton
    public WalletRepository provideWalletRepository() {
        return WalletRepository.getInstance();
    }
}
package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.view.GuessTheColorGame;

import java.util.Map;



/**
 * Фоновая задача для игры "Угадай цвет": пока экран открыт, раз в секунду
 * подготавливает следующий случайный цвет, отличный от предыдущего.
 */
public class GenColorRun implements Runnable {
    public static volatile boolean FLAG = true;
    private boolean nextColor;



    /** Цикл работы потока: пока FLAG включён, раз в секунду обновляет следующий цвет, если игра видна на экране. */
    @Override
    public void run() {
        while (FLAG) {
            if (GuessTheColorGame.VISIBLE_ON_SCREEN) {
                genNextColor();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /** Генерирует случайный цвет, отличный от текущего значения nextColor, и запоминает его как следующий. */
    private void genNextColor() {
        Map<Boolean, String> map = Lotto.genNumberAndColor();
        while (map.containsKey(nextColor)) {
            map = Lotto.genNumberAndColor();
        }
        nextColor = map.containsKey(true);
    }
}
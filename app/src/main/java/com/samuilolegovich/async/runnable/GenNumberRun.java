package com.samuilolegovich.async.runnable;

import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.view.GuessTheNumberGame;

import java.util.Map;



/**
 * Фоновая задача для игры "Угадай число": пока экран открыт, раз в секунду
 * подготавливает следующее случайное значение, отличное от предыдущего.
 */
public class GenNumberRun implements Runnable {
    public static volatile boolean FLAG = true;
    private boolean nextColor;



    /** Цикл работы потока: пока FLAG включён, раз в секунду обновляет следующее значение, если игра видна на экране. */
    @Override
    public void run() {
        while (FLAG) {
            if (GuessTheNumberGame.VISIBLE_ON_SCREEN) {
                genNextColor();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /** Генерирует случайное значение, отличное от текущего nextColor, и запоминает его как следующее. */
    private void genNextColor() {
        Map<Boolean, String> map = Lotto.genNumberAndColor();
        while (map.containsKey(nextColor)) {
            map = Lotto.genNumberAndColor();
        }
        nextColor = map.containsKey(true);
    }
}
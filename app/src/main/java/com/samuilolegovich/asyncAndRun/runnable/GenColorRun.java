package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.view.GuessTheColorGame;

import java.util.Map;



public class GenColorRun implements Runnable {
    public static volatile boolean FLAG = true;
    private boolean nextColor;



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


    private void genNextColor() {
        Map<Boolean, String> map = Lotto.genNumberAndColor();
        while (map.containsKey(nextColor)) {
            map = Lotto.genNumberAndColor();
        }
        nextColor = map.containsKey(true);
    }
}
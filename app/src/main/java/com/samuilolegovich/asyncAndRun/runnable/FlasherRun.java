package com.samuilolegovich.asyncAndRun.runnable;

import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.view.Flasher;

import java.util.Map;

public class FlasherRun implements Runnable {
    public static volatile boolean FLAG = true;
    private boolean nextColor;

    @Override
    public void run() {
        while (FLAG) {
            if (Flasher.FLASHER != null) {
                genNumberAndColor();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                genNumberAndColor();
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void genNumberAndColor() {
        Map<Boolean, String> map = Lotto.genNumberAndColor();

        while (map.containsKey(nextColor)) {
            map = Lotto.genNumberAndColor();
        }
        if (map.containsKey(true)) {
            Flasher.FLASHER.setColorAndText(map.get(true), true);
            nextColor = true;
        } else {
            Flasher.FLASHER.setColorAndText(map.get(false), false);
            nextColor = false;
        }
    }
}

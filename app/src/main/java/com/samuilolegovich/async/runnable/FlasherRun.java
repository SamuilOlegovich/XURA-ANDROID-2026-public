package com.samuilolegovich.async.runnable;

import com.samuilolegovich.utils.Lotto;
import com.samuilolegovich.view.Flasher;

import java.util.Map;



/**
 * Фоновая задача для экрана "Flasher" (лотерея с быстрой сменой числа/цвета):
 * пока экран открыт, раз в ~300мс дважды подряд генерирует новое число и цвет,
 * создавая эффект мигающего табло, и обновляет отображаемое значение на экране.
 */
public class FlasherRun implements Runnable {
    public static volatile boolean FLAG = true;
    private boolean nextColor;



    /** Цикл работы потока: пока FLAG включён, периодически дёргает генерацию числа/цвета, если экран Flasher открыт. */
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


    /** Генерирует следующее число/цвет (отличное от предыдущего) и передаёт его на отображение в Flasher. */
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

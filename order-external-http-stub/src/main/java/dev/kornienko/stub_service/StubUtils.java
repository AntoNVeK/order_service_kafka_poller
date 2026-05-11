package dev.kornienko.stub_service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Вспомогательные утилиты для заглушки
 */
public class StubUtils {

    public static boolean chance(double chance) {
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    public static void randomSafeSleepMs(
            int min,
            int max
    ) {
        try {
            var sleepMs = ThreadLocalRandom.current().nextInt(min, max + 1);
            if (sleepMs <= 0) {
                return;
            }
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}

package com.belmu.butler.utility;

import java.util.concurrent.TimeUnit;

public class Duration {

    public static String getFormattedDuration(long duration) {
        long hours   = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

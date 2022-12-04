package com.belmu.butler.level;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GainExpEvent extends ListenerAdapter {

    public static Map<User, Long> cooldown = new HashMap<>();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User user = event.getMember().getUser();

        if(!user.isBot()) {
            LevelConfig.backupLevels();
            long currentTimeMillis = System.currentTimeMillis();

            if(cooldown.containsKey(user)) {
                if((cooldown.get(user)) <= currentTimeMillis) LevelUtils.addExp(user, randomXP(1, 2.5));
                else return;
            }
            cooldown.put(user, currentTimeMillis + (10 * 1000));
        }
    }

    public static double round(double value, int places) {
        if(places < 0) throw new IllegalArgumentException();
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    double randomXP(double min, double max) {
        return round(ThreadLocalRandom.current().nextDouble(min, max), 2);
    }
}

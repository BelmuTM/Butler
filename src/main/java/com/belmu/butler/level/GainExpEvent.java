package com.belmu.butler.level;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GainExpEvent extends ListenerAdapter {

    public static Map<User, Long> cooldown = new HashMap<>();

    private final int cooldownTime = 10; // seconds

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User user = event.getMember().getUser();
        if(user.isBot()) return;

        long currentTimeMillis = System.currentTimeMillis();

        if (cooldown.containsKey(user)) {
            if ((cooldown.get(user)) <= currentTimeMillis) Levels.addExp(user, randomXP(5, 10));
            else return;
        }
        cooldown.put(user, currentTimeMillis + (cooldownTime * 1000));

        if (Levels.hasPassedLevel(user)) {
            Member member = event.getMember();

            double newLvl = Levels.getLevel(user) + 1D;
            Levels.setLevel(user, newLvl);

            EmbedBuilder levelUp = new EmbedBuilder()
                    .setColor(member.getColor())
                    .setTitle(":green_circle: **Level Up!**")
                    .setDescription(member.getAsMention() + " Achieved Level " + new DecimalFormat("#").format(newLvl))
                    .setThumbnail(user.getAvatarUrl());
            event.getChannel().sendMessageEmbeds(levelUp.build()).queue();
        }

        LevelConfig.backupLevels();
    }

    public static double round(double value, int places) {
        if(places < 0) throw new IllegalArgumentException();
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    double randomXP(double min, double max) {
        return round(ThreadLocalRandom.current().nextDouble(min, max), 2);
    }
}

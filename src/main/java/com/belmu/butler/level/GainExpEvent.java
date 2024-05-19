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

    private final int cooldownTime = 10 * 1000; // milliseconds

    private final String[] levelUpEmojis = new String[] {
            ":tada:",
            ":sparkles:",
            ":hibiscus:",
            ":sunflower:",
            ":crown:",
            ":partying_face:",
            ":star_struck:",
            ":confetti_ball:"
    };

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        assert event.getMember() != null;
        User user = event.getMember().getUser();
        if(user.isBot()) return;

        long currentTimeMillis = System.currentTimeMillis();

        int oldLevel = Levels.getLevel(user);

        if (cooldown.containsKey(user)) {
            if ((cooldown.get(user)) <= currentTimeMillis) {
                Levels.addExp(user, round(ThreadLocalRandom.current().nextDouble(5, 10), 2));
                Levels.backupXp();
            }
            else return;
        }
        cooldown.put(user, currentTimeMillis + cooldownTime);

        int newLevel = Levels.getLevel(user);

        if (oldLevel < newLevel) {
            Member member = event.getMember();

            String emoji = levelUpEmojis[new Random().ints(0, levelUpEmojis.length).findFirst().getAsInt()];

            EmbedBuilder levelUp = new EmbedBuilder()
                    .setColor(member.getColor())
                    .setTitle(emoji + " **Level Up!**")
                    .setDescription(member.getAsMention() + " Achieved Level " + new DecimalFormat("#").format(newLevel))
                    .setThumbnail(user.getAvatarUrl());
            event.getChannel().sendMessageEmbeds(levelUp.build()).queue();
        }
    }

    public static double round(double value, int places) {
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}

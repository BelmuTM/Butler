package com.belmu.butler.level;

import com.belmu.butler.Butler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.*;

public class LevelUtils {

    public static LinkedHashMap<String, Double> sortedRanking = new LinkedHashMap<>();

    public static void setLevel(User user, double value) {
        LevelConfig.levelMap.put(user.getId(), value);
        LevelConfig.xpMap.put(user.getId(), calcXpForLevel(value));
    }

    public static Double getLevel(User user) {
        return LevelConfig.levelMap.computeIfAbsent(user.getId(), k -> 0D);
    }

    public static void setXp(User user, double value) {
        LevelConfig.xpMap.put(user.getId(), value);
    }

    public static Double getXp(User user) {
        return LevelConfig.xpMap.computeIfAbsent(user.getId(), k -> 0D);
    }

    public static void addExp(User user, double value) {
        double xp = getXp(user);
        setXp(user, xp + value);
    }

    private static final double BASE_XP = 100;
    private static final double EXPONENT = 1.36f;

    public static double calcXpForLevel(double level) {
        return (level == 0 ? 0 : BASE_XP) + (75 * Math.pow(level, EXPONENT));
    }

    private static double calculateFullTargetXp(double level) {
        double requiredXP = 0;
        for (int i = 0; i <= level; i++) requiredXP += calcXpForLevel(i);
        return requiredXP;
    }

    private static int calculateLevel(double xp) {
        int level = 0;
        double maxXp = calcXpForLevel(0);

        do {
            maxXp += calcXpForLevel(++level);
        } while (maxXp < xp);

        return level;
    }

    public static boolean hasPassedALevel(User user) {
        double nextLevel = getLevel(user) + 1D;
        return getXp(user) >= calcXpForLevel(nextLevel);
    }

    public static LinkedHashMap<String, Double> getGuildSortedRanking(Guild guild) {
        LinkedHashMap<String, Double> sorted = new LinkedHashMap<>();
        Map<String, Double> guildXpMap = new HashMap<>();

        for(Member member : guild.getMembers()) {
            if(!member.getUser().isBot()) guildXpMap.put(member.getId(), getXp(member.getUser()));
        }

        guildXpMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sorted.put(x.getKey(), x.getValue()));
        return sorted;
    }

    public static int getRank(User user, List keys) throws NullPointerException {
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).toString().equals(user.getId())) return i + 1;
        }
        throw new NullPointerException();
    }
}

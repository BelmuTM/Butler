package com.belmu.butler.level;

import com.belmu.butler.Butler;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.json.simple.JSONObject;

import java.lang.reflect.Type;
import java.util.*;

public class Levels {

    public static final String xpDataPath = "src/main/java/com/belmu/butler/data/xp_data.json";

    public static Map<String, Double>           globalXp      = new HashMap<>();
    public static LinkedHashMap<String, Double> globalRanking = new LinkedHashMap<>();

    public static void retrieveXpBackup() {
        long processStart = System.currentTimeMillis();

        JSONObject xpData = (JSONObject) Butler.dataParser.readJSON(xpDataPath);

        Type type = new TypeToken<Map<String, Double>>(){}.getType();
        globalXp = Butler.dataParser.gson.fromJson(xpData.toString(), type);

        globalXp.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> globalRanking.put(x.getKey(), x.getValue()));

        System.out.println("[INFO] Retrieved levels backup in " + (System.currentTimeMillis() - processStart) + "ms.");
    }

    public static void backupXp() {
        Butler.dataParser.writeJSON(xpDataPath, globalXp);
    }

    public static void startXpBackupRoutine() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                backupXp();
                System.out.println("[INFO] Saved levels backup on " + new java.util.Date());
            }
        }, 0L, 600000L); // 10 minutes
    }

    public static void setLevel(User user, int value) {
        globalXp.put(user.getId(), calculateXp(value));
    }

    public static int getLevel(User user) {
        return calculateLevel(globalXp.computeIfAbsent(user.getId(), k -> 0D));
    }

    public static void setXp(User user, double value) {
        globalXp.put(user.getId(), value);
    }

    public static Double getXp(User user) {
        return globalXp.computeIfAbsent(user.getId(), k -> 0D);
    }

    public static void addExp(User user, double value) {
        double xp = getXp(user);
        setXp(user, xp + value);
    }

    private static final double BASE_XP  = 100;
    private static final double EXPONENT = 1.36f;

    public static double calculateXp(int level) {
        return (level == 0 ? 0 : BASE_XP) + (75 * Math.pow(level, EXPONENT));
    }

    public static int calculateLevel(double xp) {
        if(xp < BASE_XP) return 0;
        else             return (int) Math.floor(Math.pow((xp - BASE_XP) / 75, 1.0 / EXPONENT));
    }

    public static LinkedHashMap<String, Double> getGuildSortedRanking(Guild guild) {
        LinkedHashMap<String, Double> sorted     = new LinkedHashMap<>();
        Map<String, Double>           guildXpMap = new HashMap<>();

        for(Member member : guild.getMembers()) {
            if(!member.getUser().isBot()) guildXpMap.put(member.getId(), getXp(member.getUser()));
        }

        guildXpMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sorted.put(x.getKey(), x.getValue()));
        return sorted;
    }

    public static int getRank(User user, ArrayList<String> uuidList) {
        return uuidList.indexOf(user.getId()) + 1;
    }
}

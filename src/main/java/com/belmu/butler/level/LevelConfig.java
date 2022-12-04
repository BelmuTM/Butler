package com.belmu.butler.level;

import com.belmu.butler.Butler;
import com.belmu.butler.DataManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class LevelConfig {

    private static final String levelsPath = Butler.dataPath + "levels.json";
    private static final JSONObject levels = (JSONObject) DataManager.readJSON(levelsPath);

    public static Map<String, Double> xpMap = new HashMap<>();
    public static Map<String, Double> levelMap = new HashMap<>();

    public static void backupLevels() {
        JSONObject root = new JSONObject();

        JSONArray xp = new JSONArray();
        JSONArray level = new JSONArray();

        JSONObject objectXp = new JSONObject();
        JSONObject objectLevel = new JSONObject();

        for (Map.Entry<String, Double> entry : xpMap.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();

            if(key != null) {
                objectXp.put(key, value);
            }
        }

        for (Map.Entry<String, Double> entry : levelMap.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();

            if(key != null) {
                objectLevel.put(key, value);
            }
        }

        xp.add(objectXp);
        level.add(objectLevel);

        root.put("xp", objectXp);
        root.put("level", objectLevel);

        try {
            Files.write(Paths.get(levelsPath), root.toJSONString().getBytes());
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void retrieveBackup() {
        long processStart = System.currentTimeMillis();
        JSONObject backupXp = (JSONObject) levels.get("xp");
        JSONObject backupLevel = (JSONObject) levels.get("level");

        for(Object key : backupXp.keySet()) {
            String backupValue = backupXp.get(key).toString();

            String id = key.toString();
            Double value = Double.parseDouble(backupValue);

            xpMap.put(id, value);
        }

        for(Object key : backupLevel.keySet()) {
            String backupValue = backupLevel.get(key).toString();

            String id = key.toString();
            Double value = Double.parseDouble(backupValue);

            levelMap.put(id, value);
        }

        LevelConfig.xpMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> LevelUtils.sortedRanking.put(x.getKey(), x.getValue()));

        System.out.println("[INFO] Retrieved levels backup in " + (System.currentTimeMillis() - processStart) + "ms.");
    }
}

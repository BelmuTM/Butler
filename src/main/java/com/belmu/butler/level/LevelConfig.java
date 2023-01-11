package com.belmu.butler.level;

import com.belmu.butler.Butler;
import com.belmu.butler.DataParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class LevelConfig {

    public static Map<String, Double> xpMap = new HashMap<>();
    public static Map<String, Double> levelMap = new HashMap<>();

    public static void backupLevels() {

        final JSONArray xp = new JSONArray();
        final JSONArray level = new JSONArray();

        final JSONObject objectXp = new JSONObject();
        final JSONObject objectLevel = new JSONObject();

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

        Butler.data.put("xp", objectXp);
        Butler.data.put("level", objectLevel);

        DataParser.writeJSON(Butler.dataPath, Butler.data);
    }

    public static void retrieveBackup() {
        long processStart = System.currentTimeMillis();
        final Object xpObject = Butler.data.get("xp");
        final Object levelObject = Butler.data.get("level");

        if(!xpObject.toString().isEmpty()) {
            JSONObject backupXp = (JSONObject) xpObject;

            for (Object key : backupXp.keySet()) {
                String backupValue = backupXp.get(key).toString();

                String id = key.toString();
                Double value = Double.parseDouble(backupValue);

                xpMap.put(id, value);
            }
        }

        if(!levelObject.toString().isEmpty()) {
            JSONObject backupLevel = (JSONObject) levelObject;

            for (Object key : backupLevel.keySet()) {
                String backupValue = backupLevel.get(key).toString();

                String id = key.toString();
                Double value = Double.parseDouble(backupValue);

                levelMap.put(id, value);
            }
        }

        LevelConfig.xpMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> Levels.sortedRanking.put(x.getKey(), x.getValue()));

        System.out.println("[INFO] Retrieved levels backup in " + (System.currentTimeMillis() - processStart) + "ms.");
    }
}

package com.belmu.butler;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataManager {

    public static void writeJSON(String path, String object, String value) {
        try {
            JSONObject sampleObject = new JSONObject();
            sampleObject.put(object, value);

            Files.write(Paths.get(path), sampleObject.toJSONString().getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Object readJSON(String path) {
        try {
            FileReader reader = new FileReader(path);
            JSONParser jsonParser = new JSONParser();

            return jsonParser.parse(reader);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

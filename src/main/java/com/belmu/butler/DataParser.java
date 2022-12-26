package com.belmu.butler;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataParser {

    public static void writeJSON(String path, JSONObject object) {
        try {
            Files.write(Paths.get(path), object.toJSONString().getBytes());
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

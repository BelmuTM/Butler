package com.belmu.butler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class DataParser {

    public final Gson gson;

    public DataParser() {
        this.gson = new GsonBuilder().create();
    }

    public void writeJSON(String path, Object object) {
        try (Writer writer = new FileWriter(path)) {
            this.gson.toJson(object, writer);
        } catch(IOException ioe) {
            System.out.println("Error: " + ioe.getMessage());
        }
    }

    public Object readJSON(String path) {
        try (FileReader reader = new FileReader(path)) {
            return new JSONParser().parse(reader);
        } catch (IOException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }
}

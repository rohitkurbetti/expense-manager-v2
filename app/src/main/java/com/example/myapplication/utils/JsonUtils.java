package com.example.myapplication.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonUtils {
    public static boolean isValidJson(String json) {
        try {
            JsonElement jsonElement = JsonParser.parseString(json);
            return jsonElement.isJsonObject(); // Ensure it's a valid JSON object
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
}

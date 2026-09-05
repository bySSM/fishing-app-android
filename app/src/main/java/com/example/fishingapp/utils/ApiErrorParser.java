// app/src/main/java/com/example/fishingapp/utils/ApiErrorParser.java
package com.example.fishingapp.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.ResponseBody;

public class ApiErrorParser {

    public static String extractMessage(ResponseBody errorBody, int httpCode) {
        if (errorBody == null) {
            return "Ошибка сервера (" + httpCode + ")";
        }

        try {
            String raw = errorBody.string();
            if (raw == null || raw.isEmpty()) {
                return "Ошибка сервера (" + httpCode + ")";
            }

            JSONObject json = new JSONObject(raw);

            if (json.has("error")) {
                return json.optString("error", "Ошибка сервера (" + httpCode + ")");
            }

            StringBuilder sb = new StringBuilder();
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = json.optString(key, null);
                if (value != null && !value.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(value);
                }
            }

            return sb.length() > 0 ? sb.toString() : ("Ошибка (" + httpCode + ")");

        } catch (IOException | JSONException e) {
            return "Ошибка сервера (" + httpCode + ")";
        }
    }
}
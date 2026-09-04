// app/src/main/java/com/example/fishingapp/utils/TokenManager.java
package com.example.fishingapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREF_NAME = "fishing_app";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String USERNAME_KEY = "username";

    private SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(TOKEN_KEY, token).apply();
    }

    public String getToken() {
        return prefs.getString(TOKEN_KEY, null);
    }

    public void saveUsername(String username) {
        prefs.edit().putString(USERNAME_KEY, username).apply();
    }

    public String getUsername() {
        return prefs.getString(USERNAME_KEY, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }
}
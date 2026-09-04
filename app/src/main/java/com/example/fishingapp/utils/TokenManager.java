// app/src/main/java/com/example/fishingapp/utils/TokenManager.java
package com.example.fishingapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {

    private static final String TAG = "TokenManager";

    private static final String PREF_NAME = "fishing_app_secure";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String USERNAME_KEY = "username";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = createEncryptedPrefs(context);
    }

    private SharedPreferences createEncryptedPrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to create EncryptedSharedPreferences, falling back to " +
                    "clearing stored session", e);
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
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
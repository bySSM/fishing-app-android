// app/src/main/java/com/example/fishingapp/utils/TokenManager.java
package com.example.fishingapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ConcurrentHashMap;

public class TokenManager {

    private static final String TAG = "TokenManager";

    private static final String PREF_NAME = "fishing_app_secure";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String USERNAME_KEY = "username";

    private interface Store {
        String get(String key);
        void put(String key, String value);
        void clear();
    }

    private static class EncryptedPrefsStore implements Store {
        private final SharedPreferences prefs;

        EncryptedPrefsStore(SharedPreferences prefs) {
            this.prefs = prefs;
        }

        @Override
        public String get(String key) {
            return prefs.getString(key, null);
        }

        @Override
        public void put(String key, String value) {
            prefs.edit().putString(key, value).apply();
        }

        @Override
        public void clear() {
            prefs.edit().clear().apply();
        }
    }

    private static class InMemoryStore implements Store {
        private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

        @Override
        public String get(String key) {
            return map.get(key);
        }

        @Override
        public void put(String key, String value) {
            map.put(key, value);
        }

        @Override
        public void clear() {
            map.clear();
        }
    }

    private final Store store;

    public TokenManager(Context context) {
        store = createStore(context);
    }

    private Store createStore(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            return new EncryptedPrefsStore(encryptedPrefs);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to create EncryptedSharedPreferences — session will not " +
                    "persist across app restarts until this is resolved", e);
            return new InMemoryStore();
        }
    }

    public void saveToken(String token) {
        store.put(TOKEN_KEY, token);
    }

    public String getToken() {
        return store.get(TOKEN_KEY);
    }

    public void saveUsername(String username) {
        store.put(USERNAME_KEY, username);
    }

    public String getUsername() {
        return store.get(USERNAME_KEY);
    }

    public void clear() {
        store.clear();
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }
}
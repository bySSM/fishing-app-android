// app/src/main/java/com/example/fishingapp/MainActivity.java
package com.example.fishingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fishingapp.api.ApiClient;
import com.example.fishingapp.api.FishingApi;
import com.example.fishingapp.utils.TokenManager;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;
    private TokenManager tokenManager;
    private FishingApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenManager = new TokenManager(this);
        api = ApiClient.getApi();

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Если уже залогинен — сразу переходим к списку
        if (tokenManager.isLoggedIn()) {
            openAquarium();
            return;
        }

        loginButton.setOnClickListener(v -> {
            Log.d(TAG, "Login button clicked");
            login();
        });

        registerButton.setOnClickListener(v -> {
            Log.d(TAG, "Register button clicked");
            openRegisterScreen();
        });

        Log.d(TAG, "MainActivity created");
    }

    private void login() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполни все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting login for: " + username);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        Call<Map<String, Object>> call = api.login(credentials);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.d(TAG, "Response received: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Object tokenObj = response.body().get("token");
                    if (tokenObj != null) {
                        String token = tokenObj.toString();
                        tokenManager.saveToken(token);
                        tokenManager.saveUsername(username);

                        Log.d(TAG, "Login successful");
                        Toast.makeText(MainActivity.this, "Успешный вход!", Toast.LENGTH_SHORT).show();

                        openAquarium();
                    }
                } else {
                    Log.e(TAG, "Login failed: " + response.code());
                    Toast.makeText(MainActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Ошибка: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openRegisterScreen() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    // В MainActivity.java, замени openCatchList():
    private void openAquarium() {
        Intent intent = new Intent(MainActivity.this, AquariumActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
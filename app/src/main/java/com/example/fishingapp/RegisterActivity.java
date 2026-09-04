// app/src/main/java/com/example/fishingapp/RegisterActivity.java
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

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText usernameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button submitButton;
    private Button backButton;
    private TokenManager tokenManager;
    private FishingApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tokenManager = new TokenManager(this);
        api = ApiClient.getApi();

        usernameInput = findViewById(R.id.regUsernameInput);
        emailInput = findViewById(R.id.regEmailInput);
        passwordInput = findViewById(R.id.regPasswordInput);
        confirmPasswordInput = findViewById(R.id.regConfirmPasswordInput);
        submitButton = findViewById(R.id.submitRegisterButton);
        backButton = findViewById(R.id.backToLoginButton);

        submitButton.setOnClickListener(v -> register());

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void register() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Валидация
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполни все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 3) {
            Toast.makeText(this, "Логин должен быть минимум 3 символа", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            Toast.makeText(this, "Введи корректный email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен быть минимум 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting registration for: " + username);

        Map<String, String> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("password", password);

        Call<Map<String, Object>> call = api.register(userData);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Log.d(TAG, "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Object tokenObj = response.body().get("token");
                    if (tokenObj != null) {
                        String token = tokenObj.toString();
                        tokenManager.saveToken(token);
                        tokenManager.saveUsername(username);

                        Log.d(TAG, "Registration successful");
                        Toast.makeText(RegisterActivity.this,
                                "Регистрация успешна!", Toast.LENGTH_SHORT).show();

                        // В RegisterActivity.java, замени переход:
                        Intent intent = new Intent(RegisterActivity.this, AquariumActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                } else {
                    String errorMessage = "Ошибка регистрации";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }

                    Log.e(TAG, "Registration failed: " + response.code() + " - " + errorMessage);
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this,
                        "Ошибка: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
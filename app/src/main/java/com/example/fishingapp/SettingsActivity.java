// app/src/main/java/com/example/fishingapp/SettingsActivity.java
package com.example.fishingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fishingapp.utils.TokenManager;

public class SettingsActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tokenManager = new TokenManager(this);

        Button backButton = findViewById(R.id.backButton);
        LinearLayout profileSection = findViewById(R.id.profileSection);
        LinearLayout aboutSection = findViewById(R.id.aboutSection);
        LinearLayout contactSection = findViewById(R.id.contactSection);
        Button logoutButton = findViewById(R.id.logoutButton);

        backButton.setOnClickListener(v -> {
            finish(); // Стандартное закрытие — плавная анимация
        });

        profileSection.setOnClickListener(v -> {
            showProfile();
        });

        aboutSection.setOnClickListener(v -> {
            showAbout();
        });

        contactSection.setOnClickListener(v -> {
            showContact();
        });

        logoutButton.setOnClickListener(v -> {
            confirmLogout();
        });
    }

    private void showProfile() {
        String username = tokenManager.getUsername();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Мой профиль");
        builder.setMessage("Имя пользователя: " + username + "\n\n" +
                "Здесь будет полная информация о профиле");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("О программе");
        builder.setMessage("Fishing App v1.0\n\n" +
                "Приложение для рыболовов.\n" +
                "Коллекционируй уловы, соревнуйся с другими рыбаками, " +
                "находи лучшие места для рыбалки.\n\n" +
                "© 2026 Fishing App");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void showContact() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Связь с разработчиком");
        builder.setMessage("Email: developer@fishingapp.com\n\n" +
                "Telegram: @fishing_app_dev\n\n" +
                "GitHub: github.com/bySSM");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void confirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выйти?");
        builder.setMessage("Ты уверен, что хочешь выйти из аккаунта?");
        builder.setPositiveButton("Выйти", (dialog, which) -> {
            tokenManager.clear();

            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
}
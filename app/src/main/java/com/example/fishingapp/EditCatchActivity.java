// app/src/main/java/com/example/fishingapp/EditCatchActivity.java
package com.example.fishingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fishingapp.api.ApiClient;
import com.example.fishingapp.api.FishingApi;
import com.example.fishingapp.model.Catch;
import com.example.fishingapp.utils.ApiErrorParser;
import com.example.fishingapp.utils.TokenManager;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCatchActivity extends AppCompatActivity {

    private EditText fishTypeInput;
    private EditText weightInput;
    private EditText lengthInput;
    private EditText baitInput;
    private EditText descriptionInput;
    private Button saveButton;
    private Button cancelButton;

    private TokenManager tokenManager;
    private FishingApi api;
    private Long catchId;

    // Сохраняем оригинальные координаты из Intent
    private double originalLatitude = 55.7558;
    private double originalLongitude = 37.6173;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_catch);

        tokenManager = new TokenManager(this);
        api = ApiClient.getApi();

        fishTypeInput = findViewById(R.id.editFishTypeInput);
        weightInput = findViewById(R.id.editWeightInput);
        lengthInput = findViewById(R.id.editLengthInput);
        baitInput = findViewById(R.id.editBaitInput);
        descriptionInput = findViewById(R.id.editDescriptionInput);
        saveButton = findViewById(R.id.saveEditButton);
        cancelButton = findViewById(R.id.cancelEditButton);

        // Получаем данные из Intent
        catchId = getIntent().getLongExtra("catchId", -1);
        fishTypeInput.setText(getIntent().getStringExtra("fishType"));

        Double weight = getIntent().getDoubleExtra("weight", 0);
        if (weight != null && weight > 0) {
            weightInput.setText(String.valueOf(weight));
        }

        Double length = getIntent().getDoubleExtra("length", 0);
        if (length != null && length > 0) {
            lengthInput.setText(String.valueOf(length));
        }

        baitInput.setText(getIntent().getStringExtra("bait"));
        descriptionInput.setText(getIntent().getStringExtra("description"));

        // Получаем оригинальные координаты
        double lat = getIntent().getDoubleExtra("latitude", 55.7558);
        double lng = getIntent().getDoubleExtra("longitude", 37.6173);
        if (lat != 0 && lng != 0) {
            originalLatitude = lat;
            originalLongitude = lng;
        }

        saveButton.setOnClickListener(v -> updateCatch());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void updateCatch() {
        String fishType = fishTypeInput.getText().toString().trim();
        String weightStr = weightInput.getText().toString().trim();
        String lengthStr = lengthInput.getText().toString().trim();
        String bait = baitInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (fishType.isEmpty()) {
            Toast.makeText(this, "Введи вид рыбы", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> catchData = new HashMap<>();
        catchData.put("fishType", fishType);

        if (!weightStr.isEmpty()) {
            catchData.put("weight", Double.parseDouble(weightStr));
        }
        if (!lengthStr.isEmpty()) {
            catchData.put("length", Double.parseDouble(lengthStr));
        }
        if (!bait.isEmpty()) {
            catchData.put("bait", bait);
        }
        if (!description.isEmpty()) {
            catchData.put("description", description);
        }

        // Используем ОРИГИНАЛЬНЫЕ координаты (не меняем их)
        catchData.put("latitude", originalLatitude);
        catchData.put("longitude", originalLongitude);

        String token = tokenManager.getToken();
        String authHeader = "Bearer " + token;

        api.updateCatch(authHeader, catchId, catchData).enqueue(new Callback<Catch>() {
            @Override
            public void onResponse(Call<Catch> call, Response<Catch> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditCatchActivity.this, "Улов обновлён!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String message = ApiErrorParser.extractMessage(response.errorBody(), response.code());
                    Toast.makeText(EditCatchActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Catch> call, Throwable t) {
                Toast.makeText(EditCatchActivity.this,
                        "Ошибка: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
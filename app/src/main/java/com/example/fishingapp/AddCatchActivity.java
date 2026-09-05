// app/src/main/java/com/example/fishingapp/AddCatchActivity.java
package com.example.fishingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fishingapp.api.ApiClient;
import com.example.fishingapp.api.FishingApi;
import com.example.fishingapp.model.Catch;
import com.example.fishingapp.utils.ApiErrorParser;
import com.example.fishingapp.utils.LocationHelper;
import com.example.fishingapp.utils.TokenManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCatchActivity extends AppCompatActivity {

    private static final String TAG = "AddCatchActivity";
    private static final int PICK_IMAGE = 1;
    private static final int REQUEST_CAMERA = 2;

    private ImageView photoPreview;
    private EditText fishTypeInput;
    private EditText weightInput;
    private EditText lengthInput;
    private EditText baitInput;
    private EditText descriptionInput;
    private Button selectPhotoButton;
    private Button takePhotoButton;
    private Button saveButton;
    private Button cancelButton;
    private Button getLocationButton;
    private TextView locationTextView;
    private TokenManager tokenManager;
    private FishingApi api;
    private LocationHelper locationHelper;
    private Switch hideLocationSwitch;

    // БЕЗ значений по умолчанию!
    private Uri selectedImageUri = null;
    private Double currentLatitude = null;
    private Double currentLongitude = null;
    private boolean locationReceived = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_catch);

        tokenManager = new TokenManager(this);
        api = ApiClient.getApi();
        locationHelper = new LocationHelper(this);

        photoPreview = findViewById(R.id.catchPhotoPreview);
        fishTypeInput = findViewById(R.id.fishTypeInput);
        weightInput = findViewById(R.id.weightInput);
        lengthInput = findViewById(R.id.lengthInput);
        baitInput = findViewById(R.id.baitInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        saveButton = findViewById(R.id.saveCatchButton);
        cancelButton = findViewById(R.id.cancelButton);
        getLocationButton = findViewById(R.id.getLocationButton);
        locationTextView = findViewById(R.id.locationTextView);

        selectPhotoButton.setOnClickListener(v -> openGallery());
        takePhotoButton.setOnClickListener(v -> openCamera());
        saveButton.setOnClickListener(v -> saveCatch());
        cancelButton.setOnClickListener(v -> finish());
        getLocationButton.setOnClickListener(v -> getLocation());
        hideLocationSwitch = findViewById(R.id.hideLocationSwitch);

        // Автоматически получаем местоположение
        getLocation();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void openCamera() {
        Intent intent = new Intent(AddCatchActivity.this, CameraActivity.class);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void getLocation() {
        locationTextView.setText("Определение местоположения...");

        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                currentLatitude = latitude;
                currentLongitude = longitude;
                locationReceived = true;
                locationTextView.setText("📍 " + String.format("%.4f, %.4f", latitude, longitude));
                locationTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }

            @Override
            public void onLocationError(String error) {
                locationReceived = false;
                currentLatitude = null;
                currentLongitude = null;
                locationTextView.setText("❌ " + error);
                locationTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            photoPreview.setImageURI(selectedImageUri);
        } else if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK && data != null) {
            String photoPath = data.getStringExtra("photoPath");
            if (photoPath != null) {
                selectedImageUri = Uri.fromFile(new File(photoPath));
                photoPreview.setImageURI(selectedImageUri);
            }
        }
    }

    private void saveCatch() {
        String fishType = fishTypeInput.getText().toString().trim();
        String weightStr = weightInput.getText().toString().trim();
        String lengthStr = lengthInput.getText().toString().trim();
        String bait = baitInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (fishType.isEmpty()) {
            Toast.makeText(this, "Введи вид рыбы", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверяем, что геолокация определена
        if (!locationReceived || currentLatitude == null || currentLongitude == null) {
            Toast.makeText(this, "Не удалось определить местоположение. Включи GPS и нажми '📍 Обновить'",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String authHeader = "Bearer " + token;

        if (selectedImageUri != null) {
            uploadWithPhoto(authHeader, fishType, weightStr, lengthStr, bait, description);
        } else {
            uploadWithoutPhoto(authHeader, fishType, weightStr, lengthStr, bait, description);
        }
    }

    private void uploadWithoutPhoto(String authHeader, String fishType, String weightStr,
                                    String lengthStr, String bait, String description) {
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

        catchData.put("latitude", currentLatitude);
        catchData.put("longitude", currentLongitude);

        api.createCatch(authHeader, catchData).enqueue(new Callback<Catch>() {
            @Override
            public void onResponse(Call<Catch> call, Response<Catch> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddCatchActivity.this, "Улов сохранён!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String message = ApiErrorParser.extractMessage(response.errorBody(), response.code());
                    Toast.makeText(AddCatchActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Catch> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage(), t);
                Toast.makeText(AddCatchActivity.this,
                        "Ошибка: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadWithPhoto(String authHeader, String fishType, String weightStr,
                                 String lengthStr, String bait, String description) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            File tempFile = new File(getCacheDir(), "temp_photo.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            RequestBody photoBody = RequestBody.create(MediaType.parse("image/*"), tempFile);
            MultipartBody.Part photoPart = MultipartBody.Part.createFormData("photo",
                    tempFile.getName(), photoBody);

            RequestBody fishTypeBody = RequestBody.create(MediaType.parse("text/plain"), fishType);

            RequestBody isLocationHiddenBody = RequestBody.create(
                    MediaType.parse("text/plain"),
                    String.valueOf(hideLocationSwitch.isChecked())
            );

            Call<Map<String, Object>> call = api.createCatchWithPhoto(
                    authHeader, photoPart, fishTypeBody,
                    weightStr.isEmpty() ? null : RequestBody.create(MediaType.parse("text/plain"), weightStr),
                    lengthStr.isEmpty() ? null : RequestBody.create(MediaType.parse("text/plain"), lengthStr),
                    RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentLatitude)),
                    RequestBody.create(MediaType.parse("text/plain"), String.valueOf(currentLongitude)),
                    bait.isEmpty() ? null : RequestBody.create(MediaType.parse("text/plain"), bait),
                    description.isEmpty() ? null : RequestBody.create(MediaType.parse("text/plain"), description),
                    isLocationHiddenBody
            );

            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddCatchActivity.this, "Улов с фото сохранён!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String message = ApiErrorParser.extractMessage(response.errorBody(), response.code());
                        Toast.makeText(AddCatchActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "Error: " + t.getMessage(), t);
                    Toast.makeText(AddCatchActivity.this,
                            "Ошибка: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error uploading photo: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка загрузки фото", Toast.LENGTH_SHORT).show();
        }
    }
}
// app/src/main/java/com/example/fishingapp/RatingActivity.java
package com.example.fishingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fishingapp.api.ApiClient;
import com.example.fishingapp.api.FishingApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RatingActivity extends AppCompatActivity {

    private ListView listView;
    private FishingApi api;
    private List<String> ratingItems = new ArrayList<>();
    private List<Map<String, Object>> ratingData = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        api = ApiClient.getApi();
        listView = findViewById(R.id.ratingListView);
        Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, ratingItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < ratingData.size()) {
                Map<String, Object> user = ratingData.get(position);
                Long userId = ((Number) user.get("userId")).longValue();
                String username = (String) user.get("username");

                openUserAquarium(userId, username);
            }
        });

        loadRating();
    }

    private void loadRating() {
        api.getTop100().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ratingItems.clear();
                    ratingData.clear();
                    ratingData.addAll(response.body());

                    int position = 1;
                    for (Map<String, Object> row : ratingData) {
                        String username = (String) row.get("username");

                        // Получаем общий вес (сумма 15 рыб)
                        Object totalWeightObj = row.get("totalWeight");
                        double totalWeight = 0;
                        if (totalWeightObj instanceof Number) {
                            totalWeight = ((Number) totalWeightObj).doubleValue();
                        }

                        // Получаем количество рыб
                        Object fishCountObj = row.get("fishCount");
                        int fishCount = 0;
                        if (fishCountObj instanceof Number) {
                            fishCount = ((Number) fishCountObj).intValue();
                        }

                        String item = String.format("#%d %s — %.1f кг (%s)",
                                position++,
                                username,
                                totalWeight,
                                getFishWord(fishCount));
                        ratingItems.add(item);
                    }

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(RatingActivity.this,
                        "Ошибка: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openUserAquarium(Long userId, String username) {
        Intent intent = new Intent(RatingActivity.this, UserAquariumActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private String getFishWord(int count) {
        int lastDigit = count % 10;
        int lastTwoDigits = count % 100;

        if (lastTwoDigits >= 11 && lastTwoDigits <= 14) {
            return count + " рыб";
        }

        switch (lastDigit) {
            case 1:
                return count + " рыба";
            case 2:
            case 3:
            case 4:
                return count + " рыбы";
            default:
                return count + " рыб";
        }
    }
}
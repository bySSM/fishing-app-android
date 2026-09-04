// app/src/main/java/com/example/fishingapp/SearchActivity.java
package com.example.fishingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fishingapp.api.ApiClient;
import com.example.fishingapp.api.FishingApi;
import com.example.fishingapp.utils.TokenManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private Button searchButton;
    private Button backButton;
    private ListView resultsListView;
    private TextView emptyResultTextView;

    private TokenManager tokenManager;
    private FishingApi api;
    private ArrayAdapter<String> adapter;
    private List<String> results = new ArrayList<>();
    private List<Map<String, Object>> usersData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        tokenManager = new TokenManager(this);
        api = ApiClient.getApi();

        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        backButton = findViewById(R.id.backButton);
        resultsListView = findViewById(R.id.searchResultsListView);
        emptyResultTextView = findViewById(R.id.emptyResultTextView);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, results);
        resultsListView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());

        searchButton.setOnClickListener(v -> performSearch());

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        resultsListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < usersData.size()) {
                Map<String, Object> user = usersData.get(position);
                Long userId = ((Number) user.get("id")).longValue();
                String username = (String) user.get("username");

                openUserAquarium(userId, username);
            }
        });
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(this, "Введи запрос", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = tokenManager.getToken();
        if (token == null) {
            finish();
            return;
        }

        String authHeader = "Bearer " + token;

        api.searchUsers(authHeader, query).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    results.clear();
                    usersData.clear();
                    usersData.addAll(response.body());

                    for (Map<String, Object> user : usersData) {
                        String username = (String) user.get("username");

                        Object ratingObj = user.get("rating");
                        int rating = 0;
                        if (ratingObj instanceof Number) {
                            rating = ((Number) ratingObj).intValue();
                        } else if (ratingObj != null) {
                            try {
                                rating = Integer.parseInt(ratingObj.toString());
                            } catch (NumberFormatException e) {
                                rating = 0;
                            }
                        }

                        if (rating > 0) {
                            results.add(username + " (рейтинг: #" + rating + ")");
                        } else {
                            results.add(username + " (не в рейтинге)");
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (results.isEmpty()) {
                        emptyResultTextView.setVisibility(TextView.VISIBLE);
                        emptyResultTextView.setText("Ничего не найдено");
                        resultsListView.setVisibility(ListView.GONE);
                    } else {
                        emptyResultTextView.setVisibility(TextView.GONE);
                        resultsListView.setVisibility(ListView.VISIBLE);
                    }
                } else {
                    Toast.makeText(SearchActivity.this,
                            "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(SearchActivity.this,
                        "Ошибка: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openUserAquarium(Long userId, String username) {
        Intent intent = new Intent(SearchActivity.this, UserAquariumActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
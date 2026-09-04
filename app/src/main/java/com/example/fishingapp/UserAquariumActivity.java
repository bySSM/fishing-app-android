package com.example.fishingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fishingapp.api.ApiClient;
import com.example.fishingapp.api.FishingApi;
import com.example.fishingapp.utils.TokenManager;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAquariumActivity extends AppCompatActivity {

    private static final String TAG = "UserAquariumActivity";

    private FrameLayout aquariumContainer;

    private TextView titleView;

    private TextView usernameView;

    private TokenManager tokenManager;

    private FishingApi api;

    private Long userId;

    private String username;


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_user_aquarium
        );


        tokenManager =
                new TokenManager(this);

        api =
                ApiClient.getApi();


        aquariumContainer =
                findViewById(
                        R.id.aquariumContainer
                );


        titleView =
                findViewById(
                        R.id.aquariumTitle
                );


        usernameView =
                findViewById(
                        R.id.aquariumUsername
                );


        Button backButton =
                findViewById(
                        R.id.backButton
                );


        userId =
                getIntent().getLongExtra(
                        "userId",
                        -1
                );


        username =
                getIntent().getStringExtra(
                        "username"
                );


        backButton.setOnClickListener(
                v -> finish()
        );


        if (username != null
                && !username.isEmpty()) {

            usernameView.setText(
                    "Аквариум: " + username
            );
        }


        loadUserAquarium();
    }


    private void loadUserAquarium() {

        String token =
                tokenManager.getToken();


        if (
                token == null
                        || userId == -1
        ) {

            Toast.makeText(
                    this,
                    "Не удалось определить пользователя",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        String authHeader =
                "Bearer " + token;


        api.getUserAquarium(
                authHeader,
                userId
        ).enqueue(
                new Callback<Map<String, Object>>() {

                    @Override
                    public void onResponse(
                            Call<Map<String, Object>> call,
                            Response<Map<String, Object>> response
                    ) {

                        if (
                                response.isSuccessful()
                                        && response.body() != null
                        ) {

                            Map<String, Object> aquarium =
                                    response.body();


                            Object usernameObject =
                                    aquarium.get("username");


                            if (usernameObject != null) {

                                String usernameFromResponse =
                                        String.valueOf(
                                                usernameObject
                                        );


                                usernameView.setText(
                                        "Аквариум: "
                                                + usernameFromResponse
                                );
                            }


                            Object topFishesObject =
                                    aquarium.get("topFishes");


                            if (
                                    topFishesObject
                                            instanceof List
                            ) {

                                List<Map<String, Object>> topFishes =
                                        (List<Map<String, Object>>)
                                                topFishesObject;


                                if (
                                        !topFishes.isEmpty()
                                ) {

                                    displayFishes(
                                            topFishes
                                    );

                                } else {

                                    showEmptyAquarium();
                                }

                            } else {

                                showEmptyAquarium();
                            }

                        } else {

                            Log.e(
                                    TAG,
                                    "Aquarium request failed: "
                                            + response.code()
                            );


                            Toast.makeText(
                                    UserAquariumActivity.this,
                                    "Не удалось загрузить аквариум",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<Map<String, Object>> call,
                            Throwable t
                    ) {

                        Log.e(
                                TAG,
                                "Error loading aquarium",
                                t
                        );


                        Toast.makeText(
                                UserAquariumActivity.this,
                                "Ошибка соединения",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    private void displayFishes(
            List<Map<String, Object>> fishes
    ) {

        aquariumContainer.removeAllViews();


        int fishCount =
                Math.min(
                        fishes.size(),
                        5
                );


        Random random =
                new Random();


        for (
                int i = 0;
                i < fishCount;
                i++
        ) {

            Map<String, Object> fish =
                    fishes.get(i);


            /*
             * Backend возвращает CatchResponse.
             *
             * В нём:
             * id       -> ID конкретного улова
             * photoUrl -> фотография
             * fishType -> вид рыбы
             * и т.д.
             */
            Long catchId =
                    extractCatchId(fish);


            String photoUrl =
                    extractString(
                            fish.get("photoUrl")
                    );


            ImageView fishView =
                    new ImageView(this);


            FrameLayout.LayoutParams params =
                    new FrameLayout.LayoutParams(
                            120,
                            80
                    );


            params.leftMargin =
                    random.nextInt(300);


            params.topMargin =
                    random.nextInt(400);


            fishView.setLayoutParams(
                    params
            );


            fishView.setScaleType(
                    ImageView.ScaleType.CENTER_CROP
            );


            if (
                    photoUrl != null
                            && !photoUrl.isEmpty()
            ) {

                String fullUrl =
                        buildPhotoUrl(photoUrl);


                Picasso.get()
                        .load(fullUrl)
                        .placeholder(
                                android.R.drawable.ic_menu_gallery
                        )
                        .error(
                                android.R.drawable.ic_menu_gallery
                        )
                        .into(fishView);

            } else {

                fishView.setImageResource(
                        android.R.drawable.ic_menu_gallery
                );
            }


            /*
             * Главное изменение.
             *
             * Теперь каждая рыбка в чужом аквариуме
             * знает ID настоящего улова.
             *
             * Нажатие открывает универсальную
             * CatchDetailActivity.
             */
            if (catchId != null) {

                final Long selectedCatchId =
                        catchId;


                fishView.setOnClickListener(
                        v -> openCatchDetails(
                                selectedCatchId
                        )
                );


                fishView.setContentDescription(
                        "Открыть улов"
                );

            } else {

                /*
                 * Защита на случай старого/некорректного
                 * ответа backend без id.
                 */
                fishView.setOnClickListener(
                        null
                );
            }


            aquariumContainer.addView(
                    fishView
            );


            animateFish(
                    fishView,
                    i
            );
        }
    }


    private Long extractCatchId(
            Map<String, Object> fish
    ) {

        Object idObject =
                fish.get("id");


        if (idObject instanceof Number) {

            return (
                    (Number) idObject
            ).longValue();
        }


        if (idObject instanceof String) {

            try {

                return Long.parseLong(
                        (String) idObject
                );

            } catch (NumberFormatException e) {

                Log.e(
                        TAG,
                        "Invalid catch id: "
                                + idObject
                );
            }
        }


        return null;
    }


    private String extractString(
            Object value
    ) {

        if (value == null) {
            return null;
        }


        String result =
                String.valueOf(value);


        return result.isEmpty()
                ? null
                : result;
    }


    private String buildPhotoUrl(
            String photoUrl
    ) {

        if (
                photoUrl.startsWith("http://")
                        || photoUrl.startsWith("https://")
        ) {

            return photoUrl;
        }


        return "http://10.0.2.2:8080"
                + photoUrl;
    }


    private void openCatchDetails(
            Long catchId
    ) {

        if (catchId == null) {

            Toast.makeText(
                    this,
                    "Не удалось определить улов",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Intent intent =
                new Intent(
                        this,
                        CatchDetailActivity.class
                );


        intent.putExtra(
                CatchDetailActivity.EXTRA_CATCH_ID,
                catchId
        );


        startActivity(intent);
    }


    private void showEmptyAquarium() {

        aquariumContainer.removeAllViews();


        Toast.makeText(
                this,
                "У этого пользователя пока нет рыб",
                Toast.LENGTH_SHORT
        ).show();
    }


    private void animateFish(
            ImageView fish,
            int index
    ) {

        Random random =
                new Random();


        TranslateAnimation horizontal =
                new TranslateAnimation(
                        0,
                        random.nextInt(200) - 100,
                        0,
                        0
                );


        horizontal.setDuration(
                3000 + random.nextInt(2000)
        );


        horizontal.setRepeatCount(
                Animation.INFINITE
        );


        horizontal.setRepeatMode(
                Animation.REVERSE
        );


        TranslateAnimation vertical =
                new TranslateAnimation(
                        0,
                        0,
                        0,
                        random.nextInt(100) - 50
                );


        vertical.setDuration(
                2000 + random.nextInt(1500)
        );


        vertical.setRepeatCount(
                Animation.INFINITE
        );


        vertical.setRepeatMode(
                Animation.REVERSE
        );


        AnimationSet animationSet =
                new AnimationSet(true);


        animationSet.addAnimation(
                horizontal
        );


        animationSet.addAnimation(
                vertical
        );


        fish.startAnimation(
                animationSet
        );
    }
}

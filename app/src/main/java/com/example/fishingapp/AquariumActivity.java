package com.example.fishingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fishingapp.api.ApiClient;
import com.example.fishingapp.api.FishingApi;
import com.example.fishingapp.utils.TokenManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AquariumActivity extends AppCompatActivity {

    private static final String TAG = "AquariumActivity";

    private FrameLayout aquariumContainer;
    private TextView titleView;
    private TextView usernameView;
    private TokenManager tokenManager;
    private FishingApi api;
    private FrameLayout aquariumFishContainer;
    private View bubble1;
    private View bubble2;
    private View bubble3;
    private View bubble4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_aquarium);

        tokenManager = new TokenManager(this);
        api = ApiClient.getApi();

        aquariumContainer = findViewById(R.id.aquariumContainer);
        aquariumFishContainer = findViewById(R.id.aquariumFishContainer);
        bubble1 = findViewById(R.id.bubble1);
        bubble2 = findViewById(R.id.bubble2);
        bubble3 = findViewById(R.id.bubble3);
        bubble4 = findViewById(R.id.bubble4);
        titleView = findViewById(R.id.aquariumTitle);
        usernameView = findViewById(R.id.aquariumUsername);

        animateBubbles();
        setupNavigation();

        // ВАЖНО:
        // loadAquarium() здесь НЕ вызываем.
        //
        // Загрузка выполняется только в onResume().
        // Это исключает двойной запрос при первом открытии Activity.
    }

    /**
     * Загружаем актуальное состояние аквариума
     * каждый раз, когда пользователь возвращается
     * на этот экран.
     */
    @Override
    protected void onResume() {
        super.onResume();

        loadAquarium();
    }

    /**
     * Нижняя навигация.
     */
    private void setupNavigation() {

        // Мои уловы
        findViewById(R.id.navMyCatchesButton).setOnClickListener(v -> {

            Intent intent = new Intent(
                    AquariumActivity.this,
                    CatchListActivity.class
            );

            startActivity(intent);
        });


        // Рейтинг
        findViewById(R.id.navRatingButton).setOnClickListener(v -> {

            Intent intent = new Intent(
                    AquariumActivity.this,
                    RatingActivity.class
            );

            startActivity(intent);
        });


        // Поиск
        findViewById(R.id.navSearchButton).setOnClickListener(v -> {

            Intent intent = new Intent(
                    AquariumActivity.this,
                    SearchActivity.class
            );

            startActivity(intent);
        });


        // Люди
        findViewById(R.id.navCommunityButton).setOnClickListener(v -> {

            // Пока используем существующий экран поиска пользователей.
            Intent intent = new Intent(
                    AquariumActivity.this,
                    SearchActivity.class
            );

            startActivity(intent);
        });


        // Настройки
        findViewById(R.id.navSettingsButton).setOnClickListener(v -> {

            Intent intent = new Intent(
                    AquariumActivity.this,
                    SettingsActivity.class
            );

            startActivity(intent);
        });


        // Карта
        findViewById(R.id.navMapButton).setOnClickListener(v -> {

            Intent intent = new Intent(
                    AquariumActivity.this,
                    MapActivity.class
            );

            startActivity(intent);
        });
    }


    /**
     * Загружает актуальные данные аквариума с сервера.
     */
    private void loadAquarium() {

        String token = tokenManager.getToken();

        if (token == null) {

            Toast.makeText(
                    this,
                    "Не авторизован",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        String authHeader = "Bearer " + token;

        api.getMyAquarium(authHeader).enqueue(
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

                            Map<String, Object> data =
                                    response.body();


                            // Имя пользователя
                            String username =
                                    (String) data.get("username");

                            if (username != null) {

                                usernameView.setText(
                                        "Аквариум: " + username
                                );
                            }


                            // Лучшие рыбы пользователя
                            List<Map<String, Object>> topFishes =
                                    (List<Map<String, Object>>)
                                            data.get("topFishes");


                            if (
                                    topFishes != null
                                            && !topFishes.isEmpty()
                            ) {

                                displayFishes(topFishes);

                            } else {

                                // Рыб нет — очищаем только рыб.
                                // Декорации остаются.
                                removeFishViews();
                            }

                        } else {

                            Log.e(
                                    TAG,
                                    "Aquarium loading error: "
                                            + response.code()
                            );
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
                    }
                }
        );
    }


    /**
     * Показывает актуальные рыбы в аквариуме.
     *
     * ВАЖНО:
     * removeAllViews() здесь НЕТ.
     *
     * Мы удаляем только ImageView рыб.
     * Камни, водоросли, пузыри и другие элементы
     * аквариума остаются на месте.
     */
    private void displayFishes(
            List<Map<String, Object>> fishes
    ) {

        // Ждём, пока aquariumContainer получит
        // реальные размеры.
        aquariumContainer.post(() -> {

            int containerWidth =
                    aquariumContainer.getWidth();

            int containerHeight =
                    aquariumContainer.getHeight();


            if (
                    containerWidth <= 0
                            || containerHeight <= 0
            ) {

                Log.w(
                        TAG,
                        "Aquarium container has invalid size"
                );

                return;
            }


            /*
             * Удаляем только рыб.
             *
             * Рыбы находятся в отдельном
             * aquariumFishContainer, поэтому
             * декорации аквариума не затрагиваются.
             */
            removeFishViews();


            // Максимум 5 лучших рыб.
            int fishCount =
                    Math.min(
                            fishes.size(),
                            5
                    );


            Random random =
                    new Random();


            for (int i = 0; i < fishCount; i++) {

                Map<String, Object> fish =
                        fishes.get(i);


                // Получаем вид рыбы.
                String fishType =
                        (String) fish.get("fishType");


                ImageView fishView =
                        new ImageView(this);


                // Размер рыбы.
                int fishWidth =
                        dpToPx(150);

                int fishHeight =
                        dpToPx(90);


                FrameLayout.LayoutParams params =
                        new FrameLayout.LayoutParams(
                                fishWidth,
                                fishHeight
                        );


                // Случайное положение внутри аквариума.
                int maxX =
                        Math.max(
                                0,
                                containerWidth - fishWidth
                        );

                int maxY =
                        Math.max(
                                0,
                                containerHeight - fishHeight
                        );


                params.leftMargin =
                        random.nextInt(maxX + 1);

                params.topMargin =
                        random.nextInt(maxY + 1);


                fishView.setLayoutParams(
                        params
                );


                // Устанавливаем реалистичную
                // текстуру соответствующего вида.
                fishView.setImageResource(
                        getFishImage(fishType)
                );


                fishView.setScaleType(
                        ImageView.ScaleType.FIT_CENTER
                );


                // Добавляем рыбу в отдельный слой.
                aquariumFishContainer.addView(
                        fishView
                );


                // Запускаем плавание.
                animateFish(
                        fishView,
                        i
                );
            }
        });
    }


    /**
     * Удаляет только рыб из aquariumContainer.
     *
     * Декорации аквариума не удаляются.
     */
    private void removeFishViews() {
        for (int i = aquariumFishContainer.getChildCount() - 1; i >= 0; i--) {
            View child = aquariumFishContainer.getChildAt(i);

            child.clearAnimation();
            aquariumFishContainer.removeViewAt(i);
        }
    }


    /**
     * Анимация плавания рыбы.
     */
    private void animateFish(
            ImageView fish,
            int index
    ) {

        Random random =
                new Random();


        // Горизонтальное движение.
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


        // Вертикальное движение.
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


        // Объединяем две анимации.
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

    private void animateBubbles() {

        animateBubble(
                bubble1,
                6500,
                0
        );

        animateBubble(
                bubble2,
                5200,
                1200
        );

        animateBubble(
                bubble3,
                7200,
                2300
        );

        animateBubble(
                bubble4,
                5800,
                3500
        );
    }

    private void animateBubble(
            View bubble,
            long duration,
            long startDelay
    ) {

        if (bubble == null) {
            return;
        }

        bubble.postDelayed(() -> {

            float aquariumHeight =
                    aquariumContainer.getHeight();

            float startY =
                    aquariumHeight * 0.75f;

            float endY =
                    -aquariumHeight * 0.65f;

            bubble.setTranslationY(startY);

            bubble.animate()
                    .translationY(endY)
                    .alpha(0.05f)
                    .setDuration(duration)
                    .setInterpolator(
                            new android.view.animation.LinearInterpolator()
                    )
                    .withEndAction(() -> {

                        bubble.setTranslationY(startY);
                        bubble.setAlpha(0.2f);

                        animateBubble(
                                bubble,
                                duration,
                                0
                        );
                    })
                    .start();

        }, startDelay);
    }


    /**
     * Перевод dp в px.
     */
    private int dpToPx(int dp) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                dp * density
        );
    }


    /**
     * Возвращает изображение рыбы
     * в зависимости от её вида.
     */
    private int getFishImage(
            String fishType
    ) {

        if (fishType == null) {

            return R.drawable.fish_pike;
        }


        String type =
                fishType
                        .trim()
                        .toLowerCase();


        if (
                type.contains("щука")
        ) {

            return R.drawable.fish_pike;
        }


        if (
                type.contains("окунь")
        ) {

            return R.drawable.fish_perch;
        }


        if (
                type.contains("судак")
        ) {

            return R.drawable.fish_zander;
        }


        if (
                type.contains("карп")
                        || type.contains("карась")
        ) {

            return R.drawable.fish_crucian;
        }


        if (
                type.contains("сом")
        ) {

            return R.drawable.fish_catfish;
        }


        // Универсальная рыба
        // для неизвестного вида.
        return R.drawable.fish_pike;
    }
}
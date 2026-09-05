package com.example.fishingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fishingapp.api.ApiClient;
import com.example.fishingapp.api.FishingApi;
import com.example.fishingapp.model.Catch;
import com.example.fishingapp.model.LikeResponse;
import com.example.fishingapp.model.LikeStatusResponse;
import com.example.fishingapp.utils.ApiErrorParser;
import com.example.fishingapp.utils.TokenManager;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatchDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CATCH_ID = "catchId";

    private Long catchId;

    private FishingApi api;

    private TokenManager tokenManager;

    private ImageView photoView;

    private TextView fishTypeView;
    private TextView authorView;
    private TextView weightView;
    private TextView lengthView;
    private TextView baitView;
    private TextView descriptionView;

    private Button likeButton;
    private Button commentsButton;

    private Catch currentCatch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_catch_detail
        );

        catchId = getIntent().getLongExtra(
                EXTRA_CATCH_ID,
                -1
        );

        if (catchId == -1) {

            Toast.makeText(
                    this,
                    "Не указан улов",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        api = ApiClient.getApi();

        tokenManager = new TokenManager(this);

        initViews();

        loadCatch();
    }


    private void initViews() {

        photoView = findViewById(
                R.id.catchDetailImage
        );

        fishTypeView = findViewById(
                R.id.detailFishType
        );

        authorView = findViewById(
                R.id.detailAuthor
        );

        weightView = findViewById(
                R.id.detailWeight
        );

        lengthView = findViewById(
                R.id.detailLength
        );

        baitView = findViewById(
                R.id.detailBait
        );

        descriptionView = findViewById(
                R.id.detailDescription
        );

        likeButton = findViewById(
                R.id.detailLikeButton
        );

        commentsButton = findViewById(
                R.id.detailCommentsButton
        );
    }


    private void loadCatch() {

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

        api.getCatchById(
                "Bearer " + token,
                catchId
        ).enqueue(
                new Callback<Catch>() {

                    @Override
                    public void onResponse(
                            Call<Catch> call,
                            Response<Catch> response
                    ) {

                        if (
                                response.isSuccessful()
                                        && response.body() != null
                        ) {

                            currentCatch = response.body();

                            showCatch();

                            loadLikeStatus();

                        } else {

                            String message = ApiErrorParser.extractMessage(
                                    response.errorBody(), response.code());

                            Toast.makeText(
                                    CatchDetailActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<Catch> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                CatchDetailActivity.this,
                                "Ошибка загрузки улова",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }
                }
        );
    }


    private void showCatch() {

        fishTypeView.setText(
                safeText(currentCatch.getFishType())
        );

        authorView.setText(
                "Автор: "
                        + safeText(currentCatch.getUsername())
        );

        if (currentCatch.getWeight() != null) {

            weightView.setText(
                    "Вес: "
                            + currentCatch.getWeight()
                            + " кг"
            );

        } else {

            weightView.setText(
                    "Вес не указан"
            );
        }

        if (currentCatch.getLength() != null) {

            lengthView.setText(
                    "Длина: "
                            + currentCatch.getLength()
                            + " см"
            );

        } else {

            lengthView.setText(
                    "Длина не указана"
            );
        }

        baitView.setText(
                "Наживка: "
                        + safeText(currentCatch.getBait())
        );

        descriptionView.setText(
                safeText(currentCatch.getDescription())
        );


        String photoUrl =
                currentCatch.getFullPhotoUrl();

        if (photoUrl != null
                && !photoUrl.isEmpty()) {

            Picasso.get()
                    .load(photoUrl)
                    .placeholder(
                            android.R.drawable.ic_menu_gallery
                    )
                    .error(
                            android.R.drawable.ic_menu_gallery
                    )
                    .into(photoView);

        } else {

            photoView.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }


        likeButton.setOnClickListener(
                v -> toggleLike()
        );


        commentsButton.setOnClickListener(
                v -> openComments()
        );
    }


    private void loadLikeStatus() {

        String token = tokenManager.getToken();

        if (token == null) {
            return;
        }

        api.getLikeStatus(
                "Bearer " + token,
                catchId
        ).enqueue(
                new Callback<LikeStatusResponse>() {

                    @Override
                    public void onResponse(
                            Call<LikeStatusResponse> call,
                            Response<LikeStatusResponse> response
                    ) {

                        if (
                                response.isSuccessful()
                                        && response.body() != null
                        ) {

                            updateLikeButton(
                                    response.body().isLiked(),
                                    response.body().getLikesCount()
                            );
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<LikeStatusResponse> call,
                            Throwable t
                    ) {
                        // Ничего не делаем.
                    }
                }
        );
    }


    private void toggleLike() {

        String token = tokenManager.getToken();

        if (token == null) {

            Toast.makeText(
                    this,
                    "Не авторизован",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        likeButton.setEnabled(false);

        api.toggleLike(
                "Bearer " + token,
                catchId
        ).enqueue(
                new Callback<LikeResponse>() {

                    @Override
                    public void onResponse(
                            Call<LikeResponse> call,
                            Response<LikeResponse> response
                    ) {

                        likeButton.setEnabled(true);

                        if (
                                response.isSuccessful()
                                        && response.body() != null
                        ) {

                            LikeResponse result =
                                    response.body();

                            updateLikeButton(
                                    result.isLiked(),
                                    result.getLikesCount()
                            );

                        } else {

                            String message = ApiErrorParser.extractMessage(
                                    response.errorBody(), response.code());

                            Toast.makeText(
                                    CatchDetailActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<LikeResponse> call,
                            Throwable t
                    ) {

                        likeButton.setEnabled(true);

                        Toast.makeText(
                                CatchDetailActivity.this,
                                "Ошибка соединения",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    private void updateLikeButton(
            boolean liked,
            Integer likesCount
    ) {

        int likes =
                likesCount != null
                        ? likesCount
                        : 0;

        likeButton.setText(
                liked
                        ? "❤️ " + likes
                        : "♡ " + likes
        );
    }


    private void openComments() {

        Intent intent = new Intent(
                this,
                CommentsActivity.class
        );

        intent.putExtra(
                "catchId",
                catchId
        );

        intent.putExtra(
                "fishType",
                currentCatch != null
                        ? currentCatch.getFishType()
                        : ""
        );

        startActivity(intent);
    }


    private String safeText(String value) {

        return value != null && !value.isEmpty()
                ? value
                : "Не указано";
    }

}
package com.example.fishingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fishingapp.api.ApiClient;
import com.example.fishingapp.api.FishingApi;
import com.example.fishingapp.model.Catch;
import com.example.fishingapp.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatchListActivity extends AppCompatActivity implements CatchAdapter.CatchActionListener {

    private static final String TAG =
            "CatchListActivity";


    private ListView listView;

    private TokenManager tokenManager;

    private FishingApi api;

    private CatchAdapter adapter;

    private final List<Catch> catchList =
            new ArrayList<>();


    private Button addCatchButton;

//    private Button logoutButton;

    private Button backToAquariumButton;


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_catch_list
        );


        tokenManager =
                new TokenManager(this);

        api =
                ApiClient.getApi();


        listView =
                findViewById(
                        R.id.catchListView
                );

        addCatchButton =
                findViewById(
                        R.id.addCatchButton
                );

//        logoutButton =
//                findViewById(
//                        R.id.logoutButton
//                );

        backToAquariumButton =
                findViewById(
                        R.id.backToAquariumButton
                );


        adapter =
                new CatchAdapter(
                        this,
                        catchList,
                        this
                );


        listView.setAdapter(
                adapter
        );


        backToAquariumButton.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    CatchListActivity.this,
                                    AquariumActivity.class
                            );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    );

                    startActivity(intent);

                    finish();
                }
        );


        addCatchButton.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    CatchListActivity.this,
                                    AddCatchActivity.class
                            );

                    startActivity(intent);
                }
        );


//        logoutButton.setOnClickListener(
//                v -> logout()
//        );


        loadMyCatches();
    }


    @Override
    protected void onResume() {

        super.onResume();

        loadMyCatches();
    }


    private void loadMyCatches() {

        String token =
                tokenManager.getToken();


        if (token == null) {

            Toast.makeText(
                    this,
                    "Не авторизован",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        api.getMyCatches(
                "Bearer " + token
        ).enqueue(
                new Callback<List<Catch>>() {

                    @Override
                    public void onResponse(
                            Call<List<Catch>> call,
                            Response<List<Catch>> response
                    ) {

                        if (
                                response.isSuccessful()
                                        && response.body() != null
                        ) {

                            catchList.clear();

                            catchList.addAll(
                                    response.body()
                            );

                            adapter.notifyDataSetChanged();

                        } else {

                            Toast.makeText(
                                    CatchListActivity.this,
                                    "Ошибка загрузки: "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<List<Catch>> call,
                            Throwable t
                    ) {

                        Log.e(
                                TAG,
                                "Error loading catches",
                                t
                        );

                        Toast.makeText(
                                CatchListActivity.this,
                                "Ошибка: "
                                        + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    @Override
    public void onEditClick(
            Catch catchItem
    ) {

        Intent intent =
                new Intent(
                        CatchListActivity.this,
                        EditCatchActivity.class
                );


        intent.putExtra(
                "catchId",
                catchItem.getId()
        );


        intent.putExtra(
                "fishType",
                catchItem.getFishType()
        );


        if (catchItem.getWeight() != null) {

            intent.putExtra(
                    "weight",
                    catchItem.getWeight()
            );
        }


        if (catchItem.getLength() != null) {

            intent.putExtra(
                    "length",
                    catchItem.getLength()
            );
        }


        intent.putExtra(
                "bait",
                catchItem.getBait()
        );


        intent.putExtra(
                "description",
                catchItem.getDescription()
        );


        if (catchItem.getLatitude() != null) {

            intent.putExtra(
                    "latitude",
                    catchItem.getLatitude()
            );
        }


        if (catchItem.getLongitude() != null) {

            intent.putExtra(
                    "longitude",
                    catchItem.getLongitude()
            );
        }


        startActivity(intent);
    }


    @Override
    public void onDeleteClick(
            Catch catchItem
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Удалить улов?")
                .setMessage(
                        "Ты уверен, что хочешь удалить "
                                + catchItem.getFishType()
                                + "?"
                )
                .setPositiveButton(
                        "Удалить",
                        (dialog, which) ->
                                deleteCatch(
                                        catchItem.getId()
                                )
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    private void deleteCatch(
            Long catchId
    ) {

        String token =
                tokenManager.getToken();


        if (token == null) {

            Toast.makeText(
                    this,
                    "Не авторизован",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        api.deleteCatch(
                "Bearer " + token,
                catchId
        ).enqueue(
                new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    CatchListActivity.this,
                                    "Улов удалён",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadMyCatches();

                        } else {

                            Toast.makeText(
                                    CatchListActivity.this,
                                    "Ошибка удаления: "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                CatchListActivity.this,
                                "Ошибка: "
                                        + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    private void logout() {

        tokenManager.clear();


        Intent intent =
                new Intent(
                        CatchListActivity.this,
                        MainActivity.class
                );


        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }

}

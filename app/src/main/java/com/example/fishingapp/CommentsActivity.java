package com.example.fishingapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fishingapp.api.ApiClient;
import com.example.fishingapp.api.FishingApi;
import com.example.fishingapp.model.Comment;
import com.example.fishingapp.model.CommentsResponse;
import com.example.fishingapp.model.Catch;
import com.example.fishingapp.utils.TokenManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsActivity extends AppCompatActivity
        implements CommentAdapter.CommentActionListener {

    private static final String TAG = "CommentsActivity";


    private Long catchId;

    private String fishType;


    private TokenManager tokenManager;

    private FishingApi api;


    private ListView commentsListView;

    private EditText commentEditText;

    private Button sendCommentButton;

    private Button backButton;

    private TextView commentsTitleTextView;

    private TextView catchTitleTextView;


    private CommentAdapter adapter;

    private final List<Comment> commentList =
            new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_comments
        );


        // ========================================================
        // Intent data
        // ========================================================

        catchId =
                getIntent().getLongExtra(
                        "catchId",
                        -1
                );

        fishType =
                getIntent().getStringExtra(
                        "fishType"
                );


        if (catchId == null || catchId <= 0) {

            Toast.makeText(
                    this,
                    "Некорректный ID улова",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }


        // ========================================================
        // Init
        // ========================================================

        tokenManager =
                new TokenManager(this);

        api =
                ApiClient.getApi();


        commentsListView =
                findViewById(
                        R.id.commentsListView
                );

        commentEditText =
                findViewById(
                        R.id.commentEditText
                );

        sendCommentButton =
                findViewById(
                        R.id.sendCommentButton
                );

        backButton =
                findViewById(
                        R.id.backButton
                );

        commentsTitleTextView =
                findViewById(
                        R.id.commentsTitleTextView
                );

        catchTitleTextView =
                findViewById(
                        R.id.catchTitleTextView
                );


        // ========================================================
        // Header
        // ========================================================

        if (fishType != null && !fishType.isEmpty()) {

            catchTitleTextView.setText(
                    fishType
            );
        }


        // ========================================================
        // Adapter
        // ========================================================

        adapter =
                new CommentAdapter(
                        this,
                        commentList,
                        this
                );

        commentsListView.setAdapter(
                adapter
        );


        // ========================================================
        // Buttons
        // ========================================================

        backButton.setOnClickListener(
                v -> finish()
        );

        sendCommentButton.setOnClickListener(
                v -> sendComment()
        );


        // ========================================================
        // Load
        // ========================================================

        loadComments();
    }


    @Override
    protected void onResume() {

        super.onResume();

        /*
         * Обновляем список после возвращения.
         */
        if (api != null) {
            loadComments();
        }
    }


    // ============================================================
    // Load comments
    // ============================================================

    private void loadComments() {

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


        String authHeader =
                "Bearer " + token;


        api.getCatchComments(
                authHeader,
                catchId
        ).enqueue(
                new Callback<CommentsResponse>() {

                    @Override
                    public void onResponse(
                            Call<CommentsResponse> call,
                            Response<CommentsResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            CommentsResponse result =
                                    response.body();


                            commentList.clear();


                            if (result.getComments() != null) {

                                commentList.addAll(
                                        result.getComments()
                                );
                            }


                            adapter.notifyDataSetChanged();


                            updateTitle(
                                    result.getCount()
                            );

                        } else {

                            Toast.makeText(
                                    CommentsActivity.this,
                                    "Ошибка загрузки комментариев: "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<CommentsResponse> call,
                            Throwable t
                    ) {

                        Log.e(
                                TAG,
                                "Failed to load comments",
                                t
                        );

                        Toast.makeText(
                                CommentsActivity.this,
                                "Ошибка: "
                                        + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    // ============================================================
    // Title
    // ============================================================

    private void updateTitle(long count) {

        commentsTitleTextView.setText(
                "Комментарии (" + count + ")"
        );
    }


    // ============================================================
    // Add comment
    // ============================================================

    private void sendComment() {

        String content =
                commentEditText
                        .getText()
                        .toString()
                        .trim();


        if (content.isEmpty()) {

            commentEditText.setError(
                    "Введите комментарий"
            );

            return;
        }


        if (content.length() > 1000) {

            commentEditText.setError(
                    "Максимум 1000 символов"
            );

            return;
        }


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


        String authHeader =
                "Bearer " + token;


        Map<String, String> request =
                new HashMap<>();

        request.put(
                "content",
                content
        );


        sendCommentButton.setEnabled(
                false
        );


        api.addComment(
                authHeader,
                catchId,
                request
        ).enqueue(
                new Callback<Comment>() {

                    @Override
                    public void onResponse(
                            Call<Comment> call,
                            Response<Comment> response
                    ) {

                        sendCommentButton.setEnabled(
                                true
                        );


                        if (response.isSuccessful()) {

                            commentEditText
                                    .setText("");


                            /*
                             * Не пытаемся вручную считать
                             * количество комментариев.
                             *
                             * Получаем актуальное состояние
                             * с backend.
                             */
                            loadComments();

                        } else {

                            Toast.makeText(
                                    CommentsActivity.this,
                                    "Ошибка добавления: "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<Comment> call,
                            Throwable t
                    ) {

                        sendCommentButton.setEnabled(
                                true
                        );


                        Log.e(
                                TAG,
                                "Failed to add comment",
                                t
                        );


                        Toast.makeText(
                                CommentsActivity.this,
                                "Ошибка: "
                                        + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }


    // ============================================================
    // Delete comment
    // ============================================================

    @Override
    public void onDeleteComment(
            Comment comment
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Удалить комментарий?")
                .setMessage(
                        "Комментарий будет удалён."
                )
                .setPositiveButton(
                        "Удалить",
                        (dialog, which) ->
                                deleteComment(
                                        comment
                                )
                )
                .setNegativeButton(
                        "Отмена",
                        null
                )
                .show();
    }


    private void deleteComment(
            Comment comment
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


        String authHeader =
                "Bearer " + token;


        api.deleteComment(
                authHeader,
                comment.getId()
        ).enqueue(
                new Callback<Map<String, String>>() {

                    @Override
                    public void onResponse(
                            Call<Map<String, String>> call,
                            Response<Map<String, String>> response
                    ) {

                        if (response.isSuccessful()) {

                            loadComments();

                        } else {

                            Toast.makeText(
                                    CommentsActivity.this,
                                    "Удаление не выполнено: "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<Map<String, String>> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                CommentsActivity.this,
                                "Ошибка: "
                                        + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}
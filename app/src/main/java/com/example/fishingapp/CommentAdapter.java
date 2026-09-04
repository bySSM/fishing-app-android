package com.example.fishingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fishingapp.model.Comment;

import java.util.List;

public class CommentAdapter extends ArrayAdapter<Comment> {

    public interface CommentActionListener {

        void onDeleteComment(Comment comment);
    }


    private final Context context;

    private final List<Comment> comments;

    private final CommentActionListener listener;


    public CommentAdapter(
            @NonNull Context context,
            @NonNull List<Comment> comments,
            CommentActionListener listener
    ) {
        super(context, 0, comments);

        this.context = context;
        this.comments = comments;
        this.listener = listener;
    }


    @NonNull
    @Override
    public View getView(
            int position,
            @Nullable View convertView,
            @NonNull ViewGroup parent
    ) {

        Comment comment = comments.get(position);


        if (convertView == null) {

            convertView = LayoutInflater
                    .from(context)
                    .inflate(
                            R.layout.item_comment,
                            parent,
                            false
                    );
        }


        TextView usernameView =
                convertView.findViewById(
                        R.id.commentUsernameTextView
                );

        TextView dateView =
                convertView.findViewById(
                        R.id.commentDateTextView
                );

        TextView contentView =
                convertView.findViewById(
                        R.id.commentContentTextView
                );

        Button deleteButton =
                convertView.findViewById(
                        R.id.deleteCommentButton
                );


        // Username

        if (comment.getUsername() != null
                && !comment.getUsername().isEmpty()) {

            usernameView.setText(
                    comment.getUsername()
            );

        } else {

            usernameView.setText(
                    "Пользователь"
            );
        }


        // Content

        contentView.setText(
                comment.getContent()
        );


        // Date

        String createdAt =
                comment.getCreatedAt();

        if (createdAt != null
                && createdAt.length() >= 16) {

            dateView.setText(
                    createdAt.substring(0, 16)
                            .replace('T', ' ')
            );

        } else {

            dateView.setText("");
        }


        // Delete

        deleteButton.setOnClickListener(v -> {

            if (listener != null) {

                listener.onDeleteComment(
                        comment
                );
            }
        });


        return convertView;
    }
}
package com.example.fishingapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fishingapp.model.Catch;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CatchAdapter extends ArrayAdapter<Catch> {

    public interface CatchActionListener {

        void onEditClick(Catch catchItem);

        void onDeleteClick(Catch catchItem);
    }


    private final Context context;

    private final List<Catch> catches;

    private final CatchActionListener actionListener;


    public CatchAdapter(
            @NonNull Context context,
            @NonNull List<Catch> catches,
            CatchActionListener listener
    ) {

        super(
                context,
                0,
                catches
        );

        this.context = context;

        this.catches = catches;

        this.actionListener = listener;
    }


    @NonNull
    @Override
    public View getView(
            int position,
            @Nullable View convertView,
            @NonNull ViewGroup parent
    ) {

        Catch catchItem =
                catches.get(position);


        if (convertView == null) {

            convertView =
                    LayoutInflater
                            .from(context)
                            .inflate(
                                    R.layout.item_catch,
                                    parent,
                                    false
                            );
        }


        ImageView photoView =
                convertView.findViewById(
                        R.id.catchImageView
                );

        TextView fishTypeView =
                convertView.findViewById(
                        R.id.fishTypeTextView
                );

        TextView weightView =
                convertView.findViewById(
                        R.id.weightTextView
                );

        TextView dateView =
                convertView.findViewById(
                        R.id.dateTextView
                );

        Button likeButton =
                convertView.findViewById(
                        R.id.likeButton
                );

        Button commentsButton =
                convertView.findViewById(
                        R.id.commentsButton
                );

        Button editButton =
                convertView.findViewById(
                        R.id.editButton
                );

        Button deleteButton =
                convertView.findViewById(
                        R.id.deleteButton
                );


        fishTypeView.setText(
                catchItem.getFishType()
        );


        if (catchItem.getWeight() != null) {

            weightView.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.1f кг",
                            catchItem.getWeight()
                    )
            );

        } else {

            weightView.setText(
                    "Вес не указан"
            );
        }


        if (catchItem.getCreatedAt() != null) {

            try {

                SimpleDateFormat inputFormat =
                        new SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss",
                                Locale.getDefault()
                        );

                SimpleDateFormat outputFormat =
                        new SimpleDateFormat(
                                "dd.MM.yyyy HH:mm",
                                Locale.getDefault()
                        );

                Date date =
                        inputFormat.parse(
                                catchItem.getCreatedAt()
                        );

                if (date != null) {

                    dateView.setText(
                            outputFormat.format(date)
                    );
                }

            } catch (Exception e) {

                dateView.setText(
                        catchItem.getCreatedAt()
                );
            }
        }


        int likes =
                catchItem.getLikesCount() != null
                        ? catchItem.getLikesCount()
                        : 0;

        boolean liked =
                Boolean.TRUE.equals(
                        catchItem.getLikedByCurrentUser()
                );

        likeButton.setText(
                liked
                        ? "❤️ " + likes
                        : "♡ " + likes
        );


        /*
         * ВАЖНО:
         *
         * Лайк больше не отправляется здесь.
         * Нажатие на кнопку открывает единую страницу
         * конкретного улова.
         */
        likeButton.setOnClickListener(
                v -> openCatchDetails(catchItem)
        );


        /*
         * Аналогично комментарии.
         */
        commentsButton.setOnClickListener(
                v -> openCatchDetails(catchItem)
        );


        /*
         * Нажатие на саму карточку тоже открывает
         * страницу улова.
         */
        convertView.setOnClickListener(
                v -> openCatchDetails(catchItem)
        );


        String photoUrl =
                catchItem.getFullPhotoUrl();

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


        editButton.setOnClickListener(
                v -> {

                    if (actionListener != null) {

                        actionListener.onEditClick(
                                catchItem
                        );
                    }
                }
        );


        deleteButton.setOnClickListener(
                v -> {

                    if (actionListener != null) {

                        actionListener.onDeleteClick(
                                catchItem
                        );
                    }
                }
        );


        return convertView;
    }


    private void openCatchDetails(
            Catch catchItem
    ) {

        if (catchItem.getId() == null) {
            return;
        }

        Intent intent =
                new Intent(
                        context,
                        CatchDetailActivity.class
                );

        intent.putExtra(
                CatchDetailActivity.EXTRA_CATCH_ID,
                catchItem.getId()
        );

        context.startActivity(intent);
    }

}

package com.example.fishingapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fishingapp.model.Catch;
import com.squareup.picasso.Picasso;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class CatchInfoWindow extends InfoWindow {

    private Catch currentCatch;

    public CatchInfoWindow(MapView mapView) {
        super(R.layout.info_window_catch, mapView);
    }

    @Override
    public void onOpen(Object item) {

        InfoWindow.closeAllInfoWindowsOn(mMapView);

        if (!(item instanceof Marker marker)) {
            close();
            return;
        }

        if (!(marker.getRelatedObject() instanceof Catch catchItem)) {
            close();
            return;
        }

        currentCatch = catchItem;

        Context context = mView.getContext();

        ImageView photoView =
                mView.findViewById(R.id.catchPhotoView);

        TextView noPhotoText =
                mView.findViewById(R.id.noPhotoText);

        TextView fishTypeText =
                mView.findViewById(R.id.fishTypeText);

        TextView weightText =
                mView.findViewById(R.id.weightText);

        TextView usernameText =
                mView.findViewById(R.id.usernameText);

        TextView tapHintText =
                mView.findViewById(R.id.tapHintText);

        ImageButton closeButton =
                mView.findViewById(R.id.closeInfoButton);


        /*
         * Название рыбы.
         */
        String fishType =
                catchItem.getFishType();

        fishTypeText.setText(
                fishType != null
                        ? fishType
                        : context.getString(
                        R.string.catch_default_title
                )
        );


        /*
         * Вес.
         */
        if (catchItem.getWeight() != null) {

            weightText.setText(
                    context.getString(
                            R.string.catch_weight_kg,
                            catchItem.getWeight()
                    )
            );

        } else {

            weightText.setText(
                    R.string.catch_weight_unknown
            );
        }


        /*
         * Автор.
         */
        String username =
                catchItem.getUsername();

        if (
                username != null
                        && !username.isEmpty()
        ) {

            usernameText.setVisibility(
                    View.VISIBLE
            );

            usernameText.setText(
                    context.getString(
                            R.string.catch_username,
                            username
                    )
            );

        } else {

            usernameText.setVisibility(
                    View.GONE
            );
        }


        /*
         * Фото.
         */
        Picasso.get().cancelRequest(
                photoView
        );


        String photoUrl =
                catchItem.getFullPhotoUrl();


        if (
                photoUrl != null
                        && !photoUrl.isEmpty()
        ) {

            noPhotoText.setVisibility(
                    View.GONE
            );

            tapHintText.setVisibility(
                    View.VISIBLE
            );


            Picasso.get()
                    .load(photoUrl)
                    .resize(640, 420)
                    .centerCrop()
                    .placeholder(
                            android.R.drawable.ic_menu_gallery
                    )
                    .error(
                            android.R.drawable.ic_menu_gallery
                    )
                    .into(photoView);


            /*
             * Фото оставляем отдельным действием:
             * нажатие открывает полноэкранную фотографию.
             */
            photoView.setOnClickListener(
                    v -> showFullPhoto(photoUrl)
            );


        } else {

            photoView.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );

            noPhotoText.setVisibility(
                    View.VISIBLE
            );

            tapHintText.setVisibility(
                    View.GONE
            );


            photoView.setOnClickListener(
                    v ->
                            Toast.makeText(
                                    context,
                                    R.string.catch_no_photo_toast,
                                    Toast.LENGTH_SHORT
                            ).show()
            );
        }


        /*
         * Кнопка закрытия.
         */
        closeButton.setOnClickListener(
                v -> close()
        );


        /*
         * Главное изменение.
         *
         * tapHintText теперь является точкой входа
         * в полноценную карточку улова.
         *
         * Здесь currentCatch — это именно тот Catch,
         * который был привязан к Marker в MapActivity:
         *
         * marker.setRelatedObject(catchItem)
         */
        tapHintText.setOnClickListener(
                v -> openCatchDetails()
        );


        /*
         * Само окно не открывает детали при обычном
         * нажатии, чтобы не конфликтовать с фото
         * и кнопкой закрытия.
         */
        mView.setOnClickListener(
                v -> {
                    // Ничего не делаем.
                }
        );
    }


    private void openCatchDetails() {

        if (currentCatch == null) {
            return;
        }


        Long catchId =
                currentCatch.getId();


        if (catchId == null) {

            Toast.makeText(
                    mView.getContext(),
                    "Не удалось определить улов",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Intent intent =
                new Intent(
                        mView.getContext(),
                        CatchDetailActivity.class
                );


        intent.putExtra(
                CatchDetailActivity.EXTRA_CATCH_ID,
                catchId
        );


        mView.getContext().startActivity(
                intent
        );


        close();
    }


    @Override
    public void onClose() {

        currentCatch = null;

        ImageView photoView =
                mView.findViewById(
                        R.id.catchPhotoView
                );


        if (photoView != null) {

            Picasso.get().cancelRequest(
                    photoView
            );
        }
    }


    private void showFullPhoto(
            String photoUrl
    ) {

        Dialog dialog =
                new Dialog(
                        mView.getContext(),
                        android.R.style.Theme_Black_NoTitleBar_Fullscreen
                );


        dialog.setContentView(
                R.layout.dialog_catch_photo
        );


        ImageView fullPhotoView =
                dialog.findViewById(
                        R.id.fullPhotoView
                );


        ImageButton closeFullPhotoButton =
                dialog.findViewById(
                        R.id.closeFullPhotoButton
                );


        Picasso.get()
                .load(photoUrl)
                .placeholder(
                        android.R.drawable.ic_menu_gallery
                )
                .error(
                        android.R.drawable.ic_menu_gallery
                )
                .into(fullPhotoView);


        View.OnClickListener dismiss =
                v -> dialog.dismiss();


        closeFullPhotoButton.setOnClickListener(
                dismiss
        );


        fullPhotoView.setOnClickListener(
                dismiss
        );


        dialog.show();
    }

}

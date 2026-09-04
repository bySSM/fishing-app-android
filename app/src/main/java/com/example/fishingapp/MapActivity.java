// app/src/main/java/com/example/fishingapp/MapActivity.java
package com.example.fishingapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fishingapp.api.ApiClient;
import com.example.fishingapp.api.FishingApi;
import com.example.fishingapp.model.Catch;
import com.example.fishingapp.utils.LocationHelper;
import com.example.fishingapp.utils.TokenManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";

    private static final int CATCH_MARKER_WIDTH_DP = 56;
    private static final int CATCH_MARKER_HEIGHT_DP = 70;
    private static final int MY_LOCATION_MARKER_DP = 48;

    private MapView mapView;
    private TokenManager tokenManager;
    private FishingApi api;
    private Button backButton;
    private Button nearbyButton;
    private List<Marker> markers = new ArrayList<>();
    private Marker myLocationMarker;
    private LocationHelper locationHelper;
    private double currentLat = 55.7558;
    private double currentLng = 37.6173;

    private Drawable catchMarkerIcon;
    private Drawable myLocationIcon;
    private CatchInfoWindow catchInfoWindow;
    private MapEventsOverlay mapEventsOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));

        setContentView(R.layout.activity_map);

        tokenManager = new TokenManager(this);
        api = ApiClient.getApi();
        locationHelper = new LocationHelper(this);

        catchMarkerIcon = createScaledIcon(
                R.drawable.ic_marker_fish,
                CATCH_MARKER_WIDTH_DP,
                CATCH_MARKER_HEIGHT_DP
        );
        myLocationIcon = createScaledIcon(
                R.drawable.ic_marker_my_location,
                MY_LOCATION_MARKER_DP,
                MY_LOCATION_MARKER_DP
        );

        backButton = findViewById(R.id.backButton);
        nearbyButton = findViewById(R.id.nearbyButton);

        backButton.setOnClickListener(v -> finish());
        nearbyButton.setOnClickListener(v -> loadNearbyCatches());

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.OpenTopo);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);

        catchInfoWindow = new CatchInfoWindow(mapView);
        setupMapClickToCloseInfo();

        GeoPoint startPoint = new GeoPoint(55.7558, 37.6173);
        mapView.getController().setZoom(10.0);
        mapView.getController().setCenter(startPoint);

        getCurrentLocation();
    }

    private void setupMapClickToCloseInfo() {
        mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                InfoWindow.closeAllInfoWindowsOn(mapView);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });
        mapView.getOverlays().add(mapEventsOverlay);
    }

    private void getCurrentLocation() {
        Log.d(TAG, "Получаем местоположение...");

        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                currentLat = latitude;
                currentLng = longitude;

                Log.d(TAG, "Местоположение: " + latitude + ", " + longitude);

                GeoPoint myPosition = new GeoPoint(latitude, longitude);
                mapView.getController().setCenter(myPosition);
                mapView.getController().setZoom(12.0);

                updateMyLocationMarker(latitude, longitude);

                Toast.makeText(MapActivity.this,
                        "Location: " + String.format("%.4f, %.4f", latitude, longitude),
                        Toast.LENGTH_LONG).show();

                loadNearbyCatches();
            }

            @Override
            public void onLocationError(String error) {
                Log.e(TAG, "Error: " + error);
                Toast.makeText(MapActivity.this,
                        "Error " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateMyLocationMarker(double latitude, double longitude) {
        if (myLocationMarker != null) {
            mapView.getOverlays().remove(myLocationMarker);
        }

        myLocationMarker = new Marker(mapView);
        myLocationMarker.setPosition(new GeoPoint(latitude, longitude));
        myLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        myLocationMarker.setIcon(myLocationIcon);
        myLocationMarker.setTitle("Я здесь");
        myLocationMarker.setInfoWindow(null);

        mapView.getOverlays().add(myLocationMarker);
        mapView.invalidate();
    }

    private void loadNearbyCatches() {
        String token = tokenManager.getToken();
        if (token == null) {
            return;
        }

        String authHeader = "Bearer " + token;

        api.getNearbyCatches(authHeader, currentLat, currentLng, 100).enqueue(
                new Callback<List<Catch>>() {
                    @Override
                    public void onResponse(Call<List<Catch>> call, Response<List<Catch>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            displayCatchesOnMap(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Catch>> call, Throwable t) {
                        Log.e(TAG, "Error: " + t.getMessage(), t);
                    }
                });
    }

    private void displayCatchesOnMap(List<Catch> catches) {
        InfoWindow.closeAllInfoWindowsOn(mapView);

        for (Marker marker : markers) {
            mapView.getOverlays().remove(marker);
        }
        markers.clear();

        for (Catch catchItem : catches) {
            if (catchItem.getLatitude() == null || catchItem.getLongitude() == null) {
                continue;
            }

            GeoPoint position = new GeoPoint(
                    catchItem.getLatitude(),
                    catchItem.getLongitude()
            );

            Marker marker = new Marker(mapView);
            marker.setPosition(position);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(catchMarkerIcon);
            marker.setTitle(catchItem.getFishType());
            marker.setRelatedObject(catchItem);
            marker.setInfoWindow(catchInfoWindow);
            marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);

            marker.setOnMarkerClickListener((clickedMarker, map) -> {
                if (clickedMarker.isInfoWindowOpen()) {
                    clickedMarker.closeInfoWindow();
                } else {
                    clickedMarker.showInfoWindow();
                }
                map.getController().animateTo(clickedMarker.getPosition());
                return true;
            });

            mapView.getOverlays().add(marker);
            markers.add(marker);
        }

        if (currentLat != 0 && currentLng != 0) {
            updateMyLocationMarker(currentLat, currentLng);
        }

        mapView.invalidate();

        if (markers.isEmpty()) {
            Toast.makeText(this, "Нет уловов рядом", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Найдено уловов: " + markers.size(), Toast.LENGTH_SHORT).show();
        }
    }

    private Drawable createScaledIcon(int drawableRes, int widthDp, int heightDp) {
        Drawable source = ContextCompat.getDrawable(this, drawableRes);
        if (source == null) {
            return null;
        }

        float density = getResources().getDisplayMetrics().density;
        int widthPx = Math.max(1, Math.round(widthDp * density));
        int heightPx = Math.max(1, Math.round(heightDp * density));

        Bitmap bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        source.setBounds(0, 0, widthPx, heightPx);
        source.draw(canvas);

        BitmapDrawable icon = new BitmapDrawable(getResources(), bitmap);
        icon.setBounds(0, 0, widthPx, heightPx);
        return icon;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            InfoWindow.closeAllInfoWindowsOn(mapView);
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) {
            InfoWindow.closeAllInfoWindowsOn(mapView);
        }
        super.onDestroy();
    }
}
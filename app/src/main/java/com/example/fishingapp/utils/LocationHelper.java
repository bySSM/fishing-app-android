// app/src/main/java/com/example/fishingapp/utils/LocationHelper.java
package com.example.fishingapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationHelper {

    private static final String TAG = "LocationHelper";
    private static final int LOCATION_PERMISSION_REQUEST = 200;

    private Activity activity;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private com.google.android.gms.location.LocationCallback continuousCallback;

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String error);
    }

    public LocationHelper(Activity activity) {
        this.activity = activity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public boolean checkPermission() {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST);
    }

    public void getCurrentLocation(LocationCallback callback) {
        this.locationCallback = callback;

        if (!checkPermission()) {
            requestPermission();
            if (callback != null) {
                callback.onLocationError("Нет разрешения на геолокацию");
            }
            return;
        }

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(activity, location -> {
                        if (location != null) {
                            Log.d(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());
                            if (callback != null) {
                                callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                            }
                        } else {
                            // Пробуем lastLocation
                            fusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(activity, lastLocation -> {
                                        if (lastLocation != null) {
                                            if (callback != null) {
                                                callback.onLocationReceived(lastLocation.getLatitude(), lastLocation.getLongitude());
                                            }
                                        } else {
                                            if (callback != null) {
                                                callback.onLocationError("GPS не определил местоположение. Включи GPS и попробуй снова.");
                                            }
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(activity, e -> {
                        Log.e(TAG, "Error: " + e.getMessage(), e);
                        if (callback != null) {
                            callback.onLocationError("Ошибка GPS: " + e.getMessage());
                        }
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception", e);
            if (callback != null) {
                callback.onLocationError("Ошибка безопасности");
            }
        }
    }

    public void stopContinuousUpdates() {
        if (continuousCallback != null) {
            fusedLocationClient.removeLocationUpdates(continuousCallback);
            continuousCallback = null;
        }
    }
}
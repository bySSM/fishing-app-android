// app/src/main/java/com/example/fishingapp/utils/LocationHelper.java
package com.example.fishingapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class LocationHelper {

    private static final String TAG = "LocationHelper";
    private static final int LOCATION_PERMISSION_REQUEST = 200;

    private static final long FRESH_LOCATION_TIMEOUT_MS = 8000;

    private Activity activity;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

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
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(activity, lastLocation -> {
                        boolean hadLastLocation = lastLocation != null;
                        if (hadLastLocation && callback != null) {
                            callback.onLocationReceived(lastLocation.getLatitude(), lastLocation.getLongitude());
                        }
                        requestFreshLocation(callback, hadLastLocation);
                    })
                    .addOnFailureListener(activity, e -> requestFreshLocation(callback, false));
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception", e);
            if (callback != null) {
                callback.onLocationError("Ошибка безопасности");
            }
        }
    }

    private void requestFreshLocation(LocationCallback callback, boolean hadLastLocation) {
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            cancellationTokenSource.cancel();
            Log.d(TAG, "Fresh location timed out after " + FRESH_LOCATION_TIMEOUT_MS + "ms");
            if (!hadLastLocation && callback != null) {
                callback.onLocationError("GPS не определил местоположение. Включи GPS и попробуй снова.");
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, FRESH_LOCATION_TIMEOUT_MS);

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            cancellationTokenSource.getToken())
                    .addOnSuccessListener(activity, location -> {
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        if (location != null) {
                            Log.d(TAG, "Fresh location: " + location.getLatitude() + ", " + location.getLongitude());
                            if (callback != null) {
                                callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                            }
                        } else if (!hadLastLocation && callback != null) {
                            callback.onLocationError("GPS не определил местоположение. Включи GPS и попробуй снова.");
                        }
                    })
                    .addOnFailureListener(activity, e -> {
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        Log.e(TAG, "Error: " + e.getMessage(), e);
                        if (!hadLastLocation && callback != null) {
                            callback.onLocationError("Ошибка GPS: " + e.getMessage());
                        }
                    });
        } catch (SecurityException e) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            if (!hadLastLocation && callback != null) {
                callback.onLocationError("Ошибка безопасности");
            }
        }
    }
}
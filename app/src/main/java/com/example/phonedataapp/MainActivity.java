package com.example.phonedataapp;
/*
* This defines the package name for the app and imports all the required libraries and classes.
* Without these imports, the app won’t know about Android components like
* TextView, Location, or FusedLocationProviderClient.
* */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.util.List;

/*
* This is the main activity class where the app starts. It extends AppCompatActivity, which is a base class for activities in Android.
* This is where all the app logic lives.
* */
public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 1001;
    private static final String TAG = "MainActivity";

    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationTextView;
    private LocationCallback locationCallback;

    /* 
    * This is the first method that runs when the app starts. 
    * It sets up the user interface, fetches device information, and starts the process of getting the location.
    * This is where the app initializes everything and prepares to interact with the user.
     */    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to the TextViews
        TextView deviceModelTextView = findViewById(R.id.deviceModel);
        TextView androidVersionTextView = findViewById(R.id.androidVersion);
        TextView installedAppsTextView = findViewById(R.id.installedApps);
        locationTextView = findViewById(R.id.location);

        // Extract device information
        String deviceModel = Build.MODEL;
        String androidVersion = Build.VERSION.RELEASE;

        // Display the extracted data
        deviceModelTextView.setText("Device Model: " + deviceModel);
        androidVersionTextView.setText("Android Version: " + androidVersion);

        // Fetch and display installed apps
        Log.d(TAG, "Fetching installed apps...");
        installedAppsTextView.setText(getLaunchableApps());

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Initialize LocationCallback
        locationCallback = new LocationCallback() {
            @Override
                public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    locationTextView.setText("Location: " + latitude + ", " + longitude);
                }
            }
        };
        // Check and request location permission
        checkAndRequestLocationPermission();
    }

    /*
     * This method fetches all the installed apps on the device that are launchable.
     * It uses the PackageManager class to query the device for launchable apps.
     * It then filters the apps based on keywords related to finance.
     * This is where the app fetches and displays the installed apps on the device.
     */
    private String getLaunchableApps() {
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = packageManager.queryIntentActivities(intent, 0);
        StringBuilder installedApps = new StringBuilder("Installed Apps:\n");

        if (apps.isEmpty()) {
            Log.d(TAG, "No launchable apps found.");
            installedApps.append("No launchable apps found.");
        } else {
            for (ResolveInfo info : apps) {
                String appName = info.loadLabel(packageManager).toString();
                String packageName = info.activityInfo.packageName;

                Log.d(TAG, "App: " + appName + " (" + packageName + ")");

                if (isFinancialApp(appName, packageName)) {
                    installedApps.append("(Financial) ");
                }
                installedApps.append(appName).append(" (").append(packageName).append(")\n");
            }
        }

        return installedApps.toString();
    }

    /*
     * This method checks if an app is related to finance based on keywords in the app name or package name.
     * It returns true if the app is related to finance and false otherwise.
     * This is where the app determines if an app is related to finance based on keywords.
     */
    private boolean isFinancialApp(String appName, String packageName) {
        String[] financialKeywords = {"bank", "finance", "pay", "wallet", "investment", "credit", "loan", "seb", "nordea", "swish", "zettle"};

        for (String keyword : financialKeywords) {
            if (appName.toLowerCase().contains(keyword) || packageName.toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Checks if the app has permission to access the device’s location. If not, it asks the user for permission.
     * If the app already has permission, it proceeds to check the location settings.
     * Location access is a sensitive permission, so the app must request it explicitly.
    */
    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkLocationSettings();
        }
    }

    /*
     * This method checks the location settings(GPS) on the device is enabled and requests the last known location.
     * If the location settings are not satisfied, it prompts the user to change them.
     * If the location is not available, it requests location updates.
     * If the location is available, it displays the location in the locationTextView.
     */
    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest());

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getLastLocation();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.e(TAG, "Error opening location settings", sendEx);
                    }
                }
            }
        });
    }


    /* 
     * This method is called when the user responds to the permission request.
     * It checks if the user granted the location permission and then proceeds to check the location settings.
     * If the user denied the permission, it displays a message in the locationTextView.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettings();
            } else {
                locationTextView.setText("Location permission denied");
            }
        }
    }

    /* 
     * This method creates a LocationRequest object with the desired accuracy and interval for location updates.
     * It sets the priority to high accuracy and the interval to 10 seconds.
     * The fastest interval is set to 5 seconds.
     * This method is used to request location updates.
     */
    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        return locationRequest;
    }

    /*
     * This method gets the last known location of the device using the FusedLocationProviderClient.
     * If the location is available, it displays the location in the locationTextView.
     * If the location is not available, it requests location updates.
     * If the app doesn’t have location permissions, it displays a message in the locationTextView.
     * If there is an error getting the location, it logs the error and displays a message in the locationTextView.
     * This is where the app fetches and displays the device’s location.
     */
    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                locationTextView.setText("Location: " + latitude + ", " + longitude);
                            } else {
                                locationTextView.setText("Location not available, requesting updates...");
                                requestLocationUpdates();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get last known location", e);
                        locationTextView.setText("Failed to get location");
                    });
        } else {
            locationTextView.setText("Location permissions not granted");
        }
    }

    /* 
     * This method requests location updates using the FusedLocationProviderClient.
     * It uses the LocationRequest object created by the createLocationRequest method.
     * This is where the app requests location updates.
     * Ensures the app can get the current location even if the last known location is outdated.
     */
    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(createLocationRequest(), locationCallback, Looper.getMainLooper());
        } else {
            locationTextView.setText("Location permissions not granted");
        }
    }

    /* 
     * This method is called when the user responds to the location settings request.
     * If the user enables the location settings, it proceeds to get the last known location.
     * If the user doesn’t enable the location settings, it displays a message in the locationTextView.
     * This is where the app handles the user’s response to the location settings request.
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                getLastLocation();
            } else {
                locationTextView.setText("Location services are required");
            }
        }
    }

    /* 
     * This method is called when the app is paused.
     * It stops location updates to save battery and resources.
     * This is where the app stops location updates when it’s paused.
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /* 
     * This method is called when the app is resumed.
     * It checks the location settings and requests the last known location.
     * This is where the app resumes location updates when it’s resumed.
     */
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
package com.example.places;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;

import com.example.places.databinding.ActivityMainBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraChangeListener {

    private ActivityMainBinding binding;
    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private static final int REQUEST_LOCATION_SETTINGS = 1;
    private static final int REQUEST_WIFI_SETTINGS = 2;
    private CoordinatorLayout mMainLayout = null;
    private static boolean mUserDeniedPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        checkSettings();
    }

    private void completeSetUp() {
        requestLocationPermission();
    }

    private void checkSettings() {
        final boolean gpsEnabled = locationTrackingEnabled();
        final boolean internetConnected = internetConnectivity();

        if (gpsEnabled && internetConnected) {
            completeSetUp();
        } else if (!gpsEnabled) {
            final Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            showDialog(gpsIntent, REQUEST_LOCATION_SETTINGS, getString(R.string.location_tracking_off));
        } else {
            final Intent internetIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            showDialog(internetIntent, REQUEST_WIFI_SETTINGS, getString(R.string.wireless_off));
        }
    }

    private void showDialog(final Intent intent, final int requestCode, final String message) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Yes", (dialog, which) -> startActivityForResult(intent, requestCode));
        alertDialog.setNegativeButton("No", (dialog, which) -> finish());
        alertDialog.create().show();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || mUserDeniedPermission) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }
    }

    private boolean locationTrackingEnabled() {
        final LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean internetConnectivity() {
        final ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifi = connManager.getActiveNetworkInfo();
        return wifi != null && wifi.isConnected();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_WIFI_SETTINGS || requestCode == REQUEST_LOCATION_SETTINGS) {
            checkSettings();
        }

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}

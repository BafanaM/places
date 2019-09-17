package com.example.places;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.esri.arcgisruntime.geometry.Envelope;
import com.example.places.databinding.ActivityMainBinding;
import com.example.places.filter.FilterContract;
import com.example.places.filter.FilterDialogFragment;
import com.example.places.filter.FilterPresenter;
import com.example.places.places.PlacesFragment;
import com.example.places.places.PlacesPresenter;
import com.example.places.util.ActivityUtils;

public class PlacesActivity extends AppCompatActivity implements FilterContract.FilterView,
        ActivityCompat.OnRequestPermissionsResultCallback, PlacesFragment.FragmentListener {

    private ActivityMainBinding binding;
    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private static final int REQUEST_LOCATION_SETTINGS = 1;
    private static final int REQUEST_WIFI_SETTINGS = 2;
    private static boolean mUserDeniedPermission = false;
    private PlacesPresenter mPresenter = null;
    private PlacesFragment mPlacesFragment = null;

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

    private void showMap() {
        final Envelope envelope = mPresenter.getExtentForNearbyPlaces();
        final Intent intent = createMapIntent(this, envelope);
        startActivity(intent);
    }

    public static Intent createMapIntent(final Activity a, final Envelope envelope) {
        final Intent intent = new Intent(a, MapActivity.class);
        if (envelope != null) {
            intent.putExtra("MIN_X", envelope.getXMin());
            intent.putExtra("MIN_Y", envelope.getYMin());
            intent.putExtra("MAX_X", envelope.getXMax());
            intent.putExtra("MAX_Y", envelope.getYMax());
            intent.putExtra("SR", envelope.getSpatialReference().getWKText());
        }
        return intent;
    }

    private void setUpToolbar() {
        setSupportActionBar(binding.placeListToolbar);

        assert binding.placeListToolbar != null;
        binding.placeListToolbar.setOnMenuItemClickListener(item -> {
            if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.map_view))) {
                showMap();
            }
            if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.filter))) {
                final FilterDialogFragment dialogFragment = new FilterDialogFragment();
                final FilterPresenter filterPresenter = new FilterPresenter();
                dialogFragment.setPresenter(filterPresenter);
                dialogFragment.show(getFragmentManager(), "dialog_fragment");

            }
            return false;
        });
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || mUserDeniedPermission) {
            setUpToolbar();
            setUpFragments();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }
    }

    private void setUpFragments() {

        mPlacesFragment = (PlacesFragment) getSupportFragmentManager().findFragmentById(R.id.recycleView);

        if (mPlacesFragment == null) {
            mPlacesFragment = PlacesFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), mPlacesFragment, R.id.list_fragment_container, "list fragment");
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
    public void onFilterDialogClose(boolean applyFilter) {

    }

    @Override
    public void onCreationComplete() {

    }
}

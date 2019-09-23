package com.example.places.places;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;

import com.esri.arcgisruntime.geometry.Envelope;
import com.example.places.R;
import com.example.places.filter.FilterContract;
import com.example.places.filter.FilterDialogFragment;
import com.example.places.filter.FilterPresenter;
import com.example.places.map.MapActivity;
import com.example.places.util.ActivityUtils;
import com.google.android.material.snackbar.Snackbar;

public class PlacesActivity extends AppCompatActivity implements FilterContract.FilterView,
        ActivityCompat.OnRequestPermissionsResultCallback, PlacesFragment.FragmentListener {

    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private static final int REQUEST_LOCATION_SETTINGS = 1;
    private static final int REQUEST_WIFI_SETTINGS = 2;
    private PlacesFragment mPlacesFragment = null;
    private CoordinatorLayout mMainLayout = null;
    private PlacesPresenter mPresenter = null;

    private static boolean mUserDeniedPermission = false;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainLayout = findViewById(R.id.list_coordinator_layout);
        checkSettings();
    }

    private void completeSetUp(){
        requestLocationPermission();
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setUpToolbar(){
        final Toolbar toolbar = findViewById(R.id.placeList_toolbar);
        setSupportActionBar(toolbar);

        assert toolbar != null;
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override public boolean onMenuItemClick(final MenuItem item) {
                if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.map_view))){
                    showMap();
                }
                if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.filter))){
                    final FilterDialogFragment dialogFragment = new FilterDialogFragment();
                    final FilterPresenter filterPresenter = new FilterPresenter();
                    dialogFragment.setPresenter(filterPresenter);
                    dialogFragment.show(getFragmentManager(),"dialog_fragment");

                }
                return false;
            }
        });
    }
    @Override public final void onFilterDialogClose(final boolean applyFilter) {
        if (applyFilter){
            mPresenter.start();
        }
    }
    public static Intent createMapIntent(final Activity a, final Envelope envelope){
        final Intent intent = new Intent(a, MapActivity.class);

        if (envelope != null){
            intent.putExtra("MIN_X", envelope.getXMin());
            intent.putExtra("MIN_Y", envelope.getYMin());
            intent.putExtra("MAX_X", envelope.getXMax());
            intent.putExtra("MAX_Y", envelope.getYMax());
            intent.putExtra("SR", envelope.getSpatialReference().getWKText());
        }
        return  intent;
    }
    private void showMap(){
        final Envelope envelope = mPresenter.getExtentForNearbyPlaces();
        final Intent intent = createMapIntent(this, envelope);
        startActivity(intent);
    }

    private void setUpFragments(){
        mPlacesFragment = (PlacesFragment) getSupportFragmentManager().findFragmentById(R.id.recycleView) ;

        if (mPlacesFragment == null){
            mPlacesFragment = PlacesFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), mPlacesFragment, R.id.list_fragment_container, "list fragment");
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || mUserDeniedPermission) {
            setUpToolbar();
            setUpFragments();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }
    }

    @Override
    public final void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                                 @NonNull final int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (!mUserDeniedPermission) {

                    mUserDeniedPermission = true;
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Snackbar.make(mMainLayout, "Location access is required to search for places nearby.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(final View view) {
                                        // Request the permission
                                        ActivityCompat.requestPermissions(PlacesActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                PERMISSION_REQUEST_LOCATION);
                                    }
                                }).show();
                    }
                } else {
                    setUpToolbar();
                    setUpFragments();
                }
            } else {
                setUpToolbar();
                setUpFragments();
            }
        }
    }

    @Override public void onCreationComplete() {
        mPresenter = new PlacesPresenter(mPlacesFragment);
    }

    private boolean locationTrackingEnabled() {
        final LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean internetConnectivity(){
        final ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifi = connManager.getActiveNetworkInfo();
        return wifi != null && wifi.isConnected();
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

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_WIFI_SETTINGS || requestCode == REQUEST_LOCATION_SETTINGS) {
            checkSettings();
        }

    }

    private void showDialog(final Intent intent, final int requestCode, final String message) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                startActivityForResult(intent, requestCode);
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                finish();
            }
        });
        alertDialog.create().show();
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            finish();
        }
    }
}
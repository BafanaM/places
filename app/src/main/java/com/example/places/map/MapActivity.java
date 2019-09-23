package com.example.places.map;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.places.R;
import com.example.places.filter.FilterContract;
import com.example.places.util.ActivityUtils;

public class MapActivity extends AppCompatActivity implements FilterContract.FilterView {

    private MapPresenter mMapPresenter = null;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        setUpMapFragment();

    }

    @Override
    public final void onFilterDialogClose(final boolean applyFilter) {
        if (applyFilter) {
            mMapPresenter.start();
        }
    }

    private void setUpMapFragment() {
        final FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment_container);

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), mapFragment, R.id.map_fragment_container, getString(R.string.map_fragment));
        }
        mMapPresenter = new MapPresenter(mapFragment);

    }

    @Override
    public void onBackPressed() {
        finish();

    }
}

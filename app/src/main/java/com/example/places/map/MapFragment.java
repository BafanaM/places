package com.example.places.map;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.DrawStatus;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedEvent;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.NavigationChangedEvent;
import com.esri.arcgisruntime.mapping.view.NavigationChangedListener;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.example.places.PlaceListener;
import com.example.places.places.PlacesActivity;
import com.example.places.R;
import com.example.places.data.CategoryHelper;
import com.example.places.data.Place;
import com.example.places.filter.FilterContract;
import com.example.places.filter.FilterDialogFragment;
import com.example.places.filter.FilterPresenter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapFragment extends Fragment implements MapContract.View, PlaceListener {

    private MapContract.Presenter mPresenter = null;
    private CoordinatorLayout mMapLayout = null;
    private MapView mMapView = null;
    private LocationDisplay mLocationDisplay = null;
    private GraphicsOverlay mGraphicOverlay = null;
    private boolean mInitialLocationLoaded = false;
    private Graphic mCenteredGraphic = null;

    @Nullable
    private Place mCenteredPlace = null;

    @Nullable
    private NavigationChangedListener mNavigationChangedListener = null;
    private final static String TAG = MapFragment.class.getSimpleName();
    private int mCurrentPosition = 0;
    @Nullable
    private String mCenteredPlaceName = null;
    private BottomSheetBehavior mBottomSheetBehavior = null;
    private FrameLayout mBottomSheet = null;
    private Viewpoint mViewpoint = null;
    private View mRouteHeaderView = null;
    private ProgressDialog mProgressDialog = null;

    public MapFragment() {
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public final void onCreate(final Bundle savedInstance) {

        super.onCreate(savedInstance);
        setRetainInstance(true);

        setUpToolbar();
        setToolbarTransparent();

        setHasOptionsMenu(true);
        mMapLayout = getActivity().findViewById(R.id.map_coordinator_layout);
        setUpBottomSheet();
    }

    @Override
    @Nullable
    public final View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
                                   final Bundle savedInstance) {
        final View root = layoutInflater.inflate(R.layout.map_fragment, container, false);

        final Intent intent = getActivity().getIntent();
        if (intent.getSerializableExtra("PLACE_DETAIL") != null) {
            mCenteredPlaceName = getActivity().getIntent().getStringExtra("PLACE_DETAIL");
        }
        if (intent.hasExtra("MIN_X")) {

            final double minX = intent.getDoubleExtra("MIN_X", 0);
            final double minY = intent.getDoubleExtra("MIN_Y", 0);
            final double maxX = intent.getDoubleExtra("MAX_X", 0);
            final double maxY = intent.getDoubleExtra("MAX_Y", 0);
            final String spatRefStr = intent.getStringExtra("SR");
            if (spatRefStr != null) {
                final Envelope envelope = new Envelope(minX, minY, maxX, maxY, SpatialReference.create(spatRefStr));
                mViewpoint = new Viewpoint(envelope);
            }
        }
        showProgressIndicator("Loading map");
        setUpMapView(root);
        return root;
    }

    @Override
    public final void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
    }

    private void setUpToolbar() {
        final Toolbar toolbar = getActivity().findViewById(R.id.map_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("");
        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(0);
        }

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.list_view))) {
                    showList();
                }
                if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.filter))) {
                    final FilterDialogFragment dialogFragment = new FilterDialogFragment();
                    final FilterContract.Presenter filterPresenter = new FilterPresenter();
                    dialogFragment.setPresenter(filterPresenter);
                    dialogFragment.show(getActivity().getFragmentManager(), "dialog_fragment");

                }

                return false;
            }
        });
    }

    @Override
    public void showMessage(final String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showProgressIndicator(final String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
        }
        mProgressDialog.dismiss();
        mProgressDialog.setTitle(getString(R.string.nearby_places));
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void removeRouteHeaderView() {
        if (mRouteHeaderView != null) {
            mMapLayout.removeView(mRouteHeaderView);
        }
        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.show();
        }
    }

    private void showList() {
        final Intent intent = new Intent(getActivity(), PlacesActivity.class);
        startActivity(intent);
    }

    private void setToolbarTransparent() {
        final AppBarLayout appBarLayout = getActivity().findViewById(R.id.map_appbar);
        appBarLayout.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
    }

    private void setUpMapView(final View root) {
        mMapView = root.findViewById(R.id.map);
        final ArcGISMap map = new ArcGISMap(Basemap.createNavigationVector());
        mMapView.setMap(map);

        if (mViewpoint != null) {
            mMapView.setViewpoint(mViewpoint);
        }

        mGraphicOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(mGraphicOverlay);

        mMapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
            @Override
            public void drawStatusChanged(final DrawStatusChangedEvent drawStatusChangedEvent) {
                if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED) {
                    mMapView.removeDrawStatusChangedListener(this);
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }
                    mPresenter.start();
                }
            }
        });

        mMapView.setOnTouchListener(new MapTouchListener(getActivity().getApplicationContext(), mMapView));
    }

    private void setUpBottomSheet() {
        mBottomSheetBehavior = BottomSheetBehavior.from(getActivity().findViewById(R.id.bottom_card_view));
        mBottomSheetBehavior.setPeekHeight(0);


        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull final View bottomSheet, final int newState) {
                getActivity().invalidateOptionsMenu();
                if ((newState == BottomSheetBehavior.STATE_COLLAPSED)) {
                    clearCenteredPin();
                }
            }

            @Override
            public void onSlide(@NonNull final View bottomSheet, final float slideOffset) {
            }
        });

        mBottomSheet = getActivity().findViewById(R.id.bottom_card_view);
    }

    @Override
    public final void onPrepareOptionsMenu(final Menu menu) {
        final MenuItem listItem = menu.findItem(R.id.list_action);
        final MenuItem filterItem = menu.findItem(R.id.filter_in_map);


        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            listItem.setVisible(true);
            filterItem.setVisible(true);
        } else if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            listItem.setVisible(false);
            filterItem.setVisible(true);
        }
    }

    private void setNavigationChangeListener() {
        mNavigationChangedListener = new NavigationChangedListener() {
            @Override
            public void navigationChanged(final NavigationChangedEvent navigationChangedEvent) {
                if (!mMapView.isNavigating()) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {

                            if (!mMapView.isNavigating()) {
                                onMapViewChange();
                            }
                        }
                    }, 50);
                }
            }

        };
        mMapView.addNavigationChangedListener(mNavigationChangedListener);
    }

    private void removeNavigationChangedListener() {
        if (mNavigationChangedListener != null) {
            mMapView.removeNavigationChangedListener(mNavigationChangedListener);
            mNavigationChangedListener = null;
        }
    }

    @Override
    public final void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.resume();
            mLocationDisplay = mMapView.getLocationDisplay();
            if (mLocationDisplay != null && !mLocationDisplay.isStarted()) {
                mLocationDisplay.startAsync();
            }
        }
    }

    @Override
    public final void onPause() {
        super.onPause();
        mMapView.pause();
        if (mLocationDisplay != null && mLocationDisplay.isStarted()) {
            mLocationDisplay.stop();
        }
    }

    @Override
    public final void showNearbyPlaces(final List<Place> places) {

        mGraphicOverlay.getGraphics().clear();

        if (!mInitialLocationLoaded) {
            setNavigationChangeListener();
        }
        mInitialLocationLoaded = true;
        if (places == null || places.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.no_places_found), Toast.LENGTH_SHORT).show();
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            return;
        }

        for (final Place place : places) {
            final BitmapDrawable pin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(), getDrawableForPlace(place));
            addGraphicToMap(pin, place.getLocation());
        }

        if (mCenteredPlaceName != null) {
            for (final Place p : places) {
                if (p.getName().equalsIgnoreCase(mCenteredPlaceName)) {
                    showDetail(p);
                    mCenteredPlaceName = null;
                    break;
                }
            }
        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

    }

    private void addGraphicToMap(BitmapDrawable bitdrawable, Geometry geometry) {
        final PictureMarkerSymbol pinSymbol = new PictureMarkerSymbol(bitdrawable);
        final Graphic graphic = new Graphic(geometry, pinSymbol);
        mGraphicOverlay.getGraphics().add(graphic);
    }

    @Override
    public final void showDetail(final Place place) {
        final TextView txtName = mBottomSheet.findViewById(R.id.placeName);
        txtName.setText(place.getName());
        String address = place.getAddress();
        final String[] splitStrs = TextUtils.split(address, ",");
        if (splitStrs.length > 0) {
            address = splitStrs[0];
        }
        final TextView txtAddress = mBottomSheet.findViewById(R.id.placeAddress);
        txtAddress.setText(address);
        final TextView txtPhone = mBottomSheet.findViewById(R.id.placePhone);
        txtPhone.setText(place.getPhone());

        final LinearLayout linkLayout = mBottomSheet.findViewById(R.id.linkLayout);
        if (place.getURL().isEmpty()) {
            linkLayout.setLayoutParams(new LinearLayoutCompat.LayoutParams(0, 0));
            linkLayout.requestLayout();
        } else {
            final int height = (int) (48 * Resources.getSystem().getDisplayMetrics().density);
            linkLayout.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    height));
            linkLayout.requestLayout();
            final TextView txtUrl = mBottomSheet.findViewById(R.id.placeUrl);
            txtUrl.setText(place.getURL());
        }


        final TextView txtType = mBottomSheet.findViewById(R.id.placeType);
        txtType.setText(place.getType());

        final Drawable d = CategoryHelper.getDrawableForPlace(place, getActivity());
        txtType.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        mPresenter.centerOnPlace(place);
        mCenteredPlaceName = place.getName();
    }

    @Override
    public final void onMapViewChange() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            mPresenter.setCurrentExtent(mMapView.getVisibleArea().getExtent());
        }
    }

    private static int getDrawableForPlace(final Place p) {
        return CategoryHelper.getResourceIdForPlacePin(p);
    }

    @Override
    public final MapView getMapView() {
        return mMapView;
    }

    @Override
    public final void centerOnPlace(final Place p) {
        if (p.getLocation() == null) {
            return;
        }
        removeRouteHeaderView();

        mCenteredPlace = p;

        removeNavigationChangedListener();
        final ListenableFuture<Boolean> viewCentered = mMapView.setViewpointCenterAsync(p.getLocation());
        viewCentered.addDoneListener(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (mNavigationChangedListener == null) {
                            setNavigationChangeListener();
                        }
                    }
                }, 50);

            }
        });
        clearCenteredPin();

        final List<Graphic> graphics = mGraphicOverlay.getGraphics();
        for (final Graphic g : graphics) {
            if (g.getGeometry().equals(p.getLocation())) {
                mCenteredGraphic = g;
                mCenteredGraphic.setZIndex(3);
                mCenteredGraphic.setSelected(true);
                break;
            }
        }
    }

    private void clearCenteredPin() {
        if (mCenteredGraphic != null) {
            mCenteredGraphic.setZIndex(0);
            mCenteredGraphic.setSelected(false);
        }
    }

    @Override
    public final void setPresenter(final MapContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private Place getPlaceForPoint(final Point p) {
        return mPresenter.findPlaceForPoint(p);
    }

    private class MapTouchListener extends DefaultMapViewOnTouchListener {

        public MapTouchListener(final Context context, final MapView mapView) {
            super(context, mapView);
        }

        @Override
        public final boolean onSingleTapConfirmed(final MotionEvent motionEvent) {
            final android.graphics.Point screenPoint = new android.graphics.Point(
                    (int) motionEvent.getX(),
                    (int) motionEvent.getY());
            final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView
                    .identifyGraphicsOverlayAsync(mGraphicOverlay, screenPoint, 10, false);

            identifyGraphic.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        final IdentifyGraphicsOverlayResult graphic = identifyGraphic.get();

                        final int identifyResultSize = graphic.getGraphics().size();
                        if (identifyResultSize > 0) {
                            final Graphic foundGraphic = graphic.getGraphics().get(0);
                            final Place foundPlace = getPlaceForPoint((Point) foundGraphic.getGeometry());
                            if (foundPlace != null) {
                                showDetail(foundPlace);
                            }
                        }
                    } catch (InterruptedException | ExecutionException ie) {
                        Log.e(MapFragment.TAG, ie.getMessage());
                    }
                }

            });
            return true;
        }
    }
}

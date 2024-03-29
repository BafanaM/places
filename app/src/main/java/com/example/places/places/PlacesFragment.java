package com.example.places.places;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.esri.arcgisruntime.geometry.Envelope;

import com.example.places.R;
import com.example.places.data.CategoryHelper;
import com.example.places.data.Place;
import com.example.places.networking.LocationService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PlacesFragment extends Fragment implements PlacesContract.View,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private PlacesContract.Presenter mPresenter;
    private PlacesFragment.PlacesAdapter mPlaceAdapter;
    private static final String TAG = PlacesFragment.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private FragmentListener mCallback;
    private ProgressDialog mProgressDialog;

    public PlacesFragment() {

    }

    public static PlacesFragment newInstance() {
        return new PlacesFragment();

    }

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mPlaceAdapter = new PlacesFragment.PlacesAdapter(new ArrayList<Place>());
        mCallback.onCreationComplete();
    }

    @Nullable
    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                   final Bundle savedInstance) {

        RecyclerView mPlacesView = (RecyclerView) inflater.inflate(
                R.layout.place_recycler_view, container, false);

        mPlacesView.setLayoutManager(new LinearLayoutManager(mPlacesView.getContext()));
        mPlacesView.setAdapter(mPlaceAdapter);

        return mPlacesView;
    }

    @Override
    public void onAttach(final Context activity) {
        super.onAttach(activity);

        try {
            mCallback = (FragmentListener) activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListener");
        }
    }

    @Override
    public final void showNearbyPlaces(final List<Place> places) {
        if (places.isEmpty()) {
            showMessage("No places found");
        } else {
            Collections.sort(places);
            mPlaceAdapter.setPlaces(places);
            mPlaceAdapter.notifyDataSetChanged();
        }
        mProgressDialog.dismiss();
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


    @Override
    public void showMessage(final String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }


    @Override
    public final void setPresenter(final PlacesContract.Presenter presenter) {
        mPresenter = presenter;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }


    public class PlacesAdapter extends RecyclerView.Adapter<PlacesFragment.RecyclerViewHolder> {

        private List<Place> mPlaces;

        public PlacesAdapter(final List<Place> places) {
            mPlaces = places;
        }

        public final void setPlaces(final List<Place> places) {
            mPlaces = places;
            notifyDataSetChanged();
        }

        @Override
        public final PlacesFragment.RecyclerViewHolder onCreateViewHolder(final ViewGroup parent,
                                                                          final int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View itemView = inflater.inflate(R.layout.place, parent, false);
            return new PlacesFragment.RecyclerViewHolder(itemView);
        }


        @Override
        public final void onBindViewHolder(final PlacesFragment.RecyclerViewHolder holder, final int position) {
            final Place place = mPlaces.get(position);
            holder.placeName.setText(place.getName());
            holder.address.setText(place.getAddress());
            final Drawable drawable = assignIcon(position);
            holder.icon.setImageDrawable(drawable);
            holder.distance.setText(place.getDistance() + getString(R.string.m));
            holder.bind(place);
        }

        @Override
        public final int getItemCount() {
            return mPlaces.size();
        }

        private Drawable assignIcon(final int position) {
            final Place p = mPlaces.get(position);
            return CategoryHelper.getDrawableForPlace(p, getActivity());
        }
    }


    @Override
    public final void onConnected(@Nullable final Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final Task getLocationTask = LocationServices.getFusedLocationProviderClient(this.getContext()).getLastLocation();
            getLocationTask.addOnCompleteListener(this.getActivity(), new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    startPresenter((Location) getLocationTask.getResult());
                }
            });
        } else {
            startPresenter(null);
        }
    }

    @Override
    public void onConnectionSuspended(final int i) {
        Log.i(TAG, getString(R.string.location_connection_lost));
        mGoogleApiClient.connect();
    }

    @Override
    public final void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public final void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
        Toast.makeText(getContext(), getString(R.string.google_location_connection_problem), Toast.LENGTH_LONG).show();
        Log.e(TAG, getString(R.string.google_location_problem) + connectionResult.getErrorMessage());
    }

    private void startPresenter(Location location) {
        if (location == null) {
            location = new Location("Default");
            location.setLatitude(-26.109550);
            location.setLongitude(	28.056185);
        }
        Log.i(PlacesFragment.TAG, getString(R.string.latlong) + location.getLatitude() + "/" + location.getLongitude());
        mPresenter.setLocation(location);
        LocationService.getInstance().setCurrentLocation(location);
        mPresenter.start();
    }

    public interface FragmentListener {
        void onCreationComplete();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        public final TextView placeName;
        public final TextView address;
        public final ImageView icon;
        public final TextView distance;

        public RecyclerViewHolder(final View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.placeName);
            address = itemView.findViewById(R.id.placeAddress);
            icon = itemView.findViewById(R.id.placeTypeIcon);
            distance = itemView.findViewById(R.id.placeDistance);
        }

        public final void bind(final Place place) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final Envelope envelope = mPresenter.getExtentForNearbyPlaces();
                    final Intent intent = PlacesActivity.createMapIntent(getActivity(), envelope);
                    intent.putExtra(getString(R.string.place_detail), place.getName());
                    startActivity(intent);
                }
            });
        }

    }
}

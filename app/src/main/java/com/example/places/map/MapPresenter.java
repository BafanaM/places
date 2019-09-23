
package com.example.places.map;

import androidx.annotation.NonNull;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.example.places.data.Place;
import com.example.places.networking.LocationService;
import com.example.places.networking.PlacesServiceApi;

import java.util.List;


public class MapPresenter implements MapContract.Presenter {

    private final static String TAG = MapPresenter.class.getSimpleName();

    private final MapContract.View mMapView;
    private LocationService mLocationService;
    private final static int MAX_RESULT_COUNT = 10;
    private final static String GEOCODE_URL = "http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer";
    private Place mCenteredPlace;

    public MapPresenter(@NonNull final MapContract.View mapView) {
        mMapView = mapView;
        mMapView.setPresenter(this);
    }

    @Override
    public final void findPlacesNearby() {
        mMapView.showProgressIndicator("Finding nearby places...");
        final Point g = mMapView.getMapView().getVisibleArea().getExtent().getCenter();

        if (g != null) {
            final GeocodeParameters parameters = new GeocodeParameters();
            parameters.setMaxResults(MAX_RESULT_COUNT);
            parameters.setPreferredSearchLocation(g);
            mLocationService.getPlacesFromService(parameters, new PlacesServiceApi.PlacesServiceCallback() {
                @Override
                public void onLoaded(final Object places) {
                    final List<Place> data = (List) places;
                    mMapView.showNearbyPlaces(data);
                }
            });
        }
    }

    @Override
    public final void centerOnPlace(final Place p) {
        mCenteredPlace = p;
        mMapView.centerOnPlace(mCenteredPlace);
    }

    @Override
    public final Place findPlaceForPoint(final Point p) {
        Place foundPlace = null;
        final List<Place> foundPlaces = mLocationService.getPlacesFromRepo();
        for (final Place place : foundPlaces) {
            if (p.equals(place.getLocation())) {
                foundPlace = place;
                break;
            }
        }
        return foundPlace;
    }

    @Override
    public void setCurrentExtent(final Envelope envelope) {
        mLocationService.setCurrentEnvelope(envelope);
    }

    @Override
    public final void start() {
        mLocationService = LocationService.getInstance();
        final List<Place> existingPlaces = mLocationService.getPlacesFromRepo();
        if (existingPlaces != null) {
            mMapView.showNearbyPlaces(existingPlaces);
        } else {
            LocationService.configureService(GEOCODE_URL,
                    new Runnable() {
                        @Override
                        public void run() {
                            findPlacesNearby();
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            mMapView.showMessage("The locator task was unable to load");
                        }
                    });
        }
    }

}

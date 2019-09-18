package com.example.places.networking;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Multipoint;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.example.places.data.Place;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationService implements PlacesServiceApi {

    private final static String TAG = LocationService.class.getSimpleName();
    private static LocatorTask locatorTask = null;
    private static LocationService instance = null;
    private Point point = null;
    private Envelope currentEnvelope = null;
    private RouteTask mRouteTask = null;
    private Map<String, Place> cachedPlaces = null;

    public static LocationService getInstance() {
        if (instance == null) {
            instance = new LocationService();
        }
        return instance;
    }

    public static void configureService(@NonNull String locatorUrl, @NonNull final Runnable onSuccess, @NonNull final
    Runnable onError) {
        if (null == locatorTask) {
            locatorTask = new LocatorTask(locatorUrl);
            locatorTask.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    if (locatorTask.getLoadStatus() == LoadStatus.LOADED) {
                        onSuccess.run();
                    } else if (locatorTask.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                        Log.e("LocationService", "Locator task failed to load: " + locatorTask.getLoadError().getMessage());
                        if (locatorTask.getLoadError().getCause() != null) {
                            Log.e("LocationService", "Locator task failed cause: " + locatorTask.getLoadError().getCause().getMessage());
                        }
                        onError.run();
                    }
                }
            });
            locatorTask.loadAsync();
        }
    }

    @Override
    public void getRouteFromService(final Point start, final Point end, Context context, final RouteServiceCallback callback) {
        //Do nothing
    }

    @Override
    public void getPlacesFromService(@NonNull final GeocodeParameters parameters, @NonNull final PlacesServiceCallback callback) {
        final String searchText = "";
        provisionOutputAttributes(parameters);
        provisionCategories(parameters);
        final ListenableFuture<List<GeocodeResult>> results = locatorTask
                .geocodeAsync(searchText, parameters);
        Log.i(TAG, "Geocode search started...");
        results.addDoneListener(new GeocodeProcessor(results, callback));
    }

    @Override
    public List<Place> getPlacesFromRepo() {
        return cachedPlaces != null ? filterPlaces(new ArrayList<>(cachedPlaces.values())) :
                null;
    }

    private static void provisionCategories(@NonNull final GeocodeParameters parameters) {
        final List<String> categories = parameters.getCategories();
        categories.add("Food");
        categories.add("Hotel");
        categories.add("Pizza");
        categories.add("Coffee Shop");
        categories.add("Bar or Pub");
    }

    private static void provisionOutputAttributes(@NonNull final GeocodeParameters parameters) {
        final List<String> outputAttributes = parameters.getResultAttributeNames();
        outputAttributes.clear();
        outputAttributes.add("*");
    }

    private static List<Place> filterPlaces(final List<Place> foundPlaces) {
        final Collection<Place> placesToRemove = new ArrayList<>();
        final List<String> selectedTypes = CategoryKeeper.getInstance().getSelectedTypes();
        if (!selectedTypes.isEmpty()) {
            for (final Place place : foundPlaces) {
                for (final String filter : selectedTypes) {
                    if (filter.equalsIgnoreCase(place.getType())) {
                        placesToRemove.add(place);
                    }
                }
            }
        }
        if (!placesToRemove.isEmpty()) {
            foundPlaces.removeAll(placesToRemove);
        }
        return foundPlaces;
    }

    public void setCurrentLocation(final Location currentLocation) {
        point = new Point(currentLocation.getLongitude(), currentLocation.getLatitude());
    }

    public void setCurrentEnvelope(final Envelope envelope) {
        currentEnvelope = envelope;
    }

    public Point getCurrentLocation() {
        return point;
    }

    private class GeocodeProcessor implements Runnable {
        private final ListenableFuture<List<GeocodeResult>> mResults;
        private final PlacesServiceCallback mCallback;

        public GeocodeProcessor(final ListenableFuture<List<GeocodeResult>> results, final PlacesServiceCallback callback) {
            mCallback = callback;
            mResults = results;
        }

        @Override
        public void run() {

            try {
                if (cachedPlaces == null) {
                    cachedPlaces = new HashMap<>();
                }
                cachedPlaces.clear();
                final List<GeocodeResult> data = mResults.get();
                final List<Place> places = new ArrayList<>();
                int i = 0;
                for (final GeocodeResult result : data) {
                    i = i + 1;
                    final Map<String, Object> attributes = result.getAttributes();
                    final String address = (String) attributes.get("Place_addr");
                    final String name = (String) attributes.get("PlaceName");
                    final String phone = (String) attributes.get("Phone");
                    final String url = (String) attributes.get("URL");
                    final String type = (String) attributes.get("Type");
                    final Point location = result.getDisplayLocation();

                    final Place place = new Place(name, type, location, address, url, phone, null, 0);
                    Log.i("PLACE", place.toString());

                    if (currentEnvelope != null) {
                        currentEnvelope = (Envelope) GeometryEngine.project(currentEnvelope, SpatialReferences.getWgs84());

                        if (location != null) {
                            final Point placePoint = new Point(location.getX(), location.getY(), SpatialReferences.getWgs84());
                            if (GeometryEngine.within(placePoint, currentEnvelope)) {
                                places.add(place);
                                cachedPlaces.put(name, place);
                            } else {
                                Log.i("GeometryEngine", "***Excluding " + place.getName() + " because it's outside the visible area of map.***");
                            }
                        }

                    } else {
                        places.add(place);
                        cachedPlaces.put(name, place);
                    }
                }

                mCallback.onLoaded(filterPlaces(places));
            } catch (final Exception e) {
                Log.e("LocationService", "Geocoding processing problem " + e.getMessage());
            }
        }
    }

    public final Envelope getResultEnvelope() {
        if (currentEnvelope == null) {
            Envelope envelope = null;
            final List<Place> places = getPlacesFromRepo();
            if (places != null && !places.isEmpty()) {
                final List<Point> points = new ArrayList<>();
                for (final Place place : places) {
                    points.add(place.getLocation());
                }
                final Multipoint multipoint = new Multipoint(points);
                envelope = GeometryEngine.buffer(multipoint, 0.0007).getExtent();
            }
            return envelope;
        } else {
            return currentEnvelope;
        }

    }
}

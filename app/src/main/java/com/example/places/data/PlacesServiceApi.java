package com.example.places.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;

import java.util.List;

public interface PlacesServiceApi {
    interface PlacesServiceCallback<List> {
        void onLoaded(List places);
    }

    interface RouteServiceCallback {
        void onRouteReturned(RouteResult result);
    }

    void getRouteFromService(Point start, Point end, Context context, RouteServiceCallback callback);
    void getPlacesFromService(@NonNull GeocodeParameters parameters, @NonNull PlacesServiceCallback callback);
    List<Place> getPlacesFromRepo();
}

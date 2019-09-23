package com.example.places.networking;

import androidx.annotation.NonNull;

import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.example.places.data.Place;

import java.util.List;

public interface PlacesServiceApi {
    interface PlacesServiceCallback<List> {
        void onLoaded(List places);
    }

    void getPlacesFromService(@NonNull GeocodeParameters parameters, @NonNull PlacesServiceCallback callback);
    List<Place> getPlacesFromRepo();
}

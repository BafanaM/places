package com.example.places.networking;

import com.example.places.data.CityDataModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NearbyPlacesApi {
    @GET("api/place/nearbysearch/json?sensor=true&key=AIzaSyDzxG-ENPmNuH4puM0Ri6-y1nVDoM-UzLI")
    Call<CityDataModel> getNearbyPlaces(@Query("type") String type, @Query("location") String location, @Query("radius") int radius);
}

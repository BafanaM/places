
package com.example.places.places;

import android.location.Location;

import com.esri.arcgisruntime.geometry.Envelope;
import com.example.mapsdemo.BasePresenter;
import com.example.mapsdemo.BaseView;
import com.example.mapsdemo.data.Place;

import java.util.List;


public interface PlacesContract {

  interface View extends BaseView<Presenter> {

    void showNearbyPlaces(List<Place> places);

    void showProgressIndicator(String message);

    void showMessage(String message);


  }

  interface Presenter extends BasePresenter {

    void setPlacesNearby(List<Place> places);

    void setLocation(Location location);

    void getPlacesNearby();

    Envelope getExtentForNearbyPlaces();

  }
}

package com.example.places.map;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.MapView;

import com.example.places.BasePresenter;
import com.example.places.BaseView;
import com.example.places.data.Place;

import java.util.List;

public interface MapContract {

    interface View extends BaseView<Presenter> {
        void showNearbyPlaces(List<Place> placeList);
        MapView getMapView();
        void centerOnPlace(Place p);
        void showMessage(String message);
        void showProgressIndicator(String message);

    }

    interface Presenter extends BasePresenter {

        void findPlacesNearby();
        void centerOnPlace(Place p);
        Place findPlaceForPoint(Point p);
        void setCurrentExtent(Envelope envelope);
    }
}

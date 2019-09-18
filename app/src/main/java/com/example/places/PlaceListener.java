package com.example.places;

import com.example.places.data.Place;

public interface PlaceListener {
  void showDetail(Place place);
  void onMapViewChange();
}

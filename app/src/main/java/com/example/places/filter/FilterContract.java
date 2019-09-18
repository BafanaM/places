package com.example.places.filter;

import com.example.places.BasePresenter;
import com.example.places.BaseView;

import java.util.List;


public interface FilterContract {
  interface View extends BaseView<Presenter> {

  }
  interface Presenter extends BasePresenter {
    List<FilterItem> getFilteredCategories();
  }

  interface FilterView{
    void onFilterDialogClose(boolean applyFilter);
  }
}

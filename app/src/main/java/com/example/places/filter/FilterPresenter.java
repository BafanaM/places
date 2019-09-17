
package com.example.places.filter;


import com.example.mapsdemo.data.CategoryKeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilterPresenter implements FilterContract.Presenter {
    private List<FilterItem> mFilters = new ArrayList<>();

    @Override
    public final List<FilterItem> getFilteredCategories() {
        return Collections.unmodifiableList(mFilters);
    }

    @Override
    public final void start() {
        final CategoryKeeper keeper = CategoryKeeper.getInstance();
        mFilters = keeper.getCategories();
    }

}

package com.example.places.data;

import com.example.places.R;
import com.example.places.filter.FilterItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryKeeper {
    private static CategoryKeeper instance = null;
    private final ArrayList<FilterItem> categories = new ArrayList<>();

    private CategoryKeeper() {
        categories.add(new FilterItem("Bar", R.drawable.ic_local_bar_grey_48dp, true, R.drawable.ic_local_bar_blue_48dp));
        categories.add(new FilterItem("Coffee Shop", R.drawable.ic_local_cafe_grey_48dp, true, R.drawable.ic_local_cafe_blue_48dp));
        categories.add(new FilterItem("Food", R.drawable.ic_local_dining_grey_48dp, true, R.drawable.ic_local_dining_blue_48dp));
        categories.add(new FilterItem("Hotel", R.drawable.ic_local_hotel_grey_48dp, true, R.drawable.ic_local_hotel_blue_48dp));
        categories.add(new FilterItem("Pizza", R.drawable.ic_local_pizza_gray_48dp, false, R.drawable.ic_local_pizza_blue_48dp));
    }

    public static CategoryKeeper getInstance() {
        if (CategoryKeeper.instance == null) {
            CategoryKeeper.instance = new CategoryKeeper();
        }
        return CategoryKeeper.instance;
    }

    public final List<FilterItem> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    public final List<String> getSelectedTypes() {
        final List<String> selectedTypes = new ArrayList<>();
        for (final FilterItem item : categories) {
            if (!item.getSelected()) {
                if (item.getTitle().equalsIgnoreCase("Food")) {
                    selectedTypes.addAll(CategoryHelper.foodTypes);
                } else {
                    selectedTypes.add(item.getTitle());
                }
            }
        }
        return selectedTypes;
    }
}

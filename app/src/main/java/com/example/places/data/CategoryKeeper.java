package com.example.places.data;

import com.example.places.filter.FilterItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryKeeper {
    private static CategoryKeeper instance = null;
    private final ArrayList<FilterItem> categories = new ArrayList<>();

    private CategoryKeeper() {

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

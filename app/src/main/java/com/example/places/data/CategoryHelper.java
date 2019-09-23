package com.example.places.data;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import com.example.places.R;

import java.util.Arrays;
import java.util.List;

public class CategoryHelper {
    static final List<String> foodTypes = Arrays.asList(
            "African Food",
            "American Food",
            "Argentinean Food",
            "Australian Food",
            "Austrian Food",
            "Bakery",
            "BBQ and Southern Food",
            "Belgian Food",
            "Bistro",
            "Brazilian Food",
            "Breakfast",
            "Brewpub",
            "British Isles Food",
            "Burgers",
            "Cajun and Creole Food",
            "Californian Food",
            "Caribbean Food",
            "Chicken Restaurant",
            "Chilean Food",
            "Chinese Food",
            "Continental Food",
            "Creperie",
            "East European Food",
            "Fast Food",
            "Filipino Food",
            "Fondue",
            "French Food",
            "Fusion Food",
            "German Food",
            "Greek Food",
            "Grill",
            "Hawaiian Food",
            "Ice Cream Shop",
            "Indian Food",
            "Indonesian Food",
            "International Food",
            "Irish Food",
            "Italian Food",
            "Japanese Food",
            "Korean Food",
            "Kosher Food",
            "Latin American Food",
            "Malaysian Food",
            "Mexican Food",
            "Middle Eastern Food",
            "Moroccan Food",
            "Other Restaurant",
            "Pastries",
            "Polish Food",
            "Portuguese Food",
            "Russian Food",
            "Sandwich Shop",
            "Scandinavian Food",
            "Seafood",
            "Snacks",
            "South American Food",
            "Southeast Asian Food",
            "Southwestern Food",
            "Spanish Food",
            "Steak House",
            "Sushi",
            "Swiss Food",
            "Tapas",
            "Thai Food",
            "Turkish Food",
            "Vegetarian Food",
            "Vietnamese Food",
            "Winery");

    private static String getCategoryForFoodType(final String foodType) {
        String category = foodType;
        if (foodTypes.contains(foodType)) {
            category = "Food";
        }
        return category;
    }

    public static Integer getResourceIdForPlacePin(final Place place) {
        final String category = CategoryHelper.getCategoryForFoodType(place.getType());
        final int pinIcon;
        switch (category) {
            case "Pizza":
                pinIcon = R.drawable.pizza_pin;
                break;
            case "Hotel":
                pinIcon = R.drawable.hotel_pin;
                break;
            case "Food":
                pinIcon = R.drawable.restaurant_pin;
                break;
            case "Bar or Pub":
                pinIcon = R.drawable.bar_pin;
                break;
            case "Coffee Shop":
                pinIcon = R.drawable.cafe_pin;
                break;
            default:
                pinIcon = R.drawable.empty_pin;
        }
        return pinIcon;
    }

    public static Drawable getDrawableForPlace(final Place place, final Activity activity) {

        final String placeType = place.getType();
        final String category = CategoryHelper.getCategoryForFoodType(placeType);
        final Drawable categoryIcon;
        switch (category) {
            case "Pizza":
                categoryIcon = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_local_pizza_black_24dp, null);
                break;
            case "Hotel":
                categoryIcon = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_hotel_black_24dp, null);
                break;
            case "Food":
                categoryIcon = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_local_dining_black_24dp, null);
                break;
            case "Bar or Pub":
                categoryIcon = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_local_bar_black_24dp, null);
                break;
            case "Coffee Shop":
                categoryIcon = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_local_cafe_black_24dp, null);
                break;
            default:
                categoryIcon = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_place_black_24dp, null);
        }
        return categoryIcon;
    }
}

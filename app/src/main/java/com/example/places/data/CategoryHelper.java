package com.example.places.data;

import android.app.Activity;
import android.graphics.drawable.Drawable;

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
                break;
            case "Hotel":
                break;
            case "Food":
                break;
            case "Bar or Pub":
                break;
            case "Coffee Shop":
                break;
            default:
        }
        return pinIcon;
    }

    public static Drawable getDrawableForPlace(final Place place, final Activity activity) {

        final String placeType = place.getType();
        final String category = CategoryHelper.getCategoryForFoodType(placeType);
        final Drawable categoryIcon;
        switch (category) {
            case "Pizza":
                break;
            case "Hotel":
                break;
            case "Food":
                break;
            case "Bar or Pub":
                break;
            case "Coffee Shop":
                break;
            default:
        }
        return categoryIcon;
    }
}

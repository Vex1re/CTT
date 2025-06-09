package space.krokodilich.ctt;

import android.content.Context;
import android.content.SharedPreferences;

public class RatingPreferences {
    private static final String PREF_NAME = "rating_preferences";
    private static final String KEY_RATING_PREFIX = "rating_";
    private final SharedPreferences preferences;

    public RatingPreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveRating(String postId, int rating) {
        preferences.edit().putInt(KEY_RATING_PREFIX + postId, rating).apply();
    }

    public int getRating(String postId) {
        return preferences.getInt(KEY_RATING_PREFIX + postId, 0);
    }

    public void clearRating(String postId) {
        preferences.edit().remove(KEY_RATING_PREFIX + postId).apply();
    }
} 
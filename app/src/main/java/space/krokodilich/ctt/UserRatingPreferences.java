package space.krokodilich.ctt;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class UserRatingPreferences {
    private static final String PREF_NAME = "user_ratings";
    private static final String KEY_PREFIX = "post_rating_";
    private final SharedPreferences preferences;

    public UserRatingPreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveRating(Long postId, int rating) {
        preferences.edit().putInt(KEY_PREFIX + postId, rating).apply();
        Log.d("UserRatingPreferences", "Saved rating " + rating + " for post " + postId);
    }

    public int getRating(Long postId) {
        return preferences.getInt(KEY_PREFIX + postId, 0);
    }

    public void clearRating(Long postId) {
        preferences.edit().remove(KEY_PREFIX + postId).apply();
        Log.d("UserRatingPreferences", "Cleared rating for post " + postId);
    }
} 
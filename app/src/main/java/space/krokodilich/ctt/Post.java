package space.krokodilich.ctt;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class Post {
    private static final String TAG = "ImageDebug";

    @SerializedName("id")
    private Long id;
    @SerializedName("name")
    private String name; // Author's full name
    @SerializedName("location")
    private String location;
    @SerializedName("time")
    private String time;
    private String placeName;
    @SerializedName("description")
    private String description;
    @SerializedName("rating")
    private int rating;
    @SerializedName("commentsCount")
    private int commentsCount;
    @SerializedName("tag")
    private String tag;
    @SerializedName("login")
    private String login; // User's login
    @SerializedName("images")
    private String images; // JSON string of image URLs

    private int userRating; // Текущая оценка пользователя (1, -1 или 0)

    public Post(Long id, String name, String location, String time,
               String description, int rating, String tag,
               int commentsCount, String placeName, String login, String images) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.time = time;
        this.description = description;
        this.rating = rating;
        this.tag = tag;
        this.commentsCount = commentsCount;
        this.placeName = placeName;
        this.login = login;
        this.images = images;
        this.userRating = 0; // По умолчанию нет оценки
    }

    public Long getId() {
        return id;
    }

    public String getAuthorName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getTime() {
        return time;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getPlaceTag() {
        return tag;
    }

    public String getDescription() {
        return description;
    }

    public int getRating() {
        return rating;
    }

    public int getCommentsCount() {
        return commentsCount;
    }
    
    public String getLogin() {
        return login;
    }

    public String getImages() {
        return images;
    }

    public List<String> getImagesList() {
        Log.d(TAG, "Raw images JSON: " + images);
        if (images == null || images.isEmpty() || images.equals("[]")) {
            Log.d(TAG, "Images JSON is null, empty, or empty array string.");
            return new ArrayList<>();
        }
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> imageUrls = gson.fromJson(images, listType);
            Log.d(TAG, "Parsed image URLs: " + imageUrls);
            return imageUrls;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing images JSON: " + images, e);
            return new ArrayList<>();
        }
    }

    public int getUserRating() {
        return userRating;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public void setImagesList(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            this.images = "[]";
        } else {
            Gson gson = new Gson();
            this.images = gson.toJson(imageUrls);
        }
    }

    public void setUserRating(int userRating) {
        this.userRating = userRating;
    }
}

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
    private String placeTag;
    @SerializedName("login")
    private String login; // User's login
    @SerializedName("images")
    private String images; // JSON string of image URLs

    @SerializedName("likes")
    private String likes; // JSON string of user reactions in format "login:status"

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
        this.placeTag = tag;
        this.commentsCount = commentsCount;
        this.placeName = placeName;
        this.login = login;
        this.images = images;
        this.likes = "[]"; // Initialize with empty array
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
        return placeTag;
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

    public String getLikes() {
        return likes;
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
            if (imageUrls == null) {
                Log.e(TAG, "Parsed imageUrls is null");
                return new ArrayList<>();
            }
            // Проверяем и корректируем URL изображений
            List<String> correctedUrls = new ArrayList<>();
            for (String url : imageUrls) {
                if (url != null && !url.isEmpty()) {
                    if (url.startsWith("http")) {
                        correctedUrls.add(url);
                    } else if (url.startsWith("/")) {
                        correctedUrls.add("https://spring-boot-production-6510.up.railway.app" + url);
                    } else {
                        correctedUrls.add("https://spring-boot-production-6510.up.railway.app/" + url);
                    }
                }
            }
            Log.d(TAG, "Corrected image URLs: " + correctedUrls);
            return correctedUrls;
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

    public void setPlaceTag(String placeTag) {
        this.placeTag = placeTag;
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
            Log.d(TAG, "Setting images list: " + imageUrls);
            Gson gson = new Gson();
            this.images = gson.toJson(imageUrls);
            Log.d(TAG, "Serialized images JSON: " + this.images);
        }
    }

    public void setUserRating(int userRating) {
        this.userRating = userRating;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public List<String> getLikesList() {
        if (likes == null || likes.isEmpty() || likes.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> result = gson.fromJson(likes, listType);
            if (result == null) return new ArrayList<>();
            // фильтруем только строки с хотя бы одним ':'
            List<String> filtered = new ArrayList<>();
            for (String s : result) {
                if (s != null && s.contains(":")) filtered.add(s);
            }
            return filtered;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing likes JSON: " + likes, e);
            return new ArrayList<>();
        }
    }

    public void setLikesList(List<String> reactions) {
        if (reactions == null || reactions.isEmpty()) {
            this.likes = "[]";
        } else {
            Gson gson = new Gson();
            this.likes = gson.toJson(reactions);
        }
    }

    public boolean hasUserLiked(String userLogin) {
        List<String> reactions = getLikesList();
        for (String reaction : reactions) {
            String[] parts = reaction.split(":");
            if (parts.length >= 2 && parts[0].equals(userLogin)) {
                return parts[1].equals("true");
            }
        }
        return false;
    }

    public boolean hasUserDisliked(String userLogin) {
        List<String> reactions = getLikesList();
        for (String reaction : reactions) {
            String[] parts = reaction.split(":");
            if (parts.length >= 2 && parts[0].equals(userLogin)) {
                return parts[1].equals("false");
            }
        }
        return false;
    }

    public void addUserReaction(String userLogin, boolean isPositive) {
        List<String> reactions = getLikesList();
        // Удаляем предыдущую реакцию пользователя, если она есть
        reactions.removeIf(reaction -> reaction.startsWith(userLogin + ":"));
        // Добавляем новую реакцию с текущим временем
        String timestamp = String.valueOf(System.currentTimeMillis());
        reactions.add(userLogin + ":" + isPositive + ":" + timestamp);
        setLikesList(reactions);
    }

    public void removeUserReaction(String userLogin) {
        List<String> reactions = getLikesList();
        reactions.removeIf(reaction -> reaction.startsWith(userLogin + ":"));
        setLikesList(reactions);
    }

    public boolean hasUserReaction(String userLogin) {
        List<String> reactions = getLikesList();
        return reactions.stream().anyMatch(reaction -> reaction.startsWith(userLogin + ":"));
    }

    public boolean isUserReactionPositive(String userLogin) {
        List<String> reactions = getLikesList();
        return reactions.stream()
                .filter(reaction -> reaction.startsWith(userLogin + ":"))
                .findFirst()
                .map(reaction -> reaction.split(":").length >= 2 && reaction.split(":")[1].equals("true"))
                .orElse(false);
    }

    // Новый метод для получения времени реакции пользователя
    public long getUserReactionTimestamp(String userLogin) {
        List<String> reactions = getLikesList();
        for (String reaction : reactions) {
            String[] parts = reaction.split(":");
            if (parts.length >= 3 && parts[0].equals(userLogin)) {
                try {
                    return Long.parseLong(parts[2]);
                } catch (NumberFormatException e) {
                    return System.currentTimeMillis(); // Fallback если время не парсится
                }
            }
        }
        return System.currentTimeMillis(); // Fallback если реакция не найдена
    }
}

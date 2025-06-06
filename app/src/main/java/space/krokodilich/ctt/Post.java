package space.krokodilich.ctt;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Post {
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
    private List<String> images; // List of image URLs

    public Post(Long id, String name, String location, String time,
               String description, int rating, String tag,
               int commentsCount, String placeName, String login, List<String> images) {
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

    public List<String> getImages() {
        return images;
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

    public void setImages(List<String> images) {
        this.images = images;
    }
}

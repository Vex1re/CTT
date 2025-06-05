package space.krokodilich.ctt;

import com.google.gson.annotations.SerializedName;

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

    public Post(Long id, String name, String location, String time,
               String description, int rating, String tag,
               int commentsCount, String placeName, String login) {
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
}

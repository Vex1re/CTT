package space.krokodilich.ctt;

public class Post {
    private String authorName;
    private String authorAvatar;
    private String location;
    private String time;
    private String imageUrl;
    private String placeName;
    private String placeTag;
    private String description;
    private int rating;
    private int commentsCount;

    public Post(String authorName, String authorAvatar, String location, String time,
               String imageUrl, String placeName, String placeTag, String description,
               int rating, int commentsCount) {
        this.authorName = authorName;
        this.authorAvatar = authorAvatar;
        this.location = location;
        this.time = time;
        this.imageUrl = imageUrl;
        this.placeName = placeName;
        this.placeTag = placeTag;
        this.description = description;
        this.rating = rating;
        this.commentsCount = commentsCount;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public String getLocation() {
        return location;
    }

    public String getTime() {
        return time;
    }

    public String getImageUrl() {
        return imageUrl;
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
}

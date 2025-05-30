package space.krokodilich.ctt;

public class Post {
    public Post(String authorName, String location, String postTime, String placeName, String placeTag, String placeDescription, int rating) {
        this.authorName = authorName;
        this.location = location;
        this.postTime = postTime;
        this.placeName = placeName;
        this.placeTag = placeTag;
        this.placeDescription = placeDescription;
        this.rating = rating;
    }

    String authorName;
    String location;
    String postTime;
    String placeName;
    String placeTag;
    String placeDescription;
    int rating;

}

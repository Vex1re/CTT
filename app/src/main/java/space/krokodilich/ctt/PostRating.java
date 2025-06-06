package space.krokodilich.ctt;

import com.google.gson.annotations.SerializedName;

public class PostRating {
    @SerializedName("postId")
    private Long postId;
    
    @SerializedName("userLogin")
    private String userLogin;
    
    @SerializedName("rating")
    private int rating; // 1 для лайка, -1 для дизлайка, 0 для отмены оценки

    public PostRating(Long postId, String userLogin, int rating) {
        this.postId = postId;
        this.userLogin = userLogin;
        this.rating = rating;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
} 
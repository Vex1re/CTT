package space.krokodilich.ctt;

public class PostRating {
    private Long postId;
    private String userLogin;
    private Boolean isPositive;

    public PostRating(Long postId, String userLogin, Boolean isPositive) {
        this.postId = postId;
        this.userLogin = userLogin;
        this.isPositive = isPositive;
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

    public Boolean getIsPositive() {
        return isPositive;
    }

    public void setIsPositive(Boolean isPositive) {
        this.isPositive = isPositive;
    }
} 
package space.krokodilich.ctt;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("login")
    private String username;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("surname")
    private String surname;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("city")
    private String city;
    
    @SerializedName("password")
    private String password;

    @SerializedName("posts")
    private int posts;

    @SerializedName("rating")
    private int rating;

    @SerializedName("status")
    private String status;

    public User(String username, String name, String surname, String email, String city, String password) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.city = city;
        this.password = password;
        this.posts = 0;
        this.rating = 0;
        this.status = "Новичок";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPosts() {
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", city='" + city + '\'' +
                ", posts=" + posts +
                ", rating=" + rating +
                ", status='" + status + '\'' +
                '}';
    }

    public void register(){

    }
}

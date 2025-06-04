package space.krokodilich.ctt;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private String id;
    
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
    
    @SerializedName("login")
    private String login;

    public User(String id, String name, String surname, String email, String city, String password, String login) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.city = city;
        this.password = password;
        this.login = login;
    }

    // Геттеры
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
    public String getCity() { return city; }
    public String getPassword() { return password; }
    public String getLogin() { return login; }

    // Сеттеры
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setEmail(String email) { this.email = email; }
    public void setCity(String city) { this.city = city; }
    public void setPassword(String password) { this.password = password; }
    public void setLogin(String login) { this.login = login; }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", city='" + city + '\'' +
                ", login='" + login + '\'' +
                '}';
    }

    public void register(){

    }
}

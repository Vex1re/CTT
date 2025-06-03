package space.krokodilich.ctt;

public class User {
    public String id, name, surname, email, city, password, login;

    public User() {
    }

    public User(String id, String name, String surname, String email, String city, String password, String login) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.city = city;
        this.password = password;
        this.login = login;
    }
}

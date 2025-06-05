package space.krokodilich.ctt;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

public interface ChatService {
    @POST("/users/register")
    Call<User> register(@Body User user);

    @POST("/users/login")
    Call<User> login(@Body User user);

    @GET("/users/{id}")
    Call<User> getUser(@Path("id") String userId);

    @GET("/users/all")
    Call<List<User>> getAllUsers();
}

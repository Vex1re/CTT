package space.krokodilich.ctt;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

public interface ChatService {
    @POST("/api/user/register")
    Call<Void> register(@Body User user);

    @GET("/api/user/{id}")
    Call<User> getUser(@Path("id") String userId);

    @GET("/api/user/all")
    Call<List<User>> getAllUsers();
}

package space.krokodilich.ctt;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET("posts/all")
    Call<List<Post>> getPosts();

    @GET("users/{id}")
    Call<User> getUser(@Path("id") String id);

    @POST("users")
    Call<User> createUser(@Body User user);

    @POST("users/login")
    Call<User> loginUser(@Body User user);

    @POST("posts/all")
    Call<Post> createPost(@Body Post post);
} 
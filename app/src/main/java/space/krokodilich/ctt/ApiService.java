package space.krokodilich.ctt;

import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.PUT;

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

    @GET("posts/{id}")
    Call<Post> getPost(@Path("id") Long id);

    @Multipart
    @POST("posts/{postId}/images")
    Call<Post> uploadImages(
        @Path("postId") Long postId,
        @Part List<MultipartBody.Part> files
    );

    @DELETE("posts/{id}/images")
    Call<Void> deleteImage(@Path("id") Long postId, @Query("imageUrl") String imageUrl);

    @PUT("posts/{postId}/rating")
    Call<Post> updatePostRating(@Path("postId") Long postId, @Body PostRating postRating);

    @POST("posts/{id}/like")
    Call<Post> addLike(@Path("id") Long postId, @Body Map<String, String> userData);

    @DELETE("posts/{id}/like")
    Call<Post> removeLike(@Path("id") Long postId, @Body Map<String, String> userData);

    @GET("posts/{id}/likes/check")
    Call<Post> checkLike(@Path("id") Long postId, @Query("userLogin") String userLogin);

    @GET("posts/user/{userLogin}/reactions")
    Call<Map<Long, Boolean>> getUserReactions(@Path("userLogin") String userLogin);

    @Multipart
    @POST("users/{userId}/avatar")
    Call<String> uploadAvatar(
        @Path("userId") Long userId,
        @Part MultipartBody.Part file
    );

    @PUT("users/{userId}")
    Call<User> updateUser(@Path("userId") Long userId, @Body User user);

    @DELETE("posts/{id}")
    Call<Void> deletePost(@Path("id") Long postId);
} 
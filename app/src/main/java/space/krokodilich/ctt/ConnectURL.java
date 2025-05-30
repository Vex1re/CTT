package space.krokodilich.ctt;

import android.annotation.SuppressLint;
import android.graphics.ColorSpace;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConnectURL {
    private Post post;

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("postgresql://postgres:ECAJmrFyEkPHBzxMsjwBiNLXjxJPwhsR@hopper.proxy.rlwy.net:30334/railway")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    RestApi service = retrofit.create(RestApi.class);
    Call<Post> call = service.predict(post.authorName, post.location, "ru");

}
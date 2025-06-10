package space.krokodilich.ctt;

import android.util.Log;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static final String BASE_URL = "https://spring-boot-production-6510.up.railway.app/";
    private static Retrofit retrofit = null;
    private static RetrofitClient instance = null;

    public static RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApiService() {
        return getClient().create(ApiService.class);
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            Log.d(TAG, "Initializing Retrofit client with base URL: " + BASE_URL);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            try {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                Log.d(TAG, "Retrofit client initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Retrofit client: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return retrofit;
    }

    public static void resetClient() {
        retrofit = null;
    }
}

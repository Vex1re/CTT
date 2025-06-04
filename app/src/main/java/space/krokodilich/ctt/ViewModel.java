package space.krokodilich.ctt;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class ViewModel {
    private static final String TAG = "ViewModel";
    private ChatService chatService;
    private OnNetworkCallback callback;

    public interface OnNetworkCallback {
        void onSuccess();
        void onError(String error);
    }

    public void setCallback(OnNetworkCallback callback) {
        this.callback = callback;
    }

    public void connect() {
        try {
            Log.d(TAG, "Connecting to server...");
            this.chatService = RetrofitClient.getClient().create(ChatService.class);
            Log.d(TAG, "ChatService created successfully");
            if (callback != null) {
                callback.onSuccess();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to server: " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                callback.onError("Ошибка подключения к серверу: " + e.getMessage());
            }
        }
    }

    public void register(User user) {
        if (chatService == null) {
            Log.e(TAG, "ChatService is null");
            if (callback != null) {
                callback.onError("Сервис не инициализирован");
            }
            return;
        }

        Log.d(TAG, "Attempting to register user: " + user.toString());
        chatService.register(user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "Registration response received. Code: " + response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Registration successful");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Registration failed. Code: " + response.code() + ", Error: " + errorBody);
                    if (callback != null) {
                        callback.onError("Ошибка регистрации: " + response.code() + " " + errorBody);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error during registration", t);
                if (callback != null) {
                    callback.onError("Ошибка сети: " + t.getMessage());
                }
            }
        });
    }

    public void getUser(String userId, OnUserCallback callback) {
        if (chatService == null) {
            Log.e(TAG, "ChatService is null");
            callback.onError("Сервис не инициализирован");
            return;
        }

        Log.d(TAG, "Attempting to get user with ID: " + userId);
        chatService.getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, "Get user response received. Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "User retrieved successfully: " + response.body().toString());
                    callback.onSuccess(response.body());
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Failed to get user. Code: " + response.code() + ", Error: " + errorBody);
                    callback.onError("Ошибка получения данных пользователя: " + response.code() + " " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Network error while getting user", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    public interface OnUserCallback {
        void onSuccess(User user);
        void onError(String error);
    }
}

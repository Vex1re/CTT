package space.krokodilich.ctt;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import java.util.ArrayList;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.logging.HttpLoggingInterceptor;

public class ViewModel {
    private static final String TAG = "ViewModel";
    private ChatService chatService;
    private OnNetworkCallback callback;
    private Context context;
    private User currentUser; // Поле для хранения текущего пользователя
    private List<Post> posts;
    private static final String BASE_URL = "https://spring-boot-production-6510.up.railway.app/";
    private ApiService apiService;

    public void initialize(Context context) {
        this.context = context;
        chatService = RetrofitClient.getClient().create(ChatService.class);
        apiService = RetrofitClient.getClient().create(ApiService.class);
        Log.d(TAG, "ViewModel initialized");
        
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        apiService = retrofit.create(ApiService.class);
    }

    public void setCallback(OnNetworkCallback callback) {
        this.callback = callback;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public interface OnNetworkCallback {
        void onSuccess();
        void onError(String error);
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
                callback.onError("Ошибка инициализации сервиса");
            }
            return;
        }

        Log.d(TAG, "Attempting to register user with username: " + user.getUsername());
        Log.d(TAG, "Attempting to register user with email: " + user.getEmail());
        Log.d(TAG, "Attempting to register user with name: " + user.getName());
        Log.d(TAG, "Attempting to register user with surname: " + user.getSurname());
        Log.d(TAG, "Attempting to register user with city: " + user.getCity());
        // Не логируем пароль из соображений безопасности

        chatService.register(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User registeredUser = response.body();
                    Log.d(TAG, "User registered successfully: " + registeredUser.getUsername());
                    currentUser = registeredUser; // Сохраняем зарегистрированного пользователя
                    saveUserId(registeredUser.getId()); // Сохраняем ID пользователя при успешной регистрации
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
            public void onFailure(Call<User> call, Throwable t) {
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

    public void login(User user) {
        if (chatService == null) {
            Log.e(TAG, "ChatService is null");
            if (callback != null) {
                callback.onError("Ошибка инициализации сервиса");
            }
            return;
        }

        Log.d(TAG, "Attempting to login user: " + user.toString());
        chatService.login(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, "Login response received. Code: " + response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Login successful");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                    currentUser = response.body(); // Сохраняем найденного пользователя
                    saveUserId(response.body().getId()); // Сохраняем ID пользователя при успешном входе
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Login failed. Code: " + response.code() + ", Error: " + errorBody);
                    if (callback != null) {
                        callback.onError("Ошибка входа: " + response.code() + " " + errorBody);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Network error during login", t);
                if (callback != null) {
                    callback.onError("Ошибка сети: " + t.getMessage());
                }
            }
        });
    }

    public void checkUserExists(String loginOrEmail, String email, boolean isRegistration) {
        if (chatService == null) {
            Log.e(TAG, "ChatService is null");
            if (callback != null) {
                callback.onError("Ошибка инициализации сервиса");
            }
            return;
        }

        Log.d(TAG, "Checking if user exists with login/email: " + loginOrEmail);
        chatService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                Log.d(TAG, "Get all users response received. Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    
                    if (isRegistration) {
                        // При регистрации проверяем, что логин и email уникальны
                        for (User user : users) {
                            String userLogin = user.getUsername();
                            String userEmail = user.getEmail();
                            
                            if (loginOrEmail != null && userLogin != null && loginOrEmail.equals(userLogin)) {
                                if (callback != null) {
                                    callback.onError("Пользователь с таким логином уже существует");
                                }
                                return;
                            }
                            if (email != null && userEmail != null && email.equals(userEmail)) {
                                if (callback != null) {
                                    callback.onError("Пользователь с таким email уже существует");
                                }
                                return;
                            }
                        }
                        // Если логин и email уникальны, передаем данные в RegisterFragment для создания пользователя
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else {
                        // При входе проверяем существование пользователя
                        boolean userExists = false;
                        for (User user : users) {
                            String userLogin = user.getUsername();
                            String userEmail = user.getEmail();
                            String userPassword = user.getPassword();
                            
                            if (loginOrEmail != null && email != null && 
                                ((userLogin != null && loginOrEmail.equals(userLogin)) || 
                                 (userEmail != null && loginOrEmail.equals(userEmail))) && 
                                userPassword != null && email.equals(userPassword)) {
                                userExists = true;
                                currentUser = user; // Сохраняем найденного пользователя
                                saveUserId(user.getId()); // Сохраняем ID пользователя при успешном входе
                                break;
                            }
                        }
                        
                        if (userExists) {
                            Log.d(TAG, "User found");
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        } else {
                            Log.e(TAG, "User not found");
                            if (callback != null) {
                                callback.onError("Неверный логин/email или пароль");
                            }
                        }
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
                    Log.e(TAG, "Failed to get users. Code: " + response.code() + ", Error: " + errorBody);
                    if (callback != null) {
                        callback.onError("Ошибка проверки пользователя: " + response.code() + " " + errorBody);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e(TAG, "Network error while checking user", t);
                if (callback != null) {
                    callback.onError("Ошибка сети: " + t.getMessage());
                }
            }
        });
    }

    public interface OnUserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    private void saveUserId(Long userId) {
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (userId != null) {
                editor.putLong("user_id", userId);
            } else {
                editor.remove("user_id");
            }
            editor.apply();
        }
    }

    public void clearUserId() {
        saveUserId(null); // Передаем null для удаления ID
    }

    public void fetchUserById(Long userId) {
        if (chatService == null) {
            Log.e(TAG, "ChatService is null");
            if (callback != null) {
                callback.onError("Ошибка инициализации сервиса");
            }
            return;
        }

        if (userId == null) {
            Log.e(TAG, "User ID is null for fetching.");
            if (callback != null) {
                 callback.onError("Ошибка: ID пользователя не найден.");
            }
            return;
        }

        Log.d(TAG, "Attempting to fetch user with ID: " + userId);
        chatService.getUser(String.valueOf(userId)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    Log.d(TAG, "User fetched successfully: " + currentUser.getUsername());
                    if (callback != null) {
                        callback.onSuccess(); // Указываем успешную загрузку пользователя
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
                    Log.e(TAG, "Failed to fetch user by ID. Code: " + response.code() + ", Error: " + errorBody);
                    if (callback != null) {
                         callback.onError("Ошибка загрузки пользователя: " + response.code() + " " + errorBody);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Network error during user fetch", t);
                 if (callback != null) {
                    callback.onError("Ошибка сети при загрузке пользователя: " + t.getMessage());
                }
            }
        });
    }

    public Long getSavedUserId() {
         if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            long userId = sharedPreferences.getLong("user_id", -1L); // -1L как значение по умолчанию, если ID нет
            return userId != -1L ? userId : null;
        }
        return null;
    }

    public void getPosts(OnNetworkCallback callback) {
        this.callback = callback;
        apiService.getPosts().enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    posts = response.body();
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
                    Log.e(TAG, "Failed to get posts. Code: " + response.code() + ", Error: " + errorBody);
                    if (callback != null) {
                        callback.onError("Ошибка при загрузке постов: " + response.code() + " " + errorBody);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.e(TAG, "Network error while getting posts", t);
                if (callback != null) {
                    callback.onError("Ошибка сети при загрузке постов: " + t.getMessage());
                }
            }
        });
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void createPost(Post post, OnNetworkCallback callback) {
        this.callback = callback;
        // Логируем объект Post перед отправкой
        Log.d(TAG, "Отправка поста: " + post.toString());

        apiService.createPost(post).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
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
                    Log.e(TAG, "Failed to create post. Code: " + response.code() + ", Error: " + errorBody);
                    if (callback != null) {
                        callback.onError("Ошибка при создании поста: " + response.code() + " " + errorBody);
                    }
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.e(TAG, "Network error while creating post", t);
                if (callback != null) {
                    callback.onError("Ошибка сети при создании поста: " + t.getMessage());
                }
            }
        });
    }
}

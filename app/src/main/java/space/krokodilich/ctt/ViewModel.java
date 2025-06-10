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
import retrofit2.converter.scalars.ScalarsConverterFactory;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.MultipartBody;
import java.io.IOException;
import com.google.gson.Gson;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import android.net.Uri;
import java.io.File;
import android.database.Cursor;
import android.provider.MediaStore;
import java.util.HashMap;
import java.util.Map;

public class ViewModel {
    private static final String TAG = "ViewModel";
    private ChatService chatService;
    private OnNetworkCallback callback;
    private Context context;
    private User currentUser; // Поле для хранения текущего пользователя
    private List<Post> posts;
    private static final String BASE_URL = "https://spring-boot-production-6510.up.railway.app/";
    private ApiService apiService;
    private Map<String, String> userAvatars = new HashMap<>();
    private Map<String, User> userCache = new HashMap<>(); // Кэш пользователей для уведомлений

    public void initialize(Context context) {
        this.context = context;
        chatService = RetrofitClient.getClient().create(ChatService.class);
        apiService = RetrofitClient.getClient().create(ApiService.class);
        Log.d(TAG, "ViewModel initialized");
    }

    public void setCallback(OnNetworkCallback callback) {
        this.callback = callback;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void updateCurrentUser(User user) {
        this.currentUser = user;
        // Сохраняем ID пользователя, если он изменился
        if (user != null && user.getId() != null) {
            saveUserId(user.getId());
        }
        // Обновляем кэш аватарки пользователя
        if (user != null && user.getUsername() != null && user.getAvatarUrl() != null) {
            updateUserAvatarCache(user.getUsername(), user.getAvatarUrl());
        }
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

        Log.d(TAG, "=== STARTING USER REGISTRATION ===");
        Log.d(TAG, "User data to register:");
        Log.d(TAG, "  - Username: " + user.getUsername());
        Log.d(TAG, "  - Email: " + user.getEmail());
        Log.d(TAG, "  - Name: " + user.getName());
        Log.d(TAG, "  - Surname: " + user.getSurname());
        Log.d(TAG, "  - City: " + user.getCity());
        Log.d(TAG, "  - Password length: " + (user.getPassword() != null ? user.getPassword().length() : "null"));
        Log.d(TAG, "  - ID: " + user.getId());
        Log.d(TAG, "  - Posts: " + user.getPosts());
        Log.d(TAG, "  - Rating: " + user.getRating());
        Log.d(TAG, "  - Status: " + user.getStatus());
        Log.d(TAG, "  - Avatar: " + user.getAvatar());

        chatService.register(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, "=== REGISTRATION RESPONSE RECEIVED ===");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response successful: " + response.isSuccessful());
                Log.d(TAG, "Response body is null: " + (response.body() == null));
                
                if (response.isSuccessful() && response.body() != null) {
                    User registeredUser = response.body();
                    Log.d(TAG, "=== USER REGISTERED SUCCESSFULLY ===");
                    Log.d(TAG, "Registered user data:");
                    Log.d(TAG, "  - ID: " + registeredUser.getId());
                    Log.d(TAG, "  - Username: " + registeredUser.getUsername());
                    Log.d(TAG, "  - Email: " + registeredUser.getEmail());
                    Log.d(TAG, "  - Name: " + registeredUser.getName());
                    Log.d(TAG, "  - Surname: " + registeredUser.getSurname());
                    Log.d(TAG, "  - City: " + registeredUser.getCity());
                    
                    currentUser = registeredUser; // Сохраняем зарегистрированного пользователя
                    saveUserId(registeredUser.getId()); // Сохраняем ID пользователя при успешной регистрации
                    
                    if (callback != null) {
                        Log.d(TAG, "Calling callback.onSuccess()");
                        callback.onSuccess();
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "=== REGISTRATION FAILED ===");
                    Log.e(TAG, "Failed registration. Code: " + response.code() + ", Error: " + errorBody);
                    if (callback != null) {
                        callback.onError("Ошибка регистрации: " + response.code() + " " + errorBody);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "=== REGISTRATION NETWORK ERROR ===");
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
                        
                        // Если логин и email уникальны, создаем нового пользователя
                        Log.d(TAG, "Login and email are unique, creating new user");
                        createNewUser(loginOrEmail, email);
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
                    Log.e(TAG, "Failed to get all users, response code: " + response.code());
                    callback.onError("Failed to get all users");
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
    
    private void createNewUser(String login, String email) {
        Log.d(TAG, "=== CREATING NEW USER ===");
        Log.d(TAG, "Login: " + login);
        Log.d(TAG, "Email: " + email);
        
        // Получаем данные из RegisterFragment через callback
        if (callback instanceof RegisterFragment) {
            RegisterFragment registerFragment = (RegisterFragment) callback;
            User newUser = registerFragment.getUserData();
            
            if (newUser != null) {
                Log.d(TAG, "=== USER DATA RETRIEVED FROM REGISTER FRAGMENT ===");
                Log.d(TAG, "User data:");
                Log.d(TAG, "  - Username: " + newUser.getUsername());
                Log.d(TAG, "  - Email: " + newUser.getEmail());
                Log.d(TAG, "  - Name: " + newUser.getName());
                Log.d(TAG, "  - Surname: " + newUser.getSurname());
                Log.d(TAG, "  - City: " + newUser.getCity());
                Log.d(TAG, "  - Password length: " + (newUser.getPassword() != null ? newUser.getPassword().length() : "null"));
                
                Log.d(TAG, "Calling register() method");
                register(newUser);
            } else {
                Log.e(TAG, "=== FAILED TO GET USER DATA ===");
                Log.e(TAG, "getUserData() returned null");
                if (callback != null) {
                    callback.onError("Ошибка получения данных пользователя");
                }
            }
        } else {
            Log.e(TAG, "=== CALLBACK TYPE ERROR ===");
            Log.e(TAG, "Callback is not RegisterFragment, actual type: " + (callback != null ? callback.getClass().getSimpleName() : "null"));
            if (callback != null) {
                callback.onError("Ошибка инициализации регистрации");
            }
        }
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
        currentUser = null; // Очищаем текущего пользователя
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
        apiService.getPosts().enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    posts = response.body();
                    Log.d("ViewModel", "Received posts: " + posts.size());
                    for (Post post : posts) {
                        Log.d("ViewModel", "Post " + post.getId() + " images: " + post.getImagesList());
                    }
                    callback.onSuccess();
                } else {
                    String errorMessage = "Ошибка при получении постов";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += ": " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("ViewModel", "Error reading error body", e);
                    }
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.e("ViewModel", "Network error while getting posts", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void loadUserReactions() {
        if (currentUser == null || currentUser.getUsername() == null) {
            Log.d(TAG, "Cannot load user reactions: currentUser or username is null");
            return;
        }

        Log.d(TAG, "Loading reactions for user: " + currentUser.getUsername());
        apiService.getUserReactions(currentUser.getUsername()).enqueue(new Callback<Map<Long, Boolean>>() {
            @Override
            public void onResponse(Call<Map<Long, Boolean>> call, Response<Map<Long, Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && posts != null) {
                    Map<Long, Boolean> reactions = response.body();
                    Log.d(TAG, "Received reactions: " + reactions);
                    
                    // Обновляем посты с реакциями пользователя
                    for (Post post : posts) {
                        Boolean isPositive = reactions.get(post.getId());
                        if (isPositive != null) {
                            // Устанавливаем рейтинг пользователя (1 для лайка, -1 для дизлайка)
                            post.setUserRating(isPositive ? 1 : -1);
                            
                            // Добавляем реакцию в список лайков поста
                            post.addUserReaction(currentUser.getUsername(), isPositive);
                            
                            Log.d(TAG, "Updated post " + post.getId() + " with user rating: " + post.getUserRating());
                        } else {
                            // Если реакции нет, сбрасываем рейтинг пользователя
                            post.setUserRating(0);
                        }
                    }
                    
                    // Уведомляем об успешной загрузке
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    String errorMessage = "Unknown error";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Failed to load user reactions: " + errorMessage);
                    if (callback != null) {
                        callback.onError("Failed to load user reactions");
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<Long, Boolean>> call, Throwable t) {
                Log.e(TAG, "Network error while loading user reactions", t);
                if (callback != null) {
                    callback.onError("Network error: " + t.getMessage());
                }
            }
        });
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void createPost(Post post, OnNetworkCallback callback) {
        apiService.createPost(post).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Post createdPost = response.body();
                    Log.d("ViewModel", "Created post with ID: " + createdPost.getId());
                    
                    // Добавляем пост в начало списка
                    if (posts == null) {
                        posts = new ArrayList<>();
                    }
                    posts.add(0, createdPost);
                    
                    // Если есть изображения, загружаем их
                    if (post.getImagesList() != null && !post.getImagesList().isEmpty()) {
                        List<Uri> imageUris = new ArrayList<>();
                        for (String imagePath : post.getImagesList()) {
                            imageUris.add(Uri.parse(imagePath));
                        }
                        uploadImages(createdPost.getId(), imageUris, callback);
                    } else {
                        callback.onSuccess();
                    }
                } else {
                    String errorMessage = "Ошибка при создании поста";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += ": " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("ViewModel", "Error reading error body", e);
                    }
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.e("ViewModel", "Network error while creating post", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    public void uploadImages(Long postId, List<Uri> imageUris, OnNetworkCallback callback) {
        if (imageUris == null || imageUris.isEmpty()) {
            Log.d("ViewModel", "No images to upload");
            callback.onSuccess();
            return;
        }

        Log.d("ViewModel", "Starting upload of " + imageUris.size() + " images for post " + postId);
        
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        long totalSize = 0;
        final long MAX_REQUEST_SIZE = 8 * 1024 * 1024; // 8MB limit to stay under 10MB server limit
        final int MAX_IMAGES_PER_BATCH = 3; // Maximum images per batch
        
        for (int i = 0; i < imageUris.size(); i++) {
            Uri imageUri = imageUris.get(i);
            try {
                Log.d("ViewModel", "Processing image " + (i + 1) + "/" + imageUris.size() + ": " + imageUri);
                
                String realPath = getRealPathFromUri(imageUri);
                if (realPath != null) {
                    File file = new File(realPath);
                    if (!file.exists()) {
                        Log.e("ViewModel", "File does not exist: " + realPath);
                        continue;
                    }
                    
                    // Compress image if it's too large
                    File compressedFile = compressImageIfNeeded(file);
                    long fileSize = compressedFile.length();
                    Log.d("ViewModel", "File size: " + fileSize + " bytes (" + (fileSize / 1024 / 1024) + " MB)");
                    
                    if (fileSize > 10 * 1024 * 1024) {
                        Log.e("ViewModel", "File too large even after compression: " + realPath + ", size: " + fileSize);
                        continue;
                    }
                    
                    Log.d("ViewModel", "Preparing file for upload: " + compressedFile.getAbsolutePath() + ", size: " + fileSize);
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), compressedFile);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("files", compressedFile.getName(), requestFile);
                    imageParts.add(body);
                    totalSize += fileSize;
                    Log.d("ViewModel", "Successfully added file to upload list: " + compressedFile.getName());
                    
                    // Check if we need to start a new batch
                    if (imageParts.size() >= MAX_IMAGES_PER_BATCH || totalSize > MAX_REQUEST_SIZE) {
                        Log.d("ViewModel", "Batch limit reached. Uploading batch of " + imageParts.size() + " images (total size: " + (totalSize / 1024 / 1024) + " MB)");
                        uploadImageBatch(postId, new ArrayList<>(imageParts), callback);
                        imageParts.clear();
                        totalSize = 0;
                    }
                } else {
                    Log.e("ViewModel", "Could not get real path for URI: " + imageUri);
                }
            } catch (Exception e) {
                Log.e("ViewModel", "Error preparing image for upload: " + imageUri, e);
            }
        }

        // Upload remaining images
        if (!imageParts.isEmpty()) {
            Log.d("ViewModel", "Uploading final batch of " + imageParts.size() + " images (total size: " + (totalSize / 1024 / 1024) + " MB)");
            uploadImageBatch(postId, imageParts, callback);
        } else if (imageUris.size() > 0) {
            Log.d("ViewModel", "No valid images to upload");
            callback.onError("Не удалось подготовить изображения для загрузки");
        }
    }
    
    private void uploadImageBatch(Long postId, List<MultipartBody.Part> imageParts, OnNetworkCallback callback) {
        Log.d("ViewModel", "Uploading batch of " + imageParts.size() + " images for post " + postId);
        apiService.uploadImages(postId, imageParts).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                Log.d("ViewModel", "Upload response received. Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Post updatedPost = response.body();
                    Log.d("ViewModel", "Images uploaded successfully. Updated post images: " + updatedPost.getImagesList());
                    updatePostInList(updatedPost);
                    callback.onSuccess();
                } else {
                    String errorMessage = "Ошибка загрузки изображений: код " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMessage += ", ответ: " + errorBody;
                            Log.e("ViewModel", "Server error response: " + errorBody);
                        }
                    } catch (IOException e) {
                        Log.e("ViewModel", "Error reading error body", e);
                    }
                    
                    if (response.code() == 413) {
                        errorMessage = "Размер изображений слишком большой. Попробуйте загрузить меньше изображений или изображения меньшего размера.";
                    }
                    
                    Log.e("ViewModel", errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.e("ViewModel", "Network error while uploading images", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }
    
    private File compressImageIfNeeded(File originalFile) throws IOException {
        long originalSize = originalFile.length();
        long maxSize = 2 * 1024 * 1024; // 2MB target size
        
        if (originalSize <= maxSize) {
            return originalFile;
        }
        
        Log.d("ViewModel", "Compressing image from " + (originalSize / 1024 / 1024) + " MB to target 2MB");
        
        // Create compressed file
        File compressedFile = File.createTempFile("compressed_", ".jpg", context.getCacheDir());
        
        try {
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(originalFile.getAbsolutePath());
            if (bitmap == null) {
                Log.e("ViewModel", "Could not decode bitmap from file: " + originalFile.getAbsolutePath());
                return originalFile;
            }
            
            // Calculate compression quality
            int quality = 90;
            long currentSize = originalSize;
            
            while (currentSize > maxSize && quality > 10) {
                quality -= 10;
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, baos);
                currentSize = baos.size();
                Log.d("ViewModel", "Compression attempt: quality=" + quality + ", size=" + (currentSize / 1024 / 1024) + " MB");
            }
            
            // Write compressed image to file
            java.io.FileOutputStream fos = new java.io.FileOutputStream(compressedFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, fos);
            fos.close();
            
            Log.d("ViewModel", "Image compressed successfully: " + (compressedFile.length() / 1024 / 1024) + " MB");
            return compressedFile;
            
        } catch (Exception e) {
            Log.e("ViewModel", "Error compressing image", e);
            return originalFile;
        }
    }

    private String getRealPathFromUri(Uri uri) {
        try {
            if (uri == null) {
                Log.e("ViewModel", "URI is null");
                return null;
            }

            Log.d("ViewModel", "Processing URI: " + uri.toString() + ", scheme: " + uri.getScheme());

            // Для content:// URI
            if (uri.getScheme().equals("content")) {
                try {
                    // Создаем временный файл
                    File tempFile = File.createTempFile("image_", ".jpg", context.getCacheDir());
                    Log.d("ViewModel", "Created temp file: " + tempFile.getAbsolutePath());
                    
                    // Копируем данные из URI во временный файл
                    java.io.InputStream inputStream = context.getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long totalBytes = 0;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            totalBytes += bytesRead;
                        }
                        outputStream.close();
                        inputStream.close();
                        
                        Log.d("ViewModel", "Successfully copied " + totalBytes + " bytes to temp file");
                        return tempFile.getAbsolutePath();
                    } else {
                        Log.e("ViewModel", "Could not open input stream for URI: " + uri);
                    }
                } catch (Exception e) {
                    Log.e("ViewModel", "Error copying content to temp file", e);
                }
            }
            // Для file:// URI
            else if (uri.getScheme().equals("file")) {
                String path = uri.getPath();
                Log.d("ViewModel", "File URI path: " + path);
                return path;
            }
            // Для других типов URI (например, https://)
            else {
                Log.d("ViewModel", "Unsupported URI scheme: " + uri.getScheme() + ", trying to handle as file path");
                String path = uri.getPath();
                if (path != null) {
                    File file = new File(path);
                    if (file.exists()) {
                        Log.d("ViewModel", "File exists at path: " + path);
                        return path;
                    }
                }
            }
            
            Log.e("ViewModel", "Could not process URI: " + uri);
            return null;
        } catch (Exception e) {
            Log.e("ViewModel", "Error getting real path from URI", e);
            return null;
        }
    }

    private void updatePostInList(Post updatedPost) {
        if (posts != null && updatedPost != null) {
            Log.d(TAG, "Updating post in list. Post ID: " + updatedPost.getId() + ", Images: " + updatedPost.getImagesList());
            
            // Получаем текущего пользователя
            String currentUsername = currentUser != null ? currentUser.getUsername() : null;
            
            // Проверяем, есть ли реакция пользователя в обновленном посте
            List<String> likes = updatedPost.getLikesList();
            int userRating = 0;
            
            if (currentUsername != null && likes != null) {
                for (String like : likes) {
                    if (like.startsWith(currentUsername + ":")) {
                        userRating = like.endsWith(":true") ? 1 : -1;
                        break;
                    }
                }
            }
            
            // Устанавливаем рейтинг пользователя
            updatedPost.setUserRating(userRating);
            
            // Обновляем пост в списке
            for (int i = 0; i < posts.size(); i++) {
                if (posts.get(i).getId().equals(updatedPost.getId())) {
                    posts.set(i, updatedPost);
                    Log.d(TAG, "Post updated in list at position " + i);
                    break;
                }
            }
        }
    }

    public void deleteImage(Long postId, String imageUrl, OnNetworkCallback callback) {
        apiService.deleteImage(postId, imageUrl).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
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
                    if (callback != null) {
                        callback.onError("Ошибка при удалении изображения: " + response.code() + " " + errorBody);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error while deleting image", t);
                if (callback != null) {
                    callback.onError("Ошибка сети при удалении изображения: " + t.getMessage());
                }
            }
        });
    }

    public void updatePostRating(PostRating postRating, OnNetworkCallback callback) {
        // Используем один эндпоинт для всех операций с рейтингом
        apiService.updatePostRating(postRating.getPostId(), postRating).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updatePostInList(response.body());
                    callback.onSuccess();
                } else {
                    callback.onError("Ошибка при обновлении рейтинга: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    public void addLike(Long postId, String userLogin, OnNetworkCallback callback) {
        if (userLogin == null || userLogin.isEmpty()) {
            Log.e(TAG, "User login is null or empty");
            if (callback != null) {
                callback.onError("Логин пользователя не указан");
            }
            return;
        }

        Log.d(TAG, "Adding like for post " + postId + " and user " + userLogin);
        this.callback = callback;
        
        Map<String, String> userData = new HashMap<>();
        userData.put("userLogin", userLogin);
        
        apiService.addLike(postId, userData).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Post updatedPost = response.body();
                    // Проверяем, что лайк действительно добавлен
                    if (updatedPost.hasUserLiked(userLogin)) {
                        if (posts != null) {
                            for (int i = 0; i < posts.size(); i++) {
                                if (posts.get(i).getId().equals(updatedPost.getId())) {
                                    posts.set(i, updatedPost);
                                    break;
                                }
                            }
                        }
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else {
                        if (callback != null) {
                            callback.onError("Не удалось добавить лайк");
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
                    Log.e(TAG, "Failed to add like. Code: " + response.code() + ", Error: " + errorBody);
                    if (callback != null) {
                        callback.onError("Ошибка при добавлении лайка: " + response.code() + " " + errorBody);
                    }
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.e(TAG, "Network error while adding like", t);
                if (callback != null) {
                    callback.onError("Ошибка сети при добавлении лайка: " + t.getMessage());
                }
            }
        });
    }

    public void removeLike(Long postId, String userLogin, OnNetworkCallback callback) {
        if (userLogin == null || userLogin.isEmpty()) {
            Log.e(TAG, "User login is null or empty");
            if (callback != null) {
                callback.onError("Логин пользователя не указан");
            }
            return;
        }

        Log.d(TAG, "Removing like for post " + postId + " and user " + userLogin);
        this.callback = callback;
        
        Map<String, String> userData = new HashMap<>();
        userData.put("userLogin", userLogin);
        
        apiService.removeLike(postId, userData).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Post updatedPost = response.body();
                    // Проверяем, что лайк действительно удален
                    if (!updatedPost.hasUserLiked(userLogin)) {
                        if (posts != null) {
                            for (int i = 0; i < posts.size(); i++) {
                                if (posts.get(i).getId().equals(updatedPost.getId())) {
                                    posts.set(i, updatedPost);
                                    break;
                                }
                            }
                        }
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else {
                        if (callback != null) {
                            callback.onError("Не удалось удалить лайк");
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
                    Log.e(TAG, "Failed to remove like. Code: " + response.code() + ", Error: " + errorBody);
                    if (callback != null) {
                        callback.onError("Ошибка при удалении лайка: " + response.code() + " " + errorBody);
                    }
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.e(TAG, "Network error while removing like", t);
                if (callback != null) {
                    callback.onError("Ошибка сети при удалении лайка: " + t.getMessage());
                }
            }
        });
    }

    public void checkLike(Long postId, String userLogin, OnNetworkCallback callback) {
        apiService.checkLike(postId, userLogin).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Post post = response.body();
                    boolean hasLiked = post.hasUserLiked(userLogin);
                    Log.d(TAG, "User " + userLogin + " has liked post " + postId + ": " + hasLiked);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    if (callback != null) {
                        callback.onError("Ошибка при проверке лайка: " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                if (callback != null) {
                    callback.onError("Ошибка сети: " + t.getMessage());
                }
            }
        });
    }

    public interface OnLikeCheckCallback {
        void onSuccess(boolean hasLiked);
        void onError(String error);
    }

    private void removePostFromList(Long postId) {
        if (posts != null) {
            posts.removeIf(post -> post.getId().equals(postId));
            Log.d(TAG, "Post with ID " + postId + " removed from local list. Remaining posts: " + posts.size());
        }
    }
    
    public void deletePost(Long postId, OnNetworkCallback callback) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is null");
            if (callback != null) {
                callback.onError("Ошибка инициализации сервиса");
            }
            return;
        }

        Log.d(TAG, "=== DELETING POST ===");
        Log.d(TAG, "Deleting post with ID: " + postId);

        apiService.deletePost(postId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "=== DELETE POST RESPONSE ===");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful()) {
                    Log.d(TAG, "Post deleted successfully from server");
                    
                    // Удаляем пост из локального списка
                    removePostFromList(postId);
                    
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "=== DELETE POST FAILED ===");
                    Log.e(TAG, "Failed to delete post. Code: " + response.code() + ", Error: " + errorBody);
                    if (callback != null) {
                        callback.onError("Ошибка удаления поста: " + response.code() + " " + errorBody);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "=== DELETE POST NETWORK ERROR ===");
                Log.e(TAG, "Network error during post deletion", t);
                if (callback != null) {
                    callback.onError("Ошибка сети: " + t.getMessage());
                }
            }
        });
    }

    public void getUserAvatarUrl(String login, OnAvatarUrlCallback callback) {
        if (login == null || login.isEmpty()) {
            Log.e(TAG, "Login is null or empty");
            callback.onSuccess(null);
            return;
        }

        // Проверяем кэш
        if (userAvatars.containsKey(login)) {
            String cachedUrl = userAvatars.get(login);
            Log.d(TAG, "Avatar URL found in cache for user: " + login + " -> " + cachedUrl);
            callback.onSuccess(cachedUrl);
            return;
        }

        // Если это текущий пользователь, используем его аватар
        if (currentUser != null && login.equals(currentUser.getUsername())) {
            String avatarUrl = currentUser.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Log.d(TAG, "Using current user avatar: " + login + " -> " + avatarUrl);
                userAvatars.put(login, avatarUrl);
                callback.onSuccess(avatarUrl);
                return;
            }
        }

        // Для других пользователей пробуем найти их ID в постах и получить аватар
        if (posts != null) {
            for (Post post : posts) {
                if (login.equals(post.getLogin())) {
                    // Пробуем получить всех пользователей и найти нужного
                    Log.d(TAG, "Trying to get avatar for user: " + login + " via getAllUsers");
                    tryGetUserFromAllUsers(login, callback);
                    return;
                }
            }
        }

        // Если не удалось получить аватар, возвращаем null (будет показан аватар по умолчанию)
        Log.d(TAG, "No avatar available for user: " + login + ", using default");
        callback.onSuccess(null);
    }

    private void tryGetUserFromAllUsers(String login, OnAvatarUrlCallback callback) {
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    for (User user : users) {
                        if (login.equals(user.getUsername())) {
                            String avatarUrl = user.getAvatarUrl();
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Log.d(TAG, "Received avatar URL for user " + login + " via getAllUsers: " + avatarUrl);
                                userAvatars.put(login, avatarUrl);
                                callback.onSuccess(avatarUrl);
                                return;
                            }
                            break;
                        }
                    }
                    Log.e(TAG, "User not found in getAllUsers response");
                    callback.onSuccess(null);
                } else {
                    Log.e(TAG, "Network error while getting avatar URL for user: " + login + " via getAllUsers, code: " + response.code());
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e(TAG, "Network error while getting avatar URL for user: " + login + " via getAllUsers", t);
                callback.onSuccess(null);
            }
        });
    }

    public interface OnAvatarUrlCallback {
        void onSuccess(String avatarUrl);
    }

    public void clearUserAvatarCache() {
        userAvatars.clear();
        Log.d(TAG, "User avatar cache cleared");
    }

    public void updateUserAvatarCache(String login, String avatarUrl) {
        userAvatars.put(login, avatarUrl);
        Log.d(TAG, "Updated avatar cache for user: " + login + " -> " + avatarUrl);
    }

    public void forceUpdateUserAvatar(String login) {
        userAvatars.remove(login);
        Log.d(TAG, "Forced avatar cache update for user: " + login);
    }

    public void refreshAllUserAvatars() {
        userAvatars.clear();
        Log.d(TAG, "All user avatars cache refreshed");
    }

    public void getPost(Long postId, OnNetworkCallback callback) {
        apiService.getPost(postId).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Post post = response.body();
                    Log.d(TAG, "Retrieved post: " + post.getId() + " with images: " + post.getImagesList());
                    callback.onSuccess();
                } else {
                    String errorMessage = "Ошибка при получении поста";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += ": " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.e(TAG, "Network error while getting post", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    public Post getPostById(Long postId) {
        if (posts != null) {
            for (Post post : posts) {
                if (post.getId().equals(postId)) {
                    return post;
                }
            }
        }
        return null;
    }

    public void getUserProfile(String login, OnNetworkCallback callback) {
        apiService.getUserProfile(login).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d(TAG, "Retrieved user profile: " + user.getUsername());
                    callback.onSuccess();
                } else {
                    String errorMessage = "Ошибка при получении профиля пользователя";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += ": " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Network error while getting user profile", t);
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    public User getUserByLogin(String login) {
        if (login == null || login.isEmpty()) {
            return null;
        }

        // Проверяем кэш пользователей
        if (userCache.containsKey(login)) {
            return userCache.get(login);
        }

        // Если нет в кэше, возвращаем null
        // В реальном приложении здесь можно сделать запрос к серверу
        return null;
    }

    public void addLike(Long postId, Map<String, String> userData, OnNetworkCallback callback) {
        apiService.addLike(postId, userData).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updatePostInList(response.body());
                    callback.onSuccess();
                } else {
                    callback.onError("Ошибка при добавлении лайка: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    public void removeLike(Long postId, Map<String, String> userData, OnNetworkCallback callback) {
        apiService.removeLike(postId, userData).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updatePostInList(response.body());
                    callback.onSuccess();
                } else {
                    callback.onError("Ошибка при удалении лайка: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void updateUserInList(User updatedUser) {
        // Обновляем пользователя в кэше
        if (updatedUser != null && updatedUser.getUsername() != null) {
            userCache.put(updatedUser.getUsername(), updatedUser);
        }
    }
}

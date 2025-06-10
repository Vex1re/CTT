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
import androidx.lifecycle.ViewModel;

public class AppViewModel extends ViewModel {
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
        Log.d(TAG, "Отправка поста: " + post.toString());

        apiService.createPost(post).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Post createdPost = response.body();
                    // После создания поста загружаем изображения
                    if (post.getImagesList() != null && !post.getImagesList().isEmpty()) {
                        uploadImages(createdPost.getId(), post.getImagesList(), callback);
                    } else {
                        if (callback != null) {
                            callback.onSuccess();
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

    public void uploadImages(Long postId, List<String> imageUris, OnNetworkCallback callback) {
        if (imageUris == null || imageUris.isEmpty()) {
            Log.d(TAG, "No images to upload");
            callback.onSuccess();
            return;
        }

        Log.d(TAG, "Starting upload of " + imageUris.size() + " images for post " + postId);
        
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        long totalSize = 0;
        final long MAX_REQUEST_SIZE = 8 * 1024 * 1024; // 8MB limit to stay under 10MB server limit
        final int MAX_IMAGES_PER_BATCH = 3; // Maximum images per batch
        
        for (int i = 0; i < imageUris.size(); i++) {
            String imageUri = imageUris.get(i);
            try {
                Log.d(TAG, "Processing image " + (i + 1) + "/" + imageUris.size() + ": " + imageUri);
                
                File imageFile = new File(Uri.parse(imageUri).getPath());
                if (!imageFile.exists()) {
                    Log.e(TAG, "File does not exist: " + imageUri);
                    continue;
                }
                
                // Compress image if it's too large
                File compressedFile = compressImageIfNeeded(imageFile);
                long fileSize = compressedFile.length();
                Log.d(TAG, "File size: " + fileSize + " bytes (" + (fileSize / 1024 / 1024) + " MB)");
                
                if (fileSize > 10 * 1024 * 1024) {
                    Log.e(TAG, "File too large even after compression: " + imageUri + ", size: " + fileSize);
                    continue;
                }
                
                Log.d(TAG, "Preparing file for upload: " + compressedFile.getAbsolutePath() + ", size: " + fileSize);
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), compressedFile);
                MultipartBody.Part body = MultipartBody.Part.createFormData("files", compressedFile.getName(), requestFile);
                imageParts.add(body);
                totalSize += fileSize;
                Log.d(TAG, "Successfully added file to upload list: " + compressedFile.getName());
                
                // Check if we need to start a new batch
                if (imageParts.size() >= MAX_IMAGES_PER_BATCH || totalSize > MAX_REQUEST_SIZE) {
                    Log.d(TAG, "Batch limit reached. Uploading batch of " + imageParts.size() + " images (total size: " + (totalSize / 1024 / 1024) + " MB)");
                    uploadImageBatch(postId, new ArrayList<>(imageParts), callback);
                    imageParts.clear();
                    totalSize = 0;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error preparing image for upload: " + imageUri, e);
            }
        }

        // Upload remaining images
        if (!imageParts.isEmpty()) {
            Log.d(TAG, "Uploading final batch of " + imageParts.size() + " images (total size: " + (totalSize / 1024 / 1024) + " MB)");
            uploadImageBatch(postId, imageParts, callback);
        } else if (imageUris.size() > 0) {
            Log.d(TAG, "No valid images to upload");
            callback.onError("Не удалось подготовить изображения для загрузки");
        }
    }
    
    private void uploadImageBatch(Long postId, List<MultipartBody.Part> imageParts, OnNetworkCallback callback) {
        Log.d(TAG, "Uploading batch of " + imageParts.size() + " images for post " + postId);
        apiService.uploadImages(postId, imageParts).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                Log.d(TAG, "Upload response received. Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Post updatedPost = response.body();
                    Log.d(TAG, "Images uploaded successfully. Updated post images: " + updatedPost.getImagesList());
                    callback.onSuccess();
                } else {
                    String errorMessage = "Ошибка загрузки изображений: код " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMessage += ", ответ: " + errorBody;
                            Log.e(TAG, "Server error response: " + errorBody);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    
                    if (response.code() == 413) {
                        errorMessage = "Размер изображений слишком большой. Попробуйте загрузить меньше изображений или изображения меньшего размера.";
                    }
                    
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.e(TAG, "Network error while uploading images", t);
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
        
        Log.d(TAG, "Compressing image from " + (originalSize / 1024 / 1024) + " MB to target 2MB");
        
        // Create compressed file
        File compressedFile = File.createTempFile("compressed_", ".jpg", context.getCacheDir());
        
        try {
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(originalFile.getAbsolutePath());
            if (bitmap == null) {
                Log.e(TAG, "Could not decode bitmap from file: " + originalFile.getAbsolutePath());
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
                Log.d(TAG, "Compression attempt: quality=" + quality + ", size=" + (currentSize / 1024 / 1024) + " MB");
            }
            
            // Write compressed image to file
            java.io.FileOutputStream fos = new java.io.FileOutputStream(compressedFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, fos);
            fos.close();
            
            Log.d(TAG, "Image compressed successfully: " + (compressedFile.length() / 1024 / 1024) + " MB");
            return compressedFile;
            
        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            return originalFile;
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
        this.callback = callback;
        
        // Отправляем запрос на сервер через Retrofit
        apiService.updatePostRating(postRating.getPostId(), postRating).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Обновляем только обновленный пост в списке
                    Post updatedPost = response.body();
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
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Failed to update post rating. Code: " + response.code() + ", Error: " + errorBody);
                    if (callback != null) {
                        callback.onError("Ошибка при обновлении рейтинга: " + response.code() + " " + errorBody);
                    }
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.e(TAG, "Network error while updating post rating", t);
                if (callback != null) {
                    callback.onError("Ошибка сети при обновлении рейтинга: " + t.getMessage());
                }
            }
        });
    }
}

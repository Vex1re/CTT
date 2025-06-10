package space.krokodilich.ctt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
// import androidx.lifecycle.ViewModel; // Удаляем импорт стандартного ViewModel
import space.krokodilich.ctt.ViewModel; // Импортируем ваш пользовательский ViewModel
import android.app.AlertDialog;
import com.google.android.material.button.MaterialButton;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.List;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import android.util.Log;
import android.app.ProgressDialog;
import android.os.Handler;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import android.graphics.drawable.Drawable;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import android.os.Looper;
import java.util.stream.Collectors;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

public class ProfileFragment extends Fragment {

    private TextView nameTextView;
    private TextView cityTextView;
    private TextView statusTextView;
    private TextView postsTextView;
    private TextView ratingTextView;
    private TextView statusCountTextView;
    private MaterialButton createPostButton;
    private space.krokodilich.ctt.ViewModel viewModel;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ProfilePagerAdapter pagerAdapter;
    private ImageView avatarImageView;
    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
        }

        // Инициализация UI элементов
        nameTextView = view.findViewById(R.id.profile_name);
        cityTextView = view.findViewById(R.id.profile_location);
        statusTextView = view.findViewById(R.id.profile_status_text);
        postsTextView = view.findViewById(R.id.profile_posts_count);
        ratingTextView = view.findViewById(R.id.profile_rating_count);
        statusCountTextView = view.findViewById(R.id.profile_status_count);
        avatarImageView = view.findViewById(R.id.profile_avatar);
        createPostButton = view.findViewById(R.id.create_post_button);

        // Настройка ViewPager и TabLayout
        viewPager = view.findViewById(R.id.profile_viewpager);
        tabLayout = view.findViewById(R.id.profile_tabs);
        
        // Проверяем, что пользователь авторизован
        if (viewModel != null && viewModel.getCurrentUser() != null) {
            pagerAdapter = new ProfilePagerAdapter(this, viewModel.getCurrentUser().getUsername(), true);
            viewPager.setAdapter(pagerAdapter);

            // Показываем TabLayout и настраиваем вкладки
            tabLayout.setVisibility(View.VISIBLE);
            
            // Настраиваем вкладки для текущего пользователя
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText("Мои посты");
                        break;
                    case 1:
                        tab.setText("Понравившиеся");
                        break;
                }
            }).attach();
        } else {
            // Если пользователь не авторизован, скрываем TabLayout
            tabLayout.setVisibility(View.GONE);
            Log.w("ProfileFragment", "Current user is null, hiding tabs");
        }

        // Настройка кнопки создания поста
        createPostButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new CreatePostFragment());
            }
        });

        // Обновляем данные постов перед загрузкой профиля
        if (viewModel != null) {
            viewModel.getPosts(new ViewModel.OnNetworkCallback() {
                @Override
                public void onSuccess() {
                    // После обновления постов загружаем профиль
                    loadUserProfile();
                }

                @Override
                public void onError(String error) {
                    // Даже если обновление не удалось, загружаем профиль с текущими данными
                    loadUserProfile();
                }
            });
        } else {
            loadUserProfile();
        }

        // Устанавливаем обработчик нажатия на аватарку
        avatarImageView.setOnClickListener(v -> openImagePicker());

        // Обработчики нажатий
        createPostButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToCreatePost();
            }
        });

        MaterialButton logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Выход из аккаунта")
                .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                .setPositiveButton("Да", (dialog, which) -> {
                    if (getActivity() instanceof MainActivity) {
                        // Очищаем историю прочитанных уведомлений
                        ((MainActivity) getActivity()).clearNotificationHistory();
                        viewModel.clearUserId();
                        ((MainActivity) getActivity()).showAuthFragmentAndHideBottomNav();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем данные постов при возвращении на экран
        if (viewModel != null) {
            viewModel.getPosts(new ViewModel.OnNetworkCallback() {
                @Override
                public void onSuccess() {
                    // После обновления постов загружаем профиль
                    loadUserProfile();
                    // Обновляем вкладки
                    updateTabs();
                }

                @Override
                public void onError(String error) {
                    // Даже если обновление не удалось, загружаем профиль с текущими данными
                    loadUserProfile();
                    // Обновляем вкладки
                    updateTabs();
                }
            });
        } else {
            loadUserProfile();
            updateTabs();
        }
    }

    private void loadUserProfile() {
        if (viewModel != null && viewModel.getCurrentUser() != null) {
            User user = viewModel.getCurrentUser();
            nameTextView.setText(user.getName() + " " + user.getSurname());
            cityTextView.setText(user.getCity());
            
            // Обновляем аватарку
            if (user.getAvatar() != null) {
                String avatarUrl = user.getAvatar();
                if (!avatarUrl.startsWith("http")) {
                    avatarUrl = "https://spring-boot-production-6510.up.railway.app" + avatarUrl;
                }
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(avatarImageView);
            } else {
                // Если аватарки нет, устанавливаем аватарку по умолчанию
                avatarImageView.setImageResource(R.drawable.ic_default_avatar);
            }
            
            // Получаем все посты и подсчитываем количество постов и суммарный рейтинг текущего пользователя
            List<Post> allPosts = viewModel.getPosts();
            int userPostsCount = 0;
            int totalUserRating = 0;
            
            Log.d("ProfileFragment", "Loading profile for user: " + user.getUsername());
            Log.d("ProfileFragment", "Total posts in ViewModel: " + (allPosts != null ? allPosts.size() : 0));
            
            if (allPosts != null) {
                List<Post> userPosts = allPosts.stream()
                    .filter(post -> user.getUsername().equals(post.getLogin()))
                    .collect(Collectors.toList());
                
                userPostsCount = userPosts.size();
                totalUserRating = userPosts.stream()
                    .mapToInt(Post::getRating)
                    .sum();
                
                Log.d("ProfileFragment", "User posts count: " + userPostsCount);
                Log.d("ProfileFragment", "Total user rating: " + totalUserRating);
                
                // Логируем детали каждого поста пользователя
                for (Post post : userPosts) {
                    Log.d("ProfileFragment", "Post ID: " + post.getId() + ", Rating: " + post.getRating() + ", Images: " + post.getImagesList());
                }
            }
            
            // Определяем статус на основе суммарного рейтинга
            String status;
            if (totalUserRating >= 1000) {
                status = "Легенда";
            } else if (totalUserRating >= 500) {
                status = "Эксперт";
            } else if (totalUserRating >= 100) {
                status = "Профессионал";
            } else if (totalUserRating >= 50) {
                status = "Опытный";
            } else if (totalUserRating >= 10) {
                status = "Активный";
            } else {
                status = "Новичок";
            }
            
            Log.d("ProfileFragment", "Calculated status: " + status);
            
            statusTextView.setVisibility(View.GONE); // Скрываем TextView со статусом
            postsTextView.setText(String.valueOf(userPostsCount));
            ratingTextView.setText(String.valueOf(totalUserRating));
            statusCountTextView.setText(status);
        }
    }

    private void clearUserData() {
        if (getActivity() instanceof MainActivity) {
            space.krokodilich.ctt.ViewModel viewModel = ((MainActivity) getActivity()).getViewModel();
            viewModel.clearUserId();
        }
    }

    private void openImagePicker() {
        // Проверяем разрешения в зависимости от версии Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
                return;
            }
        } else {
            if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadAvatarToServer(imageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение получено, открываем галерею
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            } else {
                Toast.makeText(requireContext(), 
                        "Для загрузки аватара необходимо разрешение на доступ к галерее", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void uploadAvatarToServer(Uri imageUri) {
        if (getActivity() == null) return;

        // Показываем индикатор загрузки
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Загрузка аватара...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Работаем с URI напрямую через ContentResolver
        try {
            // Получаем InputStream из URI
            java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Ошибка: не удалось открыть файл", Toast.LENGTH_LONG).show();
                return;
            }

            // Читаем данные в байтовый массив
            byte[] imageBytes = new byte[0];
            try {
                imageBytes = readInputStreamToByteArray(inputStream);
                inputStream.close();
            } catch (Exception e) {
                progressDialog.dismiss();
                android.util.Log.e("ProfileFragment", "Ошибка чтения файла", e);
                Toast.makeText(getContext(), "Ошибка чтения файла: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            // Проверяем размер файла (максимум 10MB)
            if (imageBytes.length > 10 * 1024 * 1024) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Ошибка: файл слишком большой", Toast.LENGTH_LONG).show();
                return;
            }

            // Логируем размер файла
            android.util.Log.d("ProfileFragment", "Avatar file size: " + imageBytes.length + " bytes");

            // Создаем RequestBody из байтового массива
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
            
            // Получаем имя файла из URI
            String fileName = getFileNameFromUri(imageUri);
            if (fileName == null) {
                fileName = "avatar.jpg"; // fallback имя
            }
            
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);

            Long userId = viewModel.getCurrentUser().getId();
            if (userId == null) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Ошибка: не найден ID пользователя", Toast.LENGTH_LONG).show();
                return;
            }

            ApiService apiService = RetrofitClient.getInstance().getApiService();
            apiService.uploadAvatar(userId, body).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    progressDialog.dismiss();
                    if (getActivity() == null) return;
                    
                    // Логируем детали ответа
                    android.util.Log.d("ProfileFragment", "Response code: " + response.code());
                    android.util.Log.d("ProfileFragment", "Response headers: " + response.headers());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body();
                        android.util.Log.d("ProfileFragment", "Response body: " + responseBody);
                        
                        // Проверяем, что ответ не пустой и не содержит только пробелы
                        if (responseBody != null && !responseBody.trim().isEmpty()) {
                            String avatarPath = responseBody.trim();
                            android.util.Log.d("ProfileFragment", "Parsed avatar path: " + avatarPath);
                            
                            User currentUser = viewModel.getCurrentUser();
                            if (currentUser != null) {
                                currentUser.setAvatar(avatarPath);
                                apiService.updateUser(currentUser.getId(), currentUser).enqueue(new Callback<User>() {
                                    @Override
                                    public void onResponse(Call<User> call, Response<User> userResponse) {
                                        if (userResponse.isSuccessful() && userResponse.body() != null) {
                                            apiService.getUser(String.valueOf(currentUser.getId())).enqueue(new Callback<User>() {
                                                @Override
                                                public void onResponse(Call<User> call, Response<User> freshUserResponse) {
                                                    if (freshUserResponse.isSuccessful() && freshUserResponse.body() != null) {
                                                        viewModel.updateCurrentUser(freshUserResponse.body());
                                                        viewModel.clearUserAvatarCache();
                                                        if (freshUserResponse.body().getUsername() != null) {
                                                            viewModel.forceUpdateUserAvatar(freshUserResponse.body().getUsername());
                                                        }
                                                        viewModel.refreshAllUserAvatars();
                                                        if (getActivity() != null) {
                                                            getActivity().runOnUiThread(() -> {
                                                                loadUserProfile();
                                                                Toast.makeText(getContext(), "Аватарка успешно обновлена!", Toast.LENGTH_SHORT).show();
                                                            });
                                                        }
                                                    }
                                                }
                                                @Override
                                                public void onFailure(Call<User> call, Throwable t) {}
                                            });
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<User> call, Throwable t) {}
                                });
                            }
                        } else {
                            android.util.Log.e("ProfileFragment", "Empty or null response body");
                            Toast.makeText(getContext(), "Ошибка: сервер вернул пустой ответ", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String errorMsg = "Ошибка загрузки аватара: код " + response.code();
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                errorMsg += ", ответ: " + errorBody;
                                android.util.Log.e("ProfileFragment", "Error response body: " + errorBody);
                            } catch (Exception e) {
                                android.util.Log.e("ProfileFragment", "Error reading error body", e);
                            }
                        }
                        android.util.Log.e("ProfileFragment", errorMsg);
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    progressDialog.dismiss();
                    android.util.Log.e("ProfileFragment", "Ошибка загрузки аватара", t);
                    Toast.makeText(getContext(), "Ошибка загрузки аватара: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            android.util.Log.e("ProfileFragment", "Ошибка при работе с файлом", e);
            Toast.makeText(getContext(), "Ошибка при работе с файлом: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Вспомогательный метод для чтения InputStream в байтовый массив
    private byte[] readInputStreamToByteArray(java.io.InputStream inputStream) throws Exception {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    // Вспомогательный метод для получения имени файла из URI
    private String getFileNameFromUri(Uri uri) {
        try {
            String result = null;
            if (uri.getScheme().equals("content")) {
                android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                            if (index >= 0) {
                                result = cursor.getString(index);
                            }
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }
            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
            return result;
        } catch (Exception e) {
            android.util.Log.e("ProfileFragment", "Ошибка получения имени файла", e);
            return "avatar.jpg";
        }
    }

    private String getFallbackAvatarUrl() {
        String currentAvatar = viewModel.getCurrentUser().getAvatar();
        if (currentAvatar != null && !currentAvatar.startsWith("http")) {
            if (currentAvatar.startsWith("/uploads/")) {
                currentAvatar = "https://spring-boot-production-6510.up.railway.app" + currentAvatar;
            } else {
                currentAvatar = "https://spring-boot-production-6510.up.railway.app/uploads/" + currentAvatar;
            }
        }
        return currentAvatar;
    }

    private void updateTabs() {
        if (viewModel != null && viewModel.getCurrentUser() != null && tabLayout != null) {
            // Показываем вкладки и обновляем адаптер
            tabLayout.setVisibility(View.VISIBLE);
            if (pagerAdapter == null) {
                pagerAdapter = new ProfilePagerAdapter(this, viewModel.getCurrentUser().getUsername(), true);
                viewPager.setAdapter(pagerAdapter);
                
                // Настраиваем вкладки
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Мои посты");
                            break;
                        case 1:
                            tab.setText("Понравившиеся");
                            break;
                    }
                }).attach();
            }
        } else if (tabLayout != null) {
            // Скрываем вкладки если пользователь не авторизован
            tabLayout.setVisibility(View.GONE);
        }
    }
}

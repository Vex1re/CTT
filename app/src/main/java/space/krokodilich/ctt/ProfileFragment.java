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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация ViewModel
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
        createPostButton = view.findViewById(R.id.create_post_button);
        MaterialButton logoutButton = view.findViewById(R.id.logout_button);
        avatarImageView = view.findViewById(R.id.profile_avatar);

        // Настройка ViewPager и TabLayout
        viewPager = view.findViewById(R.id.profile_viewpager);
        tabLayout = view.findViewById(R.id.profile_tabs);
        pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Мои посты");
                            break;
                        case 1:
                            tab.setText("Понравившиеся");
                            break;
                    }
                }
        ).attach();

        // Загрузка данных пользователя
        loadUserProfile();

        // Загрузка аватара
        if (viewModel.getCurrentUser() != null && viewModel.getCurrentUser().getAvatar() != null) {
            String avatarUrl = viewModel.getCurrentUser().getAvatar();
            if (!avatarUrl.startsWith("http")) {
                avatarUrl = "https://spring-boot-production-6510.up.railway.app" + avatarUrl;
            }
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(avatarImageView);
        }
        avatarImageView.setOnClickListener(v -> openImagePicker());

        // Обработчики нажатий
        createPostButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToCreatePost();
            }
        });

        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Выход из аккаунта")
                .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                .setPositiveButton("Да", (dialog, which) -> {
                    if (getActivity() instanceof MainActivity) {
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
        // Обновляем данные профиля при возвращении на экран
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (viewModel != null && viewModel.getCurrentUser() != null) {
            User user = viewModel.getCurrentUser();
            nameTextView.setText(user.getName() + " " + user.getSurname());
            cityTextView.setText(user.getCity());
            
            // Определяем статус на основе рейтинга
            String status;
            if (user.getRating() >= 1000) {
                status = "Легенда";
            } else if (user.getRating() >= 500) {
                status = "Эксперт";
            } else if (user.getRating() >= 100) {
                status = "Профессионал";
            } else if (user.getRating() >= 50) {
                status = "Опытный";
            } else if (user.getRating() >= 10) {
                status = "Активный";
            } else {
                status = "Новичок";
            }
            
            statusTextView.setText(status);
            
            // Получаем все посты и подсчитываем количество постов текущего пользователя
            List<Post> allPosts = viewModel.getPosts();
            int userPostsCount = 0;
            if (allPosts != null) {
                userPostsCount = (int) allPosts.stream()
                    .filter(post -> user.getUsername().equals(post.getLogin()))
                    .count();
            }
            postsTextView.setText(String.valueOf(userPostsCount));
            
            ratingTextView.setText(String.valueOf(user.getRating()));
            statusCountTextView.setText(String.valueOf(user.getRating())); // Показываем рейтинг вместо статуса
        }
    }

    private void clearUserData() {
        if (getActivity() instanceof MainActivity) {
            space.krokodilich.ctt.ViewModel viewModel = ((MainActivity) getActivity()).getViewModel();
            viewModel.clearUserId();
        }
    }

    private void openImagePicker() {
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

    private void uploadAvatarToServer(Uri imageUri) {
        if (getActivity() == null) return;

        // Показываем индикатор загрузки
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Загрузка аватара...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String realPath = getRealPathFromUri(imageUri);
        if (realPath == null) {
            progressDialog.dismiss();
            return;
        }

        File imageFile = new File(realPath);
        if (!imageFile.exists()) {
            progressDialog.dismiss();
            return;
        }

        // Проверяем размер файла (максимум 10MB)
        if (imageFile.length() > 10 * 1024 * 1024) {
            progressDialog.dismiss();
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        Long userId = viewModel.getCurrentUser().getId();
        if (userId == null) {
            progressDialog.dismiss();
            return;
        }

        // Сразу обновляем локально
        Glide.with(ProfileFragment.this)
            .load(imageUri)
            .placeholder(R.drawable.ic_default_avatar)
            .error(R.drawable.ic_default_avatar)
            .into(avatarImageView);

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        apiService.uploadAvatar(userId, body).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                progressDialog.dismiss();
                
                if (getActivity() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    String avatarPath = response.body().trim();
                    
                    // Обновляем данные пользователя
                    User currentUser = viewModel.getCurrentUser();
                    if (currentUser != null) {
                        currentUser.setAvatar(avatarPath);
                        viewModel.updateCurrentUser(currentUser);

                        // Загружаем новый аватар с сервера
                        String avatarUrl = avatarPath;
                        if (!avatarUrl.startsWith("http")) {
                            if (avatarUrl.startsWith("/uploads/")) {
                                avatarUrl = "https://spring-boot-production-6510.up.railway.app" + avatarUrl;
                            } else {
                                avatarUrl = "https://spring-boot-production-6510.up.railway.app/uploads/" + avatarUrl;
                            }
                        }

                        Glide.with(ProfileFragment.this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .into(avatarImageView);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
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

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }
}

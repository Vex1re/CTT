package space.krokodilich.ctt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.bumptech.glide.Glide;
import android.util.Log;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.stream.Collectors;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public class UserProfileFragment extends Fragment {
    private static final String TAG = "UserProfileFragment";
    private static final String ARG_USER_LOGIN = "user_login";
    
    private TextView nameTextView;
    private TextView cityTextView;
    private TextView statusTextView;
    private TextView postsTextView;
    private TextView ratingTextView;
    private TextView statusCountTextView;
    private ImageView avatarImageView;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ProfilePagerAdapter pagerAdapter;
    private ViewModel viewModel;
    private String userLogin;

    public static UserProfileFragment newInstance(String login) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_LOGIN, login);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userLogin = getArguments().getString(ARG_USER_LOGIN);
        }
    }

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
        view.findViewById(R.id.create_post_button).setVisibility(View.GONE);
        view.findViewById(R.id.logout_button).setVisibility(View.GONE);

        // Настройка ViewPager и TabLayout
        viewPager = view.findViewById(R.id.profile_viewpager);
        tabLayout = view.findViewById(R.id.profile_tabs);
        pagerAdapter = new ProfilePagerAdapter(this, userLogin, false);
        viewPager.setAdapter(pagerAdapter);

        // Скрываем TabLayout, так как у нас только одна вкладка
        tabLayout.setVisibility(View.GONE);

        // Загрузка данных пользователя
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (viewModel != null) {
            List<Post> allPosts = viewModel.getPosts();
            if (allPosts != null) {
                // Получаем посты только этого пользователя
                List<Post> userPosts = allPosts.stream()
                    .filter(post -> userLogin.equals(post.getLogin()))
                    .collect(Collectors.toList());
                
                // Подсчитываем количество постов и суммарный рейтинг
                int userPostsCount = userPosts.size();
                int totalUserRating = userPosts.stream()
                    .mapToInt(Post::getRating)
                    .sum();
                
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
                
                // Устанавливаем данные пользователя из первого поста
                if (!userPosts.isEmpty()) {
                    Post firstPost = userPosts.get(0);
                    nameTextView.setText(firstPost.getAuthorName());
                    cityTextView.setText(firstPost.getLocation());
                }
                
                statusTextView.setVisibility(View.GONE); // Скрываем TextView со статусом
                postsTextView.setText(String.valueOf(userPostsCount));
                ratingTextView.setText(String.valueOf(totalUserRating));
                statusCountTextView.setText(status);

                // Загружаем аватар пользователя
                viewModel.getUserAvatarUrl(userLogin, avatarUrl -> {
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(this)
                            .load(avatarUrl)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .into(avatarImageView);
                    }
                });
            }
        }
    }
} 
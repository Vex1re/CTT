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

        // Настройка ViewPager и TabLayout
        viewPager = view.findViewById(R.id.profile_viewpager);
        tabLayout = view.findViewById(R.id.profile_tabs);
        pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Мои посты" : "Понравившиеся")
        ).attach();

        // Загрузка данных пользователя
        loadUserProfile();

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

    private void loadUserProfile() {
        if (viewModel != null && viewModel.getCurrentUser() != null) {
            User user = viewModel.getCurrentUser();
            nameTextView.setText(user.getName() + " " + user.getSurname());
            cityTextView.setText(user.getCity());
            statusTextView.setText(user.getStatus());
            postsTextView.setText(String.valueOf(user.getPosts()));
            ratingTextView.setText(String.valueOf(user.getRating()));
            statusCountTextView.setText(user.getStatus());
        }
    }

    private void clearUserData() {
        if (getActivity() instanceof MainActivity) {
            space.krokodilich.ctt.ViewModel viewModel = ((MainActivity) getActivity()).getViewModel();
            viewModel.clearUserId();
        }
    }
}

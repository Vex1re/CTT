package space.krokodilich.ctt;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private space.krokodilich.ctt.ViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new space.krokodilich.ctt.ViewModel();
        viewModel.initialize(this);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // bottomNavigationView.setVisibility(View.GONE); // Изначально скрываем панель, пока не решим, показывать главную страницу или нет
        setupBottomNavigation();

        // Проверяем, есть ли сохраненный ID пользователя
        Long savedUserId = viewModel.getSavedUserId();

        if (savedUserId != null) {
            // Если ID найден, пытаемся загрузить пользователя
            viewModel.setCallback(new ViewModel.OnNetworkCallback() {
                @Override
                public void onSuccess() {
                    // Если пользователь успешно загружен, показываем основное содержимое
                    showMainContent();
                    // Убираем callback после успешной загрузки, чтобы он не перехватывал другие события
                    viewModel.setCallback(null);
                }

                @Override
                public void onError(String error) {
                    // Если загрузка пользователя не удалась (например, ID недействителен), очищаем сохраненный ID и показываем экран аутентификации
                    viewModel.clearUserId();
                    showAuthFragmentAndHideBottomNav();
                    // Убираем callback
                    viewModel.setCallback(null);
                }
            });
            viewModel.fetchUserById(savedUserId);
        } else {
            // Если ID не найден, показываем экран аутентификации
            showAuthFragmentAndHideBottomNav();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_notifications) {
                selectedFragment = new NotificationsFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    public void showMainContent() {
        bottomNavigationView.setVisibility(View.VISIBLE);
        loadFragment(new HomeFragment());
    }

    public space.krokodilich.ctt.ViewModel getViewModel() {
        return viewModel;
    }

    public void showLoginFragment() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, new LoginFragment())
            .addToBackStack(null)
            .commit();
    }

    public void showAuthFragmentAndHideBottomNav() {
        bottomNavigationView.setVisibility(View.GONE);
        loadFragment(new AuthFragment());
    }

    public void navigateToCreatePost() {
        loadFragment(new CreatePostFragment());
    }
}

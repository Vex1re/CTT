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
import android.view.animation.AnimationUtils;
import androidx.transition.TransitionInflater;
import androidx.transition.Transition;
import androidx.transition.Slide;
import androidx.transition.Fade;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import com.google.android.material.badge.BadgeDrawable;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

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

        // Настройка обработки клавиатуры
        setupKeyboardHandling();

        // Проверяем, есть ли сохраненный ID пользователя
        Long savedUserId = viewModel.getSavedUserId();

        if (savedUserId != null) {
            // Если ID найден, проверяем валидность пользователя на сервере
            viewModel.fetchUserById(savedUserId);
            viewModel.setCallback(new space.krokodilich.ctt.ViewModel.OnNetworkCallback() {
                @Override
                public void onSuccess() {
                    // Пользователь валиден, показываем основное содержимое
            showMainContent();
                }

                @Override
                public void onError(String error) {
                    // Пользователь не валиден или ошибка сети, очищаем данные и показываем экран аутентификации
                    Log.d("MainActivity", "User validation failed: " + error);
                    viewModel.clearUserId();
                    showAuthFragmentAndHideBottomNav();
                }
            });
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

    private void setupKeyboardHandling() {
        // Настройка флага для автоматического изменения размера окна при появлении клавиатуры
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        
        // Слушатель изменений размера окна для определения появления/скрытия клавиатуры
        View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Получаем текущий фрагмент
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof AuthFragment) {
                    // Если это фрагмент аутентификации, даем ему знать об изменениях клавиатуры
                    ((AuthFragment) currentFragment).onKeyboardVisibilityChanged();
                }
            }
        });
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        
        // Проверяем, не находится ли Activity в процессе уничтожения
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        // Проверяем состояние фрагмента
        if (fragmentManager.isStateSaved()) {
            return;
        }
        
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Если это фрагмент авторизации или регистрации, скрываем нижнюю навигацию
        if (fragment instanceof AuthFragment || fragment instanceof LoginFragment || fragment instanceof RegisterFragment) {
            bottomNavigationView.setVisibility(View.GONE);
            transaction.replace(R.id.fragment_container, fragment)
                      .commit();
        } else if (fragment instanceof FullscreenImageFragment) {
            // Скрываем нижнюю навигацию с анимацией для просмотра изображений
            bottomNavigationView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> bottomNavigationView.setVisibility(View.GONE))
                .start();
            
            // Добавляем переход для фрагмента
            Transition enterTransition = TransitionInflater.from(this)
                .inflateTransition(R.transition.slide_up);
            fragment.setEnterTransition(enterTransition);
            
            // Сохраняем текущий фрагмент в стеке
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
            transaction.add(R.id.fragment_container, fragment)
                      .addToBackStack(null)
                      .commit();
        } else {
            // Для основных фрагментов (Home, Notifications, Profile)
            // показываем нижнюю навигацию
            if (bottomNavigationView.getVisibility() != View.VISIBLE) {
                bottomNavigationView.setVisibility(View.VISIBLE);
                bottomNavigationView.setAlpha(0f);
                bottomNavigationView.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
            }
            
            transaction.replace(R.id.fragment_container, fragment)
                      .commit();
        }
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        
        // Проверяем состояние
        if (isFinishing() || isDestroyed() || fragmentManager.isStateSaved()) {
            return;
        }
        
        bottomNavigationView.setVisibility(View.GONE);
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, new AuthFragment())
            .commit();
    }

    public void navigateToCreatePost() {
        loadFragment(new CreatePostFragment());
    }

    public void navigateToUserProfile(String login) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        
        // Проверяем состояние
        if (isFinishing() || isDestroyed() || fragmentManager.isStateSaved()) {
            return;
        }
        
        UserProfileFragment fragment = UserProfileFragment.newInstance(login);
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit();

        // Сохраняем видимость нижней навигации
        if (bottomNavigationView.getVisibility() == View.VISIBLE) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            // Если возвращаемся с FullscreenImageFragment
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof FullscreenImageFragment) {
                // Добавляем переход для исчезновения
                Transition exitTransition = TransitionInflater.from(this)
                    .inflateTransition(R.transition.slide_down);
                currentFragment.setExitTransition(exitTransition);
                
                // Показываем нижнюю навигацию с анимацией
                bottomNavigationView.setVisibility(View.VISIBLE);
                bottomNavigationView.setAlpha(0f);
                bottomNavigationView.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
            }
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public String getCurrentUserLogin() {
        User currentUser = viewModel.getCurrentUser();
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public void showError(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }
    
    public void updateNotificationBadge(int count) {
        if (bottomNavigationView != null) {
            MenuItem notificationItem = bottomNavigationView.getMenu().findItem(R.id.navigation_notifications);
            if (notificationItem != null) {
                // Получаем BadgeDrawable для элемента меню
                BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(notificationItem.getItemId());
                
                if (count > 0) {
                    badge.setVisible(true);
                    badge.setNumber(count);
                } else {
                    badge.setVisible(false);
                }
            }
        }
    }
    
    public void clearNotificationHistory() {
        // Очищаем историю прочитанных уведомлений
        space.krokodilich.ctt.Notification.clearReadNotifications(this);
    }
}

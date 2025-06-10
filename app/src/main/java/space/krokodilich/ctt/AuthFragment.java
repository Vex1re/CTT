package space.krokodilich.ctt;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

public class AuthFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private AuthPagerAdapter pagerAdapter;
    private View headerContainer;
    private MaterialCardView authCard;
    
    // Переменные для анимации
    private boolean isExpanded = false;
    private float originalHeaderMarginTop;
    private float expandedHeaderMarginTop = 0f; // Верхняя часть будет подниматься вверх
    private static final int ANIMATION_DURATION = 300;
    private int originalCardMarginTop; // Исходный отступ карточки
    private int originalCardHeight = 0;
    private boolean isCardExpanded = false;
    private float expandedCardRadius = 32f; // dp
    private float collapsedCardRadius = 20f; // dp

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация элементов
        viewPager = view.findViewById(R.id.auth_viewpager);
        tabLayout = view.findViewById(R.id.auth_tabs);
        headerContainer = view.findViewById(R.id.header_container);
        authCard = view.findViewById(R.id.auth_card);

        // Настройка ViewPager и TabLayout
        pagerAdapter = new AuthPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Вход" : "Регистрация")
        ).attach();

        // Инициализация анимации
        setupAnimation();
        
        // Запуск анимаций
        startAnimations();
    }

    private void setupAnimation() {
        // Получаем исходный отступ заголовка
        headerContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                originalHeaderMarginTop = ((ViewGroup.MarginLayoutParams) headerContainer.getLayoutParams()).topMargin;
                originalCardMarginTop = ((ViewGroup.MarginLayoutParams) authCard.getLayoutParams()).topMargin;
                
                // Небольшая задержка для корректного получения размеров
                headerContainer.post(() -> {
                    // Настраиваем слушатели фокуса для полей ввода
                    setupFocusListeners();
                });
                
                headerContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setupFocusListeners() {
        // Слушаем изменения в ViewPager для настройки фокусов
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // Небольшая задержка для загрузки фрагмента
                viewPager.post(() -> {
                    setupInputFieldFocusListeners();
                });
            }
        });
        
        // Настройка фокусов для начального состояния
        viewPager.post(() -> {
            setupInputFieldFocusListeners();
        });
    }

    private void setupInputFieldFocusListeners() {
        // Находим текущий фрагмент
        Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        
        if (currentFragment != null) {
            View fragmentView = currentFragment.getView();
            if (fragmentView != null) {
                // Находим все поля ввода в текущем фрагменте
                findAndSetupInputFields(fragmentView);
            }
        }
    }

    private void findAndSetupInputFields(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                findAndSetupInputFields(viewGroup.getChildAt(i));
            }
        } else if (view instanceof TextInputEditText || view instanceof EditText || view instanceof android.widget.AutoCompleteTextView) {
            setupInputFieldFocus((EditText) view);
        }
    }

    private void setupInputFieldFocus(EditText editText) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                expandCard();
            } else {
                if (!hasAnyInputFieldFocus()) {
                    collapseCard();
                }
            }
        });
    }

    private boolean hasAnyInputFieldFocus() {
        Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentFragment != null) {
            View fragmentView = currentFragment.getView();
            if (fragmentView != null) {
                return findFocusedInputField(fragmentView);
            }
        }
        return false;
    }

    private boolean findFocusedInputField(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                if (findFocusedInputField(viewGroup.getChildAt(i))) {
                    return true;
                }
            }
        } else if (view instanceof TextInputEditText || view instanceof EditText || view instanceof android.widget.AutoCompleteTextView) {
            return view.hasFocus();
        }
        return false;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void animateCardRadius(float from, float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(350);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            authCard.setRadius((float) animation.getAnimatedValue());
        });
        animator.start();
    }

    private void expandCard() {
        if (isCardExpanded) return;
        isCardExpanded = true;
        authCard.post(() -> {
            if (originalCardHeight == 0) {
                originalCardHeight = authCard.getHeight();
            }
            int targetHeight = ((View) authCard.getParent()).getHeight(); // match_parent
            animateCardHeight(authCard, authCard.getHeight(), targetHeight);
            animateCardRadius(authCard.getRadius(), dpToPx(expandedCardRadius));
        });
    }

    private void collapseCard() {
        if (!isCardExpanded) return;
        isCardExpanded = false;
        if (originalCardHeight > 0) {
            animateCardHeight(authCard, authCard.getHeight(), originalCardHeight);
            animateCardRadius(authCard.getRadius(), dpToPx(collapsedCardRadius));
        }
    }

    private void animateCardHeight(View view, int from, int to) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(350);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            view.setLayoutParams(params);
        });
        animator.start();
    }

    public void onKeyboardVisibilityChanged() {
        // Этот метод вызывается при изменении видимости клавиатуры
        // Можно использовать для дополнительной логики, если потребуется
    }

    private void startAnimations() {
        // Анимация для заголовка
        ObjectAnimator headerFadeIn = ObjectAnimator.ofFloat(headerContainer, "alpha", 0f, 1f);
        headerFadeIn.setDuration(800);
        headerFadeIn.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator headerSlideUp = ObjectAnimator.ofFloat(headerContainer, "translationY", -100f, 0f);
        headerSlideUp.setDuration(800);
        headerSlideUp.setInterpolator(new DecelerateInterpolator());

        // Анимация для карточки
        ObjectAnimator cardFadeIn = ObjectAnimator.ofFloat(authCard, "alpha", 0f, 1f);
        cardFadeIn.setDuration(600);
        cardFadeIn.setStartDelay(300);

        ObjectAnimator cardSlideUp = ObjectAnimator.ofFloat(authCard, "translationY", 200f, 0f);
        cardSlideUp.setDuration(600);
        cardSlideUp.setStartDelay(300);
        cardSlideUp.setInterpolator(new DecelerateInterpolator());

        // Запуск анимаций
        AnimatorSet headerAnimSet = new AnimatorSet();
        headerAnimSet.playTogether(headerFadeIn, headerSlideUp);

        AnimatorSet cardAnimSet = new AnimatorSet();
        cardAnimSet.playTogether(cardFadeIn, cardSlideUp);

        headerAnimSet.start();
        cardAnimSet.start();
    }
} 
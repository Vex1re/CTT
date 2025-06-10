package space.krokodilich.ctt;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment implements ViewModel.OnNetworkCallback {
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private MaterialButton loginButton;
    private MaterialCardView loginFormCard;
    private ViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        initializeViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
            viewModel.setCallback(this);
        }

        setupLoginButton();
        startAnimations();

        MaterialButton forgotPasswordButton = view.findViewById(R.id.forgot_password_button);
        forgotPasswordButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Для смены пароля обратитесь к администратору", Toast.LENGTH_LONG).show();
        });
    }

    private void initializeViews(View view) {
        emailInput = view.findViewById(R.id.login_email);
        passwordInput = view.findViewById(R.id.login_password);
        emailLayout = view.findViewById(R.id.login_email_layout);
        passwordLayout = view.findViewById(R.id.login_password_layout);
        loginButton = view.findViewById(R.id.login_button);
        loginFormCard = view.findViewById(R.id.login_form_card);
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> {
            if (validateInputs()) {
                animateButtonPress();
                String loginOrEmail = emailInput.getText() != null ? emailInput.getText().toString() : "";
                String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

                if (viewModel != null) {
                    viewModel.checkUserExists(loginOrEmail, password, false);
                } else {
                    Toast.makeText(getContext(), "Ошибка инициализации сервиса", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startAnimations() {
        // Анимация появления карточки
        loginFormCard.setAlpha(0f);
        loginFormCard.setTranslationY(50f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(loginFormCard, "alpha", 0f, 1f);
        fadeIn.setDuration(600);
        fadeIn.setStartDelay(200);

        ObjectAnimator slideUp = ObjectAnimator.ofFloat(loginFormCard, "translationY", 50f, 0f);
        slideUp.setDuration(600);
        slideUp.setStartDelay(200);
        slideUp.setInterpolator(new DecelerateInterpolator());

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(fadeIn, slideUp);
        animSet.start();
    }

    private void animateButtonPress() {
        ObjectAnimator scaleDown = ObjectAnimator.ofFloat(loginButton, "scaleX", 1f, 0.95f);
        scaleDown.setDuration(100);

        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(loginButton, "scaleX", 0.95f, 1f);
        scaleUp.setDuration(100);
        scaleUp.setStartDelay(100);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playSequentially(scaleDown, scaleUp);
        animSet.start();
    }

    private void showFieldError(TextInputLayout layout, String error) {
        layout.setError(error);
        layout.setErrorEnabled(true);
        
        // Анимация встряхивания
        ObjectAnimator shake = ObjectAnimator.ofFloat(layout, "translationX", 0f, -10f, 10f, -10f, 10f, 0f);
        shake.setDuration(500);
        shake.start();
    }

    private void clearFieldError(TextInputLayout layout) {
        layout.setErrorEnabled(false);
        layout.setError(null);
    }

    private boolean validateInputs() {
        if (emailInput == null || passwordInput == null) {
            Toast.makeText(getContext(), "Ошибка инициализации полей ввода", Toast.LENGTH_SHORT).show();
            return false;
        }

        String loginOrEmail = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        boolean isValid = true;

        // Очищаем предыдущие ошибки
        clearFieldError(emailLayout);
        clearFieldError(passwordLayout);

        if (loginOrEmail.isEmpty()) {
            showFieldError(emailLayout, "Поле обязательно для заполнения");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginOrEmail).matches() && loginOrEmail.length() < 3) {
            showFieldError(emailLayout, "Введите корректный email или логин");
            isValid = false;
        }

        if (password.isEmpty()) {
            showFieldError(passwordLayout, "Поле обязательно для заполнения");
            isValid = false;
        } else if (password.length() < 6) {
            showFieldError(passwordLayout, "Пароль должен содержать минимум 6 символов");
            isValid = false;
        }

        if (!isValid) {
            // Анимация встряхивания всей карточки
            ObjectAnimator shake = ObjectAnimator.ofFloat(loginFormCard, "translationX", 0f, -5f, 5f, -5f, 5f, 0f);
            shake.setDuration(400);
            shake.start();
        }

        return isValid;
    }

    @Override
    public void onSuccess() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showMainContent();
        }
    }

    @Override
    public void onError(String error) {
        if (getContext() != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        }
    }
} 
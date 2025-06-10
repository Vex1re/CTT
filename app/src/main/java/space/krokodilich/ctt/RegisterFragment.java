package space.krokodilich.ctt;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import android.widget.AutoCompleteTextView;

public class RegisterFragment extends Fragment implements ViewModel.OnNetworkCallback {
    private TextInputEditText loginInput;
    private TextInputEditText nameInput;
    private TextInputEditText surnameInput;
    private TextInputEditText emailInput;
    private AutoCompleteTextView citySpinner;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private TextInputLayout loginLayout;
    private TextInputLayout nameLayout;
    private TextInputLayout surnameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout cityLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private MaterialCheckBox termsCheckbox;
    private MaterialCheckBox privacyCheckbox;
    private MaterialButton registerButton;
    private TextView loginLink;
    private MaterialCardView registerFormCard;
    private ViewModel viewModel;
    private ProgressBar progressBar;
    private static final String TAG = "RegisterFragment";
    private static final String[] CITIES = {
        "Москва",
        "Санкт-Петербург",
        "Новосибирск",
        "Екатеринбург",
        "Казань",
        "Нижний Новгород",
        "Самара",
        "Челябинск",
        "Омск",
        "Другой"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        
        // Инициализация ViewModel из MainActivity
        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
            viewModel.setCallback(this);
        } else {
            // Fallback если MainActivity недоступна
            viewModel = new ViewModel();
            viewModel.setCallback(this);
        }
        
        // Инициализация элементов интерфейса
        initializeViews(view);
        
        // Настройка AutoCompleteTextView для городов
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            CITIES
        );
        citySpinner.setAdapter(adapter);
        
        // Добавляем слушатель для поля "Другой"
        citySpinner.setOnItemClickListener((parent, view1, position, id) -> {
            if (position == CITIES.length - 1) { // Если выбран "Другой"
                // Показываем диалог для ввода другого города
                showCityInputDialog();
            }
        });
        
        // Настройка слушателей
        registerButton.setOnClickListener(v -> {
            if (validateInputs()) {
                registerUser();
            }
        });
        
        loginLink.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showLoginFragment();
            }
        });
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRegisterButton();
        startAnimations();
    }

    private void initializeViews(View view) {
        loginInput = view.findViewById(R.id.register_login);
        nameInput = view.findViewById(R.id.register_name);
        surnameInput = view.findViewById(R.id.register_surname);
        emailInput = view.findViewById(R.id.register_email);
        citySpinner = view.findViewById(R.id.register_city);
        passwordInput = view.findViewById(R.id.register_password);
        confirmPasswordInput = view.findViewById(R.id.register_confirm_password);
        
        loginLayout = view.findViewById(R.id.register_login_layout);
        nameLayout = view.findViewById(R.id.register_name_layout);
        surnameLayout = view.findViewById(R.id.register_surname_layout);
        emailLayout = view.findViewById(R.id.register_email_layout);
        cityLayout = view.findViewById(R.id.cityLayout);
        passwordLayout = view.findViewById(R.id.register_password_layout);
        confirmPasswordLayout = view.findViewById(R.id.register_confirm_password_layout);
        
        termsCheckbox = view.findViewById(R.id.terms_checkbox);
        privacyCheckbox = view.findViewById(R.id.privacy_checkbox);
        registerButton = view.findViewById(R.id.register_button);
        loginLink = view.findViewById(R.id.loginLink);
        registerFormCard = view.findViewById(R.id.register_form_card);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void startAnimations() {
        // Анимация появления карточки
        registerFormCard.setAlpha(0f);
        registerFormCard.setTranslationY(50f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(registerFormCard, "alpha", 0f, 1f);
        fadeIn.setDuration(600);
        fadeIn.setStartDelay(200);

        ObjectAnimator slideUp = ObjectAnimator.ofFloat(registerFormCard, "translationY", 50f, 0f);
        slideUp.setDuration(600);
        slideUp.setStartDelay(200);
        slideUp.setInterpolator(new DecelerateInterpolator());

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(fadeIn, slideUp);
        animSet.start();
    }

    private void animateButtonPress() {
        ObjectAnimator scaleDown = ObjectAnimator.ofFloat(registerButton, "scaleX", 1f, 0.95f);
        scaleDown.setDuration(100);

        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(registerButton, "scaleX", 0.95f, 1f);
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

    private void setupRegisterButton() {
        registerButton.setOnClickListener(v -> {
            if (validateInputs()) {
                animateButtonPress();
                registerUser();
            }
        });
    }

    private boolean validateInputs() {
        if (loginInput == null || nameInput == null || surnameInput == null || 
            emailInput == null || citySpinner == null || passwordInput == null || 
            confirmPasswordInput == null || termsCheckbox == null || privacyCheckbox == null) {
            Toast.makeText(getContext(), "Ошибка инициализации полей ввода", Toast.LENGTH_SHORT).show();
            return false;
        }

        String login = loginInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String surname = surnameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String city = citySpinner.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        boolean isValid = true;

        // Очищаем предыдущие ошибки
        clearFieldError(loginLayout);
        clearFieldError(nameLayout);
        clearFieldError(surnameLayout);
        clearFieldError(emailLayout);
        clearFieldError(cityLayout);
        clearFieldError(passwordLayout);
        clearFieldError(confirmPasswordLayout);

        if (login.isEmpty()) {
            showFieldError(loginLayout, "Поле обязательно для заполнения");
            isValid = false;
        } else if (!login.matches("[a-zA-Z]+")) {
            showFieldError(loginLayout, "Логин должен содержать только английские буквы");
            isValid = false;
        }

        if (name.isEmpty()) {
            showFieldError(nameLayout, "Поле обязательно для заполнения");
            isValid = false;
        }

        if (surname.isEmpty()) {
            showFieldError(surnameLayout, "Поле обязательно для заполнения");
            isValid = false;
        }

        if (email.isEmpty()) {
            showFieldError(emailLayout, "Поле обязательно для заполнения");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showFieldError(emailLayout, "Введите корректный email");
            isValid = false;
        }

        if (city.isEmpty()) {
            showFieldError(cityLayout, "Выберите город");
            isValid = false;
        }

        if (password.isEmpty()) {
            showFieldError(passwordLayout, "Поле обязательно для заполнения");
            isValid = false;
        } else if (password.length() < 6) {
            showFieldError(passwordLayout, "Пароль должен содержать минимум 6 символов");
            isValid = false;
        }

        if (confirmPassword.isEmpty()) {
            showFieldError(confirmPasswordLayout, "Поле обязательно для заполнения");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            showFieldError(confirmPasswordLayout, "Пароли не совпадают");
            isValid = false;
        }

        if (!termsCheckbox.isChecked()) {
            Toast.makeText(getContext(), "Необходимо согласие с условиями использования", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!privacyCheckbox.isChecked()) {
            Toast.makeText(getContext(), "Необходимо согласие на обработку персональных данных", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!isValid) {
            // Анимация встряхивания всей карточки
            ObjectAnimator shake = ObjectAnimator.ofFloat(registerFormCard, "translationX", 0f, -5f, 5f, -5f, 5f, 0f);
            shake.setDuration(400);
            shake.start();
        }

        return isValid;
    }

    private void registerUser() {
        Log.d(TAG, "=== REGISTER USER METHOD CALLED ===");
        
        if (loginInput == null || nameInput == null || surnameInput == null || 
            emailInput == null || citySpinner == null || passwordInput == null || 
            confirmPasswordInput == null || termsCheckbox == null || privacyCheckbox == null) {
            Log.e(TAG, "One or more input fields are null in registerUser()");
            Toast.makeText(getContext(), "Ошибка инициализации полей", Toast.LENGTH_SHORT).show();
            return;
        }

        // Отключаем кнопку регистрации, чтобы предотвратить повторные нажатия
        registerButton.setEnabled(false);
        Log.d(TAG, "Register button disabled");

        String login = loginInput.getText() != null ? loginInput.getText().toString() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        Log.d(TAG, "Registration data:");
        Log.d(TAG, "  - Login: '" + login + "'");
        Log.d(TAG, "  - Email: '" + email + "'");
        Log.d(TAG, "  - Password length: " + password.length());

        if (viewModel != null) {
            Log.d(TAG, "ViewModel is not null, calling checkUserExists()");
            // Проверяем уникальность логина
            viewModel.checkUserExists(login, email, true);
        } else {
            Log.e(TAG, "ViewModel is null!");
            Toast.makeText(getContext(), "Ошибка инициализации сервиса", Toast.LENGTH_SHORT).show();
            registerButton.setEnabled(true);
        }
    }

    public User getUserData() {
        Log.d(TAG, "=== GETTING USER DATA FROM REGISTER FRAGMENT ===");
        
        if (loginInput == null || nameInput == null || surnameInput == null || 
            emailInput == null || citySpinner == null || passwordInput == null) {
            Log.e(TAG, "One or more input fields are null:");
            Log.e(TAG, "  - loginInput: " + (loginInput != null ? "not null" : "null"));
            Log.e(TAG, "  - nameInput: " + (nameInput != null ? "not null" : "null"));
            Log.e(TAG, "  - surnameInput: " + (surnameInput != null ? "not null" : "null"));
            Log.e(TAG, "  - emailInput: " + (emailInput != null ? "not null" : "null"));
            Log.e(TAG, "  - citySpinner: " + (citySpinner != null ? "not null" : "null"));
            Log.e(TAG, "  - passwordInput: " + (passwordInput != null ? "not null" : "null"));
            return null;
        }
        
        String login = loginInput.getText() != null ? loginInput.getText().toString().trim() : "";
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String surname = surnameInput.getText() != null ? surnameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String city = citySpinner.getText() != null ? citySpinner.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";
        
        Log.d(TAG, "Retrieved field values:");
        Log.d(TAG, "  - Login: '" + login + "'");
        Log.d(TAG, "  - Name: '" + name + "'");
        Log.d(TAG, "  - Surname: '" + surname + "'");
        Log.d(TAG, "  - Email: '" + email + "'");
        Log.d(TAG, "  - City: '" + city + "'");
        Log.d(TAG, "  - Password length: " + password.length());
        
        if (login.isEmpty() || name.isEmpty() || surname.isEmpty() || 
            email.isEmpty() || city.isEmpty() || password.isEmpty()) {
            Log.e(TAG, "One or more fields are empty:");
            Log.e(TAG, "  - Login empty: " + login.isEmpty());
            Log.e(TAG, "  - Name empty: " + name.isEmpty());
            Log.e(TAG, "  - Surname empty: " + surname.isEmpty());
            Log.e(TAG, "  - Email empty: " + email.isEmpty());
            Log.e(TAG, "  - City empty: " + city.isEmpty());
            Log.e(TAG, "  - Password empty: " + password.isEmpty());
            return null;
        }
        
        User user = new User();
        user.setUsername(login);
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setCity(city);
        user.setPassword(password);
        
        Log.d(TAG, "=== USER OBJECT CREATED SUCCESSFULLY ===");
        Log.d(TAG, "Created user data: " + user.getUsername() + ", " + user.getEmail());
        return user;
    }

    @Override
    public void onSuccess() {
        Log.d(TAG, "=== REGISTRATION SUCCESS CALLBACK ===");
        Log.d(TAG, "Registration completed successfully");
        
        if (getActivity() instanceof MainActivity) {
            Log.d(TAG, "Navigating to main content");
            ((MainActivity) getActivity()).showMainContent();
        } else {
            Log.e(TAG, "Activity is not MainActivity");
        }
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "=== REGISTRATION ERROR CALLBACK ===");
        Log.e(TAG, "Registration error: " + error);
        
        if (getContext() != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        }
        // Включаем кнопку обратно в случае ошибки
        if (registerButton != null) {
            Log.d(TAG, "Re-enabling register button");
            registerButton.setEnabled(true);
        }
    }

    private void showCityInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Введите название города");

        final EditText input = new EditText(requireContext());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String cityName = input.getText().toString().trim();
            if (!cityName.isEmpty()) {
                citySpinner.setText(cityName);
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}

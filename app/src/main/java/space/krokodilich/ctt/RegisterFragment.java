package space.krokodilich.ctt;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class RegisterFragment extends Fragment implements ViewModel.OnNetworkCallback {
    private TextInputEditText loginInput;
    private TextInputEditText nameInput;
    private TextInputEditText surnameInput;
    private TextInputEditText emailInput;
    private MaterialAutoCompleteTextView citySpinner;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private MaterialCheckBox termsCheckbox;
    private MaterialCheckBox privacyCheckbox;
    private MaterialButton registerButton;
    private TextView loginLink;
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
        
        // Инициализация ViewModel
        viewModel = new ViewModel();
        viewModel.setCallback(this);
        
        // Инициализация элементов интерфейса
        loginInput = view.findViewById(R.id.register_login);
        nameInput = view.findViewById(R.id.register_name);
        surnameInput = view.findViewById(R.id.register_surname);
        emailInput = view.findViewById(R.id.register_email);
        citySpinner = view.findViewById(R.id.register_city);
        passwordInput = view.findViewById(R.id.register_password);
        confirmPasswordInput = view.findViewById(R.id.register_confirm_password);
        termsCheckbox = view.findViewById(R.id.terms_checkbox);
        privacyCheckbox = view.findViewById(R.id.privacy_checkbox);
        registerButton = view.findViewById(R.id.register_button);
        loginLink = view.findViewById(R.id.loginLink);
        progressBar = view.findViewById(R.id.progressBar);
        
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
        
        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
            viewModel.setCallback(this);
        }

        setupRegisterButton();
    }

    private void initializeViews(View view) {
        loginInput = view.findViewById(R.id.register_login);
        nameInput = view.findViewById(R.id.register_name);
        surnameInput = view.findViewById(R.id.register_surname);
        emailInput = view.findViewById(R.id.register_email);
        citySpinner = view.findViewById(R.id.register_city);
        passwordInput = view.findViewById(R.id.register_password);
        confirmPasswordInput = view.findViewById(R.id.register_confirm_password);
        termsCheckbox = view.findViewById(R.id.terms_checkbox);
        privacyCheckbox = view.findViewById(R.id.privacy_checkbox);
        registerButton = view.findViewById(R.id.register_button);
    }

    private void setupRegisterButton() {
        registerButton.setOnClickListener(v -> {
            if (loginInput == null || nameInput == null || surnameInput == null || 
                emailInput == null || citySpinner == null || passwordInput == null || 
                confirmPasswordInput == null || termsCheckbox == null || privacyCheckbox == null) {
                Toast.makeText(getContext(), "Ошибка инициализации полей", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validateInputs()) {
                return;
            }

            // Отключаем кнопку регистрации, чтобы предотвратить повторные нажатия
            registerButton.setEnabled(false);

            String login = loginInput.getText() != null ? loginInput.getText().toString() : "";
            String email = emailInput.getText() != null ? emailInput.getText().toString() : "";
            String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

            if (viewModel != null) {
                // Проверяем уникальность логина
                viewModel.checkUserExists(login, email, true);
            } else {
                Toast.makeText(getContext(), "Ошибка инициализации сервиса", Toast.LENGTH_SHORT).show();
                registerButton.setEnabled(true);
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

        if (login.isEmpty() || name.isEmpty() || surname.isEmpty() || 
            email.isEmpty() || city.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!login.matches("[a-zA-Z]+")) {
            Toast.makeText(getContext(), "Логин должен содержать только английские буквы", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Введите корректный email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(getContext(), "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!termsCheckbox.isChecked() || !privacyCheckbox.isChecked()) {
            Toast.makeText(getContext(), "Необходимо принять условия использования и политику конфиденциальности", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registerUser() {
        if (viewModel == null) {
            Toast.makeText(getContext(), "Ошибка инициализации ViewModel", Toast.LENGTH_SHORT).show();
            return;
        }

        // Отключаем кнопку сразу после нажатия
        registerButton.setEnabled(false);
        
        String login = loginInput.getText() != null ? loginInput.getText().toString() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString() : "";
        
        viewModel.checkUserExists(login, email, true);
    }

    @Override
    public void onSuccess() {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            // Отключаем кнопку после успешной регистрации
            if (registerButton != null) {
                registerButton.setEnabled(false);
            }
            
            // Создаем нового пользователя со всеми данными
            User newUser = new User(
                loginInput.getText().toString().trim(),
                nameInput.getText().toString().trim(),
                surnameInput.getText().toString().trim(),
                emailInput.getText().toString().trim(),
                citySpinner.getText().toString().trim(),
                passwordInput.getText().toString().trim()
            );
            
            // Логируем созданного пользователя перед регистрацией
            Log.d(TAG, "Created user object for registration: " + newUser.toString());

            // Регистрируем пользователя
            viewModel.register(newUser);
            
            // Переходим на главную страницу
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showMainContent();
            }
        });
    }

    @Override
    public void onError(String error) {
        if (getContext() != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            // Включаем кнопку регистрации обратно в случае ошибки
            registerButton.setEnabled(true);
        }
    }

    private void showCityInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Введите название города");
        
        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        
        builder.setPositiveButton("OK", (dialog, which) -> {
            String city = input.getText().toString();
            if (!city.isEmpty()) {
                // Обновляем список городов, добавляя новый город
                String[] currentCities = new String[citySpinner.getAdapter().getCount()];
                for (int i = 0; i < currentCities.length - 1; i++) {
                    currentCities[i] = (String) citySpinner.getAdapter().getItem(i);
                }
                currentCities[currentCities.length - 2] = city;
                currentCities[currentCities.length - 1] = "Другой";
                
                ArrayAdapter<String> newAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    currentCities
                );
                citySpinner.setAdapter(newAdapter);
                citySpinner.setText(city, false);
            }
        });
        
        builder.setNegativeButton("Отмена", (dialog, which) -> {
            citySpinner.setText(CITIES[0], false); // Возвращаемся к первому городу
            dialog.cancel();
        });
        
        builder.show();
    }
}

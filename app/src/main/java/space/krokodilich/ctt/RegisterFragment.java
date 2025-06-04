package space.krokodilich.ctt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterFragment extends Fragment {
    private TextInputEditText loginInput;
    private TextInputEditText nameInput;
    private TextInputEditText surnameInput;
    private TextInputEditText emailInput;
    private TextInputEditText cityInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private MaterialCheckBox termsCheckbox;
    private MaterialCheckBox privacyCheckbox;
    private MaterialButton registerButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRegisterButton();
    }

    private void initializeViews(View view) {
        loginInput = view.findViewById(R.id.register_login);
        nameInput = view.findViewById(R.id.register_name);
        surnameInput = view.findViewById(R.id.register_surname);
        emailInput = view.findViewById(R.id.register_email);
        cityInput = view.findViewById(R.id.register_city);
        passwordInput = view.findViewById(R.id.register_password);
        confirmPasswordInput = view.findViewById(R.id.register_confirm_password);
        termsCheckbox = view.findViewById(R.id.terms_checkbox);
        privacyCheckbox = view.findViewById(R.id.privacy_checkbox);
        registerButton = view.findViewById(R.id.register_button);
    }

    private void setupRegisterButton() {
        registerButton.setOnClickListener(v -> {
            if (!validateInputs()) {
                return;
            }

            // Временно отключаем проверку сервера
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showMainContent();
            }
        });
    }

    private boolean validateInputs() {
        if (loginInput.getText().toString().isEmpty() ||
            nameInput.getText().toString().isEmpty() ||
            surnameInput.getText().toString().isEmpty() ||
            emailInput.getText().toString().isEmpty() ||
            cityInput.getText().toString().isEmpty() ||
            passwordInput.getText().toString().isEmpty() ||
            confirmPasswordInput.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!passwordInput.getText().toString().equals(confirmPasswordInput.getText().toString())) {
            Toast.makeText(getContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!termsCheckbox.isChecked() || !privacyCheckbox.isChecked()) {
            Toast.makeText(getContext(), "Необходимо принять условия использования", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}

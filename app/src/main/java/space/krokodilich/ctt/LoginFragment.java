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
import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment implements ViewModel.OnNetworkCallback {
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
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
    }

    private void initializeViews(View view) {
        emailInput = view.findViewById(R.id.login_email);
        passwordInput = view.findViewById(R.id.login_password);
        loginButton = view.findViewById(R.id.login_button);
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> {
            if (validateInputs()) {
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

    private boolean validateInputs() {
        if (emailInput == null || passwordInput == null) {
            Toast.makeText(getContext(), "Ошибка инициализации полей ввода", Toast.LENGTH_SHORT).show();
            return false;
        }

        String loginOrEmail = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (loginOrEmail.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
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
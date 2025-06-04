package space.krokodilich.ctt;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterFragment extends Fragment implements ViewModel.OnNetworkCallback {
    private static final String TAG = "RegisterFragment";
    private EditText edName, edSurName, edEmail, edCity, edPassword, edConfPassword, edLogin;
    private Button register;
    private ViewModel viewModel;
    private CheckBox termsCheckbox;
    private CheckBox privacyCheckbox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
            viewModel.setCallback(this);
        } else {
            Log.e(TAG, "Activity is not MainActivity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        initializeViews(view);
        setupListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        edName = view.findViewById(R.id.register_name);
        edSurName = view.findViewById(R.id.register_surname);
        edEmail = view.findViewById(R.id.register_email);
        edCity = view.findViewById(R.id.register_city);
        edPassword = view.findViewById(R.id.register_password);
        edConfPassword = view.findViewById(R.id.register_confirm_password);
        edLogin = view.findViewById(R.id.register_login);
        register = view.findViewById(R.id.register_button);
        termsCheckbox = view.findViewById(R.id.terms_checkbox);
        privacyCheckbox = view.findViewById(R.id.privacy_checkbox);
    }

    private void setupListeners() {
        register.setOnClickListener(this::register);
    }

    private void register(View view) {
        if (!validateInputs()) {
            return;
        }

        String name = edName.getText().toString();
        String surname = edSurName.getText().toString();
        String city = edCity.getText().toString();
        String email = edEmail.getText().toString();
        String password = edPassword.getText().toString();
        String login = edLogin.getText().toString();

        User newUser = new User("1", name, surname, email, city, password, login);
        Log.d(TAG, "Attempting to register user: " + newUser.toString());
        viewModel.register(newUser);
    }

    private boolean validateInputs() {
        if (edName.getText().toString().isEmpty() ||
            edSurName.getText().toString().isEmpty() ||
            edEmail.getText().toString().isEmpty() ||
            edCity.getText().toString().isEmpty() ||
            edPassword.getText().toString().isEmpty() ||
            edLogin.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!edPassword.getText().toString().equals(edConfPassword.getText().toString())) {
            Toast.makeText(getContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!termsCheckbox.isChecked()) {
            Toast.makeText(getContext(), "Пожалуйста, примите условия использования", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!privacyCheckbox.isChecked()) {
            Toast.makeText(getContext(), "Пожалуйста, примите политику конфиденциальности", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onSuccess() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Регистрация успешна", Toast.LENGTH_SHORT).show();
                // Показываем BottomNavigationView и переходим на главный экран
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.showBottomNavigation();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                }
            });
        }
    }

    @Override
    public void onError(String error) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            });
        }
    }
}

package space.krokodilich.ctt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterFragment extends AppCompatActivity implements rgst {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        Button button = (Button) view.findViewById(R.id.register_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register(v);
            }
        });
        return view;
    }

    private EditText edName, edSurName,edEmail, edCity, edPassword, edConfPassword, edLogin;
    private DatabaseReference mDataBase;
    private String USER_KEY = "User";
    private void init(){
        edName = findViewById(R.id.register_name);
        edSurName = findViewById(R.id.register_surname);
        edEmail = findViewById(R.id.register_email);
        edCity = findViewById(R.id.register_city);
        edPassword = findViewById(R.id.register_password);
        edConfPassword = findViewById(R.id.register_confirm_password);
        edLogin = findViewById(R.id.register_login);
        mDataBase = FirebaseDatabase.getInstance().getReference("User");
    }

    @Override
    public void Register(View view){
            String id = mDataBase.getKey();
            String name = edName.getText().toString();
            String surname = edSurName.getText().toString();
            String city = edCity.getText().toString();
            String email = edEmail.getText().toString();
            String password = edPassword.getText().toString();
            String login = edLogin.getText().toString();
            User newUser = new User(id, name, surname, email, city, password, login);
            mDataBase.push().setValue(newUser);
            setContentView(R.layout.activity_main);
    }

}

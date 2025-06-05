package space.krokodilich.ctt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
// import androidx.lifecycle.ViewModel; // Удаляем импорт стандартного ViewModel
import space.krokodilich.ctt.ViewModel; // Импортируем ваш пользовательский ViewModel

public class ProfileFragment extends Fragment {

    private TextView nameTextView;
    private TextView cityTextView;
    private Button logoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Инициализация TextView для имени и города (по разметке есть только эти)
        nameTextView = view.findViewById(R.id.profile_name);
        cityTextView = view.findViewById(R.id.profile_location);
        logoutButton = view.findViewById(R.id.logout_button);

        // Загрузка данных пользователя (пример, нужно адаптировать под вашу логику)
        loadUserProfile();

        logoutButton.setOnClickListener(v -> {
            // Очистка данных сессии (например, SharedPreferences)
            clearUserData();

            // Переход на экран входа/регистрации
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showAuthFragmentAndHideBottomNav();
            }
        });

        return view;
    }

    private void loadUserProfile() {
        // Получаем текущего пользователя из ViewModel
        if (getActivity() instanceof MainActivity) {
            space.krokodilich.ctt.ViewModel viewModel = ((MainActivity) getActivity()).getViewModel();
            User currentUser = viewModel.getCurrentUser();

            if (currentUser != null) {
                // Отображаем полное имя (Имя + Фамилия)
                if (nameTextView != null) {
                    String fullName = currentUser.getName() + " " + currentUser.getSurname();
                    nameTextView.setText(fullName);
                }
                // Отображаем только название города
                if (cityTextView != null) {
                    cityTextView.setText(currentUser.getCity());
                }
                // Можно добавить отображение других полей, если они есть в разметке и классе User
                // Например, username, email, surname, если добавить для них TextView в fragment_profile.xml
            }
        }
    }

    private void clearUserData() {
        // Очистка данных пользователя через ViewModel
        if (getActivity() instanceof MainActivity) {
            space.krokodilich.ctt.ViewModel viewModel = ((MainActivity) getActivity()).getViewModel();
            viewModel.clearUserId();
            // Если нужно очистить другие данные, не связанные с ID пользователя в SharedPreferences, добавьте их здесь
        }
    }
}

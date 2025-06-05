package space.krokodilich.ctt;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfilePagerAdapter extends FragmentStateAdapter {
    public ProfilePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Возвращаем разные фрагменты для разных вкладок
        switch (position) {
            case 0:
                return new UserPostsFragment(); // Фрагмент для "Мои посты"
            case 1:
                // Здесь будет фрагмент для "Понравившиеся"
                return new Fragment(); // Пока возвращаем пустой фрагмент
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Количество вкладок: "Мои посты" и "Понравившиеся"
    }
} 
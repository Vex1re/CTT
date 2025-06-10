package space.krokodilich.ctt;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import android.util.Log;

public class ProfilePagerAdapter extends FragmentStateAdapter {
    private static final String TAG = "ProfilePagerAdapter";
    private String userLogin;
    private boolean isCurrentUser;

    public ProfilePagerAdapter(@NonNull Fragment fragment, String login, boolean isCurrentUser) {
        super(fragment);
        this.userLogin = login;
        this.isCurrentUser = isCurrentUser;
        Log.d(TAG, "ProfilePagerAdapter created - userLogin: " + login + ", isCurrentUser: " + isCurrentUser);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d(TAG, "Creating fragment for position: " + position + ", isCurrentUser: " + isCurrentUser);
        
        if (isCurrentUser) {
            // Для текущего пользователя показываем обе вкладки
            switch (position) {
                case 0:
                    Log.d(TAG, "Creating UserPostsFragment for current user");
                    return UserPostsFragment.newInstance(userLogin);
                case 1:
                    Log.d(TAG, "Creating LikedPostsFragment for current user");
                    return new LikedPostsFragment();
                default:
                    Log.e(TAG, "Invalid position: " + position);
                    throw new IllegalArgumentException("Invalid position: " + position);
            }
        } else {
            // Для других пользователей показываем только посты
            Log.d(TAG, "Creating UserPostsFragment for other user");
            return UserPostsFragment.newInstance(userLogin);
        }
    }

    @Override
    public int getItemCount() {
        int count = isCurrentUser ? 2 : 1;
        Log.d(TAG, "Item count: " + count + " (isCurrentUser: " + isCurrentUser + ")");
        return count;
    }
} 
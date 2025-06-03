package space.krokodilich.ctt;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    public int s = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null & s == 1) {

            BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
            bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        } else{
            setContentView(R.layout.fragment_register);
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = getFragment(item);

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                        return true;
                    }
                    return false;
                }

                @Nullable
                private Fragment getFragment(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    int itemId = item.getItemId();
                    if (itemId == R.id.navigation_home) {
                        selectedFragment = new HomeFragment();
                    } else if (itemId == R.id.navigation_notifications) {
                        selectedFragment = new NotificationsFragment();
                    } else if (itemId == R.id.navigation_profile) {
                        selectedFragment = new ProfileFragment();
                    }
                    return selectedFragment;
                }
            };
}

package space.krokodilich.ctt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.List;

public class FullscreenImageFragment extends Fragment {
    private static final String ARG_IMAGES = "images";
    private static final String ARG_INITIAL_POSITION = "initial_position";

    public static FullscreenImageFragment newInstance(List<String> images, int initialPosition) {
        FullscreenImageFragment fragment = new FullscreenImageFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_IMAGES, images.toArray(new String[0]));
        args.putInt(ARG_INITIAL_POSITION, initialPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fullscreen_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        TextView indicator = view.findViewById(R.id.image_position_indicator);

        Bundle args = getArguments();
        if (args != null) {
            String[] imageUrls = args.getStringArray(ARG_IMAGES);
            int initialPosition = args.getInt(ARG_INITIAL_POSITION, 0);

            if (imageUrls != null) {
                FullscreenImagePagerAdapter adapter = new FullscreenImagePagerAdapter(
                    List.of(imageUrls), initialPosition);
                viewPager.setAdapter(adapter);
                viewPager.setCurrentItem(initialPosition, false);

                // Индикатор позиции
                indicator.setText((initialPosition + 1) + "/" + imageUrls.length);
                viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        indicator.setText((position + 1) + "/" + imageUrls.length);
                    }
                });
            }
        }

        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }
} 
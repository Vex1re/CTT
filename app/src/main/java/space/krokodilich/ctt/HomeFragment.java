package space.krokodilich.ctt;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private EditText searchInput;
    private ChipGroup filterChipGroup;
    private Chip sortAscButton;
    private Chip sortDescButton;
    private List<Post> originalPosts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.posts_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter();
        recyclerView.setAdapter(postAdapter);

        searchInput = view.findViewById(R.id.search_input);
        filterChipGroup = view.findViewById(R.id.filter_chip_group);
        sortAscButton = view.findViewById(R.id.sort_asc_button);
        sortDescButton = view.findViewById(R.id.sort_desc_button);

        setupFilters();
        loadPosts();
        setupSearch();
        setupSorting();
    }

    private void setupFilters() {
        List<String> tags = Arrays.asList("Все", "Достопримечательности", "Музеи", "Рестораны", "Отели", "Развлечения", "Пейзаж", "Двор");
        List<String> cities = Arrays.asList("Все города", "Москва", "Санкт-Петербург", "Новосибирск", "Екатеринбург", "Казань", "Нижний Новгород", "Самара", "Челябинск", "Омск");
        
        // Добавляем фильтр по городам
        Chip cityFilterChip = new Chip(requireContext());
        cityFilterChip.setText("Город");
        cityFilterChip.setCheckable(true);
        cityFilterChip.setOnClickListener(v -> showCityFilterDialog(cities));
        filterChipGroup.addView(cityFilterChip);
        
        // Добавляем остальные теги
        for (String tag : tags) {
            Chip chip = new Chip(requireContext());
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    String selectedTag = tag.equals("Все") ? "" : tag;
                    postAdapter.filterPosts(searchInput.getText().toString(), selectedTag, "");
                }
            });
            filterChipGroup.addView(chip);
        }
    }

    private void showCityFilterDialog(List<String> cities) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Выберите город");

        final String[] cityArray = cities.toArray(new String[0]);
        builder.setItems(cityArray, (dialog, which) -> {
            String selectedCity = cityArray[which];
            if (selectedCity.equals("Все города")) {
                selectedCity = "";
            }
            postAdapter.filterPosts(searchInput.getText().toString(), "", selectedCity);
        });

        builder.show();
    }

    private void setupSearch() {
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchInput.getText().toString();
            String selectedTag = "";
            String selectedCity = "";
            
            for (int i = 0; i < filterChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) filterChipGroup.getChildAt(i);
                if (chip.isChecked()) {
                    selectedTag = chip.getText().toString();
                    if (selectedTag.equals("Все")) {
                        selectedTag = "";
                    }
                    break;
                }
            }
            
            postAdapter.filterPosts(query, selectedTag, selectedCity);
            return true;
        });
    }

    private void setupSorting() {
        sortAscButton.setOnClickListener(v -> {
            sortAscButton.setChecked(true);
            sortDescButton.setChecked(false);
            sortPosts(true);
        });

        sortDescButton.setOnClickListener(v -> {
            sortDescButton.setChecked(true);
            sortAscButton.setChecked(false);
            sortPosts(false);
        });
    }

    private void sortPosts(boolean ascending) {
        List<Post> currentPosts = new ArrayList<>(postAdapter.getPosts());
        Collections.sort(currentPosts, (p1, p2) -> {
            if (ascending) {
                return Integer.compare(p1.getRating(), p2.getRating());
            } else {
                return Integer.compare(p2.getRating(), p1.getRating());
            }
        });
        postAdapter.setPosts(currentPosts);
    }

    private void loadPosts() {
        if (getActivity() instanceof MainActivity) {
            space.krokodilich.ctt.ViewModel viewModel = ((MainActivity) getActivity()).getViewModel();
            viewModel.getPosts(new space.krokodilich.ctt.ViewModel.OnNetworkCallback() {
                @Override
                public void onSuccess() {
                    List<Post> posts = viewModel.getPosts();
                    if (posts != null) {
                        originalPosts = posts;
                        postAdapter.setPosts(posts);
                    }
                }

                @Override
                public void onError(String error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}

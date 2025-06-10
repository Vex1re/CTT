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
    private static final String KEY_SELECTED_CITY = "selected_city";
    private static final String KEY_SELECTED_TAG = "selected_tag";
    private static final String KEY_SEARCH_QUERY = "search_query";
    private static final String KEY_SORT_ASCENDING = "sort_ascending";

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private EditText searchInput;
    private ChipGroup filterChipGroup;
    private Chip sortAscButton;
    private Chip sortDescButton;
    private List<Post> originalPosts;
    private String selectedCity = "";
    private String selectedTag = "";
    private String searchQuery = "";
    private boolean sortAscending = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedCity = savedInstanceState.getString(KEY_SELECTED_CITY, "");
            selectedTag = savedInstanceState.getString(KEY_SELECTED_TAG, "");
            searchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY, "");
            sortAscending = savedInstanceState.getBoolean(KEY_SORT_ASCENDING, false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SELECTED_CITY, selectedCity);
        outState.putString(KEY_SELECTED_TAG, selectedTag);
        outState.putString(KEY_SEARCH_QUERY, searchInput != null ? searchInput.getText().toString() : searchQuery);
        outState.putBoolean(KEY_SORT_ASCENDING, sortAscButton != null ? sortAscButton.isChecked() : sortAscending);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Проверяем аутентификацию пользователя
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            space.krokodilich.ctt.ViewModel viewModel = mainActivity.getViewModel();
            User currentUser = viewModel.getCurrentUser();
            
            if (currentUser == null) {
                // Пользователь не авторизован, перенаправляем на экран входа
                mainActivity.showAuthFragmentAndHideBottomNav();
                return;
            }
        }

        recyclerView = view.findViewById(R.id.posts_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        space.krokodilich.ctt.ViewModel viewModel = ((MainActivity) getActivity()).getViewModel();
        postAdapter = new PostAdapter(requireContext(), new ArrayList<>(), viewModel, false);
        recyclerView.setAdapter(postAdapter);

        searchInput = view.findViewById(R.id.search_input);
        filterChipGroup = view.findViewById(R.id.filter_chip_group);
        sortAscButton = view.findViewById(R.id.sort_asc_button);
        sortDescButton = view.findViewById(R.id.sort_desc_button);

        setupFilters();
        loadPosts();
        setupSearch();
        setupSorting();

        // Восстанавливаем состояние фильтров
        if (!searchQuery.isEmpty()) {
            searchInput.setText(searchQuery);
        }
        if (sortAscending) {
            sortAscButton.setChecked(true);
            sortDescButton.setChecked(false);
        } else {
            sortAscButton.setChecked(false);
            sortDescButton.setChecked(true);
        }
    }

    private void setupFilters() {
        List<String> tags = Arrays.asList("Все", "Достопримечательность", "Ресторан", "Кафе", "Парк", "Музей");
        List<String> cities = Arrays.asList("Все города", "Москва", "Санкт-Петербург", "Новосибирск", "Екатеринбург", "Казань", "Нижний Новгород", "Самара", "Челябинск", "Омск");
        
        // Добавляем фильтр по городам
        Chip cityFilterChip = new Chip(requireContext());
        cityFilterChip.setText("Город");
        cityFilterChip.setCheckable(true);
        if (!selectedCity.isEmpty()) {
            cityFilterChip.setText("Город: " + selectedCity);
        }
        cityFilterChip.setOnClickListener(v -> showCityFilterDialog(cities));
        filterChipGroup.addView(cityFilterChip);
        
        // Добавляем остальные теги
        for (String tag : tags) {
            Chip chip = new Chip(requireContext());
            chip.setText(tag);
            chip.setCheckable(true);
            if (tag.equals(selectedTag) || (selectedTag.isEmpty() && tag.equals("Все"))) {
                chip.setChecked(true);
            }
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Сбрасываем выбор других тегов
                    for (int i = 0; i < filterChipGroup.getChildCount(); i++) {
                        Chip otherChip = (Chip) filterChipGroup.getChildAt(i);
                        if (otherChip != chip && !otherChip.getText().equals("Город")) {
                            otherChip.setChecked(false);
                        }
                    }
                    selectedTag = tag.equals("Все") ? "" : tag;
                    applyFilters();
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
            selectedCity = cityArray[which].equals("Все города") ? "" : cityArray[which];
            Chip cityChip = (Chip) filterChipGroup.getChildAt(0);
            cityChip.setText(selectedCity.isEmpty() ? "Город" : "Город: " + selectedCity);
            applyFilters();
        });

        builder.show();
    }

    private void applyFilters() {
        String currentQuery = searchInput != null ? searchInput.getText().toString() : searchQuery;
        postAdapter.filterPosts(currentQuery, selectedTag, selectedCity);
    }

    private void setupSearch() {
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            searchQuery = searchInput.getText().toString();
            applyFilters();
            return true;
        });
    }

    private void setupSorting() {
        sortAscButton.setOnClickListener(v -> {
            sortAscButton.setChecked(true);
            sortDescButton.setChecked(false);
            sortAscending = true;
            sortPosts(true);
        });

        sortDescButton.setOnClickListener(v -> {
            sortDescButton.setChecked(true);
            sortAscButton.setChecked(false);
            sortAscending = false;
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
                        // Применяем сохраненные фильтры после загрузки постов
                        applyFilters();
                        if (sortAscending) {
                            sortPosts(true);
                        } else {
                            sortPosts(false);
                        }
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

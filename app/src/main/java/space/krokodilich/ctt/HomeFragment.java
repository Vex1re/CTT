package space.krokodilich.ctt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
        loadSamplePosts();
        setupSearch();
        setupSorting();
    }

    private void setupFilters() {
        List<String> tags = Arrays.asList("Все", "Достопримечательности", "Музеи", "Рестораны", "Отели", "Развлечения");
        
        for (String tag : tags) {
            Chip chip = new Chip(requireContext());
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    String selectedTag = tag.equals("Все") ? "" : tag;
                    postAdapter.filterPosts(searchInput.getText().toString(), selectedTag);
                }
            });
            filterChipGroup.addView(chip);
        }
    }

    private void setupSearch() {
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchInput.getText().toString();
            String selectedTag = "";
            
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
            
            postAdapter.filterPosts(query, selectedTag);
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

    private void loadSamplePosts() {
        originalPosts = new ArrayList<>();
        
        originalPosts.add(new Post(
            "Иван Петров",
            "https://example.com/avatar1.jpg",
            "Москва",
            "2 часа назад",
            "https://example.com/red_square.jpg",
            "Красная площадь",
            "Достопримечательности",
            "Красная площадь - главная площадь Москвы, расположенная в центре города...",
            42,
            15
        ));

        originalPosts.add(new Post(
            "Мария Иванова",
            "https://example.com/avatar2.jpg",
            "Санкт-Петербург",
            "5 часов назад",
            "https://example.com/hermitage.jpg",
            "Эрмитаж",
            "Музеи",
            "Эрмитаж - один из крупнейших музеев мира, расположенный в Санкт-Петербурге...",
            38,
            12
        ));

        postAdapter.setPosts(originalPosts);
    }
}

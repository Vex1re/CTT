package space.krokodilich.ctt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.appbar.MaterialToolbar;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CreatePostFragment extends Fragment {
    private TextInputEditText placeNameInput;
    private MaterialAutoCompleteTextView placeCityInput;
    private TextInputEditText placeDescriptionInput;
    private ChipGroup tagChipGroup;
    private MaterialButton publishButton;
    private MaterialToolbar toolbar;
    private ViewModel viewModel;

    private MaterialCardView addFirstImagePlaceholder;
    private LinearLayout addedImagesContainer;
    private RecyclerView imagesRecyclerView;
    private MaterialButton addImageButton;
    private ImageAdapter imageAdapter;
    private List<Uri> selectedImageUris = new ArrayList<>();

    private ActivityResultLauncher<String> selectImageLauncher;

    private static final int MAX_IMAGES = 5;

    private static final String[] CITIES = {
        "Москва",
        "Санкт-Петербург",
        "Новосибирск",
        "Екатеринбург",
        "Казань",
        "Нижний Новгород",
        "Самара",
        "Челябинск",
        "Омск",
        "Другой"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация ViewModel
        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
        }

        // Инициализация UI элементов
        placeNameInput = view.findViewById(R.id.place_name_input);
        placeCityInput = view.findViewById(R.id.place_city_input);
        placeDescriptionInput = view.findViewById(R.id.place_description_input);
        tagChipGroup = view.findViewById(R.id.tag_chip_group);
        publishButton = view.findViewById(R.id.publish_button);
        toolbar = view.findViewById(R.id.toolbar);

        addFirstImagePlaceholder = view.findViewById(R.id.add_first_image_placeholder);
        addedImagesContainer = view.findViewById(R.id.added_images_container);
        imagesRecyclerView = view.findViewById(R.id.images_recycler_view);
        addImageButton = view.findViewById(R.id.add_image_button);

        // Настройка AutoCompleteTextView для городов
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            CITIES
        );
        placeCityInput.setAdapter(adapter);

        // Добавляем слушатель для поля "Другой"
        placeCityInput.setOnItemClickListener((parent, view1, position, id) -> {
            if (position == CITIES.length - 1) { // Если выбран "Другой"
                // Показываем диалог для ввода другого города
                showCityInputDialog();
            }
        });

        // Настройка RecyclerView для изображений
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(selectedImageUris);
        imagesRecyclerView.setAdapter(imageAdapter);

        // Настройка ActivityResultLauncher для выбора изображения
        selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && selectedImageUris.size() < MAX_IMAGES) {
                    selectedImageUris.add(uri);
                    imageAdapter.addImageUri(uri);
                    updateImageUI();
                } else if (selectedImageUris.size() >= MAX_IMAGES) {
                    Toast.makeText(getContext(), "Выбрано максимальное количество фотографий", Toast.LENGTH_SHORT).show();
                }
            }
        );

        // Настройка обработчиков
        setupListeners();
        setupImageListeners();
        updateImageUI(); // Изначальное обновление UI изображений
    }

    private void setupImageListeners() {
        addFirstImagePlaceholder.setOnClickListener(v -> selectImage());
        addImageButton.setOnClickListener(v -> selectImage());
    }

    private void selectImage() {
        selectImageLauncher.launch("image/*"); // Запускаем выбор изображений
    }

    private void updateImageUI() {
        if (selectedImageUris.isEmpty()) {
            addFirstImagePlaceholder.setVisibility(View.VISIBLE);
            addedImagesContainer.setVisibility(View.GONE);
        } else {
            addFirstImagePlaceholder.setVisibility(View.GONE);
            addedImagesContainer.setVisibility(View.VISIBLE);

            // Скрываем кнопку добавления, если достигнут лимит
            if (selectedImageUris.size() >= MAX_IMAGES) {
                addImageButton.setVisibility(View.GONE);
            } else {
                addImageButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showCityInputDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Введите город");

        final TextInputEditText input = new TextInputEditText(requireContext());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String city = input.getText().toString().trim();
            if (!city.isEmpty()) {
                placeCityInput.setText(city);
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void setupListeners() {
        // Обработчик кнопки закрытия
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Обработчик кнопки публикации
        publishButton.setOnClickListener(v -> {
            if (validateInputs()) {
                createPost();
            }
        });
    }

    private boolean validateInputs() {
        String placeName = placeNameInput.getText() != null ? placeNameInput.getText().toString().trim() : "";
        String city = placeCityInput.getText() != null ? placeCityInput.getText().toString().trim() : "";
        String description = placeDescriptionInput.getText() != null ? placeDescriptionInput.getText().toString().trim() : "";
        int selectedChipId = tagChipGroup.getCheckedChipId();

        if (placeName.isEmpty()) {
            placeNameInput.setError("Введите название места");
            return false;
        }

        if (city.isEmpty()) {
            placeCityInput.setError("Выберите город");
            return false;
        }

        if (description.isEmpty()) {
            placeDescriptionInput.setError("Введите описание места");
            return false;
        }

        if (selectedChipId == View.NO_ID) {
            Toast.makeText(getContext(), "Выберите категорию", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Проверка наличия хотя бы одной фотографии
        if (selectedImageUris.isEmpty()) {
             Toast.makeText(getContext(), "Пожалуйста, добавьте хотя бы одну фотографию.", Toast.LENGTH_SHORT).show();
             return false;
        }

        return true;
    }

    private void createPost() {
        String placeName = placeNameInput.getText().toString().trim();
        String city = placeCityInput.getText().toString().trim();
        String description = placeDescriptionInput.getText().toString().trim();
        Chip selectedChip = tagChipGroup.findViewById(tagChipGroup.getCheckedChipId());
        String tag = selectedChip != null ? selectedChip.getText().toString() : "";

        if (viewModel != null && viewModel.getCurrentUser() != null) {
            User currentUser = viewModel.getCurrentUser();
            
            // Получаем текущую дату в формате дд.мм.гггг
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            // Получаем список URI изображений в виде строк
            List<String> imageUrls = selectedImageUris.stream()
                                                .map(Uri::toString)
                                                .collect(Collectors.toList());

            // Показываем прогресс
            publishButton.setEnabled(false);
            publishButton.setText("Публикация...");

            // Сначала получаем текущий список постов, чтобы определить следующий ID
            viewModel.getPosts(new ViewModel.OnNetworkCallback() {
                @Override
                public void onSuccess() {
                    List<Post> currentPosts = viewModel.getPosts();
                    // Формируем полное имя пользователя (имя + фамилия)
                    String fullName = currentUser.getName() + " " + currentUser.getSurname();
                    String loginnn = currentUser.getUsername();

                    // Создаем новый пост с определенным ID, включая список изображений
                    Post newPost = new Post(
                        null, // Передаем null для id, чтобы сервер сгенерировал его
                        fullName, // имя и фамилия автора через пробел
                        city, // город
                        currentDate, // текущая дата в формате дд.мм.гггг
                        description, // описание
                        0, // рейтинг (по умолчанию 0)
                        tag, // тэг
                        0, // количество комментариев (по умолчанию 0)
                        placeName, // название места
                        loginnn, // логин пользователя
                        imageUrls // список URL изображений
                    );

                    // Отправляем пост на сервер
                    viewModel.createPost(newPost, new ViewModel.OnNetworkCallback() {
                        @Override
                        public void onSuccess() {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Пост успешно опубликован", Toast.LENGTH_SHORT).show();
                                    
                                    // Обновляем список постов
                                    viewModel.getPosts(new ViewModel.OnNetworkCallback() {
                                        @Override
                                        public void onSuccess() {
                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> {
                                                    // Возвращаемся на главную страницу
                                                    if (getActivity() instanceof MainActivity) {
                                                        ((MainActivity) getActivity()).loadFragment(new HomeFragment());
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onError(String error) {
                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> {
                                                    Toast.makeText(getContext(), "Ошибка при обновлении списка постов: " + error, Toast.LENGTH_LONG).show();
                                                    // Даже при ошибке обновления возвращаемся на главную
                                                    if (getActivity() instanceof MainActivity) {
                                                        ((MainActivity) getActivity()).loadFragment(new HomeFragment());
                                                    }
                                                });
                                            }
                                        }
                                    });
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                                    publishButton.setEnabled(true);
                                    publishButton.setText("Опубликовать");
                                });
                            }
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Ошибка при получении списка постов для определения ID: " + error, Toast.LENGTH_LONG).show();
                            publishButton.setEnabled(true);
                            publishButton.setText("Опубликовать");
                        });
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
        }
    }
} 
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
import android.util.Log;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.content.ClipData;
import android.os.Build;
import android.app.Activity;
import android.provider.MediaStore;
import android.database.Cursor;
import java.io.File;

public class CreatePostFragment extends Fragment implements ImageAdapter.OnImageRemoveListener {
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

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PICK_IMAGES_REQUEST = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
        }

        placeNameInput = view.findViewById(R.id.place_name_input);
        placeCityInput = view.findViewById(R.id.place_city_input);
        placeDescriptionInput = view.findViewById(R.id.place_description_input);
        tagChipGroup = view.findViewById(R.id.tag_chip_group);
        publishButton = view.findViewById(R.id.publish_button);
        toolbar = view.findViewById(R.id.toolbar);

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

        // Настройка ChipGroup для тегов
        tagChipGroup.setSingleSelection(true);
        tagChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip != null) {
                // Можно добавить дополнительную логику при выборе тега
                Log.d("CreatePostFragment", "Selected tag: " + selectedChip.getText());
            }
        });

        // Инициализация компонентов для работы с изображениями
        addFirstImagePlaceholder = view.findViewById(R.id.add_first_image_placeholder);
        addedImagesContainer = view.findViewById(R.id.added_images_container);
        imagesRecyclerView = view.findViewById(R.id.images_recycler_view);
        addImageButton = view.findViewById(R.id.add_image_button);

        // Настройка RecyclerView для изображений
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(this);
        imagesRecyclerView.setAdapter(imageAdapter);

        // Настройка выбора изображений
        selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    // Ограничиваем количество изображений
                    int remainingSlots = MAX_IMAGES - selectedImageUris.size();
                    if (remainingSlots > 0) {
                        List<Uri> newUris = uris.subList(0, Math.min(remainingSlots, uris.size()));
                        selectedImageUris.addAll(newUris);
                        updateImagesUI();
                    }
                }
            }
        );

        // Настройка кнопки добавления изображения
        addImageButton.setOnClickListener(v -> {
            if (selectedImageUris.size() < MAX_IMAGES) {
                openImagePicker();
            } else {
                Toast.makeText(getContext(), "Максимальное количество изображений: " + MAX_IMAGES, Toast.LENGTH_SHORT).show();
            }
        });

        // Настройка плейсхолдера для первого изображения
        addFirstImagePlaceholder.setOnClickListener(v -> {
            if (selectedImageUris.size() < MAX_IMAGES) {
                openImagePicker();
            }
        });

        // Настройка кнопки публикации
        publishButton.setOnClickListener(v -> createPost());

        // Настройка toolbar
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new ProfileFragment());
            }
        });
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

    private void updateImagesUI() {
        if (selectedImageUris.isEmpty()) {
            imagesRecyclerView.setVisibility(View.GONE);
            addedImagesContainer.setVisibility(View.GONE);
            addFirstImagePlaceholder.setVisibility(View.VISIBLE);
        } else {
            imagesRecyclerView.setVisibility(View.VISIBLE);
            addedImagesContainer.setVisibility(View.VISIBLE);
            addFirstImagePlaceholder.setVisibility(View.GONE);
            imageAdapter.setImages(selectedImageUris);
        }
    }

    @Override
    public void onImageRemove(int position) {
        selectedImageUris.remove(position);
        updateImagesUI();
    }

    private void createPost() {
        String title = placeNameInput.getText().toString().trim();
        String content = placeDescriptionInput.getText().toString().trim();
        String city = placeCityInput.getText().toString().trim();
        Chip selectedChip = tagChipGroup.findViewById(tagChipGroup.getCheckedChipId());
        String tag = selectedChip != null ? selectedChip.getText().toString() : "";

        if (title.isEmpty() || content.isEmpty() || city.isEmpty()) {
            Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUris.isEmpty()) {
            Toast.makeText(getContext(), "Добавьте хотя бы одну фотографию", Toast.LENGTH_SHORT).show();
            return;
        }

        if (viewModel != null && viewModel.getCurrentUser() != null) {
            User currentUser = viewModel.getCurrentUser();
            
            // Получаем текущую дату в формате дд.мм.гггг
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            // Показываем прогресс
            publishButton.setEnabled(false);
            publishButton.setText("Публикация...");

            // Создаем новый пост
            Post newPost = new Post(
                null, // ID будет присвоен сервером
                currentUser.getName() + " " + currentUser.getSurname(),
                city,
                currentDate,
                content,
                0, // Начальный рейтинг
                tag, // Передаем тег
                0, // Начальное количество комментариев
                title,
                currentUser.getUsername(),
                "[]" // Пустой JSON массив для images
            );

            // Отправляем пост на сервер
            viewModel.createPost(newPost, new ViewModel.OnNetworkCallback() {
                @Override
                public void onSuccess() {
                    if (getActivity() == null) return;
                    
                    // После создания поста загружаем изображения
                    if (!selectedImageUris.isEmpty()) {
                        publishButton.setText("Загрузка изображений...");
                        // Получаем созданный пост из ViewModel
                        Post createdPost = viewModel.getPosts().get(0); // Пост добавляется в начало списка
                        if (createdPost != null && createdPost.getId() != null) {
                            viewModel.uploadImages(createdPost.getId(), selectedImageUris, new ViewModel.OnNetworkCallback() {
                                @Override
                                public void onSuccess() {
                                    if (getActivity() == null) return;
                                    getActivity().runOnUiThread(() -> {
                                        publishButton.setEnabled(true);
                                        publishButton.setText("Опубликовать");
                                        if (getActivity() instanceof MainActivity) {
                                            ((MainActivity) getActivity()).loadFragment(new ProfileFragment());
                                        }
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    if (getActivity() == null) return;
                                    getActivity().runOnUiThread(() -> {
                                        publishButton.setEnabled(true);
                                        publishButton.setText("Опубликовать");
                                        if (getActivity() instanceof MainActivity) {
                                            ((MainActivity) getActivity()).loadFragment(new ProfileFragment());
                                        }
                                    });
                                }
                            });
                        } else {
                            if (getActivity() == null) return;
                            getActivity().runOnUiThread(() -> {
                                publishButton.setEnabled(true);
                                publishButton.setText("Опубликовать");
                                if (getActivity() instanceof MainActivity) {
                                    ((MainActivity) getActivity()).loadFragment(new ProfileFragment());
                                }
                            });
                        }
                    } else {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            publishButton.setEnabled(true);
                            publishButton.setText("Опубликовать");
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).loadFragment(new ProfileFragment());
                            }
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        publishButton.setEnabled(true);
                        publishButton.setText("Опубликовать");
                    });
                }
            });
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }

    private void openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
                return;
            }
        } else {
            if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        selectImageLauncher.launch("image/*");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение получено, открываем галерею
                selectImageLauncher.launch("image/*");
            } else {
                Toast.makeText(requireContext(), 
                        "Для загрузки изображений необходимо разрешение на доступ к галерее", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }
} 
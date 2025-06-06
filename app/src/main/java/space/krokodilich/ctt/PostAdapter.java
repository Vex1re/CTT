package space.krokodilich.ctt;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private static final String TAG = "ImageDebug";

    private List<Post> posts;
    private List<Post> filteredPosts;

    public PostAdapter() {
        this.posts = new ArrayList<>();
        this.filteredPosts = new ArrayList<>();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = filteredPosts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return filteredPosts.size();
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        this.filteredPosts = new ArrayList<>(posts);
        notifyDataSetChanged();
    }

    public List<Post> getPosts() {
        return filteredPosts;
    }

    public void filterPosts(String query, String tag, String city) {
        filteredPosts.clear();
        for (Post post : posts) {
            boolean matchesQuery = query.isEmpty() ||
                    post.getPlaceName().toLowerCase().contains(query.toLowerCase()) ||
                    post.getDescription().toLowerCase().contains(query.toLowerCase());
            boolean matchesTag = tag.isEmpty() || post.getPlaceTag().equals(tag);
            boolean matchesCity = city.isEmpty() || post.getLocation().equals(city);

            if (matchesQuery && matchesTag && matchesCity) {
                filteredPosts.add(post);
            }
        }
        notifyDataSetChanged();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView authorName;
        private TextView location;
        private TextView time;
        private TextView placeName;
        private TextView placeTag;
        private TextView description;
        private TextView ratingValue;
        private TextView commentsCount;
        private RecyclerView imagesRecyclerView;
        private PostImageAdapter imageAdapter;
        private MaterialButton upvoteButton;
        private MaterialButton downvoteButton;
        private ViewModel viewModel;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            authorName = itemView.findViewById(R.id.author_name);
            location = itemView.findViewById(R.id.post_location);
            time = itemView.findViewById(R.id.post_time);
            placeName = itemView.findViewById(R.id.place_name);
            placeTag = itemView.findViewById(R.id.place_tag);
            description = itemView.findViewById(R.id.place_description);
            ratingValue = itemView.findViewById(R.id.rating_value);
            commentsCount = itemView.findViewById(R.id.comments_button);
            imagesRecyclerView = itemView.findViewById(R.id.post_images_recyclerview);
            upvoteButton = itemView.findViewById(R.id.upvote_button);
            downvoteButton = itemView.findViewById(R.id.downvote_button);

            // Setup images RecyclerView
            imagesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            imageAdapter = new PostImageAdapter(new ArrayList<>());
            imagesRecyclerView.setAdapter(imageAdapter);

            // Получаем ViewModel
            if (itemView.getContext() instanceof MainActivity) {
                viewModel = ((MainActivity) itemView.getContext()).getViewModel();
            }
        }

        public void bind(Post post) {
            authorName.setText(post.getAuthorName());
            location.setText(post.getLocation());
            time.setText(post.getTime());
            placeName.setText(post.getPlaceName());
            placeTag.setText(post.getPlaceTag());
            description.setText(post.getDescription());
            ratingValue.setText(String.valueOf(post.getRating()));
            commentsCount.setText(post.getCommentsCount() + " комментариев");

            // Обновляем состояние кнопок рейтинга
            updateRatingButtons(post);

            // Настраиваем обработчики нажатий для кнопок рейтинга
            setupRatingButtons(post);

            // Set images for the images RecyclerView
            List<String> imageUrls = post.getImagesList();
            Log.d(TAG, "Binding post with image URLs: " + imageUrls);
            if (imageUrls != null && !imageUrls.isEmpty()) {
                imageAdapter.setImageUrls(imageUrls);
                imagesRecyclerView.setVisibility(View.VISIBLE);
            } else {
                imageAdapter.setImageUrls(new ArrayList<>()); // Clear previous images
                imagesRecyclerView.setVisibility(View.GONE);
            }
        }

        private void updateRatingButtons(Post post) {
            // Сбрасываем цвет кнопок
            upvoteButton.setIconTint(null);
            downvoteButton.setIconTint(null);

            // Устанавливаем цвет в зависимости от текущей оценки
            if (post.getUserRating() == 1) {
                upvoteButton.setIconTintResource(android.R.color.holo_blue_dark);
            } else if (post.getUserRating() == -1) {
                downvoteButton.setIconTintResource(android.R.color.holo_red_dark);
            }
        }

        private void setupRatingButtons(Post post) {
            if (viewModel == null || viewModel.getCurrentUser() == null) {
                upvoteButton.setEnabled(false);
                downvoteButton.setEnabled(false);
                return;
            }

            String currentUserLogin = viewModel.getCurrentUser().getUsername();

            upvoteButton.setOnClickListener(v -> {
                int newRating = post.getUserRating() == 1 ? 0 : 1;
                updatePostRating(post, newRating);
            });

            downvoteButton.setOnClickListener(v -> {
                int newRating = post.getUserRating() == -1 ? 0 : -1;
                updatePostRating(post, newRating);
            });
        }

        private void updatePostRating(Post post, int newRating) {
            if (viewModel == null || viewModel.getCurrentUser() == null) return;

            String currentUserLogin = viewModel.getCurrentUser().getUsername();
            PostRating postRating = new PostRating(post.getId(), currentUserLogin, newRating);

            // Отправляем оценку на сервер
            viewModel.updatePostRating(postRating, new ViewModel.OnNetworkCallback() {
                @Override
                public void onSuccess() {
                    // Обновляем рейтинг поста
                    int ratingChange = newRating - post.getUserRating();
                    post.setRating(post.getRating() + ratingChange);
                    post.setUserRating(newRating);
                    
                    // Обновляем UI
                    if (itemView.getContext() != null) {
                        ((MainActivity) itemView.getContext()).runOnUiThread(() -> {
                            ratingValue.setText(String.valueOf(post.getRating()));
                            updateRatingButtons(post);
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (itemView.getContext() != null) {
                        ((MainActivity) itemView.getContext()).runOnUiThread(() -> {
                            Toast.makeText(itemView.getContext(), 
                                "Ошибка при обновлении рейтинга: " + error, 
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }
    }
} 
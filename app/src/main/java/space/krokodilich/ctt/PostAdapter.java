package space.krokodilich.ctt;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private static final String TAG = "ImageDebug";

    private List<Post> posts;
    private List<Post> filteredPosts;
    private List<Post> originalPosts;
    private final UserRatingPreferences ratingPreferences;
    private final Context context;
    private final ViewModel viewModel;
    private final boolean isUserPosts;

    public PostAdapter(Context context, List<Post> posts, ViewModel viewModel, boolean isUserPosts) {
        this.context = context;
        this.posts = posts;
        this.originalPosts = new ArrayList<>(posts);
        this.viewModel = viewModel;
        this.ratingPreferences = new UserRatingPreferences(context);
        this.filteredPosts = new ArrayList<>(posts);
        this.isUserPosts = isUserPosts;
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
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void setPosts(List<Post> newPosts) {
        this.posts = newPosts;
        this.originalPosts = new ArrayList<>(newPosts);
        this.filteredPosts = new ArrayList<>(newPosts);
        notifyDataSetChanged();
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void filterPosts(String query, String tag, String city) {
        posts.clear();
        for (Post post : originalPosts) {
            boolean matchesQuery = query.isEmpty() ||
                    post.getPlaceName().toLowerCase().contains(query.toLowerCase()) ||
                    post.getDescription().toLowerCase().contains(query.toLowerCase());
            boolean matchesTag = tag.isEmpty() || (post.getPlaceTag() != null && post.getPlaceTag().equals(tag));
            boolean matchesCity = city.isEmpty() || (post.getLocation() != null && post.getLocation().equals(city));

            if (matchesQuery && matchesTag && matchesCity) {
                posts.add(post);
            }
        }
        notifyDataSetChanged();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final TextView ratingValue;
        private final TextView authorTextView;
        private final TextView dateTextView;
        private final TextView cityTextView;
        private final TextView tagTextView;
        private final RecyclerView imageRecyclerView;
        private final PostImageAdapter imageAdapter;
        private final MaterialButton upvoteButton;
        private final MaterialButton downvoteButton;
        private final RatingPreferences ratingPreferences;
        private final ImageButton deletePostButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.place_name);
            descriptionTextView = itemView.findViewById(R.id.place_description);
            ratingValue = itemView.findViewById(R.id.rating_value);
            authorTextView = itemView.findViewById(R.id.author_name);
            dateTextView = itemView.findViewById(R.id.post_time);
            cityTextView = itemView.findViewById(R.id.post_location);
            tagTextView = itemView.findViewById(R.id.place_tag);
            imageRecyclerView = itemView.findViewById(R.id.post_images_recyclerview);
            upvoteButton = itemView.findViewById(R.id.upvote_button);
            downvoteButton = itemView.findViewById(R.id.downvote_button);
            deletePostButton = itemView.findViewById(R.id.delete_post_button);
            ratingPreferences = new RatingPreferences(context);

            imageAdapter = new PostImageAdapter(new ArrayList<>());
            imageRecyclerView.setAdapter(imageAdapter);
            imageRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }

        public void bind(Post post) {
            titleTextView.setText(post.getPlaceName());
            descriptionTextView.setText(post.getDescription());
            ratingValue.setText(String.valueOf(post.getRating()));
            authorTextView.setText(post.getAuthorName());
            dateTextView.setText(post.getTime());
            cityTextView.setText(post.getLocation());
            tagTextView.setText(post.getPlaceTag());

            // Показываем кнопку удаления только в разделе "Мои посты"
            String currentUserLogin = viewModel.getCurrentUser() != null ? viewModel.getCurrentUser().getUsername() : null;
            if (isUserPosts && currentUserLogin != null && currentUserLogin.equals(post.getLogin())) {
                deletePostButton.setVisibility(View.VISIBLE);
                deletePostButton.setOnClickListener(v -> {
                    new AlertDialog.Builder(context)
                        .setTitle("Удаление поста")
                        .setMessage("Вы уверены, что хотите удалить этот пост?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            viewModel.deletePost(post.getId(), new ViewModel.OnNetworkCallback() {
                                @Override
                                public void onSuccess() {
                                    int position = getAdapterPosition();
                                    if (position != RecyclerView.NO_POSITION) {
                                        posts.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, posts.size());
                                    }
                                }

                                @Override
                                public void onError(String error) {
                                    if (context != null) {
                                        Toast.makeText(context, "Ошибка при удалении поста: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
                });
            } else {
                deletePostButton.setVisibility(View.GONE);
            }

            // Загружаем изображения
            List<String> imageUrls = post.getImagesList();
            imageAdapter.setImages(imageUrls);
            imageRecyclerView.setVisibility(imageUrls.isEmpty() ? View.GONE : View.VISIBLE);

            // Проверяем текущую реакцию пользователя
            String userLogin = viewModel.getCurrentUser() != null ? viewModel.getCurrentUser().getUsername() : null;
            if (userLogin != null) {
                if (post.hasUserLiked(userLogin)) {
                    post.setUserRating(1);
                } else if (post.hasUserDisliked(userLogin)) {
                    post.setUserRating(-1);
                } else {
                    post.setUserRating(0);
                }
            }

            // Обновляем состояние кнопок
            updateRatingButtons(post);

            // Настраиваем обработчики нажатий
            upvoteButton.setOnClickListener(v -> {
                if (userLogin == null) {
                    Toast.makeText(context, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
                    return;
                }

                int currentRating = post.getRating();
                int currentUserRating = post.getUserRating();
                int newUserRating = currentUserRating == 1 ? 0 : 1;
                
                Log.d(TAG, "Upvote clicked for post " + post.getId() + 
                    ", current rating: " + currentRating + 
                    ", current user rating: " + currentUserRating + 
                    ", new user rating: " + newUserRating);
                
                // Сразу обновляем UI
                post.setUserRating(newUserRating);
                // Корректно обновляем общий рейтинг
                if (currentUserRating == 1) {
                    post.setRating(currentRating - 1); // Убираем лайк
                } else if (currentUserRating == -1) {
                    post.setRating(currentRating + 2); // Меняем дизлайк на лайк
                } else {
                    post.setRating(currentRating + 1); // Добавляем лайк
                }
                updateRatingButtons(post);
                
                // Отправляем на сервер информацию о реакции
                PostRating postRating = new PostRating(post.getId(), userLogin, newUserRating == 1 ? true : null);
                viewModel.updatePostRating(postRating, new ViewModel.OnNetworkCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully updated rating for post " + post.getId());
                        // Обновляем локальные настройки
                        if (newUserRating == 0) {
                            ratingPreferences.clearRating(String.valueOf(post.getId()));
                        } else {
                            ratingPreferences.saveRating(String.valueOf(post.getId()), newUserRating);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error updating rating for post " + post.getId() + ": " + error);
                        // В случае ошибки возвращаем предыдущее состояние
                        post.setUserRating(currentUserRating);
                        post.setRating(currentRating);
                        updateRatingButtons(post);
                        Toast.makeText(context, "Ошибка при обновлении рейтинга", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            downvoteButton.setOnClickListener(v -> {
                if (userLogin == null) {
                    Toast.makeText(context, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
                    return;
                }

                int currentRating = post.getRating();
                int currentUserRating = post.getUserRating();
                int newUserRating = currentUserRating == -1 ? 0 : -1;
                
                Log.d(TAG, "Downvote clicked for post " + post.getId() + 
                    ", current rating: " + currentRating + 
                    ", current user rating: " + currentUserRating + 
                    ", new user rating: " + newUserRating);
                
                // Сразу обновляем UI
                post.setUserRating(newUserRating);
                // Корректно обновляем общий рейтинг
                if (currentUserRating == -1) {
                    post.setRating(currentRating + 1); // Убираем дизлайк
                } else if (currentUserRating == 1) {
                    post.setRating(currentRating - 2); // Меняем лайк на дизлайк
                } else {
                    post.setRating(currentRating - 1); // Добавляем дизлайк
                }
                updateRatingButtons(post);
                
                // Отправляем на сервер информацию о реакции
                PostRating postRating = new PostRating(post.getId(), userLogin, newUserRating == -1 ? false : null);
                viewModel.updatePostRating(postRating, new ViewModel.OnNetworkCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully updated rating for post " + post.getId());
                        // Обновляем локальные настройки
                        if (newUserRating == 0) {
                            ratingPreferences.clearRating(String.valueOf(post.getId()));
                        } else {
                            ratingPreferences.saveRating(String.valueOf(post.getId()), newUserRating);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error updating rating for post " + post.getId() + ": " + error);
                        // В случае ошибки возвращаем предыдущее состояние
                        post.setUserRating(currentUserRating);
                        post.setRating(currentRating);
                        updateRatingButtons(post);
                        Toast.makeText(context, "Ошибка при обновлении рейтинга", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        private void updateRatingButtons(Post post) {
            int userRating = post.getUserRating();
            Log.d(TAG, "Updating rating buttons for post " + post.getId() + ", user rating: " + userRating);
            
            // Обновляем цвет и состояние кнопки лайка
            if (userRating > 0) {
                upvoteButton.setBackgroundTintList(ColorStateList.valueOf(
                    itemView.getContext().getResources().getColor(R.color.upvote_active)));
                upvoteButton.setAlpha(1.0f);
                downvoteButton.setBackgroundTintList(ColorStateList.valueOf(
                    itemView.getContext().getResources().getColor(android.R.color.white)));
                downvoteButton.setAlpha(0.5f);
            } else if (userRating < 0) {
                downvoteButton.setBackgroundTintList(ColorStateList.valueOf(
                    itemView.getContext().getResources().getColor(R.color.downvote_active)));
                downvoteButton.setAlpha(1.0f);
                upvoteButton.setBackgroundTintList(ColorStateList.valueOf(
                    itemView.getContext().getResources().getColor(android.R.color.white)));
                upvoteButton.setAlpha(0.5f);
            } else {
                upvoteButton.setBackgroundTintList(ColorStateList.valueOf(
                    itemView.getContext().getResources().getColor(android.R.color.white)));
                downvoteButton.setBackgroundTintList(ColorStateList.valueOf(
                    itemView.getContext().getResources().getColor(android.R.color.white)));
                upvoteButton.setAlpha(0.5f);
                downvoteButton.setAlpha(0.5f);
            }

            // Обновляем отображение рейтинга
                    ratingValue.setText(String.valueOf(post.getRating()));
        }
    }
} 
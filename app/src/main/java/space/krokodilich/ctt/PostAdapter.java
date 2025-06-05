package space.krokodilich.ctt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
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
        }
    }
} 
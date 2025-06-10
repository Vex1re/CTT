package space.krokodilich.ctt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import android.util.Log;
import space.krokodilich.ctt.LikedPostAdapter;

public class LikedPostsFragment extends Fragment {
    private static final String TAG = "LikedPostsFragment";
    private RecyclerView likedPostsRecyclerView;
    private ProgressBar loadingIndicator;
    private TextView noPostsMessage;
    private LikedPostAdapter postAdapter;
    private ViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liked_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
        }

        likedPostsRecyclerView = view.findViewById(R.id.liked_posts_recyclerview);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        noPostsMessage = view.findViewById(R.id.no_posts_message);

        likedPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new LikedPostAdapter(requireContext(), new ArrayList<>(), viewModel);
        likedPostsRecyclerView.setAdapter(postAdapter);

        loadLikedPosts();
    }

    private void loadLikedPosts() {
        if (viewModel != null) {
            loadingIndicator.setVisibility(View.VISIBLE);
            viewModel.getPosts(new ViewModel.OnNetworkCallback() {
                @Override
                public void onSuccess() {
                    List<Post> allPosts = viewModel.getPosts();
                    if (allPosts != null && !allPosts.isEmpty()) {
                        // Фильтруем посты, которые пользователь лайкнул
                        List<Post> likedPosts = new ArrayList<>();
                        String userLogin = viewModel.getCurrentUser() != null ? 
                            viewModel.getCurrentUser().getUsername() : null;
                        
                        for (Post post : allPosts) {
                            if (userLogin != null && post.hasUserLiked(userLogin)) {
                                likedPosts.add(post);
                            }
                        }

                        Log.d(TAG, "Found " + likedPosts.size() + " liked posts");

                        if (likedPosts.isEmpty()) {
                            noPostsMessage.setVisibility(View.VISIBLE);
                            likedPostsRecyclerView.setVisibility(View.GONE);
                        } else {
                            noPostsMessage.setVisibility(View.GONE);
                            likedPostsRecyclerView.setVisibility(View.VISIBLE);
                            postAdapter.setPosts(likedPosts);
                        }
                    } else {
                        Log.d(TAG, "No posts available");
                        noPostsMessage.setVisibility(View.VISIBLE);
                        likedPostsRecyclerView.setVisibility(View.GONE);
                    }
                    loadingIndicator.setVisibility(View.GONE);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error loading posts: " + error);
                    loadingIndicator.setVisibility(View.GONE);
                    noPostsMessage.setVisibility(View.VISIBLE);
                    noPostsMessage.setText("Ошибка при загрузке постов: " + error);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLikedPosts(); // Обновляем список при возвращении на экран
    }
} 
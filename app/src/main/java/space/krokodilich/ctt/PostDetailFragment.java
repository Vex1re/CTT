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
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;
import space.krokodilich.ctt.NotificationsFragment;

public class PostDetailFragment extends Fragment {
    private static final String ARG_POST_ID = "post_id";
    private static final String ARG_SOURCE_FRAGMENT = "source_fragment";
    private Long postId;
    private String sourceFragment;
    private ViewModel viewModel;
    private RecyclerView postRecyclerView;
    private ProgressBar loadingIndicator;
    private TextView errorTextView;
    private PostAdapter postAdapter;

    public static PostDetailFragment newInstance(Long postId) {
        return newInstance(postId, null);
    }

    public static PostDetailFragment newInstance(Long postId, String sourceFragment) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_POST_ID, postId);
        if (sourceFragment != null) {
            args.putString(ARG_SOURCE_FRAGMENT, sourceFragment);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getLong(ARG_POST_ID);
            sourceFragment = getArguments().getString(ARG_SOURCE_FRAGMENT);
        }
        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (sourceFragment != null && sourceFragment.equals("notifications")) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new NotificationsFragment());
                }
            } else {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        postRecyclerView = view.findViewById(R.id.post_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        errorTextView = view.findViewById(R.id.error_text_view);

        postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter(getContext(), new ArrayList<>(), viewModel, false);
        postRecyclerView.setAdapter(postAdapter);

        loadPost();

        return view;
    }

    private void loadPost() {
        if (viewModel != null) {
            loadingIndicator.setVisibility(View.VISIBLE);
            postRecyclerView.setVisibility(View.GONE);
            errorTextView.setVisibility(View.GONE);

            viewModel.getPost(postId, new ViewModel.OnNetworkCallback() {
                @Override
                public void onSuccess() {
                    if (getActivity() == null) return;
                    
                    getActivity().runOnUiThread(() -> {
                        Post post = viewModel.getPostById(postId);
                        if (post != null) {
                            List<Post> posts = new ArrayList<>();
                            posts.add(post);
                            postAdapter.setPosts(posts);
                            postRecyclerView.setVisibility(View.VISIBLE);
                            loadingIndicator.setVisibility(View.GONE);
                            errorTextView.setVisibility(View.GONE);
                        } else {
                            showError("Пост не найден");
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        showError("Ошибка загрузки поста: " + error);
                    });
                }
            });
        }
    }

    private void showError(String message) {
        loadingIndicator.setVisibility(View.GONE);
        postRecyclerView.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(message);
    }
} 
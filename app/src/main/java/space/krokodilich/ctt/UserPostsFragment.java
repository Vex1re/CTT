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

public class UserPostsFragment extends Fragment implements ViewModel.OnNetworkCallback {

    private static final String ARG_USER_LOGIN = "user_login";
    private String userLogin;
    private RecyclerView userPostsRecyclerView;
    private ProgressBar loadingIndicator;
    private TextView noPostsMessage;
    private PostAdapter postAdapter;
    private ViewModel viewModel;

    public static UserPostsFragment newInstance(String login) {
        UserPostsFragment fragment = new UserPostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_LOGIN, login);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userLogin = getArguments().getString(ARG_USER_LOGIN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
        }

        userPostsRecyclerView = view.findViewById(R.id.user_posts_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        noPostsMessage = view.findViewById(R.id.no_posts_message);

        userPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Фильтруем посты только текущего пользователя
        List<Post> userPosts = new ArrayList<>();
        if (viewModel != null && viewModel.getPosts() != null) {
            userPosts = viewModel.getPosts().stream()
                .filter(post -> userLogin.equals(post.getLogin()))
                .collect(Collectors.toList());
        }

        // Создаем адаптер с isUserPosts = true только если это посты текущего пользователя
        boolean isCurrentUser = viewModel.getCurrentUser() != null && 
                               viewModel.getCurrentUser().getUsername().equals(userLogin);
        postAdapter = new PostAdapter(getContext(), userPosts, viewModel, isCurrentUser);
        userPostsRecyclerView.setAdapter(postAdapter);

        loadUserPosts();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Обновляем список постов при возвращении на экран
        if (viewModel != null && viewModel.getPosts() != null) {
            List<Post> userPosts = viewModel.getPosts().stream()
                .filter(post -> userLogin.equals(post.getLogin()))
                .collect(Collectors.toList());
            
            // Обновляем существующий адаптер вместо создания нового
            if (postAdapter != null) {
                postAdapter.setPosts(userPosts);
            } else {
                // Создаем новый адаптер только если его нет
                boolean isCurrentUser = viewModel.getCurrentUser() != null && 
                                       viewModel.getCurrentUser().getUsername().equals(userLogin);
                postAdapter = new PostAdapter(getContext(), userPosts, viewModel, isCurrentUser);
                userPostsRecyclerView.setAdapter(postAdapter);
            }
        }
    }

    private void loadUserPosts() {
        if (viewModel != null && viewModel.getCurrentUser() != null) {
            loadingIndicator.setVisibility(View.VISIBLE);
            userPostsRecyclerView.setVisibility(View.GONE);
            noPostsMessage.setVisibility(View.GONE);

            viewModel.getPosts(this);
        } else {
            loadingIndicator.setVisibility(View.GONE);
            userPostsRecyclerView.setVisibility(View.GONE);
            noPostsMessage.setVisibility(View.VISIBLE);
            noPostsMessage.setText("Пожалуйста, войдите в аккаунт для просмотра ваших постов.");
        }
    }

    @Override
    public void onSuccess() {
        loadingIndicator.setVisibility(View.GONE);

        if (viewModel != null) {
            List<Post> allPosts = viewModel.getPosts();

            if (allPosts != null) {
                List<Post> userPosts = allPosts.stream()
                        .filter(post -> userLogin.equals(post.getLogin()))
                        .collect(Collectors.toList());

                if (!userPosts.isEmpty()) {
                    postAdapter.setPosts(userPosts);
                    userPostsRecyclerView.setVisibility(View.VISIBLE);
                    noPostsMessage.setVisibility(View.GONE);
                } else {
                    userPostsRecyclerView.setVisibility(View.GONE);
                    noPostsMessage.setVisibility(View.VISIBLE);
                    noPostsMessage.setText("У пользователя пока нет опубликованных постов.");
                }
            } else {
                // Если список постов пуст или null
                userPostsRecyclerView.setVisibility(View.GONE);
                noPostsMessage.setVisibility(View.VISIBLE);
                noPostsMessage.setText("Ошибка загрузки постов или постов нет.");
            }
        } else {
            // Если ViewModel недоступен
            userPostsRecyclerView.setVisibility(View.GONE);
            noPostsMessage.setVisibility(View.VISIBLE);
            noPostsMessage.setText("Ошибка загрузки данных.");
        }
    }

    @Override
    public void onError(String error) {
        // Скрываем индикатор загрузки и показываем сообщение об ошибке
        loadingIndicator.setVisibility(View.GONE);
        userPostsRecyclerView.setVisibility(View.GONE);
        noPostsMessage.setVisibility(View.VISIBLE);
        noPostsMessage.setText("Ошибка при загрузке постов: " + error);
    }
} 
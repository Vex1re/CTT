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

    private RecyclerView userPostsRecyclerView;
    private ProgressBar loadingIndicator;
    private TextView noPostsMessage;
    private PostAdapter postAdapter;
    private ViewModel viewModel;

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
        postAdapter = new PostAdapter(requireContext(), new ArrayList<>(), viewModel, true);
        userPostsRecyclerView.setAdapter(postAdapter);

        loadUserPosts();
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
        // Скрываем индикатор загрузки
        loadingIndicator.setVisibility(View.GONE);

        if (viewModel != null && viewModel.getCurrentUser() != null) {
            String currentUserLogin = viewModel.getCurrentUser().getUsername();
            List<Post> allPosts = viewModel.getPosts();

            if (allPosts != null) {
                // Фильтруем посты по логину текущего пользователя
                List<Post> userPosts = allPosts.stream()
                        .filter(post -> currentUserLogin.equals(post.getLogin()))
                        .collect(Collectors.toList());

                if (!userPosts.isEmpty()) {
                    // Отображаем отфильтрованные посты
                    postAdapter.setPosts(userPosts); // Используем метод setPosts
                    userPostsRecyclerView.setVisibility(View.VISIBLE);
                    noPostsMessage.setVisibility(View.GONE);
                } else {
                    // Показываем сообщение, если постов нет
                    userPostsRecyclerView.setVisibility(View.GONE);
                    noPostsMessage.setVisibility(View.VISIBLE);
                    noPostsMessage.setText("У вас пока нет опубликованных постов.");
                }
            } else {
                // Если список постов пуст или null
                 userPostsRecyclerView.setVisibility(View.GONE);
                 noPostsMessage.setVisibility(View.VISIBLE);
                 noPostsMessage.setText("Ошибка загрузки постов или постов нет.");
            }
        } else {
             // Если после загрузки пользователь оказался неавторизован (маловероятно)
             userPostsRecyclerView.setVisibility(View.GONE);
             noPostsMessage.setVisibility(View.VISIBLE);
             noPostsMessage.setText("Пожалуйста, войдите в аккаунт для просмотра ваших постов.");
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
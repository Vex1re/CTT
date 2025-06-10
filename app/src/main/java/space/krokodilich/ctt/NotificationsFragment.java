package space.krokodilich.ctt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import space.krokodilich.ctt.PostDetailFragment;

public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView emptyView;
    private ViewModel viewModel;
    private List<Notification> notifications;
    private SimpleDateFormat dateFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
        }
        notifications = new ArrayList<>();
        dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", new Locale("ru"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        
        recyclerView = view.findViewById(R.id.notifications_recycler_view);
        emptyView = view.findViewById(R.id.empty_notifications_view);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notifications, this);
        recyclerView.setAdapter(adapter);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Проверяем аутентификацию пользователя
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            viewModel = mainActivity.getViewModel();
            User currentUser = viewModel.getCurrentUser();
            
            if (currentUser == null) {
                // Пользователь не авторизован, перенаправляем на экран входа
                mainActivity.showAuthFragmentAndHideBottomNav();
                return;
            }
        }
        
        loadNotifications();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        if (viewModel != null && viewModel.getCurrentUser() != null) {
            String currentUserLogin = viewModel.getCurrentUser().getUsername();
            List<Post> allPosts = viewModel.getPosts();
            if (allPosts != null) {
                notifications.clear();
                try {
                    // Получаем все посты текущего пользователя (без stream/toList)
                    List<Post> userPosts = new ArrayList<>();
                    for (Post post : allPosts) {
                        if (currentUserLogin.equals(post.getLogin())) {
                            userPosts.add(post);
                        }
                    }
                    for (Post post : userPosts) {
                        List<String> reactions;
                        try {
                            reactions = post.getLikesList();
                        } catch (Exception e) {
                            reactions = new ArrayList<>();
                        }
                        for (String reaction : reactions) {
                            try {
                                String[] parts = reaction.split(":");
                                if (parts.length >= 2) {
                                    String reactorLogin = parts[0];
                                    boolean isPositive = Boolean.parseBoolean(parts[1]);
                                    if (!reactorLogin.equals(currentUserLogin)) {
                                        String reactorName = getUserNameByLogin(reactorLogin, allPosts);
                                        Date reactionTime;
                                        if (parts.length >= 3) {
                                            try {
                                                long timestamp = Long.parseLong(parts[2]);
                                                reactionTime = new Date(timestamp);
                                            } catch (Exception e) {
                                                reactionTime = new Date();
                                            }
                                        } else {
                                            reactionTime = new Date();
                                        }
                                        String notificationId = post.getId() + "_" + reactorLogin + "_" + (isPositive ? "like" : "dislike");
                                        Notification notification = new Notification(
                                            notificationId,
                                            "like",
                                            post.getId().toString(),
                                            post.getPlaceName(),
                                            reactorLogin,
                                            reactorName,
                                            isPositive ? 1 : -1,
                                            null,
                                            reactionTime
                                        );
                                        boolean isRead = false;
                                        try {
                                            isRead = Notification.isNotificationRead(requireContext(), notificationId);
                                        } catch (Exception e) {}
                                        notification.setRead(isRead);
                                        notifications.add(notification);
                                    }
                                }
                            } catch (Exception e) {
                                // skip broken reaction
                            }
                        }
                    }
                } catch (Exception e) {
                    // skip all notifications if something is wrong
                }
                try {
                    notifications.sort((n1, n2) -> {
                        try {
                            Long postId1 = Long.parseLong(n1.getPostId());
                            Long postId2 = Long.parseLong(n2.getPostId());
                            int postComparison = postId2.compareTo(postId1);
                            if (postComparison != 0) {
                                return postComparison;
                            }
                            return n2.getTimestamp().compareTo(n1.getTimestamp());
                        } catch (Exception e) {
                            return n2.getTimestamp().compareTo(n1.getTimestamp());
                        }
                    });
                } catch (Exception e) {}
                adapter.updateNotifications(notifications);
                updateEmptyView();
            }
        }
    }

    private String getUserNameByLogin(String login, List<Post> allPosts) {
        // Используем ViewModel для получения информации о пользователе
        if (viewModel != null) {
            User user = viewModel.getUserByLogin(login);
            if (user != null && user.getName() != null) {
                return user.getName();
            }
        }
        
        // Fallback: ищем пользователя по логину среди всех постов
        for (Post post : allPosts) {
            if (login.equals(post.getLogin())) {
                return post.getAuthorName();
            }
        }
        return login; // Если имя не найдено, возвращаем логин
    }

    private void updateEmptyView() {
        if (notifications.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        
        // Обновляем счетчик непрочитанных уведомлений в нижней навигации
        updateUnreadCount();
    }
    
    private void updateUnreadCount() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            int unreadCount = 0;
            for (Notification notification : notifications) {
                if (!notification.isRead()) {
                    unreadCount++;
                }
            }
            
            mainActivity.updateNotificationBadge(unreadCount);
        }
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Отмечаем уведомление как прочитанное
        notification.setRead(true);
        
        // Сохраняем статус прочитанности в локальное хранилище
        Notification.markNotificationAsRead(requireContext(), notification.getId());
        
        adapter.notifyDataSetChanged();
        
        // Обновляем счетчик непрочитанных уведомлений
        updateUnreadCount();
        
        // Открываем пост в детальном просмотре
        if (getActivity() instanceof MainActivity) {
            try {
                Long postId = Long.parseLong(notification.getPostId());
                ((MainActivity) getActivity()).loadFragment(
                    PostDetailFragment.newInstance(postId, "notifications")
                );
            } catch (NumberFormatException e) {
                ((MainActivity) getActivity()).showError("Ошибка при открытии поста: неверный ID");
            }
        }
    }
}

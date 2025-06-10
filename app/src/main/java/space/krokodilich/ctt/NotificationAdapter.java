package space.krokodilich.ctt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notifications;
    private SimpleDateFormat dateFormat;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", new Locale("ru"));
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        
        // Устанавливаем имя пользователя
        holder.userNameText.setText(notification.getAuthorName());
        
        // Устанавливаем текст уведомления (без имени пользователя)
        String notificationText = getNotificationTextWithoutName(notification);
        holder.notificationText.setText(notificationText);
        
        // Устанавливаем время создания уведомления
        holder.timestampText.setText(dateFormat.format(notification.getTimestamp()));
        
        // Устанавливаем цвет индикатора в зависимости от типа уведомления
        int indicatorColor = getIndicatorColor(notification);
        holder.indicatorView.setBackgroundResource(indicatorColor);
        
        // Настройка визуальных пометок для непрочитанных уведомлений
        if (!notification.isRead()) {
            // Показываем красную точку
            holder.unreadDot.setVisibility(View.VISIBLE);
            
            // Делаем имя пользователя и текст жирными
            holder.userNameText.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.notificationText.setTypeface(null, android.graphics.Typeface.BOLD);
            
            // Изменяем цвета для непрочитанных
            holder.userNameText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.notificationText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.gray_700));
            
            // Делаем фон карточки чуть темнее
            holder.itemView.setBackgroundResource(R.color.unread_notification_background);
        } else {
            // Скрываем точку для прочитанных
            holder.unreadDot.setVisibility(View.GONE);
            
            // Обычный шрифт для прочитанных
            holder.userNameText.setTypeface(null, android.graphics.Typeface.BOLD); // Имя всегда жирное
            holder.notificationText.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            // Обычные цвета
            holder.userNameText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.notificationText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.gray_700));
            
            // Обычный фон
            holder.itemView.setBackgroundResource(R.color.white);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    private String getNotificationTextWithoutName(Notification notification) {
        switch (notification.getType()) {
            case "like":
                if (notification.getRating() > 0) {
                    return "поставил(а) лайк вашему посту \"" + notification.getPostTitle() + "\"";
                } else {
                    return "поставил(а) дизлайк вашему посту \"" + notification.getPostTitle() + "\"";
                }
            case "rating":
                return "изменил(а) рейтинг вашего поста \"" + notification.getPostTitle() + 
                       "\" на " + (notification.getRating() > 0 ? "+" : "") + notification.getRating();
            case "comment":
                return "прокомментировал(а) ваш пост \"" + notification.getPostTitle() + 
                       "\": \"" + notification.getComment() + "\"";
            default:
                return "новое уведомление";
        }
    }

    private int getIndicatorColor(Notification notification) {
        switch (notification.getType()) {
            case "like":
                return notification.getRating() > 0 ? R.color.green_500 : R.color.red_500;
            case "rating":
                return R.color.blue_500;
            case "comment":
                return R.color.orange_500;
            default:
                return R.color.gray_500;
        }
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView userNameText;
        TextView notificationText;
        TextView timestampText;
        View indicatorView;
        View unreadDot;

        NotificationViewHolder(View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.notification_user_name);
            notificationText = itemView.findViewById(R.id.notification_text);
            timestampText = itemView.findViewById(R.id.notification_timestamp);
            indicatorView = itemView.findViewById(R.id.notification_indicator);
            unreadDot = itemView.findViewById(R.id.unread_dot);
        }
    }
} 
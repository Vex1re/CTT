package space.krokodilich.ctt;

import java.util.Date;
import android.content.Context;
import android.content.SharedPreferences;

public class Notification {
    private String id;
    private String type; // "like", "comment", "rating"
    private String postId;
    private String postTitle;
    private String authorLogin;
    private String authorName;
    private int rating; // для уведомлений о рейтинге
    private String comment; // для уведомлений о комментариях
    private Date timestamp;
    private boolean isRead;

    public Notification(String id, String type, String postId, String postTitle, 
                       String authorLogin, String authorName, int rating, 
                       String comment, Date timestamp) {
        this.id = id;
        this.type = type;
        this.postId = postId;
        this.postTitle = postTitle;
        this.authorLogin = authorLogin;
        this.authorName = authorName;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    // Геттеры
    public String getId() { return id; }
    public String getType() { return type; }
    public String getPostId() { return postId; }
    public String getPostTitle() { return postTitle; }
    public String getAuthorLogin() { return authorLogin; }
    public String getAuthorName() { return authorName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public Date getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }

    // Сеттеры
    public void setRead(boolean read) { 
        this.isRead = read; 
    }

    // Методы для работы с локальным хранением
    public static boolean isNotificationRead(Context context, String notificationId) {
        SharedPreferences prefs = context.getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("read_" + notificationId, false);
    }

    public static void markNotificationAsRead(Context context, String notificationId) {
        SharedPreferences prefs = context.getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("read_" + notificationId, true);
        editor.apply();
    }

    public static void clearReadNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    // Метод для получения текста уведомления
    public String getNotificationText() {
        switch (type) {
            case "like":
                if (rating > 0) {
                    return authorName + " поставил(а) лайк вашему посту \"" + postTitle + "\"";
                } else {
                    return authorName + " поставил(а) дизлайк вашему посту \"" + postTitle + "\"";
                }
            case "rating":
                return authorName + " изменил(а) рейтинг вашего поста \"" + postTitle + 
                       "\" на " + (rating > 0 ? "+" : "") + rating;
            case "comment":
                return authorName + " прокомментировал(а) ваш пост \"" + postTitle + 
                       "\": \"" + comment + "\"";
            default:
                return "Новое уведомление";
        }
    }
} 
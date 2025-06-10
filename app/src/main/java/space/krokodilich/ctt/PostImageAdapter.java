package space.krokodilich.ctt;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import java.util.ArrayList;
import java.util.List;

public class PostImageAdapter extends RecyclerView.Adapter<PostImageAdapter.ImageViewHolder> {
    private List<String> images;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(String imageUrl, int position);
    }

    public PostImageAdapter(List<String> images) {
        this.images = images;
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String originalImageUrl = images.get(position);
        if (originalImageUrl == null || originalImageUrl.isEmpty()) {
            Log.e("PostImageAdapter", "Empty image URL at position: " + position);
            return;
        }

        // Добавляем базовый URL сервера, если его нет
        final String imageUrl;
        if (originalImageUrl.startsWith("http")) {
            imageUrl = originalImageUrl;
        } else if (originalImageUrl.startsWith("/uploads/")) {
            imageUrl = "https://spring-boot-production-6510.up.railway.app" + originalImageUrl;
        } else {
            imageUrl = "https://spring-boot-production-6510.up.railway.app/uploads/" + originalImageUrl;
        }
            
        Log.d("PostImageAdapter", "Loading image from URL: " + imageUrl);
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("PostImageAdapter", "Failed to load image: " + imageUrl, e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d("PostImageAdapter", "Successfully loaded image: " + imageUrl);
                        return false;
                    }
                })
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(imageUrl, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setImages(List<String> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.post_image);
        }
    }
} 
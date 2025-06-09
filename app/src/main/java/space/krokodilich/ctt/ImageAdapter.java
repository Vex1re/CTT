package space.krokodilich.ctt;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<Uri> images;
    private OnImageRemoveListener listener;

    public interface OnImageRemoveListener {
        void onImageRemove(int position);
    }

    public ImageAdapter(OnImageRemoveListener listener) {
        this.images = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_edit, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = images.get(position);
        
        // Загружаем изображение с помощью Glide
        Glide.with(holder.itemView.getContext())
            .load(imageUri)
            .centerCrop()
            .into(holder.imageView);

        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setImages(List<Uri> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        FloatingActionButton removeButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            removeButton = itemView.findViewById(R.id.remove_image_button);
        }
    }
} 
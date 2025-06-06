package space.krokodilich.ctt;

import android.net.Uri;
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

    private List<Uri> imageUris;
    private OnImageRemoveListener listener;

    public interface OnImageRemoveListener {
        void onImageRemove(int position);
    }

    public ImageAdapter(List<Uri> imageUris, OnImageRemoveListener listener) {
        this.imageUris = new ArrayList<>(imageUris);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        holder.bind(imageUri);
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public void addImageUri(Uri uri) {
        imageUris.add(uri);
        notifyItemInserted(imageUris.size() - 1);
    }

    public List<Uri> getImageUris() {
        return imageUris;
    }

    public void removeImageAt(int position) {
        imageUris.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, imageUris.size());
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        FloatingActionButton removeButton;

        public ImageViewHolder(@NonNull View itemView, OnImageRemoveListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            removeButton = itemView.findViewById(R.id.remove_image_button);

            removeButton.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onImageRemove(getAdapterPosition());
                }
            });
        }

        public void bind(Uri uri) {
            // Use Glide or another image loading library to load the image from URI
            // Make sure you have Glide added as a dependency in your build.gradle file
            Glide.with(itemView.getContext())
                    .load(uri)
                    .centerCrop()
                    .into(imageView);
        }
    }
} 
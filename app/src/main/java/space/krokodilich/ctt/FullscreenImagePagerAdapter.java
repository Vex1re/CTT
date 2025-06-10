package space.krokodilich.ctt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import java.util.List;

public class FullscreenImagePagerAdapter extends RecyclerView.Adapter<FullscreenImagePagerAdapter.ImageViewHolder> {
    private List<String> images;
    private int initialPosition;

    public FullscreenImagePagerAdapter(List<String> images, int initialPosition) {
        this.images = images;
        this.initialPosition = initialPosition;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fullscreen_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = images.get(position);
        Glide.with(holder.itemView.getContext())
            .load(imageUrl)
            .into(holder.photoView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public int getInitialPosition() {
        return initialPosition;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.fullscreen_image);
        }
    }
} 
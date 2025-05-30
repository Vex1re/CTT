package space.krokodilich.ctt;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView {

    public UserAdapter(@NonNull Context context) {
        super(context);
    }

    public UserAdapter(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public UserAdapter(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}

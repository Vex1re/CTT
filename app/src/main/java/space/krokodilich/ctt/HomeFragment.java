package space.krokodilich.ctt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_menu, container, false);
    }

    public void loadPost(){
        ArrayList<Post> posts = new ArrayList<>();

        posts.add(new Post(
                "Andrew",
                "Moscow",
                "1 hour ago",
                "Red square",
                "Yes",
                "its a beautiful and pretty place",
                100
        ));


    }
}

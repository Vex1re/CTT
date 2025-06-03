package space.krokodilich.ctt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public interface rgst extends LayoutInflater.Factory2, Window.Callback {
    View onCreateView(LayoutInflater inflater, ViewGroup container,
                      Bundle savedInstanceState);

    void Register(View view);
}

package com.example.artistlan.Fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;
import com.example.artistlan.Carrusel.adapter.CarruselAdapter;
import com.example.artistlan.Carrusel.model.ObraCarruselItem;

import java.util.ArrayList;
import java.util.List;

public class FragMain extends Fragment {

    private ViewPager2 viewPager;
    private ImageButton btnIzq, btnDer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_frag_main, container, false);
        new BotonesMenuSuperior(this, root);
        viewPager = root.findViewById(R.id.viewPagerCarrusel);
        btnIzq = root.findViewById(R.id.btnCarruselIzquierdo);
        btnDer = root.findViewById(R.id.btnCarruselDerecho);

        List<ObraCarruselItem> obras = new ArrayList<>();
        obras.add(new ObraCarruselItem(R.drawable.pin1, "Obra 1", "Descripción 1", "Superman", "135K"));
        obras.add(new ObraCarruselItem(R.drawable.pin2, "Obra 2", "Descripción 2", "Batman", "80K"));
        obras.add(new ObraCarruselItem(R.drawable.pin3, "Obra 3", "Descripción 3", "Wonder Woman", "95K"));

        CarruselAdapter adapter = new CarruselAdapter(obras, getContext());
        viewPager.setAdapter(adapter);

        btnDer.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < obras.size() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                animarBoton(v);
            }
        });

        btnIzq.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                animarBoton(v);
            }
        });

        return root;
    }

    private void animarBoton(View v) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.2f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300);
        set.start();
    }
}

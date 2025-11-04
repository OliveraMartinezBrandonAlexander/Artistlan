package com.example.artistlan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;

public class FragMain extends Fragment {

    private ImageButton btnLike;
    private TextView tvLikesCount;
    private boolean isLiked = false;
    private int likesCount = 135000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frag_main, container, false);

        // INICIALIZAR MENÚ SUPERIOR - Esto es lo más importante
        new BotonesMenuSuperior(this, view);

        // INICIALIZAR SISTEMA DE LIKES
        inicializarSistemaLikes(view);

        return view;
    }

    private void inicializarSistemaLikes(View view) {
        btnLike = view.findViewById(R.id.btnLike);
        tvLikesCount = view.findViewById(R.id.tvLikesCount);

        if (btnLike != null && tvLikesCount != null) {
            btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleLike();
                }
            });
            updateLikesCount();
        }
    }

    private void toggleLike() {
        if (isLiked) {
            animateHeartChange(R.drawable.ic_heart_red);
            likesCount--;
            isLiked = false;
        } else {
            animateHeartChange(R.drawable.ic_heart_purple);
            likesCount++;
            isLiked = true;
            Toast.makeText(getContext(), "¡Te ha gustado esta obra!", Toast.LENGTH_SHORT).show();
        }
        updateLikesCount();
    }

    private void animateHeartChange(int newDrawable) {
        if (btnLike != null) {
            btnLike.animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(200)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            btnLike.setImageResource(newDrawable);
                            btnLike.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(200)
                                    .start();
                        }
                    })
                    .start();
        }
    }
    private void updateLikesCount() {
        if (tvLikesCount != null) {
            if (likesCount >= 1000) {
                int thousands = likesCount / 1000;
                tvLikesCount.setText(thousands + "K");
            } else {
                tvLikesCount.setText(String.valueOf(likesCount));
            }
        }
    }
}
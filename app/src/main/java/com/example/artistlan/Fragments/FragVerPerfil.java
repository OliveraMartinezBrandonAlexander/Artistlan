package com.example.artistlan.Fragments;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;

public class FragVerPerfil extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_frag_ver_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this, view);
        ImageView btnFavoritos = view.findViewById(R.id.btnFavoritos);
        btnFavoritos.setVisibility(View.VISIBLE);

        btnFavoritos.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btnFavoritos)
        {
            Navigation.findNavController(v).navigate(R.id.fragFavoritos);
        }

    }
}
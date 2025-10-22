package com.example.artistlan.Fragments;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.artistlan.Activitys.ActCrearCuenta;
import com.example.artistlan.Activitys.ActIniciarSesion;
import com.example.artistlan.Activitys.MainActivity;
import com.example.artistlan.BotonesMenuInferior;
import com.example.artistlan.R;

public class FragMain extends Fragment implements View.OnClickListener {

    Button btnCerrarSesion;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_frag_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(this);

        new BotonesMenuInferior(this, view);
    }

    @Override
    public void onClick(View v) {
        int idClick = v.getId();
        Intent irActivity = null;

        if (idClick == R.id.btnCerrarSesion) {
            irActivity = new Intent(getContext(), MainActivity.class);
        }
        if (irActivity != null) {
            startActivity(irActivity);
        }
    }
}
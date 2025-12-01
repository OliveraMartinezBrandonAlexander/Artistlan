package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.artistlan.Activitys.ActActualizarDatos;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;

public class FragVerPerfil extends Fragment implements View.OnClickListener {

    private TextView tvNombre, tvUsuario, tvCorreo, tvDescripcion, tvTelefono, tvRedes, tvFecNac;
    private ImageView imgFotoPerfil, btnFavoritos;
    private Button btnMisServicios, btnMiArte, btnSubirObra, btnEditarPefil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_ver_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this, view);

        btnFavoritos = view.findViewById(R.id.btnFavoritos);
        btnSubirObra = view.findViewById(R.id.btnSubirObra);
        btnEditarPefil= view.findViewById(R.id.btnEditarPefil);
        btnFavoritos.setVisibility(View.VISIBLE);
        btnFavoritos.setOnClickListener(this);
        btnSubirObra.setOnClickListener(this);
        btnEditarPefil.setOnClickListener(this);

        tvNombre = view.findViewById(R.id.VrpTxvNombre);
        tvUsuario = view.findViewById(R.id.VrpTxvUsuario);
        tvCorreo = view.findViewById(R.id.VrpTxvCorreo);
        tvDescripcion = view.findViewById(R.id.VrpTxvDescripcion);
        tvTelefono = view.findViewById(R.id.VrpTxvTelefono);
        tvRedes = view.findViewById(R.id.VrpTxvRedes);
        tvFecNac = view.findViewById(R.id.VrpTxvFecNac);

        imgFotoPerfil = view.findViewById(R.id.imgPerfil);

        btnMisServicios = view.findViewById(R.id.btnMisServicios);
        btnMiArte = view.findViewById(R.id.btnMiArte);

        cargarDatosUsuario();

        btnMisServicios.setOnClickListener(v -> {

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentsPerfil, new FragMisServicios())
                    .commit();
        });


        btnMiArte.setOnClickListener(v -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentsPerfil, new FragMiArte())
                    .addToBackStack(null)
                    .commit();
        });
    }
    @Override
    public void onStop() {
        super.onStop();

        Fragment fragmentoActual = getChildFragmentManager()
                .findFragmentById(R.id.contenedorFragmentsPerfil);

        if (fragmentoActual != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .remove(fragmentoActual)
                    .commitAllowingStateLoss();
        }
    }

    private void cargarDatosUsuario() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);

        tvNombre.setText(prefs.getString("nombreCompleto", "Nombre no disponible"));
        tvUsuario.setText(prefs.getString("usuario", "usuario"));
        tvCorreo.setText(prefs.getString("correo", "correo no disponible"));
        tvDescripcion.setText(prefs.getString("descripcion", "Sin descripción"));
        tvTelefono.setText(prefs.getString("telefono", "No disponible"));
        tvRedes.setText(prefs.getString("redes", "Sin redes"));
        tvFecNac.setText(prefs.getString("fechaNac", "Sin fecha"));

        // Si luego usas Glide:
        // Glide.with(this).load(prefs.getString("fotoPerfil", null)).into(imgFotoPerfil);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnFavoritos) {
            Navigation.findNavController(v).navigate(R.id.fragFavoritos);
        }
        if (v.getId() == R.id.btnSubirObra) {
            Navigation.findNavController(v).navigate(R.id.fragSubirObra);
        }
        if (v.getId() == R.id.btnEditarPefil) {
            Intent intent = new Intent(v.getContext(), ActActualizarDatos.class);

            v.getContext().startActivity(intent);
        }
    }
}
package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.Activitys.ActActualizarDatos;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;

public class FragVerPerfil extends Fragment implements View.OnClickListener {

    private TextView tvNombre, tvUsuario, tvCorreo, tvDescripcion, tvTelefono, tvRedes, tvFecNac, tvCategoria;
    private ImageView imgFotoPerfil, btnFavoritos;

    private ImageButton btnEditarPefil;


    // Ficha expandible
    private CardView cardPerfilInfo;
    private View expandedSectionPerfil;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_ver_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this, view);

        View root = view.findViewById(R.id.rootPerfil);
        root.setOnClickListener(v -> colapsarFicha());

        btnFavoritos = view.findViewById(R.id.btnFavoritos);
        btnFavoritos.setVisibility(View.VISIBLE);
        btnFavoritos.setOnClickListener(this);

        cardPerfilInfo = view.findViewById(R.id.cardPerfilInfo);
        expandedSectionPerfil = view.findViewById(R.id.expanded_section_perfil);

        cardPerfilInfo.setOnClickListener(v -> {
            toggleFicha();
        });

        btnEditarPefil = view.findViewById(R.id.btnEditarPefil);
        btnEditarPefil.setOnClickListener(this);

        // TextViews datos
        tvNombre = view.findViewById(R.id.VrpTxvNombre);
        tvUsuario = view.findViewById(R.id.VrpTxvUsuario);
        tvCorreo = view.findViewById(R.id.VrpTxvCorreo);
        tvDescripcion = view.findViewById(R.id.VrpTxvDescripcion);
        tvTelefono = view.findViewById(R.id.VrpTxvTelefono);
        tvRedes = view.findViewById(R.id.VrpTxvRedes);
        tvFecNac = view.findViewById(R.id.VrpTxvFecNac);
        tvCategoria = view.findViewById(R.id.VrpTxvCategoria);

        imgFotoPerfil = view.findViewById(R.id.imgPerfil);

        cargarDatosUsuario();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDatosUsuario();
    }


    private void cargarDatosUsuario() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);

        String nombre = prefs.getString("nombreCompleto", "Nombre no disponible");
        String usuario = prefs.getString("usuario", "usuario");
        String correo = prefs.getString("correo", "correo no disponible");
        String descripcion = prefs.getString("descripcion", "");
        String telefono = prefs.getString("telefono", "");
        String redes = prefs.getString("redes", "");
        String fechaNac = prefs.getString("fechaNac", "");
        String categoria = prefs.getString("categoria", "Sin categoría");

        tvNombre.setText(nombre.isEmpty() ? "Nombre no disponible" : nombre);
        tvUsuario.setText(usuario.isEmpty() ? "usuario" : usuario);
        tvCorreo.setText(correo.isEmpty() ? "correo no disponible" : correo);
        tvDescripcion.setText(descripcion.isEmpty() ? "Sin descripción" : descripcion);
        tvTelefono.setText(telefono.isEmpty() ? "No disponible" : telefono);
        tvRedes.setText(redes.isEmpty() ? "Sin redes" : redes);
        tvFecNac.setText(fechaNac.isEmpty() ? "Sin fecha" : fechaNac);
        tvCategoria.setText(categoria.isEmpty() ? "Sin categoría" : categoria);

        String fotoPerfil = prefs.getString("fotoPerfil", null);
        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            Glide.with(this)
                    .load(fotoPerfil)
                    .placeholder(R.drawable.fotoperfilprueba)
                    .error(R.drawable.fotoperfilprueba)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imgFotoPerfil);
        } else {
            imgFotoPerfil.setImageResource(R.drawable.fotoperfilprueba);
        }
    }

    // ======== FICHA EXPANDIBLE ========

    private void toggleFicha() {
        if (expandedSectionPerfil.getVisibility() == View.VISIBLE) {
            animarExpand(expandedSectionPerfil, false);
        } else {
            animarExpand(expandedSectionPerfil, true);
        }
    }

    private void colapsarFicha() {
        if (expandedSectionPerfil != null && expandedSectionPerfil.getVisibility() == View.VISIBLE) {
            animarExpand(expandedSectionPerfil, false);
        }
    }

    private void animarExpand(View v, boolean expandir) {
        if (expandir) {
            if (v.getVisibility() == View.VISIBLE) return;
            v.setVisibility(View.VISIBLE);
            v.setAlpha(0f);
            v.setScaleY(0f);
            v.animate().alpha(1f).scaleY(1f).setDuration(120).start();
        } else {
            if (v.getVisibility() == View.GONE) return;
            v.animate().alpha(0f).scaleY(0f).setDuration(150)
                    .withEndAction(() -> v.setVisibility(View.GONE))
                    .start();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnFavoritos) {
            Navigation.findNavController(v).navigate(R.id.fragFavoritos);

        } else if (id == R.id.btnEditarPefil) {
            Intent intent = new Intent(v.getContext(), ActActualizarDatos.class);
            v.getContext().startActivity(intent);
        }
    }
}

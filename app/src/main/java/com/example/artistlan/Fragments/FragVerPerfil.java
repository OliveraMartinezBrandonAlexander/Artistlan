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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.Activitys.ActActualizarDatos;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;

public class FragVerPerfil extends Fragment implements View.OnClickListener {

    private TextView tvNombre, tvUsuario, tvCorreo, tvDescripcion, tvTelefono, tvRedes, tvFecNac;
    private ImageView imgFotoPerfil, btnFavoritos;
    private Button btnMisServicios, btnMiArte, btnSubirObra, btnEditarPefil, btnSubirServicio;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_ver_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this, view);

        btnFavoritos     = view.findViewById(R.id.btnFavoritos);
        btnSubirObra     = view.findViewById(R.id.btnSubirObra);
        btnEditarPefil   = view.findViewById(R.id.btnEditarPefil);
        btnSubirServicio = view.findViewById(R.id.btnSubirServicio);

        btnFavoritos.setVisibility(View.VISIBLE);

        btnFavoritos.setOnClickListener(this);
        btnSubirObra.setOnClickListener(this);
        btnEditarPefil.setOnClickListener(this);
        btnSubirServicio.setOnClickListener(this);

        tvNombre      = view.findViewById(R.id.VrpTxvNombre);
        tvUsuario     = view.findViewById(R.id.VrpTxvUsuario);
        tvCorreo      = view.findViewById(R.id.VrpTxvCorreo);
        tvDescripcion = view.findViewById(R.id.VrpTxvDescripcion);
        tvTelefono    = view.findViewById(R.id.VrpTxvTelefono);
        tvRedes       = view.findViewById(R.id.VrpTxvRedes);
        tvFecNac      = view.findViewById(R.id.VrpTxvFecNac);

        imgFotoPerfil = view.findViewById(R.id.imgPerfil);

        btnMisServicios = view.findViewById(R.id.btnMisServicios);
        btnMiArte       = view.findViewById(R.id.btnMiArte);

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
    public void onResume() {
        super.onResume();
        cargarDatosUsuario();
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

        // Obtener los datos como cadenas
        String nombre = prefs.getString("nombreCompleto", "Nombre no disponible");
        String usuario = prefs.getString("usuario", "usuario");
        String correo = prefs.getString("correo", "correo no disponible");
        String descripcion = prefs.getString("descripcion", "");
        String telefono = prefs.getString("telefono", "");
        String redes = prefs.getString("redes", "");
        String fechaNac = prefs.getString("fechaNac", "");
        String categoria = prefs.getString("categoria", "Sin categoría"); // NUEVO

        // Aplicar los valores a los TextViews, usando el valor por defecto si la cadena está vacía
        tvNombre.setText(nombre.isEmpty() ? "Nombre no disponible" : nombre);
        tvUsuario.setText(usuario.isEmpty() ? "usuario" : usuario);
        tvCorreo.setText(correo.isEmpty() ? "correo no disponible" : correo);
        tvDescripcion.setText(descripcion.isEmpty() ? "Sin descripción" : descripcion);
        tvTelefono.setText(telefono.isEmpty() ? "No disponible" : telefono);
        tvRedes.setText(redes.isEmpty() ? "Sin redes" : redes);
        tvFecNac.setText(fechaNac.isEmpty() ? "Sin fecha" : fechaNac);

        // Cargar la foto de perfil
        String fotoPerfil = prefs.getString("fotoPerfil", null);
        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            Glide.with(this)
                    .load(fotoPerfil)
                    .placeholder(R.drawable.fotoperfilprueba)
                    .error(R.drawable.fotoperfilprueba)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imgFotoPerfil);
        }
        TextView tvCategoria = getView().findViewById(R.id.VrpTxvCategoria);
        if (tvCategoria != null) {
            tvCategoria.setText(categoria.isEmpty() ? "Sin categoría" : categoria);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnFavoritos) {
            Navigation.findNavController(v).navigate(R.id.fragFavoritos);
        } else if (id == R.id.btnSubirObra) {
            Navigation.findNavController(v).navigate(R.id.fragSubirObra);
        } else if (id == R.id.btnSubirServicio) {
            Navigation.findNavController(v).navigate(R.id.fragSubirServicio);
        } else if (id == R.id.btnEditarPefil) {
            Intent intent = new Intent(v.getContext(), ActActualizarDatos.class);
            v.getContext().startActivity(intent);
        }
    }
}

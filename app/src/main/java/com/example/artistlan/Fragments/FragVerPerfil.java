package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private Button btnMisServicios, btnMiArte, btnSubirObra, btnSubirServicio;
    private Button btnEditarObra, btnEditarServicio;

    // Ficha expandible
    private CardView cardPerfilInfo;
    private View expandedSectionPerfil;

    // Segment control
    private View segmentContainer;
    private View segmentIndicator;
    private boolean selectedObras = true; // default

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_ver_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this, view);

        // Root click => colapsar ficha si está abierta
        View root = view.findViewById(R.id.rootPerfil);
        root.setOnClickListener(v -> colapsarFicha());

        // Menú superior
        btnFavoritos = view.findViewById(R.id.btnFavoritos);
        btnFavoritos.setVisibility(View.VISIBLE);
        btnFavoritos.setOnClickListener(this);

        // Ficha
        cardPerfilInfo = view.findViewById(R.id.cardPerfilInfo);
        expandedSectionPerfil = view.findViewById(R.id.expanded_section_perfil);

        cardPerfilInfo.setOnClickListener(v -> {
            // Consumimos el click y toggle
            toggleFicha();
        });

        // Editar perfil (icono dentro de ficha)
        btnEditarPefil = view.findViewById(R.id.btnEditarPefil);
        btnEditarPefil.setOnClickListener(this);

        // Botones subir
        btnSubirObra = view.findViewById(R.id.btnSubirObra);
        btnSubirServicio = view.findViewById(R.id.btnSubirServicio);
        btnSubirObra.setOnClickListener(this);
        btnSubirServicio.setOnClickListener(this);

        // Botones editar (nuevos)
        btnEditarObra = view.findViewById(R.id.btnEditarObra);
        btnEditarServicio = view.findViewById(R.id.btnEditarServicio);
        btnEditarObra.setOnClickListener(this);
        btnEditarServicio.setOnClickListener(this);

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

        // Segment control refs
        segmentContainer = view.findViewById(R.id.segmentContainer);
        segmentIndicator = view.findViewById(R.id.segmentIndicator);

        // Botones segmento
        btnMiArte = view.findViewById(R.id.btnMiArte);
        btnMisServicios = view.findViewById(R.id.btnMisServicios);

        btnMiArte.setOnClickListener(v -> seleccionarSegmento(true));
        btnMisServicios.setOnClickListener(v -> seleccionarSegmento(false));

        // Cargar datos usuario (tu lógica)
        cargarDatosUsuario();

        // Default: Mis Obras SIEMPRE al entrar
        if (savedInstanceState == null) {
            seleccionarSegmento(true); // además carga FragMiArte
        } else {
            // Si quieres, podrías conservar el estado, pero pediste default siempre "Mis Obras"
            seleccionarSegmento(true);
        }
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

    // ======== SEGMENT CONTROL ========

    private void seleccionarSegmento(boolean obras) {
        selectedObras = obras;

        // 1) UI (animación del indicador)
        segmentContainer.post(() -> {
            int w = segmentContainer.getWidth();
            int half = w / 2;

            ViewGroup.LayoutParams lp = segmentIndicator.getLayoutParams();
            lp.width = half - (int) (8 * getResources().getDisplayMetrics().density); // margen aprox por padding
            segmentIndicator.setLayoutParams(lp);

            float targetX = obras ? 0f : half;
            segmentIndicator.animate()
                    .translationX(targetX)
                    .setDuration(180)
                    .start();

            // colores de texto invertidos
            if (obras) {
                btnMiArte.setTextColor(0xFFFFFFFF);
                btnMisServicios.setTextColor(0xFF1E3A8A);
            } else {
                btnMiArte.setTextColor(0xFF1E3A8A);
                btnMisServicios.setTextColor(0xFFFFFFFF);
            }
        });

        // 2) Cargar fragment correspondiente (tu lógica de BD no cambia)
        if (obras) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentsPerfil, new FragMiArte())
                    .commit();
        } else {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentsPerfil, new FragMisServicios())
                    .commit();
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

        } else if (id == R.id.btnEditarObra) {
            // Pendiente: pantalla real de edición obra
            Toast.makeText(requireContext(), "Editar Obra (pendiente pantalla)", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.btnEditarServicio) {
            // Pendiente: pantalla real de edición servicio
            Toast.makeText(requireContext(), "Editar Servicio (pendiente pantalla)", Toast.LENGTH_SHORT).show();
        }
    }
}

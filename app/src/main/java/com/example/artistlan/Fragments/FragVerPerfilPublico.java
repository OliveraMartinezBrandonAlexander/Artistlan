//package com.example.artistlan.Fragments;
//
//import android.graphics.Rect;
//import android.os.Bundle;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.bumptech.glide.Glide;
//import com.example.artistlan.BotonesMenuSuperior;
//import com.example.artistlan.R;
//
//public class FragVerPerfilPublico extends Fragment {
//
//    // Args
//    public static final String ARG_ID_USUARIO = "idUsuario";
//    public static final String ARG_USUARIO = "usuario";
//    public static final String ARG_NOMBRE = "nombreCompleto";
//    public static final String ARG_CORREO = "correo";
//    public static final String ARG_TELEFONO = "telefono";
//    public static final String ARG_REDES = "redes";
//    public static final String ARG_FECHA = "fechaNac";
//    public static final String ARG_CATEGORIA = "categoria";
//    public static final String ARG_DESCRIPCION = "descripcion";
//    public static final String ARG_FOTO = "fotoPerfil";
//
//    private int idUsuario = -1;
//    private String usuario = "";
//
//    // UI
//    private View root;
//    private View cardPerfil, expandedInfo;
//    private ImageView imgPerfil;
//    private TextView tvNombre, tvDescripcion, tvCorreo, tvTelefono, tvRedes, tvFecha, tvCategoria;
//
//    // Tabs
//    private View tabsContainer, indicator;
//    private TextView btnTabObras, btnTabServicios;
//
//    private boolean expandido = false;
//    private boolean mostrandoObras = true;
//
//    public FragVerPerfilPublico() {
//        super(R.layout.fragment_frag_ver_perfil_publico);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        this.root = view;
//
//        new BotonesMenuSuperior(this, view);
//
//        bindViews();
//        leerArgs();
//        pintarPerfilConArgs();
//        setupExpandCollapse();
//        setupTabs();
//
//        // Default: Obras
//        mostrarObras();
//    }
//
//    private void bindViews() {
//        cardPerfil = root.findViewById(R.id.cardPerfilPublico);
//        expandedInfo = root.findViewById(R.id.expandedInfoPublico);
//
//        imgPerfil = root.findViewById(R.id.imgPerfilPublico);
//        tvNombre = root.findViewById(R.id.tvNombrePublico);
//        tvDescripcion = root.findViewById(R.id.tvDescripcionPublico);
//
//        tvCorreo = root.findViewById(R.id.tvCorreoPublico);
//        tvTelefono = root.findViewById(R.id.tvTelefonoPublico);
//        tvRedes = root.findViewById(R.id.tvRedesPublico);
//        tvFecha = root.findViewById(R.id.tvFecNacPublico);
//        tvCategoria = root.findViewById(R.id.tvCategoriaPublico);
//
//        tabsContainer = root.findViewById(R.id.tabsContainer);
//        indicator = root.findViewById(R.id.tabIndicator);
//        btnTabObras = root.findViewById(R.id.btnTabObras);
//        btnTabServicios = root.findViewById(R.id.btnTabServicios);
//    }
//
//    private void leerArgs() {
//        Bundle b = getArguments();
//        if (b == null) return;
//
//        idUsuario = b.getInt(ARG_ID_USUARIO, -1);
//        usuario = safe(b.getString(ARG_USUARIO, ""));
//    }
//
//    private void pintarPerfilConArgs() {
//        Bundle b = getArguments();
//        if (b == null) return;
//
//        String nombre = safe(b.getString(ARG_NOMBRE, usuario));
//        String descripcion = safe(b.getString(ARG_DESCRIPCION, "Hola, estoy usando Artistlan"));
//        String correo = safe(b.getString(ARG_CORREO, "No disponible"));
//        String telefono = safe(b.getString(ARG_TELEFONO, "No disponible"));
//        String redes = safe(b.getString(ARG_REDES, "Sin redes"));
//        String fecha = safe(b.getString(ARG_FECHA, "Sin fecha"));
//        String categoria = safe(b.getString(ARG_CATEGORIA, "Sin categoría"));
//        String foto = b.getString(ARG_FOTO, null);
//
//        tvNombre.setText(nombre.isEmpty() ? "Usuario" : nombre);
//        tvDescripcion.setText(descripcion.isEmpty() ? "Hola, estoy usando Artistlan" : descripcion);
//
//        tvCorreo.setText("Correo: " + (correo.isEmpty() ? "No disponible" : correo));
//        tvTelefono.setText("Teléfono: " + (telefono.isEmpty() ? "No disponible" : telefono));
//        tvRedes.setText("Redes: " + (redes.isEmpty() ? "Sin redes" : redes));
//        tvFecha.setText("Fecha: " + (fecha.isEmpty() ? "Sin fecha" : fecha));
//        tvCategoria.setText("Categoría: " + (categoria.isEmpty() ? "Sin categoría" : categoria));
//
//        // Foto
//        if (foto != null && !foto.isEmpty()) {
//            Glide.with(this)
//                    .load(foto)
//                    .placeholder(R.drawable.fotoperfilprueba)
//                    .error(R.drawable.fotoperfilprueba)
//                    .into(imgPerfil);
//        } else {
//            imgPerfil.setImageResource(R.drawable.fotoperfilprueba);
//        }
//    }
//
//    private void setupExpandCollapse() {
//        // Click en la tarjeta -> alterna expandido
//        cardPerfil.setOnClickListener(v -> toggleExpand());
//
//        // Tap fuera del card -> colapsa
//        root.setOnTouchListener((v, event) -> {
//            if (event.getAction() == MotionEvent.ACTION_DOWN && expandido) {
//                Rect rect = new Rect();
//                cardPerfil.getGlobalVisibleRect(rect);
//
//                int x = (int) event.getRawX();
//                int y = (int) event.getRawY();
//
//                if (!rect.contains(x, y)) {
//                    colapsar();
//                    return true;
//                }
//            }
//            return false;
//        });
//    }
//
//    private void toggleExpand() {
//        if (expandido) colapsar();
//        else expandir();
//    }
//
//    private void expandir() {
//        expandido = true;
//        expandedInfo.setVisibility(View.VISIBLE);
//        expandedInfo.setAlpha(0f);
//        expandedInfo.setScaleY(0.9f);
//        expandedInfo.animate().alpha(1f).scaleY(1f).setDuration(160).start();
//    }
//
//    private void colapsar() {
//        expandido = false;
//        expandedInfo.animate()
//                .alpha(0f)
//                .scaleY(0.9f)
//                .setDuration(160)
//                .withEndAction(() -> expandedInfo.setVisibility(View.GONE))
//                .start();
//    }
//
//    private void setupTabs() {
//        // Ajustar ancho del indicador a la mitad del contenedor
//        tabsContainer.post(() -> {
//            int w = tabsContainer.getWidth();
//            if (w <= 0) return;
//            indicator.getLayoutParams().width = w / 2;
//            indicator.requestLayout();
//            moverIndicador(true, false); // default a Obras sin animación
//        });
//
//        btnTabObras.setOnClickListener(v -> {
//            if (!mostrandoObras) {
//                mostrarObras();
//            }
//        });
//
//        btnTabServicios.setOnClickListener(v -> {
//            if (mostrandoObras) {
//                mostrarServicios();
//            }
//        });
//    }
//
//    private void mostrarObras() {
//        mostrandoObras = true;
//        moverIndicador(true, true);
//
//        btnTabObras.setTextColor(0xFFFFFFFF);
//        btnTabServicios.setTextColor(0xFF2F6FED);
//
//        Fragment f = FragPerfilObrasPublico.newInstance(idUsuario);
//        getChildFragmentManager()
//                .beginTransaction()
//                .replace(R.id.contenedorFragmentsPerfilPublico, f)
//                .commit();
//    }
//
//    private void mostrarServicios() {
//        mostrandoObras = false;
//        moverIndicador(false, true);
//
//        btnTabServicios.setTextColor(0xFFFFFFFF);
//        btnTabObras.setTextColor(0xFF2F6FED);
//
//        Fragment f = FragPerfilServiciosPublico.newInstance(usuario);
//        getChildFragmentManager()
//                .beginTransaction()
//                .replace(R.id.contenedorFragmentsPerfilPublico, f)
//                .commit();
//    }
//
//    private void moverIndicador(boolean aObras, boolean animar) {
//        int w = tabsContainer.getWidth();
//        float targetX = aObras ? 0f : (w / 2f);
//
//        if (!animar) {
//            indicator.setTranslationX(targetX);
//            return;
//        }
//
//        indicator.animate().translationX(targetX).setDuration(220).start();
//    }
//
//    private String safe(String s, String def) {
//        return (s == null) ? def : s;
//    }
//}

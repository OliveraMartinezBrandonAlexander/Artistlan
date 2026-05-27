package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.api.SolicitudesApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Fragments.DialogReportarContenido;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.model.PerfilPublicoArtistaDTO;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;
import com.example.artistlan.TarjetaTextoServicio.adapter.TarjetaTextoServicioAdapter;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;
import com.example.artistlan.utils.LikeStateManager;
import com.example.artistlan.utils.ReporteUiPermissions;
import com.example.artistlan.utils.SocialNetworkHelper;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragVerPerfilPublico extends Fragment {
    private static final long LIKE_THROTTLE_MS = 500L;

    private View root;
    private View cardPerfil;
    private View expandedInfo;
    private View tabsContainer;
    private View indicator;
    private FrameLayout frameFotoPerfilPublico;
    private ImageView imgPerfil;
    private TextView tvUsuario;
    private TextView tvDescripcion;
    private TextView tvNombreCompleto;
    private TextView tvRedes;
    private TextView tvFecha;
    private TextView tvCategorias;
    private TextView tvUbicacion;
    private TextView tvVacio;
    private TextView btnTabObras;
    private TextView btnTabServicios;
    private Button btnReportarUsuario;
    private LinearLayout contenedorRedesChips;
    private RecyclerView recyclerPublico;
    private NestedScrollView scrollPerfilPublico;

    private boolean expandido = false;
    private boolean mostrandoObras = true;
    private boolean redesExpandidas = false;
    private float swipeStartX = 0f;
    private float swipeStartY = 0f;
    private boolean swipeHorizontalDetectado = false;
    private int idUsuarioLogueado = -1;
    private int idArtista = -1;
    private String rolUsuarioLogueado = "";
    private String redesPublicoActual = "";

    private TarjetaTextoObraAdapter obraAdapter;
    private TarjetaTextoServicioAdapter servicioAdapter;
    private FavoritosApi favoritosApi;
    private SolicitudesApi solicitudesApi;
    private UsuarioApi usuarioApi;
    private ThemeManager themeManager;

    private List<TarjetaTextoObraItem> obras = new ArrayList<>();
    private List<TarjetaTextoServicioItem> servicios = new ArrayList<>();
    private final Map<String, Long> ultimoToqueLike = new HashMap<>();
    private final Set<String> likesEnCurso = new HashSet<>();

    public FragVerPerfilPublico() {
        super(R.layout.fragment_frag_ver_perfil_publico);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);
        themeManager = new ThemeManager(requireContext());
        new BotonesMenuSuperior(this);
        root = view;

        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        LikeStateManager.setCurrentUserId(idUsuarioLogueado);
        rolUsuarioLogueado = prefs.getString("rol", "");
        idArtista = getArguments() != null ? getArguments().getInt("idArtista", -1) : -1;

        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        solicitudesApi = RetrofitClient.getClient().create(SolicitudesApi.class);
        usuarioApi = RetrofitClient.getClient().create(UsuarioApi.class);

        bindViews();
        aplicarTemaVisualPublico();
        setupExpandCollapse();
        setupTabs();
        setupRecycler();
        cargarPerfilPublico();
    }

    @Override
    public void onResume() {
        super.onResume();
        themeManager = new ThemeManager(requireContext());
        aplicarTemaVisualPublico();
    }

    private void bindViews() {
        cardPerfil = root.findViewById(R.id.cardPerfilPublico);
        expandedInfo = root.findViewById(R.id.expandedInfoPublico);
        frameFotoPerfilPublico = root.findViewById(R.id.frameFotoPerfilPublico);
        imgPerfil = root.findViewById(R.id.imgPerfilPublico);
        tvUsuario = root.findViewById(R.id.tvNombrePublico);
        tvDescripcion = root.findViewById(R.id.tvDescripcionPublico);
        tvNombreCompleto = root.findViewById(R.id.tvNombreCompletoPublico);
        tvRedes = root.findViewById(R.id.tvRedesPublico);
        tvFecha = root.findViewById(R.id.tvFecNacPublico);
        tvCategorias = root.findViewById(R.id.tvCategoriaPublico);
        tvUbicacion = root.findViewById(R.id.tvUbicacionPublico);
        tabsContainer = root.findViewById(R.id.tabsContainer);
        indicator = root.findViewById(R.id.tabIndicator);
        btnTabObras = root.findViewById(R.id.btnTabObras);
        btnTabServicios = root.findViewById(R.id.btnTabServicios);
        btnReportarUsuario = root.findViewById(R.id.btnReportarUsuarioPublico);
        contenedorRedesChips = root.findViewById(R.id.contenedorRedesChipsPublico);
        recyclerPublico = root.findViewById(R.id.recyclerPublico);
        scrollPerfilPublico = root.findViewById(R.id.scrollPerfilPublico);
        tvVacio = root.findViewById(R.id.tvPublicoVacio);
    }

    private void aplicarTemaVisualPublico() {
        if (themeManager == null) {
            return;
        }
        if (root != null && root.getBackground() != null) {
            root.getBackground().setColorFilter(themeManager.color(ThemeKeys.BG_MID), PorterDuff.Mode.SRC_ATOP);
        }
        if (cardPerfil instanceof CardView) {
            CardView card = (CardView) cardPerfil;
            ThemeApplier.applyCard(card, themeManager, ThemeKeys.ACCOUNT_GLASS_PANEL);
            card.setRadius(dpToPx(22));
            card.setCardElevation(dpToPx(6));
        }
        if (cardPerfil instanceof MaterialCardView) {
            MaterialCardView materialCard = (MaterialCardView) cardPerfil;
            materialCard.setStrokeColor(themeManager.color(ThemeKeys.ACCOUNT_GLASS_STROKE));
            materialCard.setStrokeWidth(dpToPx(1));
        }
        if (frameFotoPerfilPublico != null) {
            frameFotoPerfilPublico.setBackground(crearFondoOval(
                    ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.ACCENT_PRIMARY), 38),
                    themeManager.color(ThemeKeys.CARD_BORDER),
                    2
            ));
        }
        if (tvDescripcion != null) {
            tvDescripcion.setBackground(null);
        }
        if (expandedInfo != null) {
            expandedInfo.setBackground(null);
        }
        aplicarColorTextoRecursivo(cardPerfil, themeManager.color(ThemeKeys.TEXT_PRIMARY));
        ThemeApplier.applyTextPrimary(tvUsuario, themeManager);
        ThemeApplier.applyTextSecondary(tvDescripcion, themeManager);
        ThemeApplier.applyTextSecondary(tvNombreCompleto, themeManager);
        ThemeApplier.applyTextSecondary(tvFecha, themeManager);
        ThemeApplier.applyTextSecondary(tvCategorias, themeManager);
        ThemeApplier.applyTextSecondary(tvUbicacion, themeManager);
        ThemeApplier.applyTextSecondary(tvRedes, themeManager);
        ThemeApplier.applyTextSecondary(tvVacio, themeManager);
        aplicarTemaBotonReportar();
        aplicarTemaTabsPublicos();
        actualizarDivisor(R.id.dividerPublicoTop);
        actualizarDivisor(R.id.dividerPublicoInfo);
        actualizarDivisor(R.id.dividerPublicoDetalles);
        actualizarPresentacionRedes(redesPublicoActual);
    }

    private void aplicarTemaBotonReportar() {
        if (btnReportarUsuario == null || themeManager == null) {
            return;
        }
        int bg = ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_BG), 225);
        int stroke = ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_STROKE), 185);
        btnReportarUsuario.setBackgroundTintList(null);
        btnReportarUsuario.setBackground(crearFondoRedondeado(bg, stroke, 1, 14));
        btnReportarUsuario.setTextColor(elegirColorConContraste(
                bg,
                4.5d,
                themeManager.color(ThemeKeys.TEXT_PRIMARY),
                themeManager.color(ThemeKeys.TEXT_SECONDARY),
                themeManager.color(ThemeKeys.BUTTON_TEXT_DARK),
                themeManager.color(ThemeKeys.BUTTON_TEXT_LIGHT),
                themeManager.color(ThemeKeys.ICON_ACTIVE)
        ));
    }

    private void actualizarDivisor(int id) {
        if (root == null || themeManager == null) {
            return;
        }
        View divider = root.findViewById(id);
        if (divider != null) {
            divider.setBackgroundColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_BORDER), 90));
        }
    }

    private void actualizarPresentacionRedes(@Nullable String redes) {
        redesPublicoActual = redes == null ? "" : redes;
        if (contenedorRedesChips == null || themeManager == null) {
            if (tvRedes != null) {
                tvRedes.setText(redesPublicoActual.trim().isEmpty() ? "Sin redes" : redesPublicoActual);
            }
            return;
        }

        List<String> items = SocialNetworkHelper.separarRedes(redesPublicoActual);
        contenedorRedesChips.removeAllViews();
        if (tvRedes != null) {
            tvRedes.setVisibility(View.GONE);
        }

        contenedorRedesChips.setVisibility(View.VISIBLE);
        if (items.isEmpty()) {
            LinearLayout row = crearFilaRedes();
            row.addView(crearChipRedSocial("Sin redes", R.drawable.ic_social_link, null));
            contenedorRedesChips.addView(row);
            return;
        }

        LinearLayout filaActual = null;
        int maxChipsVisibles = 4;
        int visibles = redesExpandidas ? items.size() : Math.min(items.size(), maxChipsVisibles);
        for (int i = 0; i < visibles; i++) {
            if (i % 2 == 0) {
                filaActual = crearFilaRedes();
                contenedorRedesChips.addView(filaActual);
            }
            String item = items.get(i);
            boolean chipMas = !redesExpandidas && i == maxChipsVisibles - 1 && items.size() > maxChipsVisibles;
            String etiqueta = chipMas
                    ? "+" + (items.size() - maxChipsVisibles + 1)
                    : SocialNetworkHelper.crearEtiquetaCorta(item);
            int icono = chipMas
                    ? R.drawable.ic_social_link
                    : SocialNetworkHelper.resolverIconoRedSocial(item);
            filaActual.addView(crearChipRedSocial(etiqueta, icono, chipMas ? this::toggleRedesExpandidas : null));
        }
        if (redesExpandidas && items.size() > maxChipsVisibles) {
            if (visibles % 2 == 0) {
                filaActual = crearFilaRedes();
                contenedorRedesChips.addView(filaActual);
            }
            if (filaActual != null) {
                filaActual.addView(crearChipRedSocial("Ver menos", R.drawable.ic_social_link, this::toggleRedesExpandidas));
            }
        }
    }

    private void toggleRedesExpandidas() {
        redesExpandidas = !redesExpandidas;
        actualizarPresentacionRedes(redesPublicoActual);
    }

    @NonNull
    private LinearLayout crearFilaRedes() {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.START);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dpToPx(6);
        row.setLayoutParams(params);
        return row;
    }

    @NonNull
    private LinearLayout crearChipRedSocial(@NonNull String texto, int iconRes, @Nullable Runnable onClick) {
        LinearLayout chip = new LinearLayout(requireContext());
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setPadding(dpToPx(9), dpToPx(6), dpToPx(10), dpToPx(6));

        int chipFill = ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_CHIP_BG), 220);
        int chipStroke = ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_STROKE), 150);
        int iconColor = elegirColorConContraste(
                chipFill,
                3.0d,
                themeManager.color(ThemeKeys.ICON_ACTIVE),
                themeManager.color(ThemeKeys.TEXT_PRIMARY),
                themeManager.color(ThemeKeys.TEXT_SECONDARY),
                themeManager.color(ThemeKeys.BUTTON_TEXT_DARK),
                themeManager.color(ThemeKeys.BUTTON_TEXT_LIGHT)
        );
        int textColor = elegirColorConContraste(
                chipFill,
                4.5d,
                themeManager.color(ThemeKeys.TEXT_PRIMARY),
                themeManager.color(ThemeKeys.TEXT_SECONDARY),
                themeManager.color(ThemeKeys.BUTTON_TEXT_DARK),
                themeManager.color(ThemeKeys.BUTTON_TEXT_LIGHT),
                themeManager.color(ThemeKeys.ICON_ACTIVE)
        );

        chip.setBackground(crearFondoRedondeado(chipFill, chipStroke, 1, 14));
        if (onClick != null) {
            chip.setClickable(true);
            chip.setFocusable(true);
            chip.setOnClickListener(v -> onClick.run());
        }

        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(0, dpToPx(34), 1f);
        chipParams.setMarginEnd(dpToPx(6));
        chip.setLayoutParams(chipParams);

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dpToPx(16), dpToPx(16));
        iconParams.setMarginEnd(dpToPx(6));
        chip.addView(icon, iconParams);

        TextView label = new TextView(requireContext());
        label.setText(texto);
        label.setTextColor(textColor);
        label.setTextSize(12);
        label.setSingleLine(true);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        label.setIncludeFontPadding(false);
        chip.addView(label, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return chip;
    }

    private GradientDrawable crearFondoRedondeado(int fillColor, int strokeColor, int strokeDp, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dpToPx(radiusDp));
        drawable.setStroke(dpToPx(strokeDp), strokeColor);
        return drawable;
    }

    private GradientDrawable crearFondoOval(int fillColor, int strokeColor, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(fillColor);
        drawable.setStroke(dpToPx(strokeDp), strokeColor);
        return drawable;
    }

    private int elegirColorConContraste(int backgroundColor, double minContrast, int preferredColor, int... candidates) {
        int opaqueBackground = asegurarBackgroundOpacoParaContraste(backgroundColor);
        if (contrasteSeguro(preferredColor, opaqueBackground) >= minContrast) {
            return preferredColor;
        }
        int selected = preferredColor;
        double bestContrast = contrasteSeguro(preferredColor, opaqueBackground);
        for (int candidate : candidates) {
            double contrast = contrasteSeguro(candidate, opaqueBackground);
            if (contrast > bestContrast) {
                bestContrast = contrast;
                selected = candidate;
            }
        }
        if (bestContrast >= minContrast) {
            return selected;
        }
        double contrastWhite = contrasteSeguro(Color.WHITE, opaqueBackground);
        double contrastBlack = contrasteSeguro(Color.BLACK, opaqueBackground);
        return contrastWhite >= contrastBlack ? Color.WHITE : Color.BLACK;
    }

    private int asegurarBackgroundOpacoParaContraste(int backgroundColor) {
        if (Color.alpha(backgroundColor) == 255) {
            return backgroundColor;
        }
        int baseColor = themeManager != null
                ? themeManager.color(ThemeKeys.BG_MID)
                : Color.BLACK;
        int opaqueBase = ColorUtils.setAlphaComponent(baseColor, 255);
        return ColorUtils.compositeColors(backgroundColor, opaqueBase);
    }

    private double contrasteSeguro(int foregroundColor, int opaqueBackgroundColor) {
        try {
            return ColorUtils.calculateContrast(foregroundColor, ColorUtils.setAlphaComponent(opaqueBackgroundColor, 255));
        } catch (IllegalArgumentException ignored) {
            return 0d;
        }
    }

    private void aplicarColorTextoRecursivo(@Nullable View view, int textColor) {
        if (view == null) {
            return;
        }
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(textColor);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                aplicarColorTextoRecursivo(group.getChildAt(i), textColor);
            }
        }
    }

    private void setupRecycler() {
        recyclerPublico.setLayoutManager(new LinearLayoutManager(requireContext()));
        obraAdapter = new TarjetaTextoObraAdapter(new ArrayList<>(), requireContext());
        servicioAdapter = new TarjetaTextoServicioAdapter(new ArrayList<>(), requireContext());
        servicioAdapter.setCurrentUserId(idUsuarioLogueado);
        obraAdapter.setOnLikeClickListener(this::toggleLikeObra);
        obraAdapter.setOnPrimaryActionClickListener(this::solicitarCompraDesdePerfilPublico);
        servicioAdapter.setOnLikeClickListener(this::toggleLikeServicio);
    }

    private void solicitarCompraDesdePerfilPublico(TarjetaTextoObraItem obraItem, int position) {
        SolicitudCompraUiHelper.mostrarDialogoSolicitudCompra(
                this,
                idUsuarioLogueado,
                solicitudesApi,
                obraItem,
                this::cargarPerfilPublico
        );
    }

    private void toggleLikeObra(TarjetaTextoObraItem item, int position) {
        if (item == null || item.getIdObra() <= 0) {
            return;
        }
        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idObra = item.getIdObra();
        cambiarFavoritoObra(item, position, dto);
    }

    private void cambiarFavoritoObra(TarjetaTextoObraItem item, int pos, FavoritoDTO dto) {
        String llave = "obra:" + item.getIdObra();
        if (!puedeProcesarLike(llave)) {
            return;
        }
        if (!LikeStateManager.beginObraRequest(item.getIdObra())) {
            likesEnCurso.remove(llave);
            return;
        }

        boolean previo = item.isUserLiked();
        int likesPrevios = item.getLikes();
        item.setUserLiked(!previo);
        item.setLikes(Math.max(0, likesPrevios + (previo ? -1 : 1)));
        LikeStateManager.setObraState(item.getIdObra(), item.isUserLiked(), item.getLikes());
        obraAdapter.updateLikeStateById(item.getIdObra(), item.isUserLiked(), item.getLikes());

        Call<Void> call = previo ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                likesEnCurso.remove(llave);
                LikeStateManager.finishObraRequest(item.getIdObra());
                if (!response.isSuccessful()) {
                    if (!previo && response.code() == 409) {
                        item.setUserLiked(true);
                        item.setLikes(Math.max(0, likesPrevios + 1));
                        LikeStateManager.setObraState(item.getIdObra(), true, item.getLikes());
                        obraAdapter.updateLikeStateById(item.getIdObra(), true, item.getLikes());
                        return;
                    }
                    revertir();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                likesEnCurso.remove(llave);
                LikeStateManager.finishObraRequest(item.getIdObra());
                revertir();
            }

            private void revertir() {
                item.setUserLiked(previo);
                item.setLikes(likesPrevios);
                LikeStateManager.setObraState(item.getIdObra(), previo, likesPrevios);
                obraAdapter.updateLikeStateById(item.getIdObra(), previo, likesPrevios);
            }
        });
    }

    private void toggleLikeServicio(TarjetaTextoServicioItem item, int position) {
        if (item.getIdServicio() == null) {
            return;
        }
        String llave = "servicio:" + item.getIdServicio();
        if (!puedeProcesarLike(llave)) {
            return;
        }

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idServicio = item.getIdServicio();

        boolean previo = item.isFavorito();
        int likesPrevios = item.getLikes();
        item.setFavorito(!previo);
        item.setLikes(Math.max(0, likesPrevios + (previo ? -1 : 1)));
        servicioAdapter.notifyLikeChanged(position);

        Call<Void> call = previo ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                likesEnCurso.remove(llave);
                if (!response.isSuccessful()) {
                    revertir();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                likesEnCurso.remove(llave);
                revertir();
            }

            private void revertir() {
                item.setFavorito(previo);
                item.setLikes(likesPrevios);
                servicioAdapter.notifyLikeChanged(position);
            }
        });
    }

    private boolean puedeProcesarLike(@NonNull String llave) {
        long ahora = System.currentTimeMillis();
        Long ultimo = ultimoToqueLike.get(llave);
        if (ultimo != null && (ahora - ultimo) < LIKE_THROTTLE_MS) {
            return false;
        }
        if (likesEnCurso.contains(llave)) {
            return false;
        }
        ultimoToqueLike.put(llave, ahora);
        likesEnCurso.add(llave);
        return true;
    }

    private void cargarPerfilPublico() {
        configurarBotonReportarUsuario(null);
        if (idArtista <= 0) {
            Toast.makeText(getContext(), "Artista inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        usuarioApi.obtenerPerfilPublicoArtista(idArtista, idUsuarioLogueado > 0 ? idUsuarioLogueado : null)
                .enqueue(new Callback<PerfilPublicoArtistaDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<PerfilPublicoArtistaDTO> call, @NonNull Response<PerfilPublicoArtistaDTO> response) {
                        if (!isAdded()) {
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            String mensaje = response.code() == 404
                                    ? "Este perfil ya no está disponible."
                                    : "No se pudo cargar el perfil público";
                            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        renderizarPerfil(response.body());
                    }

                    @Override
                    public void onFailure(@NonNull Call<PerfilPublicoArtistaDTO> call, @NonNull Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Error de red al cargar perfil", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void renderizarPerfil(PerfilPublicoArtistaDTO perfil) {
        if (perfil.getIdUsuario() != null && perfil.getIdUsuario() > 0) {
            idArtista = perfil.getIdUsuario();
        }

        tvUsuario.setText(safe(perfil.getUsuario(), "usuario"));
        tvDescripcion.setText(safe(perfil.getDescripcion(), "Sin descripción"));
        tvNombreCompleto.setText("Nombre: " + safe(perfil.getNombreCompleto(), "No disponible"));
        tvRedes.setText("Redes: " + safe(perfil.getRedesSociales(), "Sin redes"));
        redesExpandidas = false;
        actualizarPresentacionRedes(perfil.getRedesSociales());
        tvFecha.setText("Fecha nac.: " + safe(perfil.getFechaNacimiento(), "No disponible"));
        tvUbicacion.setText("Ubicación: " + safe(perfil.getUbicacion(), "No especificada"));
        tvCategorias.setText("Ocupación: " + safe(perfil.getOcupacion(), "Sin ocupación"));
        cargarOcupacionUsuarioConsultado();
        configurarBotonReportarUsuario(safe(perfil.getUsuario(), "usuario"));

        Glide.with(this)
                .load(perfil.getFotoPerfil())
                .placeholder(R.drawable.fotoperfilprueba)
                .error(R.drawable.fotoperfilprueba)
                .into(imgPerfil);

        obras = convertirObras(perfil.getObras());
        actualizarOwnedObraIdsPerfilPublico();
        servicios = convertirServicios(perfil.getServicios());
        mostrarObras();
    }

    private void actualizarOwnedObraIdsPerfilPublico() {
        Set<Integer> ownedObraIds = new HashSet<>();
        if (idUsuarioLogueado > 0 && idArtista > 0 && idUsuarioLogueado == idArtista) {
            for (TarjetaTextoObraItem obra : obras) {
                if (obra != null && obra.getIdObra() > 0) {
                    ownedObraIds.add(obra.getIdObra());
                }
            }
        }
        if (obraAdapter != null) {
            obraAdapter.setOwnedObraIds(ownedObraIds);
        }
    }

    private void configurarBotonReportarUsuario(@Nullable String nombreUsuarioArtista) {
        if (btnReportarUsuario == null) {
            return;
        }

        boolean mostrar = idUsuarioLogueado > 0
                && ReporteUiPermissions.esRolUsuarioReportanteValido(rolUsuarioLogueado)
                && idArtista > 0
                && idArtista != idUsuarioLogueado
                && nombreUsuarioArtista != null
                && !nombreUsuarioArtista.trim().isEmpty();

        btnReportarUsuario.setVisibility(mostrar ? View.VISIBLE : View.GONE);

        if (!mostrar) {
            btnReportarUsuario.setOnClickListener(null);
            return;
        }

        String nombreSeguro = safe(nombreUsuarioArtista, "usuario");
        btnReportarUsuario.setOnClickListener(v -> {
            DialogReportarContenido dialog = DialogReportarContenido.newInstance(
                    "USUARIO",
                    idArtista,
                    idUsuarioLogueado,
                    nombreSeguro
            );
            dialog.show(getParentFragmentManager(), "DialogReportarContenido");
        });
    }

    private void cargarOcupacionUsuarioConsultado() {
        if (idArtista <= 0 || usuarioApi == null) {
            return;
        }
        usuarioApi.obtenerUsuarioPorId(idArtista, idUsuarioLogueado > 0 ? idUsuarioLogueado : null)
                .enqueue(new Callback<UsuariosDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<UsuariosDTO> call, @NonNull Response<UsuariosDTO> response) {
                        if (!isAdded() || !response.isSuccessful() || response.body() == null) {
                            return;
                        }
                        String ocupacion = safe(response.body().getCategoria(), "Sin ocupación");
                        tvCategorias.setText("Ocupación: " + ocupacion);
                    }

                    @Override
                    public void onFailure(@NonNull Call<UsuariosDTO> call, @NonNull Throwable t) {
                        // fallback: ya hay un valor renderizado.
                    }
                });
    }

    private List<TarjetaTextoObraItem> convertirObras(List<ObraDTO> dtoList) {
        List<TarjetaTextoObraItem> items = new ArrayList<>();
        if (dtoList == null) {
            return items;
        }
        for (ObraDTO dto : dtoList) {
            int idObra = dto.getIdObra() != null ? dto.getIdObra() : -1;
            int likesBackend = dto.getLikes() != null ? dto.getLikes() : 0;
            LikeStateManager.LikeState likeState = LikeStateManager.resolveObraState(
                    idObra,
                    Boolean.TRUE.equals(dto.getEsFavorito()),
                    likesBackend
            );
            TarjetaTextoObraItem item = new TarjetaTextoObraItem(
                    idObra,
                    dto.getTitulo(),
                    dto.getDescripcion(),
                    dto.getEstado(),
                    dto.getPrecio(),
                    dto.getImagen1(),
                    dto.getImagen2(),
                    dto.getImagen3(),
                    dto.getTecnicas(),
                    dto.getMedidas(),
                    likeState.getLikesCount(),
                    dto.getNombreAutor(),
                    dto.getNombreCategoria(),
                    dto.getFotoPerfilAutor(),
                    likeState.isLiked(),
                    false
            );
            item.setEditable(!Boolean.FALSE.equals(dto.getEditable()));
            item.setEliminable(!Boolean.FALSE.equals(dto.getEliminable()));
            item.setPuedeSolicitarCompra(Boolean.TRUE.equals(dto.getPuedeSolicitarCompra()));
            item.setIdAutor(dto.getIdUsuario());
            items.add(item);
        }
        return items;
    }

    private List<TarjetaTextoServicioItem> convertirServicios(List<ServicioDTO> dtoList) {
        List<TarjetaTextoServicioItem> items = new ArrayList<>();
        if (dtoList == null) {
            return items;
        }
        for (ServicioDTO dto : dtoList) {
            items.add(new TarjetaTextoServicioItem(
                    dto.getIdServicio(),
                    dto.getIdUsuario(),
                    dto.getTitulo(),
                    dto.getDescripcion(),
                    dto.getContacto(),
                    dto.getTipoContacto(),
                    dto.getTecnicas(),
                    dto.getNombreUsuario(),
                    dto.getCategoria(),
                    dto.getFotoPerfilAutor(),
                    dto.getPrecioMin(),
                    dto.getPrecioMax(),
                    dto.getLikes() != null ? dto.getLikes() : 0,
                    Boolean.TRUE.equals(dto.getEsFavorito()),
                    false
            ));
        }
        return items;
    }

    private void setupExpandCollapse() {
        cardPerfil.setOnClickListener(v -> toggleExpand());
        root.setOnTouchListener((v, event) -> {
            if (manejarSwipeSecciones(event)) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN && expandido) {
                Rect rect = new Rect();
                cardPerfil.getGlobalVisibleRect(rect);
                if (!rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    colapsar();
                    return true;
                }
            }
            return false;
        });
        View.OnTouchListener swipeListener = (v, event) -> manejarSwipeSecciones(event);
        if (scrollPerfilPublico != null) {
            scrollPerfilPublico.setOnTouchListener(swipeListener);
        }
        if (recyclerPublico != null) {
            recyclerPublico.setOnTouchListener(swipeListener);
        }
    }

    private void toggleExpand() {
        if (expandido) {
            colapsar();
        } else {
            expandir();
        }
    }

    private void expandir() {
        expandido = true;
        expandedInfo.setVisibility(View.VISIBLE);
    }

    private void colapsar() {
        expandido = false;
        expandedInfo.setVisibility(View.GONE);
    }

    private void setupTabs() {
        tabsContainer.post(() -> {
            int w = tabsContainer.getWidth();
            if (w <= 0) {
                return;
            }
            indicator.getLayoutParams().width = w / 2;
            indicator.requestLayout();
            aplicarTemaTabsPublicos();
            moverIndicador(true, false);
        });

        btnTabObras.setOnClickListener(v -> {
            if (!mostrandoObras) {
                mostrarObras();
            }
        });
        btnTabServicios.setOnClickListener(v -> {
            if (mostrandoObras) {
                mostrarServicios();
            }
        });
    }

    private boolean manejarSwipeSecciones(@NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                swipeStartX = event.getX();
                swipeStartY = event.getY();
                swipeHorizontalDetectado = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveDx = event.getX() - swipeStartX;
                float moveDy = event.getY() - swipeStartY;
                if (Math.abs(moveDx) > dpToPx(28) && Math.abs(moveDx) > Math.abs(moveDy) * 1.35f) {
                    swipeHorizontalDetectado = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                float dx = event.getX() - swipeStartX;
                float dy = event.getY() - swipeStartY;
                if (Math.abs(dx) > dpToPx(72) && Math.abs(dx) > Math.abs(dy) * 1.35f) {
                    if (dx < 0 && mostrandoObras) {
                        mostrarServicios();
                    } else if (dx > 0 && !mostrandoObras) {
                        mostrarObras();
                    }
                    swipeHorizontalDetectado = false;
                    return true;
                }
                swipeHorizontalDetectado = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                swipeHorizontalDetectado = false;
                break;
            default:
                break;
        }
        return swipeHorizontalDetectado;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void aplicarTemaTabsPublicos() {
        if (themeManager == null) {
            return;
        }
        if (tabsContainer != null && tabsContainer.getBackground() != null) {
            tabsContainer.getBackground().setColorFilter(themeManager.color(ThemeKeys.FILTER_BUTTON_BG), PorterDuff.Mode.SRC_ATOP);
        }
        if (indicator != null && indicator.getBackground() != null) {
            indicator.getBackground().setColorFilter(themeManager.color(ThemeKeys.ACCENT_PRIMARY), PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void mostrarObras() {
        mostrandoObras = true;
        moverIndicador(true, true);
        int selected = themeManager.color(ThemeKeys.TEXT_PRIMARY);
        int unselected = themeManager.color(ThemeKeys.TEXT_SECONDARY);
        btnTabObras.setTextColor(selected);
        btnTabServicios.setTextColor(unselected);
        obraAdapter.actualizarLista(obras);
        recyclerPublico.setAdapter(obraAdapter);
        tvVacio.setVisibility(obras.isEmpty() ? View.VISIBLE : View.GONE);
        tvVacio.setText("Este artista aún no tiene obras públicas.");
    }

    private void mostrarServicios() {
        mostrandoObras = false;
        moverIndicador(false, true);
        int selected = themeManager.color(ThemeKeys.TEXT_PRIMARY);
        int unselected = themeManager.color(ThemeKeys.TEXT_SECONDARY);
        btnTabServicios.setTextColor(selected);
        btnTabObras.setTextColor(unselected);
        servicioAdapter.actualizarLista(servicios);
        recyclerPublico.setAdapter(servicioAdapter);
        tvVacio.setVisibility(servicios.isEmpty() ? View.VISIBLE : View.GONE);
        tvVacio.setText("Este artista aún no tiene servicios públicos.");
    }

    private void moverIndicador(boolean aObras, boolean animar) {
        int w = tabsContainer.getWidth();
        float targetX = aObras ? 0f : (w / 2f);
        if (!animar) {
            indicator.setTranslationX(targetX);
            return;
        }
        indicator.animate()
                .translationX(targetX)
                .setDuration(220)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
    }

    private String safe(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }
}

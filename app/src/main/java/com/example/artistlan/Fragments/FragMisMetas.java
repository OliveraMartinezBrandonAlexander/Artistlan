package com.example.artistlan.Fragments;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.artistlan.Activitys.ActFragmentoPrincipal;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.MetaPersonalApi;
import com.example.artistlan.Conector.model.MetaPersonalCancelRequestDTO;
import com.example.artistlan.Conector.model.MetaPersonalDTO;
import com.example.artistlan.Conector.model.MetaPersonalRequestDTO;
import com.example.artistlan.Conector.model.MetaPersonalResumenDTO;
import com.example.artistlan.Conector.model.MetaPersonalUpdateDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.utils.ArtistlanDialogFactory;
import com.example.artistlan.utils.CardThemeHelper;
import com.example.artistlan.utils.DialogConfig;
import com.example.artistlan.utils.DialogThemeHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragMisMetas extends Fragment {

    private static final int TAB_ACTIVAS = 0;
    private static final int TAB_HISTORIAL = 1;
    private static final String FILTRO_TODOS = "TODOS";
    private static final String[] FILTRO_HISTORIAL_KEYS = {
            FILTRO_TODOS, "VENTAS", "INGRESOS", "PUBLICACIONES", "FAVORITOS"
    };
    private static final String[] FILTRO_HISTORIAL_LABELS = {
            "Todos", "Ventas", "Ingresos", "Publicaciones", "Favoritos"
    };
    private static final String[] TIPO_META_KEYS = {
            "VENTAS", "INGRESOS", "PUBLICACIONES", "FAVORITOS"
    };
    private static final String[] TIPO_META_LABELS = {
            "Ventas", "Ingresos", "Publicaciones", "Favoritos"
    };
    private static final DateTimeFormatter DATE_SOURCE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_OUTPUT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "MX"));
    private static final DateTimeFormatter DATE_DIALOG_OUTPUT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es", "MX"));

    private ThemeManager themeManager;
    private MetaPersonalApi metaPersonalApi;

    private NestedScrollView scrollMisMetas;
    private TextView tvTitulo;
    private TextView tvSubtitulo;
    private TextView tvResumenActivasValor;
    private TextView tvResumenCompletadasValor;
    private TextView tvResumenExpiradasValor;
    private TextView tvResumenCanceladasValor;
    private TextView tvResumenActivasLabel;
    private TextView tvResumenCompletadasLabel;
    private TextView tvResumenExpiradasLabel;
    private TextView tvResumenCanceladasLabel;
    private View segmentContainer;
    private View segmentIndicator;
    private Button btnCrearMeta;
    private Button btnTabActivas;
    private Button btnTabHistorial;
    private TextView tvSeccionTitulo;
    private TextView tvSeccionSubtitulo;
    private MaterialCardView cardFiltroHistorial;
    private TextView tvFiltroHistorialLabel;
    private Spinner spinnerFiltroHistorial;
    private LinearLayout containerMetas;
    private View layoutEstado;
    private TextView tvEstadoTitulo;
    private TextView tvEstadoMensaje;
    private Button btnReintentar;
    private View layoutLoading;
    private ProgressBar progressMisMetas;
    private TextView tvLoading;

    private final List<MetaPersonalDTO> metas = new ArrayList<>();
    private MetaPersonalResumenDTO resumen;
    private int tabSeleccionada = TAB_ACTIVAS;
    private boolean metasCargadas = false;
    private String filtroTipoHistorial = FILTRO_TODOS;
    private boolean ignorarCambiosFiltroHistorial = false;
    @Nullable private Call<List<MetaPersonalDTO>> metasCall;
    @Nullable private Call<MetaPersonalResumenDTO> resumenCall;
    @Nullable private Call<MetaPersonalDTO> metaMutationCall;
    @Nullable private AlertDialog dialogMetaActual;
    @Nullable private AlertDialog dialogCancelacionActual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_mis_metas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);
        themeManager = new ThemeManager(requireContext());
        metaPersonalApi = RetrofitClient.getClient().create(MetaPersonalApi.class);

        new BotonesMenuSuperior(this);
        bindViews(view);
        configurarTabs();
        aplicarTema();
        cargarDatos();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isAdded()) {
            return;
        }
        themeManager = new ThemeManager(requireContext());
        aplicarTema();
        renderContenido();
    }

    @Override
    public void onDestroyView() {
        if (metasCall != null) {
            metasCall.cancel();
            metasCall = null;
        }
        if (resumenCall != null) {
            resumenCall.cancel();
            resumenCall = null;
        }
        if (metaMutationCall != null) {
            metaMutationCall.cancel();
            metaMutationCall = null;
        }
        if (dialogMetaActual != null) {
            dialogMetaActual.dismiss();
            dialogMetaActual = null;
        }
        if (dialogCancelacionActual != null) {
            dialogCancelacionActual.dismiss();
            dialogCancelacionActual = null;
        }
        super.onDestroyView();
    }

    private void bindViews(@NonNull View view) {
        scrollMisMetas = view.findViewById(R.id.scrollMisMetas);
        tvTitulo = view.findViewById(R.id.tvMisMetasTitulo);
        tvSubtitulo = view.findViewById(R.id.tvMisMetasSubtitulo);
        tvResumenActivasValor = view.findViewById(R.id.tvResumenActivasValor);
        tvResumenCompletadasValor = view.findViewById(R.id.tvResumenCompletadasValor);
        tvResumenExpiradasValor = view.findViewById(R.id.tvResumenExpiradasValor);
        tvResumenCanceladasValor = view.findViewById(R.id.tvResumenCanceladasValor);
        tvResumenActivasLabel = view.findViewById(R.id.tvResumenActivasLabel);
        tvResumenCompletadasLabel = view.findViewById(R.id.tvResumenCompletadasLabel);
        tvResumenExpiradasLabel = view.findViewById(R.id.tvResumenExpiradasLabel);
        tvResumenCanceladasLabel = view.findViewById(R.id.tvResumenCanceladasLabel);
        segmentContainer = view.findViewById(R.id.segmentContainerMisMetas);
        segmentIndicator = view.findViewById(R.id.segmentIndicatorMisMetas);
        btnCrearMeta = view.findViewById(R.id.btnCrearMeta);
        btnTabActivas = view.findViewById(R.id.btnSegmentActivas);
        btnTabHistorial = view.findViewById(R.id.btnSegmentHistorial);
        tvSeccionTitulo = view.findViewById(R.id.tvMetasSeccionTitulo);
        tvSeccionSubtitulo = view.findViewById(R.id.tvMetasSeccionSubtitulo);
        cardFiltroHistorial = view.findViewById(R.id.cardFiltroHistorialMetas);
        tvFiltroHistorialLabel = view.findViewById(R.id.tvFiltroHistorialLabel);
        spinnerFiltroHistorial = view.findViewById(R.id.spinnerFiltroHistorialMetas);
        containerMetas = view.findViewById(R.id.containerMetasList);
        layoutEstado = view.findViewById(R.id.layoutEstadoMetas);
        tvEstadoTitulo = view.findViewById(R.id.tvEstadoMetasTitulo);
        tvEstadoMensaje = view.findViewById(R.id.tvEstadoMetasMensaje);
        btnReintentar = view.findViewById(R.id.btnMetasReintentar);
        layoutLoading = view.findViewById(R.id.layoutMisMetasLoading);
        progressMisMetas = view.findViewById(R.id.progressMisMetas);
        tvLoading = view.findViewById(R.id.tvMisMetasLoading);
    }

    private void configurarTabs() {
        btnCrearMeta.setOnClickListener(v -> abrirDialogoMeta(null));
        btnTabActivas.setOnClickListener(v -> seleccionarTab(TAB_ACTIVAS));
        btnTabHistorial.setOnClickListener(v -> seleccionarTab(TAB_HISTORIAL));
        configurarFiltroHistorial();
        segmentContainer.post(() -> seleccionarTab(tabSeleccionada));
        btnReintentar.setOnClickListener(v -> cargarDatos());
    }

    private void configurarFiltroHistorial() {
        if (spinnerFiltroHistorial == null) {
            return;
        }
        spinnerFiltroHistorial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ignorarCambiosFiltroHistorial) {
                    return;
                }
                String nuevoFiltro = obtenerFiltroHistorialKey(position);
                if (!nuevoFiltro.equals(filtroTipoHistorial)) {
                    filtroTipoHistorial = nuevoFiltro;
                }
                if (tabSeleccionada == TAB_HISTORIAL) {
                    renderContenido();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void cargarDatos() {
        mostrarLoading(true);
        cargarResumen();
        if (metasCall != null) {
            metasCall.cancel();
        }
        metasCall = metaPersonalApi.obtenerMisMetas();
        metasCall.enqueue(new Callback<List<MetaPersonalDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<MetaPersonalDTO>> call, @NonNull Response<List<MetaPersonalDTO>> response) {
                if (metasCall == call) {
                    metasCall = null;
                }
                if (!isAdded()) {
                    return;
                }
                mostrarLoading(false);
                if (!response.isSuccessful()) {
                    mostrarError(construirMensajeError(response));
                    return;
                }
                metas.clear();
                if (response.body() != null) {
                    metas.addAll(response.body());
                }
                metasCargadas = true;
                if (resumen == null) {
                    resumen = construirResumenFallback();
                }
                renderContenido();
            }

            @Override
            public void onFailure(@NonNull Call<List<MetaPersonalDTO>> call, @NonNull Throwable t) {
                if (metasCall == call) {
                    metasCall = null;
                }
                if (!isAdded()) {
                    return;
                }
                if (call.isCanceled()) {
                    return;
                }
                mostrarLoading(false);
                mostrarError("No pudimos cargar tus metas. Revisa tu conexión e intenta nuevamente.");
            }
        });
    }

    private void cargarResumen() {
        if (resumenCall != null) {
            resumenCall.cancel();
        }
        resumenCall = metaPersonalApi.obtenerResumenMisMetas();
        resumenCall.enqueue(new Callback<MetaPersonalResumenDTO>() {
            @Override
            public void onResponse(@NonNull Call<MetaPersonalResumenDTO> call, @NonNull Response<MetaPersonalResumenDTO> response) {
                if (resumenCall == call) {
                    resumenCall = null;
                }
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    resumen = response.body();
                } else if (metasCargadas) {
                    resumen = construirResumenFallback();
                }
                renderResumen();
            }

            @Override
            public void onFailure(@NonNull Call<MetaPersonalResumenDTO> call, @NonNull Throwable t) {
                if (resumenCall == call) {
                    resumenCall = null;
                }
                if (!isAdded()) {
                    return;
                }
                if (call.isCanceled()) {
                    return;
                }
                if (metasCargadas) {
                    resumen = construirResumenFallback();
                    renderResumen();
                }
            }
        });
    }

    private void seleccionarTab(int tab) {
        tabSeleccionada = tab;
        moverIndicador(tab);
        aplicarEstadoTabs();
        actualizarVisibilidadFiltroHistorial();
        renderContenido();
    }

    private void moverIndicador(int tab) {
        if (segmentContainer == null || segmentIndicator == null) {
            return;
        }
        int width = segmentContainer.getWidth();
        if (width <= 0) {
            return;
        }
        int innerWidth = width - segmentContainer.getPaddingLeft() - segmentContainer.getPaddingRight();
        int segmentWidth = innerWidth / 2;
        float targetX = segmentContainer.getPaddingLeft() + (segmentWidth * tab);
        ViewGroup.LayoutParams params = segmentIndicator.getLayoutParams();
        if (params.width != segmentWidth) {
            params.width = segmentWidth;
            segmentIndicator.setLayoutParams(params);
        }
        segmentIndicator.animate().cancel();
        segmentIndicator.animate().x(targetX).setDuration(220).start();
    }

    private void aplicarTema() {
        ThemeApplier.applyTextPrimary(tvTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvSubtitulo, themeManager);
        ThemeApplier.applyTextPrimary(tvSeccionTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvSeccionSubtitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvFiltroHistorialLabel, themeManager);
        ThemeApplier.applyTextPrimary(tvEstadoTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvEstadoMensaje, themeManager);
        ThemeApplier.applyTextSecondary(tvLoading, themeManager);
        CardThemeHelper.tintProgress(progressMisMetas, themeManager);
        CardThemeHelper.applyPrimaryBubbleButton(btnCrearMeta, themeManager);
        CardThemeHelper.applyPrimaryBubbleButton(btnReintentar, themeManager);

        aplicarTemaResumenCard(R.id.cardResumenActivas, tvResumenActivasValor, tvResumenActivasLabel);
        aplicarTemaResumenCard(R.id.cardResumenCompletadas, tvResumenCompletadasValor, tvResumenCompletadasLabel);
        aplicarTemaResumenCard(R.id.cardResumenExpiradas, tvResumenExpiradasValor, tvResumenExpiradasLabel);
        aplicarTemaResumenCard(R.id.cardResumenCanceladas, tvResumenCanceladasValor, tvResumenCanceladasLabel);

        if (segmentContainer != null && segmentContainer.getBackground() != null) {
            segmentContainer.getBackground().setColorFilter(
                    themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL),
                    PorterDuff.Mode.SRC_ATOP
            );
        }
        if (segmentIndicator != null && segmentIndicator.getBackground() != null) {
            segmentIndicator.getBackground().setColorFilter(
                    themeManager.color(ThemeKeys.ACCENT_PRIMARY),
                    PorterDuff.Mode.SRC_ATOP
            );
        }
        if (layoutEstado != null) {
            CardThemeHelper.applyGradientGlassCard(layoutEstado, themeManager, 22);
        }
        if (cardFiltroHistorial != null) {
            CardThemeHelper.applyGradientGlassCard(cardFiltroHistorial, themeManager, 20);
        }
        aplicarTemaFiltroHistorial();
        aplicarEstadoTabs();
        actualizarVisibilidadFiltroHistorial();
        renderResumen();
    }

    private void aplicarTemaFiltroHistorial() {
        if (spinnerFiltroHistorial == null || !isAdded()) {
            return;
        }
        ignorarCambiosFiltroHistorial = true;
        spinnerFiltroHistorial.setAdapter(DialogThemeHelper.createLightGlassComboAdapter(
                requireContext(),
                Arrays.asList(FILTRO_HISTORIAL_LABELS)
        ));
        DialogThemeHelper.applyLightGlassComboStyle(spinnerFiltroHistorial, requireContext());
        spinnerFiltroHistorial.setSelection(obtenerIndiceFiltroHistorial(), false);
        ignorarCambiosFiltroHistorial = false;
    }

    private void aplicarTemaResumenCard(int cardId, @Nullable TextView valor, @Nullable TextView label) {
        View card = requireView().findViewById(cardId);
        if (card != null) {
            CardThemeHelper.applyGradientGlassCard(card, themeManager, 20);
        }
        ThemeApplier.applyTextPrimary(valor, themeManager);
        ThemeApplier.applyTextSecondary(label, themeManager);
    }

    private void aplicarEstadoTabs() {
        int colorSeleccionado = themeManager.color(ThemeKeys.TEXT_PRIMARY);
        int colorInactivo = themeManager.color(ThemeKeys.TEXT_SECONDARY);
        if (btnTabActivas != null) {
            btnTabActivas.setBackgroundColor(Color.TRANSPARENT);
            btnTabActivas.setTextColor(tabSeleccionada == TAB_ACTIVAS ? colorSeleccionado : colorInactivo);
        }
        if (btnTabHistorial != null) {
            btnTabHistorial.setBackgroundColor(Color.TRANSPARENT);
            btnTabHistorial.setTextColor(tabSeleccionada == TAB_HISTORIAL ? colorSeleccionado : colorInactivo);
        }
    }

    private void renderContenido() {
        if (!isAdded()) {
            return;
        }
        renderResumen();
        if (!metasCargadas) {
            return;
        }

        actualizarVisibilidadFiltroHistorial();
        List<MetaPersonalDTO> metasBase = filtrarMetas(tabSeleccionada);
        List<MetaPersonalDTO> filtradas = tabSeleccionada == TAB_HISTORIAL
                ? aplicarFiltroHistorial(metasBase)
                : metasBase;
        containerMetas.removeAllViews();

        if (tabSeleccionada == TAB_ACTIVAS) {
            tvSeccionTitulo.setText("Metas activas");
            tvSeccionSubtitulo.setText("Tus objetivos vigentes se actualizan con progreso real de la plataforma.");
        } else {
            tvSeccionTitulo.setText("Historial de metas");
            tvSeccionSubtitulo.setText("Consulta tus metas completadas, expiradas o canceladas.");
        }

        if (filtradas.isEmpty()) {
            if (tabSeleccionada == TAB_ACTIVAS) {
                mostrarEstado(
                        "Sin metas en esta sección",
                        "Aún no tienes metas activas dentro de Artistlan.",
                        false
                );
            } else if (metasBase.isEmpty()) {
                mostrarEstado(
                        "Sin metas en esta sección",
                        "No hay metas en el historial.",
                        false
                );
            } else {
                mostrarEstado(
                        "Sin metas en esta sección",
                        "No hay metas en el historial de este tipo.",
                        false
                );
            }
            return;
        }

        ocultarEstado();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (MetaPersonalDTO meta : filtradas) {
            View item = inflater.inflate(R.layout.item_meta_personal, containerMetas, false);
            bindMetaItem(item, meta);
            containerMetas.addView(item);
        }
    }

    private void bindMetaItem(@NonNull View itemView, @NonNull MetaPersonalDTO meta) {
        MaterialCardView card = itemView.findViewById(R.id.cardMetaPersonal);
        ImageView ivTipo = itemView.findViewById(R.id.ivMetaTipo);
        FrameLayout bubbleIcon = itemView.findViewById(R.id.containerMetaTipoIcono);
        TextView tvTipo = itemView.findViewById(R.id.tvMetaTipo);
        TextView tvEstado = itemView.findViewById(R.id.tvMetaEstado);
        TextView tvObjetivo = itemView.findViewById(R.id.tvMetaObjetivo);
        TextView tvPeriodo = itemView.findViewById(R.id.tvMetaPeriodo);
        ProgressBar progressBar = itemView.findViewById(R.id.progressMeta);
        TextView tvPorcentaje = itemView.findViewById(R.id.tvMetaPorcentaje);
        TextView tvProgresoTexto = itemView.findViewById(R.id.tvMetaProgresoTexto);
        TextView tvMensajeEstado = itemView.findViewById(R.id.tvMetaMensajeEstado);
        View layoutAcciones = itemView.findViewById(R.id.layoutMetaAcciones);
        View spaceAcciones = itemView.findViewById(R.id.spaceMetaAcciones);
        Button btnEditar = itemView.findViewById(R.id.btnMetaEditar);
        Button btnCancelar = itemView.findViewById(R.id.btnMetaCancelar);

        MetaTipoStyle tipoStyle = obtenerEstiloTipo(meta.getTipoMeta());
        MetaEstadoStyle estadoStyle = obtenerEstiloEstado(meta.getEstado());

        CardThemeHelper.applyGradientGlassCard(card, themeManager, 24);
        card.setStrokeColor(ColorUtils.setAlphaComponent(tipoStyle.accentColor, 170));
        card.setStrokeWidth(dpToPx(1));

        bubbleIcon.setBackground(crearBubble(tipoStyle.softColor, 16));
        ivTipo.setImageResource(tipoStyle.iconRes);
        ivTipo.setColorFilter(tipoStyle.accentColor, PorterDuff.Mode.SRC_IN);

        ThemeApplier.applyTextPrimary(tvTipo, themeManager);
        ThemeApplier.applyTextSecondary(tvObjetivo, themeManager);
        ThemeApplier.applyTextSecondary(tvPeriodo, themeManager);
        ThemeApplier.applyTextPrimary(tvPorcentaje, themeManager);
        ThemeApplier.applyTextSecondary(tvProgresoTexto, themeManager);
        ThemeApplier.applyTextSecondary(tvMensajeEstado, themeManager);

        tvTipo.setText(tipoStyle.label);
        tvEstado.setText(estadoStyle.label);
        tvEstado.setTextColor(estadoStyle.textColor);
        tvEstado.setBackground(crearChip(estadoStyle.fillColor, estadoStyle.strokeColor));

        tvObjetivo.setText("Objetivo: " + formatearObjetivo(meta));
        tvPeriodo.setText("Período: " + formatearPeriodo(meta.getFechaInicio(), meta.getFechaFin()));
        tvPorcentaje.setText(formatearPorcentajeVisual(meta) + "%");
        tvProgresoTexto.setText(valorNoVacio(meta.getProgresoTexto(), "Sin progreso disponible"));
        tvMensajeEstado.setText(valorNoVacio(meta.getMensajeEstado(), mensajeEstadoFallback(meta.getEstado())));

        int progreso = calcularEnteroProgreso(meta);
        progressBar.setProgress(progreso);
        progressBar.setProgressTintList(ColorStateList.valueOf(tipoStyle.accentColor));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_CHIP_BG), 190)
        ));

        CardThemeHelper.applySecondaryBubbleButton(btnEditar, themeManager);
        CardThemeHelper.applySecondaryBubbleButton(btnCancelar, themeManager);
        prepararBotonesMeta(layoutAcciones, spaceAcciones, btnEditar, btnCancelar, meta);
    }

    private void prepararBotonesMeta(@Nullable View layoutAcciones,
                                     @Nullable View spaceAcciones,
                                     @NonNull Button btnEditar,
                                     @NonNull Button btnCancelar,
                                     @NonNull MetaPersonalDTO meta) {
        boolean permitirEditar = Boolean.TRUE.equals(meta.getEditable());
        boolean permitirCancelar = Boolean.TRUE.equals(meta.getCancelable());

        btnEditar.setVisibility(permitirEditar ? View.VISIBLE : View.GONE);
        btnCancelar.setVisibility(permitirCancelar ? View.VISIBLE : View.GONE);
        btnEditar.setEnabled(permitirEditar);
        btnCancelar.setEnabled(permitirCancelar);
        btnEditar.setAlpha(permitirEditar ? 1f : 0f);
        btnCancelar.setAlpha(permitirCancelar ? 1f : 0f);
        btnEditar.setOnClickListener(permitirEditar ? v -> abrirDialogoMeta(meta) : null);
        btnCancelar.setOnClickListener(permitirCancelar ? v -> abrirDialogoCancelar(meta) : null);

        if (spaceAcciones != null) {
            spaceAcciones.setVisibility(permitirEditar && permitirCancelar ? View.VISIBLE : View.GONE);
        }
        if (layoutAcciones != null) {
            layoutAcciones.setVisibility(permitirEditar || permitirCancelar ? View.VISIBLE : View.GONE);
        }
    }

    private void renderResumen() {
        MetaPersonalResumenDTO resumenActual = resumen != null ? resumen : construirResumenFallback();
        tvResumenActivasValor.setText(String.valueOf(valorSeguro(resumenActual.getActivas())));
        tvResumenCompletadasValor.setText(String.valueOf(valorSeguro(resumenActual.getCompletadas())));
        tvResumenExpiradasValor.setText(String.valueOf(valorSeguro(resumenActual.getExpiradas())));
        tvResumenCanceladasValor.setText(String.valueOf(valorSeguro(resumenActual.getCanceladas())));
    }

    private void abrirDialogoMeta(@Nullable MetaPersonalDTO metaEditando) {
        if (!isAdded()) {
            return;
        }
        boolean editando = metaEditando != null && metaEditando.getIdMeta() != null;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_meta_personal_form, null, false);
        View dialogRoot = dialogView.findViewById(R.id.layoutMetaDialogRoot);
        TextView tvBadge = dialogView.findViewById(R.id.tvMetaDialogBadge);
        TextView tvTituloDialog = dialogView.findViewById(R.id.tvMetaDialogTitulo);
        TextView tvSubtituloDialog = dialogView.findViewById(R.id.tvMetaDialogSubtitulo);
        TextView tvTipoLabel = dialogView.findViewById(R.id.tvMetaDialogTipoLabel);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerMetaDialogTipo);
        TextInputLayout tilObjetivo = dialogView.findViewById(R.id.tilMetaDialogObjetivo);
        TextInputEditText etObjetivo = dialogView.findViewById(R.id.etMetaDialogObjetivo);
        TextInputLayout tilFechaInicio = dialogView.findViewById(R.id.tilMetaDialogFechaInicio);
        TextInputEditText etFechaInicio = dialogView.findViewById(R.id.etMetaDialogFechaInicio);
        TextInputLayout tilFechaFin = dialogView.findViewById(R.id.tilMetaDialogFechaFin);
        TextInputEditText etFechaFin = dialogView.findViewById(R.id.etMetaDialogFechaFin);
        TextView tvAyuda = dialogView.findViewById(R.id.tvMetaDialogAyuda);
        ProgressBar progressDialog = dialogView.findViewById(R.id.progressMetaDialog);
        Button btnCancelarDialog = dialogView.findViewById(R.id.btnMetaDialogCancelar);
        Button btnGuardarDialog = dialogView.findViewById(R.id.btnMetaDialogGuardar);

        final LocalDate[] fechaInicioSeleccionada = {parseLocalDate(metaEditando != null ? metaEditando.getFechaInicio() : null)};
        final LocalDate[] fechaFinSeleccionada = {parseLocalDate(metaEditando != null ? metaEditando.getFechaFin() : null)};

        tvBadge.setText(editando ? "Edit" : "Meta");
        tvTituloDialog.setText(editando ? "Editar meta" : "Nueva meta");
        tvSubtituloDialog.setText(editando
                ? "Actualiza el tipo, objetivo y rango de tu meta personal."
                : "Crea una meta personal y deja que Artistlan siga su progreso.");
        if (metaEditando != null && metaEditando.getObjetivo() != null) {
            etObjetivo.setText(formatearNumero(metaEditando.getObjetivo()));
        }
        actualizarTextoFecha(etFechaInicio, fechaInicioSeleccionada[0]);
        actualizarTextoFecha(etFechaFin, fechaFinSeleccionada[0]);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialog.setOnShowListener(d -> {
            dialogMetaActual = dialog;
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            DialogThemeHelper.applyFieldDialogWindowSize(dialog, requireContext());
            dialogRoot.setBackground(DialogThemeHelper.createFieldDialogBackground(requireContext()));
            CardThemeHelper.applyPrimaryBubbleSurface(tvBadge, tvBadge, themeManager);
            ThemeApplier.applyTextPrimary(tvTituloDialog, themeManager);
            ThemeApplier.applyTextSecondary(tvSubtituloDialog, themeManager);
            ThemeApplier.applyTextSecondary(tvTipoLabel, themeManager);
            ThemeApplier.applyTextSecondary(tvAyuda, themeManager);
            DialogThemeHelper.applyLightGlassTextInputLayoutStyle(tilObjetivo, requireContext());
            DialogThemeHelper.applyLightGlassTextInputLayoutStyle(tilFechaInicio, requireContext());
            DialogThemeHelper.applyLightGlassTextInputLayoutStyle(tilFechaFin, requireContext());
            DialogThemeHelper.applyLightGlassTextInputEditTextStyle(etObjetivo, requireContext());
            DialogThemeHelper.applyLightGlassTextInputEditTextStyle(etFechaInicio, requireContext());
            DialogThemeHelper.applyLightGlassTextInputEditTextStyle(etFechaFin, requireContext());
            spinnerTipo.setAdapter(DialogThemeHelper.createLightGlassComboAdapter(
                    requireContext(),
                    Arrays.asList(TIPO_META_LABELS)
            ));
            DialogThemeHelper.applyLightGlassComboStyle(spinnerTipo, requireContext());
            CardThemeHelper.applySecondaryBubbleButton(btnCancelarDialog, themeManager);
            CardThemeHelper.applyPrimaryBubbleButton(btnGuardarDialog, themeManager);
            CardThemeHelper.tintProgress(progressDialog, themeManager);

            spinnerTipo.setSelection(obtenerIndiceTipoMeta(metaEditando != null ? metaEditando.getTipoMeta() : null), false);
            actualizarAyudaObjetivo(tilObjetivo, spinnerTipo.getSelectedItemPosition());
            spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    actualizarAyudaObjetivo(tilObjetivo, position);
                    tilObjetivo.setError(null);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            View.OnClickListener inicioClick = v -> mostrarDatePicker(fechaInicioSeleccionada[0], LocalDate.now(), fecha -> {
                fechaInicioSeleccionada[0] = fecha;
                actualizarTextoFecha(etFechaInicio, fecha);
                tilFechaInicio.setError(null);
                if (fechaFinSeleccionada[0] != null && fecha.isAfter(fechaFinSeleccionada[0])) {
                    fechaFinSeleccionada[0] = fecha;
                    actualizarTextoFecha(etFechaFin, fecha);
                    tilFechaFin.setError(null);
                }
            });
            View.OnClickListener finClick = v -> mostrarDatePicker(
                    fechaFinSeleccionada[0] != null ? fechaFinSeleccionada[0] : fechaInicioSeleccionada[0],
                    fechaInicioSeleccionada[0] != null ? fechaInicioSeleccionada[0] : LocalDate.now(),
                    fecha -> {
                fechaFinSeleccionada[0] = fecha;
                actualizarTextoFecha(etFechaFin, fecha);
                tilFechaFin.setError(null);
            });
            etFechaInicio.setOnClickListener(inicioClick);
            tilFechaInicio.setEndIconOnClickListener(inicioClick);
            etFechaFin.setOnClickListener(finClick);
            tilFechaFin.setEndIconOnClickListener(finClick);
            tilFechaInicio.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
            tilFechaInicio.setEndIconDrawable(R.drawable.ic_calendar_artistlan);
            tilFechaFin.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
            tilFechaFin.setEndIconDrawable(R.drawable.ic_calendar_artistlan);

            btnCancelarDialog.setOnClickListener(v -> dialog.dismiss());
            btnGuardarDialog.setOnClickListener(v -> {
                limpiarErroresMetaDialog(tilObjetivo, tilFechaInicio, tilFechaFin);
                BigDecimal objetivo = parseObjetivo(etObjetivo);
                int tipoIndex = spinnerTipo.getSelectedItemPosition();
                boolean valido = validarFormularioMeta(
                        tipoIndex,
                        objetivo,
                        fechaInicioSeleccionada[0],
                        fechaFinSeleccionada[0],
                        tilObjetivo,
                        tilFechaInicio,
                        tilFechaFin
                );
                if (!valido) {
                    return;
                }

                String tipoMeta = obtenerTipoMetaKey(tipoIndex);
                if (editando) {
                    MetaPersonalUpdateDTO request = new MetaPersonalUpdateDTO();
                    request.setTipoMeta(tipoMeta);
                    request.setObjetivo(objetivo);
                    request.setFechaInicio(fechaInicioSeleccionada[0].toString());
                    request.setFechaFin(fechaFinSeleccionada[0].toString());
                    ejecutarGuardadoMeta(
                            metaEditando.getIdMeta(),
                            request,
                            dialog,
                            btnGuardarDialog,
                            btnCancelarDialog,
                            progressDialog,
                            etObjetivo,
                            tvAyuda,
                            true
                    );
                } else {
                    MetaPersonalRequestDTO request = new MetaPersonalRequestDTO();
                    request.setTipoMeta(tipoMeta);
                    request.setObjetivo(objetivo);
                    request.setFechaInicio(fechaInicioSeleccionada[0].toString());
                    request.setFechaFin(fechaFinSeleccionada[0].toString());
                    ejecutarGuardadoMeta(
                            null,
                            request,
                            dialog,
                            btnGuardarDialog,
                            btnCancelarDialog,
                            progressDialog,
                            etObjetivo,
                            tvAyuda,
                            false
                    );
                }
            });
        });
        dialog.setOnDismissListener(d -> {
            if (dialogMetaActual == dialog) {
                dialogMetaActual = null;
            }
        });
        dialog.show();
    }

    private void abrirDialogoCancelar(@NonNull MetaPersonalDTO meta) {
        if (!isAdded() || meta.getIdMeta() == null) {
            return;
        }
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_meta_personal_cancel, null, false);
        View dialogRoot = dialogView.findViewById(R.id.layoutMetaCancelDialogRoot);
        TextView tvBadge = dialogView.findViewById(R.id.tvMetaCancelDialogBadge);
        TextView tvTituloDialog = dialogView.findViewById(R.id.tvMetaCancelDialogTitulo);
        TextView tvSubtituloDialog = dialogView.findViewById(R.id.tvMetaCancelDialogSubtitulo);
        TextView tvContexto = dialogView.findViewById(R.id.tvMetaCancelDialogContexto);
        View containerContexto = dialogView.findViewById(R.id.containerMetaCancelDialogContexto);
        TextInputLayout tilMotivo = dialogView.findViewById(R.id.tilMetaCancelDialogMotivo);
        TextInputEditText etMotivo = dialogView.findViewById(R.id.etMetaCancelDialogMotivo);
        ProgressBar progressDialog = dialogView.findViewById(R.id.progressMetaCancelDialog);
        Button btnVolver = dialogView.findViewById(R.id.btnMetaCancelDialogVolver);
        Button btnConfirmar = dialogView.findViewById(R.id.btnMetaCancelDialogConfirmar);

        tvContexto.setText(construirResumenMeta(meta));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialog.setOnShowListener(d -> {
            dialogCancelacionActual = dialog;
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            DialogThemeHelper.applyFieldDialogWindowSize(dialog, requireContext());
            dialogRoot.setBackground(DialogThemeHelper.createFieldDialogBackground(requireContext()));
            CardThemeHelper.applySecondaryBubbleSurface(tvBadge, tvBadge, themeManager);
            CardThemeHelper.applyGlassChipSection(containerContexto, themeManager, 16);
            ThemeApplier.applyTextPrimary(tvTituloDialog, themeManager);
            ThemeApplier.applyTextSecondary(tvSubtituloDialog, themeManager);
            ThemeApplier.applyTextSecondary(tvContexto, themeManager);
            DialogThemeHelper.applyLightGlassTextInputLayoutStyle(tilMotivo, requireContext());
            DialogThemeHelper.applyLightGlassTextInputEditTextStyle(etMotivo, requireContext());
            CardThemeHelper.applySecondaryBubbleButton(btnVolver, themeManager);
            CardThemeHelper.applyPrimaryBubbleButton(btnConfirmar, themeManager);
            CardThemeHelper.tintProgress(progressDialog, themeManager);

            btnVolver.setOnClickListener(v -> dialog.dismiss());
            btnConfirmar.setOnClickListener(v -> ejecutarCancelacionMeta(
                    meta,
                    etMotivo.getText() != null ? etMotivo.getText().toString().trim() : "",
                    dialog,
                    btnConfirmar,
                    btnVolver,
                    progressDialog,
                    etMotivo
            ));
        });
        dialog.setOnDismissListener(d -> {
            if (dialogCancelacionActual == dialog) {
                dialogCancelacionActual = null;
            }
        });
        dialog.show();
    }

    private void ejecutarGuardadoMeta(@Nullable Integer idMeta,
                                      @NonNull Object request,
                                      @NonNull AlertDialog dialog,
                                      @NonNull Button btnGuardar,
                                      @NonNull Button btnCancelarDialog,
                                      @NonNull ProgressBar progressDialog,
                                      @NonNull TextInputEditText etObjetivo,
                                      @NonNull TextView tvAyuda,
                                      boolean esEdicion) {
        if (metaMutationCall != null) {
            metaMutationCall.cancel();
        }
        actualizarEstadoDialogoMeta(btnGuardar, btnCancelarDialog, progressDialog, etObjetivo, false);
        String ayudaOriginal = esEdicion
                ? "Actualiza el objetivo o el período de tu meta y vuelve a guardar."
                : "Completa los campos y guarda tu nueva meta personal.";
        tvAyuda.setText(ayudaOriginal);
        ThemeApplier.applyTextSecondary(tvAyuda, themeManager);

        metaMutationCall = esEdicion
                ? metaPersonalApi.actualizarMeta(idMeta, (MetaPersonalUpdateDTO) request)
                : metaPersonalApi.crearMeta((MetaPersonalRequestDTO) request);
        final Call<MetaPersonalDTO> callRef = metaMutationCall;
        callRef.enqueue(new Callback<MetaPersonalDTO>() {
            @Override
            public void onResponse(@NonNull Call<MetaPersonalDTO> call, @NonNull Response<MetaPersonalDTO> response) {
                if (metaMutationCall == callRef) {
                    metaMutationCall = null;
                }
                if (!isAdded() || call.isCanceled()) {
                    return;
                }
                actualizarEstadoDialogoMeta(btnGuardar, btnCancelarDialog, progressDialog, etObjetivo, true);
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    mostrarDialogoResultado(
                            esEdicion ? "Meta actualizada" : "Meta creada",
                            esEdicion
                                    ? "Los cambios de tu meta se guardaron correctamente."
                                    : "Tu nueva meta ya forma parte de tu seguimiento personal.",
                            DialogConfig.Type.SUCCESS
                    );
                    cargarDatos();
                    refrescarBadgeNotificacionesSiEsPosible();
                    return;
                }

                String mensaje = construirMensajeErrorMeta(response, !esEdicion);
                if (response.code() == 409) {
                    tvAyuda.setText(mensaje);
                    tvAyuda.setTextColor(themeManager.color(ThemeKeys.ACCENT_PRIMARY));
                    cargarDatos();
                } else {
                    mostrarDialogoResultado("No se pudo guardar", mensaje, DialogConfig.Type.ERROR);
                    if (esEdicion) {
                        cargarDatos();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MetaPersonalDTO> call, @NonNull Throwable t) {
                if (metaMutationCall == callRef) {
                    metaMutationCall = null;
                }
                if (!isAdded() || call.isCanceled()) {
                    return;
                }
                actualizarEstadoDialogoMeta(btnGuardar, btnCancelarDialog, progressDialog, etObjetivo, true);
                mostrarDialogoResultado(
                        "Error de conexión",
                        "No pudimos guardar tu meta en este momento.",
                        DialogConfig.Type.ERROR
                );
            }
        });
    }

    private void ejecutarCancelacionMeta(@NonNull MetaPersonalDTO meta,
                                         @NonNull String motivo,
                                         @NonNull AlertDialog dialog,
                                         @NonNull Button btnConfirmar,
                                         @NonNull Button btnVolver,
                                         @NonNull ProgressBar progressDialog,
                                         @NonNull TextInputEditText etMotivo) {
        if (meta.getIdMeta() == null) {
            return;
        }
        if (metaMutationCall != null) {
            metaMutationCall.cancel();
        }
        MetaPersonalCancelRequestDTO request = new MetaPersonalCancelRequestDTO();
        request.setMotivoCancelacion(motivo.isEmpty() ? null : motivo);
        actualizarEstadoDialogoCancelacion(btnConfirmar, btnVolver, progressDialog, etMotivo, false);
        metaMutationCall = metaPersonalApi.cancelarMeta(meta.getIdMeta(), request);
        final Call<MetaPersonalDTO> callRef = metaMutationCall;
        callRef.enqueue(new Callback<MetaPersonalDTO>() {
            @Override
            public void onResponse(@NonNull Call<MetaPersonalDTO> call, @NonNull Response<MetaPersonalDTO> response) {
                if (metaMutationCall == callRef) {
                    metaMutationCall = null;
                }
                if (!isAdded() || call.isCanceled()) {
                    return;
                }
                actualizarEstadoDialogoCancelacion(btnConfirmar, btnVolver, progressDialog, etMotivo, true);
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    mostrarDialogoResultado(
                            "Meta cancelada",
                            "La meta se canceló correctamente y el resumen ya fue actualizado.",
                            DialogConfig.Type.SUCCESS
                    );
                    cargarDatos();
                    return;
                }
                mostrarDialogoResultado(
                        "No se pudo cancelar",
                        construirMensajeErrorMeta(response, false),
                        DialogConfig.Type.ERROR
                );
                cargarDatos();
            }

            @Override
            public void onFailure(@NonNull Call<MetaPersonalDTO> call, @NonNull Throwable t) {
                if (metaMutationCall == callRef) {
                    metaMutationCall = null;
                }
                if (!isAdded() || call.isCanceled()) {
                    return;
                }
                actualizarEstadoDialogoCancelacion(btnConfirmar, btnVolver, progressDialog, etMotivo, true);
                mostrarDialogoResultado(
                        "Error de conexión",
                        "No pudimos cancelar la meta en este momento.",
                        DialogConfig.Type.ERROR
                );
            }
        });
    }

    private void refrescarBadgeNotificacionesSiEsPosible() {
        if (!isAdded()) {
            return;
        }
        try {
            if (getActivity() instanceof ActFragmentoPrincipal) {
                ((ActFragmentoPrincipal) getActivity()).refrescarBadgeMensajesInmediato();
            }
        } catch (Exception ignored) {
            // El flujo de metas no debe depender del refresh del badge.
        }
    }

    private void actualizarEstadoDialogoMeta(@NonNull Button btnGuardar,
                                             @NonNull Button btnCancelarDialog,
                                             @NonNull ProgressBar progressDialog,
                                             @NonNull TextInputEditText etObjetivo,
                                             boolean habilitado) {
        btnGuardar.setEnabled(habilitado);
        btnCancelarDialog.setEnabled(habilitado);
        etObjetivo.setEnabled(habilitado);
        progressDialog.setVisibility(habilitado ? View.GONE : View.VISIBLE);
    }

    private void actualizarEstadoDialogoCancelacion(@NonNull Button btnConfirmar,
                                                    @NonNull Button btnVolver,
                                                    @NonNull ProgressBar progressDialog,
                                                    @NonNull TextInputEditText etMotivo,
                                                    boolean habilitado) {
        btnConfirmar.setEnabled(habilitado);
        btnVolver.setEnabled(habilitado);
        etMotivo.setEnabled(habilitado);
        progressDialog.setVisibility(habilitado ? View.GONE : View.VISIBLE);
    }

    private void mostrarLoading(boolean mostrar) {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
        if (scrollMisMetas != null) {
            scrollMisMetas.setVisibility(mostrar ? View.INVISIBLE : View.VISIBLE);
        }
        if (mostrar) {
            metasCargadas = false;
        }
    }

    private void mostrarError(@NonNull String mensaje) {
        metasCargadas = true;
        metas.clear();
        resumen = construirResumenFallback();
        containerMetas.removeAllViews();
        tvSeccionTitulo.setText("No pudimos cargar tus metas");
        tvSeccionSubtitulo.setText("Puedes intentarlo nuevamente sin salir del modulo.");
        mostrarEstado("Error al cargar", mensaje);
        renderResumen();
    }

    private void mostrarEstado(@NonNull String titulo, @NonNull String mensaje) {
        mostrarEstado(titulo, mensaje, true);
    }

    private void mostrarEstado(@NonNull String titulo, @NonNull String mensaje, boolean mostrarReintento) {
        if (layoutEstado != null) {
            layoutEstado.setVisibility(View.VISIBLE);
        }
        tvEstadoTitulo.setText(titulo);
        tvEstadoMensaje.setText(mensaje);
        btnReintentar.setVisibility(mostrarReintento ? View.VISIBLE : View.GONE);
    }

    private void ocultarEstado() {
        if (layoutEstado != null) {
            layoutEstado.setVisibility(View.GONE);
        }
    }

    @NonNull
    private List<MetaPersonalDTO> filtrarMetas(int tab) {
        List<MetaPersonalDTO> resultado = new ArrayList<>();
        for (MetaPersonalDTO meta : metas) {
            String estado = valorNoVacio(meta.getEstado(), "");
            boolean esActiva = esMetaActiva(estado);
            boolean esHistorica = esMetaHistorica(estado);
            if ((tab == TAB_ACTIVAS && esActiva) || (tab == TAB_HISTORIAL && esHistorica)) {
                resultado.add(meta);
            }
        }
        return resultado;
    }

    @NonNull
    private List<MetaPersonalDTO> aplicarFiltroHistorial(@NonNull List<MetaPersonalDTO> historialBase) {
        if (FILTRO_TODOS.equalsIgnoreCase(filtroTipoHistorial)) {
            return historialBase;
        }
        List<MetaPersonalDTO> resultado = new ArrayList<>();
        for (MetaPersonalDTO meta : historialBase) {
            if (filtroTipoHistorial.equalsIgnoreCase(valorNoVacio(meta.getTipoMeta(), ""))) {
                resultado.add(meta);
            }
        }
        return resultado;
    }

    private MetaPersonalResumenDTO construirResumenFallback() {
        MetaPersonalResumenDTO fallback = new MetaPersonalResumenDTO();
        int activas = 0;
        int completadas = 0;
        int expiradas = 0;
        int canceladas = 0;
        for (MetaPersonalDTO meta : metas) {
            String estado = valorNoVacio(meta.getEstado(), "");
            if ("POR_COMENZAR".equalsIgnoreCase(estado) || "EN_PROCESO".equalsIgnoreCase(estado)) {
                activas++;
            } else if ("COMPLETADA".equalsIgnoreCase(estado)) {
                completadas++;
            } else if ("EXPIRADA".equalsIgnoreCase(estado)) {
                expiradas++;
            } else if ("CANCELADA".equalsIgnoreCase(estado)) {
                canceladas++;
            }
        }
        fallback.setActivas(activas);
        fallback.setCompletadas(completadas);
        fallback.setExpiradas(expiradas);
        fallback.setCanceladas(canceladas);
        fallback.setTotal(metas.size());
        fallback.setPorComenzar(activas);
        fallback.setEnProceso(0);
        return fallback;
    }

    private String construirMensajeError(@NonNull Response<?> response) {
        String backend = ApiErrorParser.extractMessage(response);
        if (backend != null && !backend.trim().isEmpty()) {
            return backend;
        }
        if (response.code() == 403) {
            return "No tienes permisos para consultar tus metas personales.";
        }
        if (response.code() == 401) {
            return "Tu sesión ya no es válida. Inicia sesión nuevamente.";
        }
        return "No pudimos obtener tus metas en este momento.";
    }

    @Nullable
    private LocalDate parseLocalDate(@Nullable String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        String limpia = valor.length() >= 10 ? valor.substring(0, 10) : valor;
        try {
            return LocalDate.parse(limpia, DATE_SOURCE);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private void actualizarTextoFecha(@Nullable TextInputEditText editText, @Nullable LocalDate fecha) {
        if (editText == null) {
            return;
        }
        editText.setText(fecha != null ? DATE_DIALOG_OUTPUT.format(fecha) : "");
    }

    private void mostrarDatePicker(@Nullable LocalDate fechaBase, @NonNull FechaSeleccionListener listener) {
        mostrarDatePicker(fechaBase, LocalDate.now(), listener);
    }

    private void mostrarDatePicker(@Nullable LocalDate fechaBase,
                                   @NonNull LocalDate fechaMinima,
                                   @NonNull FechaSeleccionListener listener) {
        LocalDate base = fechaBase != null && !fechaBase.isBefore(fechaMinima) ? fechaBase : fechaMinima;
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> listener.onFechaSeleccionada(LocalDate.of(year, month + 1, dayOfMonth)),
                base.getYear(),
                base.getMonthValue() - 1,
                base.getDayOfMonth()
        );
        if (dialog.getDatePicker() != null) {
            dialog.getDatePicker().setMinDate(
                    fechaMinima.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            );
        }
        dialog.show();
    }

    private void actualizarAyudaObjetivo(@NonNull TextInputLayout tilObjetivo, int tipoIndex) {
        String tipo = obtenerTipoMetaKey(tipoIndex);
        if ("INGRESOS".equalsIgnoreCase(tipo)) {
            tilObjetivo.setHint("Ej. 5000");
            return;
        }
        if ("PUBLICACIONES".equalsIgnoreCase(tipo)) {
            tilObjetivo.setHint("Ej. 15 publicaciones");
            return;
        }
        if ("FAVORITOS".equalsIgnoreCase(tipo)) {
            tilObjetivo.setHint("Ej. 20 favoritos");
            return;
        }
        tilObjetivo.setHint("Ej. 10 ventas");
    }

    private void limpiarErroresMetaDialog(TextInputLayout... fields) {
        for (TextInputLayout field : fields) {
            if (field == null) {
                continue;
            }
            field.setError(null);
            field.setErrorEnabled(false);
        }
    }

    @Nullable
    private BigDecimal parseObjetivo(@Nullable TextInputEditText etObjetivo) {
        if (etObjetivo == null || etObjetivo.getText() == null) {
            return null;
        }
        String valor = etObjetivo.getText().toString().trim().replace(",", "");
        if (valor.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean validarFormularioMeta(int tipoIndex,
                                          @Nullable BigDecimal objetivo,
                                          @Nullable LocalDate fechaInicio,
                                          @Nullable LocalDate fechaFin,
                                          @NonNull TextInputLayout tilObjetivo,
                                          @NonNull TextInputLayout tilFechaInicio,
                                          @NonNull TextInputLayout tilFechaFin) {
        boolean valido = true;
        LocalDate hoy = LocalDate.now();
        if (tipoIndex < 0 || tipoIndex >= TIPO_META_KEYS.length) {
            mostrarDialogoResultado("Tipo requerido", "Selecciona un tipo de meta válido.", DialogConfig.Type.WARNING);
            valido = false;
        }
        if (objetivo == null) {
            tilObjetivo.setError("Ingresa un objetivo válido.");
            valido = false;
        } else if (objetivo.compareTo(BigDecimal.ZERO) <= 0) {
            tilObjetivo.setError("El objetivo debe ser mayor a 0.");
            valido = false;
        }
        if (fechaInicio == null) {
            tilFechaInicio.setError("Selecciona la fecha de inicio.");
            valido = false;
        } else if (fechaInicio.isBefore(hoy)) {
            tilFechaInicio.setError("La fecha de inicio no puede ser anterior a hoy.");
            valido = false;
        }
        if (fechaFin == null) {
            tilFechaFin.setError("Selecciona la fecha de fin.");
            valido = false;
        }
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            tilFechaFin.setError("La fecha fin no puede ser menor a la fecha inicio.");
            valido = false;
        }
        return valido;
    }

    private String construirMensajeErrorMeta(@NonNull Response<?> response, boolean esCreacion) {
        if (response.code() == 409) {
            return "Ya tienes una meta activa de este tipo.";
        }
        String backend = ApiErrorParser.extractMessage(response);
        if (backend != null && !backend.trim().isEmpty()) {
            return backend;
        }
        if (response.code() == 403) {
            return "No tienes permisos para realizar esta acción.";
        }
        if (response.code() == 401) {
            return "Tu sesión ya no es válida. Inicia sesión nuevamente.";
        }
        if (esCreacion) {
            return "No pudimos crear la meta en este momento.";
        }
        return "No pudimos actualizar la meta en este momento.";
    }

    private void mostrarDialogoResultado(@NonNull String titulo, @NonNull String mensaje, @NonNull DialogConfig.Type tipo) {
        ArtistlanDialogFactory.show(this, DialogConfig.builder()
                .setType(tipo)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveText("Aceptar")
                .build());
    }

    private String construirResumenMeta(@NonNull MetaPersonalDTO meta) {
        return tipoMetaLabelCorto(meta.getTipoMeta()) + "\n"
                + "Objetivo: " + formatearObjetivo(meta) + "\n"
                + "Período: " + formatearPeriodo(meta.getFechaInicio(), meta.getFechaFin());
    }

    private int obtenerIndiceTipoMeta(@Nullable String tipoMeta) {
        if (tipoMeta == null) {
            return 0;
        }
        for (int i = 0; i < TIPO_META_KEYS.length; i++) {
            if (TIPO_META_KEYS[i].equalsIgnoreCase(tipoMeta)) {
                return i;
            }
        }
        return 0;
    }

    @NonNull
    private String obtenerTipoMetaKey(int index) {
        if (index < 0 || index >= TIPO_META_KEYS.length) {
            return TIPO_META_KEYS[0];
        }
        return TIPO_META_KEYS[index];
    }

    private void actualizarVisibilidadFiltroHistorial() {
        if (cardFiltroHistorial == null) {
            return;
        }
        cardFiltroHistorial.setVisibility(tabSeleccionada == TAB_HISTORIAL ? View.VISIBLE : View.GONE);
    }

    private int obtenerIndiceFiltroHistorial() {
        for (int i = 0; i < FILTRO_HISTORIAL_KEYS.length; i++) {
            if (FILTRO_HISTORIAL_KEYS[i].equalsIgnoreCase(filtroTipoHistorial)) {
                return i;
            }
        }
        return 0;
    }

    @NonNull
    private String obtenerFiltroHistorialKey(int position) {
        if (position < 0 || position >= FILTRO_HISTORIAL_KEYS.length) {
            return FILTRO_TODOS;
        }
        return FILTRO_HISTORIAL_KEYS[position];
    }

    private boolean esMetaActiva(@Nullable String estado) {
        return "POR_COMENZAR".equalsIgnoreCase(estado) || "EN_PROCESO".equalsIgnoreCase(estado);
    }

    private boolean esMetaHistorica(@Nullable String estado) {
        return "COMPLETADA".equalsIgnoreCase(estado)
                || "EXPIRADA".equalsIgnoreCase(estado)
                || "CANCELADA".equalsIgnoreCase(estado);
    }

    private String formatearObjetivo(@NonNull MetaPersonalDTO meta) {
        String tipo = valorNoVacio(meta.getTipoMeta(), "");
        if ("INGRESOS".equalsIgnoreCase(tipo)) {
            return formatearMoneda(meta.getObjetivo());
        }
        return formatearNumero(meta.getObjetivo()) + " " + tipoMetaLabelCorto(tipo).toLowerCase(Locale.ROOT);
    }

    private String formatearPeriodo(@Nullable String fechaInicio, @Nullable String fechaFin) {
        return formatearFecha(fechaInicio) + " - " + formatearFecha(fechaFin);
    }

    private String formatearFecha(@Nullable String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "--";
        }
        String limpia = valor.length() >= 10 ? valor.substring(0, 10) : valor;
        try {
            LocalDate date = LocalDate.parse(limpia, DATE_SOURCE);
            return DATE_OUTPUT.format(date);
        } catch (DateTimeParseException ignored) {
            return limpia;
        }
    }

    private int calcularEnteroProgreso(@NonNull MetaPersonalDTO meta) {
        BigDecimal porcentaje = meta.getPorcentajeVisual() != null ? meta.getPorcentajeVisual() : meta.getPorcentaje();
        if (porcentaje == null) {
            return 0;
        }
        int valor = porcentaje.setScale(0, RoundingMode.HALF_UP).intValue();
        return Math.max(0, Math.min(100, valor));
    }

    private String formatearPorcentajeVisual(@NonNull MetaPersonalDTO meta) {
        BigDecimal porcentaje = meta.getPorcentajeVisual() != null ? meta.getPorcentajeVisual() : meta.getPorcentaje();
        if (porcentaje == null) {
            return "0";
        }
        BigDecimal limpio = porcentaje.setScale(0, RoundingMode.HALF_UP);
        return limpio.toPlainString();
    }

    private String formatearNumero(@Nullable BigDecimal valor) {
        if (valor == null) {
            return "0";
        }
        BigDecimal normalizado = valor.stripTrailingZeros();
        return normalizado.scale() <= 0
                ? normalizado.toPlainString()
                : normalizado.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private String formatearMoneda(@Nullable BigDecimal valor) {
        return "$" + formatearNumero(valor);
    }

    private int valorSeguro(@Nullable Integer valor) {
        return valor != null ? valor : 0;
    }

    private String valorNoVacio(@Nullable String valor, @NonNull String fallback) {
        return valor != null && !valor.trim().isEmpty() ? valor : fallback;
    }

    private String mensajeEstadoFallback(@Nullable String estado) {
        if (estado == null) {
            return "Sigue de cerca esta meta desde Artistlan.";
        }
        switch (estado.toUpperCase(Locale.ROOT)) {
            case "POR_COMENZAR":
                return "Tu meta aún no inicia.";
            case "EN_PROCESO":
                return "Tu meta sigue avanzando.";
            case "COMPLETADA":
                return "Objetivo alcanzado correctamente.";
            case "EXPIRADA":
                return "El período finalizó sin llegar al objetivo.";
            case "CANCELADA":
                return "La meta fue cancelada.";
            default:
                return "Sigue de cerca esta meta desde Artistlan.";
        }
    }

    private String tipoMetaLabelCorto(@Nullable String tipoMeta) {
        if (tipoMeta == null) {
            return "Meta";
        }
        switch (tipoMeta.toUpperCase(Locale.ROOT)) {
            case "VENTAS":
                return "Ventas";
            case "INGRESOS":
                return "Ingresos";
            case "PUBLICACIONES":
                return "Publicaciones";
            case "FAVORITOS":
                return "Favoritos";
            default:
                return "Meta";
        }
    }

    private MetaTipoStyle obtenerEstiloTipo(@Nullable String tipoMeta) {
        if (tipoMeta == null) {
            return new MetaTipoStyle("Meta", R.drawable.ic_nav_metas_artistlan,
                    Color.parseColor("#7D8B9B"), Color.parseColor("#2B3642"));
        }
        switch (tipoMeta.toUpperCase(Locale.ROOT)) {
            case "VENTAS":
                return new MetaTipoStyle("Meta de ventas", R.drawable.ic_carrito_artistlan,
                        Color.parseColor("#4CAF7D"), Color.parseColor("#20392E"));
            case "INGRESOS":
                return new MetaTipoStyle("Meta de ingresos", R.drawable.ic_meta_ingresos_artistlan,
                        Color.parseColor("#D4A85A"), Color.parseColor("#3B311F"));
            case "PUBLICACIONES":
                return new MetaTipoStyle("Meta de publicaciones", R.drawable.ic_nav_subir_artistlan,
                        Color.parseColor("#AAB4C0"), Color.parseColor("#2E3540"));
            case "FAVORITOS":
                return new MetaTipoStyle("Meta de favoritos", R.drawable.ic_like_filled,
                        Color.parseColor("#D56C87"), Color.parseColor("#3A2430"));
            default:
                return new MetaTipoStyle("Meta personal", R.drawable.ic_nav_metas_artistlan,
                        Color.parseColor("#7D8B9B"), Color.parseColor("#2B3642"));
        }
    }

    private MetaEstadoStyle obtenerEstiloEstado(@Nullable String estado) {
        if (estado == null) {
            return new MetaEstadoStyle("Pendiente", Color.parseColor("#6B7280"), Color.parseColor("#303847"), Color.WHITE);
        }
        switch (estado.toUpperCase(Locale.ROOT)) {
            case "POR_COMENZAR":
                return new MetaEstadoStyle("Por comenzar", Color.parseColor("#8B6FC9"), Color.parseColor("#302645"), Color.WHITE);
            case "EN_PROCESO":
                return new MetaEstadoStyle("En proceso", Color.parseColor("#5E90D6"), Color.parseColor("#24324B"), Color.WHITE);
            case "COMPLETADA":
                return new MetaEstadoStyle("Completada", Color.parseColor("#4FA96D"), Color.parseColor("#21382A"), Color.WHITE);
            case "EXPIRADA":
                return new MetaEstadoStyle("Expirada", Color.parseColor("#D59C5A"), Color.parseColor("#3C2E1E"), Color.WHITE);
            case "CANCELADA":
                return new MetaEstadoStyle("Cancelada", Color.parseColor("#8C96A4"), Color.parseColor("#303843"), Color.WHITE);
            default:
                return new MetaEstadoStyle(estado, Color.parseColor("#6B7280"), Color.parseColor("#303847"), Color.WHITE);
        }
    }

    private GradientDrawable crearBubble(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpToPx(radiusDp));
        drawable.setColor(ColorUtils.setAlphaComponent(color, 220));
        drawable.setStroke(dpToPx(1), ColorUtils.setAlphaComponent(color, 245));
        return drawable;
    }

    private GradientDrawable crearChip(int fillColor, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpToPx(14));
        drawable.setColor(fillColor);
        drawable.setStroke(dpToPx(1), strokeColor);
        return drawable;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }

    private static final class MetaTipoStyle {
        final String label;
        @DrawableRes final int iconRes;
        final int accentColor;
        final int softColor;

        MetaTipoStyle(String label, int iconRes, int accentColor, int softColor) {
            this.label = label;
            this.iconRes = iconRes;
            this.accentColor = accentColor;
            this.softColor = softColor;
        }
    }

    private static final class MetaEstadoStyle {
        final String label;
        final int strokeColor;
        final int fillColor;
        final int textColor;

        MetaEstadoStyle(String label, int strokeColor, int fillColor, int textColor) {
            this.label = label;
            this.strokeColor = strokeColor;
            this.fillColor = fillColor;
            this.textColor = textColor;
        }
    }

    private interface FechaSeleccionListener {
        void onFechaSeleccionada(@NonNull LocalDate fecha);
    }
}

package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.Admin.adapter.AdminRankingAdapter;
import com.example.artistlan.Admin.adapter.AdminCategoriaStatsAdapter;
import com.example.artistlan.Admin.view.AdminChartThemeHelper;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.SessionManager;
import com.example.artistlan.Conector.api.AdminEstadisticasApi;
import com.example.artistlan.Conector.model.AdminCategoriaStatsDTO;
import com.example.artistlan.Conector.model.AdminCrecimientoDTO;
import com.example.artistlan.Conector.model.AdminObservacionDTO;
import com.example.artistlan.Conector.model.AdminObservacionRequestDTO;
import com.example.artistlan.Conector.model.AdminPuntoSerieDTO;
import com.example.artistlan.Conector.model.AdminRankingItemDTO;
import com.example.artistlan.Conector.model.AdminRankingResponseDTO;
import com.example.artistlan.Conector.model.AdminSerieTemporalDTO;
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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;

import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragEstadisticasPlataforma extends Fragment {

    private enum CategoriaTipo {
        OBRAS("OBRAS"),
        SERVICIOS("SERVICIOS"),
        ARTISTAS("ARTISTAS");

        private final String backendValue;

        CategoriaTipo(String backendValue) {
            this.backendValue = backendValue;
        }
    }

    private enum TabActiva {
        CATEGORIAS,
        VENTAS,
        POPULARIDAD,
        CRECIMIENTO
    }

    private static final Locale LOCALE_ES_MX = new Locale("es", "MX");
    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter API_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter VIEW_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_ES_MX);
    private static final DateTimeFormatter VIEW_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE_ES_MX);
    private static final int RANKING_LIMIT = 5;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(LOCALE_ES_MX);
    private final List<AdminCategoriaStatsDTO> categoriasActuales = new ArrayList<>();
    private final List<AdminPuntoSerieDTO> ventasActuales = new ArrayList<>();
    private final List<AdminRankingItemDTO> rankingActual = new ArrayList<>();
    private final List<AdminPuntoSerieDTO> crecimientoActual = new ArrayList<>();
    private final List<AdminPuntoSerieDTO> crecimientoAnterior = new ArrayList<>();

    private ThemeManager themeManager;
    private AdminEstadisticasApi adminEstadisticasApi;
    private AdminCategoriaStatsAdapter categoriaAdapter;
    private AdminRankingAdapter rankingAdapter;
    @Nullable
    private Call<List<AdminCategoriaStatsDTO>> categoriasCall;
    @Nullable
    private Call<AdminSerieTemporalDTO> ventasCall;
    @Nullable
    private Call<AdminRankingResponseDTO> rankingCall;
    @Nullable
    private Call<AdminCrecimientoDTO> crecimientoCall;
    @Nullable
    private Call<List<AdminObservacionDTO>> observacionesCall;
    @Nullable
    private Call<AdminObservacionDTO> observacionMutationCall;
    @Nullable
    private Call<Void> observacionDeleteCall;
    @Nullable
    private AdminCrecimientoDTO ultimoCrecimiento;
    private int ultimoIndiceRealCrecimiento = -1;
    @Nullable
    private AdminObservacionDTO observacionActual;
    @Nullable
    private ObservacionContexto observacionContextoActual;
    @Nullable
    private String observacionEstadoMensaje;
    private boolean observacionEstadoEsError = false;
    private boolean observacionLoading = false;
    private boolean observacionMutationLoading = false;
    private long observacionRequestSequence = 0L;
    private long observacionRequestActivo = 0L;
    @Nullable
    private AlertDialog dialogObservacion;

    private View menuInferior;
    private ImageButton btnRegresar;
    private MaterialCardView cardTabs;
    private MaterialCardView cardCategorias;
    private MaterialCardView cardPopularidad;
    private MaterialCardView cardVentas;
    private MaterialCardView cardCrecimiento;
    private MaterialCardView cardTotalVentas;
    private MaterialCardView cardTotalIngresos;
    private MaterialCardView cardSemanaActualCrecimiento;
    private MaterialCardView cardSemanaAnteriorCrecimiento;
    private MaterialCardView cardCambioCrecimiento;
    private MaterialCardView cardResumenTotalCategorias;
    private MaterialCardView cardResumenMayorCategorias;
    private MaterialCardView cardResumenConDatosCategorias;
    private MaterialCardView cardLeyendaCategorias;
    private MaterialCardView cardObservaciones;
    private View layoutTabsPrincipales;
    private TabLayout tabLayoutPrincipal;
    private Button btnTipoObras;
    private Button btnTipoServicios;
    private Button btnTipoArtistas;
    private Button btnTipoObrasPopularidad;
    private Button btnTipoServiciosPopularidad;
    private Button btnTipoArtistasPopularidad;
    private Button btnTipoObrasCrecimiento;
    private Button btnTipoServiciosCrecimiento;
    private Button btnTipoArtistasCrecimiento;
    private Button btnActualizar;
    private ImageButton btnActualizarCategoriasMini;
    private ImageButton btnActualizarPopularidadMini;
    private ImageButton btnActualizarVentasMini;
    private ImageButton btnActualizarCrecimientoMini;
    private Button btnSemanaAnterior;
    private Button btnSemanaSiguiente;
    private Button btnSemanaAnteriorCrecimiento;
    private Button btnSemanaSiguienteCrecimiento;
    private Button btnAgregarObservacion;
    private Button btnEditarObservacion;
    private Button btnEliminarObservacion;
    private ProgressBar progressCategorias;
    private ProgressBar progressPopularidad;
    private ProgressBar progressVentas;
    private ProgressBar progressCrecimiento;
    private ProgressBar progressObservacion;
    private TextView tvTitulo;
    private TextView tvSubtitulo;
    private TextView tvTabsTitulo;
    private TextView tvTabsSubtitulo;
    private TextView tvTabsPendientes;
    private TextView tvCategoriasTitulo;
    private TextView tvCategoriasSubtitulo;
    private TextView tvEstadoCategorias;
    private TextView tvZeroCategorias;
    private TextView tvResumenTitulo;
    private TextView tvResumenTotalCategoriasLabel;
    private TextView tvResumenTotalCategoriasValue;
    private TextView tvResumenMayorCategoriasLabel;
    private TextView tvResumenMayorCategoriasValue;
    private TextView tvResumenConDatosCategoriasLabel;
    private TextView tvResumenConDatosCategoriasValue;
    private TextView tvLeyendaCategoriasTitulo;
    private TextView tvPopularidadTitulo;
    private TextView tvPopularidadSubtitulo;
    private TextView tvEstadoPopularidad;
    private TextView tvMensajePopularidad;
    private TextView tvTopRankingPopularidad;
    private TextView tvVentasTitulo;
    private TextView tvVentasSubtitulo;
    private TextView tvRangoSemanaVentas;
    private TextView tvEstadoVentas;
    private TextView tvTotalVentasLabel;
    private TextView tvTotalVentasValue;
    private TextView tvTotalIngresosLabel;
    private TextView tvTotalIngresosValue;
    private TextView tvMensajeVentas;
    private TextView tvCrecimientoTitulo;
    private TextView tvCrecimientoSubtitulo;
    private TextView tvRangoSemanaCrecimiento;
    private TextView tvEstadoCrecimiento;
    private TextView tvSemanaActualCrecimientoLabel;
    private TextView tvSemanaActualCrecimientoValue;
    private TextView tvSemanaAnteriorCrecimientoLabel;
    private TextView tvSemanaAnteriorCrecimientoValue;
    private TextView tvCambioCrecimientoLabel;
    private TextView tvCambioCrecimientoValue;
    private TextView tvMensajeCrecimiento;
    private TextView tvObservacionesTitulo;
    private TextView tvObservacionContexto;
    private TextView tvObservacionEstado;
    private TextView tvObservacionTexto;
    private TextView tvObservacionMeta;
    private RecyclerView recyclerView;
    private RecyclerView recyclerPopularidad;
    private PieChart viewGraficaCategorias;
    private LineChart viewGraficaVentas;
    private LineChart viewGraficaCrecimiento;
    private View containerDistribucionCategorias;
    private LinearLayout containerLeyendaCategorias;
    private View containerResumenVentas;
    private View containerResumenCrecimiento;
    private View containerPodioPopularidad;
    private View rowPodioSecundario;
    private View layoutAccionesObservacion;
    private PodioViews podioPrimero;
    private PodioViews podioSegundo;
    private PodioViews podioTercero;

    private CategoriaTipo tipoCategoriaSeleccionado = CategoriaTipo.OBRAS;
    private CategoriaTipo tipoPopularidadSeleccionado = CategoriaTipo.OBRAS;
    private CategoriaTipo tipoCrecimientoSeleccionado = CategoriaTipo.OBRAS;
    private TabActiva tabActiva = TabActiva.CRECIMIENTO;
    private boolean puedeConsultarEstadisticas = false;
    private LocalDate semanaVentasSeleccionada = obtenerInicioSemanaActual();
    private LocalDate semanaCrecimientoSeleccionada = obtenerInicioSemanaActual();

    public FragEstadisticasPlataforma() {
        super(R.layout.fragment_frag_estadisticas_plataforma);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);
        themeManager = new ThemeManager(requireContext());
        adminEstadisticasApi = RetrofitClient.getClient().create(AdminEstadisticasApi.class);

        bindViews(view);
        configurarRecycler();
        configurarMenuInferior();
        configurarNavegacion();
        configurarTabs();
        configurarSelectorCategorias();
        configurarAccionesVentas();
        configurarAccionesCrecimiento();
        aplicarTemaVisual(view);
        actualizarVisibilidadTab();
        actualizarRangoSemanaVentas(semanaVentasSeleccionada, semanaVentasSeleccionada.plusDays(6));
        actualizarRangoSemanaCrecimiento(semanaCrecimientoSeleccionada, semanaCrecimientoSeleccionada.plusDays(6));
        validarPermisosYCargar();
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) {
            ThemeModuleStyler.styleFragment(this, view);
        }
        themeManager = new ThemeManager(requireContext());
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }
        aplicarTemaVisual(view);
        if (categoriaAdapter != null) {
            categoriaAdapter.notifyDataSetChanged();
        }
        if (rankingAdapter != null) {
            rankingAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        cancelarLlamadasActivas();
        if (dialogObservacion != null) {
            dialogObservacion.dismiss();
            dialogObservacion = null;
        }
        if (menuInferior != null) {
            menuInferior.setVisibility(View.VISIBLE);
        }
        super.onDestroyView();
    }

    private void bindViews(@NonNull View view) {
        btnRegresar = view.findViewById(R.id.btnRegresarEstadisticasPlataforma);
        cardTabs = view.findViewById(R.id.cardTabsEstadisticasPlataforma);
        layoutTabsPrincipales = view.findViewById(R.id.layoutTabsPrincipalesEstadisticas);
        tabLayoutPrincipal = view.findViewById(R.id.tabLayoutPrincipalEstadisticas);
        cardCategorias = view.findViewById(R.id.cardCategoriasEstadisticasPlataforma);
        cardPopularidad = view.findViewById(R.id.cardPopularidadEstadisticasPlataforma);
        cardVentas = view.findViewById(R.id.cardVentasEstadisticasPlataforma);
        cardCrecimiento = view.findViewById(R.id.cardCrecimientoEstadisticasPlataforma);
        cardTotalVentas = view.findViewById(R.id.cardTotalVentasEstadisticas);
        cardTotalIngresos = view.findViewById(R.id.cardTotalIngresosEstadisticas);
        cardSemanaActualCrecimiento = view.findViewById(R.id.cardSemanaActualCrecimientoEstadisticas);
        cardSemanaAnteriorCrecimiento = view.findViewById(R.id.cardSemanaAnteriorCrecimientoEstadisticas);
        cardCambioCrecimiento = view.findViewById(R.id.cardCambioCrecimientoEstadisticas);
        cardResumenTotalCategorias = view.findViewById(R.id.cardResumenTotalCategoriasEstadisticas);
        cardResumenMayorCategorias = view.findViewById(R.id.cardResumenMayorCategoriasEstadisticas);
        cardResumenConDatosCategorias = view.findViewById(R.id.cardResumenConDatosCategoriasEstadisticas);
        cardLeyendaCategorias = view.findViewById(R.id.cardLeyendaCategoriasEstadisticas);
        cardObservaciones = view.findViewById(R.id.cardObservacionesEstadisticasPlataforma);
        btnTipoObras = view.findViewById(R.id.btnTipoObrasEstadisticas);
        btnTipoServicios = view.findViewById(R.id.btnTipoServiciosEstadisticas);
        btnTipoArtistas = view.findViewById(R.id.btnTipoArtistasEstadisticas);
        btnTipoObrasPopularidad = view.findViewById(R.id.btnTipoObrasPopularidadEstadisticas);
        btnTipoServiciosPopularidad = view.findViewById(R.id.btnTipoServiciosPopularidadEstadisticas);
        btnTipoArtistasPopularidad = view.findViewById(R.id.btnTipoArtistasPopularidadEstadisticas);
        btnTipoObrasCrecimiento = view.findViewById(R.id.btnTipoObrasCrecimientoEstadisticas);
        btnTipoServiciosCrecimiento = view.findViewById(R.id.btnTipoServiciosCrecimientoEstadisticas);
        btnTipoArtistasCrecimiento = view.findViewById(R.id.btnTipoArtistasCrecimientoEstadisticas);
        btnActualizar = view.findViewById(R.id.btnActualizarCategoriasEstadisticas);
        btnActualizarCategoriasMini = view.findViewById(R.id.btnActualizarCategoriasMiniEstadisticas);
        btnActualizarPopularidadMini = view.findViewById(R.id.btnActualizarPopularidadMiniEstadisticas);
        btnActualizarVentasMini = view.findViewById(R.id.btnActualizarVentasMiniEstadisticas);
        btnActualizarCrecimientoMini = view.findViewById(R.id.btnActualizarCrecimientoMiniEstadisticas);
        btnSemanaAnterior = view.findViewById(R.id.btnSemanaAnteriorVentasEstadisticas);
        btnSemanaSiguiente = view.findViewById(R.id.btnSemanaSiguienteVentasEstadisticas);
        btnSemanaAnteriorCrecimiento = view.findViewById(R.id.btnSemanaAnteriorCrecimientoEstadisticas);
        btnSemanaSiguienteCrecimiento = view.findViewById(R.id.btnSemanaSiguienteCrecimientoEstadisticas);
        btnAgregarObservacion = view.findViewById(R.id.btnAgregarObservacionEstadisticas);
        btnEditarObservacion = view.findViewById(R.id.btnEditarObservacionEstadisticas);
        btnEliminarObservacion = view.findViewById(R.id.btnEliminarObservacionEstadisticas);
        progressCategorias = view.findViewById(R.id.progressCategoriasEstadisticas);
        progressPopularidad = view.findViewById(R.id.progressPopularidadEstadisticas);
        progressVentas = view.findViewById(R.id.progressVentasEstadisticas);
        progressCrecimiento = view.findViewById(R.id.progressCrecimientoEstadisticas);
        progressObservacion = view.findViewById(R.id.progressObservacionEstadisticas);
        tvTitulo = view.findViewById(R.id.tvTituloEstadisticasPlataforma);
        tvSubtitulo = view.findViewById(R.id.tvSubtituloEstadisticasPlataforma);
        tvTabsTitulo = view.findViewById(R.id.tvTabsTituloEstadisticas);
        tvTabsSubtitulo = view.findViewById(R.id.tvTabsSubtituloEstadisticas);
        tvTabsPendientes = view.findViewById(R.id.tvTabsPendientesEstadisticas);
        tvCategoriasTitulo = view.findViewById(R.id.tvCategoriasTituloEstadisticas);
        tvCategoriasSubtitulo = view.findViewById(R.id.tvCategoriasSubtituloEstadisticas);
        tvEstadoCategorias = view.findViewById(R.id.tvEstadoCategoriasEstadisticas);
        tvZeroCategorias = view.findViewById(R.id.tvZeroCategoriasEstadisticas);
        tvResumenTitulo = view.findViewById(R.id.tvResumenCategoriasEstadisticas);
        tvResumenTotalCategoriasLabel = view.findViewById(R.id.tvResumenTotalCategoriasLabelEstadisticas);
        tvResumenTotalCategoriasValue = view.findViewById(R.id.tvResumenTotalCategoriasValueEstadisticas);
        tvResumenMayorCategoriasLabel = view.findViewById(R.id.tvResumenMayorCategoriasLabelEstadisticas);
        tvResumenMayorCategoriasValue = view.findViewById(R.id.tvResumenMayorCategoriasValueEstadisticas);
        tvResumenConDatosCategoriasLabel = view.findViewById(R.id.tvResumenConDatosCategoriasLabelEstadisticas);
        tvResumenConDatosCategoriasValue = view.findViewById(R.id.tvResumenConDatosCategoriasValueEstadisticas);
        tvLeyendaCategoriasTitulo = view.findViewById(R.id.tvLeyendaCategoriasTituloEstadisticas);
        tvPopularidadTitulo = view.findViewById(R.id.tvPopularidadTituloEstadisticas);
        tvPopularidadSubtitulo = view.findViewById(R.id.tvPopularidadSubtituloEstadisticas);
        tvEstadoPopularidad = view.findViewById(R.id.tvEstadoPopularidadEstadisticas);
        tvMensajePopularidad = view.findViewById(R.id.tvMensajePopularidadEstadisticas);
        tvTopRankingPopularidad = view.findViewById(R.id.tvTopRankingPopularidadEstadisticas);
        tvVentasTitulo = view.findViewById(R.id.tvVentasTituloEstadisticas);
        tvVentasSubtitulo = view.findViewById(R.id.tvVentasSubtituloEstadisticas);
        tvRangoSemanaVentas = view.findViewById(R.id.tvRangoSemanaVentasEstadisticas);
        tvEstadoVentas = view.findViewById(R.id.tvEstadoVentasEstadisticas);
        tvTotalVentasLabel = view.findViewById(R.id.tvTotalVentasLabelEstadisticas);
        tvTotalVentasValue = view.findViewById(R.id.tvTotalVentasValueEstadisticas);
        tvTotalIngresosLabel = view.findViewById(R.id.tvTotalIngresosLabelEstadisticas);
        tvTotalIngresosValue = view.findViewById(R.id.tvTotalIngresosValueEstadisticas);
        tvMensajeVentas = view.findViewById(R.id.tvMensajeVentasEstadisticas);
        tvCrecimientoTitulo = view.findViewById(R.id.tvCrecimientoTituloEstadisticas);
        tvCrecimientoSubtitulo = view.findViewById(R.id.tvCrecimientoSubtituloEstadisticas);
        tvRangoSemanaCrecimiento = view.findViewById(R.id.tvRangoSemanaCrecimientoEstadisticas);
        tvEstadoCrecimiento = view.findViewById(R.id.tvEstadoCrecimientoEstadisticas);
        tvSemanaActualCrecimientoLabel = view.findViewById(R.id.tvSemanaActualCrecimientoLabelEstadisticas);
        tvSemanaActualCrecimientoValue = view.findViewById(R.id.tvSemanaActualCrecimientoValueEstadisticas);
        tvSemanaAnteriorCrecimientoLabel = view.findViewById(R.id.tvSemanaAnteriorCrecimientoLabelEstadisticas);
        tvSemanaAnteriorCrecimientoValue = view.findViewById(R.id.tvSemanaAnteriorCrecimientoValueEstadisticas);
        tvCambioCrecimientoLabel = view.findViewById(R.id.tvCambioCrecimientoLabelEstadisticas);
        tvCambioCrecimientoValue = view.findViewById(R.id.tvCambioCrecimientoValueEstadisticas);
        tvMensajeCrecimiento = view.findViewById(R.id.tvMensajeCrecimientoEstadisticas);
        tvObservacionesTitulo = view.findViewById(R.id.tvObservacionesTituloEstadisticas);
        tvObservacionContexto = view.findViewById(R.id.tvObservacionContextoEstadisticas);
        tvObservacionEstado = view.findViewById(R.id.tvObservacionEstadoEstadisticas);
        tvObservacionTexto = view.findViewById(R.id.tvObservacionTextoEstadisticas);
        tvObservacionMeta = view.findViewById(R.id.tvObservacionMetaEstadisticas);
        recyclerView = view.findViewById(R.id.rvCategoriasEstadisticas);
        recyclerPopularidad = view.findViewById(R.id.rvPopularidadEstadisticas);
        viewGraficaCategorias = view.findViewById(R.id.viewGraficaCategoriasEstadisticas);
        viewGraficaVentas = view.findViewById(R.id.viewGraficaVentasEstadisticas);
        viewGraficaCrecimiento = view.findViewById(R.id.viewGraficaCrecimientoEstadisticas);
        containerDistribucionCategorias = view.findViewById(R.id.containerDistribucionCategoriasEstadisticas);
        containerLeyendaCategorias = view.findViewById(R.id.containerLeyendaCategoriasEstadisticas);
        containerResumenVentas = view.findViewById(R.id.containerResumenVentasEstadisticas);
        containerResumenCrecimiento = view.findViewById(R.id.containerResumenCrecimientoEstadisticas);
        containerPodioPopularidad = view.findViewById(R.id.containerPodioPopularidadEstadisticas);
        rowPodioSecundario = view.findViewById(R.id.rowPodioSecundarioEstadisticas);
        layoutAccionesObservacion = view.findViewById(R.id.layoutAccionesObservacionEstadisticas);
        podioPrimero = new PodioViews(
                view.findViewById(R.id.cardPodioPrimeroEstadisticas),
                view.findViewById(R.id.tvBadgePodioPrimeroEstadisticas),
                view.findViewById(R.id.tvTagPodioPrimeroEstadisticas),
                view.findViewById(R.id.imgPodioPrimeroRectEstadisticas),
                view.findViewById(R.id.imgPodioPrimeroCircleEstadisticas),
                view.findViewById(R.id.layoutPerfilPodioPrimeroEstadisticas),
                view.findViewById(R.id.layoutAutorPodioPrimeroEstadisticas),
                view.findViewById(R.id.imgAutorPodioPrimeroEstadisticas),
                view.findViewById(R.id.tvAutorPodioPrimeroEstadisticas),
                view.findViewById(R.id.tvNombrePodioPrimeroEstadisticas),
                view.findViewById(R.id.tvSubtituloPodioPrimeroEstadisticas),
                view.findViewById(R.id.tvNombrePerfilPodioPrimeroEstadisticas),
                view.findViewById(R.id.tvSubtituloPerfilPodioPrimeroEstadisticas),
                view.findViewById(R.id.tvTotalPodioPrimeroEstadisticas),
                view.findViewById(R.id.tvDetallePodioPrimeroEstadisticas)
        );
        podioSegundo = new PodioViews(
                view.findViewById(R.id.cardPodioSegundoEstadisticas),
                view.findViewById(R.id.tvBadgePodioSegundoEstadisticas),
                view.findViewById(R.id.tvTagPodioSegundoEstadisticas),
                view.findViewById(R.id.imgPodioSegundoRectEstadisticas),
                view.findViewById(R.id.imgPodioSegundoCircleEstadisticas),
                view.findViewById(R.id.layoutPerfilPodioSegundoEstadisticas),
                view.findViewById(R.id.layoutAutorPodioSegundoEstadisticas),
                view.findViewById(R.id.imgAutorPodioSegundoEstadisticas),
                view.findViewById(R.id.tvAutorPodioSegundoEstadisticas),
                view.findViewById(R.id.tvNombrePodioSegundoEstadisticas),
                view.findViewById(R.id.tvSubtituloPodioSegundoEstadisticas),
                view.findViewById(R.id.tvNombrePerfilPodioSegundoEstadisticas),
                view.findViewById(R.id.tvSubtituloPerfilPodioSegundoEstadisticas),
                view.findViewById(R.id.tvTotalPodioSegundoEstadisticas),
                view.findViewById(R.id.tvDetallePodioSegundoEstadisticas)
        );
        podioTercero = new PodioViews(
                view.findViewById(R.id.cardPodioTerceroEstadisticas),
                view.findViewById(R.id.tvBadgePodioTerceroEstadisticas),
                view.findViewById(R.id.tvTagPodioTerceroEstadisticas),
                view.findViewById(R.id.imgPodioTerceroRectEstadisticas),
                view.findViewById(R.id.imgPodioTerceroCircleEstadisticas),
                view.findViewById(R.id.layoutPerfilPodioTerceroEstadisticas),
                view.findViewById(R.id.layoutAutorPodioTerceroEstadisticas),
                view.findViewById(R.id.imgAutorPodioTerceroEstadisticas),
                view.findViewById(R.id.tvAutorPodioTerceroEstadisticas),
                view.findViewById(R.id.tvNombrePodioTerceroEstadisticas),
                view.findViewById(R.id.tvSubtituloPodioTerceroEstadisticas),
                view.findViewById(R.id.tvNombrePerfilPodioTerceroEstadisticas),
                view.findViewById(R.id.tvSubtituloPerfilPodioTerceroEstadisticas),
                view.findViewById(R.id.tvTotalPodioTerceroEstadisticas),
                view.findViewById(R.id.tvDetallePodioTerceroEstadisticas)
        );
    }

    private void configurarRecycler() {
        categoriaAdapter = new AdminCategoriaStatsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(categoriaAdapter);

        rankingAdapter = new AdminRankingAdapter();
        recyclerPopularidad.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPopularidad.setNestedScrollingEnabled(false);
        recyclerPopularidad.setAdapter(rankingAdapter);
    }

    private void configurarMenuInferior() {
        menuInferior = requireActivity().findViewById(R.id.MenuInferiorFrame);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }
    }

    private void configurarNavegacion() {
        btnRegresar.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            boolean regreso = navController.popBackStack();
            if (!regreso) {
                try {
                    navController.navigate(R.id.fragMain);
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void configurarTabs() {
        configurarTabsPrincipales();
        btnActualizar.setOnClickListener(v -> recargarTabActivo());
        btnActualizarCategoriasMini.setOnClickListener(v -> recargarTabActivo());
        btnActualizarPopularidadMini.setOnClickListener(v -> recargarTabActivo());
        btnActualizarVentasMini.setOnClickListener(v -> recargarTabActivo());
        btnActualizarCrecimientoMini.setOnClickListener(v -> recargarTabActivo());
        btnAgregarObservacion.setOnClickListener(v -> abrirDialogoObservacion(false));
        btnEditarObservacion.setOnClickListener(v -> abrirDialogoObservacion(true));
        btnEliminarObservacion.setOnClickListener(v -> confirmarEliminarObservacion());
    }

    private void configurarTabsPrincipales() {
        if (tabLayoutPrincipal == null) {
            return;
        }
        tabLayoutPrincipal.clearOnTabSelectedListeners();
        tabLayoutPrincipal.removeAllTabs();
        tabLayoutPrincipal.addTab(crearTabPrincipal("Crecimiento"));
        tabLayoutPrincipal.addTab(crearTabPrincipal("Popularidad"));
        tabLayoutPrincipal.addTab(crearTabPrincipal("Ventas"));
        tabLayoutPrincipal.addTab(crearTabPrincipal("Categor\u00EDas"));
        seleccionarTabPrincipal(tabActiva, false);
        tabLayoutPrincipal.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                aplicarEstadoTabPrincipal(tab, true);
                TabActiva destino = obtenerTabDesdePosicion(tab.getPosition());
                if (destino != tabActiva) {
                    seleccionarTab(destino, true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                aplicarEstadoTabPrincipal(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                aplicarEstadoTabPrincipal(tab, true);
            }
        });
    }

    @NonNull
    private TabLayout.Tab crearTabPrincipal(@NonNull String texto) {
        return tabLayoutPrincipal.newTab().setText(texto).setCustomView(crearVistaTabPrincipal(texto));
    }

    @NonNull
    private TextView crearVistaTabPrincipal(@NonNull String texto) {
        TextView tabView = new TextView(requireContext());
        tabView.setText(texto);
        tabView.setGravity(Gravity.CENTER);
        tabView.setSingleLine(true);
        tabView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        tabView.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        tabView.setMinHeight(dp(46));
        tabView.setMinWidth(dp(118));
        tabView.setPadding(dp(16), dp(10), dp(16), dp(10));
        tabView.setIncludeFontPadding(false);
        return tabView;
    }

    private void seleccionarTabPrincipal(@NonNull TabActiva tabActivaObjetivo, boolean animarSeleccion) {
        if (tabLayoutPrincipal == null || tabLayoutPrincipal.getTabCount() == 0) {
            return;
        }
        int posicion = obtenerPosicionTab(tabActivaObjetivo);
        TabLayout.Tab tab = tabLayoutPrincipal.getTabAt(posicion);
        if (tab != null && tabLayoutPrincipal.getSelectedTabPosition() != posicion) {
            tab.select();
        }
        aplicarEstadoTabsPrincipales(animarSeleccion);
    }

    private void aplicarEstadoTabsPrincipales(boolean animarSeleccion) {
        if (tabLayoutPrincipal == null) {
            return;
        }
        int posicionSeleccionada = obtenerPosicionTab(tabActiva);
        for (int i = 0; i < tabLayoutPrincipal.getTabCount(); i++) {
            aplicarEstadoTabPrincipal(tabLayoutPrincipal.getTabAt(i), i == posicionSeleccionada, animarSeleccion);
        }
    }

    private void aplicarEstadoTabPrincipal(@Nullable TabLayout.Tab tab, boolean seleccionado) {
        aplicarEstadoTabPrincipal(tab, seleccionado, true);
    }

    private void aplicarEstadoTabPrincipal(@Nullable TabLayout.Tab tab, boolean seleccionado, boolean animar) {
        if (tab == null || themeManager == null) {
            return;
        }
        View customView = tab.getCustomView();
        if (!(customView instanceof TextView)) {
            return;
        }
        TextView tabView = (TextView) customView;
        tabView.animate().cancel();
        if (seleccionado) {
            tabView.setTextColor(resolverColorTextoSobre(themeManager.color(ThemeKeys.ACCENT_PRIMARY)));
            tabView.setBackground(crearFondoTabPrincipalActivo());
            tabView.setAlpha(1f);
            tabView.setScaleX(1f);
            tabView.setScaleY(1f);
            if (animar) {
                tabView.animate().scaleX(1.02f).scaleY(1.02f).setDuration(110)
                        .withEndAction(() -> tabView.animate().scaleX(1f).scaleY(1f).setDuration(110).start())
                        .start();
            }
        } else {
            tabView.setTextColor(themeManager.color(ThemeKeys.TEXT_SECONDARY));
            tabView.setBackground(crearFondoTabPrincipalInactivo());
            tabView.setAlpha(0.9f);
            tabView.setScaleX(0.985f);
            tabView.setScaleY(0.985f);
        }
    }

    @NonNull
    private GradientDrawable crearFondoTabPrincipalActivo() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dp(16));
        drawable.setColor(themeManager.color(ThemeKeys.ACCENT_PRIMARY));
        drawable.setStroke(Math.max(1, dp(1)), ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.ACCENT_PRIMARY_LIGHT), 200));
        return drawable;
    }

    @NonNull
    private GradientDrawable crearFondoTabPrincipalInactivo() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dp(16));
        drawable.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_BG), 88));
        drawable.setStroke(Math.max(1, dp(1)), ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_STROKE), 120));
        return drawable;
    }

    private int obtenerPosicionTab(@NonNull TabActiva tab) {
        switch (tab) {
            case POPULARIDAD:
                return 1;
            case VENTAS:
                return 2;
            case CATEGORIAS:
                return 3;
            case CRECIMIENTO:
            default:
                return 0;
        }
    }

    @NonNull
    private TabActiva obtenerTabDesdePosicion(int position) {
        switch (position) {
            case 1:
                return TabActiva.POPULARIDAD;
            case 2:
                return TabActiva.VENTAS;
            case 3:
                return TabActiva.CATEGORIAS;
            case 0:
            default:
                return TabActiva.CRECIMIENTO;
        }
    }

    private void configurarSelectorCategorias() {
        btnTipoObras.setOnClickListener(v -> seleccionarTipoCategoria(CategoriaTipo.OBRAS, true));
        btnTipoServicios.setOnClickListener(v -> seleccionarTipoCategoria(CategoriaTipo.SERVICIOS, true));
        btnTipoArtistas.setOnClickListener(v -> seleccionarTipoCategoria(CategoriaTipo.ARTISTAS, true));
        btnTipoObrasPopularidad.setOnClickListener(v -> seleccionarTipoPopularidad(CategoriaTipo.OBRAS, true));
        btnTipoServiciosPopularidad.setOnClickListener(v -> seleccionarTipoPopularidad(CategoriaTipo.SERVICIOS, true));
        btnTipoArtistasPopularidad.setOnClickListener(v -> seleccionarTipoPopularidad(CategoriaTipo.ARTISTAS, true));
        btnTipoObrasCrecimiento.setOnClickListener(v -> seleccionarTipoCrecimiento(CategoriaTipo.OBRAS, true));
        btnTipoServiciosCrecimiento.setOnClickListener(v -> seleccionarTipoCrecimiento(CategoriaTipo.SERVICIOS, true));
        btnTipoArtistasCrecimiento.setOnClickListener(v -> seleccionarTipoCrecimiento(CategoriaTipo.ARTISTAS, true));
        actualizarTextosCategoria();
        actualizarTextosPopularidad();
        actualizarTextosCrecimiento();
        aplicarTemaSelectorCategorias();
        aplicarTemaSelectorPopularidad();
        aplicarTemaSelectorCrecimiento();
    }

    private void configurarAccionesVentas() {
        btnSemanaAnterior.setOnClickListener(v -> irSemanaAnterior());
        btnSemanaSiguiente.setOnClickListener(v -> irSemanaSiguiente());
    }

    private void configurarAccionesCrecimiento() {
        btnSemanaAnteriorCrecimiento.setOnClickListener(v -> irSemanaAnteriorCrecimiento());
        btnSemanaSiguienteCrecimiento.setOnClickListener(v -> irSemanaSiguienteCrecimiento());
    }

    private void seleccionarTab(@NonNull TabActiva tab, boolean recargarSiCambio) {
        boolean cambio = tabActiva != tab;
        if (cambio) {
            cancelarLlamadasActivas();
        }
        tabActiva = tab;
        if (cambio && recargarSiCambio && puedeConsultarEstadisticas) {
            marcarCambioContextoObservacion();
        }
        actualizarVisibilidadTab();
        aplicarTemaTabs();
        actualizarEstadoControles(false);
        if (cambio && recargarSiCambio && puedeConsultarEstadisticas) {
            cargarContenidoActivo();
        }
    }

    private void seleccionarTipoCategoria(@NonNull CategoriaTipo tipo, boolean recargarSiCambio) {
        boolean cambio = tipoCategoriaSeleccionado != tipo;
        tipoCategoriaSeleccionado = tipo;
        actualizarTextosCategoria();
        aplicarTemaSelectorCategorias();
        if (tabActiva == TabActiva.CATEGORIAS && cambio && recargarSiCambio && puedeConsultarEstadisticas) {
            marcarCambioContextoObservacion();
            cargarCategorias();
        }
    }

    private void seleccionarTipoPopularidad(@NonNull CategoriaTipo tipo, boolean recargarSiCambio) {
        boolean cambio = tipoPopularidadSeleccionado != tipo;
        tipoPopularidadSeleccionado = tipo;
        actualizarTextosPopularidad();
        aplicarTemaSelectorPopularidad();
        if (tabActiva == TabActiva.POPULARIDAD && cambio && recargarSiCambio && puedeConsultarEstadisticas) {
            marcarCambioContextoObservacion();
            cargarPopularidad();
        }
    }

    private void seleccionarTipoCrecimiento(@NonNull CategoriaTipo tipo, boolean recargarSiCambio) {
        boolean cambio = tipoCrecimientoSeleccionado != tipo;
        tipoCrecimientoSeleccionado = tipo;
        actualizarTextosCrecimiento();
        aplicarTemaSelectorCrecimiento();
        if (tabActiva == TabActiva.CRECIMIENTO && cambio && recargarSiCambio && puedeConsultarEstadisticas) {
            marcarCambioContextoObservacion();
            cargarCrecimiento();
        }
    }

    private void validarPermisosYCargar() {
        if (!esAdminLocal()) {
            puedeConsultarEstadisticas = false;
            if (cardObservaciones != null) {
                cardObservaciones.setVisibility(View.GONE);
            }
            actualizarEstadoControles(false);
            mostrarErrorActivo("Acceso solo para administradores.");
            return;
        }
        puedeConsultarEstadisticas = true;
        actualizarEstadoControles(false);
        cargarContenidoActivo();
    }

    private boolean esAdminLocal() {
        SharedPreferences prefs = requireContext().getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE);
        String rol = prefs.getString("rol", "USER");
        return rol != null && "ADMIN".equalsIgnoreCase(rol.trim());
    }

    private void recargarTabActivo() {
        marcarCambioContextoObservacion();
        cargarContenidoActivo();
    }

    private void cargarContenidoActivo() {
        if (tabActiva == TabActiva.VENTAS) {
            cargarVentasSemanales();
            return;
        }
        if (tabActiva == TabActiva.POPULARIDAD) {
            cargarPopularidad();
            return;
        }
        if (tabActiva == TabActiva.CRECIMIENTO) {
            cargarCrecimiento();
            return;
        }
        cargarCategorias();
    }

    private void cargarCategorias() {
        if (!isAdded() || !puedeConsultarEstadisticas) {
            return;
        }

        mostrarLoadingCategorias(true);
        cargarObservacionActual();
        if (categoriasCall != null) {
            categoriasCall.cancel();
        }

        categoriasCall = adminEstadisticasApi.obtenerCategorias(tipoCategoriaSeleccionado.backendValue);
        final Call<List<AdminCategoriaStatsDTO>> callRef = categoriasCall;
        callRef.enqueue(new Callback<List<AdminCategoriaStatsDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<AdminCategoriaStatsDTO>> call,
                                   @NonNull Response<List<AdminCategoriaStatsDTO>> response) {
                if (categoriasCall == callRef) {
                    categoriasCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi()) {
                    return;
                }

                if (response.isSuccessful()) {
                    List<AdminCategoriaStatsDTO> body = response.body();
                    if (body == null || body.isEmpty()) {
                        mostrarVacioCategorias("No hay categor\u00EDas disponibles para este criterio.");
                        return;
                    }
                    mostrarContenidoCategorias(body);
                    return;
                }

                String backendMessage = ApiErrorParser.extractMessage(response);
                String mensaje;
                if (response.code() == 403) {
                    mensaje = backendMessage != null ? backendMessage : "Acceso solo para administradores.";
                } else if (response.code() == 401) {
                    mensaje = backendMessage != null ? backendMessage : "Tu sesi\u00F3n expir\u00F3 o ya no es v\u00E1lida.";
                } else {
                    mensaje = backendMessage != null ? backendMessage : "No se pudieron cargar las categor\u00EDas.";
                }
                mostrarErrorCategorias(mensaje);
            }

            @Override
            public void onFailure(@NonNull Call<List<AdminCategoriaStatsDTO>> call, @NonNull Throwable t) {
                if (categoriasCall == callRef) {
                    categoriasCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi()) {
                    return;
                }
                mostrarErrorCategorias("Error de conexi\u00F3n al cargar categor\u00EDas.");
            }
        });
    }

    private void cargarVentasSemanales() {
        if (!isAdded() || !puedeConsultarEstadisticas) {
            return;
        }

        mostrarLoadingVentas(true);
        cargarObservacionActual();
        if (ventasCall != null) {
            ventasCall.cancel();
        }

        String fechaReferencia = API_DATE_FORMATTER.format(semanaVentasSeleccionada);
        ventasCall = adminEstadisticasApi.obtenerVentasSemanales(fechaReferencia);
        final Call<AdminSerieTemporalDTO> callRef = ventasCall;
        callRef.enqueue(new Callback<AdminSerieTemporalDTO>() {
            @Override
            public void onResponse(@NonNull Call<AdminSerieTemporalDTO> call,
                                   @NonNull Response<AdminSerieTemporalDTO> response) {
                if (ventasCall == callRef) {
                    ventasCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi()) {
                    return;
                }

                if (response.isSuccessful()) {
                    AdminSerieTemporalDTO body = response.body();
                    if (body == null) {
                        mostrarErrorVentas("No se pudo interpretar la respuesta de ventas semanales.");
                        return;
                    }
                    mostrarContenidoVentas(body);
                    return;
                }

                String backendMessage = ApiErrorParser.extractMessage(response);
                String mensaje;
                if (response.code() == 400) {
                    mensaje = backendMessage != null ? backendMessage : "No se permiten semanas futuras.";
                } else if (response.code() == 403) {
                    mensaje = backendMessage != null ? backendMessage : "Acceso solo para administradores.";
                } else if (response.code() == 401) {
                    mensaje = backendMessage != null ? backendMessage : "Tu sesi\u00F3n expir\u00F3 o ya no es v\u00E1lida.";
                } else {
                    mensaje = backendMessage != null ? backendMessage : "No se pudieron cargar las ventas semanales.";
                }
                mostrarErrorVentas(mensaje);
            }

            @Override
            public void onFailure(@NonNull Call<AdminSerieTemporalDTO> call, @NonNull Throwable t) {
                if (ventasCall == callRef) {
                    ventasCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi()) {
                    return;
                }
                mostrarErrorVentas("Error de conexi\u00F3n al cargar ventas semanales.");
            }
        });
    }

    private void cargarPopularidad() {
        if (!isAdded() || !puedeConsultarEstadisticas) {
            return;
        }

        mostrarLoadingPopularidad(true);
        cargarObservacionActual();
        if (rankingCall != null) {
            rankingCall.cancel();
        }

        rankingCall = adminEstadisticasApi.obtenerRanking(tipoPopularidadSeleccionado.backendValue, RANKING_LIMIT);
        final Call<AdminRankingResponseDTO> callRef = rankingCall;
        callRef.enqueue(new Callback<AdminRankingResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<AdminRankingResponseDTO> call,
                                   @NonNull Response<AdminRankingResponseDTO> response) {
                if (rankingCall == callRef) {
                    rankingCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi()) {
                    return;
                }

                if (response.isSuccessful()) {
                    AdminRankingResponseDTO body = response.body();
                    if (body == null) {
                        mostrarErrorPopularidad("No se pudo interpretar la respuesta del ranking.");
                        return;
                    }
                    mostrarContenidoPopularidad(body);
                    return;
                }

                String backendMessage = ApiErrorParser.extractMessage(response);
                String mensaje;
                if (response.code() == 403) {
                    mensaje = backendMessage != null ? backendMessage : "Acceso solo para administradores.";
                } else if (response.code() == 401) {
                    mensaje = backendMessage != null ? backendMessage : "Tu sesi\u00F3n expir\u00F3 o ya no es v\u00E1lida.";
                } else {
                    mensaje = backendMessage != null ? backendMessage : "No se pudo cargar el ranking de popularidad.";
                }
                mostrarErrorPopularidad(mensaje);
            }

            @Override
            public void onFailure(@NonNull Call<AdminRankingResponseDTO> call, @NonNull Throwable t) {
                if (rankingCall == callRef) {
                    rankingCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi()) {
                    return;
                }
                mostrarErrorPopularidad("Error de conexi\u00F3n al cargar el ranking de popularidad.");
            }
        });
    }

    private void cargarCrecimiento() {
        if (!isAdded() || !puedeConsultarEstadisticas) {
            return;
        }

        mostrarLoadingCrecimiento(true);
        cargarObservacionActual();
        if (crecimientoCall != null) {
            crecimientoCall.cancel();
        }

        String fechaReferencia = API_DATE_FORMATTER.format(semanaCrecimientoSeleccionada);
        crecimientoCall = adminEstadisticasApi.obtenerCrecimiento(
                tipoCrecimientoSeleccionado.backendValue,
                fechaReferencia
        );
        final Call<AdminCrecimientoDTO> callRef = crecimientoCall;
        callRef.enqueue(new Callback<AdminCrecimientoDTO>() {
            @Override
            public void onResponse(@NonNull Call<AdminCrecimientoDTO> call,
                                   @NonNull Response<AdminCrecimientoDTO> response) {
                if (crecimientoCall == callRef) {
                    crecimientoCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi()) {
                    return;
                }

                if (response.isSuccessful()) {
                    AdminCrecimientoDTO body = response.body();
                    if (body == null) {
                        mostrarErrorCrecimiento("No se pudo interpretar la respuesta de crecimiento.");
                        return;
                    }
                    mostrarContenidoCrecimiento(body);
                    return;
                }

                String backendMessage = ApiErrorParser.extractMessage(response);
                String mensaje;
                if (response.code() == 400) {
                    mensaje = backendMessage != null ? backendMessage : "No se permiten semanas futuras.";
                } else if (response.code() == 403) {
                    mensaje = backendMessage != null ? backendMessage : "Acceso solo para administradores.";
                } else if (response.code() == 401) {
                    mensaje = backendMessage != null ? backendMessage : "Tu sesi\u00F3n expir\u00F3 o ya no es v\u00E1lida.";
                } else {
                    mensaje = backendMessage != null ? backendMessage : "No se pudo cargar el crecimiento semanal.";
                }
                mostrarErrorCrecimiento(mensaje);
            }

            @Override
            public void onFailure(@NonNull Call<AdminCrecimientoDTO> call, @NonNull Throwable t) {
                if (crecimientoCall == callRef) {
                    crecimientoCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi()) {
                    return;
                }
                mostrarErrorCrecimiento("Error de conexi\u00F3n al cargar el crecimiento semanal.");
            }
        });
    }

    private void mostrarLoadingCategorias(boolean loading) {
        progressCategorias.setVisibility(loading ? View.VISIBLE : View.GONE);
        actualizarEstadoControles(loading);
        if (loading) {
            tvEstadoCategorias.setVisibility(View.GONE);
            tvZeroCategorias.setVisibility(View.GONE);
            containerDistribucionCategorias.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            tvResumenTitulo.setVisibility(View.GONE);
        }
    }

    private void mostrarVacioCategorias(@NonNull String mensaje) {
        progressCategorias.setVisibility(View.GONE);
        actualizarEstadoControles(false);
        categoriasActuales.clear();
        containerDistribucionCategorias.setVisibility(View.GONE);
        if (containerLeyendaCategorias != null) {
            containerLeyendaCategorias.removeAllViews();
        }
        recyclerView.setVisibility(View.GONE);
        tvResumenTitulo.setVisibility(View.GONE);
        tvZeroCategorias.setVisibility(View.GONE);
        tvEstadoCategorias.setVisibility(View.VISIBLE);
        tvEstadoCategorias.setText(mensaje);
    }

    private void mostrarErrorCategorias(@NonNull String mensaje) {
        mostrarVacioCategorias(mensaje);
        mostrarSnackbar(mensaje);
    }

    private void mostrarContenidoCategorias(@NonNull List<AdminCategoriaStatsDTO> categorias) {
        progressCategorias.setVisibility(View.GONE);
        actualizarEstadoControles(false);
        tvEstadoCategorias.setVisibility(View.GONE);
        tvResumenTitulo.setVisibility(View.GONE);
        containerDistribucionCategorias.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        List<AdminCategoriaStatsDTO> copia = new ArrayList<>(categorias);
        categoriasActuales.clear();
        categoriasActuales.addAll(copia);
        categoriaAdapter.actualizar(copia);
        actualizarResumenCategorias(copia);
        renderizarDistribucionCategorias(copia);
    }

    private void actualizarResumenCategorias(@NonNull List<AdminCategoriaStatsDTO> categorias) {
        long totalGeneral = 0L;
        long categoriasConDatos = 0L;
        AdminCategoriaStatsDTO categoriaMayor = null;

        for (AdminCategoriaStatsDTO item : categorias) {
            if (item == null) {
                continue;
            }
            long total = Math.max(0L, item.getTotal());
            totalGeneral += total;
            if (total > 0L) {
                categoriasConDatos++;
                if (categoriaMayor == null || total > categoriaMayor.getTotal()) {
                    categoriaMayor = item;
                }
            }
        }

        tvResumenTotalCategoriasValue.setText(formatearResumenTotalCategorias(totalGeneral));
        tvResumenMayorCategoriasValue.setText(categoriaMayor != null
                ? normalizarCategoria(categoriaMayor.getCategoria())
                : "Sin registros");
        tvResumenConDatosCategoriasValue.setText(String.valueOf(categoriasConDatos));
    }

    private void renderizarDistribucionCategorias(@NonNull List<AdminCategoriaStatsDTO> categorias) {
        AdminChartThemeHelper.CategoriaDonutModel model =
                AdminChartThemeHelper.buildCategoriasDonutModel(categorias, themeManager);

        if (model.isEmpty()) {
            viewGraficaCategorias.clear();
            viewGraficaCategorias.setVisibility(View.GONE);
            cardLeyendaCategorias.setVisibility(View.GONE);
            containerLeyendaCategorias.removeAllViews();
            tvZeroCategorias.setVisibility(View.VISIBLE);
            tvZeroCategorias.setText("No hay registros disponibles para esta distribuci\u00F3n.");
            return;
        }

        tvZeroCategorias.setVisibility(View.GONE);
        viewGraficaCategorias.setVisibility(View.VISIBLE);
        cardLeyendaCategorias.setVisibility(View.VISIBLE);
        AdminChartThemeHelper.renderCategoriasDonutChart(viewGraficaCategorias, model, themeManager);
        viewGraficaCategorias.setCenterText(formatearCentroDona(model));
        renderizarLeyendaCategorias(model);
    }

    private void renderizarLeyendaCategorias(@NonNull AdminChartThemeHelper.CategoriaDonutModel model) {
        containerLeyendaCategorias.removeAllViews();
        if (model.isEmpty()) {
            cardLeyendaCategorias.setVisibility(View.GONE);
            return;
        }

        for (int i = 0; i < model.getItems().size(); i++) {
            AdminChartThemeHelper.CategoriaDonutLegendItem item = model.getItems().get(i);
            containerLeyendaCategorias.addView(crearFilaLeyendaCategoria(item));
            if (i < model.getItems().size() - 1) {
                View separador = new View(requireContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(1)
                );
                lp.topMargin = dp(6);
                lp.bottomMargin = dp(6);
                separador.setLayoutParams(lp);
                CardThemeHelper.applySubtleDivider(separador, themeManager);
                containerLeyendaCategorias.addView(separador);
            }
        }
    }

    @NonNull
    private View crearFilaLeyendaCategoria(@NonNull AdminChartThemeHelper.CategoriaDonutLegendItem item) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        View punto = new View(requireContext());
        LinearLayout.LayoutParams puntoLp = new LinearLayout.LayoutParams(dp(10), dp(10));
        puntoLp.rightMargin = dp(10);
        punto.setLayoutParams(puntoLp);
        GradientDrawable puntoBg = new GradientDrawable();
        puntoBg.setShape(GradientDrawable.OVAL);
        puntoBg.setColor(item.getColor());
        punto.setBackground(puntoBg);

        TextView nombre = new TextView(requireContext());
        LinearLayout.LayoutParams nombreLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        nombre.setLayoutParams(nombreLp);
        nombre.setText(item.getCategoria());
        nombre.setMaxLines(2);
        nombre.setEllipsize(TextUtils.TruncateAt.END);
        nombre.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        ThemeApplier.applyTextPrimary(nombre, themeManager);

        TextView meta = new TextView(requireContext());
        meta.setText(formatearMetaLeyenda(item));
        meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        meta.setPadding(dp(10), 0, 0, 0);
        ThemeApplier.applyTextSecondary(meta, themeManager);

        row.addView(punto);
        row.addView(nombre);
        row.addView(meta);
        return row;
    }

    private void mostrarLoadingPopularidad(boolean loading) {
        progressPopularidad.setVisibility(loading ? View.VISIBLE : View.GONE);
        actualizarEstadoControles(loading);
        if (loading) {
            tvEstadoPopularidad.setVisibility(View.GONE);
            tvMensajePopularidad.setVisibility(View.GONE);
            tvTopRankingPopularidad.setVisibility(View.GONE);
            recyclerPopularidad.setVisibility(View.GONE);
            containerPodioPopularidad.setVisibility(View.GONE);
            podioPrimero.card.setVisibility(View.GONE);
            rowPodioSecundario.setVisibility(View.GONE);
        }
    }

    private void mostrarVacioPopularidad(@NonNull String mensaje) {
        progressPopularidad.setVisibility(View.GONE);
        actualizarEstadoControles(false);
        rankingActual.clear();
        tvTopRankingPopularidad.setVisibility(View.GONE);
        recyclerPopularidad.setVisibility(View.GONE);
        tvMensajePopularidad.setVisibility(View.GONE);
        containerPodioPopularidad.setVisibility(View.GONE);
        podioPrimero.card.setVisibility(View.GONE);
        rowPodioSecundario.setVisibility(View.GONE);
        tvEstadoPopularidad.setVisibility(View.VISIBLE);
        tvEstadoPopularidad.setText(mensaje);
    }

    private void mostrarErrorPopularidad(@NonNull String mensaje) {
        mostrarVacioPopularidad(mensaje);
        mostrarSnackbar(mensaje);
    }

    private void mostrarContenidoPopularidad(@NonNull AdminRankingResponseDTO response) {
        progressPopularidad.setVisibility(View.GONE);
        actualizarEstadoControles(false);
        tvEstadoPopularidad.setVisibility(View.GONE);

        List<AdminRankingItemDTO> items = new ArrayList<>(response.getItems());
        rankingActual.clear();
        rankingActual.addAll(items);

        if (items.isEmpty()) {
            String mensaje = response.getMensaje();
            if (mensaje == null || mensaje.trim().isEmpty()) {
                mensaje = "No hay datos de popularidad disponibles para este criterio.";
            }
            mostrarVacioPopularidad(mensaje);
            return;
        }

        rankingAdapter.actualizar(items, tipoPopularidadSeleccionado.backendValue);
        renderizarPodioPopularidad(items);

        containerPodioPopularidad.setVisibility(View.VISIBLE);
        tvTopRankingPopularidad.setVisibility(View.VISIBLE);
        recyclerPopularidad.setVisibility(View.VISIBLE);
        tvMensajePopularidad.setVisibility(View.VISIBLE);
        tvMensajePopularidad.setText(obtenerMensajePopularidad(response));
    }

    private void mostrarLoadingVentas(boolean loading) {
        progressVentas.setVisibility(loading ? View.VISIBLE : View.GONE);
        actualizarEstadoControles(loading);
        if (loading) {
            tvEstadoVentas.setVisibility(View.GONE);
            viewGraficaVentas.setVisibility(View.GONE);
            containerResumenVentas.setVisibility(View.GONE);
            tvMensajeVentas.setVisibility(View.GONE);
        }
    }

    private void mostrarErrorVentas(@NonNull String mensaje) {
        progressVentas.setVisibility(View.GONE);
        actualizarEstadoControles(false);
        ventasActuales.clear();
        viewGraficaVentas.setVisibility(View.GONE);
        containerResumenVentas.setVisibility(View.GONE);
        tvMensajeVentas.setVisibility(View.GONE);
        tvEstadoVentas.setVisibility(View.VISIBLE);
        tvEstadoVentas.setText(mensaje);
        mostrarSnackbar(mensaje);
    }

    private void mostrarContenidoVentas(@NonNull AdminSerieTemporalDTO serie) {
        progressVentas.setVisibility(View.GONE);
        tvEstadoVentas.setVisibility(View.GONE);
        viewGraficaVentas.setVisibility(View.VISIBLE);
        containerResumenVentas.setVisibility(View.VISIBLE);
        tvMensajeVentas.setVisibility(View.VISIBLE);

        LocalDate inicioPeriodo = parseFecha(serie.getFechaInicioPeriodo());
        if (inicioPeriodo == null) {
            inicioPeriodo = semanaVentasSeleccionada;
        } else {
            semanaVentasSeleccionada = inicioPeriodo.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        LocalDate finPeriodo = parseFecha(serie.getFechaFinPeriodo());
        if (finPeriodo == null) {
            finPeriodo = inicioPeriodo.plusDays(6);
        }

        actualizarRangoSemanaVentas(inicioPeriodo, finPeriodo);
        List<AdminPuntoSerieDTO> puntos = normalizarPuntosSemana(serie.getPuntos(), inicioPeriodo);
        ventasActuales.clear();
        ventasActuales.addAll(puntos);
        AdminChartThemeHelper.renderVentasChart(viewGraficaVentas, puntos, themeManager);

        tvTotalVentasValue.setText(String.valueOf(Math.max(0L, serie.getTotalVentas())));
        tvTotalIngresosValue.setText(formatearMoneda(serie.getTotalIngresosSeguro()));

        String mensaje = serie.getMensaje();
        if (mensaje == null || mensaje.trim().isEmpty()) {
            mensaje = serie.getTotalVentas() > 0
                    ? "Se registraron ventas completadas durante la semana seleccionada."
                    : "No se registraron ventas completadas en la semana seleccionada.";
        }
        tvMensajeVentas.setText(mensaje);
        actualizarEstadoControles(false);
    }

    private void mostrarLoadingCrecimiento(boolean loading) {
        progressCrecimiento.setVisibility(loading ? View.VISIBLE : View.GONE);
        actualizarEstadoControles(loading);
        if (loading) {
            tvEstadoCrecimiento.setVisibility(View.GONE);
            viewGraficaCrecimiento.setVisibility(View.GONE);
            containerResumenCrecimiento.setVisibility(View.GONE);
            tvMensajeCrecimiento.setVisibility(View.GONE);
        }
    }

    private void mostrarErrorCrecimiento(@NonNull String mensaje) {
        progressCrecimiento.setVisibility(View.GONE);
        actualizarEstadoControles(false);
        crecimientoActual.clear();
        crecimientoAnterior.clear();
        ultimoCrecimiento = null;
        ultimoIndiceRealCrecimiento = -1;
        viewGraficaCrecimiento.setVisibility(View.GONE);
        containerResumenCrecimiento.setVisibility(View.GONE);
        tvMensajeCrecimiento.setVisibility(View.GONE);
        tvEstadoCrecimiento.setVisibility(View.VISIBLE);
        tvEstadoCrecimiento.setText(mensaje);
        mostrarSnackbar(mensaje);
    }

    private void mostrarContenidoCrecimiento(@NonNull AdminCrecimientoDTO crecimiento) {
        progressCrecimiento.setVisibility(View.GONE);
        tvEstadoCrecimiento.setVisibility(View.GONE);
        viewGraficaCrecimiento.setVisibility(View.VISIBLE);
        containerResumenCrecimiento.setVisibility(View.VISIBLE);
        tvMensajeCrecimiento.setVisibility(View.VISIBLE);

        LocalDate inicioSemanaActual = parseFecha(crecimiento.getFechaInicioSemanaActual());
        if (inicioSemanaActual == null) {
            inicioSemanaActual = semanaCrecimientoSeleccionada;
        } else {
            semanaCrecimientoSeleccionada = inicioSemanaActual.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        LocalDate finSemanaActual = parseFecha(crecimiento.getFechaFinSemanaActual());
        if (finSemanaActual == null) {
            finSemanaActual = inicioSemanaActual.plusDays(6);
        }

        actualizarRangoSemanaCrecimiento(inicioSemanaActual, finSemanaActual);
        LocalDate inicioSemanaAnterior = parseFecha(crecimiento.getFechaInicioSemanaAnterior());
        if (inicioSemanaAnterior == null) {
            inicioSemanaAnterior = inicioSemanaActual.minusWeeks(1);
        }

        List<AdminPuntoSerieDTO> puntosActuales = normalizarPuntosSemana(
                crecimiento.getSerieSemanaActual(),
                inicioSemanaActual
        );
        List<AdminPuntoSerieDTO> puntosAnteriores = normalizarPuntosSemana(
                crecimiento.getSerieSemanaAnterior(),
                inicioSemanaAnterior
        );
        ultimoIndiceRealCrecimiento = resolverUltimoIndiceRealCrecimiento(inicioSemanaActual, finSemanaActual);
        crecimientoActual.clear();
        crecimientoAnterior.clear();
        crecimientoActual.addAll(puntosActuales);
        crecimientoAnterior.addAll(puntosAnteriores);
        ultimoCrecimiento = crecimiento;

        AdminChartThemeHelper.renderCrecimientoChart(
                viewGraficaCrecimiento,
                puntosActuales,
                puntosAnteriores,
                ultimoIndiceRealCrecimiento,
                themeManager
        );

        tvSemanaActualCrecimientoValue.setText(
                formatearTotalCrecimiento(crecimiento.getTotalSemanaActual())
        );
        tvSemanaAnteriorCrecimientoValue.setText(
                formatearTotalCrecimiento(crecimiento.getTotalSemanaAnterior())
        );
        tvCambioCrecimientoValue.setText(formatearCambioCrecimiento(crecimiento));

        String mensaje = crecimiento.getMensaje();
        if (mensaje == null || mensaje.trim().isEmpty()) {
            if (crecimiento.isPeriodoAnteriorSinDatos()) {
                mensaje = "No hay actividad registrada en la semana anterior completa.";
            } else if (crecimiento.getTotalSemanaActual() > 0L || crecimiento.getTotalSemanaAnterior() > 0L) {
                mensaje = "Consulta el avance acumulado de la semana seleccionada frente a la semana anterior.";
            } else {
                mensaje = "Sin actividad registrada en ambas semanas.";
            }
        }
        tvMensajeCrecimiento.setText(mensaje);
        actualizarEstadoControles(false);
    }

    private void cargarObservacionActual() {
        if (!isAdded() || !puedeConsultarEstadisticas || observacionMutationLoading) {
            return;
        }

        ObservacionContexto contexto = resolverContextoObservacionActual();
        final long requestId = prepararConsultaObservacion(contexto);

        observacionesCall = adminEstadisticasApi.obtenerObservaciones(
                contexto.tipoEstadistica,
                contexto.tipoDato,
                formatearFechaApiNullable(contexto.fechaInicioPeriodo),
                formatearFechaApiNullable(contexto.fechaFinPeriodo)
        );
        final String contextKey = contexto.buildKey();
        final Call<List<AdminObservacionDTO>> callRef = observacionesCall;
        callRef.enqueue(new Callback<List<AdminObservacionDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<AdminObservacionDTO>> call,
                                   @NonNull Response<List<AdminObservacionDTO>> response) {
                if (observacionesCall == callRef) {
                    observacionesCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi()
                        || !esSolicitudObservacionVigente(requestId, contextKey)) {
                    return;
                }

                if (response.isSuccessful()) {
                    List<AdminObservacionDTO> body = response.body();
                    if (body == null || body.isEmpty()) {
                        mostrarObservacionVacia(contexto);
                        return;
                    }
                    AdminObservacionDTO observacion = body.get(0);
                    if (observacion == null) {
                        mostrarObservacionVacia(contexto);
                        return;
                    }
                    mostrarContenidoObservacion(contexto, observacion);
                    return;
                }

                String backendMessage = ApiErrorParser.extractMessage(response);
                String mensaje;
                if (response.code() == 403) {
                    mensaje = backendMessage != null ? backendMessage : "Acceso solo para administradores.";
                } else if (response.code() == 401) {
                    mensaje = backendMessage != null ? backendMessage : "Tu sesi\u00F3n expir\u00F3 o ya no es v\u00E1lida.";
                } else {
                    mensaje = backendMessage != null ? backendMessage : "No se pudo cargar la observaci\u00F3n de esta estad\u00EDstica.";
                }
                mostrarErrorObservacion(contexto, mensaje);
            }

            @Override
            public void onFailure(@NonNull Call<List<AdminObservacionDTO>> call, @NonNull Throwable t) {
                if (observacionesCall == callRef) {
                    observacionesCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi()
                        || !esSolicitudObservacionVigente(requestId, contextKey)) {
                    return;
                }
                mostrarErrorObservacion(contexto, "Error de conexi\u00F3n al cargar la observaci\u00F3n.");
            }
        });
    }

    private void marcarCambioContextoObservacion() {
        if (!isAdded() || !puedeConsultarEstadisticas || observacionMutationLoading) {
            return;
        }
        if (observacionesCall != null) {
            observacionesCall.cancel();
            observacionesCall = null;
        }
        registrarNuevaSolicitudObservacion();
        mostrarLoadingObservacion(resolverContextoObservacionActual());
    }

    private long prepararConsultaObservacion(@NonNull ObservacionContexto contexto) {
        if (observacionesCall != null) {
            observacionesCall.cancel();
            observacionesCall = null;
        }
        long requestId = registrarNuevaSolicitudObservacion();
        mostrarLoadingObservacion(contexto);
        return requestId;
    }

    private long registrarNuevaSolicitudObservacion() {
        observacionRequestActivo = ++observacionRequestSequence;
        return observacionRequestActivo;
    }

    private void mostrarLoadingObservacion(@NonNull ObservacionContexto contexto) {
        observacionContextoActual = contexto;
        observacionActual = null;
        observacionEstadoMensaje = "Cargando observaci\u00F3n...";
        observacionEstadoEsError = false;
        observacionLoading = true;
        renderizarEstadoObservacionActual();
    }

    private void mostrarObservacionVacia(@NonNull ObservacionContexto contexto) {
        observacionContextoActual = contexto;
        observacionActual = null;
        observacionEstadoMensaje = "No hay observaciones para esta estad\u00EDstica.";
        observacionEstadoEsError = false;
        observacionLoading = false;
        observacionMutationLoading = false;
        renderizarEstadoObservacionActual();
    }

    private void mostrarContenidoObservacion(@NonNull ObservacionContexto contexto,
                                             @NonNull AdminObservacionDTO observacion) {
        observacionContextoActual = contexto;
        observacionActual = observacion;
        observacionEstadoMensaje = null;
        observacionEstadoEsError = false;
        observacionLoading = false;
        observacionMutationLoading = false;
        renderizarEstadoObservacionActual();
    }

    private void mostrarErrorObservacion(@NonNull ObservacionContexto contexto, @NonNull String mensaje) {
        observacionContextoActual = contexto;
        observacionActual = null;
        observacionEstadoMensaje = mensaje;
        observacionEstadoEsError = true;
        observacionLoading = false;
        observacionMutationLoading = false;
        renderizarEstadoObservacionActual();
    }

    private void renderizarEstadoObservacionActual() {
        if (cardObservaciones == null || tvObservacionEstado == null || tvObservacionContexto == null) {
            return;
        }

        if (observacionContextoActual == null) {
            cardObservaciones.setVisibility(View.GONE);
            return;
        }

        cardObservaciones.setVisibility(View.VISIBLE);
        tvObservacionContexto.setText(construirDescripcionContextoObservacion(observacionContextoActual));
        progressObservacion.setVisibility((observacionLoading || observacionMutationLoading) ? View.VISIBLE : View.GONE);

        if (observacionLoading) {
            tvObservacionEstado.setVisibility(View.VISIBLE);
            tvObservacionEstado.setText(observacionEstadoMensaje);
            tvObservacionTexto.setVisibility(View.GONE);
            tvObservacionMeta.setVisibility(View.GONE);
            btnAgregarObservacion.setVisibility(View.GONE);
            btnEditarObservacion.setVisibility(View.GONE);
            btnEliminarObservacion.setVisibility(View.GONE);
            layoutAccionesObservacion.setVisibility(View.GONE);
            return;
        }

        if (observacionActual != null) {
            tvObservacionEstado.setVisibility(View.GONE);
            tvObservacionTexto.setVisibility(View.VISIBLE);
            tvObservacionTexto.setText(observacionActual.getObservacion());

            String meta = construirMetaObservacion(observacionActual);
            if (TextUtils.isEmpty(meta)) {
                tvObservacionMeta.setVisibility(View.GONE);
            } else {
                tvObservacionMeta.setVisibility(View.VISIBLE);
                tvObservacionMeta.setText(meta);
            }

            layoutAccionesObservacion.setVisibility(View.VISIBLE);
            btnAgregarObservacion.setVisibility(View.GONE);
            btnEditarObservacion.setVisibility(View.VISIBLE);
            btnEliminarObservacion.setVisibility(View.VISIBLE);
            actualizarEstadoVista(btnEditarObservacion, !observacionMutationLoading);
            actualizarEstadoVista(btnEliminarObservacion, !observacionMutationLoading);
            return;
        }

        tvObservacionTexto.setVisibility(View.GONE);
        tvObservacionMeta.setVisibility(View.GONE);
        tvObservacionEstado.setVisibility(View.VISIBLE);
        tvObservacionEstado.setText(
                TextUtils.isEmpty(observacionEstadoMensaje)
                        ? "No hay observaciones para esta estad\u00EDstica."
                        : observacionEstadoMensaje
        );

        if (observacionEstadoEsError) {
            btnAgregarObservacion.setVisibility(View.GONE);
            btnEditarObservacion.setVisibility(View.GONE);
            btnEliminarObservacion.setVisibility(View.GONE);
            layoutAccionesObservacion.setVisibility(View.GONE);
            return;
        }

        layoutAccionesObservacion.setVisibility(View.VISIBLE);
        btnAgregarObservacion.setVisibility(View.VISIBLE);
        btnEditarObservacion.setVisibility(View.GONE);
        btnEliminarObservacion.setVisibility(View.GONE);
        actualizarEstadoVista(btnAgregarObservacion, !observacionMutationLoading);
    }
    private void abrirDialogoObservacion(boolean editar) {
        if (!canInteractWithUi() || observacionContextoActual == null || observacionLoading || observacionMutationLoading) {
            return;
        }
        if (editar && observacionActual == null) {
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_admin_observacion_estadistica, null, false);
        View dialogRoot = dialogView.findViewById(R.id.layoutObservacionDialogRoot);
        TextView tvBadgeDialog = dialogView.findViewById(R.id.tvObservacionDialogBadge);
        TextView tvTituloDialog = dialogView.findViewById(R.id.tvObservacionDialogTitulo);
        TextView tvSubtituloDialog = dialogView.findViewById(R.id.tvObservacionDialogSubtitulo);
        TextView tvContextoLabel = dialogView.findViewById(R.id.tvObservacionDialogContextoLabel);
        TextView tvContextoDialog = dialogView.findViewById(R.id.tvObservacionDialogContexto);
        TextView tvAyudaDialog = dialogView.findViewById(R.id.tvObservacionDialogAyuda);
        View containerContextoDialog = dialogView.findViewById(R.id.containerObservacionDialogContexto);
        TextInputLayout tilObservacion = dialogView.findViewById(R.id.tilObservacionDialog);
        TextInputEditText etObservacion = dialogView.findViewById(R.id.etObservacionDialog);
        ProgressBar progressDialog = dialogView.findViewById(R.id.progressObservacionDialog);
        Button btnCancelarDialog = dialogView.findViewById(R.id.btnObservacionDialogCancelar);
        Button btnGuardarDialog = dialogView.findViewById(R.id.btnObservacionDialogGuardar);

        tvBadgeDialog.setText(editar ? "Edit" : "Obs");
        tvTituloDialog.setText(editar ? "Editar observaci\u00F3n" : "Nueva observaci\u00F3n");
        tvSubtituloDialog.setText(editar
                ? "Actualiza la nota asociada al contexto seleccionado."
                : "Guarda una nota breve para el contexto seleccionado.");
        tvContextoDialog.setText(construirDescripcionContextoObservacion(observacionContextoActual));
        if (editar && observacionActual != null && observacionActual.getObservacion() != null) {
            etObservacion.setText(observacionActual.getObservacion());
            etObservacion.setSelection(etObservacion.length());
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialog.setOnShowListener(d -> {
            dialogObservacion = dialog;
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            DialogThemeHelper.applyFieldDialogWindowSize(dialog, requireContext());
            dialogRoot.setBackground(DialogThemeHelper.createFieldDialogBackground(requireContext()));
            CardThemeHelper.applySecondaryBubbleSurface(tvBadgeDialog, tvBadgeDialog, themeManager);
            CardThemeHelper.applyGlassChipSection(containerContextoDialog, themeManager, 16);
            DialogThemeHelper.applyLightGlassTextInputLayoutStyle(tilObservacion, requireContext());
            DialogThemeHelper.applyLightGlassTextInputEditTextStyle(etObservacion, requireContext());
            ThemeApplier.applyTextPrimary(tvTituloDialog, themeManager);
            ThemeApplier.applyTextSecondary(tvSubtituloDialog, themeManager);
            ThemeApplier.applyTextSecondary(tvContextoLabel, themeManager);
            ThemeApplier.applyTextSecondary(tvContextoDialog, themeManager);
            ThemeApplier.applyTextSecondary(tvAyudaDialog, themeManager);
            CardThemeHelper.applySecondaryBubbleButton(btnCancelarDialog, themeManager);
            CardThemeHelper.applyPrimaryBubbleButton(btnGuardarDialog, themeManager);
            CardThemeHelper.tintProgress(progressDialog, themeManager);

            btnCancelarDialog.setOnClickListener(v -> dialog.dismiss());
            btnGuardarDialog.setOnClickListener(v -> {
                String texto = etObservacion.getText() != null ? etObservacion.getText().toString().trim() : "";
                if (texto.isEmpty()) {
                    tilObservacion.setError("Escribe una observaci\u00F3n.");
                    etObservacion.requestFocus();
                    return;
                }
                tilObservacion.setError(null);
                guardarObservacion(
                        observacionContextoActual,
                        texto,
                        editar && observacionActual != null ? observacionActual.getIdObservacion() : null,
                        dialog,
                        btnGuardarDialog,
                        btnCancelarDialog,
                        progressDialog,
                        etObservacion,
                        tilObservacion
                );
            });
        });
        dialog.setOnDismissListener(d -> {
            if (dialogObservacion == dialog) {
                dialogObservacion = null;
            }
        });
        dialog.show();
    }

    private void guardarObservacion(@Nullable ObservacionContexto contexto,
                                    @NonNull String texto,
                                    @Nullable Integer idObservacion,
                                    @NonNull AlertDialog dialog,
                                    @NonNull Button primaryButton,
                                    @NonNull Button secondaryButton,
                                    @NonNull ProgressBar progressDialog,
                                    @NonNull TextInputEditText editText,
                                    @NonNull TextInputLayout inputLayout) {
        if (contexto == null) {
            return;
        }

        AdminObservacionRequestDTO request = new AdminObservacionRequestDTO();
        request.setTipoEstadistica(contexto.tipoEstadistica);
        request.setTipoDato(contexto.tipoDato);
        request.setFechaInicioPeriodo(formatearFechaApiNullable(contexto.fechaInicioPeriodo));
        request.setFechaFinPeriodo(formatearFechaApiNullable(contexto.fechaFinPeriodo));
        request.setObservacion(texto);

        observacionMutationLoading = true;
        renderizarEstadoObservacionActual();
        primaryButton.setEnabled(false);
        secondaryButton.setEnabled(false);
        progressDialog.setVisibility(View.VISIBLE);
        editText.setEnabled(false);

        if (observacionMutationCall != null) {
            observacionMutationCall.cancel();
        }

        boolean esEdicion = idObservacion != null;
        observacionMutationCall = esEdicion
                ? adminEstadisticasApi.actualizarObservacion(idObservacion, request)
                : adminEstadisticasApi.crearObservacion(request);
        final String contextKey = contexto.buildKey();
        final Call<AdminObservacionDTO> callRef = observacionMutationCall;
        callRef.enqueue(new Callback<AdminObservacionDTO>() {
            @Override
            public void onResponse(@NonNull Call<AdminObservacionDTO> call,
                                   @NonNull Response<AdminObservacionDTO> response) {
                if (observacionMutationCall == callRef) {
                    observacionMutationCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi() || !esMismaClaveObservacion(contextKey)) {
                    return;
                }

                if (response.isSuccessful()) {
                    AdminObservacionDTO body = response.body();
                    observacionMutationLoading = false;
                    progressDialog.setVisibility(View.GONE);
                    if (body != null) {
                        mostrarContenidoObservacion(contexto, body);
                    } else {
                        cargarObservacionActual();
                    }
                    dialog.dismiss();
                    mostrarSnackbar(esEdicion
                            ? "Observaci\u00F3n actualizada."
                            : "Observaci\u00F3n guardada.");
                    return;
                }

                observacionMutationLoading = false;
                String backendMessage = ApiErrorParser.extractMessage(response);
                if (response.code() == 409 && !esEdicion) {
                    progressDialog.setVisibility(View.GONE);
                    dialog.dismiss();
                    mostrarSnackbar(backendMessage != null
                            ? backendMessage
                            : "Ya existe una observaci\u00F3n para este contexto.");
                    cargarObservacionActual();
                    return;
                }

                String mensaje;
                if (response.code() == 403) {
                    mensaje = backendMessage != null ? backendMessage : "Acceso solo para administradores.";
                } else if (response.code() == 401) {
                    mensaje = backendMessage != null ? backendMessage : "Tu sesi\u00F3n expir\u00F3 o ya no es v\u00E1lida.";
                } else {
                    mensaje = backendMessage != null ? backendMessage : "No se pudo guardar la observaci\u00F3n.";
                }
                inputLayout.setError(mensaje);
                primaryButton.setEnabled(true);
                secondaryButton.setEnabled(true);
                progressDialog.setVisibility(View.GONE);
                editText.setEnabled(true);
                renderizarEstadoObservacionActual();
            }

            @Override
            public void onFailure(@NonNull Call<AdminObservacionDTO> call, @NonNull Throwable t) {
                if (observacionMutationCall == callRef) {
                    observacionMutationCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi() || !esMismaClaveObservacion(contextKey)) {
                    return;
                }
                observacionMutationLoading = false;
                inputLayout.setError("Error de conexi\u00F3n al guardar la observaci\u00F3n.");
                primaryButton.setEnabled(true);
                secondaryButton.setEnabled(true);
                progressDialog.setVisibility(View.GONE);
                editText.setEnabled(true);
                renderizarEstadoObservacionActual();
            }
        });
    }

    private void confirmarEliminarObservacion() {
        if (observacionActual == null || observacionMutationLoading || !canInteractWithUi()) {
            return;
        }

        ArtistlanDialogFactory.show(this, DialogConfig.builder()
                .setType(DialogConfig.Type.DANGER)
                .setTitle("Eliminar observaci\u00F3n")
                .setMessage("Esta acci\u00F3n quitar\u00E1 la observaci\u00F3n del contexto actual.")
                .setPositiveText("Eliminar")
                .setNegativeText("Cancelar")
                .setOnPositive(this::eliminarObservacionActual)
                .build());
    }

    private void eliminarObservacionActual() {
        if (observacionActual == null || observacionActual.getIdObservacion() == null || observacionContextoActual == null) {
            return;
        }

        if (observacionDeleteCall != null) {
            observacionDeleteCall.cancel();
        }

        observacionMutationLoading = true;
        renderizarEstadoObservacionActual();
        final String contextKey = observacionContextoActual.buildKey();
        final ObservacionContexto contexto = observacionContextoActual;
        observacionDeleteCall = adminEstadisticasApi.eliminarObservacion(observacionActual.getIdObservacion());
        final Call<Void> callRef = observacionDeleteCall;
        callRef.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (observacionDeleteCall == callRef) {
                    observacionDeleteCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi() || !esMismaClaveObservacion(contextKey)) {
                    return;
                }

                if (response.isSuccessful()) {
                    observacionMutationLoading = false;
                    mostrarObservacionVacia(contexto);
                    mostrarSnackbar("Observaci\u00F3n eliminada.");
                    return;
                }

                observacionMutationLoading = false;
                String backendMessage = ApiErrorParser.extractMessage(response);
                String mensaje;
                if (response.code() == 403) {
                    mensaje = backendMessage != null ? backendMessage : "Acceso solo para administradores.";
                } else if (response.code() == 401) {
                    mensaje = backendMessage != null ? backendMessage : "Tu sesi\u00F3n expir\u00F3 o ya no es v\u00E1lida.";
                } else {
                    mensaje = backendMessage != null ? backendMessage : "No se pudo eliminar la observaci\u00F3n.";
                }
                renderizarEstadoObservacionActual();
                mostrarSnackbar(mensaje);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (observacionDeleteCall == callRef) {
                    observacionDeleteCall = null;
                }
                if (call.isCanceled() || !canInteractWithUi() || !esMismaClaveObservacion(contextKey)) {
                    return;
                }
                observacionMutationLoading = false;
                renderizarEstadoObservacionActual();
                mostrarSnackbar("Error de conexi\u00F3n al eliminar la observaci\u00F3n.");
            }
        });
    }

    @NonNull
    private ObservacionContexto resolverContextoObservacionActual() {
        switch (tabActiva) {
            case VENTAS:
                return new ObservacionContexto(
                        "VENTAS",
                        "SEMANAL",
                        semanaVentasSeleccionada,
                        semanaVentasSeleccionada.plusDays(6)
                );
            case CRECIMIENTO:
                return new ObservacionContexto(
                        "CRECIMIENTO",
                        obtenerTipoDatoCrecimientoObservacion(),
                        semanaCrecimientoSeleccionada,
                        semanaCrecimientoSeleccionada.plusDays(6)
                );
            case CATEGORIAS:
                return new ObservacionContexto(
                        "CATEGORIAS",
                        tipoCategoriaSeleccionado.backendValue,
                        null,
                        null
                );
            case POPULARIDAD:
            default:
                return new ObservacionContexto(
                        "POPULARIDAD",
                        tipoPopularidadSeleccionado.backendValue,
                        null,
                        null
                );
        }
    }

    @NonNull
    private String obtenerTipoDatoCrecimientoObservacion() {
        switch (tipoCrecimientoSeleccionado) {
            case SERVICIOS:
                return "SERVICIOS_PUBLICADOS";
            case ARTISTAS:
                return "ARTISTAS_NUEVOS";
            case OBRAS:
            default:
                return "OBRAS_PUBLICADAS";
        }
    }

    private boolean esMismaClaveObservacion(@NonNull String contextKey) {
        return observacionContextoActual != null && contextKey.equals(observacionContextoActual.buildKey());
    }

    private boolean esSolicitudObservacionVigente(long requestId, @NonNull String contextKey) {
        return observacionRequestActivo == requestId && esMismaClaveObservacion(contextKey);
    }

    @Nullable
    private String formatearFechaApiNullable(@Nullable LocalDate fecha) {
        return fecha != null ? API_DATE_FORMATTER.format(fecha) : null;
    }

    @NonNull
    private String construirDescripcionContextoObservacion(@NonNull ObservacionContexto contexto) {
        String rango = contexto.fechaInicioPeriodo != null && contexto.fechaFinPeriodo != null
                ? VIEW_DATE_FORMATTER.format(contexto.fechaInicioPeriodo) + " - "
                + VIEW_DATE_FORMATTER.format(contexto.fechaFinPeriodo)
                : null;

        switch (contexto.tipoEstadistica) {
            case "VENTAS":
                return rango == null
                        ? "Ventas semanales"
                        : "Ventas semanales \u00B7 " + rango;
            case "CRECIMIENTO":
                return rango == null
                        ? "Crecimiento \u00B7 " + obtenerEtiquetaTipoDatoObservacion(contexto.tipoDato)
                        : "Crecimiento \u00B7 " + obtenerEtiquetaTipoDatoObservacion(contexto.tipoDato) + " \u00B7 " + rango;
            case "CATEGORIAS":
                return "Categor\u00EDas \u00B7 " + obtenerEtiquetaTipoDatoObservacion(contexto.tipoDato);
            case "POPULARIDAD":
            default:
                return "Popularidad \u00B7 " + obtenerEtiquetaTipoDatoObservacion(contexto.tipoDato);
        }
    }

    @NonNull
    private String obtenerEtiquetaTipoDatoObservacion(@Nullable String tipoDato) {
        if (tipoDato == null) {
            return "General";
        }
        switch (tipoDato) {
            case "OBRAS":
                return "Obras";
            case "SERVICIOS":
                return "Servicios";
            case "ARTISTAS":
                return "Artistas";
            case "SEMANAL":
                return "Semanal";
            case "OBRAS_PUBLICADAS":
                return "Obras publicadas";
            case "SERVICIOS_PUBLICADOS":
                return "Servicios publicados";
            case "ARTISTAS_NUEVOS":
                return "Artistas nuevos";
            default:
                return tipoDato;
        }
    }

    @Nullable
    private String construirMetaObservacion(@Nullable AdminObservacionDTO observacion) {
        if (observacion == null) {
            return null;
        }
        String autor = observacion.getNombreAdmin();
        String fecha = formatearFechaHoraObservacion(
                !TextUtils.isEmpty(observacion.getFechaActualizacion())
                        ? observacion.getFechaActualizacion()
                        : observacion.getFechaCreacion()
        );

        if (!TextUtils.isEmpty(autor) && !TextUtils.isEmpty(fecha)) {
            return "Actualizada por " + autor.trim() + " \u00B7 " + fecha;
        }
        if (!TextUtils.isEmpty(autor)) {
            return "Actualizada por " + autor.trim();
        }
        if (!TextUtils.isEmpty(fecha)) {
            return "\u00DAltima actualizaci\u00F3n \u00B7 " + fecha;
        }
        return null;
    }

    @Nullable
    private String formatearFechaHoraObservacion(@Nullable String fechaRaw) {
        if (TextUtils.isEmpty(fechaRaw)) {
            return null;
        }
        try {
            return VIEW_DATE_TIME_FORMATTER.format(LocalDateTime.parse(fechaRaw.trim(), API_DATE_TIME_FORMATTER));
        } catch (Exception ignored) {
            return fechaRaw;
        }
    }

    private void renderizarPodioPopularidad(@NonNull List<AdminRankingItemDTO> items) {
        if (items.isEmpty()) {
            containerPodioPopularidad.setVisibility(View.GONE);
            podioPrimero.card.setVisibility(View.GONE);
            rowPodioSecundario.setVisibility(View.GONE);
            return;
        }

        containerPodioPopularidad.setVisibility(View.VISIBLE);
        bindPodio(podioPrimero, items.get(0), 1, true);
        podioPrimero.card.setVisibility(View.VISIBLE);

        boolean haySegundo = items.size() > 1;
        boolean hayTercero = items.size() > 2;
        rowPodioSecundario.setVisibility(haySegundo || hayTercero ? View.VISIBLE : View.GONE);

        if (haySegundo) {
            bindPodio(podioSegundo, items.get(1), 2, false);
            podioSegundo.card.setVisibility(View.VISIBLE);
        } else {
            podioSegundo.card.setVisibility(View.GONE);
        }

        if (hayTercero) {
            bindPodio(podioTercero, items.get(2), 3, false);
            podioTercero.card.setVisibility(View.VISIBLE);
        } else {
            podioTercero.card.setVisibility(View.GONE);
        }
    }

    private void bindPodio(@NonNull PodioViews podio,
                           @NonNull AdminRankingItemDTO item,
                           int posicion,
                           boolean destacar) {
        podio.badge.setText(posicion + "\u00B0");
        podio.tag.setText(obtenerEtiquetaPopularidad(tipoPopularidadSeleccionado));
        podio.nombre.setVisibility(View.VISIBLE);
        podio.nombre.setText(obtenerTituloPodioRanking(item));
        podio.subtitulo.setText(obtenerSubtituloRanking(item));
        podio.subtitulo.setVisibility(View.VISIBLE);
        podio.perfilLayout.setVisibility(View.GONE);
        podio.total.setVisibility(View.GONE);
        aplicarMetricaCompactaPodio(podio.detalle, item.getTotal(), destacar);

        CardThemeHelper.applyMessageCard(podio.card, themeManager, destacar);
        CardThemeHelper.applySoftChip(podio.badge, themeManager);
        CardThemeHelper.applySoftChip(podio.tag, themeManager);
        CardThemeHelper.applyAvatarStroke(podio.imagenRect, themeManager);
        CardThemeHelper.applyAvatarStroke(podio.imagenCircle, themeManager);
        CardThemeHelper.applyAvatarStroke(podio.autorImagen, themeManager);
        podio.autorLayout.setBackground(null);
        ThemeApplier.applyTextPrimary(podio.nombre, themeManager);
        ThemeApplier.applyTextSecondary(podio.subtitulo, themeManager);
        ThemeApplier.applyTextSecondary(podio.detalle, themeManager);
        ThemeApplier.applyTextSecondary(podio.autorTexto, themeManager);
        ThemeApplier.applyTextPrimary(podio.perfilNombre, themeManager);
        ThemeApplier.applyTextSecondary(podio.perfilSubtitulo, themeManager);
        aplicarEstiloPodioSecundario(podio, destacar);
        configurarPodioPorTipo(podio, item);
        ajustarLayoutAutorPodio(podio, destacar);
        ajustarMetaSecundariaPodio(podio, destacar);
    }


    private void configurarPodioPorTipo(@NonNull PodioViews podio,
                                        @NonNull AdminRankingItemDTO item) {
        podio.imagenRect.setVisibility(View.GONE);
        podio.imagenCircle.setVisibility(View.GONE);
        podio.perfilLayout.setVisibility(View.GONE);
        podio.autorLayout.setVisibility(View.GONE);
        podio.nombre.setVisibility(View.VISIBLE);
        podio.subtitulo.setVisibility(View.VISIBLE);

        Glide.with(podio.imagenRect).clear(podio.imagenRect);
        Glide.with(podio.imagenCircle).clear(podio.imagenCircle);
        Glide.with(podio.autorImagen).clear(podio.autorImagen);

        if (tipoPopularidadSeleccionado == CategoriaTipo.SERVICIOS) {
            podio.nombre.setText(obtenerTituloServicioRanking(item));
            podio.subtitulo.setText(obtenerSubtituloServicioRanking(item));
            String autor = obtenerAutorVisible(item);
            if (!TextUtils.isEmpty(autor) || !TextUtils.isEmpty(item.getImagen())) {
                podio.autorLayout.setVisibility(View.VISIBLE);
                podio.autorTexto.setText(formatearPrestadorRanking(autor));
                cargarImagenRanking(podio.autorImagen, item.getImagen(), R.drawable.fotoperfilprueba, true);
            }
            return;
        }

        if (tipoPopularidadSeleccionado == CategoriaTipo.ARTISTAS) {
            podio.nombre.setVisibility(View.GONE);
            podio.subtitulo.setVisibility(View.GONE);
            podio.perfilLayout.setVisibility(View.VISIBLE);
            podio.imagenCircle.setVisibility(View.VISIBLE);
            podio.perfilNombre.setText(obtenerNombreRanking(item));
            podio.perfilSubtitulo.setText(obtenerSubtituloRanking(item));
            cargarImagenRanking(podio.imagenCircle, item.getImagen(), R.drawable.fotoperfilprueba, true);
            return;
        }

        podio.nombre.setText(obtenerNombreRanking(item));
        podio.subtitulo.setVisibility(View.GONE);
        podio.autorLayout.setVisibility(View.VISIBLE);
        podio.autorTexto.setText(obtenerAutorObraRanking(item));
        cargarImagenRanking(podio.autorImagen, item.getImagenAutor(), R.drawable.fotoperfilprueba, true);
        podio.imagenRect.setVisibility(View.VISIBLE);
        cargarImagenRanking(podio.imagenRect, item.getImagen(), R.drawable.imagencargaobras, false);
    }

    private List<AdminPuntoSerieDTO> normalizarPuntosSemana(@Nullable List<AdminPuntoSerieDTO> puntos,
                                                            @NonNull LocalDate inicioPeriodo) {
        return normalizarPuntosPeriodo(puntos, inicioPeriodo, 7);
    }

    private List<AdminPuntoSerieDTO> normalizarPuntosPeriodo(@Nullable List<AdminPuntoSerieDTO> puntos,
                                                             @NonNull LocalDate inicioPeriodo,
                                                             int totalDias) {
        List<AdminPuntoSerieDTO> resultado = new ArrayList<>();
        int dias = Math.max(1, totalDias);
        for (int i = 0; i < dias; i++) {
            LocalDate fecha = inicioPeriodo.plusDays(i);
            AdminPuntoSerieDTO punto = buscarPuntoPorFecha(puntos, fecha);
            if (punto == null) {
                punto = new AdminPuntoSerieDTO();
                punto.setFecha(API_DATE_FORMATTER.format(fecha));
                punto.setEtiqueta(formatearEtiquetaDia(fecha));
                punto.setValor(0L);
                punto.setMonto(0d);
            } else {
                if (punto.getFecha() == null || punto.getFecha().trim().isEmpty()) {
                    punto.setFecha(API_DATE_FORMATTER.format(fecha));
                }
                if (punto.getEtiqueta() == null || punto.getEtiqueta().trim().isEmpty()) {
                    punto.setEtiqueta(formatearEtiquetaDia(fecha));
                }
                if (punto.getMonto() == null) {
                    punto.setMonto(0d);
                }
            }
            resultado.add(punto);
        }
        return resultado;
    }

    private int resolverDiasComparadosCrecimiento(@NonNull AdminCrecimientoDTO crecimiento) {
        Integer diasComparados = crecimiento.getDiasComparados();
        if (diasComparados != null && diasComparados > 0) {
            return diasComparados;
        }
        int desdeSerie = crecimiento.getSerieSemanaActual().size();
        return desdeSerie > 0 ? desdeSerie : 7;
    }

    private int resolverUltimoIndiceRealCrecimiento(@NonNull LocalDate inicioSemana,
                                                    @NonNull LocalDate finSemana) {
        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(inicioSemana) || hoy.isAfter(finSemana)) {
            return -1;
        }
        int indice = (int) ChronoUnit.DAYS.between(inicioSemana, hoy);
        return Math.max(0, Math.min(6, indice));
    }

    @Nullable
    private AdminPuntoSerieDTO buscarPuntoPorFecha(@Nullable List<AdminPuntoSerieDTO> puntos,
                                                   @NonNull LocalDate fechaObjetivo) {
        if (puntos == null) {
            return null;
        }
        String fechaIso = API_DATE_FORMATTER.format(fechaObjetivo);
        for (AdminPuntoSerieDTO punto : puntos) {
            if (punto != null && fechaIso.equals(punto.getFecha())) {
                return punto;
            }
        }
        return null;
    }

    private void irSemanaAnterior() {
        semanaVentasSeleccionada = semanaVentasSeleccionada.minusWeeks(1);
        actualizarRangoSemanaVentas(semanaVentasSeleccionada, semanaVentasSeleccionada.plusDays(6));
        marcarCambioContextoObservacion();
        cargarVentasSemanales();
    }

    private void irSemanaSiguiente() {
        LocalDate inicioSemanaActual = obtenerInicioSemanaActual();
        if (!semanaVentasSeleccionada.isBefore(inicioSemanaActual)) {
            mostrarSnackbar("No se permiten semanas futuras.");
            actualizarEstadoControles(false);
            return;
        }
        semanaVentasSeleccionada = semanaVentasSeleccionada.plusWeeks(1);
        actualizarRangoSemanaVentas(semanaVentasSeleccionada, semanaVentasSeleccionada.plusDays(6));
        marcarCambioContextoObservacion();
        cargarVentasSemanales();
    }

    private void irSemanaAnteriorCrecimiento() {
        semanaCrecimientoSeleccionada = semanaCrecimientoSeleccionada.minusWeeks(1);
        actualizarRangoSemanaCrecimiento(
                semanaCrecimientoSeleccionada,
                semanaCrecimientoSeleccionada.plusDays(6)
        );
        marcarCambioContextoObservacion();
        cargarCrecimiento();
    }

    private void irSemanaSiguienteCrecimiento() {
        LocalDate inicioSemanaActual = obtenerInicioSemanaActual();
        if (!semanaCrecimientoSeleccionada.isBefore(inicioSemanaActual)) {
            mostrarSnackbar("No se permiten semanas futuras.");
            actualizarEstadoControles(false);
            return;
        }
        semanaCrecimientoSeleccionada = semanaCrecimientoSeleccionada.plusWeeks(1);
        actualizarRangoSemanaCrecimiento(
                semanaCrecimientoSeleccionada,
                semanaCrecimientoSeleccionada.plusDays(6)
        );
        marcarCambioContextoObservacion();
        cargarCrecimiento();
    }

    private void actualizarRangoSemanaVentas(@NonNull LocalDate inicio, @NonNull LocalDate fin) {
        tvRangoSemanaVentas.setText(VIEW_DATE_FORMATTER.format(inicio) + " - " + VIEW_DATE_FORMATTER.format(fin));
    }

    private void actualizarRangoSemanaCrecimiento(@NonNull LocalDate inicio, @NonNull LocalDate fin) {
        tvRangoSemanaCrecimiento.setText(
                VIEW_DATE_FORMATTER.format(inicio) + " - " + VIEW_DATE_FORMATTER.format(fin)
        );
    }

    private void actualizarVisibilidadTab() {
        boolean mostrarCategorias = tabActiva == TabActiva.CATEGORIAS;
        boolean mostrarVentas = tabActiva == TabActiva.VENTAS;
        boolean mostrarPopularidad = tabActiva == TabActiva.POPULARIDAD;
        boolean mostrarCrecimiento = tabActiva == TabActiva.CRECIMIENTO;
        cardCategorias.setVisibility(mostrarCategorias ? View.VISIBLE : View.GONE);
        cardVentas.setVisibility(mostrarVentas ? View.VISIBLE : View.GONE);
        cardPopularidad.setVisibility(mostrarPopularidad ? View.VISIBLE : View.GONE);
        cardCrecimiento.setVisibility(mostrarCrecimiento ? View.VISIBLE : View.GONE);
    }

    private void aplicarTemaVisual(@Nullable View root) {
        if (themeManager == null || root == null) {
            return;
        }

        ThemeModuleStyler.styleFragment(this, root);
        CardThemeHelper.applyFilterButton(btnRegresar, themeManager);
        CardThemeHelper.applyMessageCard(cardTabs, themeManager, false);
        CardThemeHelper.applyMessageCard(cardCategorias, themeManager, false);
        CardThemeHelper.applyMessageCard(cardVentas, themeManager, false);
        CardThemeHelper.applyMessageCard(cardCrecimiento, themeManager, false);
        CardThemeHelper.applyMessageCard(cardTotalVentas, themeManager, false);
        CardThemeHelper.applyMessageCard(cardTotalIngresos, themeManager, true);
        CardThemeHelper.applyMessageCard(cardSemanaActualCrecimiento, themeManager, false);
        CardThemeHelper.applyMessageCard(cardSemanaAnteriorCrecimiento, themeManager, false);
        CardThemeHelper.applyMessageCard(cardCambioCrecimiento, themeManager, false);
        CardThemeHelper.applyMessageCard(cardResumenTotalCategorias, themeManager, false);
        CardThemeHelper.applyMessageCard(cardResumenMayorCategorias, themeManager, false);
        CardThemeHelper.applyMessageCard(cardResumenConDatosCategorias, themeManager, false);
        CardThemeHelper.applyMessageCard(cardLeyendaCategorias, themeManager, false);
        CardThemeHelper.applyMessageCard(cardObservaciones, themeManager, false);
        CardThemeHelper.applyPrimaryBubbleButton(btnActualizar, themeManager);
        CardThemeHelper.applyFilterButton(btnActualizarCategoriasMini, themeManager);
        CardThemeHelper.applyFilterButton(btnActualizarPopularidadMini, themeManager);
        CardThemeHelper.applyFilterButton(btnActualizarVentasMini, themeManager);
        CardThemeHelper.applyFilterButton(btnActualizarCrecimientoMini, themeManager);
        CardThemeHelper.applySecondaryBubbleButton(btnSemanaAnterior, themeManager);
        CardThemeHelper.applySecondaryBubbleButton(btnSemanaSiguiente, themeManager);
        CardThemeHelper.applySecondaryBubbleButton(btnSemanaAnteriorCrecimiento, themeManager);
        CardThemeHelper.applySecondaryBubbleButton(btnSemanaSiguienteCrecimiento, themeManager);
        CardThemeHelper.applyPrimaryBubbleButton(btnAgregarObservacion, themeManager);
        CardThemeHelper.applyPrimaryBubbleButton(btnEditarObservacion, themeManager);
        CardThemeHelper.applySecondaryBubbleButton(btnEliminarObservacion, themeManager);
        CardThemeHelper.tintProgress(progressCategorias, themeManager);
        CardThemeHelper.tintProgress(progressPopularidad, themeManager);
        CardThemeHelper.tintProgress(progressVentas, themeManager);
        CardThemeHelper.tintProgress(progressCrecimiento, themeManager);
        CardThemeHelper.tintProgress(progressObservacion, themeManager);

        ThemeApplier.applyTextPrimary(tvTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvSubtitulo, themeManager);
        ThemeApplier.applyTextPrimary(tvTabsTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvTabsSubtitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvTabsPendientes, themeManager);

        ThemeApplier.applyTextPrimary(tvCategoriasTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvCategoriasSubtitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvEstadoCategorias, themeManager);
        ThemeApplier.applyTextSecondary(tvZeroCategorias, themeManager);
        ThemeApplier.applyTextPrimary(tvResumenTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvResumenTotalCategoriasLabel, themeManager);
        ThemeApplier.applyTextPrimary(tvResumenTotalCategoriasValue, themeManager);
        ThemeApplier.applyTextSecondary(tvResumenMayorCategoriasLabel, themeManager);
        ThemeApplier.applyTextPrimary(tvResumenMayorCategoriasValue, themeManager);
        ThemeApplier.applyTextSecondary(tvResumenConDatosCategoriasLabel, themeManager);
        ThemeApplier.applyTextPrimary(tvResumenConDatosCategoriasValue, themeManager);
        ThemeApplier.applyTextPrimary(tvLeyendaCategoriasTitulo, themeManager);

        ThemeApplier.applyTextPrimary(tvPopularidadTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvPopularidadSubtitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvEstadoPopularidad, themeManager);
        ThemeApplier.applyTextSecondary(tvMensajePopularidad, themeManager);
        ThemeApplier.applyTextPrimary(tvTopRankingPopularidad, themeManager);

        ThemeApplier.applyTextPrimary(tvVentasTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvVentasSubtitulo, themeManager);
        ThemeApplier.applyTextPrimary(tvRangoSemanaVentas, themeManager);
        ThemeApplier.applyTextSecondary(tvEstadoVentas, themeManager);
        ThemeApplier.applyTextSecondary(tvTotalVentasLabel, themeManager);
        ThemeApplier.applyTextPrimary(tvTotalVentasValue, themeManager);
        ThemeApplier.applyTextSecondary(tvTotalIngresosLabel, themeManager);
        ThemeApplier.applyTextPrimary(tvTotalIngresosValue, themeManager);
        ThemeApplier.applyTextSecondary(tvMensajeVentas, themeManager);

        ThemeApplier.applyTextPrimary(tvCrecimientoTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvCrecimientoSubtitulo, themeManager);
        ThemeApplier.applyTextPrimary(tvRangoSemanaCrecimiento, themeManager);
        ThemeApplier.applyTextSecondary(tvEstadoCrecimiento, themeManager);
        ThemeApplier.applyTextSecondary(tvSemanaActualCrecimientoLabel, themeManager);
        ThemeApplier.applyTextPrimary(tvSemanaActualCrecimientoValue, themeManager);
        ThemeApplier.applyTextSecondary(tvSemanaAnteriorCrecimientoLabel, themeManager);
        ThemeApplier.applyTextPrimary(tvSemanaAnteriorCrecimientoValue, themeManager);
        ThemeApplier.applyTextSecondary(tvCambioCrecimientoLabel, themeManager);
        ThemeApplier.applyTextPrimary(tvCambioCrecimientoValue, themeManager);
        ThemeApplier.applyTextSecondary(tvMensajeCrecimiento, themeManager);
        ThemeApplier.applyTextPrimary(tvObservacionesTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvObservacionContexto, themeManager);
        ThemeApplier.applyTextSecondary(tvObservacionEstado, themeManager);
        ThemeApplier.applyTextPrimary(tvObservacionTexto, themeManager);
        ThemeApplier.applyTextSecondary(tvObservacionMeta, themeManager);

        aplicarTemaTabs();
        aplicarTemaSelectorCategorias();
        aplicarTemaSelectorPopularidad();
        aplicarTemaSelectorCrecimiento();
        AdminChartThemeHelper.prepareCategoriasDonutChart(viewGraficaCategorias, themeManager);
        AdminChartThemeHelper.prepareVentasChart(viewGraficaVentas, themeManager);
        AdminChartThemeHelper.prepareCrecimientoChart(viewGraficaCrecimiento, themeManager);
        aplicarTemaPodio();
        if (!categoriasActuales.isEmpty()) {
            renderizarDistribucionCategorias(new ArrayList<>(categoriasActuales));
            actualizarResumenCategorias(categoriasActuales);
        }
        if (!ventasActuales.isEmpty()) {
            AdminChartThemeHelper.renderVentasChart(viewGraficaVentas, ventasActuales, themeManager);
        }
        if (ultimoCrecimiento != null && !crecimientoActual.isEmpty()) {
            AdminChartThemeHelper.renderCrecimientoChart(
                    viewGraficaCrecimiento,
                    new ArrayList<>(crecimientoActual),
                    new ArrayList<>(crecimientoAnterior),
                    ultimoIndiceRealCrecimiento,
                    themeManager
            );
        }
        if (!rankingActual.isEmpty()) {
            rankingAdapter.actualizar(new ArrayList<>(rankingActual), tipoPopularidadSeleccionado.backendValue);
            renderizarPodioPopularidad(rankingActual);
        }
        renderizarEstadoObservacionActual();
        actualizarEstadoControles(false);
    }

    private void aplicarTemaTabs() {
        if (tabLayoutPrincipal == null || themeManager == null) {
            return;
        }
        if (layoutTabsPrincipales != null) {
            layoutTabsPrincipales.setBackground(crearContenedorTabsPrincipales());
        }
        tabLayoutPrincipal.setSelectedTabIndicatorColor(android.graphics.Color.TRANSPARENT);
        tabLayoutPrincipal.setTabRippleColor(ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.ACCENT_PRIMARY), 32)
        ));
        for (int i = 0; i < tabLayoutPrincipal.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayoutPrincipal.getTabAt(i);
            if (tab != null && !(tab.getCustomView() instanceof TextView)) {
                CharSequence texto = tab.getText();
                tab.setCustomView(crearVistaTabPrincipal(texto != null ? texto.toString() : ""));
            }
        }
        seleccionarTabPrincipal(tabActiva, false);
    }

    @NonNull
    private GradientDrawable crearContenedorTabsPrincipales() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dp(18));
        drawable.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_BG), 190));
        drawable.setStroke(Math.max(1, dp(1)), ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_STROKE), 150));
        return drawable;
    }

    private int resolverColorTextoSobre(int backgroundColor) {
        int preferred = themeManager.color(ThemeKeys.BUTTON_TEXT_DARK);
        int alternate = themeManager.color(ThemeKeys.BUTTON_TEXT_LIGHT);
        int normalizedBackground = ColorUtils.setAlphaComponent(backgroundColor, 255);
        double contrastePreferred = ColorUtils.calculateContrast(preferred, normalizedBackground);
        double contrasteAlternate = ColorUtils.calculateContrast(alternate, normalizedBackground);
        return contrastePreferred >= contrasteAlternate ? preferred : alternate;
    }

    private void aplicarBotonPendiente(@Nullable Button button) {
        if (button == null) {
            return;
        }
        CardThemeHelper.applySecondaryBubbleButton(button, themeManager);
        button.setAlpha(0.58f);
    }

    private void aplicarTemaSelectorCategorias() {
        aplicarBotonTipo(btnTipoObras, tipoCategoriaSeleccionado == CategoriaTipo.OBRAS);
        aplicarBotonTipo(btnTipoServicios, tipoCategoriaSeleccionado == CategoriaTipo.SERVICIOS);
        aplicarBotonTipo(btnTipoArtistas, tipoCategoriaSeleccionado == CategoriaTipo.ARTISTAS);
    }

    private void aplicarTemaSelectorPopularidad() {
        aplicarBotonTipo(btnTipoObrasPopularidad, tipoPopularidadSeleccionado == CategoriaTipo.OBRAS);
        aplicarBotonTipo(btnTipoServiciosPopularidad, tipoPopularidadSeleccionado == CategoriaTipo.SERVICIOS);
        aplicarBotonTipo(btnTipoArtistasPopularidad, tipoPopularidadSeleccionado == CategoriaTipo.ARTISTAS);
    }

    private void aplicarTemaSelectorCrecimiento() {
        aplicarBotonTipo(btnTipoObrasCrecimiento, tipoCrecimientoSeleccionado == CategoriaTipo.OBRAS);
        aplicarBotonTipo(btnTipoServiciosCrecimiento, tipoCrecimientoSeleccionado == CategoriaTipo.SERVICIOS);
        aplicarBotonTipo(btnTipoArtistasCrecimiento, tipoCrecimientoSeleccionado == CategoriaTipo.ARTISTAS);
    }

    private void actualizarTextosCategoria() {
        switch (tipoCategoriaSeleccionado) {
            case OBRAS:
                tvCategoriasTitulo.setText("Obras por categor\u00EDa");
                tvCategoriasSubtitulo.setText("Distribuci\u00F3n de obras por categor\u00EDa art\u00EDstica.");
                tvResumenTotalCategoriasLabel.setText("Total de obras");
                tvResumenConDatosCategoriasLabel.setText("Categor\u00EDas activas");
                tvLeyendaCategoriasTitulo.setText("Categor\u00EDas con presencia");
                tvResumenTitulo.setText("Detalle de obras por categor\u00EDa");
                break;
            case SERVICIOS:
                tvCategoriasTitulo.setText("Servicios por especialidad");
                tvCategoriasSubtitulo.setText("Distribuci\u00F3n de servicios por profesi\u00F3n o especialidad.");
                tvResumenTotalCategoriasLabel.setText("Total de servicios");
                tvResumenConDatosCategoriasLabel.setText("Especialidades activas");
                tvLeyendaCategoriasTitulo.setText("Especialidades con presencia");
                tvResumenTitulo.setText("Detalle de servicios por especialidad");
                break;
            case ARTISTAS:
                tvCategoriasTitulo.setText("Perfiles por profesi\u00F3n");
                tvCategoriasSubtitulo.setText("Distribuci\u00F3n de perfiles por profesi\u00F3n art\u00EDstica.");
                tvResumenTotalCategoriasLabel.setText("Total de perfiles");
                tvResumenConDatosCategoriasLabel.setText("Profesiones activas");
                tvLeyendaCategoriasTitulo.setText("Profesiones con presencia");
                tvResumenTitulo.setText("Detalle de perfiles por profesi\u00F3n");
                break;
        }
    }

    private void actualizarTextosCrecimiento() {
        tvSemanaActualCrecimientoLabel.setText("Actual");
        tvSemanaAnteriorCrecimientoLabel.setText("Anterior");
        tvCambioCrecimientoLabel.setText("Cambio");

        switch (tipoCrecimientoSeleccionado) {
            case SERVICIOS:
                tvCrecimientoTitulo.setText("Crecimiento de servicios");
                tvCrecimientoSubtitulo.setText("Evoluci\u00F3n semanal de servicios publicados.");
                break;
            case ARTISTAS:
                tvCrecimientoTitulo.setText("Crecimiento de perfiles");
                tvCrecimientoSubtitulo.setText("Evoluci\u00F3n semanal de perfiles registrados.");
                break;
            case OBRAS:
            default:
                tvCrecimientoTitulo.setText("Crecimiento de obras");
                tvCrecimientoSubtitulo.setText("Evoluci\u00F3n semanal de obras publicadas.");
                break;
        }
    }

    @NonNull
    private String formatearResumenTotalCategorias(long total) {
        String sustantivo = obtenerSustantivoCategoria(total);
        return total + " " + sustantivo;
    }

    @NonNull
    private String formatearTotalCrecimiento(long total) {
        return total + " " + obtenerSustantivoVisibleCrecimiento(total);
    }

    @NonNull
    private String obtenerSustantivoCategoria(long total) {
        switch (tipoCategoriaSeleccionado) {
            case SERVICIOS:
                return total == 1L ? "servicio" : "servicios";
            case ARTISTAS:
                return total == 1L ? "perfil" : "perfiles";
            case OBRAS:
            default:
                return total == 1L ? "obra" : "obras";
        }
    }

    @NonNull
    private String obtenerSustantivoCrecimiento(long total) {
        switch (tipoCrecimientoSeleccionado) {
            case SERVICIOS:
                return total == 1L ? "servicio" : "servicios";
            case ARTISTAS:
                return total == 1L ? "perfil" : "perfiles";
            case OBRAS:
            default:
                return total == 1L ? "obra" : "obras";
        }
    }

    @NonNull
    private String obtenerSustantivoVisibleCrecimiento(long total) {
        if (tipoCrecimientoSeleccionado == CategoriaTipo.SERVICIOS) {
            return "serv.";
        }
        return obtenerSustantivoCrecimiento(total);
    }

    @NonNull
    private String normalizarCategoria(@Nullable String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return "Sin categor\u00EDa";
        }
        return categoria.trim();
    }

    @NonNull
    private String formatearMetaLeyenda(@NonNull AdminChartThemeHelper.CategoriaDonutLegendItem item) {
        long porcentaje = Math.round(item.getPorcentaje());
        return porcentaje + "% (" + item.getTotal() + ")";
    }

    @NonNull
    private String formatearCentroDona(@NonNull AdminChartThemeHelper.CategoriaDonutModel model) {
        long total = Math.round(model.getTotalGeneral());
        switch (tipoCategoriaSeleccionado) {
            case SERVICIOS:
                return total + "\nservicios";
            case ARTISTAS:
                return total + "\nperfiles";
            case OBRAS:
            default:
                return total + "\nobras";
        }
    }

    @NonNull
    private String formatearCambioCrecimiento(@NonNull AdminCrecimientoDTO crecimiento) {
        long totalAnterior = crecimiento.getTotalSemanaAnterior();
        if (totalAnterior <= 0L) {
            return "N/A";
        }
        Double porcentajeCambio = crecimiento.getPorcentajeCambio();
        if (porcentajeCambio == null) {
            return "N/A";
        }
        double valor = porcentajeCambio;
        String prefijo = valor > 0d ? "+" : "";
        return prefijo + String.format(LOCALE_ES_MX, "%.1f%%", valor);
    }

    private int dp(int value) {
        return Math.round(value * requireContext().getResources().getDisplayMetrics().density);
    }

    private void actualizarTextosPopularidad() {
        switch (tipoPopularidadSeleccionado) {
            case OBRAS:
                tvPopularidadTitulo.setText("Obras m\u00E1s populares");
                tvPopularidadSubtitulo.setText("Ranking basado en favoritos recibidos.");
                tvTopRankingPopularidad.setText("Top 5 de obras");
                break;
            case SERVICIOS:
                tvPopularidadTitulo.setText("Servicios m\u00E1s populares");
                tvPopularidadSubtitulo.setText("Ranking basado en favoritos recibidos.");
                tvTopRankingPopularidad.setText("Top 5 de servicios");
                break;
            case ARTISTAS:
                tvPopularidadTitulo.setText("Perfiles m\u00E1s populares");
                tvPopularidadSubtitulo.setText("Ranking basado en favoritos del perfil y su contenido.");
                tvTopRankingPopularidad.setText("Top 5 de perfiles");
                break;
        }
    }

    private void actualizarEstadoControles(boolean loading) {
        boolean habilitadoBase = puedeConsultarEstadisticas && !loading;
        btnActualizar.setEnabled(habilitadoBase);
        btnActualizar.setAlpha(habilitadoBase ? 1f : 0.72f);
        actualizarEstadoVista(btnActualizarCategoriasMini, habilitadoBase && tabActiva == TabActiva.CATEGORIAS);
        actualizarEstadoVista(btnActualizarVentasMini, habilitadoBase && tabActiva == TabActiva.VENTAS);
        actualizarEstadoVista(btnActualizarPopularidadMini, habilitadoBase && tabActiva == TabActiva.POPULARIDAD);
        actualizarEstadoVista(btnActualizarCrecimientoMini, habilitadoBase && tabActiva == TabActiva.CRECIMIENTO);

        boolean controlesCategoria = habilitadoBase && tabActiva == TabActiva.CATEGORIAS;
        actualizarEstadoBoton(btnTipoObras, controlesCategoria);
        actualizarEstadoBoton(btnTipoServicios, controlesCategoria);
        actualizarEstadoBoton(btnTipoArtistas, controlesCategoria);

        boolean controlesPopularidad = habilitadoBase && tabActiva == TabActiva.POPULARIDAD;
        actualizarEstadoBoton(btnTipoObrasPopularidad, controlesPopularidad);
        actualizarEstadoBoton(btnTipoServiciosPopularidad, controlesPopularidad);
        actualizarEstadoBoton(btnTipoArtistasPopularidad, controlesPopularidad);

        boolean controlesVentas = habilitadoBase && tabActiva == TabActiva.VENTAS;
        actualizarEstadoBoton(btnSemanaAnterior, controlesVentas);
        boolean puedeIrSiguiente = controlesVentas && semanaVentasSeleccionada.isBefore(obtenerInicioSemanaActual());
        actualizarEstadoBoton(btnSemanaSiguiente, puedeIrSiguiente);

        boolean controlesCrecimiento = habilitadoBase && tabActiva == TabActiva.CRECIMIENTO;
        actualizarEstadoBoton(btnTipoObrasCrecimiento, controlesCrecimiento);
        actualizarEstadoBoton(btnTipoServiciosCrecimiento, controlesCrecimiento);
        actualizarEstadoBoton(btnTipoArtistasCrecimiento, controlesCrecimiento);
        actualizarEstadoBoton(btnSemanaAnteriorCrecimiento, controlesCrecimiento);
        boolean puedeIrSiguienteCrecimiento =
                controlesCrecimiento && semanaCrecimientoSeleccionada.isBefore(obtenerInicioSemanaActual());
        actualizarEstadoBoton(btnSemanaSiguienteCrecimiento, puedeIrSiguienteCrecimiento);
    }

    private void actualizarEstadoBoton(@Nullable Button button, boolean habilitado) {
        if (button == null) {
            return;
        }
        button.setEnabled(habilitado);
        button.setAlpha(habilitado ? 1f : 0.72f);
    }

    private void actualizarEstadoVista(@Nullable View view, boolean habilitado) {
        if (view == null) {
            return;
        }
        view.setEnabled(habilitado);
        view.setAlpha(habilitado ? 1f : 0.62f);
    }

    private void aplicarBotonTipo(@Nullable Button button, boolean seleccionado) {
        if (button == null) {
            return;
        }
        if (seleccionado) {
            CardThemeHelper.applyPrimaryBubbleButton(button, themeManager);
            button.setAlpha(1f);
        } else {
            CardThemeHelper.applySecondaryBubbleButton(button, themeManager);
            button.setAlpha(0.88f);
        }
    }

    private void aplicarTemaPodio() {
        aplicarTemaPodioCard(podioPrimero, true);
        aplicarTemaPodioCard(podioSegundo, false);
        aplicarTemaPodioCard(podioTercero, false);
    }

    private void aplicarTemaPodioCard(@NonNull PodioViews podio, boolean destacado) {
        CardThemeHelper.applyMessageCard(podio.card, themeManager, destacado);
        CardThemeHelper.applySoftChip(podio.badge, themeManager);
        CardThemeHelper.applySoftChip(podio.tag, themeManager);
        CardThemeHelper.applySoftChip(podio.total, themeManager);
        CardThemeHelper.applyAvatarStroke(podio.imagenRect, themeManager);
        CardThemeHelper.applyAvatarStroke(podio.imagenCircle, themeManager);
        CardThemeHelper.applyAvatarStroke(podio.autorImagen, themeManager);
        podio.autorLayout.setBackground(null);
        ThemeApplier.applyTextPrimary(podio.nombre, themeManager);
        ThemeApplier.applyTextSecondary(podio.subtitulo, themeManager);
        ThemeApplier.applyTextSecondary(podio.detalle, themeManager);
        ThemeApplier.applyTextSecondary(podio.autorTexto, themeManager);
        ThemeApplier.applyTextPrimary(podio.perfilNombre, themeManager);
        ThemeApplier.applyTextSecondary(podio.perfilSubtitulo, themeManager);
    }

    @NonNull
    private String obtenerEtiquetaPopularidad(@NonNull CategoriaTipo tipo) {
        switch (tipo) {
            case SERVICIOS:
                return "Servicio";
            case ARTISTAS:
                return "Perfil";
            case OBRAS:
            default:
                return "Obra";
        }
    }

    @NonNull
    private String obtenerNombreRanking(@Nullable AdminRankingItemDTO item) {
        String nombre = item != null ? item.getNombre() : null;
        return TextUtils.isEmpty(nombre) ? "Sin nombre" : nombre.trim();
    }

    @NonNull
    private String obtenerSubtituloRanking(@Nullable AdminRankingItemDTO item) {
        String valor;
        if (tipoPopularidadSeleccionado == CategoriaTipo.ARTISTAS) {
            valor = item != null ? item.getSubtitulo() : null;
        } else if (tipoPopularidadSeleccionado == CategoriaTipo.SERVICIOS) {
            valor = obtenerSubtituloServicioRanking(item);
        } else {
            valor = item != null ? item.getAutor() : null;
        }

        if (TextUtils.isEmpty(valor) && item != null) {
            valor = item.getSubtitulo();
        }
        if (TextUtils.isEmpty(valor) && item != null) {
            valor = item.getDescripcionSecundaria();
        }
        if (TextUtils.isEmpty(valor)) {
            return "Sin informaci\u00F3n adicional";
        }
        return valor.trim();
    }

    @NonNull
    private String obtenerSubtituloServicioRanking(@Nullable AdminRankingItemDTO item) {
        String contactoFormateado = construirContactoServicioRanking(item);
        if (!TextUtils.isEmpty(contactoFormateado)) {
            return contactoFormateado;
        }
        String autor = item != null ? item.getAutor() : null;
        if (!TextUtils.isEmpty(autor)) {
            return autor.trim();
        }
        String subtitulo = item != null ? item.getSubtitulo() : null;
        if (!TextUtils.isEmpty(subtitulo)) {
            return subtitulo.trim();
        }
        return "Servicio sin contacto visible";
    }

    @Nullable
    private String construirContactoServicioRanking(@Nullable AdminRankingItemDTO item) {
        if (item == null) {
            return null;
        }
        String contacto = limpiarTextoRanking(item.getContacto());
        if (TextUtils.isEmpty(contacto)) {
            return null;
        }
        String tipoContacto = limpiarTextoRanking(item.getTipoContacto());
        if (TextUtils.isEmpty(tipoContacto)) {
            return "Contacto: " + contacto;
        }
        return "Contacto: " + normalizarTipoContactoRanking(tipoContacto) + " \u00B7 " + contacto;
    }

    @NonNull
    private String obtenerAutorVisible(@Nullable AdminRankingItemDTO item) {
        String autor = item != null ? item.getAutor() : null;
        return TextUtils.isEmpty(autor) ? "" : autor.trim();
    }

    @NonNull
    private String obtenerAutorObraRanking(@Nullable AdminRankingItemDTO item) {
        String autor = obtenerAutorVisible(item);
        if (TextUtils.isEmpty(autor)) {
            return "Autor no disponible";
        }
        return autor;
    }

    @NonNull
    private String obtenerTituloPodioRanking(@Nullable AdminRankingItemDTO item) {
        return obtenerNombreRanking(item);
    }

    @NonNull
    private String obtenerTituloServicioRanking(@Nullable AdminRankingItemDTO item) {
        return obtenerNombreRanking(item);
    }

    private void aplicarEstiloPodioSecundario(@NonNull PodioViews podio, boolean destacar) {
        View contenido = podio.card.getChildAt(0);
        if (contenido == null || destacar) {
            return;
        }

        int minHeightDp;
        int paddingDp;
        int titleTopMarginDp;
        int subtitleTopMarginDp;
        int contentTopMarginDp;
        int metaTopMarginDp;

        switch (tipoPopularidadSeleccionado) {
            case SERVICIOS:
                minHeightDp = 200;
                paddingDp = 9;
                titleTopMarginDp = 7;
                subtitleTopMarginDp = 3;
                contentTopMarginDp = 4;
                metaTopMarginDp = 4;
                break;
            case ARTISTAS:
                minHeightDp = 226;
                paddingDp = 12;
                titleTopMarginDp = 8;
                subtitleTopMarginDp = 4;
                contentTopMarginDp = 6;
                metaTopMarginDp = 8;
                break;
            case OBRAS:
            default:
                minHeightDp = 320;
                paddingDp = 14;
                titleTopMarginDp = 10;
                subtitleTopMarginDp = 5;
                contentTopMarginDp = 10;
                metaTopMarginDp = 8;
                break;
        }

        contenido.setMinimumHeight(dp(minHeightDp));
        contenido.setPadding(dp(paddingDp), dp(paddingDp), dp(paddingDp), dp(paddingDp));
        podio.autorTexto.setMaxLines((tipoPopularidadSeleccionado == CategoriaTipo.OBRAS
                || tipoPopularidadSeleccionado == CategoriaTipo.SERVICIOS) ? 2 : 1);

        ajustarMargenSuperior(podio.nombre, titleTopMarginDp);
        ajustarMargenSuperior(podio.subtitulo, subtitleTopMarginDp);
        ajustarMargenSuperior(podio.imagenRect, contentTopMarginDp);
        ajustarMargenSuperior(podio.perfilLayout, contentTopMarginDp);
        ajustarMargenSuperior(podio.autorLayout, contentTopMarginDp);
        ajustarMargenSuperior((View) podio.detalle.getParent(), metaTopMarginDp);
        int margenDetalleDp = 6;
        float detalleTextSizeSp = 12f;
        if (tipoPopularidadSeleccionado == CategoriaTipo.SERVICIOS) {
            margenDetalleDp = 4;
            detalleTextSizeSp = 11f;
        } else if (tipoPopularidadSeleccionado == CategoriaTipo.OBRAS) {
            margenDetalleDp = 4;
            detalleTextSizeSp = 11.5f;
        }
        ajustarMargenInicio(podio.detalle, margenDetalleDp);
        podio.detalle.setTextSize(detalleTextSizeSp);
    }

    private void ajustarLayoutAutorPodio(@NonNull PodioViews podio, boolean destacar) {
        if (tipoPopularidadSeleccionado == CategoriaTipo.ARTISTAS) {
            return;
        }

        LinearLayout.LayoutParams imageParams =
                (LinearLayout.LayoutParams) podio.autorImagen.getLayoutParams();
        LinearLayout.LayoutParams textParams =
                (LinearLayout.LayoutParams) podio.autorTexto.getLayoutParams();

        if (destacar) {
            aplicarAutorHorizontalPodio(podio, imageParams, textParams, 34, 0, 4, 8, 12f);
            return;
        }

        if (tipoPopularidadSeleccionado == CategoriaTipo.SERVICIOS) {
            aplicarAutorVerticalPodio(podio, imageParams, textParams, 28, 0, 2, 10.5f);
            return;
        }

        aplicarAutorVerticalPodio(podio, imageParams, textParams, 28, 0, 2, 11f);
    }

    private void ajustarMetaSecundariaPodio(@NonNull PodioViews podio, boolean destacar) {
        LinearLayout metaLayout = (LinearLayout) podio.detalle.getParent();
        LinearLayout autorLayout = (LinearLayout) podio.autorLayout;
        LinearLayout.LayoutParams autorLayoutParams =
                (LinearLayout.LayoutParams) autorLayout.getLayoutParams();
        LinearLayout.LayoutParams detalleLayoutParams =
                (LinearLayout.LayoutParams) podio.detalle.getLayoutParams();

        if (destacar || tipoPopularidadSeleccionado == CategoriaTipo.ARTISTAS) {
            metaLayout.setOrientation(LinearLayout.HORIZONTAL);
            metaLayout.setGravity(Gravity.CENTER_VERTICAL);
            autorLayoutParams.width = 0;
            autorLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            autorLayoutParams.weight = 1f;
            autorLayoutParams.setMarginStart(0);
            autorLayoutParams.topMargin = 0;
            autorLayout.setLayoutParams(autorLayoutParams);
            detalleLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            detalleLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            detalleLayoutParams.weight = 0f;
            detalleLayoutParams.setMarginStart(dp(destacar ? 6 : 4));
            detalleLayoutParams.topMargin = 0;
            podio.detalle.setLayoutParams(detalleLayoutParams);
            podio.detalle.setGravity(Gravity.CENTER_VERTICAL);
            podio.detalle.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            return;
        }

        metaLayout.setOrientation(LinearLayout.VERTICAL);
        metaLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        autorLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        autorLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        autorLayoutParams.weight = 0f;
        autorLayoutParams.setMarginStart(0);
        autorLayoutParams.topMargin = 0;
        autorLayout.setLayoutParams(autorLayoutParams);

        detalleLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        detalleLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        detalleLayoutParams.weight = 0f;
        detalleLayoutParams.setMarginStart(0);
        detalleLayoutParams.topMargin = dp(tipoPopularidadSeleccionado == CategoriaTipo.SERVICIOS ? 4 : 6);
        podio.detalle.setLayoutParams(detalleLayoutParams);
        podio.detalle.setGravity(Gravity.CENTER);
        podio.detalle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }

    private void aplicarAutorHorizontalPodio(@NonNull PodioViews podio,
                                             @NonNull LinearLayout.LayoutParams imageParams,
                                             @NonNull LinearLayout.LayoutParams textParams,
                                             int imageSizeDp,
                                             int horizontalPaddingDp,
                                             int verticalPaddingDp,
                                             int textStartMarginDp,
                                             float textSizeSp) {
        LinearLayout autorLayout = (LinearLayout) podio.autorLayout;
        autorLayout.setOrientation(LinearLayout.HORIZONTAL);
        autorLayout.setGravity(Gravity.CENTER_VERTICAL);
        autorLayout.setPadding(
                dp(horizontalPaddingDp),
                dp(verticalPaddingDp),
                dp(horizontalPaddingDp),
                dp(verticalPaddingDp)
        );

        imageParams.width = dp(imageSizeDp);
        imageParams.height = dp(imageSizeDp);
        imageParams.setMarginStart(0);
        imageParams.topMargin = 0;
        podio.autorImagen.setLayoutParams(imageParams);

        textParams.width = 0;
        textParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        textParams.weight = 1f;
        textParams.setMarginStart(dp(textStartMarginDp));
        textParams.topMargin = 0;
        podio.autorTexto.setLayoutParams(textParams);
        podio.autorTexto.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        podio.autorTexto.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        podio.autorTexto.setMaxLines(2);
        podio.autorTexto.setTextSize(textSizeSp);
    }

    private void aplicarAutorVerticalPodio(@NonNull PodioViews podio,
                                           @NonNull LinearLayout.LayoutParams imageParams,
                                           @NonNull LinearLayout.LayoutParams textParams,
                                           int imageSizeDp,
                                           int horizontalPaddingDp,
                                           int verticalPaddingDp,
                                           float textSizeSp) {
        LinearLayout autorLayout = (LinearLayout) podio.autorLayout;
        autorLayout.setOrientation(LinearLayout.VERTICAL);
        autorLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        autorLayout.setPadding(
                dp(horizontalPaddingDp),
                dp(verticalPaddingDp),
                dp(horizontalPaddingDp),
                dp(verticalPaddingDp)
        );

        imageParams.width = dp(imageSizeDp);
        imageParams.height = dp(imageSizeDp);
        imageParams.setMarginStart(0);
        imageParams.topMargin = 0;
        podio.autorImagen.setLayoutParams(imageParams);

        textParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        textParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        textParams.weight = 0f;
        textParams.setMarginStart(0);
        textParams.topMargin = dp(4);
        podio.autorTexto.setLayoutParams(textParams);
        podio.autorTexto.setGravity(Gravity.CENTER);
        podio.autorTexto.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        podio.autorTexto.setMaxLines(2);
        podio.autorTexto.setTextSize(textSizeSp);
    }

    private void ajustarMargenSuperior(@Nullable View view, int marginTopDp) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
        int top = dp(marginTopDp);
        if (marginLayoutParams.topMargin != top) {
            marginLayoutParams.topMargin = top;
            view.setLayoutParams(marginLayoutParams);
        }
    }

    private void ajustarMargenInicio(@Nullable View view, int marginStartDp) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
        int start = dp(marginStartDp);
        if (marginLayoutParams.getMarginStart() != start) {
            marginLayoutParams.setMarginStart(start);
            view.setLayoutParams(marginLayoutParams);
        }
    }

    @NonNull
    private String formatearPrestadorRanking(@Nullable String autor) {
        if (TextUtils.isEmpty(autor)) {
            return "Autor no disponible";
        }
        return autor.trim();
    }

    @NonNull
    private String construirDetalleRanking(long total) {
        long valor = Math.max(0L, total);
        if (tipoPopularidadSeleccionado == CategoriaTipo.ARTISTAS) {
            return valor + " interacciones de popularidad";
        }
        return String.valueOf(valor);
    }

    private void aplicarMetricaCompactaPodio(@NonNull TextView textView, long total, boolean destacar) {
        textView.setText(construirDetalleRanking(total));
        if (tipoPopularidadSeleccionado == CategoriaTipo.ARTISTAS) {
            textView.setCompoundDrawablesRelative(null, null, null, null);
            textView.setCompoundDrawablePadding(0);
            return;
        }
        Drawable drawable = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_like_filled);
        if (drawable == null || themeManager == null) {
            textView.setCompoundDrawablesRelative(null, null, null, null);
            textView.setCompoundDrawablePadding(0);
            return;
        }
        Drawable tinted = DrawableCompat.wrap(drawable.mutate());
        int color = destacar
                ? themeManager.color(ThemeKeys.ACCENT_PRIMARY)
                : themeManager.color(ThemeKeys.TEXT_SECONDARY);
        DrawableCompat.setTint(tinted, color);
        int size = dp(destacar ? 18 : 16);
        tinted.setBounds(0, 0, size, size);
        textView.setCompoundDrawablesRelative(tinted, null, null, null);
        textView.setCompoundDrawablePadding(dp(6));
    }

    @NonNull
    private String obtenerMensajePopularidad(@NonNull AdminRankingResponseDTO response) {
        String mensaje = response.getMensaje();
        if (!TextUtils.isEmpty(mensaje)) {
            return mensaje;
        }
        if (rankingActual.isEmpty()) {
            return "No hay datos de popularidad disponibles para este criterio.";
        }
        return "Se actualizaron los elementos m\u00E1s populares del ranking.";
    }

    private void cargarImagenRanking(@NonNull ShapeableImageView imageView,
                                     @Nullable String url,
                                     int placeholder,
                                     boolean circular) {
        if (TextUtils.isEmpty(url)) {
            Glide.with(imageView).clear(imageView);
            imageView.setImageResource(placeholder);
            return;
        }

        if (circular) {
            Glide.with(imageView)
                    .load(url.trim())
                    .placeholder(placeholder)
                    .error(placeholder)
                    .thumbnail(0.25f)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .circleCrop()
                    .into(imageView);
            return;
        }

        Glide.with(imageView)
                .load(url.trim())
                .placeholder(placeholder)
                .error(placeholder)
                .thumbnail(0.25f)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .into(imageView);
    }

    @Nullable
    private String limpiarTextoRanking(@Nullable String valor) {
        if (TextUtils.isEmpty(valor)) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    @NonNull
    private String normalizarTipoContactoRanking(@NonNull String tipo) {
        String valor = tipo.trim().toUpperCase(LOCALE_ES_MX);
        switch (valor) {
            case "WHATSAPP":
                return "WhatsApp";
            case "INSTAGRAM":
                return "Instagram";
            case "EMAIL":
            case "CORREO":
                return "Correo";
            case "TELEFONO":
            case "TEL":
            case "PHONE":
                return "Tel\u00E9fono";
            default:
                return tipo.trim();
        }
    }

    private void cancelarLlamadasActivas() {
        if (categoriasCall != null) {
            categoriasCall.cancel();
            categoriasCall = null;
        }
        if (ventasCall != null) {
            ventasCall.cancel();
            ventasCall = null;
        }
        if (rankingCall != null) {
            rankingCall.cancel();
            rankingCall = null;
        }
        if (crecimientoCall != null) {
            crecimientoCall.cancel();
            crecimientoCall = null;
        }
        if (observacionesCall != null) {
            observacionesCall.cancel();
            observacionesCall = null;
        }
        if (observacionMutationCall != null) {
            observacionMutationCall.cancel();
            observacionMutationCall = null;
        }
        if (observacionDeleteCall != null) {
            observacionDeleteCall.cancel();
            observacionDeleteCall = null;
        }
    }

    private void mostrarErrorActivo(@NonNull String mensaje) {
        if (tabActiva == TabActiva.VENTAS) {
            mostrarErrorVentas(mensaje);
            return;
        }
        if (tabActiva == TabActiva.POPULARIDAD) {
            mostrarErrorPopularidad(mensaje);
            return;
        }
        if (tabActiva == TabActiva.CRECIMIENTO) {
            mostrarErrorCrecimiento(mensaje);
            return;
        }
        mostrarErrorCategorias(mensaje);
    }

    private boolean canInteractWithUi() {
        return isAdded() && getView() != null && getContext() != null;
    }

    private void mostrarSnackbar(@NonNull String mensaje) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG).show();
            return;
        }
        Context context = getContext();
        if (context != null) {
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    private String formatearMoneda(double monto) {
        return currencyFormatter.format(Math.max(0d, monto));
    }

    @NonNull
    private String formatearEtiquetaDia(@NonNull LocalDate fecha) {
        return fecha.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, LOCALE_ES_MX)
                .replace(".", "")
                .toLowerCase(LOCALE_ES_MX);
    }

    @Nullable
    private LocalDate parseFecha(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), API_DATE_FORMATTER);
        } catch (Exception ignored) {
            return null;
        }
    }

    @NonNull
    private static LocalDate obtenerInicioSemanaActual() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private static final class ObservacionContexto {
        private final String tipoEstadistica;
        private final String tipoDato;
        @Nullable
        private final LocalDate fechaInicioPeriodo;
        @Nullable
        private final LocalDate fechaFinPeriodo;

        private ObservacionContexto(@NonNull String tipoEstadistica,
                                    @NonNull String tipoDato,
                                    @Nullable LocalDate fechaInicioPeriodo,
                                    @Nullable LocalDate fechaFinPeriodo) {
            this.tipoEstadistica = tipoEstadistica;
            this.tipoDato = tipoDato;
            this.fechaInicioPeriodo = fechaInicioPeriodo;
            this.fechaFinPeriodo = fechaFinPeriodo;
        }

        @NonNull
        private String buildKey() {
            String inicio = fechaInicioPeriodo != null ? fechaInicioPeriodo.toString() : "null";
            String fin = fechaFinPeriodo != null ? fechaFinPeriodo.toString() : "null";
            return tipoEstadistica + "|" + tipoDato + "|" + inicio + "|" + fin;
        }
    }

    private static final class PodioViews {
        final MaterialCardView card;
        final TextView badge;
        final TextView tag;
        final ShapeableImageView imagenRect;
        final ShapeableImageView imagenCircle;
        final View perfilLayout;
        final View autorLayout;
        final ShapeableImageView autorImagen;
        final TextView autorTexto;
        final TextView nombre;
        final TextView subtitulo;
        final TextView perfilNombre;
        final TextView perfilSubtitulo;
        final TextView total;
        final TextView detalle;

        private PodioViews(@NonNull MaterialCardView card,
                           @NonNull TextView badge,
                           @NonNull TextView tag,
                           @NonNull ShapeableImageView imagenRect,
                           @NonNull ShapeableImageView imagenCircle,
                           @NonNull View perfilLayout,
                           @NonNull View autorLayout,
                           @NonNull ShapeableImageView autorImagen,
                           @NonNull TextView autorTexto,
                           @NonNull TextView nombre,
                           @NonNull TextView subtitulo,
                           @NonNull TextView perfilNombre,
                           @NonNull TextView perfilSubtitulo,
                           @NonNull TextView total,
                           @NonNull TextView detalle) {
            this.card = card;
            this.badge = badge;
            this.tag = tag;
            this.imagenRect = imagenRect;
            this.imagenCircle = imagenCircle;
            this.perfilLayout = perfilLayout;
            this.autorLayout = autorLayout;
            this.autorImagen = autorImagen;
            this.autorTexto = autorTexto;
            this.nombre = nombre;
            this.subtitulo = subtitulo;
            this.perfilNombre = perfilNombre;
            this.perfilSubtitulo = perfilSubtitulo;
            this.total = total;
            this.detalle = detalle;
        }
    }
}



package com.example.artistlan.Activitys;

import android.app.PendingIntent;
import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.navigation.NavDeepLinkBuilder;

import com.bumptech.glide.Glide;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.SessionManager;
import com.example.artistlan.Conector.api.CategoriaApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.CategoriaDTO;
import com.example.artistlan.Conector.model.DesactivarCuentaRequestDTO;
import com.example.artistlan.Conector.model.RespuestaModeracionDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.Conector.repository.FirebaseImageRepository;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeEffectsApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Fragments.FragCentroMensajes;
import com.example.artistlan.Fragments.FragSolicitudesMensajes;
import com.example.artistlan.utils.ArtistlanDialogFactory;
import com.example.artistlan.utils.ArtistlanLoadingDialog;
import com.example.artistlan.utils.CardThemeHelper;
import com.example.artistlan.utils.DialogConfig;
import com.example.artistlan.utils.DialogThemeHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActActualizarDatos extends AppCompatActivity implements View.OnClickListener {
    private static final long TOPBAR_NAV_DEBOUNCE_MS = 500L;

    private Button btnActualizarDatos, btnEliminarCuenta;
    private ImageButton IsbtnRegresar;
    private EditText etCorreo, etNombre, etDescripcion, etRedes, etTelefono, etFecha, etUsuario, etUbicacion;
    private ImageView btnCambiarFoto, imgFotoPerfil;
    private Spinner spinnerCategoriaUsuario;
    private View contenedorCambiarFoto, topBar, topBarLight, notiBadge;
    private ImageButton btnMenuLateral, btnCarrito, btnChatbotTopbar, btnNotificaciones;
    private ImageView ivLogo;

    private UsuarioApi api;
    private List<CategoriaDTO> listaCategorias;
    private SessionManager sessionManager;

    private ActivityResultLauncher<String> seleccionarImagenperfilLauncher;
    private ActivityResultLauncher<Void> tomarFotoPerfilLauncher;
    private ActivityResultLauncher<String> permisoCamaraPerfilLauncher;
    private Uri imageUri = null;

    // Theme
    private ThemeManager themeManager;
    private View rootMain, topDivider, cardDivider, cardContainer, glowTop, glowCenter, glowBottom;
    private TextView txtTitulo, txtDesc, txtIndicacion, tvCorreo, tvUsuario, tvFotoPerfil,
            tvNombre, tvDescripcion, tvCategoria, tvRedes, tvTelefono, tvFecha, tvUbicacion,
            tvCambiarFotografia, txtTituloTopBar;
    private ArtistlanLoadingDialog feedbackDialog;
    private boolean actualizacionEnCurso = false;
    private long ultimoClickTopbarMs = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_actualizar_datos);

        themeManager = new ThemeManager(this);

        // Theme refs
        rootMain = findViewById(R.id.main);
        topDivider = findViewById(R.id.IsTopDivider);
        cardDivider = findViewById(R.id.IsCardDivider);
        cardContainer = findViewById(R.id.IsLayCard);
        glowTop = findViewById(R.id.AdGlowTop);
        glowCenter = findViewById(R.id.AdGlowCenter);
        glowBottom = findViewById(R.id.AdGlowBottom);
        topBar = findViewById(R.id.layoutBarraSuperior);
        topBarLight = findViewById(R.id.topBarLight);
        btnMenuLateral = findViewById(R.id.btnMenuLateral);
        btnCarrito = findViewById(R.id.btnCarrito);
        btnChatbotTopbar = findViewById(R.id.btnChatbotTopbar);
        btnNotificaciones = findViewById(R.id.btnNotificaciones);
        ivLogo = findViewById(R.id.ivLogo);
        txtTituloTopBar = findViewById(R.id.txtTituloTopBar);
        notiBadge = findViewById(R.id.notiBadge);

        txtTitulo = findViewById(R.id.IsTxtTitulo);
        txtDesc = findViewById(R.id.IsTxtDesc);
        txtIndicacion = findViewById(R.id.IsTxtindicacion);
        tvCorreo = findViewById(R.id.tvCorreo);
        tvUsuario = findViewById(R.id.tvUsuario);
        tvFotoPerfil = findViewById(R.id.tvFotoPerfil);
        tvNombre = findViewById(R.id.tvNombre);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvCategoria = findViewById(R.id.lsTxtCategoria);
        tvRedes = findViewById(R.id.tvRedes);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvFecha = findViewById(R.id.tvFecha);
        tvUbicacion = findViewById(R.id.tvUbicacion);
        tvCambiarFotografia = findViewById(R.id.tvCambiarFotografia);

        // Enlazar XML
        etCorreo = findViewById(R.id.correo);
        etNombre = findViewById(R.id.nombre);
        etDescripcion = findViewById(R.id.descripcion);
        etRedes = findViewById(R.id.redes);
        etTelefono = findViewById(R.id.telefono);
        etFecha = findViewById(R.id.CrcEdtFecha);
        etUsuario = findViewById(R.id.usuario);
        etUbicacion = findViewById(R.id.ubicacion);
        spinnerCategoriaUsuario = findViewById(R.id.spinnerCategoriaUsuario);

        btnActualizarDatos = findViewById(R.id.btnActualizarDatos);
        IsbtnRegresar = findViewById(R.id.IsbtnRegresar);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);
        contenedorCambiarFoto = findViewById(R.id.contenedorCambiarFoto);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);

        applyThemeOnlyColors();
        feedbackDialog = new ArtistlanLoadingDialog(this);

        btnEliminarCuenta.setOnClickListener(this);
        btnActualizarDatos.setOnClickListener(this);
        IsbtnRegresar.setOnClickListener(this);
        ScrollView scrollView = findViewById(R.id.scrollActualizarDatos);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    imeHeight
            );
            return insets;
        });

        api = RetrofitClient.getClient().create(UsuarioApi.class);
        sessionManager = new SessionManager(this);

        etFecha.setOnClickListener(v -> mostrarDatePicker());

        seleccionarImagenperfilLauncher =
                registerForActivityResult(
                        new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                imageUri = uri;
                                imgFotoPerfil.setImageURI(uri);
                            }
                        }
                );

        tomarFotoPerfilLauncher =
                registerForActivityResult(
                        new androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview(),
                        bitmap -> {
                            if (bitmap != null) {
                                Uri cameraUri = guardarBitmapPerfilEnCache(bitmap);
                                if (cameraUri == null) {
                                    Toast.makeText(this, "No se pudo procesar la foto tomada", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                imageUri = cameraUri;
                                Glide.with(this).load(imageUri).centerCrop().into(imgFotoPerfil);
                            }
                        }
                );

        permisoCamaraPerfilLauncher =
                registerForActivityResult(
                        new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                tomarFotoPerfilLauncher.launch(null);
                            } else {
                                Toast.makeText(this, "Debes conceder permiso de cámara para tomar fotos", Toast.LENGTH_LONG).show();
                            }
                        }
                );

        View.OnClickListener cambiarFotoListener = v -> mostrarOpcionesFotoPerfil();
        btnCambiarFoto.setOnClickListener(cambiarFotoListener);
        if (contenedorCambiarFoto != null) {
            contenedorCambiarFoto.setOnClickListener(cambiarFotoListener);
        }
        configurarAccionesTopbar();

        cargarDatosUsuario();
        cargarCategoriasDesdeApi();
    }

    private void configurarAccionesTopbar() {
        if (btnMenuLateral != null) {
            btnMenuLateral.setEnabled(true);
            btnMenuLateral.setClickable(true);
            btnMenuLateral.setOnClickListener(v -> {
                if (!puedeEjecutarAccionTopbar()) return;
                v.animate()
                        .rotationBy(90f)
                        .setDuration(160)
                        .withEndAction(() -> v.setRotation(0f))
                        .start();
                finish();
            });
        }
        if (btnChatbotTopbar != null) {
            btnChatbotTopbar.setEnabled(true);
            btnChatbotTopbar.setClickable(true);
            btnChatbotTopbar.setOnClickListener(v -> {
                if (!puedeEjecutarAccionTopbar()) return;
                v.animate()
                        .rotationBy(10f)
                        .setDuration(90)
                        .withEndAction(() -> v.animate().rotation(0f).setDuration(120).start())
                        .start();
                abrirChatbotAsistenciaDesdeTopbar();
            });
        }
        if (btnNotificaciones != null) {
            btnNotificaciones.setEnabled(true);
            btnNotificaciones.setClickable(true);
            btnNotificaciones.setOnClickListener(v -> {
                if (!puedeEjecutarAccionTopbar()) return;
                v.animate()
                        .rotationBy(12f)
                        .setDuration(90)
                        .withEndAction(() -> v.animate().rotation(0f).setDuration(120).start())
                        .start();
                abrirCentroMensajesDesdeTopbar();
            });
        }
    }

    private boolean puedeEjecutarAccionTopbar() {
        long ahora = SystemClock.elapsedRealtime();
        if (ahora - ultimoClickTopbarMs < TOPBAR_NAV_DEBOUNCE_MS) {
            return false;
        }
        ultimoClickTopbarMs = ahora;
        return true;
    }

    private void abrirCentroMensajesDesdeTopbar() {
        Bundle args = new Bundle();
        args.putInt(FragCentroMensajes.ARG_TAB_INICIAL, 0);
        args.putInt(FragCentroMensajes.ARG_SOLICITUDES_MODO, FragSolicitudesMensajes.MODO_RECIBIDAS);
        try {
            PendingIntent pendingIntent = new NavDeepLinkBuilder(this)
                    .setComponentName(ActFragmentoPrincipal.class)
                    .setGraph(R.navigation.navegador)
                    .setDestination(R.id.fragCentroMensajes)
                    .setArguments(args)
                    .createPendingIntent();
            pendingIntent.send();
            finish();
        } catch (PendingIntent.CanceledException e) {
            Toast.makeText(this, "No se pudieron abrir las notificaciones.", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirChatbotAsistenciaDesdeTopbar() {
        try {
            PendingIntent pendingIntent = new NavDeepLinkBuilder(this)
                    .setComponentName(ActFragmentoPrincipal.class)
                    .setGraph(R.navigation.navegador)
                    .setDestination(R.id.fragAyuda)
                    .createPendingIntent();
            pendingIntent.send();
            finish();
        } catch (PendingIntent.CanceledException e) {
            Toast.makeText(this, "No se pudo abrir el chatbot de asistencia.", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyThemeOnlyColors() {
        ThemeApplier.applySystemBars(this, themeManager);
        aplicarTemaMenuSuperior();

        if (rootMain != null) {
            rootMain.setBackgroundColor(themeManager.color(ThemeKeys.BG_BOTTOM));
        }

        if (cardContainer != null && cardContainer.getBackground() != null) {
            cardContainer.getBackground().setColorFilter(
                    themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL),
                    PorterDuff.Mode.SRC_ATOP
            );
            cardContainer.setAlpha(0.96f);
        }

        ThemeApplier.applyTextPrimary(txtTitulo, themeManager);
        ThemeApplier.applyTextSecondary(txtDesc, themeManager);
        ThemeApplier.applyTextSecondary(txtIndicacion, themeManager);

        ThemeApplier.applyTextPrimary(tvCorreo, themeManager);
        ThemeApplier.applyTextPrimary(tvUsuario, themeManager);
        ThemeApplier.applyTextPrimary(tvFotoPerfil, themeManager);
        ThemeApplier.applyTextPrimary(tvNombre, themeManager);
        ThemeApplier.applyTextPrimary(tvDescripcion, themeManager);
        ThemeApplier.applyTextPrimary(tvCategoria, themeManager);
        ThemeApplier.applyTextPrimary(tvRedes, themeManager);
        ThemeApplier.applyTextPrimary(tvTelefono, themeManager);
        ThemeApplier.applyTextPrimary(tvFecha, themeManager);
        ThemeApplier.applyTextPrimary(tvUbicacion, themeManager);

        ThemeApplier.applyInput(etCorreo, themeManager);
        ThemeApplier.applyInput(etUsuario, themeManager);
        ThemeApplier.applyInput(etNombre, themeManager);
        ThemeApplier.applyInput(etDescripcion, themeManager);
        ThemeApplier.applyInput(etRedes, themeManager);
        ThemeApplier.applyInput(etTelefono, themeManager);
        ThemeApplier.applyInput(etFecha, themeManager);
        ThemeApplier.applyInput(etUbicacion, themeManager);

        if (spinnerCategoriaUsuario != null) {
            DialogThemeHelper.applyDialogComboStyle(spinnerCategoriaUsuario, this);
        }

        aplicarBotonPrincipal(btnActualizarDatos, themeManager);
        aplicarBotonSecundario(btnEliminarCuenta, themeManager);

        if (IsbtnRegresar != null) {
            CardThemeHelper.applyFilterButton(IsbtnRegresar, themeManager);
            ThemeApplier.animatePress(IsbtnRegresar);
        }

        if (btnCambiarFoto != null) {
            aplicarIconButtonBubble(btnCambiarFoto, themeManager, themeManager.color(ThemeKeys.BUTTON_PRIMARY_BG));
        }
        ThemeApplier.applyTextSecondary(tvCambiarFotografia, themeManager);
        if (contenedorCambiarFoto != null) {
            ThemeApplier.animatePress(contenedorCambiarFoto);
        }

        if (topDivider != null && topDivider.getBackground() != null) {
            topDivider.getBackground().setColorFilter(themeManager.color(ThemeKeys.ACCOUNT_DIVIDER), PorterDuff.Mode.SRC_ATOP);
        }

        if (cardDivider != null && cardDivider.getBackground() != null) {
            cardDivider.getBackground().setColorFilter(themeManager.color(ThemeKeys.ACCOUNT_DIVIDER), PorterDuff.Mode.SRC_ATOP);
        }

        ThemeEffectsApplier.applyPanelGlass(cardContainer, themeManager);
        ThemeEffectsApplier.applyGlowIntensity(glowTop, themeManager, ThemeKeys.GLOW_PRIMARY);
        ThemeEffectsApplier.applyGlowIntensity(glowCenter, themeManager, ThemeKeys.GLOW_TERTIARY);
        ThemeEffectsApplier.applyGlowIntensity(glowBottom, themeManager, ThemeKeys.GLOW_SECONDARY);
    }

    private void aplicarTemaMenuSuperior() {
        if (topBar != null && topBar.getBackground() != null) {
            topBar.getBackground().setColorFilter(themeManager.color(ThemeKeys.MENU_TOPBAR), PorterDuff.Mode.SRC_ATOP);
        }
        ThemeEffectsApplier.applyTopLight(topBarLight, themeManager);
        if (txtTituloTopBar != null) {
            txtTituloTopBar.setTextColor(themeManager.color(ThemeKeys.MENU_TITLE));
        }
        if (ivLogo != null) {
            ivLogo.setVisibility(View.GONE);
        }
        if (btnMenuLateral != null) {
            btnMenuLateral.setColorFilter(themeManager.color(ThemeKeys.ICON_TOPBAR), PorterDuff.Mode.SRC_IN);
        }
        if (btnCarrito != null) {
            btnCarrito.setColorFilter(themeManager.color(ThemeKeys.ICON_TOPBAR), PorterDuff.Mode.SRC_IN);
        }
        if (btnChatbotTopbar != null) {
            btnChatbotTopbar.setColorFilter(themeManager.color(ThemeKeys.ICON_TOPBAR), PorterDuff.Mode.SRC_IN);
        }
        if (btnNotificaciones != null) {
            btnNotificaciones.setColorFilter(themeManager.color(ThemeKeys.ICON_TOPBAR), PorterDuff.Mode.SRC_IN);
        }
        if (notiBadge != null && notiBadge.getBackground() != null) {
            int badgeColor = themeManager.color(ThemeKeys.MENU_BADGE);
            notiBadge.getBackground().setColorFilter(badgeColor, PorterDuff.Mode.SRC_ATOP);
            if (notiBadge instanceof TextView) {
                ((TextView) notiBadge).setTextColor(elegirColorTextoBotonTema(
                        badgeColor,
                        themeManager.color(ThemeKeys.BUTTON_TEXT_DARK),
                        themeManager.color(ThemeKeys.BUTTON_TEXT_LIGHT),
                        themeManager.color(ThemeKeys.TEXT_PRIMARY),
                        themeManager.color(ThemeKeys.TEXT_SECONDARY)
                ));
            }
        }
    }

    private void aplicarBotonPrincipal(@Nullable Button button, @NonNull ThemeManager tm) {
        aplicarFormaBoton(button);
        int backgroundColor = tm.color(ThemeKeys.BUTTON_PRIMARY_BG);
        int strokeColor = elegirColorTextoBoton(
                backgroundColor,
                tm.color(ThemeKeys.BUTTON_TEXT_DARK),
                tm.color(ThemeKeys.BUTTON_TEXT_LIGHT),
                tm.color(ThemeKeys.TEXT_PRIMARY),
                tm.color(ThemeKeys.TEXT_SECONDARY)
        );
        aplicarFondoBotonTema(button, backgroundColor, strokeColor);
        aplicarTextoBotonTema(button, backgroundColor, tm.color(ThemeKeys.BUTTON_TEXT_DARK),
                tm.color(ThemeKeys.BUTTON_TEXT_LIGHT), tm.color(ThemeKeys.TEXT_PRIMARY), tm.color(ThemeKeys.TEXT_SECONDARY));
    }

    private void aplicarBotonSecundario(@Nullable Button button, @NonNull ThemeManager tm) {
        aplicarFormaBoton(button);
        int backgroundColor = tm.color(ThemeKeys.BUTTON_SECONDARY_BG);
        int strokeColor = elegirColorTextoBoton(
                backgroundColor,
                tm.color(ThemeKeys.BUTTON_TEXT_LIGHT),
                tm.color(ThemeKeys.BUTTON_TEXT_DARK),
                tm.color(ThemeKeys.TEXT_PRIMARY),
                tm.color(ThemeKeys.TEXT_SECONDARY)
        );
        aplicarFondoBotonTema(button, backgroundColor, strokeColor);
        aplicarTextoBotonTema(button, backgroundColor, tm.color(ThemeKeys.BUTTON_TEXT_LIGHT),
                tm.color(ThemeKeys.BUTTON_TEXT_DARK), tm.color(ThemeKeys.TEXT_PRIMARY), tm.color(ThemeKeys.TEXT_SECONDARY));
    }

    private void aplicarFormaBoton(@Nullable Button button) {
        if (button == null) return;
        button.setAllCaps(false);
        button.setTextSize(17);
        button.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        button.setMinHeight(dpToPx(62));
        button.setPadding(dpToPx(18), 0, dpToPx(18), 0);
        ViewGroup.LayoutParams params = button.getLayoutParams();
        if (params != null) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = dpToPx(62);
            button.setLayoutParams(params);
        }
    }

    private void aplicarFondoBotonTema(@Nullable Button button, int backgroundColor, int textColor) {
        if (button == null) return;
        button.setBackgroundTintList(null);
        button.setBackground(crearFondoBubbleTema(backgroundColor, textColor));
        ThemeApplier.animatePress(button);
    }

    @NonNull
    private LayerDrawable crearFondoBubbleTema(int backgroundColor, int textColor) {
        float radius = dpToPx(40);

        GradientDrawable shadow = new GradientDrawable();
        shadow.setShape(GradientDrawable.RECTANGLE);
        shadow.setColor(ColorUtils.setAlphaComponent(Color.BLACK, 42));
        shadow.setCornerRadius(radius);

        GradientDrawable fill = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        ColorUtils.blendARGB(backgroundColor, Color.WHITE, 0.24f),
                        backgroundColor,
                        ColorUtils.blendARGB(backgroundColor, Color.BLACK, 0.10f)
                }
        );
        fill.setShape(GradientDrawable.RECTANGLE);
        fill.setCornerRadius(radius);
        fill.setStroke(dpToPx(1), ColorUtils.setAlphaComponent(textColor, 90));

        GradientDrawable highlight = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{ColorUtils.setAlphaComponent(Color.WHITE, 78), Color.TRANSPARENT}
        );
        highlight.setShape(GradientDrawable.RECTANGLE);
        highlight.setCornerRadius(dpToPx(30));

        LayerDrawable drawable = new LayerDrawable(new android.graphics.drawable.Drawable[]{shadow, fill, highlight});
        drawable.setLayerInset(0, 0, dpToPx(6), 0, 0);
        drawable.setLayerInset(2, dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(34));
        return drawable;
    }

    private void aplicarTextoBotonTema(@Nullable Button button, int backgroundColor, int preferredTextColor, int... themeCandidates) {
        if (button == null) return;
        button.setTextColor(elegirColorTextoBotonTema(backgroundColor, preferredTextColor, themeCandidates));
    }

    private int elegirColorTextoBotonTema(int backgroundColor, int preferredTextColor, int... themeCandidates) {
        if (ColorUtils.calculateContrast(preferredTextColor, backgroundColor) >= 3.0d) {
            return preferredTextColor;
        }
        int selected = preferredTextColor;
        double bestThemeContrast = ColorUtils.calculateContrast(preferredTextColor, backgroundColor);
        for (int candidate : themeCandidates) {
            double contrast = ColorUtils.calculateContrast(candidate, backgroundColor);
            if (contrast > bestThemeContrast) {
                bestThemeContrast = contrast;
                selected = candidate;
            }
        }
        if (bestThemeContrast >= 3.0d) {
            return selected;
        }
        return elegirColorTextoBoton(backgroundColor, selected);
    }

    private int elegirColorTextoBoton(int backgroundColor, int preferredTextColor, int... themeCandidates) {
        if (ColorUtils.calculateContrast(preferredTextColor, backgroundColor) >= 4.5d) {
            return preferredTextColor;
        }
        int selected = preferredTextColor;
        double bestContrast = ColorUtils.calculateContrast(preferredTextColor, backgroundColor);
        for (int candidate : themeCandidates) {
            double contrast = ColorUtils.calculateContrast(candidate, backgroundColor);
            if (contrast > bestContrast) {
                bestContrast = contrast;
                selected = candidate;
            }
        }
        if (bestContrast >= 4.5d) {
            return selected;
        }
        double contrastWhite = ColorUtils.calculateContrast(Color.WHITE, backgroundColor);
        double contrastBlack = ColorUtils.calculateContrast(Color.BLACK, backgroundColor);
        return contrastWhite >= contrastBlack ? Color.WHITE : Color.BLACK;
    }

    private void aplicarIconButtonBubble(@Nullable ImageView button, @NonNull ThemeManager tm, int backgroundColor) {
        if (button == null) return;
        int iconColor = elegirColorTextoBotonTema(
                backgroundColor,
                tm.color(ThemeKeys.ICON_ACTIVE),
                tm.color(ThemeKeys.BUTTON_TEXT_DARK),
                tm.color(ThemeKeys.BUTTON_TEXT_LIGHT),
                tm.color(ThemeKeys.TEXT_PRIMARY),
                tm.color(ThemeKeys.TEXT_SECONDARY)
        );
        button.setBackground(crearFondoBubbleTema(backgroundColor, iconColor));
        button.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        button.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        ThemeApplier.animatePress(button);
    }

    private void mostrarOpcionesFotoPerfil() {
        ArtistlanDialogFactory.show(this, DialogConfig.builder()
                .setTitle("Foto de perfil")
                .setMessage("Selecciona cómo quieres actualizar tu foto.")
                .setType(DialogConfig.Type.INFO)
                .setPositiveText("Elegir de galería")
                .setNegativeText("Tomar foto")
                .setNeutralText("Cancelar")
                .setOnPositive(() -> {
                    Toast.makeText(this, "Selecciona una imagen cuadrada para que tu foto se vea correctamente.", Toast.LENGTH_SHORT).show();
                    seleccionarImagenperfilLauncher.launch("image/*");
                })
                .setOnNegative(this::abrirCamaraPerfilConPermiso)
                .build());
    }

    private void abrirCamaraPerfilConPermiso() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            tomarFotoPerfilLauncher.launch(null);
            return;
        }
        permisoCamaraPerfilLauncher.launch(Manifest.permission.CAMERA);
    }

    private Uri guardarBitmapPerfilEnCache(Bitmap bitmap) {
        File picturesDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "perfil");
        if (!picturesDir.exists() && !picturesDir.mkdirs()) {
            return null;
        }

        File imageFile = new File(picturesDir, "perfil_" + System.currentTimeMillis() + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            return FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    imageFile
            );
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    private void mostrarDatePicker() {
        Calendar fechaMaxima = obtenerFechaMaximaMayorEdad();
        Calendar inicio = Calendar.getInstance();
        inicio.setTimeInMillis(fechaMaxima.getTimeInMillis());

        int anio = inicio.get(Calendar.YEAR);
        int mes = inicio.get(Calendar.MONTH);
        int dia = inicio.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) ->
                        etFecha.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                anio, mes, dia
        );
        datePickerDialog.getDatePicker().setMaxDate(fechaMaxima.getTimeInMillis());
        datePickerDialog.show();
        DialogThemeHelper.styleDialogWindow(datePickerDialog, this);
    }

    private Calendar obtenerFechaMaximaMayorEdad() {
        Calendar fechaMaxima = Calendar.getInstance();
        fechaMaxima.set(Calendar.HOUR_OF_DAY, 23);
        fechaMaxima.set(Calendar.MINUTE, 59);
        fechaMaxima.set(Calendar.SECOND, 59);
        fechaMaxima.set(Calendar.MILLISECOND, 999);
        fechaMaxima.add(Calendar.YEAR, -18);
        return fechaMaxima;
    }

    private void cargarDatosUsuario() {
        SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);

        etNombre.setText(prefs.getString("nombreCompleto", ""));
        etUsuario.setText(prefs.getString("usuario", ""));
        etCorreo.setText(prefs.getString("correo", ""));
        etDescripcion.setText(prefs.getString("descripcion", ""));
        etRedes.setText(prefs.getString("redes", ""));
        etTelefono.setText(prefs.getString("telefono", ""));
        etFecha.setText(prefs.getString("fechaNac", ""));
        etUbicacion.setText(prefs.getString("ubicacion", ""));

        String foto = prefs.getString("fotoPerfil", "");
        if (!foto.isEmpty()) {
            Glide.with(this).load(foto).centerCrop().into(imgFotoPerfil);
        } else {
            imgFotoPerfil.setImageResource(R.drawable.fotoperfilprueba);
        }
    }

    private void cargarCategoriasDesdeApi() {
        listaCategorias = new ArrayList<>();
        CategoriaApi apiCategoria = RetrofitClient.getClient().create(CategoriaApi.class);
        apiCategoria.obtenerCategorias().enqueue(new Callback<List<CategoriaDTO>>() {
            @Override
            public void onResponse(Call<List<CategoriaDTO>> call, Response<List<CategoriaDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCategorias.clear();
                    List<String> nombresCategorias = new ArrayList<>();
                    nombresCategorias.add("Ninguna");

                    for (CategoriaDTO c : response.body()) {
                        int id = c.getIdCategoria();
                        if (id >= 19 && id <= 37) {
                            listaCategorias.add(c);
                            nombresCategorias.add(c.getNombreCategoria());
                        }
                    }

                    ArrayAdapter<String> adapter = DialogThemeHelper.createDialogComboAdapter(
                            ActActualizarDatos.this,
                            nombresCategorias
                    );
                    spinnerCategoriaUsuario.setAdapter(adapter);
                    DialogThemeHelper.applyDialogComboStyle(spinnerCategoriaUsuario, ActActualizarDatos.this);

                    SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
                    String categoriaActual = prefs.getString("ocupacion", prefs.getString("categoria", "Ninguna"));
                    int posicion = nombresCategorias.indexOf(categoriaActual);
                    spinnerCategoriaUsuario.setSelection(posicion >= 0 ? posicion : 0);
                }
            }

            @Override
            public void onFailure(Call<List<CategoriaDTO>> call, Throwable t) {
                Toast.makeText(ActActualizarDatos.this, "Error al cargar categorías: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.IsbtnRegresar) {
            finish();
        } else if (v.getId() == R.id.btnActualizarDatos) {
            actualizarUsuario();
        } else if (v.getId() == R.id.btnEliminarCuenta) {
            mostrarDialogoDesactivarCuentaConContrasena();
        }
    }

    private void mostrarDialogoDesactivarCuentaConContrasena() {
        ArtistlanDialogFactory.showPassword(
                this,
                "Desactivar cuenta",
                "Tu cuenta se desactivará. No se borrará físicamente.\n\nSe conservarán tu historial de compras, ventas y transacciones.\n\nPara confirmar, ingresa tu contraseña actual.",
                "Ingresa tu contraseña actual",
                "Desactivar cuenta",
                "Cancelar",
                this::desactivarCuentaConApiV11
        );
    }

    private void mostrarDialogoDesactivarCuenta() {
        ArtistlanDialogFactory.show(this, DialogConfig.builder()
                .setTitle("Desactivar cuenta")
                .setMessage("Tu cuenta se desactivará y ya no podrá iniciar sesión ni operar normalmente. Tu historial de compras, ventas, reportes y transacciones se conservará. Esta acción no borra físicamente tu información. ¿Deseas confirmar la desactivación de tu cuenta?")
                .setType(DialogConfig.Type.DANGER)
                .setPositiveText("Sí, desactivar")
                .setNegativeText("Cancelar")
                .setOnPositive(this::desactivarCuentaConApi)
                .build());
    }

    private void desactivarCuentaConApi() {
        SharedPreferences prefs = getSharedPreferences(SessionManager.PREF_NAME, MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));

        if (idUsuario == -1) {
            Toast.makeText(this, "Tu sesión no es válida. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        DesactivarCuentaRequestDTO request = new DesactivarCuentaRequestDTO();
        request.setIdUsuarioSolicitante(idUsuario);
        request.setMotivo("Cuenta desactivada desde la aplicación por solicitud del usuario");
        request.setConfirmacion(true);

        api.desactivarCuenta(idUsuario, request).enqueue(new Callback<RespuestaModeracionDTO>() {
            @Override
            public void onResponse(Call<RespuestaModeracionDTO> call, Response<RespuestaModeracionDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ActActualizarDatos.this,
                            "Tu cuenta fue desactivada correctamente",
                            Toast.LENGTH_SHORT).show();
                    cerrarSesionYRedirigirALogin();

                } else {
                    Toast.makeText(ActActualizarDatos.this,
                            construirMensajeErrorDesactivacion(response),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaModeracionDTO> call, Throwable t) {
                Toast.makeText(ActActualizarDatos.this,
                        "No pudimos desactivar la cuenta. Revisa tu conexión e inténtalo de nuevo.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private String construirMensajeErrorDesactivacion(Response<RespuestaModeracionDTO> response) {
        String backendMessage = ApiErrorParser.extractMessage(response);
        int code = response != null ? response.code() : -1;

        if (code == 409) {
            return backendMessage != null ? backendMessage : "Tu cuenta ya se encuentra desactivada o bloqueada.";
        }
        if (code == 403) {
            return backendMessage != null ? backendMessage : "No tienes permisos para desactivar esta cuenta.";
        }
        if (code == 400) {
            return backendMessage != null ? backendMessage : "La solicitud de desactivación no es válida.";
        }
        return backendMessage != null ? backendMessage : "No se pudo desactivar la cuenta. Inténtalo de nuevo más tarde.";
    }

    private void desactivarCuentaConApiV11(String contrasenaActual, ArtistlanDialogFactory.PasswordDialogHandle handle) {
        SharedPreferences prefs = getSharedPreferences(SessionManager.PREF_NAME, MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));

        if (idUsuario == -1) {
            handle.showError("Tu sesión no es válida. Inicia sesión nuevamente.");
            return;
        }

        DesactivarCuentaRequestDTO request = new DesactivarCuentaRequestDTO();
        request.setIdUsuarioSolicitante(idUsuario);
        request.setContrasenaActual(contrasenaActual);
        request.setMotivo("Cuenta desactivada desde la aplicación por solicitud del usuario");
        request.setConfirmacion(true);

        api.desactivarCuenta(idUsuario, request).enqueue(new Callback<RespuestaModeracionDTO>() {
            @Override
            public void onResponse(Call<RespuestaModeracionDTO> call, Response<RespuestaModeracionDTO> response) {
                if (response.isSuccessful()) {
                    handle.dismiss();
                    if (feedbackDialog != null) {
                        feedbackDialog.showSuccess("Tu cuenta fue desactivada correctamente.", ActActualizarDatos.this::cerrarSesionYRedirigirALogin);
                    } else {
                        cerrarSesionYRedirigirALogin();
                    }
                } else {
                    handle.showError(construirMensajeErrorDesactivacionV11(response));
                }
            }

            @Override
            public void onFailure(Call<RespuestaModeracionDTO> call, Throwable t) {
                handle.showError("Error de conexión al desactivar la cuenta.");
            }
        });
    }

    private String construirMensajeErrorDesactivacionV11(Response<RespuestaModeracionDTO> response) {
        String backendMessage = ApiErrorParser.extractMessage(response);
        int code = response != null ? response.code() : -1;

        if (code == 400) {
            return backendMessage != null ? backendMessage : "Revisa los datos para desactivar la cuenta.";
        }
        if (code == 403) {
            return backendMessage != null ? backendMessage : "La contraseña actual es incorrecta.";
        }
        if (code == 409) {
            return backendMessage != null ? backendMessage : "La cuenta no puede desactivarse.";
        }
        return backendMessage != null ? backendMessage : "No se pudo desactivar la cuenta.";
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void cerrarSesionYRedirigirALogin() {
        if (sessionManager != null) {
            sessionManager.clearSession();
        } else {
            getSharedPreferences(SessionManager.PREF_NAME, MODE_PRIVATE).edit().clear().apply();
        }

        Intent intent = new Intent(ActActualizarDatos.this, ActIniciarSesion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void actualizarUsuario() {
        if (actualizacionEnCurso) {
            return;
        }
        SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        if (idUsuario == -1) {
            Toast.makeText(this, "No se encontró una sesión activa.", Toast.LENGTH_SHORT).show();
            return;
        }

        etCorreo.setError(null);
        etUsuario.setError(null);
        etNombre.setError(null);
        etTelefono.setError(null);

        String correo = getTrimmedText(etCorreo);
        String usuario = getTrimmedText(etUsuario);
        String nombre = getTrimmedText(etNombre);
        String fechaNac = getTrimmedText(etFecha);
        if (usuario.isEmpty()) {
            etUsuario.setError("El nombre de usuario es obligatorio.");
            etUsuario.requestFocus();
            return;
        }
        if (correo.isEmpty()) {
            etCorreo.setError("El correo es obligatorio.");
            etCorreo.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Ingresa un correo válido.");
            etCorreo.requestFocus();
            return;
        }
        if (nombre.isEmpty()) {
            etNombre.setError("El nombre no puede estar vacío.");
            etNombre.requestFocus();
            Toast.makeText(this, "El nombre no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fechaNac.isEmpty()) {
            Toast.makeText(this, "Por favor elige tu fecha de nacimiento.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!esMayorDeEdad(fechaNac)) {
            Toast.makeText(this, "Debes ser mayor de edad para usar Artistlan.", Toast.LENGTH_SHORT).show();
            return;
        }

        String telefono = etTelefono.getText().toString().trim();
        if (!telefono.isEmpty() && !esTelefonoValido(telefono)) {
            etTelefono.setError("Teléfono inválido");
            etTelefono.requestFocus();
            return;
        }

        iniciarFeedbackActualizacionPerfil();
        if (imageUri != null) {
            subirFotoPerfil(idUsuario, prefs, () -> enviarActualizacionUsuario(idUsuario, prefs));
        } else {
            enviarActualizacionUsuario(idUsuario, prefs);
        }
    }

    private void subirFotoPerfil(int idUsuario, SharedPreferences prefs, Runnable onSuccess) {
        FirebaseImageRepository repo = new FirebaseImageRepository(this);
        repo.subirFotoPerfilYGuardarEnBD(idUsuario, imageUri, new FirebaseImageRepository.ImagenListener() {
            @Override
            public void onSuccess(String urlNueva) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("fotoPerfil", urlNueva);
                editor.apply();
                onSuccess.run();
            }

            @Override
            public void onError(String mensaje) {
                finalizarFeedbackErrorPerfil("No se pudo actualizar el perfil");
                Toast.makeText(ActActualizarDatos.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enviarActualizacionUsuario(int idUsuario, SharedPreferences prefs) {
        UsuariosDTO usuarioActualizado = new UsuariosDTO();

        int posicionSeleccionada = spinnerCategoriaUsuario.getSelectedItemPosition();
        Integer idCategoria = null;
        String ocupacionActual = prefs.getString("ocupacion", prefs.getString("categoria", ""));

        if (posicionSeleccionada > 0) {
            CategoriaDTO categoriaSeleccionada = listaCategorias.get(posicionSeleccionada - 1);
            String nombreSeleccionado = categoriaSeleccionada.getNombreCategoria();
            boolean cambioCategoria = ocupacionActual == null
                    || !ocupacionActual.trim().equalsIgnoreCase(nombreSeleccionado != null ? nombreSeleccionado.trim() : "");
            if (cambioCategoria) {
                idCategoria = categoriaSeleccionada.getIdCategoria();
            }
        }
        usuarioActualizado.setIdCategoria(idCategoria);

        usuarioActualizado.setNombreCompleto(etNombre.getText().toString().trim());
        usuarioActualizado.setCorreo(etCorreo.getText().toString().trim());
        usuarioActualizado.setUsuario(etUsuario.getText().toString().trim());
        usuarioActualizado.setDescripcion(etDescripcion.getText().toString().trim());
        usuarioActualizado.setRedesSociales(etRedes.getText().toString().trim());
        usuarioActualizado.setTelefono(etTelefono.getText().toString().trim());
        usuarioActualizado.setFechaNacimiento(etFecha.getText().toString().trim());
        usuarioActualizado.setUbicacion(etUbicacion.getText().toString().trim());
        usuarioActualizado.setFotoPerfil(prefs.getString("fotoPerfil", ""));
        String rolActual = prefs.getString("rol", null);
        if (rolActual != null && !rolActual.trim().isEmpty()) {
            usuarioActualizado.setRol(rolActual);
        }

        api.actualizarUsuario(idUsuario, usuarioActualizado).enqueue(new Callback<UsuariosDTO>() {
            @Override
            public void onResponse(Call<UsuariosDTO> call, Response<UsuariosDTO> response) {
                if (response.isSuccessful()) {
                    persistirUsuarioActualizado(
                            prefs,
                            idUsuario,
                            usuarioActualizado,
                            response.body(),
                            posicionSeleccionada,
                            ocupacionActual
                    );
                    finalizarFeedbackExitoPerfil();
                } else {
                    finalizarFeedbackErrorPerfil("No se pudo actualizar el perfil");
                    manejarErrorActualizacion(response);
                }
            }

            @Override
            public void onFailure(Call<UsuariosDTO> call, Throwable t) {
                finalizarFeedbackErrorPerfil("No se pudo actualizar el perfil");
                Toast.makeText(ActActualizarDatos.this, "No pudimos actualizar el perfil. Revisa tu conexión e inténtalo de nuevo.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void iniciarFeedbackActualizacionPerfil() {
        actualizacionEnCurso = true;
        if (btnActualizarDatos != null) {
            btnActualizarDatos.setEnabled(false);
        }
        if (feedbackDialog != null) {
            feedbackDialog.showLoading("Actualizando perfil...");
        }
    }

    private void finalizarFeedbackExitoPerfil() {
        actualizacionEnCurso = false;
        if (btnActualizarDatos != null) {
            btnActualizarDatos.setEnabled(true);
        }
        if (feedbackDialog != null) {
            feedbackDialog.showSuccess("Perfil actualizado", this::finish);
        } else {
            finish();
        }
    }

    private void finalizarFeedbackErrorPerfil(String mensaje) {
        actualizacionEnCurso = false;
        if (btnActualizarDatos != null) {
            btnActualizarDatos.setEnabled(true);
        }
        if (feedbackDialog != null) {
            feedbackDialog.showError(mensaje);
        }
    }

    private void persistirUsuarioActualizado(
            SharedPreferences prefs,
            int idUsuario,
            UsuariosDTO usuarioEnviado,
            UsuariosDTO usuarioRespuesta,
            int posicionSeleccionada,
            String ocupacionActual
    ) {
        UsuariosDTO usuarioPersistido = usuarioRespuesta != null ? usuarioRespuesta : usuarioEnviado;
        usuarioPersistido.setIdUsuario(idUsuario);

        if (isBlank(usuarioPersistido.getRol())) {
            usuarioPersistido.setRol(prefs.getString("rol", "USER"));
        }
        if (isBlank(usuarioPersistido.getFotoPerfil())) {
            usuarioPersistido.setFotoPerfil(prefs.getString("fotoPerfil", ""));
        }

        String tokenActual = sessionManager != null ? sessionManager.getToken() : null;
        if (sessionManager != null && tokenActual != null) {
            sessionManager.saveUserSession(usuarioPersistido, tokenActual);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("id", idUsuario);
        editor.putInt("idUsuario", idUsuario);
        editor.putString("usuario", valueOrEmpty(usuarioPersistido.getUsuario()));
        editor.putString("correo", valueOrEmpty(usuarioPersistido.getCorreo()));
        editor.putString("nombreCompleto", valueOrEmpty(usuarioPersistido.getNombreCompleto()));
        editor.putString("descripcion", valueOrEmpty(usuarioPersistido.getDescripcion()));
        editor.putString("fotoPerfil", valueOrEmpty(usuarioPersistido.getFotoPerfil()));
        editor.putString("telefono", valueOrEmpty(usuarioPersistido.getTelefono()));
        editor.putString("redesSociales", valueOrEmpty(usuarioPersistido.getRedesSociales()));
        editor.putString("redes", valueOrEmpty(usuarioPersistido.getRedesSociales()));
        editor.putString("fechaNacimiento", valueOrEmpty(usuarioPersistido.getFechaNacimiento()));
        editor.putString("fechaNac", valueOrEmpty(usuarioPersistido.getFechaNacimiento()));
        editor.putString("ubicacion", valueOrEmpty(usuarioPersistido.getUbicacion()));
        editor.putString("rol", valueOrEmpty(usuarioPersistido.getRol()));
        editor.putBoolean("twoFactorEnabled", Boolean.TRUE.equals(usuarioPersistido.getTwoFactorEnabled()));

        String ocupacionPersistida = resolverOcupacionPersistida(usuarioPersistido, posicionSeleccionada, ocupacionActual);
        if (!isBlank(ocupacionPersistida)) {
            editor.putString("categoria", ocupacionPersistida);
            editor.putString("ocupacion", ocupacionPersistida);
        }

        editor.apply();
    }

    private String resolverOcupacionPersistida(
            UsuariosDTO usuarioPersistido,
            int posicionSeleccionada,
            String ocupacionActual
    ) {
        if (usuarioPersistido != null && !isBlank(usuarioPersistido.getCategoria())) {
            return usuarioPersistido.getCategoria().trim();
        }
        if (posicionSeleccionada > 0
                && listaCategorias != null
                && posicionSeleccionada <= listaCategorias.size()) {
            String nombreCategoria = listaCategorias.get(posicionSeleccionada - 1).getNombreCategoria();
            if (!isBlank(nombreCategoria)) {
                return nombreCategoria.trim();
            }
        }
        return isBlank(ocupacionActual) ? null : ocupacionActual.trim();
    }

    private void manejarErrorActualizacion(Response<?> response) {
        String backendMessage = ApiErrorParser.extractMessage(response);
        String mensaje = backendMessage != null
                ? backendMessage
                : "Error al actualizar (Código: " + response.code() + ")";

        if (response.code() == 409) {
            String mensajeNormalizado = backendMessage == null ? "" : backendMessage.toLowerCase(Locale.ROOT);
            if (mensajeNormalizado.contains("correo")) {
                mensaje = "El correo ya está en uso.";
                etCorreo.setError(mensaje);
                etCorreo.requestFocus();
            } else if (mensajeNormalizado.contains("usuario")) {
                mensaje = "El nombre de usuario ya está en uso.";
                etUsuario.setError(mensaje);
                etUsuario.requestFocus();
            }
        }

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private String getTrimmedText(EditText editText) {
        return editText != null && editText.getText() != null
                ? editText.getText().toString().trim()
                : "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private boolean esTelefonoValido(String telefono) {
        String limpio = telefono == null ? "" : telefono.trim();
        return limpio.matches("^\\+?\\d{10,15}$");
    }

    private boolean esMayorDeEdad(String fechaNac) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);
            Date nacimiento = sdf.parse(fechaNac);
            if (nacimiento == null) return false;
            Calendar nac = Calendar.getInstance();
            nac.setTime(nacimiento);
            Calendar hoy = Calendar.getInstance();
            int edad = hoy.get(Calendar.YEAR) - nac.get(Calendar.YEAR);
            if (hoy.get(Calendar.DAY_OF_YEAR) < nac.get(Calendar.DAY_OF_YEAR)) edad--;
            return edad >= 18;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        themeManager = new ThemeManager(this);
        applyThemeOnlyColors();
    }

    @Override
    protected void onDestroy() {
        if (feedbackDialog != null) {
            feedbackDialog.release();
            feedbackDialog = null;
        }
        super.onDestroy();
    }
}




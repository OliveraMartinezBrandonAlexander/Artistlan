package com.example.artistlan.Activitys;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.os.SystemClock;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.SessionManager;
import com.example.artistlan.Conector.api.Auth2FAApi;
import com.example.artistlan.Conector.api.CarritoPaypalApi;
import com.example.artistlan.Conector.api.PagoPaypalApi;
import com.example.artistlan.Conector.model.CapturarOrdenPaypalCarritoResponseDTO;
import com.example.artistlan.Conector.model.CapturarOrdenPaypalResponseDTO;
import com.example.artistlan.Conector.model.TwoFactorResponse;
import com.example.artistlan.Fragments.FragCarrito;
import com.example.artistlan.Fragments.FragCentroMensajes;
import com.example.artistlan.Fragments.FragSolicitudesMensajes;
import com.example.artistlan.Fragments.FragTransacciones;
import com.example.artistlan.Fragments.MensajesBadgeManager;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ActAjustesTema;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeEffectsApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.pagos.PagoPaypalSessionManager;
import com.example.artistlan.pagos.PagoSyncManager;
import com.example.artistlan.utils.ScrollMenuVisibilityHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

public class ActFragmentoPrincipal extends AppCompatActivity {
    private static final String TAG = "ActFragmentoPrincipal";
    private static final String TAG_NAV_CRASH_DEBUG = "ModeracionNavCrashDebug";
    private static final boolean ENABLE_NAV_DEBUG_LOGS = false;
    private static final long MAX_REUSABLE_SESSION_IDLE_MS = 7L * 24L * 60L * 60L * 1000L;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;

    private ImageButton btnMenuLateral;
    private ImageButton btnCarrito;
    private ImageButton btnNotificaciones;
    private ImageView ivLogo;
    private TextView txtTituloTopBar;

    private View cartContainer;
    private View topBar;
    private View bottomBarContainer;
    private MaterialCardView navCard;
    private View topBarFrame;
    private View bottomBarFrame;
    private View mainContent;

    private View topBarLight, bottomBarLight;

    // Glows del fondo principal
    private View menuGlowTop, menuGlowCenter, menuGlowBottom;

    // Badge campana
    private TextView notiBadge;

    // Glows del header drawer
    private View drawerGlow1, drawerGlow2;

    // Tema
    private ThemeManager themeManager;

    // Animaciones
    private ObjectAnimator bellAnimator;

    private ObjectAnimator glowTopX, glowTopY, glowTopAlpha;
    private ObjectAnimator glowCenterX, glowCenterY, glowCenterAlpha;
    private ObjectAnimator glowBottomX, glowBottomY, glowBottomAlpha;

    private ObjectAnimator drawerGlow1X, drawerGlow1Alpha;
    private ObjectAnimator drawerGlow2Y, drawerGlow2Alpha;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final long NAV_DEBOUNCE_MS = 400L;
    private static final long CENTRO_MENSAJES_NAV_DEBOUNCE_MS = 500L;
    private static final long BADGE_REFRESH_MIN_INTERVAL_MS = 1500L;
    private long ultimaAccionNavegacion = 0L;
    private long ultimaNavegacionCentroMensajes = 0L;
    private long ultimoRefrescoBadgeMs = 0L;
    private boolean capturandoPagoDeepLink = false;
    private boolean activationPromptShown = false;
    private boolean activationRequestInProgress = false;
    private SessionManager sessionManager;
    private Auth2FAApi auth2FAApi;
    private AlertDialog twoFactorPromptDialog;
    private AlertDialog twoFactorLoadingDialog;
    private ScrollMenuVisibilityHelper scrollMenuVisibilityHelper;
    private boolean bottomMenuHiddenByKeyboard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_fragmento_principal);

        themeManager = new ThemeManager(this);
        sessionManager = new SessionManager(this);
        auth2FAApi = RetrofitClient.getClient().create(Auth2FAApi.class);

        if (!ensureReusableSessionOrRedirect()) {
            return;
        }

        initViews();
        applyThemeOnlyColors();
        aplicarColoresSistema();
        aplicarBlurSiSePuede();
        configurarDrawer();
        cargarHeaderDrawer();
        configurarAdminDrawerSection();
        configurarNavegacion();
        conectarScrollMenuHelper();
        handlePaypalDeepLinkIntent(getIntent());
        configurarEventos();
        prepararAnimacionesIniciales();
        animarEntradaUI();
        animarCampana();
        animarGlows();
        refrescarBadgeMensajes(true);
        mostrarModalActivacion2FAIfNeeded();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        btnMenuLateral = findViewById(R.id.btnMenuLateral);
        btnCarrito = findViewById(R.id.btnCarrito);
        btnNotificaciones = findViewById(R.id.btnNotificaciones);
        ivLogo = findViewById(R.id.ivLogo);
        txtTituloTopBar = findViewById(R.id.txtTituloTopBar);

        cartContainer = findViewById(R.id.cartContainer);
        topBar = findViewById(R.id.layoutBarraSuperior);
        topBarFrame = findViewById(R.id.topBarFrame);
        bottomBarFrame = findViewById(R.id.MenuInferiorFrame);
        bottomBarContainer = findViewById(R.id.MenuInferior);
        navCard = findViewById(R.id.navCard);
        mainContent = findViewById(R.id.mainContent);

        topBarLight = findViewById(R.id.topBarLight);
        bottomBarLight = findViewById(R.id.bottomBarLight);

        menuGlowTop = findViewById(R.id.menuGlowTop);
        menuGlowCenter = findViewById(R.id.menuGlowCenter);
        menuGlowBottom = findViewById(R.id.menuGlowBottom);

        notiBadge = findViewById(R.id.notiBadge);
    }

    private void applyThemeOnlyColors() {
        themeManager = new ThemeManager(this);
        ThemeApplier.applySystemBars(this, themeManager);

        if (topBar != null && topBar.getBackground() != null) {
            topBar.getBackground().setColorFilter(
                    themeManager.color(ThemeKeys.MENU_TOPBAR),
                    PorterDuff.Mode.SRC_ATOP
            );
        }

        BottomNavigationView bottomBar = findViewById(R.id.bottomBar);
        if (navCard != null) {
            navCard.setCardBackgroundColor(ColorUtils.setAlphaComponent(
                    themeManager.color(ThemeKeys.MENU_BOTTOMBAR),
                    218
            ));
            navCard.setStrokeColor(themeManager.color(ThemeKeys.MENU_ITEM_ACTIVE_STROKE));
        }

        if (bottomBar != null) {
            bottomBar.setBackgroundColor(android.graphics.Color.TRANSPARENT);

            ColorStateList iconStates = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked},
                            new int[]{}
                    },
                    new int[]{
                            themeManager.color(ThemeKeys.ICON_ACTIVE),
                            themeManager.color(ThemeKeys.ICON_BOTTOM)
                    }
            );

            ColorStateList textStates = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked},
                            new int[]{}
                    },
                    new int[]{
                            themeManager.color(ThemeKeys.TEXT_PRIMARY),
                            themeManager.color(ThemeKeys.TEXT_SECONDARY)
                    }
            );

            bottomBar.setItemIconTintList(iconStates);
            bottomBar.setItemTextColor(textStates);
        }

        if (btnCarrito != null) {
            btnCarrito.setOnClickListener(v -> {
                if (!puedeEjecutarNavegacion() || navController == null) return;
                abrirCarrito();
            });
        }
        if (navigationView != null) {
            if (navigationView.getBackground() != null) {
                navigationView.getBackground().setColorFilter(
                        themeManager.color(ThemeKeys.MENU_DRAWER),
                        PorterDuff.Mode.SRC_ATOP
                );
            }

            ColorStateList drawerStates = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_checked},
                            new int[]{}
                    },
                    new int[]{
                            themeManager.color(ThemeKeys.ICON_ACTIVE),
                            themeManager.color(ThemeKeys.ICON_DRAWER)
                    }
            );

            navigationView.setItemIconTintList(drawerStates);
            navigationView.setItemTextColor(drawerStates);
        }

        if (btnMenuLateral != null) {
            btnMenuLateral.setColorFilter(themeManager.color(ThemeKeys.ICON_TOPBAR), PorterDuff.Mode.SRC_ATOP);
        }

        if (ivLogo != null) {
            ivLogo.setVisibility(View.GONE);
        }

        if (txtTituloTopBar != null) {
            txtTituloTopBar.setTextColor(themeManager.color(ThemeKeys.MENU_TITLE));
        }

        if (btnCarrito != null) {
            btnCarrito.setColorFilter(themeManager.color(ThemeKeys.ICON_TOPBAR), PorterDuff.Mode.SRC_ATOP);
        }

        if (btnNotificaciones != null) {
            btnNotificaciones.setColorFilter(themeManager.color(ThemeKeys.ICON_TOPBAR), PorterDuff.Mode.SRC_ATOP);
        }

        if (notiBadge != null && notiBadge.getBackground() != null) {
            notiBadge.getBackground().setColorFilter(
                    themeManager.color(ThemeKeys.MENU_BADGE),
                    PorterDuff.Mode.SRC_ATOP
            );
        }

        ThemeEffectsApplier.applyTopLight(topBarLight, themeManager);
        ThemeEffectsApplier.applyTopLight(bottomBarLight, themeManager);

        ThemeEffectsApplier.applyGlowIntensity(menuGlowTop, themeManager, ThemeKeys.GLOW_PRIMARY);
        ThemeEffectsApplier.applyGlowIntensity(menuGlowCenter, themeManager, ThemeKeys.GLOW_TERTIARY);
        ThemeEffectsApplier.applyGlowIntensity(menuGlowBottom, themeManager, ThemeKeys.GLOW_SECONDARY);
    }

    private void aplicarColoresSistema() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(themeManager.color(ThemeKeys.MENU_TOPBAR));
            getWindow().setNavigationBarColor(themeManager.color(ThemeKeys.MENU_BOTTOMBAR));
        }
    }

    private void aplicarBlurSiSePuede() {
        if (btnCarrito != null) {
            btnCarrito.setOnClickListener(v -> {
                if (!puedeEjecutarNavegacion() || navController == null) return;
                abrirCarrito();
            });
        }
        if (navigationView != null) {
            navigationView.setAlpha(1f);
        }
    }

    private void configurarDrawer() {
        if (drawerLayout == null) return;

        drawerLayout.setScrimColor(0x66000000);

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (mainContent != null) {
                    float scale = 1f - (0.055f * slideOffset);
                    float translation = 28f * slideOffset;

                    mainContent.setScaleX(scale);
                    mainContent.setScaleY(scale);
                    mainContent.setTranslationX(translation);
                    mainContent.setAlpha(1f - (0.10f * slideOffset));
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                animarItemsDrawer();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (mainContent != null) {
                    mainContent.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(180)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                }
            }
        });
    }

    private void configurarAdminDrawerSection() {
        if (navigationView == null) return;

        SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
        String rol = prefs.getString("rol", "USER");
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol);
        boolean esModerador = "MODERADOR".equalsIgnoreCase(rol);

        navigationView.getMenu().setGroupVisible(R.id.admin_group, esAdmin);
        navigationView.getMenu().findItem(R.id.navAdminSection).setVisible(esAdmin);
        navigationView.getMenu().findItem(R.id.navModeracionReportes).setVisible(esAdmin || esModerador);
    }

    private void configurarNavegacion() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        if (navHostFragment == null) return;

        navController = navHostFragment.getNavController();
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> actualizarBotonesTopBar(destination));

        BottomNavigationView bottomBar = findViewById(R.id.bottomBar);
        if (bottomBar != null) {
            bottomBar.setItemIconTintList(null);
            NavigationUI.setupWithNavController(bottomBar, navController);
            bottomBar.post(() -> animarItemActivoBottomNav(bottomBar));

            bottomBar.setOnItemSelectedListener(item -> {
                if (!puedeEjecutarNavegacion()) return false;
                ocultarTecladoAntesDeNavegar();
                animarBottomNavTap(bottomBar);
                animarItemActivoBottomNav(bottomBar);
                return navegarSinDuplicar(item.getItemId());
            });
        }

        if (btnCarrito != null) {
            btnCarrito.setOnClickListener(v -> {
                if (!puedeEjecutarNavegacion() || navController == null) return;
                abrirCarrito();
            });
        }
        if (navigationView != null) {
            NavigationUI.setupWithNavController(navigationView, navController);
        }
    }


    private void conectarScrollMenuHelper() {
        if (topBarFrame == null || bottomBarFrame == null) return;
        scrollMenuVisibilityHelper = new ScrollMenuVisibilityHelper(topBarFrame, bottomBarFrame);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if (navHostFragment != null) {
            scrollMenuVisibilityHelper.registerWith(navHostFragment.getChildFragmentManager());
        }
    }

    private void actualizarBotonesTopBar(NavDestination destination) {
        boolean mostrarCarrito = destination != null && destination.getId() == R.id.fragExplorar;
        if (cartContainer == null) return;

        if (mostrarCarrito) {
            cartContainer.setVisibility(View.VISIBLE);
            cartContainer.setAlpha(0f);
            cartContainer.animate().alpha(1f).setDuration(150).start();
        } else {
            cartContainer.setVisibility(View.GONE);
        }
    }

    private void animarItemActivoBottomNav(BottomNavigationView bottomBar) {
        if (bottomBar == null) return;

        bottomBar.post(() -> {
            ViewGroup menuView = (ViewGroup) bottomBar.getChildAt(0);
            if (menuView == null) return;

            for (int i = 0; i < menuView.getChildCount(); i++) {
                View item = menuView.getChildAt(i);
                if (item == null) continue;

                if (item.isSelected()) {
                    item.animate()
                            .scaleX(1.06f)
                            .scaleY(1.06f)
                            .setDuration(140)
                            .withEndAction(() -> item.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(120)
                                    .start())
                            .start();

                    item.setAlpha(1f);
                } else {
                    item.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();

                    item.setAlpha(0.92f);
                }
            }
        });
    }

    private void configurarEventos() {
        if (btnMenuLateral != null) {
            btnMenuLateral.setOnClickListener(v -> {
                if (!puedeEjecutarNavegacion()) return;
                v.animate()
                        .rotationBy(90f)
                        .setDuration(160)
                        .withEndAction(() -> v.setRotation(0f))
                        .start();

                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (btnCarrito != null) {
            btnCarrito.setColorFilter(themeManager.color(ThemeKeys.ICON_TOPBAR), PorterDuff.Mode.SRC_ATOP);
        }

        if (btnNotificaciones != null) {
            btnNotificaciones.setOnClickListener(v -> {
                if (!puedeEjecutarNavegacion()) return;
                v.animate()
                        .rotationBy(12f)
                        .setDuration(90)
                        .withEndAction(() ->
                                v.animate().rotation(0f).setDuration(120).start()
                        )
                        .start();

                abrirCentroMensajes(0);
            });
        }

        if (btnCarrito != null) {
            btnCarrito.setOnClickListener(v -> {
                if (!puedeEjecutarNavegacion() || navController == null) return;
                abrirCarrito();
            });
        }
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                if (!puedeEjecutarNavegacion()) return true;
                ocultarTecladoAntesDeNavegar();
                int itemId = item.getItemId();

                if (itemId == R.id.navAdminGestionarUsuarios) {
                    if (navController != null) navegarSinDuplicar(R.id.fragAdminGestionUsuarios);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                if (itemId == R.id.navAdminEditarConvocatorias) {
                    if (navController != null) navegarSinDuplicar(R.id.fragAdminConvocatorias);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                if (itemId == R.id.navAdminModuloInformativo) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                if (itemId == R.id.navCalendario) {
                    if (navController != null) navegarSinDuplicar(R.id.navCalendario);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                if (itemId == R.id.navConfiguracion) {
                    startActivity(new Intent(ActFragmentoPrincipal.this, ActAjustesTema.class));
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                if (itemId == R.id.navMensajes) {
                    abrirCentroMensajes(0);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                if (itemId == R.id.navNotificaciones) {
                    abrirCentroMensajes(1, FragSolicitudesMensajes.MODO_RECIBIDAS);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                if (itemId == R.id.navModeracionReportes) {
                    if (navController != null) {
                        navegarSinDuplicar(R.id.fragModeracionReportes);
                    }
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                if (itemId == R.id.navCerrarSesion) {
                    cerrarSesion();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                if (navController != null) {
                    boolean handled = navegarSinDuplicar(itemId);
                    if (handled) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                    return handled;
                }

                return false;
            });
        }
    }

    private void abrirCarrito() {
        if (navController == null || navController.getCurrentDestination() == null) return;
        if (navController.getCurrentDestination().getId() == R.id.fragCarrito) return;

        NavOptions navOptions = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .build();

        navController.navigate(R.id.fragCarrito, null, navOptions);
    }

    public void abrirCentroMensajes(int tabInicial) {
        abrirCentroMensajes(tabInicial, FragSolicitudesMensajes.MODO_RECIBIDAS);
    }

    public void abrirCentroMensajes(int tabInicial, int solicitudesModo) {
        if (navController == null) return;
        long ahora = SystemClock.elapsedRealtime();
        if (ahora - ultimaNavegacionCentroMensajes < CENTRO_MENSAJES_NAV_DEBOUNCE_MS) {
            logDebug(TAG_NAV_CRASH_DEBUG, "abrirCentroMensajes ignorado por debounce");
            return;
        }
        ultimaNavegacionCentroMensajes = ahora;
        ocultarTecladoAntesDeNavegar();
        NavDestination currentDestination = navController.getCurrentDestination();
        logDebug(TAG_NAV_CRASH_DEBUG, "abrirCentroMensajes currentDestination="
                + (currentDestination != null ? currentDestination.getId() : -1));

        Bundle args = new Bundle();
        args.putInt(FragCentroMensajes.ARG_TAB_INICIAL, Math.max(0, Math.min(1, tabInicial)));
        args.putInt(FragCentroMensajes.ARG_SOLICITUDES_MODO, solicitudesModo);

        NavDestination current = navController.getCurrentDestination();
        if (current != null && current.getId() == R.id.fragCentroMensajes) {
            NavHostFragment navHostFragment =
                    (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
            if (navHostFragment != null) {
                Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                if (currentFragment instanceof FragCentroMensajes) {
                    FragCentroMensajes centro = (FragCentroMensajes) currentFragment;
                    centro.seleccionarTab(tabInicial);
                    centro.seleccionarModoSolicitudes(solicitudesModo);
                    return;
                }
            }
        }

        navegarSinDuplicar(R.id.fragCentroMensajes, args);
    }

    public void abrirTransacciones(int tabInicial) {
        if (navController == null) return;
        int safeTab = Math.max(0, Math.min(1, tabInicial));

        NavDestination current = navController.getCurrentDestination();
        if (current != null && current.getId() == R.id.fragTransacciones) {
            NavHostFragment navHostFragment =
                    (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
            if (navHostFragment != null) {
                Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                if (currentFragment instanceof FragTransacciones) {
                    ((FragTransacciones) currentFragment).seleccionarTab(safeTab);
                    return;
                }
            }
        }

        Bundle args = new Bundle();
        args.putInt(FragTransacciones.ARG_TAB_INICIAL, safeTab);
        navegarSinDuplicar(R.id.fragTransacciones, args);
    }

    public void refrescarBadgeMensajes() {
        refrescarBadgeMensajes(false);
    }

    private void refrescarBadgeMensajes(boolean force) {
        long ahora = SystemClock.elapsedRealtime();
        if (!force && (ahora - ultimoRefrescoBadgeMs) < BADGE_REFRESH_MIN_INTERVAL_MS) {
            logDebug(TAG, "refrescarBadgeMensajes omitido por throttle");
            return;
        }
        ultimoRefrescoBadgeMs = ahora;
        SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        MensajesBadgeManager.refrescarBadge(idUsuario, this::actualizarBadgeMensajesVisual);
    }

    public boolean navegarDesdeCentroMensajes(int destinationId, Bundle args) {
        return navegarSinDuplicar(destinationId, args);
    }

    public void setBottomMenuHiddenByKeyboard(boolean hidden) {
        logDebug(TAG_NAV_CRASH_DEBUG, "setBottomMenuHiddenByKeyboard hidden=" + hidden);
        if (bottomBarContainer == null || isFinishing() || isDestroyed() || !bottomBarContainer.isAttachedToWindow()) {
            return;
        }
        if (hidden == bottomMenuHiddenByKeyboard) {
            return;
        }
        bottomMenuHiddenByKeyboard = hidden;

        bottomBarContainer.animate().cancel();
        float hiddenTranslation = bottomBarContainer.getHeight() > 0 ? bottomBarContainer.getHeight() : 42f;

        if (hidden) {
            bottomBarContainer.animate()
                    .alpha(0f)
                    .translationY(hiddenTranslation)
                    .setDuration(160)
                    .withEndAction(() -> {
                        if (bottomMenuHiddenByKeyboard
                                && bottomBarContainer != null
                                && !isFinishing()
                                && !isDestroyed()
                                && bottomBarContainer.isAttachedToWindow()) {
                            bottomBarContainer.setVisibility(View.GONE);
                        }
                    })
                    .start();
            return;
        }

        bottomBarContainer.setVisibility(View.VISIBLE);
        bottomBarContainer.setAlpha(0f);
        bottomBarContainer.setTranslationY(hiddenTranslation);
        bottomBarContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .start();
    }

    private void prepararAnimacionesIniciales() {
        if (topBar != null) {
            topBar.setAlpha(0f);
            topBar.setTranslationY(-36f);
        }

        if (bottomBarContainer != null) {
            bottomBarContainer.setAlpha(0f);
            bottomBarContainer.setTranslationY(42f);
        }
    }

    private void animarEntradaUI() {
        if (topBar != null) {
            topBar.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(420)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        if (bottomBarContainer != null) {
            bottomBarContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(100)
                    .setDuration(460)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void animarCampana() {
        if (btnNotificaciones == null) return;

        cancelAnimator(bellAnimator);

        bellAnimator = ObjectAnimator.ofFloat(btnNotificaciones, "rotation", -7f, 7f);
        bellAnimator.setDuration(420);
        bellAnimator.setRepeatCount(1);
        bellAnimator.setRepeatMode(ValueAnimator.REVERSE);
        bellAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        btnNotificaciones.postDelayed(() -> {
            if (bellAnimator != null) bellAnimator.start();
        }, 750);
    }

    private void actualizarBadgeMensajesVisual(int totalPendientes) {
        if (notiBadge == null) return;

        if (totalPendientes <= 0) {
            notiBadge.setVisibility(View.GONE);
            return;
        }

        String badgeText = totalPendientes > 99 ? "99+" : String.valueOf(totalPendientes);
        boolean animarEntrada = notiBadge.getVisibility() != View.VISIBLE;
        notiBadge.setText(badgeText);
        notiBadge.setVisibility(View.VISIBLE);

        if (animarEntrada) {
            notiBadge.setScaleX(0.7f);
            notiBadge.setScaleY(0.7f);
            notiBadge.setAlpha(0f);

            notiBadge.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(220)
                    .start();
        }
    }

    private void animarBottomNavTap(BottomNavigationView bottomBar) {
        if (bottomBar == null) return;

        bottomBar.animate()
                .scaleX(0.985f)
                .scaleY(0.985f)
                .setDuration(80)
                .withEndAction(() ->
                        bottomBar.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .start()
                )
                .start();
    }

    private void animarItemsDrawer() {
        if (navigationView == null) return;

        ViewGroup vg = (ViewGroup) navigationView.getChildAt(0);
        if (vg == null) return;

        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationX(-18f);
            child.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setStartDelay(i * 20L)
                    .setDuration(220)
                    .start();
        }
    }

    private void animarGlows() {
        glowTopX = createAnimator(menuGlowTop, "translationX", -40f, 20f, 5600);
        glowTopY = createAnimator(menuGlowTop, "translationY", -30f, 45f, 6200);
        glowTopAlpha = createAnimator(menuGlowTop, "alpha", 0.28f, 0.48f, 2800);

        glowCenterX = createAnimator(menuGlowCenter, "translationX", -35f, 35f, 5000);
        glowCenterY = createAnimator(menuGlowCenter, "translationY", 150f, 220f, 5400);
        glowCenterAlpha = createAnimator(menuGlowCenter, "alpha", 0.14f, 0.28f, 2600);

        glowBottomX = createAnimator(menuGlowBottom, "translationX", 65f, -5f, 5900);
        glowBottomY = createAnimator(menuGlowBottom, "translationY", 75f, -20f, 6600);
        glowBottomAlpha = createAnimator(menuGlowBottom, "alpha", 0.18f, 0.36f, 3000);

        startAnimator(glowTopX);
        startAnimator(glowTopY);
        startAnimator(glowTopAlpha);

        startAnimator(glowCenterX);
        startAnimator(glowCenterY);
        startAnimator(glowCenterAlpha);

        startAnimator(glowBottomX);
        startAnimator(glowBottomY);
        startAnimator(glowBottomAlpha);
    }

    private void animarGlowsDrawer() {
        cancelAnimator(drawerGlow1X);
        cancelAnimator(drawerGlow1Alpha);
        cancelAnimator(drawerGlow2Y);
        cancelAnimator(drawerGlow2Alpha);

        drawerGlow1X = createAnimator(drawerGlow1, "translationX", -10f, 18f, 4200);
        drawerGlow1Alpha = createAnimator(drawerGlow1, "alpha", 0.30f, 0.52f, 2400);

        drawerGlow2Y = createAnimator(drawerGlow2, "translationY", 10f, -18f, 4600);
        drawerGlow2Alpha = createAnimator(drawerGlow2, "alpha", 0.18f, 0.34f, 2600);

        startAnimator(drawerGlow1X);
        startAnimator(drawerGlow1Alpha);
        startAnimator(drawerGlow2Y);
        startAnimator(drawerGlow2Alpha);
    }

    private ObjectAnimator createAnimator(View target, String property, float from, float to, long duration) {
        if (target == null) return null;

        ObjectAnimator animator = ObjectAnimator.ofFloat(target, property, from, to);
        animator.setDuration(duration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    private void startAnimator(ObjectAnimator animator) {
        if (animator != null) animator.start();
    }

    private void cargarHeaderDrawer() {
        if (navigationView == null) return;

        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        ImageView imgPerfilDrawer = headerView.findViewById(R.id.imgPerfilDrawer);
        TextView txtNombreDrawer = headerView.findViewById(R.id.txtNombreDrawer);
        TextView txtCorreoDrawer = headerView.findViewById(R.id.txtCorreoDrawer);
        TextView txtRolDrawer = headerView.findViewById(R.id.txtRolDrawer);

        drawerGlow1 = headerView.findViewById(R.id.drawerGlow1);
        drawerGlow2 = headerView.findViewById(R.id.drawerGlow2);

        SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);

        String nombre = prefs.getString("nombreCompleto", "Perfil de usuario");
        String correo = prefs.getString("correo", "correo no disponible");
        String rol = prefs.getString("rol", "");
        String categoria = prefs.getString("categoria", "");
        String modo = prefs.getString("modo", "");

        String valorModo;
        if (!modo.isEmpty()) {
            valorModo = modo;
        } else if (!rol.isEmpty()) {
            valorModo = rol;
        } else if (!categoria.isEmpty()) {
            valorModo = categoria;
        } else {
            valorModo = "Artista";
        }

        String fotoPerfil = prefs.getString("fotoPerfil", null);

        txtNombreDrawer.setText(nombre.isEmpty() ? "Perfil de usuario" : nombre);
        txtCorreoDrawer.setText(correo.isEmpty() ? "correo no disponible" : correo);
        txtRolDrawer.setText("Modo: " + valorModo);

        txtNombreDrawer.setTextColor(themeManager.color(ThemeKeys.TEXT_PRIMARY));
        txtCorreoDrawer.setTextColor(themeManager.color(ThemeKeys.TEXT_SECONDARY));
        txtRolDrawer.setTextColor(themeManager.color(ThemeKeys.TEXT_PRIMARY));

        ThemeEffectsApplier.applyGlowIntensity(drawerGlow1, themeManager, ThemeKeys.GLOW_DRAWER_PRIMARY);
        ThemeEffectsApplier.applyGlowIntensity(drawerGlow2, themeManager, ThemeKeys.GLOW_DRAWER_SECONDARY);

        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            cargarImagenPerfilOptimizada(imgPerfilDrawer, fotoPerfil);
        } else {
            imgPerfilDrawer.setImageResource(R.drawable.cuenta);
        }

        animarGlowsDrawer();
    }

    private boolean navegarSinDuplicar(int destinationId) {
        return navegarSinDuplicar(destinationId, null);
    }

    private boolean navegarSinDuplicar(int destinationId, Bundle args) {
        if (navController == null) return false;

        NavDestination current = navController.getCurrentDestination();
        if (current != null && current.getId() == destinationId) {
            return true;
        }

        NavOptions navOptions = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(navController.getGraph().getStartDestinationId(), true)
                .build();

        try {
            navController.navigate(destinationId, args, navOptions);
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            Log.w(TAG_NAV_CRASH_DEBUG, "navigate bloqueado destinationId=" + destinationId, e);
            return false;
        }
    }

    private void ocultarTecladoAntesDeNavegar() {
        View focused = getCurrentFocus();
        if (focused != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            }
            focused.clearFocus();
        }
        setBottomMenuHiddenByKeyboard(false);
        logDebug(TAG_NAV_CRASH_DEBUG, "ocultarTecladoAntesDeNavegar ejecutado");
    }

    private void cerrarSesion() {
        if (sessionManager != null) {
            sessionManager.clearSession();
        }

        Intent irLogin = new Intent(this, MainActivity.class);
        irLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(irLogin);
        finish();
    }

    private boolean ensureReusableSessionOrRedirect() {
        if (sessionManager == null) {
            return true;
        }

        if (sessionManager.hasReusableSession(MAX_REUSABLE_SESSION_IDLE_MS)) {
            sessionManager.touchSession();
            return true;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        return false;
    }

    private void mostrarModalActivacion2FAIfNeeded() {
        if (activationPromptShown || sessionManager == null || isFinishing() || isDestroyed()) {
            return;
        }

        boolean loggedIn = sessionManager.isLoggedIn();
        boolean twoFactorEnabled = sessionManager.isTwoFactorEnabled();
        logDebug(TAG, "twoFactorEnabled guardado = " + twoFactorEnabled + ", loggedIn = " + loggedIn);

        if (!loggedIn || twoFactorEnabled) {
            return;
        }

        activationPromptShown = true;
        mostrarDialogoActivacionPersonalizado();
    }

    private void solicitarActivacion2FA() {
        if (activationRequestInProgress) {
            return;
        }
        String token = sessionManager.getToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Sesión no válida. Vuelve a iniciar sesión.", Toast.LENGTH_LONG).show();
            return;
        }

        activationRequestInProgress = true;
        mostrarLoadingActivacion();

        auth2FAApi.requestActivation("Bearer " + token.trim()).enqueue(new retrofit2.Callback<TwoFactorResponse>() {
            @Override
            public void onResponse(retrofit2.Call<TwoFactorResponse> call, retrofit2.Response<TwoFactorResponse> response) {
                activationRequestInProgress = false;
                ocultarLoadingActivacion();
                if (!response.isSuccessful() || response.body() == null) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(
                            ActFragmentoPrincipal.this,
                            backendMessage != null ? backendMessage : "No se pudo solicitar el codigo de activacion",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                TwoFactorResponse body = response.body();
                if (!Boolean.TRUE.equals(body.getSuccess())) {
                    Toast.makeText(
                            ActFragmentoPrincipal.this,
                            body.getMessage() != null ? body.getMessage() : "No se pudo solicitar el codigo de activacion",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                Intent intent = new Intent(ActFragmentoPrincipal.this, ActVerificarOtpLogin.class);
                intent.putExtra(ActVerificarOtpLogin.EXTRA_MODE, ActVerificarOtpLogin.MODE_ACTIVATION);
                startActivity(intent);
            }

            @Override
            public void onFailure(retrofit2.Call<TwoFactorResponse> call, Throwable t) {
                activationRequestInProgress = false;
                ocultarLoadingActivacion();
                Toast.makeText(ActFragmentoPrincipal.this, "Error de conexión al solicitar activación 2FA", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDialogoActivacionPersonalizado() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_two_factor_activation, null, false);
        TextView title = dialogView.findViewById(R.id.twoFactorActivationTitle);
        TextView message = dialogView.findViewById(R.id.twoFactorActivationMessage);
        TextView lockIcon = dialogView.findViewById(R.id.twoFactorActivationIcon);
        Button activarAhora = dialogView.findViewById(R.id.twoFactorActivationPrimaryButton);
        Button masTarde = dialogView.findViewById(R.id.twoFactorActivationSecondaryButton);

        ThemeApplier.applyTextPrimary(title, themeManager);
        ThemeApplier.applyTextSecondary(message, themeManager);
        ThemeApplier.applyTextPrimary(lockIcon, themeManager);
        ThemeApplier.applyPrimaryButton(activarAhora, themeManager);
        ThemeApplier.applySecondaryButton(masTarde, themeManager);

        twoFactorPromptDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (twoFactorPromptDialog.getWindow() != null) {
            twoFactorPromptDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        activarAhora.setOnClickListener(v -> {
            twoFactorPromptDialog.dismiss();
            solicitarActivacion2FA();
        });

        masTarde.setOnClickListener(v -> twoFactorPromptDialog.dismiss());

        twoFactorPromptDialog.show();
    }

    private void mostrarLoadingActivacion() {
        if (twoFactorLoadingDialog == null) {
            View loadingView = LayoutInflater.from(this).inflate(R.layout.dialog_two_factor_loading, null, false);
            TextView loadingText = loadingView.findViewById(R.id.twoFactorLoadingText);
            ThemeApplier.applyTextPrimary(loadingText, themeManager);

            twoFactorLoadingDialog = new AlertDialog.Builder(this)
                    .setView(loadingView)
                    .setCancelable(false)
                    .create();

            if (twoFactorLoadingDialog.getWindow() != null) {
                twoFactorLoadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            twoFactorLoadingDialog.setCanceledOnTouchOutside(false);
        }

        if (!twoFactorLoadingDialog.isShowing()) {
            twoFactorLoadingDialog.show();
        }
    }

    private void ocultarLoadingActivacion() {
        if (twoFactorLoadingDialog != null && twoFactorLoadingDialog.isShowing()) {
            twoFactorLoadingDialog.dismiss();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handlePaypalDeepLinkIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ensureReusableSessionOrRedirect()) {
            return;
        }
        themeManager = new ThemeManager(this);
        sessionManager.touchSession();
        applyThemeOnlyColors();
        cargarHeaderDrawer();
        configurarAdminDrawerSection();
        refrescarBadgeMensajes(false);
        if (navController != null) {
            actualizarBotonesTopBar(navController.getCurrentDestination());
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            sessionManager.touchSession();
        }
    }

    @Override
    protected void onDestroy() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if (scrollMenuVisibilityHelper != null && navHostFragment != null) {
            scrollMenuVisibilityHelper.unregisterFrom(navHostFragment.getChildFragmentManager());
        }

        super.onDestroy();
        ocultarLoadingActivacion();
        if (twoFactorPromptDialog != null && twoFactorPromptDialog.isShowing()) {
            twoFactorPromptDialog.dismiss();
        }

        cancelAnimator(bellAnimator);

        cancelAnimator(glowTopX);
        cancelAnimator(glowTopY);
        cancelAnimator(glowTopAlpha);

        cancelAnimator(glowCenterX);
        cancelAnimator(glowCenterY);
        cancelAnimator(glowCenterAlpha);

        cancelAnimator(glowBottomX);
        cancelAnimator(glowBottomY);
        cancelAnimator(glowBottomAlpha);

        cancelAnimator(drawerGlow1X);
        cancelAnimator(drawerGlow1Alpha);
        cancelAnimator(drawerGlow2Y);
        cancelAnimator(drawerGlow2Alpha);
    }

    private void cancelAnimator(ObjectAnimator animator) {
        if (animator != null) animator.cancel();
    }

    private void logDebug(String tag, String message) {
        if (ENABLE_NAV_DEBUG_LOGS && isDebugBuild()) {
            Log.d(tag, message);
        }
    }

    private boolean isDebugBuild() {
        ApplicationInfo info = getApplicationInfo();
        return info != null && (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private boolean puedeEjecutarNavegacion() {
        long ahora = SystemClock.elapsedRealtime();
        if (ahora - ultimaAccionNavegacion < NAV_DEBOUNCE_MS) {
            return false;
        }
        ultimaAccionNavegacion = ahora;
        return true;
    }

    private void cargarImagenPerfilOptimizada(ImageView imageView, String fotoPerfil) {
        if (imageView == null) return;
        int fallbackSize = (int) (72 * getResources().getDisplayMetrics().density);
        int ancho = imageView.getWidth() > 0 ? imageView.getWidth() : fallbackSize;
        int alto = imageView.getHeight() > 0 ? imageView.getHeight() : fallbackSize;

        Glide.with(this)
                .load(fotoPerfil)
                .placeholder(R.drawable.cuenta)
                .error(R.drawable.cuenta)
                .override(ancho, alto)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .circleCrop()
                .into(imageView);
    }

    public void refrescarUIRolActual() {
        mainHandler.post(() -> {
            cargarHeaderDrawer();
            configurarAdminDrawerSection();

            if (navController != null && navController.getCurrentDestination() != null) {
                int currentId = navController.getCurrentDestination().getId();
                SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
                String rol = prefs.getString("rol", "USER");
                boolean esAdmin = "ADMIN".equalsIgnoreCase(rol);
                boolean esModerador = "MODERADOR".equalsIgnoreCase(rol);

                if (currentId == R.id.fragAdminGestionUsuarios || currentId == R.id.fragAdminConvocatorias) {
                    if (!esAdmin) {
                        navController.navigate(R.id.fragMain);
                    }
                }

                if (currentId == R.id.fragModeracionReportes) {
                    if (!esAdmin && !esModerador) {
                        navController.navigate(R.id.fragMain);
                    }
                }
            }
        });
    }

    private void handlePaypalDeepLinkIntent(Intent intent) {
        if (intent == null) return;
        Uri data = intent.getData();
        if (data == null) return;

        String scheme = data.getScheme();
        String host = data.getHost();
        if (!"artistlan".equalsIgnoreCase(scheme)) return;

        if ("paypal-return".equalsIgnoreCase(host)) {
            String paypalOrderId = data.getQueryParameter("token");
            if (paypalOrderId == null || paypalOrderId.trim().isEmpty()) return;

            PagoPaypalSessionManager.markApprovalReceivedFromDeepLink(this, paypalOrderId);
            capturarPagoPaypalDesdeDeepLink(paypalOrderId);
            intent.setData(null);
            return;
        }

        if ("paypal-cancel".equalsIgnoreCase(host)) {
            PagoPaypalSessionManager.clear(this);
            onPagoPaypalCancelado();
            intent.setData(null);
        }
    }

    private void capturarPagoPaypalDesdeDeepLink(String paypalOrderId) {
        if (capturandoPagoDeepLink) return;
        if (paypalOrderId == null || paypalOrderId.trim().isEmpty()) {
            PagoPaypalSessionManager.clear(this);
            return;
        }

        capturandoPagoDeepLink = true;
        int obraIdPendiente = PagoPaypalSessionManager.getPendingObraId(this);

        if (obraIdPendiente == -1) {
            CarritoPaypalApi carritoPaypalApi = RetrofitClient.getClient().create(CarritoPaypalApi.class);
            carritoPaypalApi.capturarOrdenCarrito(paypalOrderId.trim()).enqueue(new retrofit2.Callback<CapturarOrdenPaypalCarritoResponseDTO>() {
                @Override
                public void onResponse(retrofit2.Call<CapturarOrdenPaypalCarritoResponseDTO> call, retrofit2.Response<CapturarOrdenPaypalCarritoResponseDTO> response) {
                    capturandoPagoDeepLink = false;
                    CapturarOrdenPaypalCarritoResponseDTO body = response.body();
                    String backendMessage = body != null ? body.resolveUserMessage() : null;
                    PagoPaypalSessionManager.clear(ActFragmentoPrincipal.this);

                    if (response.isSuccessful()) {
                        onPagoCapturadoExitoso();
                        Toast.makeText(
                                ActFragmentoPrincipal.this,
                                backendMessage != null ? backendMessage : "Pago capturado correctamente",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    if (response.code() == 409) {
                        onPagoPaypalCancelado();
                    }
                    Toast.makeText(
                            ActFragmentoPrincipal.this,
                            resolverMensajeCapturaPaypal(response.code(), backendMessage),
                            Toast.LENGTH_LONG
                    ).show();
                }

                @Override
                public void onFailure(retrofit2.Call<CapturarOrdenPaypalCarritoResponseDTO> call, Throwable t) {
                    capturandoPagoDeepLink = false;
                    PagoPaypalSessionManager.clear(ActFragmentoPrincipal.this);
                    Toast.makeText(
                            ActFragmentoPrincipal.this,
                            "Error de red al capturar el pago: " + t.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
            return;
        }

        PagoPaypalApi pagoPaypalApi = RetrofitClient.getClient().create(PagoPaypalApi.class);
        pagoPaypalApi.capturarOrden(paypalOrderId.trim()).enqueue(new retrofit2.Callback<CapturarOrdenPaypalResponseDTO>() {
            @Override
            public void onResponse(retrofit2.Call<CapturarOrdenPaypalResponseDTO> call, retrofit2.Response<CapturarOrdenPaypalResponseDTO> response) {
                capturandoPagoDeepLink = false;
                CapturarOrdenPaypalResponseDTO body = response.body();
                String backendMessage = body != null ? body.resolveUserMessage() : null;
                PagoPaypalSessionManager.clear(ActFragmentoPrincipal.this);

                if (response.isSuccessful()) {
                    onPagoCapturadoExitoso();
                    Toast.makeText(
                            ActFragmentoPrincipal.this,
                            backendMessage != null ? backendMessage : "Pago capturado correctamente",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                if (response.code() == 409) {
                    onPagoPaypalCancelado();
                }
                Toast.makeText(
                        ActFragmentoPrincipal.this,
                        resolverMensajeCapturaPaypal(response.code(), backendMessage),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(retrofit2.Call<CapturarOrdenPaypalResponseDTO> call, Throwable t) {
                capturandoPagoDeepLink = false;
                PagoPaypalSessionManager.clear(ActFragmentoPrincipal.this);
                Toast.makeText(
                        ActFragmentoPrincipal.this,
                        "Error de red al capturar el pago: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void onPagoCapturadoExitoso() {
        PagoSyncManager.markCaptureSuccess(this);
        refrescarBadgeMensajes(true);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if (navHostFragment == null) {
            return;
        }

        Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (currentFragment instanceof FragCarrito) {
            ((FragCarrito) currentFragment).recargarDespuesDePago();
            return;
        }
        if (currentFragment instanceof FragTransacciones) {
            ((FragTransacciones) currentFragment).recargarDespuesDePago();
        }
    }

    private void onPagoPaypalCancelado() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if (navHostFragment == null) {
            return;
        }
        Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (currentFragment instanceof FragCarrito) {
            ((FragCarrito) currentFragment).recargarDespuesDePago();
        }
    }

    private String resolverMensajeCapturaPaypal(int statusCode, String backendMessage) {
        if (statusCode == 409) {
            return "El pago no fue aprobado en PayPal. Puedes intentarlo nuevamente.";
        }
        if (backendMessage != null && !backendMessage.trim().isEmpty()) {
            return backendMessage;
        }
        return "No se pudo capturar el pago (" + statusCode + ")";
    }
}


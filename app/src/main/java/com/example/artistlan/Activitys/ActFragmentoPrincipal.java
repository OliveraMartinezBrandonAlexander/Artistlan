package com.example.artistlan.Activitys;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.os.SystemClock;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ActAjustesTema;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeEffectsApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class ActFragmentoPrincipal extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;

    private ImageButton btnMenuLateral;
    private ImageButton btnNotificaciones;

    private View topBar;
    private View bottomBarContainer;
    private View mainContent;

    private View topBarLight, bottomBarLight;

    // Glows del fondo principal
    private View menuGlowTop, menuGlowCenter, menuGlowBottom;

    // Badge campana
    private View notiBadge;

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
    private long ultimaAccionNavegacion = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_fragmento_principal);

        themeManager = new ThemeManager(this);

        initViews();
        applyThemeOnlyColors();
        aplicarColoresSistema();
        aplicarBlurSiSePuede();
        configurarDrawer();
        cargarHeaderDrawer();
        configurarAdminDrawerSection();
        configurarNavegacion();
        configurarEventos();
        prepararAnimacionesIniciales();
        animarEntradaUI();
        animarCampana();
        animarGlows();
        mostrarBadgeDemo(true);
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        btnMenuLateral = findViewById(R.id.btnMenuLateral);
        btnNotificaciones = findViewById(R.id.btnNotificaciones);

        topBar = findViewById(R.id.layoutBarraSuperior);
        bottomBarContainer = findViewById(R.id.MenuInferior);
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
        if (bottomBar != null) {
            if (bottomBar.getBackground() != null) {
                bottomBar.getBackground().setColorFilter(
                        themeManager.color(ThemeKeys.MENU_BOTTOMBAR),
                        PorterDuff.Mode.SRC_ATOP
                );
            }

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
        boolean esAdmin = "ADMIN".equals(rol);

        navigationView.getMenu().setGroupVisible(R.id.admin_group, esAdmin);
        navigationView.getMenu().findItem(R.id.navAdminSection).setVisible(esAdmin);
    }

    private void configurarNavegacion() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        if (navHostFragment == null) return;

        navController = navHostFragment.getNavController();

        BottomNavigationView bottomBar = findViewById(R.id.bottomBar);
        if (bottomBar != null) {
            bottomBar.setItemIconTintList(null);
            NavigationUI.setupWithNavController(bottomBar, navController);
            bottomBar.post(() -> animarItemActivoBottomNav(bottomBar));

            bottomBar.setOnItemSelectedListener(item -> {
                if (!puedeEjecutarNavegacion()) return false;
                animarBottomNavTap(bottomBar);
                animarItemActivoBottomNav(bottomBar);
                return navegarSinDuplicar(item.getItemId());
            });
        }

        if (navigationView != null) {
            NavigationUI.setupWithNavController(navigationView, navController);
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

                mostrarBadgeDemo(false);

                if (navController != null) {
                    try {
                        navegarSinDuplicar(R.id.fragFavoritos);
                    } catch (Exception ignored) {
                    }
                }
            });
        }

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                if (!puedeEjecutarNavegacion()) return true;
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
                    if (navController != null) {
                        Bundle args = new Bundle();
                        args.putBoolean("scroll_to_convocatorias", true);
                        navController.navigate(R.id.fragMain, args);
                    }
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                if (itemId == R.id.navConfiguracion) {
                    startActivity(new Intent(ActFragmentoPrincipal.this, ActAjustesTema.class));
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

    private void mostrarBadgeDemo(boolean visible) {
        if (notiBadge == null) return;

        notiBadge.setVisibility(visible ? View.VISIBLE : View.GONE);

        if (visible) {
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
            navController.navigate(destinationId, null, navOptions);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("idUsuario");
        editor.remove("usuario");
        editor.remove("rol");
        editor.clear();
        editor.apply();

        Intent irLogin = new Intent(this, MainActivity.class);
        irLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(irLogin);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        themeManager = new ThemeManager(this);
        applyThemeOnlyColors();
        cargarHeaderDrawer();
        configurarAdminDrawerSection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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
                if (currentId == R.id.fragAdminGestionUsuarios || currentId == R.id.fragAdminConvocatorias) {
                    SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
                    String rol = prefs.getString("rol", "USER");
                    if (!"ADMIN".equals(rol)) {
                        navController.navigate(R.id.fragMain);
                    }
                }
            }
        });
    }
}
package com.example.artistlan.Activitys;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class ActFragmentoPrincipal extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private ImageButton btnMenuLateral, btnNotificaciones;

    private View topBar, bottomBarContainer, mainContent;
    private View menuGlowTop, menuGlowCenter, menuGlowBottom;

    private ObjectAnimator bellAnimator;
    private ObjectAnimator glowTopX, glowTopY, glowTopAlpha;
    private ObjectAnimator glowCenterX, glowCenterY, glowCenterAlpha;
    private ObjectAnimator glowBottomX, glowBottomY, glowBottomAlpha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_fragmento_principal);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnMenuLateral = findViewById(R.id.btnMenuLateral);
        btnNotificaciones = findViewById(R.id.btnNotificaciones);

        topBar = findViewById(R.id.layoutBarraSuperior);
        bottomBarContainer = findViewById(R.id.MenuInferior);
        mainContent = findViewById(R.id.mainContent);

        menuGlowTop = findViewById(R.id.menuGlowTop);
        menuGlowCenter = findViewById(R.id.menuGlowCenter);
        menuGlowBottom = findViewById(R.id.menuGlowBottom);

        aplicarColoresSistema();
        configurarDrawer();
        cargarHeaderDrawer();
        configurarNavegacion();
        configurarEventos();
        prepararAnimacionesIniciales();
        animarEntradaUI();
        animarCampana();
        animarGlows();
    }

    private void aplicarColoresSistema() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getColor(R.color.artistlan_menu_topbar));
            getWindow().setNavigationBarColor(getColor(R.color.artistlan_menu_bottombar));
        }
    }

    private void configurarDrawer() {
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

    private void configurarNavegacion() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            BottomNavigationView bottomBar = findViewById(R.id.bottomBar);
            if (bottomBar != null) {
                bottomBar.setItemIconTintList(null);
                NavigationUI.setupWithNavController(bottomBar, navController);
            }

            NavigationUI.setupWithNavController(navigationView, navController);
        }
    }

    private void configurarEventos() {
        if (btnMenuLateral != null) {
            btnMenuLateral.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (btnNotificaciones != null) {
            btnNotificaciones.setOnClickListener(v -> {
                v.animate()
                        .rotationBy(12f)
                        .setDuration(90)
                        .withEndAction(() -> v.animate().rotation(0f).setDuration(120).start())
                        .start();

                if (navController != null) {
                    try {
                        navController.navigate(R.id.navNotificaciones);
                    } catch (Exception ignored) {
                    }
                }
            });
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            if (navController != null) {
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                if (handled) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return handled;
            }
            return false;
        });
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

        bellAnimator = ObjectAnimator.ofFloat(btnNotificaciones, "rotation", -7f, 7f);
        bellAnimator.setDuration(420);
        bellAnimator.setRepeatCount(1);
        bellAnimator.setRepeatMode(ValueAnimator.REVERSE);
        bellAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        btnNotificaciones.postDelayed(() -> {
            if (bellAnimator != null) bellAnimator.start();
        }, 750);
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

        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            Glide.with(this)
                    .load(fotoPerfil)
                    .placeholder(R.drawable.cuenta)
                    .error(R.drawable.cuenta)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imgPerfilDrawer);
        } else {
            imgPerfilDrawer.setImageResource(R.drawable.cuenta);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarHeaderDrawer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bellAnimator != null) bellAnimator.cancel();
        cancelAnimator(glowTopX);
        cancelAnimator(glowTopY);
        cancelAnimator(glowTopAlpha);
        cancelAnimator(glowCenterX);
        cancelAnimator(glowCenterY);
        cancelAnimator(glowCenterAlpha);
        cancelAnimator(glowBottomX);
        cancelAnimator(glowBottomY);
        cancelAnimator(glowBottomAlpha);
    }

    private void cancelAnimator(ObjectAnimator animator) {
        if (animator != null) animator.cancel();
    }
}
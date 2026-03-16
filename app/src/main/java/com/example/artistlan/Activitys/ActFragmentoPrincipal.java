package com.example.artistlan.Activitys;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.example.artistlan.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class ActFragmentoPrincipal extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;
    private ImageButton btnMenuLateral;

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

        // Aquí usa la llave que realmente guardes en tu app
        String rol = prefs.getString("rol", "");
        String categoria = prefs.getString("categoria", "");
        String modo = prefs.getString("modo", "");

        String valorModo;
        if (!modo.isEmpty()) {
            valorModo = modo;
        } else if (!rol.isEmpty()) {
            valorModo = rol;
        } else if (!categoria.isEmpty()) {
            valorModo = "Artista";
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_fragmento_principal);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnMenuLateral = findViewById(R.id.btnMenuLateral);

        drawerLayout.setScrimColor(0x99000000);

        cargarHeaderDrawer();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            BottomNavigationView bottomBar = findViewById(R.id.bottomBar);
            if (bottomBar != null) {
                bottomBar.setItemIconTintList(null);
                NavigationUI.setupWithNavController(bottomBar, navController);
            }

            NavigationUI.setupWithNavController(navigationView, navController);
        }

        if (btnMenuLateral != null) {
            btnMenuLateral.setOnClickListener(v ->
                    drawerLayout.openDrawer(GravityCompat.START)
            );
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
}
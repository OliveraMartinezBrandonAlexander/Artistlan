package com.example.artistlan.Activitys;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.artistlan.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ActFragmentoPrincipal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_fragmento_principal);

        // Conexion con el navHostFragment y la barra inferior
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomBar = findViewById(R.id.bottomBar);
            if (bottomBar != null) {
                bottomBar.setItemIconTintList(null); // obliga a mostrar los PNG con sus colores originales
                NavigationUI.setupWithNavController(bottomBar, navController);
            }
        }
    }
}
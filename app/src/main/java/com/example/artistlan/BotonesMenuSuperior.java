package com.example.artistlan;

import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.artistlan.Activitys.MainActivity;

public class BotonesMenuSuperior {

    private final Fragment fragmento;
    private ImageButton btnMenuSuperior;

    public BotonesMenuSuperior(Fragment fragmento, View ruta) {
        this.fragmento = fragmento;
        inicializarMenuSuperior(ruta);
    }

    private void inicializarMenuSuperior(View ruta) {
        btnMenuSuperior = ruta.findViewById(R.id.btnMenuSuperior);
        if (btnMenuSuperior != null) {
            btnMenuSuperior.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mostrarMenuPopup(v);
                }
            });
        }
    }

    private void mostrarMenuPopup(View anchorView) {
        try {
            PopupMenu popupMenu = new PopupMenu(fragmento.requireContext(), anchorView);
            popupMenu.getMenuInflater().inflate(R.menu.menu_superior, popupMenu.getMenu());

            // Configurar gravedad para que el menú se muestre a la derecha
            popupMenu.setGravity(Gravity.END);

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.frag_historia_arte) {
                    Toast.makeText(fragmento.getContext(), "Historia del Arte...", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.frag_historia_artistlan) {
                    Toast.makeText(fragmento.getContext(), "Historia de Artislan", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.frag_cerrar_sesion) {
                    Intent irActivity = new Intent(fragmento.requireContext(), MainActivity.class);
                    fragmento.startActivity(irActivity);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        } catch (Exception e) {
            Toast.makeText(fragmento.getContext(), "Error al mostrar menú", Toast.LENGTH_SHORT).show();
        }
    }
}
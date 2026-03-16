package com.example.artistlan;

import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.artistlan.Activitys.MainActivity;

public class BotonesMenuSuperior {

    private final Fragment fragmento;
    private ImageButton btnMenuSuperior;

    public BotonesMenuSuperior(Fragment fragmento) {
        this.fragmento = fragmento;
        inicializarMenuSuperior();
    }

    private void inicializarMenuSuperior() {
        btnMenuSuperior = fragmento.requireActivity().findViewById(R.id.btnMenuSuperior);

        if (btnMenuSuperior != null) {
            btnMenuSuperior.setOnClickListener(this::mostrarMenuPopup);
        }
    }

    private void mostrarMenuPopup(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(fragmento.requireContext(), anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_superior, popupMenu.getMenu());
        popupMenu.setGravity(Gravity.END);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.frag_historia_arte) {
                Toast.makeText(fragmento.requireContext(), "Historia del Arte...", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.frag_historia_artistlan) {
                Toast.makeText(fragmento.requireContext(), "Historia de Artistlan", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.frag_cerrar_sesion) {
                Intent intent = new Intent(fragmento.requireContext(), MainActivity.class);
                fragmento.startActivity(intent);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }
}
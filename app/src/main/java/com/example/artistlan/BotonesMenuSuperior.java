package com.example.artistlan;

import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.example.artistlan.Activitys.ActFragmentoPrincipal;

public class BotonesMenuSuperior {

    private final Fragment fragmento;
    private ImageButton btnNotificaciones;

    public BotonesMenuSuperior(Fragment fragmento) {
        this.fragmento = fragmento;
        inicializarMenuSuperior();
    }

    private void inicializarMenuSuperior() {
        btnNotificaciones = fragmento.requireActivity().findViewById(R.id.btnNotificaciones);

        if (btnNotificaciones != null) {
            btnNotificaciones.setOnClickListener(v -> {
                if (fragmento.requireActivity() instanceof ActFragmentoPrincipal) {
                    ((ActFragmentoPrincipal) fragmento.requireActivity()).abrirCentroMensajes(0);
                }
            });
        }

        if (fragmento.requireActivity() instanceof ActFragmentoPrincipal) {
            ((ActFragmentoPrincipal) fragmento.requireActivity()).refrescarBadgeMensajes();
        }
    }
}

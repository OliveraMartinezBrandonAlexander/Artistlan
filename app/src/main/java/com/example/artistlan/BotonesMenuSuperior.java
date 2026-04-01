package com.example.artistlan;

import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

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
            btnNotificaciones.setOnClickListener(v ->
                    Toast.makeText(
                            fragmento.requireContext(),
                            "Abrir notificaciones",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }
    }
}
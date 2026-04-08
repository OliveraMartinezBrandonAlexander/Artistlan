package com.example.artistlan.Fragments;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FragCentroMensajes extends Fragment {

    public static final String ARG_TAB_INICIAL = "tab_inicial";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private View menuInferior;
    private ImageButton btnAtras;
    private TextView tvResumenContador;
    private int notificacionesNoLeidas = 0;
    private int solicitudesPendientes = 0;
    private long ultimoRefreshResumenMs = 0L;
    private boolean refrescoResumenEnCurso = false;
    private static final long RESUMEN_REFRESH_MIN_INTERVAL_MS = 800L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_centro_mensajes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this);

        menuInferior = requireActivity().findViewById(R.id.MenuInferior);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }

        tabLayout = view.findViewById(R.id.tabLayoutCentroMensajes);
        viewPager = view.findViewById(R.id.viewPagerCentroMensajes);
        btnAtras = view.findViewById(R.id.btnCentroMensajesAtras);
        tvResumenContador = view.findViewById(R.id.tvCentroMensajesResumenContador);
        btnAtras.setOnClickListener(v -> navegarAtrasSeguro());

        viewPager.setAdapter(new CentroMensajesPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setText(position == 0 ? "Bandeja (0)" : "Solicitudes (0)")
        ).attach();

        int tabInicial = 0;
        Bundle args = getArguments();
        if (args != null) {
            tabInicial = args.getInt(ARG_TAB_INICIAL, 0);
        }
        seleccionarTab(tabInicial);
        refrescarResumenContadores(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof com.example.artistlan.Activitys.ActFragmentoPrincipal) {
            ((com.example.artistlan.Activitys.ActFragmentoPrincipal) getActivity()).refrescarBadgeMensajes();
        }
        refrescarResumenContadores();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (menuInferior != null) {
            menuInferior.setVisibility(View.VISIBLE);
        }
    }

    public void seleccionarTab(int tabIndex) {
        if (viewPager == null) {
            return;
        }
        int safeIndex = Math.max(0, Math.min(1, tabIndex));
        viewPager.setCurrentItem(safeIndex, false);
    }

    public void refrescarResumenContadores() {
        refrescarResumenContadores(false);
    }

    public void refrescarResumenContadores(boolean forzar) {
        if (!isAdded()) {
            return;
        }
        long ahora = SystemClock.elapsedRealtime();
        if (!forzar) {
            if (refrescoResumenEnCurso) {
                return;
            }
            if (ahora - ultimoRefreshResumenMs < RESUMEN_REFRESH_MIN_INTERVAL_MS) {
                return;
            }
        }
        refrescoResumenEnCurso = true;
        ultimoRefreshResumenMs = ahora;

        android.content.SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", android.content.Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));

        MensajesBadgeManager.refrescarBadgeDetalle(idUsuario, detalle -> {
            refrescoResumenEnCurso = false;
            if (!isAdded()) {
                return;
            }
            notificacionesNoLeidas = detalle.getNotificacionesNoLeidas();
            solicitudesPendientes = detalle.getSolicitudesPendientes();
            ultimoRefreshResumenMs = SystemClock.elapsedRealtime();
            aplicarResumenContadores();
        });
    }

    private void aplicarResumenContadores() {
        if (tvResumenContador != null) {
            tvResumenContador.setText(
                    "Pendientes: " + notificacionesNoLeidas
                            + " mensajes no leidos + "
                            + solicitudesPendientes
                            + " solicitudes"
            );
        }
        if (tabLayout == null || tabLayout.getTabCount() < 2) {
            return;
        }
        com.google.android.material.tabs.TabLayout.Tab bandeja = tabLayout.getTabAt(0);
        com.google.android.material.tabs.TabLayout.Tab solicitudes = tabLayout.getTabAt(1);
        if (bandeja != null) {
            bandeja.setText("Bandeja (" + notificacionesNoLeidas + ")");
        }
        if (solicitudes != null) {
            solicitudes.setText("Solicitudes (" + solicitudesPendientes + ")");
        }
    }

    private void navegarAtrasSeguro() {
        if (!isAdded()) {
            return;
        }
        try {
            if (!NavHostFragment.findNavController(this).navigateUp()) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        } catch (IllegalStateException ignored) {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }

    private static class CentroMensajesPagerAdapter extends FragmentStateAdapter {

        CentroMensajesPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new FragBandejaMensajes() : new FragSolicitudesMensajes();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}

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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.appcompat.widget.PopupMenu;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.artistlan.Activitys.ActFragmentoPrincipal;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FragCentroMensajes extends Fragment {

    public static final String ARG_TAB_INICIAL = "tab_inicial";
    public static final String ARG_SOLICITUDES_MODO = "solicitudes_modo";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private View menuInferior;
    private ImageButton btnAtras;
    private ImageButton btnMenuOpciones;
    private TextView tvResumenContador;
    private int notificacionesNoLeidas = 0;
    private int solicitudesPendientes = 0;
    private long ultimoRefreshResumenMs = 0L;
    private boolean refrescoResumenEnCurso = false;
    private static final long RESUMEN_REFRESH_MIN_INTERVAL_MS = 800L;
    private CentroMensajesPagerAdapter pagerAdapter;
    private int modoSolicitudesPendiente = FragSolicitudesMensajes.MODO_RECIBIDAS;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

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
        btnMenuOpciones = view.findViewById(R.id.btnCentroMensajesMenu);
        tvResumenContador = view.findViewById(R.id.tvCentroMensajesResumenContador);
        btnAtras.setOnClickListener(v -> navegarAtrasSeguro());
        btnMenuOpciones.setOnClickListener(this::mostrarMenuAcciones);

        pagerAdapter = new CentroMensajesPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 1) {
                    FragSolicitudesMensajes solicitudes = getSolicitudesFragmentActual();
                    if (solicitudes != null) {
                        solicitudes.seleccionarModoExterno(modoSolicitudesPendiente);
                    }
                }
            }
        };
        viewPager.registerOnPageChangeCallback(pageChangeCallback);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setText(position == 0 ? "Mensajes (0)" : "Solicitudes (0)")
        ).attach();

        int tabInicial = 0;
        Bundle args = getArguments();
        if (args != null) {
            tabInicial = args.getInt(ARG_TAB_INICIAL, 0);
            modoSolicitudesPendiente = args.getInt(ARG_SOLICITUDES_MODO, FragSolicitudesMensajes.MODO_RECIBIDAS);
        }
        seleccionarTab(tabInicial);
        refrescarResumenContadores(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }
        if (getActivity() instanceof com.example.artistlan.Activitys.ActFragmentoPrincipal) {
            ((com.example.artistlan.Activitys.ActFragmentoPrincipal) getActivity()).refrescarBadgeMensajes();
        }
        refrescarResumenContadores();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewPager != null && pageChangeCallback != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        pageChangeCallback = null;
        if (viewPager != null) {
            viewPager.setAdapter(null);
        }
        pagerAdapter = null;
        tabLayout = null;
        viewPager = null;
        btnAtras = null;
        btnMenuOpciones = null;
        tvResumenContador = null;
        if (menuInferior != null) {
            menuInferior.setVisibility(View.VISIBLE);
        }
        menuInferior = null;
    }

    public void seleccionarTab(int tabIndex) {
        if (viewPager == null) {
            return;
        }
        int safeIndex = Math.max(0, Math.min(1, tabIndex));
        viewPager.setCurrentItem(safeIndex, false);
    }

    public void seleccionarModoSolicitudes(int modo) {
        modoSolicitudesPendiente = (modo == FragSolicitudesMensajes.MODO_ENVIADAS)
                ? FragSolicitudesMensajes.MODO_ENVIADAS
                : FragSolicitudesMensajes.MODO_RECIBIDAS;
        FragSolicitudesMensajes solicitudes = getSolicitudesFragmentActual();
        if (solicitudes != null) {
            solicitudes.seleccionarModoExterno(modoSolicitudesPendiente);
        }
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
            bandeja.setText("Mensajes (" + notificacionesNoLeidas + ")");
        }
        if (solicitudes != null) {
            solicitudes.setText("Solicitudes (" + solicitudesPendientes + ")");
        }
    }

    private void onMarcarTodoClick() {
        if (viewPager == null || viewPager.getCurrentItem() != 0) {
            return;
        }
        FragBandejaMensajes bandeja = getBandejaFragmentActual();
        if (bandeja != null) {
            bandeja.marcarTodasDesdeHeader();
        }
    }

    private void onActualizarClick() {
        if (viewPager == null) {
            return;
        }
        if (viewPager.getCurrentItem() == 0) {
            FragBandejaMensajes bandeja = getBandejaFragmentActual();
            if (bandeja != null) {
                bandeja.recargarDesdeHeader();
            }
        } else {
            FragSolicitudesMensajes solicitudes = getSolicitudesFragmentActual();
            if (solicitudes != null) {
                solicitudes.recargarDesdeHeader();
            }
        }
    }

    private void mostrarMenuAcciones(View anchor) {
        if (!isAdded()) {
            return;
        }
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        final int MENU_MARCAR_TODO = 1;
        final int MENU_ACTUALIZAR = 2;

        boolean enMensajes = viewPager != null && viewPager.getCurrentItem() == 0;
        if (enMensajes) {
            popup.getMenu().add(0, MENU_MARCAR_TODO, 0, "Marcar todo como leido");
        }
        popup.getMenu().add(0, MENU_ACTUALIZAR, 1, "Actualizar");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == MENU_MARCAR_TODO) {
                onMarcarTodoClick();
                return true;
            }
            if (id == MENU_ACTUALIZAR) {
                onActualizarClick();
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Nullable
    private FragBandejaMensajes getBandejaFragmentActual() {
        for (Fragment child : getChildFragmentManager().getFragments()) {
            if (child instanceof FragBandejaMensajes) {
                return (FragBandejaMensajes) child;
            }
        }
        return null;
    }

    @Nullable
    private FragSolicitudesMensajes getSolicitudesFragmentActual() {
        for (Fragment child : getChildFragmentManager().getFragments()) {
            if (child instanceof FragSolicitudesMensajes) {
                return (FragSolicitudesMensajes) child;
            }
        }
        return null;
    }

    private void navegarAtrasSeguro() {
        if (!isAdded()) {
            return;
        }

        NavController navController = null;
        try {
            navController = NavHostFragment.findNavController(this);
            if (navController.popBackStack()) {
                return;
            }
        } catch (Exception ignored) {
            // fallback abajo
        }

        if (getActivity() instanceof ActFragmentoPrincipal) {
            ((ActFragmentoPrincipal) getActivity()).navegarDesdeCentroMensajes(R.id.fragExplorar, null);
            return;
        }

        try {
            if (navController != null && navController.navigateUp()) {
                return;
            }
        } catch (Exception ignored) {
            // fallback final
        }

        requireActivity().finish();
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

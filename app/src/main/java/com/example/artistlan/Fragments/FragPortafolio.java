package com.example.artistlan.Fragments;

import android.os.Bundle;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class FragPortafolio extends Fragment {

    private ViewPager2 viewPager;
    private View segmentContainer;
    private View segmentIndicator;
    private Button btnSegmentMisObras;
    private Button btnSegmentMisServicios;
    private FloatingActionsMenu fabMenu;
    private FloatingActionButton fabSubirObra, fabSubirServicio;
    private ThemeManager themeManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_frag_portafolio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);
        themeManager = new ThemeManager(requireContext());

        new com.example.artistlan.BotonesMenuSuperior(this);

        viewPager = view.findViewById(R.id.viewPagerPortafolio);
        segmentContainer = view.findViewById(R.id.segmentContainerPortafolio);
        segmentIndicator = view.findViewById(R.id.segmentIndicatorPortafolio);
        btnSegmentMisObras = view.findViewById(R.id.btnSegmentMisObras);
        btnSegmentMisServicios = view.findViewById(R.id.btnSegmentMisServicios);
        fabMenu = view.findViewById(R.id.fabMenuSubir);
        fabSubirObra = view.findViewById(R.id.fabSubirObraMenu);
        fabSubirServicio = view.findViewById(R.id.fabSubirServicioMenu);

        viewPager.setAdapter(new PortafolioPagerAdapter(this));
        aplicarTemaSelector();
        configurarSelector();

        fabSubirObra.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.fragSubirObra);
            fabMenu.collapse();
        });

        fabSubirServicio.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.fragSubirServicio);
            fabMenu.collapse();
        });
    }

    private void configurarSelector() {
        btnSegmentMisObras.setOnClickListener(v -> seleccionarSeccion(0, true));
        btnSegmentMisServicios.setOnClickListener(v -> seleccionarSeccion(1, true));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                seleccionarSeccion(position, false);
            }
        });
        segmentContainer.post(() -> seleccionarSeccion(viewPager.getCurrentItem(), false));
    }

    private void seleccionarSeccion(int position, boolean desdeClick) {
        int clampedPosition = Math.max(0, Math.min(1, position));
        if (desdeClick && viewPager.getCurrentItem() != clampedPosition) {
            viewPager.setCurrentItem(clampedPosition, true);
        }
        moverIndicador(clampedPosition, true);
        aplicarEstadoBotones(clampedPosition);
    }

    private void aplicarTemaSelector() {
        if (themeManager == null) {
            return;
        }
        if (segmentContainer != null && segmentContainer.getBackground() != null) {
            segmentContainer.getBackground().setColorFilter(
                    themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL),
                    PorterDuff.Mode.SRC_ATOP
            );
        }
        if (segmentIndicator != null && segmentIndicator.getBackground() != null) {
            segmentIndicator.getBackground().setColorFilter(
                    themeManager.color(ThemeKeys.ACCENT_PRIMARY),
                    PorterDuff.Mode.SRC_ATOP
            );
        }
        aplicarEstadoBotones(viewPager != null ? viewPager.getCurrentItem() : 0);
    }

    private void aplicarEstadoBotones(int position) {
        if (themeManager == null) {
            return;
        }
        int selected = themeManager.color(ThemeKeys.TEXT_PRIMARY);
        int unselected = themeManager.color(ThemeKeys.TEXT_SECONDARY);
        if (btnSegmentMisObras != null) {
            btnSegmentMisObras.setTextColor(position == 0 ? selected : unselected);
        }
        if (btnSegmentMisServicios != null) {
            btnSegmentMisServicios.setTextColor(position == 1 ? selected : unselected);
        }
    }

    private void moverIndicador(int position, boolean animar) {
        if (segmentContainer == null || segmentIndicator == null) {
            return;
        }
        int width = segmentContainer.getWidth();
        if (width <= 0) {
            return;
        }
        int innerWidth = width - segmentContainer.getPaddingLeft() - segmentContainer.getPaddingRight();
        int segmentWidth = innerWidth / 2;
        float targetX = segmentContainer.getPaddingLeft() + (segmentWidth * position);
        ViewGroup.LayoutParams params = segmentIndicator.getLayoutParams();
        if (params.width != segmentWidth) {
            params.width = segmentWidth;
            segmentIndicator.setLayoutParams(params);
        }
        segmentIndicator.animate().cancel();
        if (animar) {
            segmentIndicator.animate().x(targetX).setDuration(220).start();
        } else {
            segmentIndicator.setX(targetX);
        }
    }

    private static class PortafolioPagerAdapter extends FragmentStateAdapter {
        public PortafolioPagerAdapter(@NonNull Fragment fragment) { super(fragment); }
        @NonNull @Override public Fragment createFragment(int position) {
            return position == 0 ? new FragMiArte() : new FragMisServicios();
        }
        @Override public int getItemCount() { return 2; }
    }
}

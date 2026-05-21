package com.example.artistlan.Fragments;

import android.os.Bundle;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class FragPortafolio extends Fragment {
    public static final String RESULT_KEY_PORTAFOLIO_REFRESH = "result_key_portafolio_refresh";
    public static final String RESULT_EXTRA_TARGET = "result_extra_target";
    public static final String RESULT_EXTRA_GUARDADO = "result_extra_guardado";
    public static final String RESULT_EXTRA_MODO = "result_extra_modo";
    public static final String TARGET_OBRAS = "obras";
    public static final String TARGET_SERVICIOS = "servicios";
    private static final String TAG_BACK_STACK = "MiArteBackStackDebug";
    private static int selectedTabCache = 0;


    private ViewPager2 viewPager;
    private View rootView;
    private View portafolioHeader;
    private View topBarFrame;
    private View bottomBarFrame;
    private View segmentContainer;
    private View segmentIndicator;
    private Button btnSegmentMisObras;
    private Button btnSegmentMisServicios;
    private FloatingActionsMenu fabMenu;
    private FloatingActionButton fabSubirObra, fabSubirServicio;
    private ThemeManager themeManager;
    private ViewTreeObserver.OnPreDrawListener overlayInsetsListener;

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

        rootView = view;
        viewPager = view.findViewById(R.id.viewPagerPortafolio);
        portafolioHeader = view.findViewById(R.id.portafolioHeader);
        segmentContainer = view.findViewById(R.id.segmentContainerPortafolio);
        segmentIndicator = view.findViewById(R.id.segmentIndicatorPortafolio);
        btnSegmentMisObras = view.findViewById(R.id.btnSegmentMisObras);
        btnSegmentMisServicios = view.findViewById(R.id.btnSegmentMisServicios);
        fabMenu = view.findViewById(R.id.fabMenuSubir);
        fabSubirObra = view.findViewById(R.id.fabSubirObraMenu);
        fabSubirServicio = view.findViewById(R.id.fabSubirServicioMenu);

        viewPager.setAdapter(new PortafolioPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);
        viewPager.setClipToPadding(false);
        viewPager.setCurrentItem(selectedTabCache, false);
        Log.d(TAG_BACK_STACK, "FragPortafolio onViewCreated selectedTabRestaurado=" + selectedTabCache);
        aplicarTemaSelector();
        configurarSelector();
        configurarInsetsOverlay();
        observarRefreshDesdeSubidas();

        fabSubirObra.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.fragSubirObra);
            fabMenu.collapse();
        });

        fabSubirServicio.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.fragSubirServicio);
            fabMenu.collapse();
        });
    }

    @Override
    public void onDestroyView() {
        if (rootView != null && overlayInsetsListener != null) {
            ViewTreeObserver observer = rootView.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.removeOnPreDrawListener(overlayInsetsListener);
            }
        }
        overlayInsetsListener = null;
        rootView = null;
        portafolioHeader = null;
        topBarFrame = null;
        bottomBarFrame = null;
        super.onDestroyView();
    }

    private void configurarInsetsOverlay() {
        if (rootView == null || viewPager == null || portafolioHeader == null) {
            return;
        }
        topBarFrame = requireActivity().findViewById(R.id.topBarFrame);
        bottomBarFrame = requireActivity().findViewById(R.id.MenuInferiorFrame);
        overlayInsetsListener = () -> {
            actualizarInsetsOverlay();
            return true;
        };
        rootView.getViewTreeObserver().addOnPreDrawListener(overlayInsetsListener);
        rootView.post(this::actualizarInsetsOverlay);
    }

    private void actualizarInsetsOverlay() {
        if (viewPager == null || portafolioHeader == null) {
            return;
        }
        int visibleTopBar = calcularVisibleTopBar();
        int visibleBottomBar = calcularVisibleBottomBar();
        int headerHeight = Math.max(portafolioHeader.getHeight(), dpToPx(54));
        int headerTop = visibleTopBar > 0 ? visibleTopBar : calcularSafeTopInset();
        int topPadding = headerTop + headerHeight + dpToPx(8);
        int bottomPadding = visibleBottomBar;

        if (portafolioHeader.getTranslationY() != headerTop) {
            portafolioHeader.setTranslationY(headerTop);
        }
        if (viewPager.getPaddingTop() != topPadding || viewPager.getPaddingBottom() != bottomPadding) {
            viewPager.setPadding(0, topPadding, 0, bottomPadding);
        }
    }

    private int calcularVisibleTopBar() {
        if (topBarFrame == null) {
            return 0;
        }
        int height = topBarFrame.getHeight();
        return Math.max(0, height + Math.round(topBarFrame.getTranslationY()));
    }

    private int calcularVisibleBottomBar() {
        if (bottomBarFrame == null) {
            return 0;
        }
        int height = bottomBarFrame.getHeight();
        return Math.max(0, height - Math.round(bottomBarFrame.getTranslationY()));
    }

    private int calcularSafeTopInset() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
        return Math.max(statusBarHeight, dpToPx(24)) + dpToPx(8);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void observarRefreshDesdeSubidas() {
        NavBackStackEntry backStackEntry = NavHostFragment.findNavController(this).getCurrentBackStackEntry();
        if (backStackEntry == null) {
            return;
        }

        LiveData<String> refreshTarget = backStackEntry.getSavedStateHandle().getLiveData(RESULT_EXTRA_TARGET);
        refreshTarget.observe(getViewLifecycleOwner(), target -> {
            if (target == null || target.trim().isEmpty()) {
                return;
            }
            Boolean guardado = backStackEntry.getSavedStateHandle().get(RESULT_EXTRA_GUARDADO);
            String modo = backStackEntry.getSavedStateHandle().get(RESULT_EXTRA_MODO);
            Log.d(TAG_BACK_STACK, "Resultado regreso desde subir/editar target=" + target
                    + " guardado=" + guardado
                    + " modo=" + modo
                    + " selectedTab=" + selectedTabCache);
            reenviarRefreshASeccion(target, Boolean.TRUE.equals(guardado), modo);
            backStackEntry.getSavedStateHandle().remove(RESULT_EXTRA_TARGET);
            backStackEntry.getSavedStateHandle().remove(RESULT_EXTRA_GUARDADO);
            backStackEntry.getSavedStateHandle().remove(RESULT_EXTRA_MODO);
        });
    }

    private void reenviarRefreshASeccion(@NonNull String target, boolean guardado, @Nullable String modo) {
        if (!TARGET_OBRAS.equals(target) && !TARGET_SERVICIOS.equals(target)) {
            return;
        }
        if (guardado) {
            selectedTabCache = TARGET_SERVICIOS.equals(target) ? 1 : 0;
            if (viewPager != null && viewPager.getCurrentItem() != selectedTabCache) {
                viewPager.setCurrentItem(selectedTabCache, false);
            }
        }
        Log.d(TAG_BACK_STACK, "Reenviar resultado a hijos target=" + target
                + " guardado=" + guardado
                + " modo=" + modo
                + " selectedTab=" + selectedTabCache);
        Bundle result = new Bundle();
        result.putString(RESULT_EXTRA_TARGET, target);
        result.putBoolean(RESULT_EXTRA_GUARDADO, guardado);
        result.putString(RESULT_EXTRA_MODO, modo != null ? modo : "");
        getChildFragmentManager().setFragmentResult(RESULT_KEY_PORTAFOLIO_REFRESH, result);
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
        selectedTabCache = clampedPosition;
        Log.d(TAG_BACK_STACK, "Seleccion portafolio selectedTab=" + selectedTabCache + " desdeClick=" + desdeClick);
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
        aplicarTemaFabSubida();
        aplicarEstadoBotones(viewPager != null ? viewPager.getCurrentItem() : 0);
    }

    private void aplicarTemaFabSubida() {
        int primary = themeManager.color(ThemeKeys.BUTTON_PRIMARY_BG);
        int secondary = themeManager.color(ThemeKeys.BUTTON_SECONDARY_BG);
        int icon = themeManager.color(ThemeKeys.ICON_ACTIVE);
        aplicarColorFabPorReflexion(fabMenu, "setAddButtonColorNormal", primary);
        aplicarColorFabPorReflexion(fabMenu, "setAddButtonColorPressed", secondary);
        aplicarColorFabPorReflexion(fabSubirObra, "setColorNormal", themeManager.color(ThemeKeys.FILTER_BUTTON_BG));
        aplicarColorFabPorReflexion(fabSubirObra, "setColorPressed", themeManager.color(ThemeKeys.ACCENT_PRIMARY));
        aplicarColorFabPorReflexion(fabSubirServicio, "setColorNormal", themeManager.color(ThemeKeys.FILTER_BUTTON_BG));
        aplicarColorFabPorReflexion(fabSubirServicio, "setColorPressed", themeManager.color(ThemeKeys.ACCENT_PRIMARY));
        if (fabSubirObra != null) {
            fabSubirObra.setColorFilter(icon, PorterDuff.Mode.SRC_IN);
        }
        if (fabSubirServicio != null) {
            fabSubirServicio.setColorFilter(icon, PorterDuff.Mode.SRC_IN);
        }
    }

    private void aplicarColorFabPorReflexion(@Nullable Object target, @NonNull String methodName, int color) {
        if (target == null) {
            return;
        }
        try {
            java.lang.reflect.Method method = target.getClass().getMethod(methodName, int.class);
            method.invoke(target, color);
        } catch (Exception ignored) {
            // La libreria de FAB varia entre versiones; si no expone el setter, conserva el XML.
        }
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

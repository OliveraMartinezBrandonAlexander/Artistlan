package com.example.artistlan.Fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.utils.CardThemeHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class FragPortafolio extends Fragment {
    public static final String RESULT_KEY_PORTAFOLIO_REFRESH = "result_key_portafolio_refresh";
    public static final String RESULT_EXTRA_TARGET = "result_extra_target";
    public static final String RESULT_EXTRA_GUARDADO = "result_extra_guardado";
    public static final String RESULT_EXTRA_MODO = "result_extra_modo";
    public static final String TARGET_OBRAS = "obras";
    public static final String TARGET_SERVICIOS = "servicios";
    private static final String TAG_BACK_STACK = "MiArteBackStackDebug";
    private static int selectedTabCache = 0;
    private static boolean refreshPendienteObras = false;
    private static boolean refreshPendienteServicios = false;


    private ViewPager2 viewPager;
    private View rootView;
    private View portafolioHeader;
    private View bottomBarFrame;
    private View segmentContainer;
    private View segmentIndicator;
    private Button btnSegmentMisObras;
    private Button btnSegmentMisServicios;
    private FloatingActionButton fabSubirPortafolio;
    private LinearLayout fabUploadPanel;
    private View fabDismissOverlay;
    private View optionSubirObra;
    private View optionSubirServicio;
    private View fabUploadDivider;
    private ImageView iconSubirObra;
    private ImageView iconSubirServicio;
    private TextView txtSubirObra;
    private TextView txtSubirServicio;
    private ThemeManager themeManager;
    private ViewTreeObserver.OnPreDrawListener overlayInsetsListener;
    private boolean uploadPanelOpen = false;
    private boolean bottomMenuVisibleForFab = true;

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
        fabSubirPortafolio = view.findViewById(R.id.fabSubirPortafolio);
        fabUploadPanel = view.findViewById(R.id.fabUploadPanel);
        fabDismissOverlay = view.findViewById(R.id.fabDismissOverlay);
        optionSubirObra = view.findViewById(R.id.optionSubirObra);
        optionSubirServicio = view.findViewById(R.id.optionSubirServicio);
        fabUploadDivider = view.findViewById(R.id.fabUploadDivider);
        iconSubirObra = view.findViewById(R.id.iconSubirObra);
        iconSubirServicio = view.findViewById(R.id.iconSubirServicio);
        txtSubirObra = view.findViewById(R.id.txtSubirObra);
        txtSubirServicio = view.findViewById(R.id.txtSubirServicio);

        viewPager.setAdapter(new PortafolioPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);
        viewPager.setClipToPadding(false);
        portafolioHeader.bringToFront();
        viewPager.setCurrentItem(selectedTabCache, false);
        Log.d(TAG_BACK_STACK, "FragPortafolio onViewCreated selectedTabRestaurado=" + selectedTabCache);
        aplicarTemaSelector();
        configurarSelector();
        configurarInsetsOverlay();
        observarRefreshDesdeSubidas();
        view.post(this::ensureDataLoadedForCurrentTab);

        fabSubirPortafolio.setOnClickListener(v -> setUploadPanelOpen(!uploadPanelOpen, true));
        fabDismissOverlay.setOnClickListener(v -> setUploadPanelOpen(false, true));

        optionSubirObra.setOnClickListener(v -> {
            setUploadPanelOpen(false, false);
            Navigation.findNavController(view).navigate(R.id.fragSubirObra);
        });

        optionSubirServicio.setOnClickListener(v -> {
            setUploadPanelOpen(false, false);
            Navigation.findNavController(view).navigate(R.id.fragSubirServicio);
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
        bottomBarFrame = null;
        uploadPanelOpen = false;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isAdded()) {
            return;
        }
        themeManager = new ThemeManager(requireContext());
        aplicarTemaSelector();
        refrescarTemaTabsCargados();
    }

    private void configurarInsetsOverlay() {
        if (rootView == null || viewPager == null || portafolioHeader == null) {
            return;
        }
        bottomBarFrame = requireActivity().findViewById(R.id.MenuInferiorFrame);
        overlayInsetsListener = () -> {
            actualizarInsetsOverlay();
            return true;
        };
        rootView.getViewTreeObserver().addOnPreDrawListener(overlayInsetsListener);
        rootView.post(this::actualizarInsetsOverlay);
    }

    private void actualizarInsetsOverlay() {
        if (fabSubirPortafolio == null || fabUploadPanel == null) {
            return;
        }
        boolean bottomVisible = isBottomBarVisuallyVisible();
        if (bottomVisible == bottomMenuVisibleForFab) {
            return;
        }
        bottomMenuVisibleForFab = bottomVisible;
        float targetTranslationY = bottomVisible ? 0f : dpToPx(70);
        animarControlesFlotantes(targetTranslationY);
    }

    private boolean isBottomBarVisuallyVisible() {
        if (bottomBarFrame == null) {
            return true;
        }
        int height = bottomBarFrame.getHeight();
        if (height <= 0) {
            return true;
        }
        return bottomBarFrame.getTranslationY() < height * 0.45f;
    }

    private void animarControlesFlotantes(float targetTranslationY) {
        animarTranslationYSiCambio(fabSubirPortafolio, targetTranslationY);
        animarTranslationYSiCambio(fabUploadPanel, targetTranslationY);
    }

    private void animarTranslationYSiCambio(@Nullable View view, float targetTranslationY) {
        if (view == null || Math.abs(view.getTranslationY() - targetTranslationY) < 1f) {
            return;
        }
        view.animate()
                .translationY(targetTranslationY)
                .setDuration(180)
                .start();
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
            marcarRefreshPendiente(target);
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

    public static synchronized void marcarRefreshPendiente(@NonNull String target) {
        if (TARGET_OBRAS.equals(target)) {
            refreshPendienteObras = true;
            return;
        }
        if (TARGET_SERVICIOS.equals(target)) {
            refreshPendienteServicios = true;
        }
    }

    public static synchronized boolean hasRefreshPendiente(@NonNull String target) {
        if (TARGET_OBRAS.equals(target)) {
            return refreshPendienteObras;
        }
        if (TARGET_SERVICIOS.equals(target)) {
            return refreshPendienteServicios;
        }
        return false;
    }

    public static synchronized void limpiarRefreshPendiente(@NonNull String target) {
        if (TARGET_OBRAS.equals(target)) {
            refreshPendienteObras = false;
            return;
        }
        if (TARGET_SERVICIOS.equals(target)) {
            refreshPendienteServicios = false;
        }
    }

    public static synchronized boolean consumirRefreshPendiente(@NonNull String target) {
        boolean value = hasRefreshPendiente(target);
        if (value) {
            limpiarRefreshPendiente(target);
        }
        return value;
    }

    private void configurarSelector() {
        btnSegmentMisObras.setOnClickListener(v -> seleccionarSeccion(0, true));
        btnSegmentMisServicios.setOnClickListener(v -> seleccionarSeccion(1, true));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                seleccionarSeccion(position, false);
                ensureDataLoadedForCurrentTab();
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
        if (fabSubirPortafolio != null) {
            fabSubirPortafolio.setBackgroundTintList(ColorStateList.valueOf(themeManager.color(ThemeKeys.BUTTON_PRIMARY_BG)));
            fabSubirPortafolio.setImageTintList(ColorStateList.valueOf(themeManager.color(ThemeKeys.BUTTON_TEXT_DARK)));
            fabSubirPortafolio.setRippleColor(themeManager.color(ThemeKeys.BUTTON_SECONDARY_BG));
        }
        CardThemeHelper.applyFilterSurface(fabUploadPanel, themeManager);
        int panelColor = ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_BG), 205);
        int contrastPanelColor = ColorUtils.compositeColors(panelColor, ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.BG_MID), 255));
        int textColor = elegirColorTemaLegiblePreferido(contrastPanelColor,
                themeManager.color(ThemeKeys.TEXT_SECONDARY),
                themeManager.color(ThemeKeys.TEXT_PRIMARY),
                themeManager.color(ThemeKeys.BUTTON_TEXT_DARK),
                themeManager.color(ThemeKeys.CARD_BORDER));
        int iconColor = elegirColorLegible(contrastPanelColor,
                themeManager.color(ThemeKeys.ICON_ACTIVE),
                themeManager.color(ThemeKeys.BUTTON_PRIMARY_BG),
                textColor,
                themeManager.color(ThemeKeys.CARD_BORDER),
                Color.WHITE,
                Color.BLACK);
        if (fabUploadPanel != null) {
            GradientDrawable panelBg = new GradientDrawable();
            panelBg.setShape(GradientDrawable.RECTANGLE);
            panelBg.setCornerRadius(dpToPx(16));
            panelBg.setColor(panelColor);
            panelBg.setStroke(dpToPx(1), themeManager.color(ThemeKeys.CARD_BORDER));
            fabUploadPanel.setBackground(panelBg);
        }
        if (fabUploadDivider != null) {
            fabUploadDivider.setBackgroundColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_BORDER), 120));
        }
        aplicarColorTexto(txtSubirObra, textColor);
        aplicarColorTexto(txtSubirServicio, textColor);
        tintIcon(iconSubirObra, iconColor);
        tintIcon(iconSubirServicio, iconColor);
        ThemeApplier.animatePress(fabSubirPortafolio);
        ThemeApplier.animatePress(optionSubirObra);
        ThemeApplier.animatePress(optionSubirServicio);
    }

    private void aplicarColorTexto(@Nullable TextView textView, int color) {
        if (textView != null) {
            textView.setTextColor(color);
        }
    }

    private void tintIcon(@Nullable ImageView iconView, int color) {
        if (iconView != null) {
            iconView.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    private int elegirColorLegible(int backgroundColor, int... candidates) {
        int selected = candidates.length > 0 ? candidates[0] : Color.WHITE;
        double bestContrast = -1;
        for (int candidate : candidates) {
            double contrast = ColorUtils.calculateContrast(candidate, backgroundColor);
            if (contrast > bestContrast) {
                bestContrast = contrast;
                selected = candidate;
            }
        }
        return selected;
    }

    private int elegirColorTemaLegiblePreferido(int backgroundColor, int preferred, int... themeCandidates) {
        if (ColorUtils.calculateContrast(preferred, backgroundColor) >= 3.0) {
            return preferred;
        }
        int selected = elegirColorLegible(backgroundColor, themeCandidates);
        if (ColorUtils.calculateContrast(selected, backgroundColor) >= 3.0) {
            return selected;
        }
        return elegirColorLegible(backgroundColor, selected, Color.WHITE, Color.BLACK);
    }

    private void setUploadPanelOpen(boolean open, boolean animate) {
        uploadPanelOpen = open;
        animarFabSubida(open, animate);
        if (fabDismissOverlay != null) {
            fabDismissOverlay.setVisibility(open ? View.VISIBLE : View.GONE);
        }
        if (fabUploadPanel == null) {
            return;
        }
        fabUploadPanel.animate().cancel();
        if (open) {
            fabUploadPanel.bringToFront();
            if (fabSubirPortafolio != null) {
                fabSubirPortafolio.bringToFront();
            }
            fabUploadPanel.setVisibility(View.VISIBLE);
            if (animate) {
                fabUploadPanel.setAlpha(0f);
                fabUploadPanel.setScaleX(0.96f);
                fabUploadPanel.setScaleY(0.96f);
                fabUploadPanel.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(140).start();
            } else {
                fabUploadPanel.setAlpha(1f);
                fabUploadPanel.setScaleX(1f);
                fabUploadPanel.setScaleY(1f);
            }
            return;
        }
        if (animate && fabUploadPanel.getVisibility() == View.VISIBLE) {
            fabUploadPanel.animate()
                    .alpha(0f)
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(110)
                    .withEndAction(() -> {
                        if (!uploadPanelOpen && fabUploadPanel != null) {
                            fabUploadPanel.setVisibility(View.GONE);
                        }
                    })
                    .start();
        } else {
            fabUploadPanel.setVisibility(View.GONE);
            fabUploadPanel.setAlpha(1f);
            fabUploadPanel.setScaleX(1f);
            fabUploadPanel.setScaleY(1f);
        }
    }

    private void animarFabSubida(boolean open, boolean animate) {
        if (fabSubirPortafolio == null) {
            return;
        }
        float targetRotation = open ? 45f : 0f;
        float targetScale = open ? 1.06f : 1f;
        if (!animate) {
            fabSubirPortafolio.setRotation(targetRotation);
            fabSubirPortafolio.setScaleX(targetScale);
            fabSubirPortafolio.setScaleY(targetScale);
            return;
        }
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(fabSubirPortafolio, View.ROTATION, targetRotation),
                ObjectAnimator.ofFloat(fabSubirPortafolio, View.SCALE_X, targetScale),
                ObjectAnimator.ofFloat(fabSubirPortafolio, View.SCALE_Y, targetScale)
        );
        set.setDuration(160);
        set.start();
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

    private void ensureDataLoadedForCurrentTab() {
        if (!isAdded() || viewPager == null) {
            return;
        }
        int currentIndex = viewPager.getCurrentItem();
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment == null) {
                continue;
            }
            if (currentIndex == 0 && fragment instanceof FragMiArte) {
                ((FragMiArte) fragment).ensureDataLoadedForCurrentState();
                return;
            }
            if (currentIndex == 1 && fragment instanceof FragMisServicios) {
                ((FragMisServicios) fragment).ensureDataLoadedForCurrentState();
                return;
            }
        }
    }

    private void refrescarTemaTabsCargados() {
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof FragMiArte) {
                ((FragMiArte) fragment).refreshThemeOnly();
            } else if (fragment instanceof FragMisServicios) {
                ((FragMisServicios) fragment).refreshThemeOnly();
            }
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

package com.example.artistlan.Fragments;

import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.graphics.ColorUtils;

import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.CardThemeHelper;

public class FragHistoriaArtistlan extends Fragment {

    private ScrollView scrollHistoriaArtistlan;
    private View rootHistoriaArtistlan;
    private ObjectAnimator autoScrollAnimator;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean usuarioTocando = false;

    public FragHistoriaArtistlan() {
        super(R.layout.fragment_frag_historia_artistlan);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootHistoriaArtistlan = view;
        scrollHistoriaArtistlan = view.findViewById(R.id.scrollHistoriaArtistlan);

        aplicarTemaRapido();
        configurarScrollAutomatico();
    }

    private void configurarScrollAutomatico() {
        if (scrollHistoriaArtistlan == null) return;

        scrollHistoriaArtistlan.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    usuarioTocando = true;
                    pausarScrollAutomatico();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    usuarioTocando = false;
                    reanudarScrollAutomaticoConRetraso();
                    break;
            }
            return false;
        });

        scrollHistoriaArtistlan.postDelayed(this::iniciarScrollAutomatico, 900);
    }

    private void iniciarScrollAutomatico() {
        if (scrollHistoriaArtistlan == null || usuarioTocando || getContext() == null) return;

        View child = scrollHistoriaArtistlan.getChildAt(0);
        if (child == null) return;

        int maxScroll = child.getHeight() - scrollHistoriaArtistlan.getHeight();
        if (maxScroll <= 0) return;

        int scrollActual = scrollHistoriaArtistlan.getScrollY();
        int distanciaRestante = maxScroll - scrollActual;

        if (distanciaRestante <= 0) {
            handler.postDelayed(() -> {
                if (scrollHistoriaArtistlan != null && getContext() != null) {
                    scrollHistoriaArtistlan.smoothScrollTo(0, 0);
                    handler.postDelayed(this::iniciarScrollAutomatico, 1200);
                }
            }, 1200);
            return;
        }

        long duracion = Math.max(9000, distanciaRestante * 45L);

        autoScrollAnimator = ObjectAnimator.ofInt(
                scrollHistoriaArtistlan,
                "scrollY",
                scrollActual,
                maxScroll
        );

        autoScrollAnimator.setDuration(duracion);
        autoScrollAnimator.setInterpolator(new LinearInterpolator());
        autoScrollAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (!usuarioTocando && scrollHistoriaArtistlan != null) {
                    handler.postDelayed(() -> {
                        if (scrollHistoriaArtistlan != null && !usuarioTocando && getContext() != null) {
                            scrollHistoriaArtistlan.smoothScrollTo(0, 0);
                            handler.postDelayed(FragHistoriaArtistlan.this::iniciarScrollAutomatico, 1300);
                        }
                    }, 1300);
                }
            }
        });

        autoScrollAnimator.start();
    }

    private void pausarScrollAutomatico() {
        if (autoScrollAnimator != null && autoScrollAnimator.isRunning()) {
            autoScrollAnimator.cancel();
        }
        handler.removeCallbacksAndMessages(null);
    }

    private void reanudarScrollAutomaticoConRetraso() {
        handler.postDelayed(() -> {
            if (getContext() != null) {
                iniciarScrollAutomatico();
            }
        }, 2500);
    }

    private void aplicarTemaRapido() {
        if (rootHistoriaArtistlan == null || getContext() == null) {
            return;
        }
        ThemeManager tm = new ThemeManager(getContext());
        View root = rootHistoriaArtistlan.findViewById(R.id.rootHistoriaArtistlan);
        ThemeApplier.applyFragmentBackground(root, tm, null, null, null);
        ThemeApplier.applyTextPrimary(rootHistoriaArtistlan.findViewById(R.id.tvTituloHistoriaArtistlan), tm);
        ThemeApplier.applyTextSecondary(rootHistoriaArtistlan.findViewById(R.id.tvSubtituloHistoriaArtistlan), tm);
        ThemeApplier.applyTextPrimary(rootHistoriaArtistlan.findViewById(R.id.tvCierreHistoriaArtistlan), tm);

        for (int cardId : cardIds()) {
            LinearLayout card = rootHistoriaArtistlan.findViewById(cardId);
            CardThemeHelper.applyGradientGlassCard(card, tm, 28);
            aplicarTemaRecursivo(card, tm);
        }

        aplicarFades(rootHistoriaArtistlan, tm);
    }

    private void aplicarTemaRecursivo(@Nullable View view, @NonNull ThemeManager tm) {
        if (view == null) {
            return;
        }
        if (view instanceof ImageView) {
            ImageView icon = (ImageView) view;
            icon.setColorFilter(tm.color(ThemeKeys.ICON_ACTIVE));
            icon.setBackgroundTintList(ColorStateList.valueOf(tm.color(ThemeKeys.CARD_CHIP_BG)));
        } else if (view instanceof TextView) {
            TextView text = (TextView) view;
            CharSequence value = text.getText();
            boolean chip = value != null && value.toString().equals(value.toString().toUpperCase())
                    && value.length() <= 16;
            if (chip) {
                CardThemeHelper.applyChip(text, tm);
            } else if (text.getTextSize() >= spToPx(19)) {
                ThemeApplier.applyTextPrimary(text, tm);
            } else {
                ThemeApplier.applyTextSecondary(text, tm);
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                aplicarTemaRecursivo(group.getChildAt(i), tm);
            }
        }
    }

    private void aplicarFades(@NonNull View view, @NonNull ThemeManager tm) {
        View fadeTop = view.findViewById(R.id.fadeTopHistoriaArtistlan);
        View fadeBottom = view.findViewById(R.id.fadeBottomHistoriaArtistlan);
        int topColor = ColorUtils.setAlphaComponent(tm.color(ThemeKeys.BG_TOP), 255);

        if (fadeTop != null) {
            GradientDrawable top = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{
                            topColor,
                            ColorUtils.setAlphaComponent(topColor, 0)
                    }
            );
            fadeTop.setBackground(top);
            fadeTop.setClickable(false);
        }

        if (fadeBottom != null) {
            GradientDrawable bottom = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{
                            topColor,
                            ColorUtils.setAlphaComponent(topColor, 0)
                    }
            );
            fadeBottom.setBackground(bottom);
            fadeBottom.setClickable(false);
        }
    }

    private int[] cardIds() {
        return new int[]{
                R.id.cardOrigenArtistlan,
                R.id.cardProblemaArtistlan,
                R.id.cardObjetivoArtistlan,
                R.id.cardObjetivosEspecificosArtistlan,
                R.id.cardPublicoArtistlan,
                R.id.cardEvolucionArtistlan,
                R.id.cardFuncionesArtistlan,
                R.id.cardTecnologiasArtistlan,
                R.id.cardSeguridadArtistlan,
                R.id.cardModeracionArtistlan,
                R.id.cardTemasArtistlan,
                R.id.cardConvocatoriasArtistlan,
                R.id.cardTransaccionesArtistlan,
                R.id.cardFuturoArtistlan
        };
    }

    private float spToPx(int sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    @Override
    public void onPause() {
        super.onPause();
        pausarScrollAutomatico();
    }

    @Override
    public void onResume() {
        super.onResume();
        aplicarTemaRapido();
        if (scrollHistoriaArtistlan != null) {
            reanudarScrollAutomaticoConRetraso();
        }
    }

    @Override
    public void onDestroyView() {
        pausarScrollAutomatico();
        autoScrollAnimator = null;
        scrollHistoriaArtistlan = null;
        rootHistoriaArtistlan = null;
        super.onDestroyView();
    }
}

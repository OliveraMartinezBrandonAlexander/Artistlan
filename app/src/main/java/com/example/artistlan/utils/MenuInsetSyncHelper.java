package com.example.artistlan.utils;

import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.artistlan.R;

/**
 * Sincroniza el padding de un contenedor con la visibilidad real de las barras
 * superior e inferior animadas por scroll.
 */
public final class MenuInsetSyncHelper {

    @Nullable
    public static MenuInsetSyncHelper attach(
            @NonNull Fragment fragment,
            @NonNull View targetView,
            boolean syncTop,
            boolean syncBottom
    ) {
        View topMenu = fragment.requireActivity().findViewById(R.id.topBarFrame);
        View bottomMenu = fragment.requireActivity().findViewById(R.id.MenuInferiorFrame);
        if ((syncTop && topMenu == null) && (syncBottom && bottomMenu == null)) {
            return null;
        }

        MenuInsetSyncHelper helper = new MenuInsetSyncHelper(
                targetView,
                topMenu,
                bottomMenu,
                syncTop,
                syncBottom
        );
        helper.start();
        return helper;
    }

    private final View targetView;
    @Nullable private final View topMenu;
    @Nullable private final View bottomMenu;
    private final boolean syncTop;
    private final boolean syncBottom;
    private final int initialLeft;
    private final int initialTop;
    private final int initialRight;
    private final int initialBottom;
    private int lastAppliedTop = Integer.MIN_VALUE;
    private int lastAppliedBottom = Integer.MIN_VALUE;

    private final ViewTreeObserver.OnPreDrawListener preDrawListener = () -> {
        applyInsets();
        return true;
    };

    private MenuInsetSyncHelper(
            @NonNull View targetView,
            @Nullable View topMenu,
            @Nullable View bottomMenu,
            boolean syncTop,
            boolean syncBottom
    ) {
        this.targetView = targetView;
        this.topMenu = topMenu;
        this.bottomMenu = bottomMenu;
        this.syncTop = syncTop;
        this.syncBottom = syncBottom;
        this.initialLeft = targetView.getPaddingLeft();
        this.initialTop = targetView.getPaddingTop();
        this.initialRight = targetView.getPaddingRight();
        this.initialBottom = targetView.getPaddingBottom();
    }

    private void start() {
        ViewTreeObserver observer = targetView.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.addOnPreDrawListener(preDrawListener);
        }
        targetView.post(this::applyInsets);
    }

    public void detach() {
        ViewTreeObserver observer = targetView.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.removeOnPreDrawListener(preDrawListener);
        }
        restoreInitialPadding();
    }

    private void restoreInitialPadding() {
        targetView.setPadding(initialLeft, initialTop, initialRight, initialBottom);
        lastAppliedTop = initialTop;
        lastAppliedBottom = initialBottom;
    }

    private void applyInsets() {
        int topPadding = syncTop ? resolveTopPadding() : initialTop;
        int bottomPadding = syncBottom ? resolveBottomPadding() : initialBottom;
        if (topPadding == lastAppliedTop && bottomPadding == lastAppliedBottom) {
            return;
        }

        targetView.setPadding(initialLeft, topPadding, initialRight, bottomPadding);
        lastAppliedTop = topPadding;
        lastAppliedBottom = bottomPadding;
    }

    private int resolveTopPadding() {
        if (topMenu == null) {
            return initialTop;
        }
        int fullHeight = topMenu.getHeight();
        if (fullHeight <= 0) {
            return initialTop;
        }

        int extraPadding = Math.max(0, initialTop - fullHeight);
        int visibleHeight = resolveVisibleTopHeight(topMenu, fullHeight);
        return extraPadding + visibleHeight;
    }

    private int resolveBottomPadding() {
        if (bottomMenu == null) {
            return initialBottom;
        }
        int fullHeight = bottomMenu.getHeight();
        if (fullHeight <= 0) {
            return initialBottom;
        }

        int extraPadding = Math.max(0, initialBottom - fullHeight);
        int visibleHeight = resolveVisibleBottomHeight(bottomMenu, fullHeight);
        return extraPadding + visibleHeight;
    }

    private int resolveVisibleTopHeight(@NonNull View menu, int fullHeight) {
        if (menu.getVisibility() != View.VISIBLE) {
            return 0;
        }
        return clamp(Math.round(fullHeight + menu.getTranslationY()), 0, fullHeight);
    }

    private int resolveVisibleBottomHeight(@NonNull View menu, int fullHeight) {
        if (menu.getVisibility() != View.VISIBLE) {
            return 0;
        }
        return clamp(Math.round(fullHeight - menu.getTranslationY()), 0, fullHeight);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

package com.example.artistlan.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Fragments.FragMain;
import com.example.artistlan.R;

/** Helper reutilizable para ocultar/mostrar menús según dirección del scroll. */
public class ScrollMenuVisibilityHelper {
    private static final long ANIM_DURATION_MS = 320L;
    private static final int DEFAULT_HIDE_THRESHOLD_DP = 24;
    private static final int DEFAULT_SHOW_THRESHOLD_DP = 24;
    private static final int HOME_HIDE_THRESHOLD_DP = 88;
    private static final int HOME_SHOW_THRESHOLD_DP = 72;

    private enum MenuState {
        SHOWN,
        HIDDEN,
        ANIMATING_TO_SHOWN,
        ANIMATING_TO_HIDDEN
    }

    private final View topMenu;
    private final View bottomMenu;

    private MenuState menuState = MenuState.SHOWN;
    private int accumulatedDownDy = 0;
    private int accumulatedUpDy = 0;
    private int hideThresholdPx = -1;
    private int showThresholdPx = -1;
    private RecyclerView attachedRecyclerView;
    private View attachedScrollableView;
    private ViewTreeObserver.OnScrollChangedListener viewTreeScrollListener;
    private int lastScrollY = 0;
    private AbsListView attachedListView;
    private Fragment attachedFragment;
    private boolean homeFragmentAttached = false;
    private int topExpandedHeight = -1;
    private int bottomExpandedHeight = -1;
    private FragmentManager.FragmentLifecycleCallbacks callbacks;
    private final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
    private int animationVersion = 0;
    private int pendingAnimationEnds = 0;
    private View topCompanionView;

    public ScrollMenuVisibilityHelper(@NonNull View topMenu, @NonNull View bottomMenu) {
        this.topMenu = topMenu;
        this.bottomMenu = bottomMenu;
    }

    public void registerWith(@NonNull FragmentManager fragmentManager) {
        if (callbacks != null) return;

        callbacks = new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable android.os.Bundle savedInstanceState) {
                registerTopCompanionIfPresent(v);
                attachToScrollable(f, v);
            }

            @Override
            public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                View fragmentView = f.getView();
                if (fragmentView != null && topCompanionView != null
                        && fragmentView.findViewById(R.id.explorarHeader) == topCompanionView) {
                    topCompanionView.animate().cancel();
                    topCompanionView = null;
                }
                if (f == attachedFragment) {
                    detachCurrentScrollable();
                    showMenus();
                }
            }
        };

        fragmentManager.registerFragmentLifecycleCallbacks(callbacks, true);
    }

    public void unregisterFrom(@NonNull FragmentManager fragmentManager) {
        detachCurrentScrollable();
        if (callbacks != null) {
            fragmentManager.unregisterFragmentLifecycleCallbacks(callbacks);
            callbacks = null;
        }
    }

    private void attachToScrollable(@NonNull Fragment fragment, @NonNull View root) {
        detachCurrentScrollable();
        resetScrollAccumulators();
        hideThresholdPx = -1;
        showThresholdPx = -1;
        View scrollable = findScrollable(root);
        if (scrollable == null) {
            attachedFragment = null;
            showMenus();
            return;
        }

        attachedFragment = fragment;
        homeFragmentAttached = fragment instanceof FragMain;
        cacheExpandedHeights();
        if (scrollable instanceof RecyclerView) {
            attachedRecyclerView = (RecyclerView) scrollable;
            attachedRecyclerView.addOnScrollListener(recyclerListener);
        } else if (scrollable instanceof NestedScrollView) {
            attachViewTreeScrollListener(scrollable);
        } else if (scrollable instanceof ScrollView) {
            attachViewTreeScrollListener(scrollable);
        } else if (scrollable instanceof AbsListView) {
            attachedListView = (AbsListView) scrollable;
            attachedListView.setOnScrollListener(listScrollListener);
        }
    }

    private void detachCurrentScrollable() {
        if (attachedRecyclerView != null) {
            attachedRecyclerView.removeOnScrollListener(recyclerListener);
            attachedRecyclerView = null;
        }
        if (attachedScrollableView != null && viewTreeScrollListener != null) {
            ViewTreeObserver observer = attachedScrollableView.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.removeOnScrollChangedListener(viewTreeScrollListener);
            }
            attachedScrollableView = null;
            viewTreeScrollListener = null;
        }
        if (attachedListView != null) {
            attachedListView.setOnScrollListener(null);
            attachedListView = null;
        }
        attachedFragment = null;
        homeFragmentAttached = false;
    }

    private void attachViewTreeScrollListener(@NonNull View scrollable) {
        attachedScrollableView = scrollable;
        lastScrollY = scrollable.getScrollY();
        viewTreeScrollListener = () -> {
            if (attachedScrollableView == null) return;
            int scrollY = attachedScrollableView.getScrollY();
            if (scrollY <= 0 || !attachedScrollableView.canScrollVertically(-1)) {
                resetScrollAccumulators();
                showMenusIfNeeded();
                lastScrollY = scrollY;
                return;
            }
            handleScrollDirection(scrollY - lastScrollY);
            lastScrollY = scrollY;
        };
        scrollable.getViewTreeObserver().addOnScrollChangedListener(viewTreeScrollListener);
    }

    private final RecyclerView.OnScrollListener recyclerListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            if (!recyclerView.canScrollVertically(-1)) {
                resetScrollAccumulators();
                showMenusIfNeeded();
                return;
            }
            handleScrollDirection(dy);
        }
    };

    private final AbsListView.OnScrollListener listScrollListener = new AbsListView.OnScrollListener() {
        private int lastFirstVisibleItem = 0;
        private int lastTop = 0;

        @Override public void onScrollStateChanged(AbsListView view, int scrollState) { }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            View firstChild = view.getChildAt(0);
            if (firstChild == null) return;

            int top = firstChild.getTop();
            int dy = (firstVisibleItem == lastFirstVisibleItem)
                    ? (lastTop - top)
                    : (firstVisibleItem > lastFirstVisibleItem ? 1 : -1);
            lastFirstVisibleItem = firstVisibleItem;
            lastTop = top;
            if (firstVisibleItem == 0 && top >= 0) {
                resetScrollAccumulators();
                showMenusIfNeeded();
                return;
            }
            handleScrollDirection(dy);
        }
    };

    private void handleScrollDirection(int dy) {
        if (dy == 0) return;

        ensureThresholds();
        if (dy > 0) {
            accumulatedDownDy += dy;
            accumulatedUpDy = 0;
            if (accumulatedDownDy >= hideThresholdPx) {
                hideMenusIfNeeded();
                resetScrollAccumulators();
            }
        } else {
            accumulatedUpDy += Math.abs(dy);
            accumulatedDownDy = 0;
            if (accumulatedUpDy >= showThresholdPx) {
                showMenusIfNeeded();
                resetScrollAccumulators();
            }
        }
    }

    private void registerTopCompanionIfPresent(@NonNull View root) {
        View companion = root.findViewById(R.id.explorarHeader);
        if (companion == null) return;

        topCompanionView = companion;
        topCompanionView.setClickable(menuState == MenuState.SHOWN || menuState == MenuState.ANIMATING_TO_SHOWN);
        topCompanionView.post(() -> {
            if (topCompanionView != companion) return;
            topCompanionView.setTranslationY(menuState == MenuState.HIDDEN
                    || menuState == MenuState.ANIMATING_TO_HIDDEN
                    ? -Math.max(topCompanionView.getHeight(), 1)
                    : 0f);
        });
    }

    private void ensureThresholds() {
        if (hideThresholdPx > 0 && showThresholdPx > 0) return;
        float density = topMenu.getResources().getDisplayMetrics().density;
        int hideThresholdDp = homeFragmentAttached ? HOME_HIDE_THRESHOLD_DP : DEFAULT_HIDE_THRESHOLD_DP;
        int showThresholdDp = homeFragmentAttached ? HOME_SHOW_THRESHOLD_DP : DEFAULT_SHOW_THRESHOLD_DP;
        hideThresholdPx = Math.round(hideThresholdDp * density);
        showThresholdPx = Math.round(showThresholdDp * density);
    }

    private void resetScrollAccumulators() {
        accumulatedDownDy = 0;
        accumulatedUpDy = 0;
    }

    private void hideMenusIfNeeded() {
        if (menuState == MenuState.HIDDEN || menuState == MenuState.ANIMATING_TO_HIDDEN) return;
        menuState = MenuState.ANIMATING_TO_HIDDEN;
        animateBars(false);
    }

    private void showMenusIfNeeded() {
        if (menuState == MenuState.SHOWN || menuState == MenuState.ANIMATING_TO_SHOWN) return;
        menuState = MenuState.ANIMATING_TO_SHOWN;
        animateBars(true);
    }

    private void showMenus() {
        showMenusIfNeeded();
    }

    private void animateBars(boolean show) {
        cacheExpandedHeights();

        topMenu.setClickable(show);
        if (topCompanionView != null) {
            topCompanionView.setClickable(show);
        }
        bottomMenu.setClickable(show);

        topMenu.animate().cancel();
        if (topCompanionView != null) {
            topCompanionView.animate().cancel();
        }
        bottomMenu.animate().cancel();

        if (show && (topMenu.getTranslationY() == 0f && bottomMenu.getTranslationY() == 0f)) {
            topMenu.setTranslationY(-Math.max(topMenu.getHeight(), 1));
            if (topCompanionView != null) {
                topCompanionView.setTranslationY(-Math.max(topCompanionView.getHeight(), 1));
            }
            bottomMenu.setTranslationY(Math.max(bottomMenu.getHeight(), 1));
        }

        float topTarget = show ? 0f : -Math.max(topMenu.getHeight(), 1);
        float companionTarget = show || topCompanionView == null
                ? 0f
                : -Math.max(topCompanionView.getHeight(), 1);
        float bottomTarget = show ? 0f : Math.max(bottomMenu.getHeight(), 1);
        int currentAnimationVersion = ++animationVersion;
        pendingAnimationEnds = topCompanionView == null ? 2 : 3;

        topMenu.animate()
                .translationY(topTarget)
                .setDuration(ANIM_DURATION_MS)
                .setInterpolator(interpolator)
                .withEndAction(() -> onBarAnimationEnd(show, currentAnimationVersion))
                .start();

        if (topCompanionView != null) {
            topCompanionView.animate()
                    .translationY(companionTarget)
                    .setDuration(ANIM_DURATION_MS)
                    .setInterpolator(interpolator)
                    .withEndAction(() -> onBarAnimationEnd(show, currentAnimationVersion))
                    .start();
        }

        bottomMenu.animate()
                .translationY(bottomTarget)
                .setDuration(ANIM_DURATION_MS)
                .setInterpolator(interpolator)
                .withEndAction(() -> onBarAnimationEnd(show, currentAnimationVersion))
                .start();
    }

    private void onBarAnimationEnd(boolean show, int completedAnimationVersion) {
        if (completedAnimationVersion != animationVersion) return;
        pendingAnimationEnds--;
        if (pendingAnimationEnds > 0) return;
        settleBars(show);
    }

    private void settleBars(boolean show) {
        if (show && menuState != MenuState.ANIMATING_TO_SHOWN) return;
        if (!show && menuState != MenuState.ANIMATING_TO_HIDDEN) return;

        if (show) {
            topMenu.setTranslationY(0f);
            if (topCompanionView != null) {
                topCompanionView.setTranslationY(0f);
                topCompanionView.setClickable(true);
            }
            bottomMenu.setTranslationY(0f);
            topMenu.setClickable(true);
            bottomMenu.setClickable(true);
            menuState = MenuState.SHOWN;
        } else {
            topMenu.setTranslationY(-Math.max(topMenu.getHeight(), 1));
            if (topCompanionView != null) {
                topCompanionView.setTranslationY(-Math.max(topCompanionView.getHeight(), 1));
                topCompanionView.setClickable(false);
            }
            bottomMenu.setTranslationY(Math.max(bottomMenu.getHeight(), 1));
            topMenu.setClickable(false);
            bottomMenu.setClickable(false);
            menuState = MenuState.HIDDEN;
        }
    }

    private void cacheExpandedHeights() {
        if (topExpandedHeight <= 0) {
            int height = topMenu.getHeight();
            if (height > 0) {
                topExpandedHeight = height;
            }
        }
        if (bottomExpandedHeight <= 0) {
            int height = bottomMenu.getHeight();
            if (height > 0) {
                bottomExpandedHeight = height;
            }
        }
    }

    @Nullable
    private View findScrollable(@NonNull View root) {
        if (root instanceof RecyclerView || root instanceof NestedScrollView || root instanceof ScrollView || root instanceof AbsListView) {
            return root;
        }
        if (!(root instanceof ViewGroup)) return null;

        ViewGroup group = (ViewGroup) root;
        for (int i = 0; i < group.getChildCount(); i++) {
            View candidate = findScrollable(group.getChildAt(i));
            if (candidate != null) return candidate;
        }
        return null;
    }
}

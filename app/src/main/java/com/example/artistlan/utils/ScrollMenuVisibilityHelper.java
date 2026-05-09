package com.example.artistlan.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

/** Helper reutilizable para ocultar/mostrar menús según dirección del scroll. */
public class ScrollMenuVisibilityHelper {
    private static final long ANIM_DURATION_MS = 320L;

    private final View topMenu;
    private final View bottomMenu;

    private boolean menusVisible = true;
    private int topExpandedHeight = -1;
    private RecyclerView attachedRecyclerView;
    private NestedScrollView attachedNestedScroll;
    private ScrollView attachedScrollView;
    private AbsListView attachedListView;
    private FragmentManager.FragmentLifecycleCallbacks callbacks;

    public ScrollMenuVisibilityHelper(@NonNull View topMenu, @NonNull View bottomMenu) {
        this.topMenu = topMenu;
        this.bottomMenu = bottomMenu;
    }

    public void registerWith(@NonNull FragmentManager fragmentManager) {
        if (callbacks != null) return;

        callbacks = new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable android.os.Bundle savedInstanceState) {
                attachToScrollable(v);
            }

            @Override
            public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                detachCurrentScrollable();
                showMenus();
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

    private void attachToScrollable(@NonNull View root) {
        detachCurrentScrollable();
        View scrollable = findScrollable(root);
        if (scrollable == null) return;

        if (scrollable instanceof RecyclerView) {
            attachedRecyclerView = (RecyclerView) scrollable;
            attachedRecyclerView.addOnScrollListener(recyclerListener);
        } else if (scrollable instanceof NestedScrollView) {
            attachedNestedScroll = (NestedScrollView) scrollable;
            attachedNestedScroll.setOnScrollChangeListener(nestedListener);
        } else if (scrollable instanceof ScrollView) {
            attachedScrollView = (ScrollView) scrollable;
            attachedScrollView.setOnScrollChangeListener(scrollListener);
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
        if (attachedNestedScroll != null) {
            attachedNestedScroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) null);
            attachedNestedScroll = null;
        }
        if (attachedScrollView != null) {
            attachedScrollView.setOnScrollChangeListener(null);
            attachedScrollView = null;
        }
        if (attachedListView != null) {
            attachedListView.setOnScrollListener(null);
            attachedListView = null;
        }
    }

    private final RecyclerView.OnScrollListener recyclerListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            handleScrollDirection(dy);
        }
    };

    private final NestedScrollView.OnScrollChangeListener nestedListener =
            (v, scrollX, scrollY, oldScrollX, oldScrollY) -> handleScrollDirection(scrollY - oldScrollY);

    private final View.OnScrollChangeListener scrollListener =
            (v, scrollX, scrollY, oldScrollX, oldScrollY) -> handleScrollDirection(scrollY - oldScrollY);

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
            handleScrollDirection(dy);
        }
    };

    private void handleScrollDirection(int dy) {
        if (dy > 0) hideMenus();
        else if (dy < 0) showMenus();
    }

    private void hideMenus() {
        if (!menusVisible) return;
        menusVisible = false;

        topMenu.animate().cancel();
        bottomMenu.animate().cancel();

        topMenu.animate()
                .translationY(-topMenu.getHeight())
                .setDuration(ANIM_DURATION_MS)
                .withEndAction(() -> {
                    if (!menusVisible) {
                        cacheTopExpandedHeight();
                        ViewGroup.LayoutParams lp = topMenu.getLayoutParams();
                        lp.height = 0;
                        topMenu.setLayoutParams(lp);
                        topMenu.setVisibility(View.GONE);
                    }
                })
                .start();

        bottomMenu.animate()
                .translationY(bottomMenu.getHeight())
                .setDuration(ANIM_DURATION_MS)
                .withEndAction(() -> {
                    if (!menusVisible) bottomMenu.setVisibility(View.GONE);
                })
                .start();
    }

    private void showMenus() {
        if (menusVisible) return;
        menusVisible = true;

        topMenu.animate().cancel();
        bottomMenu.animate().cancel();

        restoreTopExpandedHeight();
        topMenu.setVisibility(View.VISIBLE);
        bottomMenu.setVisibility(View.VISIBLE);
        topMenu.setTranslationY(-topMenu.getHeight());
        bottomMenu.setTranslationY(bottomMenu.getHeight());

        topMenu.animate().translationY(0f).setDuration(ANIM_DURATION_MS).start();
        bottomMenu.animate().translationY(0f).setDuration(ANIM_DURATION_MS).start();
    }


    private void cacheTopExpandedHeight() {
        if (topExpandedHeight > 0) return;
        int measured = topMenu.getHeight();
        if (measured > 0) {
            topExpandedHeight = measured;
            return;
        }
        ViewGroup.LayoutParams lp = topMenu.getLayoutParams();
        if (lp != null && lp.height > 0) {
            topExpandedHeight = lp.height;
        }
    }

    private void restoreTopExpandedHeight() {
        ViewGroup.LayoutParams lp = topMenu.getLayoutParams();
        if (lp == null) return;
        if (topExpandedHeight <= 0) {
            cacheTopExpandedHeight();
        }
        if (topExpandedHeight > 0) {
            lp.height = topExpandedHeight;
            topMenu.setLayoutParams(lp);
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
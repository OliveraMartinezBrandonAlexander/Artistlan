package com.example.artistlan.Carrusel.layout;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class CenterZoomLayoutManager extends LinearLayoutManager {
    private final float shrinkAmount = 0.15f;
    private final float shrinkDistance = 0.9f;

    public CenterZoomLayoutManager(Context context) {
        super(context, HORIZONTAL, false);
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        scaleChildren();
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrolled = super.scrollHorizontallyBy(dx, recycler, state);
        if (getOrientation() == HORIZONTAL) {
            scaleChildren();
        }
        return scrolled;
    }

    private void scaleChildren() {
        float midpoint = getWidth() / 2.0f;
        float d0 = 0.0f;
        float d1 = shrinkDistance * midpoint;
        float s0 = 1.0f;
        float s1 = 1.0f - shrinkAmount;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) continue;

            int position = getPosition(child);

            if (position > 0 && position < getItemCount() - 1) {
                float childMidpoint = (getDecoratedLeft(child) + getDecoratedRight(child)) / 2.0f;
                float d = Math.min(d1, Math.abs(midpoint - childMidpoint));
                float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
                child.setScaleX(scale);
                child.setScaleY(scale);

                if (scale == 1.0f) {
                    child.setAlpha(1.0f);
                } else {
                    child.setAlpha(0.7f);
                }
            } else {
                child.setAlpha(0.0f);
            }
        }
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    private static class CenterSmoothScroller extends LinearSmoothScroller {
        CenterSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return 100f / displayMetrics.densityDpi;
        }
    }
}
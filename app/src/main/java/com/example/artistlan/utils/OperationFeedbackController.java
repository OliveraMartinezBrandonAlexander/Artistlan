package com.example.artistlan.utils;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OperationFeedbackController {

    private final ArtistlanLoadingDialog loadingDialog;
    private final List<View> controlledViews = new ArrayList<>();
    private boolean inProgress;

    public OperationFeedbackController(@NonNull Fragment fragment, @Nullable View... views) {
        this.loadingDialog = new ArtistlanLoadingDialog(fragment);
        if (views != null) {
            controlledViews.addAll(Arrays.asList(views));
        }
    }

    public OperationFeedbackController(@NonNull Context context, @Nullable View... views) {
        this.loadingDialog = new ArtistlanLoadingDialog(context);
        if (views != null) {
            controlledViews.addAll(Arrays.asList(views));
        }
    }

    public OperationFeedbackController(@NonNull ArtistlanLoadingDialog loadingDialog, @Nullable View... views) {
        this.loadingDialog = loadingDialog;
        if (views != null) {
            controlledViews.addAll(Arrays.asList(views));
        }
    }

    public boolean begin(@NonNull String loadingMessage) {
        if (inProgress) {
            return false;
        }
        inProgress = true;
        setViewsEnabled(false);
        loadingDialog.showLoading(loadingMessage);
        return true;
    }

    public void showLoading(@NonNull String loadingMessage) {
        inProgress = true;
        setViewsEnabled(false);
        loadingDialog.showLoading(loadingMessage);
    }

    public void showSuccess(@NonNull String message, @Nullable Runnable onDismiss) {
        inProgress = false;
        setViewsEnabled(true);
        loadingDialog.showSuccess(message, onDismiss);
    }

    public void showError(@NonNull String message) {
        inProgress = false;
        setViewsEnabled(true);
        loadingDialog.showError(message);
    }

    public void dismiss() {
        inProgress = false;
        setViewsEnabled(true);
        loadingDialog.dismiss();
    }

    public void release() {
        inProgress = false;
        loadingDialog.release();
    }

    public boolean isInProgress() {
        return inProgress;
    }

    private void setViewsEnabled(boolean enabled) {
        for (View view : controlledViews) {
            if (view != null) {
                view.setEnabled(enabled);
                view.setAlpha(enabled ? 1f : 0.68f);
            }
        }
    }
}

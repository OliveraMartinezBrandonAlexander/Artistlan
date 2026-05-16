package com.example.artistlan.utils;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

public final class PasswordPressVisibilityHelper {
    private static final String ACCESSIBILITY_TEXT = "Mantener presionado para ver contraseña";
    private static final int ICON_SIZE_DP = 24;
    private static final int TOUCH_AREA_DP = 48;

    private PasswordPressVisibilityHelper() {
    }

    public static void attach(
            @NonNull EditText editText,
            @DrawableRes int iconRes,
            int tintColor
    ) {
        Drawable icon = ContextCompat.getDrawable(editText.getContext(), iconRes);
        if (icon == null) return;

        Drawable endIcon = icon.mutate();
        int iconSize = dp(editText, ICON_SIZE_DP);
        endIcon.setBounds(0, 0, iconSize, iconSize);
        endIcon.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
        editText.setCompoundDrawables(null, null, endIcon, null);
        editText.setCompoundDrawablePadding(dp(editText, 10));
        editText.setContentDescription(ACCESSIBILITY_TEXT);
        ViewCompat.setTooltipText(editText, ACCESSIBILITY_TEXT);
        hidePassword(editText);

        editText.setOnTouchListener((v, event) -> {
            if (!isTouchOnEndDrawable(editText, event)) {
                return false;
            }

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    showPassword(editText);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    hidePassword(editText);
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                        editText.performClick();
                    }
                    return true;
                default:
                    return true;
            }
        });
    }

    private static boolean isTouchOnEndDrawable(@NonNull EditText editText, @NonNull MotionEvent event) {
        Drawable[] drawables = editText.getCompoundDrawables();
        Drawable endDrawable = drawables[2];
        if (endDrawable == null) return false;

        int iconWidth = endDrawable.getBounds().width();
        int touchWidth = dp(editText, TOUCH_AREA_DP);
        int extraTouch = Math.max(0, (touchWidth - iconWidth) / 2);
        int iconLeft = editText.getWidth() - editText.getPaddingRight() - iconWidth;
        int touchStart = iconLeft - extraTouch;
        int touchEnd = iconLeft + iconWidth + extraTouch;
        return event.getX() >= touchStart && event.getX() <= touchEnd;
    }

    private static void showPassword(@NonNull EditText editText) {
        int selection = Math.max(editText.getSelectionStart(), editText.getSelectionEnd());
        editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        restoreSelection(editText, selection);
    }

    private static void hidePassword(@NonNull EditText editText) {
        int selection = Math.max(editText.getSelectionStart(), editText.getSelectionEnd());
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        restoreSelection(editText, selection);
    }

    private static void restoreSelection(@NonNull EditText editText, int selection) {
        int length = editText.getText() != null ? editText.getText().length() : 0;
        int safeSelection = selection >= 0 ? selection : length;
        editText.setSelection(Math.max(0, Math.min(safeSelection, length)));
    }

    private static int dp(@NonNull View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }
}

package com.example.artistlan.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImageOptimizerUtil {

    public enum ImageType {
        PROFILE,
        ARTWORK
    }

    private static final int PROFILE_MAX_WIDTH = 600;
    private static final int PROFILE_MAX_HEIGHT = 600;
    private static final int ARTWORK_MAX_SIDE = 1080;
    private static final int TARGET_MAX_BYTES = 800 * 1024;

    private ImageOptimizerUtil() {}

    public static byte[] optimizeToJpeg(@NonNull Context context, @NonNull Uri imageUri, @NonNull ImageType imageType) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        Bitmap decoded = decodeBitmapSafely(resolver, imageUri, imageType);
        if (decoded == null) {
            throw new IOException("No se pudo decodificar la imagen seleccionada.");
        }

        Bitmap rotated = applyExifRotation(resolver, imageUri, decoded);

        if (imageType == ImageType.PROFILE) {
            Bitmap cropped = cropCenterByAspect(rotated, 1f);
            Bitmap resized = resizeToMax(cropped, PROFILE_MAX_WIDTH, PROFILE_MAX_HEIGHT);
            return compressWithTargetSize(resized);
        }

        Bitmap resizedArtwork = resizeToMaxSide(rotated, ARTWORK_MAX_SIDE);
        return compressWithTargetSize(resizedArtwork);
    }

    private static Bitmap decodeBitmapSafely(ContentResolver resolver, Uri imageUri, ImageType imageType) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;

        try (InputStream is = resolver.openInputStream(imageUri)) {
            if (is == null) throw new IOException("No se pudo leer la imagen.");
            BitmapFactory.decodeStream(is, null, bounds);
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int reqW = imageType == ImageType.PROFILE ? PROFILE_MAX_WIDTH * 2 : ARTWORK_MAX_SIDE * 2;
        int reqH = imageType == ImageType.PROFILE ? PROFILE_MAX_HEIGHT * 2 : ARTWORK_MAX_SIDE * 2;
        opts.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, reqW, reqH);

        try (InputStream is = resolver.openInputStream(imageUri)) {
            if (is == null) throw new IOException("No se pudo leer la imagen para procesarla.");
            return BitmapFactory.decodeStream(is, null, opts);
        }
    }

    private static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
        return Math.max(1, inSampleSize);
    }

    private static Bitmap applyExifRotation(ContentResolver resolver, Uri imageUri, Bitmap bitmap) {
        try (InputStream is = resolver.openInputStream(imageUri)) {
            if (is == null) return bitmap;
            ExifInterface exif = new ExifInterface(is);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) matrix.postRotate(90);
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) matrix.postRotate(180);
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) matrix.postRotate(270);
            else return bitmap;

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            return bitmap;
        }
    }

    private static Bitmap cropCenterByAspect(Bitmap src, float targetRatio) {
        int width = src.getWidth();
        int height = src.getHeight();
        float currentRatio = (float) width / (float) height;

        if (Math.abs(currentRatio - targetRatio) < 0.01f) return src;

        int newWidth = width;
        int newHeight = height;
        if (currentRatio > targetRatio) {
            newWidth = Math.round(height * targetRatio);
        } else {
            newHeight = Math.round(width / targetRatio);
        }

        int x = Math.max(0, (width - newWidth) / 2);
        int y = Math.max(0, (height - newHeight) / 2);
        return Bitmap.createBitmap(src, x, y, newWidth, newHeight);
    }

    private static Bitmap resizeToMax(Bitmap src, int maxWidth, int maxHeight) {
        int width = src.getWidth();
        int height = src.getHeight();
        if (width <= maxWidth && height <= maxHeight) return src;

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int targetW = Math.round(width * ratio);
        int targetH = Math.round(height * ratio);
        return Bitmap.createScaledBitmap(src, targetW, targetH, true);
    }


    private static Bitmap resizeToMaxSide(Bitmap src, int maxSide) {
        int width = src.getWidth();
        int height = src.getHeight();
        int currentMax = Math.max(width, height);
        if (currentMax <= maxSide) return src;

        float scale = (float) maxSide / currentMax;
        int targetW = Math.round(width * scale);
        int targetH = Math.round(height * scale);
        return Bitmap.createScaledBitmap(src, targetW, targetH, true);
    }

    private static byte[] compressWithTargetSize(Bitmap bitmap) throws IOException {
        int quality = 85;
        byte[] output;
        do {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)) {
                throw new IOException("No se pudo comprimir la imagen.");
            }
            output = baos.toByteArray();
            quality -= 5;
        } while (output.length > TARGET_MAX_BYTES && quality >= 70);

        return output;
    }
}
package com.example.artistlan.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.artistlan.Carrusel.model.ObraCarruselItem;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class LikeStateManager {

    private static final Map<Integer, LikeState> OBRA_STATES = new HashMap<>();
    private static final Set<Integer> OBRA_REQUESTS_IN_FLIGHT = new HashSet<>();
    private static Integer currentUserId;

    private LikeStateManager() {}

    public static synchronized void setCurrentUserId(@Nullable Integer userId) {
        Integer normalized = userId != null && userId > 0 ? userId : null;
        if (currentUserId == null ? normalized == null : currentUserId.equals(normalized)) {
            return;
        }
        currentUserId = normalized;
        OBRA_STATES.clear();
        OBRA_REQUESTS_IN_FLIGHT.clear();
    }

    @NonNull
    public static synchronized LikeState resolveObraState(int idObra, boolean likedFromBackend, int likesFromBackend) {
        LikeState cached = OBRA_STATES.get(idObra);
        if (idObra > 0 && cached != null) {
            return cached;
        }

        LikeState state = new LikeState(likedFromBackend, Math.max(0, likesFromBackend));
        if (idObra > 0) {
            OBRA_STATES.put(idObra, state);
        }
        return state;
    }

    public static synchronized void setObraState(int idObra, boolean liked, int likesCount) {
        if (idObra <= 0) {
            return;
        }
        OBRA_STATES.put(idObra, new LikeState(liked, Math.max(0, likesCount)));
    }

    public static synchronized void applyTo(@Nullable TarjetaTextoObraItem item) {
        if (item == null || item.getIdObra() <= 0) {
            return;
        }
        LikeState state = OBRA_STATES.get(item.getIdObra());
        if (state == null) {
            OBRA_STATES.put(item.getIdObra(), new LikeState(item.isUserLiked(), item.getLikes()));
            return;
        }
        item.setUserLiked(state.isLiked());
        item.setLikes(state.getLikesCount());
    }

    public static synchronized void applyTo(@Nullable ObraCarruselItem item) {
        if (item == null || item.getIdObra() == null || item.getIdObra() <= 0) {
            return;
        }
        LikeState state = OBRA_STATES.get(item.getIdObra());
        if (state == null) {
            OBRA_STATES.put(item.getIdObra(), new LikeState(item.isUserLiked(), item.getLikesCount()));
            return;
        }
        item.setUserLiked(state.isLiked());
        item.setLikesCount(state.getLikesCount());
    }

    public static synchronized boolean beginObraRequest(int idObra) {
        if (idObra <= 0 || OBRA_REQUESTS_IN_FLIGHT.contains(idObra)) {
            return false;
        }
        OBRA_REQUESTS_IN_FLIGHT.add(idObra);
        return true;
    }

    public static synchronized void finishObraRequest(int idObra) {
        OBRA_REQUESTS_IN_FLIGHT.remove(idObra);
    }

    public static final class LikeState {
        private final boolean liked;
        private final int likesCount;

        private LikeState(boolean liked, int likesCount) {
            this.liked = liked;
            this.likesCount = Math.max(0, likesCount);
        }

        public boolean isLiked() {
            return liked;
        }

        public int getLikesCount() {
            return likesCount;
        }
    }
}

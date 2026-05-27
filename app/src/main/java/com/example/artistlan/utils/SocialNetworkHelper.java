package com.example.artistlan.utils;

import com.example.artistlan.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SocialNetworkHelper {

    private SocialNetworkHelper() {}

    public enum Network {
        INSTAGRAM("Instagram", R.drawable.ic_social_instagram),
        WHATSAPP("WhatsApp", R.drawable.ic_social_whatsapp),
        FACEBOOK("Facebook", R.drawable.ic_social_facebook),
        TELEGRAM("Telegram", R.drawable.ic_social_telegram),
        X("X", R.drawable.ic_social_x),
        REDDIT("Reddit", R.drawable.ic_social_reddit),
        TIKTOK("TikTok", R.drawable.ic_social_tiktok),
        YOUTUBE("YouTube", R.drawable.ic_social_youtube),
        GITHUB("GitHub", R.drawable.ic_social_github),
        BEHANCE("Behance", R.drawable.ic_social_behance),
        DRIBBBLE("Dribbble", R.drawable.ic_social_dribbble),
        LINK("Link", R.drawable.ic_social_link);

        private final String displayName;
        private final int iconRes;

        Network(String displayName, int iconRes) {
            this.displayName = displayName;
            this.iconRes = iconRes;
        }
    }

    public static int resolverIconoRedSocial(String texto) {
        return resolverRedSocial(texto).iconRes;
    }

    public static String resolverNombreRedSocial(String texto) {
        return resolverRedSocial(texto).displayName;
    }

    public static boolean esRedReconocida(String texto) {
        return resolverRedSocial(texto) != Network.LINK;
    }

    public static Network resolverRedSocial(String texto) {
        String normalizado = normalizar(texto);
        if (normalizado.isEmpty()) return Network.LINK;

        if (contiene(normalizado, "instagram.com", "instagram", "insta") || token(normalizado, "ig")) {
            return Network.INSTAGRAM;
        }
        if (contiene(normalizado, "wa.me", "whatsapp", "whats", "wsp")) {
            return Network.WHATSAPP;
        }
        if (contiene(normalizado, "facebook", "fb.com") || token(normalizado, "fb")) {
            return Network.FACEBOOK;
        }
        if (contiene(normalizado, "telegram", "t.me")) {
            return Network.TELEGRAM;
        }
        if (contiene(normalizado, "x.com", "twitter") || token(normalizado, "x")) {
            return Network.X;
        }
        if (contiene(normalizado, "reddit")) {
            return Network.REDDIT;
        }
        if (contiene(normalizado, "tiktok")) {
            return Network.TIKTOK;
        }
        if (contiene(normalizado, "youtube", "youtu.be")) {
            return Network.YOUTUBE;
        }
        if (contiene(normalizado, "github")) {
            return Network.GITHUB;
        }
        if (contiene(normalizado, "behance")) {
            return Network.BEHANCE;
        }
        if (contiene(normalizado, "dribbble")) {
            return Network.DRIBBBLE;
        }
        return Network.LINK;
    }

    public static List<String> separarRedes(String texto) {
        List<String> redes = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) {
            return redes;
        }
        String[] partes = texto.split("[\\n,;]+");
        for (String parte : partes) {
            String limpia = parte == null ? "" : parte.trim();
            if (!limpia.isEmpty()) {
                redes.add(limpia);
            }
        }
        return redes;
    }

    public static String crearEtiquetaCorta(String texto) {
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.isEmpty()) {
            return resolverNombreRedSocial(texto);
        }
        String etiqueta = limpio.replaceFirst("(?i)^https?://", "")
                .replaceFirst("(?i)^www\\.", "")
                .trim();
        if (etiqueta.length() > 24) {
            return etiqueta.substring(0, 23) + "...";
        }
        return etiqueta;
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean contiene(String texto, String... palabras) {
        for (String palabra : palabras) {
            if (texto.contains(palabra)) {
                return true;
            }
        }
        return false;
    }

    private static boolean token(String texto, String token) {
        return texto.equals(token)
                || texto.startsWith(token + ":")
                || texto.startsWith(token + " ")
                || texto.startsWith(token + "/")
                || texto.startsWith(token + "@");
    }
}

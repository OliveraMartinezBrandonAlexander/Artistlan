package com.example.artistlan.Fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public final class ContactoDialogHelper {

    private ContactoDialogHelper() {
    }

    public static void mostrarDialogoContacto(
            @NonNull Fragment fragment,
            @NonNull String tituloDialogo,
            String nombre,
            String usuario,
            String tipoContacto,
            String contacto,
            String correo,
            String telefono
    ) {
        if (!fragment.isAdded()) {
            return;
        }

        ContactoInfo info = new ContactoInfo(nombre, usuario, tipoContacto, contacto, correo, telefono);
        if (!info.hasData()) {
            android.widget.Toast.makeText(fragment.requireContext(), "No hay datos de contacto disponibles", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder mensaje = new StringBuilder();
        appendLinea(mensaje, "Nombre", info.nombre);
        appendLinea(mensaje, "Usuario", info.usuario);
        appendLinea(mensaje, "Contacto", info.contactoPrincipal);
        appendLinea(mensaje, "Tipo", info.tipoContactoFinal);
        appendLinea(mensaje, "Correo", info.correo);
        appendLinea(mensaje, "Telefono", info.telefono);

        String texto = mensaje.toString().trim();
        if (texto.isEmpty()) {
            texto = "No hay informacion de contacto disponible.";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireContext())
                .setTitle(tituloDialogo)
                .setMessage(texto)
                .setNegativeButton("Cerrar", null);

        if (!isBlank(info.contactoPrincipal)) {
            builder.setPositiveButton("Copiar contacto", (dialog, which) ->
                    copiarTexto(fragment, "Contacto", info.contactoPrincipal));
        }

        if (puedeAbrirContacto(info)) {
            builder.setNeutralButton("Contactar", (dialog, which) ->
                    abrirContacto(fragment, info));
        }

        builder.show();
    }

    private static void abrirContacto(Fragment fragment, ContactoInfo info) {
        if (isBlank(info.contactoPrincipal) || !fragment.isAdded()) {
            return;
        }

        String tipo = safe(info.tipoContactoFinal, "").toUpperCase(Locale.ROOT);
        String valor = info.contactoPrincipal.trim();
        Intent intent;

        if (tipo.contains("MAIL") || tipo.contains("EMAIL") || valor.contains("@")) {
            intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + valor));
        } else if (tipo.contains("WHATS")) {
            String numero = valor.replaceAll("[^0-9]", "");
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + numero));
        } else if (tipo.contains("TELEF") || tipo.contains("PHONE")) {
            intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + valor));
        } else if (tipo.contains("INSTA")) {
            String user = valor.startsWith("@") ? valor.substring(1) : valor;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/" + user));
        } else {
            copiarTexto(fragment, "Contacto", valor);
            android.widget.Toast.makeText(fragment.requireContext(), "Contacto copiado al portapapeles", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            fragment.startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(fragment.requireContext(), "No se pudo abrir la app de contacto", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private static void copiarTexto(Fragment fragment, String label, String texto) {
        if (!fragment.isAdded()) {
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) fragment.requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            android.widget.Toast.makeText(fragment.requireContext(), "No se pudo acceder al portapapeles", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        clipboard.setPrimaryClip(ClipData.newPlainText(label, texto));
        android.widget.Toast.makeText(fragment.requireContext(), "Contacto copiado", android.widget.Toast.LENGTH_SHORT).show();
    }

    private static boolean puedeAbrirContacto(ContactoInfo info) {
        if (isBlank(info.contactoPrincipal)) {
            return false;
        }
        String tipo = safe(info.tipoContactoFinal, "").toUpperCase(Locale.ROOT);
        return tipo.contains("MAIL")
                || tipo.contains("EMAIL")
                || tipo.contains("WHATS")
                || tipo.contains("TELEF")
                || tipo.contains("PHONE")
                || tipo.contains("INSTA");
    }

    private static String inferirTipo(String contacto) {
        if (isBlank(contacto)) {
            return "No especificado";
        }
        String value = contacto.trim();
        if (value.contains("@")) {
            return "EMAIL";
        }
        if (value.matches(".*\\d{7,}.*")) {
            return "TELEFONO";
        }
        if (value.contains("instagram.com") || value.startsWith("@")) {
            return "INSTAGRAM";
        }
        return "OTRO";
    }

    private static void appendLinea(StringBuilder builder, String label, String value) {
        if (builder == null || isBlank(value)) {
            return;
        }
        builder.append(label).append(": ").append(value.trim()).append('\n');
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String safe(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private static final class ContactoInfo {
        final String nombre;
        final String usuario;
        final String tipoContactoFinal;
        final String contactoPrincipal;
        final String correo;
        final String telefono;

        ContactoInfo(String nombre, String usuario, String tipoContacto, String contacto, String correo, String telefono) {
            this.nombre = safe(nombre, "");
            this.usuario = safe(usuario, "");
            this.correo = safe(correo, "");
            this.telefono = safe(telefono, "");

            String principal = safe(contacto, "");
            if (principal.isEmpty()) {
                principal = !this.correo.isEmpty() ? this.correo : this.telefono;
            }
            this.contactoPrincipal = principal;
            this.tipoContactoFinal = !safe(tipoContacto, "").isEmpty()
                    ? safe(tipoContacto, "")
                    : inferirTipo(principal);
        }

        boolean hasData() {
            return !isBlank(nombre)
                    || !isBlank(usuario)
                    || !isBlank(contactoPrincipal)
                    || !isBlank(correo)
                    || !isBlank(telefono);
        }
    }
}

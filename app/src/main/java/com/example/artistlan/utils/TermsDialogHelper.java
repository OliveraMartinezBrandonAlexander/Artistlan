package com.example.artistlan.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;

public final class TermsDialogHelper {

    private TermsDialogHelper() {}

    private static final String TERMS_TEXT =
            "Bienvenido/a a Artistlan. Al crear una cuenta y utilizar esta aplicación, aceptas los presentes Términos y Condiciones, así como las reglas de uso, convivencia y seguridad establecidas para la plataforma.\n\n"
                    + "1. Uso de la plataforma\n"
                    + "Artistlan es una aplicación orientada a la publicación, exploración y gestión de obras, servicios artísticos, perfiles, portafolios, convocatorias, solicitudes y transacciones relacionadas con el arte. El usuario se compromete a utilizar la plataforma de forma responsable, respetuosa y conforme a la ley.\n\n"
                    + "2. Registro de cuenta\n"
                    + "Para utilizar determinadas funciones, el usuario deberá crear una cuenta proporcionando información verídica, actualizada y suficiente. El usuario es responsable de mantener la confidencialidad de sus credenciales de acceso y de las acciones realizadas desde su cuenta.\n\n"
                    + "3. Contenido publicado por usuarios\n"
                    + "El usuario declara que las obras, imágenes, descripciones, servicios, textos o cualquier contenido que publique en Artistlan son propios, cuentan con autorización suficiente o no infringen derechos de terceros. No se permite publicar contenido ofensivo, discriminatorio, fraudulento, ilegal, violento, sexual explícito, engañoso o que vulnere derechos de autor, marcas, privacidad o cualquier otro derecho.\n\n"
                    + "4. Derechos sobre el contenido\n"
                    + "El usuario conserva los derechos que le correspondan sobre el contenido que publique. Sin embargo, al publicar contenido en Artistlan, autoriza a la plataforma a mostrarlo dentro de la aplicación para fines de visualización, promoción interna, funcionamiento de perfiles, búsqueda, favoritos, portafolio, convocatorias y procesos relacionados con la interacción entre usuarios.\n\n"
                    + "5. Conducta del usuario\n"
                    + "El usuario se compromete a no acosar, amenazar, suplantar identidad, manipular transacciones, difundir información falsa, intentar vulnerar la seguridad del sistema, acceder a cuentas ajenas o utilizar la plataforma con fines maliciosos.\n\n"
                    + "6. Solicitudes, compras y transacciones\n"
                    + "Artistlan puede facilitar la comunicación entre usuarios, solicitudes, carrito, transacciones o integración con servicios de pago. El usuario acepta revisar cuidadosamente la información antes de confirmar cualquier operación. Las condiciones específicas de pago, entrega, comisión, reembolso o cancelación podrán depender de las reglas internas del proyecto y de los proveedores externos utilizados.\n\n"
                    + "7. Moderación y suspensión\n"
                    + "Artistlan podrá revisar reportes, moderar contenido, ocultar publicaciones, restringir funciones, suspender cuentas o aplicar medidas correctivas cuando se detecte incumplimiento de estos términos, uso indebido de la plataforma o afectación a otros usuarios.\n\n"
                    + "8. Privacidad y datos personales\n"
                    + "La información proporcionada por el usuario será utilizada para permitir el funcionamiento de la aplicación, la autenticación, la gestión de perfiles, publicaciones, interacciones, seguridad y comunicación dentro de la plataforma. Artistlan deberá manejar los datos conforme a las medidas de seguridad disponibles en el proyecto y evitar su uso para fines ajenos al funcionamiento de la aplicación.\n\n"
                    + "9. Seguridad\n"
                    + "El usuario acepta que debe proteger su cuenta, utilizar contraseñas seguras y notificar cualquier uso no autorizado. Artistlan podrá implementar mecanismos de seguridad como verificación, autenticación, recuperación de contraseña, control de sesión o medidas adicionales para proteger la plataforma.\n\n"
                    + "10. Cambios en la plataforma\n"
                    + "Artistlan puede actualizar, modificar, suspender o mejorar funciones de la aplicación para corregir errores, fortalecer la seguridad, agregar nuevas herramientas o mejorar la experiencia del usuario.\n\n"
                    + "11. Limitación de responsabilidad\n"
                    + "Artistlan se ofrece como plataforma tecnológica para apoyar la difusión y gestión de contenido artístico. La plataforma no garantiza resultados comerciales específicos, ventas, contrataciones, aceptación de solicitudes o disponibilidad permanente del servicio. El uso de la aplicación es responsabilidad del usuario.\n\n"
                    + "12. Aceptación\n"
                    + "Al marcar la casilla de aceptación y crear una cuenta, el usuario confirma que ha leído, comprendido y aceptado estos Términos y Condiciones.";

    public static void show(@NonNull Context context) {
        ThemeManager tm = new ThemeManager(context);
        int padding = dp(context, 20);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(padding, padding, padding, dp(context, 12));
        content.setBackground(DialogThemeHelper.createFieldDialogBackground(context));

        TextView title = new TextView(context);
        title.setText("Términos y Condiciones de Artistlan");
        title.setTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
        title.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        title.setTextSize(20f);
        content.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(false);
        TextView body = new TextView(context);
        body.setText(formatTermsText(tm));
        body.setTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
        body.setTextSize(14f);
        body.setLineSpacing(dp(context, 2), 1.0f);
        body.setPadding(0, dp(context, 14), 0, dp(context, 6));
        scroll.addView(body);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(context, 360)
        );
        content.addView(scroll, scrollParams);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(content)
                .setPositiveButton("Entendido", null)
                .create();
        dialog.setOnShowListener(d -> {
            DialogThemeHelper.styleLightGlassAlertDialog(dialog, context);
            DialogThemeHelper.applyFieldDialogWindowSize(dialog, context);
            Button ok = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            CardThemeHelper.applyPrimaryBubbleButton(ok, tm);
        });
        dialog.show();
    }

    @NonNull
    private static SpannableString formatTermsText(@NonNull ThemeManager tm) {
        SpannableString text = new SpannableString(TERMS_TEXT);
        String[] lines = TERMS_TEXT.split("\n");
        int cursor = 0;
        for (String line : lines) {
            int start = TERMS_TEXT.indexOf(line, cursor);
            if (start < 0) {
                cursor += line.length() + 1;
                continue;
            }
            int end = start + line.length();
            if (line.matches("\\d+\\.\\s.+")) {
                text.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.setSpan(new ForegroundColorSpan(tm.color(ThemeKeys.ACCENT_PRIMARY)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            cursor = end + 1;
        }
        return text;
    }

    private static int dp(@NonNull Context context, int value) {
        return Math.round(context.getResources().getDisplayMetrics().density * value);
    }
}

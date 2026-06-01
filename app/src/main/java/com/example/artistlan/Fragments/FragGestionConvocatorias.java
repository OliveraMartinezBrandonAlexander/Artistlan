package com.example.artistlan.Fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Admin.adapter.ConvocatoriaAdminAdapter;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.ConvocatoriaApi;
import com.example.artistlan.Conector.model.ConvocatoriaDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.utils.ArtistlanDialogFactory;
import com.example.artistlan.utils.CardThemeHelper;
import com.example.artistlan.utils.DialogConfig;
import com.example.artistlan.utils.DialogThemeHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import androidx.core.graphics.ColorUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragGestionConvocatorias extends Fragment {

    private static final Pattern FECHA_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private ConvocatoriaApi convocatoriaApi;
    private ConvocatoriaAdminAdapter adapter;
    private ThemeManager themeManager;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEstado;
    private ImageButton btnRegresar;
    private TextView tvTitulo;
    private FloatingActionButton btnNueva;
    private View menuInferior;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_admin_convocatorias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);
        themeManager = new ThemeManager(requireContext());

        convocatoriaApi = RetrofitClient.getClient().create(ConvocatoriaApi.class);

        btnRegresar = view.findViewById(R.id.btnRegresarAdminConvocatorias);
        tvTitulo = view.findViewById(R.id.tvTituloGestionConvocatorias);
        recyclerView = view.findViewById(R.id.rvConvocatoriasAdmin);
        progressBar = view.findViewById(R.id.pbConvocatorias);
        tvEstado = view.findViewById(R.id.tvEstadoConvocatorias);
        btnNueva = view.findViewById(R.id.fabNuevaConvocatoria);
        aplicarTemaVisual(view);

        menuInferior = requireActivity().findViewById(R.id.MenuInferiorFrame);
        if (menuInferior != null) menuInferior.setVisibility(View.GONE);

        btnRegresar.setOnClickListener(v -> {
            if (!NavHostFragment.findNavController(this).navigateUp()) {
                requireActivity().onBackPressed();
            }
        });

        adapter = new ConvocatoriaAdminAdapter(new ConvocatoriaAdminAdapter.AccionesListener() {
            @Override
            public void onEditar(ConvocatoriaDTO item) {
                mostrarDialogConvocatoria(item);
            }

            @Override
            public void onEliminar(ConvocatoriaDTO item) {
                confirmarEliminar(item);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        btnNueva.setOnClickListener(v -> animarNuevaConvocatoria(btnNueva));
        cargarConvocatorias();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (menuInferior != null) menuInferior.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }
        View view = getView();
        if (view != null) {
            ThemeModuleStyler.styleFragment(this, view);
        }
        if (!isAdded()) {
            return;
        }
        themeManager = new ThemeManager(requireContext());
        aplicarTemaVisual(view);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void aplicarTemaVisual(@Nullable View root) {
        if (themeManager == null) {
            return;
        }
        if (root != null) {
            ThemeModuleStyler.styleFragment(this, root);
        }
        CardThemeHelper.applyFilterButton(btnRegresar, themeManager);
        ThemeApplier.applyTextPrimary(tvTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvEstado, themeManager);
        CardThemeHelper.tintProgress(progressBar, themeManager);
        aplicarFabTema(btnNueva, themeManager);
    }

    private void cargarConvocatorias() {
        mostrarLoading(true);
        convocatoriaApi.getConvocatorias().enqueue(new Callback<List<ConvocatoriaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ConvocatoriaDTO>> call, @NonNull Response<List<ConvocatoriaDTO>> response) {
                mostrarLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    mostrarError("No se pudieron obtener las convocatorias");
                    return;
                }

                List<ConvocatoriaDTO> lista = response.body();
                adapter.actualizar(lista);
                boolean vacio = lista.isEmpty();
                tvEstado.setVisibility(vacio ? View.VISIBLE : View.GONE);
                tvEstado.setText(vacio ? "No hay convocatorias disponibles." : "");
            }

            @Override
            public void onFailure(@NonNull Call<List<ConvocatoriaDTO>> call, @NonNull Throwable t) {
                mostrarLoading(false);
                mostrarError("Error de conexión al obtener convocatorias.");
            }
        });
    }

    private void mostrarDialogConvocatoria(@Nullable ConvocatoriaDTO actual) {
        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_convocatoria_form, null, false);
        TextInputLayout tilTitulo = form.findViewById(R.id.tilTituloConvocatoria);
        TextInputLayout tilDescripcion = form.findViewById(R.id.tilDescripcionConvocatoria);
        TextInputLayout tilFecha = form.findViewById(R.id.tilFechaConvocatoria);
        TextInputLayout tilEnlace = form.findViewById(R.id.tilEnlaceConvocatoria);

        TextInputEditText etTitulo = form.findViewById(R.id.etTituloConvocatoria);
        TextInputEditText etDescripcion = form.findViewById(R.id.etDescripcionConvocatoria);
        TextInputEditText etFecha = form.findViewById(R.id.etFechaConvocatoria);
        TextInputEditText etEnlace = form.findViewById(R.id.etEnlaceConvocatoria);
        form.setBackgroundColor(Color.TRANSPARENT);

        boolean editando = actual != null;
        if (editando) {
            etTitulo.setText(actual.getTitulo());
            etDescripcion.setText(actual.getDescripcion());
            etFecha.setText(actual.getFecha());
            etEnlace.setText(actual.getEnlace());
        }

        etFecha.setOnClickListener(v -> mostrarDatePicker(etFecha));

        ThemeManager tm = new ThemeManager(requireContext());
        LinearLayout dialogContent = new LinearLayout(requireContext());
        dialogContent.setOrientation(LinearLayout.VERTICAL);
        dialogContent.setPadding(dpToPx(18), dpToPx(18), dpToPx(18), dpToPx(16));
        dialogContent.setBackground(DialogThemeHelper.createDialogBackground(requireContext()));

        TextView title = new TextView(requireContext());
        title.setText(editando ? "Editar convocatoria" : "Nueva convocatoria");
        title.setTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
        title.setTextSize(24f);
        title.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        dialogContent.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout.LayoutParams formParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        formParams.topMargin = dpToPx(14);
        dialogContent.addView(form, formParams);

        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(android.view.Gravity.END);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        actionsParams.topMargin = dpToPx(16);

        Button btnCancelar = new Button(requireContext());
        btnCancelar.setText("Cancelar");
        btnCancelar.setAllCaps(false);
        Button btnGuardar = new Button(requireContext());
        btnGuardar.setText(editando ? "Actualizar" : "Publicar");
        btnGuardar.setAllCaps(false);
        CardThemeHelper.applySecondaryBubbleButton(btnCancelar, tm);
        CardThemeHelper.applyPrimaryBubbleButton(btnGuardar, tm);

        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, dpToPx(46), 1f);
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, dpToPx(46), 1f);
        saveParams.leftMargin = dpToPx(10);
        actions.addView(btnCancelar, cancelParams);
        actions.addView(btnGuardar, saveParams);
        dialogContent.addView(actions, actionsParams);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogContent)
                .create();

        btnGuardar.setOnClickListener(v -> {
            limpiarErrores(tilTitulo, tilDescripcion, tilFecha, tilEnlace);

            String titulo = obtenerTexto(etTitulo);
            String descripcion = obtenerTexto(etDescripcion);
            String fecha = obtenerTexto(etFecha);
            String enlace = obtenerTexto(etEnlace);

            if (!validarFormulario(titulo, descripcion, fecha, enlace, tilTitulo, tilDescripcion, tilFecha, tilEnlace)) {
                return;
            }

            ConvocatoriaDTO body = new ConvocatoriaDTO();
            body.setTitulo(titulo);
            body.setDescripcion(descripcion);
            body.setFecha(fecha);
            body.setEnlace(enlace);

            if (editando && actual.getIdConvocatoria() != null) {
                actualizarConvocatoria(actual.getIdConvocatoria(), body);
            } else {
                crearConvocatoria(body);
            }
            dialog.dismiss();
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        DialogThemeHelper.applyFieldDialogWindowSize(dialog, requireContext());
        DialogThemeHelper.applyLightGlassTextInputLayoutStyle(tilTitulo, requireContext());
        DialogThemeHelper.applyLightGlassTextInputLayoutStyle(tilDescripcion, requireContext());
        DialogThemeHelper.applyLightGlassTextInputLayoutStyle(tilFecha, requireContext());
        DialogThemeHelper.applyLightGlassTextInputLayoutStyle(tilEnlace, requireContext());
        DialogThemeHelper.applyLightGlassTextInputEditTextStyle(etTitulo, requireContext());
        DialogThemeHelper.applyLightGlassTextInputEditTextStyle(etDescripcion, requireContext());
        DialogThemeHelper.applyLightGlassTextInputEditTextStyle(etFecha, requireContext());
        DialogThemeHelper.applyLightGlassTextInputEditTextStyle(etEnlace, requireContext());
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }

    private void mostrarDatePicker(TextInputEditText etFecha) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> etFecha.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
        DialogThemeHelper.styleDialogWindow(datePickerDialog, requireContext());
    }

    private String obtenerTexto(@Nullable TextInputEditText input) {
        if (input == null || input.getText() == null) return "";
        return input.getText().toString().trim();
    }

    private void limpiarErrores(TextInputLayout... fields) {
        for (TextInputLayout field : fields) {
            field.setError(null);
            field.setErrorEnabled(false);
        }
    }

    private boolean validarFormulario(
            String titulo,
            String descripcion,
            String fecha,
            String enlace,
            TextInputLayout tilTitulo,
            TextInputLayout tilDescripcion,
            TextInputLayout tilFecha,
            TextInputLayout tilEnlace
    ) {
        boolean valido = true;

        if (TextUtils.isEmpty(titulo)) {
            tilTitulo.setError("El título es obligatorio.");
            valido = false;
        }
        if (TextUtils.isEmpty(descripcion)) {
            tilDescripcion.setError("La descripción es obligatoria.");
            valido = false;
        }
        if (TextUtils.isEmpty(fecha)) {
            tilFecha.setError("La fecha es obligatoria.");
            valido = false;
        } else if (!FECHA_PATTERN.matcher(fecha).matches()) {
            tilFecha.setError("Usa formato yyyy-MM-dd.");
            valido = false;
        }
        if (TextUtils.isEmpty(enlace)) {
            tilEnlace.setError("El enlace es obligatorio.");
            valido = false;
        } else if (!(enlace.startsWith("http://") || enlace.startsWith("https://"))) {
            tilEnlace.setError("Debe iniciar con http:// o https://");
            valido = false;
        }

        if (!valido) {
            mostrarSnackbar("Revisa los campos marcados en rojo.");
        }
        return valido;
    }

    private void crearConvocatoria(ConvocatoriaDTO body) {
        mostrarLoading(true);
        convocatoriaApi.crearConvocatoria(body).enqueue(new Callback<ConvocatoriaDTO>() {
            @Override
            public void onResponse(@NonNull Call<ConvocatoriaDTO> call, @NonNull Response<ConvocatoriaDTO> response) {
                mostrarLoading(false);
                if (response.isSuccessful()) {
                    mostrarDialogoResultado("Convocatoria creada", "La convocatoria se publico correctamente.");
                    cargarConvocatorias();
                } else {
                    mostrarError("No se pudo crear la convocatoria.");
                    mostrarDialogoResultado("No se pudo crear", "Hubo un problema al crear la convocatoria.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ConvocatoriaDTO> call, @NonNull Throwable t) {
                mostrarLoading(false);
                mostrarError("Error de conexión al crear convocatoria.");
                mostrarDialogoResultado("Error de conexión", "No fue posible crear la convocatoria en este momento.");
            }
        });
    }

    private void actualizarConvocatoria(int id, ConvocatoriaDTO body) {
        mostrarLoading(true);
        convocatoriaApi.actualizarConvocatoria(id, body).enqueue(new Callback<ConvocatoriaDTO>() {
            @Override
            public void onResponse(@NonNull Call<ConvocatoriaDTO> call, @NonNull Response<ConvocatoriaDTO> response) {
                mostrarLoading(false);
                if (response.isSuccessful()) {
                    mostrarDialogoResultado("Convocatoria actualizada", "Los cambios se guardaron correctamente.");
                    cargarConvocatorias();
                } else {
                    mostrarError("No se pudo actualizar la convocatoria.");
                    mostrarDialogoResultado("No se pudo actualizar", "Hubo un problema al actualizar la convocatoria.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ConvocatoriaDTO> call, @NonNull Throwable t) {
                mostrarLoading(false);
                mostrarError("Error de conexión al actualizar convocatoria.");
                mostrarDialogoResultado("Error de conexión", "No fue posible actualizar la convocatoria en este momento.");
            }
        });
    }

    private void confirmarEliminar(ConvocatoriaDTO item) {
        ArtistlanDialogFactory.show(this, DialogConfig.builder()
                .setTitle("Eliminar convocatoria")
                .setMessage("¿Seguro que deseas eliminar?")
                .setType(DialogConfig.Type.DANGER)
                .setNegativeText("Cancelar")
                .setPositiveText("Eliminar")
                .setOnPositive(() -> {
                    if (item.getIdConvocatoria() != null) {
                        eliminarConvocatoria(item.getIdConvocatoria());
                    }
                })
                .build());
    }

    private void eliminarConvocatoria(int id) {
        mostrarLoading(true);
        convocatoriaApi.eliminarConvocatoria(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                mostrarLoading(false);
                if (response.isSuccessful()) {
                    mostrarSnackbar("Convocatoria eliminada.");
                    cargarConvocatorias();
                } else {
                    mostrarError("No se pudo eliminar la convocatoria.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                mostrarLoading(false);
                mostrarError("Error de conexión al eliminar convocatoria.");
            }
        });
    }

    private void mostrarLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void mostrarError(String mensaje) {
        tvEstado.setText(mensaje);
        tvEstado.setVisibility(View.VISIBLE);
        mostrarSnackbar(mensaje);
    }

    private void mostrarSnackbar(String mensaje) {
        View view = getView();
        if (view != null) Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG).show();
    }

    private void mostrarDialogoResultado(String titulo, String mensaje) {
        if (!isAdded()) {
            return;
        }
        ArtistlanDialogFactory.show(this, DialogConfig.builder()
                .setTitle(titulo)
                .setMessage(mensaje)
                .setType(DialogConfig.Type.INFO)
                .setPositiveText("Entendido")
                .build());
    }

    private void aplicarFabTema(@Nullable FloatingActionButton fab, @NonNull ThemeManager tm) {
        if (fab == null) {
            return;
        }
        int backgroundColor = tm.color(ThemeKeys.BUTTON_PRIMARY_BG);
        fab.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        fab.setImageTintList(ColorStateList.valueOf(resolveReadableTextColor(
                backgroundColor,
                tm.color(ThemeKeys.BUTTON_TEXT_DARK),
                tm.color(ThemeKeys.BUTTON_TEXT_LIGHT),
                tm.color(ThemeKeys.TEXT_PRIMARY),
                tm.color(ThemeKeys.TEXT_SECONDARY)
        )));
        fab.setRippleColor(tm.color(ThemeKeys.BUTTON_SECONDARY_BG));
    }

    private void animarNuevaConvocatoria(@NonNull FloatingActionButton fab) {
        fab.animate().cancel();
        fab.setEnabled(false);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(fab, View.ROTATION, 0f, 35f, 0f),
                ObjectAnimator.ofFloat(fab, View.SCALE_X, 1f, 1.04f, 1f),
                ObjectAnimator.ofFloat(fab, View.SCALE_Y, 1f, 1.04f, 1f)
        );
        set.setDuration(150);
        set.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                fab.setEnabled(true);
                if (isAdded()) {
                    mostrarDialogConvocatoria(null);
                }
            }
        });
        set.start();
    }

    private int resolveReadableTextColor(int backgroundColor, int preferred, int... candidates) {
        int opaqueBackground = ColorUtils.setAlphaComponent(backgroundColor, 255);
        if (safeContrast(preferred, opaqueBackground) >= 4.5d) {
            return preferred;
        }
        int selected = preferred;
        double bestContrast = safeContrast(preferred, opaqueBackground);
        for (int candidate : candidates) {
            double contrast = safeContrast(candidate, opaqueBackground);
            if (contrast > bestContrast) {
                bestContrast = contrast;
                selected = candidate;
            }
        }
        if (bestContrast >= 4.5d) {
            return selected;
        }
        double whiteContrast = safeContrast(Color.WHITE, opaqueBackground);
        double blackContrast = safeContrast(Color.BLACK, opaqueBackground);
        return whiteContrast >= blackContrast ? Color.WHITE : Color.BLACK;
    }

    private double safeContrast(int foregroundColor, int backgroundColor) {
        try {
            return ColorUtils.calculateContrast(foregroundColor, ColorUtils.setAlphaComponent(backgroundColor, 255));
        } catch (IllegalArgumentException ignored) {
            return 0d;
        }
    }
}

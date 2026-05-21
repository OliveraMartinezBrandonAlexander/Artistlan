package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.CategoriaApi;
import com.example.artistlan.Conector.api.ServicioApi;
import com.example.artistlan.Conector.model.CategoriaDTO;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.CardThemeHelper;
import com.example.artistlan.utils.DialogThemeHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragSubirServicio extends Fragment {
    private static final String TAG_CRUD = "ServicioCrudDebug";
    private static final String TAG_BACK_STACK = "MiArteBackStackDebug";

    public static final String ARG_MODO_EDICION = "modo_edicion";
    public static final String ARG_SERVICIO_ID = "servicio_id";

    private Spinner spinnerCategoriaServicio, spinnerTipoContacto;
    private EditText etTituloServicio, etDescripcionServicio, etTecnicaServicio, etContactoServicio, etPrecioMinServicio, etPrecioMaxServicio;
    private Button btnPublicarServicio;
    private ImageButton btnRegresarServicio;

    private final List<CategoriaDTO> listaCategoriasProfesiones = new ArrayList<>();
    private ArrayAdapter<String> categoriasAdapter;
    private boolean modoEdicion = false;
    private int idServicioEditar = -1;
    private String categoriaPendiente;
    private ServicioDTO servicioActual;
    private View topBarFrame;
    private View contentContainer;
    private ViewTreeObserver.OnPreDrawListener topBarOffsetListener;
    private boolean resultadoRegresoNotificado = false;

    public FragSubirServicio() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            modoEdicion = args.getBoolean(ARG_MODO_EDICION, false);
            idServicioEditar = args.getInt(ARG_SERVICIO_ID, -1);
        }
        Log.d(TAG_CRUD, "Entrada FragSubirServicio modo=" + (modoEdicion ? "editar" : "crear")
                + " idServicio=" + idServicioEditar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_frag_subir_servicio, container, false);
        ThemeModuleStyler.styleFragment(this, view);
        spinnerCategoriaServicio = view.findViewById(R.id.spinnerCategoriaServicio);
        spinnerTipoContacto = view.findViewById(R.id.spinnerTipoContacto);
        etTituloServicio = view.findViewById(R.id.etTituloServicio);
        etDescripcionServicio = view.findViewById(R.id.etDescripcionServicio);
        etTecnicaServicio = view.findViewById(R.id.etTecnicaServicio);
        etContactoServicio = view.findViewById(R.id.etContactoServicio);
        etPrecioMinServicio = view.findViewById(R.id.etPrecioMinServicio);
        etPrecioMaxServicio = view.findViewById(R.id.etPrecioMaxServicio);
        btnPublicarServicio = view.findViewById(R.id.btnPublicarServicio);
        btnRegresarServicio = view.findViewById(R.id.btnRegresarServicio);

        categoriasAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View item = super.getView(position, convertView, parent);
                tematizarSpinnerText(item, false, false);
                return item;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View item = super.getDropDownView(position, convertView, parent);
                tematizarSpinnerText(item, true, position == spinnerCategoriaServicio.getSelectedItemPosition());
                return item;
            }
        };
        categoriasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoriaServicio.setAdapter(categoriasAdapter);

        ArrayAdapter<String> tipoContactoAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item,
                Arrays.asList("Seleccione tipo de contacto", "EMAIL", "WHATSAPP", "INSTAGRAM", "TELEFONO", "OTRO")) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View item = super.getView(position, convertView, parent);
                tematizarSpinnerText(item, false, false);
                return item;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View item = super.getDropDownView(position, convertView, parent);
                tematizarSpinnerText(item, true, position == spinnerTipoContacto.getSelectedItemPosition());
                return item;
            }
        };
        tipoContactoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoContacto.setAdapter(tipoContactoAdapter);

        aplicarTemaFormulario(view);
        cargarCategoriasDesdeBD();
        configurarModoPantalla(view);
        if (modoEdicion) {
            cargarServicioParaEditar();
        }

        btnPublicarServicio.setOnClickListener(v -> validarYMostrarDialogo());

        btnRegresarServicio.setOnClickListener(v -> {
            Log.d(TAG_BACK_STACK, "Boton regresar FragSubirServicio sin guardar modo="
                    + (modoEdicion ? "editar" : "crear")
                    + " idServicio=" + idServicioEditar);
            notificarRegresoPortafolio(false);
            NavHostFragment.findNavController(this).popBackStack();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ScrollView scrollView = view.findViewById(R.id.fragScrollSubirServicio);
        contentContainer = view.findViewById(R.id.subirServicioContentContainer);
        topBarFrame = requireActivity().findViewById(R.id.topBarFrame);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;

            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    imeHeight + dpToPx(24)
            );
            return insets;
        });

        if (topBarFrame != null && contentContainer != null) {
            topBarOffsetListener = () -> {
                actualizarOffsetTopDinamico();
                return true;
            };
            topBarFrame.getViewTreeObserver().addOnPreDrawListener(topBarOffsetListener);
            actualizarOffsetTopDinamico();
        }
        View menuInferior = requireActivity().findViewById(R.id.MenuInferiorFrame);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (topBarFrame != null && topBarOffsetListener != null) {
            ViewTreeObserver observer = topBarFrame.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.removeOnPreDrawListener(topBarOffsetListener);
            }
        }
        topBarOffsetListener = null;
        topBarFrame = null;
        contentContainer = null;
        if (getActivity() == null) return;
        View menuInferior = getActivity().findViewById(R.id.MenuInferiorFrame);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.VISIBLE);
        }
        if (!resultadoRegresoNotificado && isRemoving()) {
            Log.d(TAG_BACK_STACK, "Salida FragSubirServicio por back sistema sin guardar modo="
                    + (modoEdicion ? "editar" : "crear")
                    + " idServicio=" + idServicioEditar);
            notificarRegresoPortafolio(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isAdded()) return;
        View menuInferior = requireActivity().findViewById(R.id.MenuInferiorFrame);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }
    }

    private void actualizarOffsetTopDinamico() {
        if (topBarFrame == null || contentContainer == null) {
            return;
        }
        int topBarHeight = topBarFrame.getHeight();
        int visibleTopBar = Math.max(0, topBarHeight + Math.round(topBarFrame.getTranslationY()));
        int topPadding = visibleTopBar + dpToPx(14);
        contentContainer.setPadding(
                contentContainer.getPaddingLeft(),
                topPadding,
                contentContainer.getPaddingRight(),
                contentContainer.getPaddingBottom()
        );
    }

    private int dpToPx(int dp) { return Math.round(dp * getResources().getDisplayMetrics().density); }

    private void configurarModoPantalla(View view) {
        if (!modoEdicion) return;
        ((TextView) view.findViewById(R.id.lsTxtTitulo)).setText("Editar Servicio");
        ((TextView) view.findViewById(R.id.lsTxtDesc)).setText("Actualiza la informaci\u00F3n de tu servicio. El precio no se puede editar.");
        btnPublicarServicio.setText("GUARDAR CAMBIOS");
        etPrecioMinServicio.setEnabled(false);
        etPrecioMaxServicio.setEnabled(false);
        etPrecioMinServicio.setFocusable(false);
        etPrecioMaxServicio.setFocusable(false);
        etPrecioMinServicio.setFocusableInTouchMode(false);
        etPrecioMaxServicio.setFocusableInTouchMode(false);
    }

    private void aplicarTemaFormulario(@NonNull View root) {
        ThemeManager tm = new ThemeManager(requireContext());
        aplicarBotonPrincipal(btnPublicarServicio, tm);
        CardThemeHelper.applyFilterButton(btnRegresarServicio, tm);
        aplicarTextosFormulario(root, tm);
        aplicarInputsFormulario(tm);
        aplicarTemaSpinner(spinnerCategoriaServicio);
        aplicarTemaSpinner(spinnerTipoContacto);
    }

    private void aplicarTextosFormulario(@NonNull View view, @NonNull ThemeManager tm) {
        if (view instanceof Button || view instanceof EditText) {
            return;
        }
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                aplicarTextosFormulario(group.getChildAt(i), tm);
            }
        }
    }

    private void aplicarInputsFormulario(@NonNull ThemeManager tm) {
        ThemeApplier.applyInput(etTituloServicio, tm);
        ThemeApplier.applyInput(etDescripcionServicio, tm);
        ThemeApplier.applyInput(etTecnicaServicio, tm);
        ThemeApplier.applyInput(etContactoServicio, tm);
        ThemeApplier.applyInput(etPrecioMinServicio, tm);
        ThemeApplier.applyInput(etPrecioMaxServicio, tm);
    }

    private void tematizarSpinnerText(@Nullable View item, boolean dropdown, boolean selected) {
        if (item instanceof TextView && isAdded()) {
            ThemeManager tm = new ThemeManager(requireContext());
            TextView textView = (TextView) item;
            textView.setTextColor(tm.color(selected ? ThemeKeys.ACCENT_PRIMARY : ThemeKeys.TEXT_PRIMARY));
            textView.setHintTextColor(tm.color(ThemeKeys.INPUT_HINT));
            textView.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));
            if (dropdown) {
                textView.setBackground(null);
                textView.setTypeface(Typeface.create("sans-serif-medium", selected ? Typeface.BOLD : Typeface.NORMAL));
            } else {
                textView.setBackground(null);
                textView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            }
        }
    }

    private void aplicarTemaSpinner(@Nullable Spinner spinner) {
        if (spinner == null || !isAdded()) return;
        ThemeManager tm = new ThemeManager(requireContext());
        spinner.setPopupBackgroundDrawable(crearFondoPanelDesplegable(tm));
        if (spinner.getBackground() instanceof GradientDrawable) {
            GradientDrawable bg = (GradientDrawable) spinner.getBackground().mutate();
            bg.setColor(tm.color(ThemeKeys.INPUT_BG));
            bg.setStroke(dpToPx(1), tm.color(ThemeKeys.INPUT_STROKE));
        } else if (spinner.getBackground() != null) {
            spinner.getBackground().mutate().setTint(tm.color(ThemeKeys.INPUT_BG));
        }
    }

    @NonNull
    private GradientDrawable crearFondoPanelDesplegable(@NonNull ThemeManager tm) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpToPx(18));
        drawable.setColor(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.FILTER_BUTTON_BG), 238));
        drawable.setStroke(dpToPx(1), tm.color(ThemeKeys.FILTER_BUTTON_STROKE));
        return drawable;
    }

    private void aplicarBotonPrincipal(@Nullable Button button, @NonNull ThemeManager tm) {
        if (button == null) return;
        button.setBackgroundResource(R.drawable.bg_btn_bubble_glass_primary);
        button.setAllCaps(false);
        button.setTextSize(17);
        button.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        button.setMinHeight(dpToPx(62));
        button.setPadding(dpToPx(18), 0, dpToPx(18), 0);
        ViewGroup.LayoutParams params = button.getLayoutParams();
        if (params != null) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = dpToPx(62);
            button.setLayoutParams(params);
        }
        ThemeApplier.applyPrimaryButton(button, tm);
    }

    private void cargarCategoriasDesdeBD() {
        CategoriaApi api = RetrofitClient.getClient().create(CategoriaApi.class);
        api.obtenerCategorias().enqueue(new Callback<List<CategoriaDTO>>() {
            @Override
            public void onResponse(Call<List<CategoriaDTO>> call, Response<List<CategoriaDTO>> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                listaCategoriasProfesiones.clear();

                for (CategoriaDTO c : response.body()) {
                    int id = c.getIdCategoria();
                    if (id >= 19 && id <= 37) {
                        listaCategoriasProfesiones.add(c);
                    }
                }
                actualizarSpinnerCategorias();
            }
            @Override
            public void onFailure(Call<List<CategoriaDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarSpinnerCategorias() {
        List<String> nombres = new ArrayList<>();
        nombres.add("Seleccione una categoría");

        for (CategoriaDTO c : listaCategoriasProfesiones) nombres.add(c.getNombreCategoria());
        categoriasAdapter.clear(); categoriasAdapter.addAll(nombres); categoriasAdapter.notifyDataSetChanged();
        seleccionarCategoriaPendiente();
    }


    private void cargarServicioParaEditar() {
        int idUsuario = obtenerIdUsuarioLogueado();
        Log.d(TAG_CRUD, "Cargar servicio edicion start GET servicios/{id}?usuarioId="
                + idUsuario + " idServicio=" + idServicioEditar);
        if (idUsuario <= 0 || idServicioEditar <= 0) {
            Log.w(TAG_CRUD, "Cargar servicio edicion abort idUsuario=" + idUsuario + " idServicio=" + idServicioEditar);
            Toast.makeText(getContext(), "No se pudo cargar el servicio.", Toast.LENGTH_LONG).show();
            return;
        }

        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        api.obtenerPorId(idServicioEditar, idUsuario).enqueue(new Callback<ServicioDTO>() {
            @Override public void onResponse(@NonNull Call<ServicioDTO> call, @NonNull Response<ServicioDTO> response) {
                if (!isAdded()) return;
                Log.d(TAG_CRUD, "Cargar servicio edicion response servicios/{id} code=" + response.code()
                        + " successful=" + response.isSuccessful()
                        + " bodyId=" + (response.body() != null ? response.body().getIdServicio() : null)
                        + " idServicio=" + idServicioEditar
                        + " usuarioId=" + idUsuario);
                if (!response.isSuccessful() || response.body() == null) {
                    cargarServicioParaEditarDesdePortafolio(idUsuario, response.code());
                    return;
                }
                precargarServicio(response.body());
            }
            @Override public void onFailure(@NonNull Call<ServicioDTO> call, @NonNull Throwable t) {
                Log.e(TAG_CRUD, "Cargar servicio edicion failure servicios/{id} idServicio=" + idServicioEditar + " usuarioId=" + idUsuario, t);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error de red al cargar el servicio.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void cargarServicioParaEditarDesdePortafolio(int idUsuario, int codigoDetalle) {
        Log.w(TAG_CRUD, "Fallback cargar servicio edicion GET portafolioPersonal/{usuarioId}"
                + " usuarioId=" + idUsuario
                + " idServicio=" + idServicioEditar
                + " detalleCode=" + codigoDetalle);
        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        api.obtenerServiciosDeUsuario(idUsuario).enqueue(new Callback<List<ServicioDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ServicioDTO>> call, @NonNull Response<List<ServicioDTO>> response) {
                if (!isAdded()) {
                    return;
                }
                Log.d(TAG_CRUD, "Fallback servicio edicion response code=" + response.code()
                        + " successful=" + response.isSuccessful()
                        + " bodySize=" + (response.body() != null ? response.body().size() : -1)
                        + " idServicio=" + idServicioEditar);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "No se pudo cargar el servicio.", Toast.LENGTH_LONG).show();
                    return;
                }
                for (ServicioDTO servicio : response.body()) {
                    if (servicio != null && servicio.getIdServicio() != null && servicio.getIdServicio() == idServicioEditar) {
                        precargarServicio(servicio);
                        return;
                    }
                }
                Log.w(TAG_CRUD, "Fallback servicio edicion no encontro idServicio=" + idServicioEditar
                        + " usuarioId=" + idUsuario
                        + " size=" + response.body().size());
                Toast.makeText(getContext(), "No se encontro el servicio para editar.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<List<ServicioDTO>> call, @NonNull Throwable t) {
                Log.e(TAG_CRUD, "Fallback servicio edicion failure idServicio=" + idServicioEditar + " usuarioId=" + idUsuario, t);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error de red al cargar el servicio.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void precargarServicio(@NonNull ServicioDTO s) {
        servicioActual = s;
        etTituloServicio.setText(s.getTitulo());
        etDescripcionServicio.setText(s.getDescripcion());
        etTecnicaServicio.setText(s.getTecnicas());
        etContactoServicio.setText(s.getContacto());
        etPrecioMinServicio.setText(s.getPrecioMin() != null ? String.valueOf(s.getPrecioMin()) : "");
        etPrecioMaxServicio.setText(s.getPrecioMax() != null ? String.valueOf(s.getPrecioMax()) : "");
        categoriaPendiente = s.getCategoria();
        seleccionarCategoriaPendiente();
        setSpinnerValue(spinnerTipoContacto, s.getTipoContacto());
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (value.equalsIgnoreCase(String.valueOf(spinner.getItemAtPosition(i)))) { spinner.setSelection(i); return; }
        }
    }

    private void seleccionarCategoriaPendiente() {
        if (categoriaPendiente == null || categoriaPendiente.trim().isEmpty() || listaCategoriasProfesiones.isEmpty()) {
            return;
        }

        for (int i = 0; i < listaCategoriasProfesiones.size(); i++) {
            if (categoriaPendiente.equalsIgnoreCase(listaCategoriasProfesiones.get(i).getNombreCategoria())) {
                spinnerCategoriaServicio.setSelection(i + 1);
                categoriaPendiente = null;
                return;
            }
        }
    }

    private int obtenerIdUsuarioLogueado() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("idUsuario", prefs.getInt("id", -1));
    }

    private void validarYMostrarDialogo() {
        int idUsuario = obtenerIdUsuarioLogueado();
        if (idUsuario <= 0) return;
        String titulo = etTituloServicio.getText().toString().trim();
        String descripcion = etDescripcionServicio.getText().toString().trim();
        String tecnica = etTecnicaServicio.getText().toString().trim();
        String contacto = etContactoServicio.getText().toString().trim();
        String tipoContacto = String.valueOf(spinnerTipoContacto.getSelectedItem());
        String minTxt = etPrecioMinServicio.getText().toString().trim();
        String maxTxt = etPrecioMaxServicio.getText().toString().trim();

        if (TextUtils.isEmpty(titulo)) { etTituloServicio.setError("Ingresa un t\u00EDtulo"); return; }
        if (TextUtils.isEmpty(descripcion)) { etDescripcionServicio.setError("Ingresa una descripci\u00F3n"); return; }
        if (TextUtils.isEmpty(tecnica)) { etTecnicaServicio.setError("Indica t\u00E9cnica"); return; }

        int posCategoria = spinnerCategoriaServicio.getSelectedItemPosition();
        if (posCategoria <= 0 || posCategoria > listaCategoriasProfesiones.size()) {
            if (!(modoEdicion && servicioActual != null && servicioActual.getIdCategoria() != null)) {
                Toast.makeText(requireContext(), "Selecciona una categor\u00EDa v\u00E1lida", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (spinnerTipoContacto.getSelectedItemPosition() <= 0) {
            Toast.makeText(requireContext(), "Selecciona un tipo de contacto", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validarContacto(tipoContacto, contacto)) return;

        Double min;
        Double max;
        if (modoEdicion) {
            min = servicioActual != null ? servicioActual.getPrecioMin() : null;
            max = servicioActual != null ? servicioActual.getPrecioMax() : null;
        } else {
            if (TextUtils.isEmpty(minTxt)) {
                etPrecioMinServicio.setError("Ingresa un precio m\u00EDnimo");
                etPrecioMinServicio.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(maxTxt)) {
                etPrecioMaxServicio.setError("Ingresa un precio m\u00E1ximo");
                etPrecioMaxServicio.requestFocus();
                return;
            }
            min = parsePrecio(minTxt, etPrecioMinServicio, "Precio m\u00EDnimo inv\u00E1lido");
            max = parsePrecio(maxTxt, etPrecioMaxServicio, "Precio m\u00E1ximo inv\u00E1lido");
            if (min == null || max == null) {
                return;
            }
            if (min >= max) {
                etPrecioMaxServicio.setError("El precio m\u00E1ximo debe ser mayor al m\u00EDnimo");
                etPrecioMaxServicio.requestFocus();
                return;
            }
        }

        CategoriaDTO categoriaSeleccionada = null;
        if (posCategoria > 0 && posCategoria <= listaCategoriasProfesiones.size()) {
            categoriaSeleccionada = listaCategoriasProfesiones.get(posCategoria - 1);
        } else if (modoEdicion && servicioActual != null && servicioActual.getIdCategoria() != null) {
            categoriaSeleccionada = new CategoriaDTO();
            categoriaSeleccionada.setIdCategoria(servicioActual.getIdCategoria());
            categoriaSeleccionada.setNombreCategoria(servicioActual.getCategoria());
        }

        mostrarDialogConfirmacion(idUsuario, categoriaSeleccionada, titulo, descripcion, tecnica, contacto, tipoContacto, min, max);
    }

    private boolean validarContacto(String tipo, String contacto) {
        if (TextUtils.isEmpty(contacto)) { etContactoServicio.setError("Ingresa un contacto"); return false; }
        String v = contacto.trim();
        switch (tipo.toUpperCase(Locale.ROOT)) {
            case "EMAIL":
                if (!Patterns.EMAIL_ADDRESS.matcher(v).matches()) { etContactoServicio.setError("Email inválido"); return false; }
                break;
            case "WHATSAPP":
            case "TELEFONO":
                if (!v.matches("^[+]?\\d{7,15}$")) { etContactoServicio.setError("Número inválido"); return false; }
                break;
            case "INSTAGRAM":
                if (v.length() < 2) { etContactoServicio.setError("Usuario de Instagram inválido"); return false; }
                break;
            default:
                if (v.length() < 2) { etContactoServicio.setError("Contacto inválido"); return false; }
        }
        return true;
        }
    private Double parsePrecio(String txt, EditText target, String error) {
        if (TextUtils.isEmpty(txt)) return null;
        try {
            double value = Double.parseDouble(txt);
            if (value < 0) throw new NumberFormatException();
            return value;
        } catch (Exception ex) {
            target.setError(error);
            target.requestFocus();
            return null;
        }

    }

    private void mostrarDialogConfirmacion(int idUsuario, CategoriaDTO categoria, String titulo, String descripcion, String tecnica, String contacto, String tipoContacto, Double min, Double max) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirmar_servicio, null);

        ThemeManager tm = new ThemeManager(requireContext());
        TextView txtResumen = view.findViewById(R.id.txtResumenServicio);
        Button btnEditar = view.findViewById(R.id.btnEditar);
        Button btnPublicar = view.findViewById(R.id.btnConfirmarPublicar);

        ThemeApplier.applyTextPrimary(txtResumen, tm);
        ThemeApplier.applySecondaryButton(btnEditar, tm);
        ThemeApplier.applyPrimaryButton(btnPublicar, tm);

        String categoriaTxt = categoria != null ? categoria.getNombreCategoria() : "Sin cambio";
        String precioTxt = (min == null && max == null) ? "A convenir" : ((min != null ? min : "-") + " / " + (max != null ? max : "-"));
        txtResumen.setText("Título:\n" + titulo
                + "\n\nDescripci\u00F3n:\n" + descripcion
                + "\n\nT\u00E9cnica:\n" + tecnica
                + "\n\nContacto:\n" + tipoContacto + " - " + contacto
                + "\n\nRango de precio (este campo no se puede actualizar):\n" + precioTxt
                + "\n\nCategor\u00EDa:\n" + categoriaTxt);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext()).setView(view).setCancelable(false).create();

        btnEditar.setOnClickListener(v -> dialog.dismiss());
        btnPublicar.setOnClickListener(v -> {
            dialog.dismiss();

            ServicioDTO servicio = new ServicioDTO();
            servicio.setTitulo(titulo);
            servicio.setDescripcion(descripcion);
            servicio.setTecnicas(tecnica);
            servicio.setContacto(contacto);
            servicio.setTipoContacto(tipoContacto);
            if (!modoEdicion) {
                servicio.setPrecioMin(min);
                servicio.setPrecioMax(max);
            }
            servicio.setIdUsuario(idUsuario);
            boolean enviarCategoria = categoria != null;
            if (modoEdicion && categoria != null && servicioActual != null && servicioActual.getIdCategoria() != null) {
                enviarCategoria = !servicioActual.getIdCategoria().equals(categoria.getIdCategoria());
            }
            if (enviarCategoria) {
                servicio.setIdCategoria(categoria.getIdCategoria());
                servicio.setCategoria(categoria.getNombreCategoria());
            }
            guardarServicio(idUsuario, servicio);
        });
        dialog.show();
        DialogThemeHelper.styleDialogWindow(dialog, requireContext());
        DialogThemeHelper.styleButtonPair(btnPublicar, btnEditar, requireContext());
        if (dialog.getWindow() != null && dialog.getWindow().getDecorView() != null) {
            ThemeApplier.applyCardContainer(dialog.getWindow().getDecorView(), tm);
        }
    }
    private void guardarServicio(int idUsuario, ServicioDTO servicio) {
        if (modoEdicion) actualizarServicioEnBD(idUsuario, servicio);
        else guardarServicioEnBD(idUsuario, servicio);
    }

    private void guardarServicioEnBD(int idUsuario, ServicioDTO servicio) {
        ServicioApi servicioApi = RetrofitClient.getClient().create(ServicioApi.class);
        Log.d(TAG_CRUD, "Crear servicio POST portafolioPersonal/{usuarioId} usuarioId=" + idUsuario);
        servicioApi.crearServicioDeUsuario(idUsuario, servicio).enqueue(new Callback<ServicioDTO>() {
            @Override public void onResponse(@NonNull Call<ServicioDTO> call, @NonNull Response<ServicioDTO> response) {
                Log.d(TAG_CRUD, "Crear servicio response code=" + response.code()
                        + " successful=" + response.isSuccessful()
                        + " bodyId=" + (response.body() != null ? response.body().getIdServicio() : null));
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Servicio subido con ?xito", Toast.LENGTH_LONG).show();
                    notificarRefreshPortafolio();
                    NavHostFragment.findNavController(FragSubirServicio.this).popBackStack();
                    return;
                }
                String backendMessage = ApiErrorParser.extractMessage(response);
                Toast.makeText(getContext(),
                        backendMessage != null ? backendMessage : "Error al insertar servicio " + response.code(),
                        Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(@NonNull Call<ServicioDTO> call, @NonNull Throwable t) {
                Log.e(TAG_CRUD, "Crear servicio failure usuarioId=" + idUsuario, t);
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarServicioEnBD(int idUsuario, ServicioDTO servicio) {
        if (idServicioEditar <= 0) {
            Log.w(TAG_CRUD, "Actualizar servicio abort idServicio=" + idServicioEditar + " usuarioId=" + idUsuario);
            Toast.makeText(getContext(), "No se pudo actualizar el servicio.", Toast.LENGTH_LONG).show();
            return;
        }
        ServicioApi servicioApi = RetrofitClient.getClient().create(ServicioApi.class);
        Log.d(TAG_CRUD, "Actualizar servicio PUT portafolioPersonal/{usuarioId}/{idServicio}"
                + " usuarioId=" + idUsuario
                + " idServicio=" + idServicioEditar);
        servicioApi.actualizarServicioUsuario(idUsuario, idServicioEditar, servicio).enqueue(new Callback<ServicioDTO>() {

            @Override public void onResponse(@NonNull Call<ServicioDTO> call, @NonNull Response<ServicioDTO> response) {
                Log.d(TAG_CRUD, "Actualizar servicio response code=" + response.code()
                        + " successful=" + response.isSuccessful()
                        + " bodyId=" + (response.body() != null ? response.body().getIdServicio() : null)
                        + " idServicio=" + idServicioEditar);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Servicio actualizado con ?xito", Toast.LENGTH_LONG).show();
                    notificarRefreshPortafolio();
                    NavHostFragment.findNavController(FragSubirServicio.this).popBackStack();
                    return;
                }
                String backendMessage = ApiErrorParser.extractMessage(response);
                Toast.makeText(getContext(),
                        backendMessage != null ? backendMessage : "Error al actualizar servicio " + response.code(),
                        Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(@NonNull Call<ServicioDTO> call, @NonNull Throwable t) {
                Log.e(TAG_CRUD, "Actualizar servicio failure idServicio=" + idServicioEditar + " usuarioId=" + idUsuario, t);
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void notificarRefreshPortafolio() {
        notificarRegresoPortafolio(true);
    }

    private void notificarRegresoPortafolio(boolean guardado) {
        if (resultadoRegresoNotificado && !guardado) {
            return;
        }
        resultadoRegresoNotificado = true;
        String modo = modoEdicion ? "editar_servicio" : "crear_servicio";
        Log.d(guardado ? TAG_CRUD : TAG_BACK_STACK, "Notificar regreso portafolio target=servicios"
                + " guardado=" + guardado
                + " modo=" + modo
                + " idServicio=" + idServicioEditar);
        Bundle result = new Bundle();
        result.putString(FragPortafolio.RESULT_EXTRA_TARGET, FragPortafolio.TARGET_SERVICIOS);
        result.putBoolean(FragPortafolio.RESULT_EXTRA_GUARDADO, guardado);
        result.putString(FragPortafolio.RESULT_EXTRA_MODO, modo);
        getParentFragmentManager().setFragmentResult(FragPortafolio.RESULT_KEY_PORTAFOLIO_REFRESH, result);

        androidx.navigation.NavController navController = NavHostFragment.findNavController(this);
        androidx.navigation.NavBackStackEntry previousEntry = navController.getPreviousBackStackEntry();
        if (previousEntry != null) {
            previousEntry.getSavedStateHandle().set(
                    FragPortafolio.RESULT_EXTRA_TARGET,
                    FragPortafolio.TARGET_SERVICIOS
            );
            previousEntry.getSavedStateHandle().set(
                    FragPortafolio.RESULT_EXTRA_GUARDADO,
                    guardado
            );
            previousEntry.getSavedStateHandle().set(
                    FragPortafolio.RESULT_EXTRA_MODO,
                    modo
            );
        }
    }
}




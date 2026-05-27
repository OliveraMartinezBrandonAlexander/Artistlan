package com.example.artistlan.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.CategoriaApi;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.model.CategoriaDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.repository.FirebaseImageRepository;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeEffectsApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.CardThemeHelper;
import com.example.artistlan.utils.DialogThemeHelper;
import com.example.artistlan.utils.LottieFeedbackDialog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragSubirObra extends Fragment implements View.OnClickListener {

    private static final String TAG_CRUD = "ObraCrudDebug";
    private static final String TAG_BACK_STACK = "MiArteBackStackDebug";

    public static final String ARG_MODO_EDICION = "modo_edicion";
    public static final String ARG_OBRA_ID = "obra_id";

    private Button btnSubirImg;
    private Button btnSubirObra;
    private ImageButton btnRegresar;
    private ImageView imgPreviewObra;

    private Uri uriImagenObra;
    private FirebaseImageRepository firebaseRepo;
    private ActivityResultLauncher<String> seleccionarImagenObraLauncher;
    private ActivityResultLauncher<Void> tomarFotoLauncher;
    private ActivityResultLauncher<String> permisoCamaraLauncher;

    private EditText etTituloObra, etDescripcion, etPrecio, etMedidaAncho, etMedidaAlto, etMedidaProfundidad, etTecnicas;
    private CheckBox cbAutoriaObra;
    private RadioGroup rgOpciones;
    private RadioGroup rgTipoMedida;
    private RadioButton rbMedida2d;
    private RadioButton rbMedida3d;
    private Spinner spinnerCategoria;
    private LinearLayout layoutMedidaProfundidad;
    private List<CategoriaDTO> listaCategorias = new ArrayList<>();
    private boolean modoEdicion = false;
    private int idObraEditar = -1;
    private ObraDTO obraActual;
    private String imagenActualUrl;
    private Integer categoriaPendienteId;
    private String categoriaPendienteNombre;
    private TextView txtTituloPantalla;
    private TextView txtDescripcionPantalla;
    private TextView txtPrecio;
    private View topBarFrame;
    private View contentContainer;
    private boolean resultadoRegresoNotificado = false;
    private boolean envioEnCurso = false;
    private LottieFeedbackDialog feedbackDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            modoEdicion = args.getBoolean(ARG_MODO_EDICION, false);
            idObraEditar = args.getInt(ARG_OBRA_ID, -1);
        }
        Log.d(TAG_CRUD, "Entrada FragSubirObra modo=" + (modoEdicion ? "editar" : "crear")
                + " idObra=" + idObraEditar);

        firebaseRepo = new FirebaseImageRepository(requireContext());

        seleccionarImagenObraLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                uriImagenObra = uri;
                                if (imgPreviewObra != null) {
                                    Glide.with(this).load(uri).into(imgPreviewObra);
                                }
                            }
                        }
                );
        tomarFotoLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.TakePicturePreview(),
                        bitmap -> {
                            if (bitmap != null) {
                                Uri imagenCameraUri = guardarBitmapEnCache(bitmap);
                                if (imagenCameraUri == null) {
                                    Toast.makeText(getContext(), "No se pudo procesar la foto tomada.", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                uriImagenObra = imagenCameraUri;
                                if (imgPreviewObra != null) {
                                    Glide.with(this).load(uriImagenObra).into(imgPreviewObra);
                                }
                            }
                        }
                );

        permisoCamaraLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                tomarFotoLauncher.launch(null);
                            } else if (isAdded()) {
                                Toast.makeText(getContext(), "Debes conceder permiso de cámara para tomar fotos.", Toast.LENGTH_LONG).show();
                            }
                        }
                );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_frag_subir_obra, container, false);
        ThemeModuleStyler.styleFragment(this, view);

        etTituloObra = view.findViewById(R.id.tituloObra);
        rgOpciones   = view.findViewById(R.id.radioGroupOpciones);
        etDescripcion = view.findViewById(R.id.descripcion);
        etPrecio      = view.findViewById(R.id.precio);
        etMedidaAncho = view.findViewById(R.id.medidaAncho);
        etMedidaAlto = view.findViewById(R.id.medidaAlto);
        etMedidaProfundidad = view.findViewById(R.id.medidaProfundidad);
        etTecnicas    = view.findViewById(R.id.edit_text_tecnica);
        cbAutoriaObra = view.findViewById(R.id.checkAutoriaObra);
        txtTituloPantalla = view.findViewById(R.id.IsTxtTitulo);
        txtDescripcionPantalla = view.findViewById(R.id.IsTxtDesc);
        rgTipoMedida = view.findViewById(R.id.radioGroupTipoMedida);
        rbMedida2d = view.findViewById(R.id.rbMedida2d);
        rbMedida3d = view.findViewById(R.id.rbMedida3d);
        layoutMedidaProfundidad = view.findViewById(R.id.layoutMedidaProfundidad);

        spinnerCategoria = view.findViewById(R.id.categoria);
        cargarCategorias();

        imgPreviewObra = view.findViewById(R.id.imgPreviewObra);
        btnSubirImg = view.findViewById(R.id.btnSubirImg);
        btnSubirObra = view.findViewById(R.id.btnSubirObra);
        btnRegresar = view.findViewById(R.id.btnRegresar);
        aplicarTemaFormulario(view);

        btnSubirImg.setOnClickListener(v -> mostrarOpcionesImagen());

        btnSubirObra.setOnClickListener(v ->
                validarYMostrarDialogoObra()
        );


        txtPrecio = view.findViewById(R.id.IsTxtPrecio);

        txtPrecio.setVisibility(View.GONE);
        etPrecio.setVisibility(View.GONE);
        configurarModoPantalla();
        aplicarTemaBotones();
        if (modoEdicion) {
            cargarObraParaEditar();
        }

        rgOpciones.setOnCheckedChangeListener((group, checkedId) ->
                actualizarBloquePrecioSegunEstado(checkedId)
        );
        rgTipoMedida.setOnCheckedChangeListener((group, checkedId) ->
                actualizarCamposMedidas()
        );
        actualizarCamposMedidas();

        return view;
    }

    private void mostrarOpcionesImagen() {
        if (!isAdded()) return;
        if (modoEdicion) {
            Toast.makeText(getContext(), "Las im\u00E1genes no se pueden modificar despu\u00E9s de publicar la obra.", Toast.LENGTH_LONG).show();
            return;
        }

        String[] opciones = {"Elegir de galería", "Tomar foto con cámara"};

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Selecciona una opci\u00F3n")
                .setItems(opciones, (d, which) -> {
                    if (which == 0) {
                        Toast.makeText(getContext(), "Se recomienda una imagen en formato 4:3 para mejor visualización. No se deformará tu imagen.", Toast.LENGTH_SHORT).show();
                        seleccionarImagenObraLauncher.launch("image/*");
                    } else if (which == 1) {
                        abrirCamaraConPermiso();
                    }
                })
                .show();
        DialogThemeHelper.styleAlertDialog(dialog, requireContext());
    }

    private void abrirCamaraConPermiso() {
        if (!isAdded()) return;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            tomarFotoLauncher.launch(null);
            return;
        }

        permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
    }

    @Nullable
    private Uri guardarBitmapEnCache(@NonNull Bitmap bitmap) {
        if (getContext() == null) return null;

        File picturesDir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "obras");
        if (!picturesDir.exists() && !picturesDir.mkdirs()) {
            return null;
        }

        File imageFile = new File(picturesDir, "obra_" + System.currentTimeMillis() + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            return FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    imageFile
            );
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    private void cargarCategorias() {

        CategoriaApi api = RetrofitClient.getClient().create(CategoriaApi.class);

        api.obtenerCategorias().enqueue(new Callback<List<CategoriaDTO>>() {
            @Override
            public void onResponse(Call<List<CategoriaDTO>> call, Response<List<CategoriaDTO>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "No se pudieron obtener las categorías", Toast.LENGTH_LONG).show();
                    return;
                }

                listaCategorias = response.body();

                List<CategoriaDTO> filtradas = new ArrayList<>();

                for (CategoriaDTO c : listaCategorias) {
                    int id = c.getIdCategoria();
                    if (id >= 1 && id <= 18) {
                        filtradas.add(c);
                    }
                }

                listaCategorias = filtradas;

                List<String> nombres = new ArrayList<>();
                nombres.add("Seleccione una categoría");

                for (CategoriaDTO c : listaCategorias) {
                    nombres.add(c.getNombreCategoria());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_spinner_item,
                        nombres
                ) {
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
                        tematizarSpinnerText(item, true, position == spinnerCategoria.getSelectedItemPosition());
                        return item;
                    }
                };

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategoria.setAdapter(adapter);
                aplicarTemaSpinner(spinnerCategoria);
                seleccionarCategoriaPendiente();
            }

            @Override
            public void onFailure(Call<List<CategoriaDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void configurarModoPantalla() {
        if (!modoEdicion) {
            return;
        }
        txtTituloPantalla.setText("Editar Obra");
        txtDescripcionPantalla.setText("Actualiza la informaci\u00F3n de tu obra. Las im\u00E1genes no pueden modificarse tras su publicaci\u00F3n.");
        btnSubirImg.setVisibility(View.GONE);
        btnSubirImg.setEnabled(false);
        btnSubirObra.setText("GUARDAR CAMBIOS");
        if (cbAutoriaObra != null) {
            cbAutoriaObra.setChecked(true);
            cbAutoriaObra.setError(null);
        }
    }

    private void aplicarTemaFormulario(@NonNull View root) {
        ThemeManager tm = new ThemeManager(requireContext());
        aplicarTemaBotones(tm);
        CardThemeHelper.applyFilterButton(btnRegresar, tm);

        ThemeApplier.applyTextPrimary(txtTituloPantalla, tm);
        ThemeApplier.applyTextSecondary(txtDescripcionPantalla, tm);
        aplicarTextosFormulario(root, tm);
        aplicarInputsFormulario(tm);
        aplicarOpcionesFiltro(rgOpciones, tm);
        aplicarOpcionesFiltro(rgTipoMedida, tm);
        aplicarCheckBoxFiltro(cbAutoriaObra, tm);
        aplicarTemaSpinner(spinnerCategoria);
    }

    private void aplicarTemaBotones() {
        aplicarTemaBotones(new ThemeManager(requireContext()));
    }

    private void aplicarTemaBotones(@NonNull ThemeManager tm) {
        aplicarBotonPrincipal(btnSubirObra, tm);
        aplicarBotonSecundario(btnSubirImg, tm);
    }

    private void aplicarTextosFormulario(@NonNull View view, @NonNull ThemeManager tm) {
        if (view instanceof Button || view instanceof EditText) {
            return;
        }
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            int color = textView == txtDescripcionPantalla
                    ? tm.color(ThemeKeys.TEXT_SECONDARY)
                    : tm.color(ThemeKeys.TEXT_PRIMARY);
            textView.setTextColor(color);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                aplicarTextosFormulario(group.getChildAt(i), tm);
            }
        }
    }

    private void aplicarInputsFormulario(@NonNull ThemeManager tm) {
        ThemeApplier.applyInput(etTituloObra, tm);
        ThemeApplier.applyInput(etDescripcion, tm);
        ThemeApplier.applyInput(etPrecio, tm);
        ThemeApplier.applyInput(etMedidaAncho, tm);
        ThemeApplier.applyInput(etMedidaAlto, tm);
        ThemeApplier.applyInput(etMedidaProfundidad, tm);
        ThemeApplier.applyInput(etTecnicas, tm);
    }

    private void aplicarOpcionesFiltro(@Nullable RadioGroup group, @NonNull ThemeManager tm) {
        if (group == null) return;
        group.setBackground(null);
        ColorStateList tint = new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{tm.color(ThemeKeys.ACCENT_PRIMARY), tm.color(ThemeKeys.FILTER_BUTTON_STROKE)}
        );
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                rb.setTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
                rb.setButtonTintList(tint);
            }
        }
    }

    private void aplicarCheckBoxFiltro(@Nullable CheckBox checkBox, @NonNull ThemeManager tm) {
        if (checkBox == null) return;
        checkBox.setTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        checkBox.setButtonTintList(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{tm.color(ThemeKeys.ACCENT_PRIMARY), tm.color(ThemeKeys.FILTER_BUTTON_STROKE)}
        ));
        checkBox.setBackground(null);
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
        aplicarFormaBotonMain(button, R.drawable.bg_btn_bubble_glass_primary);
        int backgroundColor = tm.color(ThemeKeys.BUTTON_PRIMARY_BG);
        int strokeColor = elegirColorTextoBoton(
                backgroundColor,
                tm.color(ThemeKeys.BUTTON_TEXT_DARK),
                tm.color(ThemeKeys.BUTTON_TEXT_LIGHT),
                tm.color(ThemeKeys.TEXT_PRIMARY),
                tm.color(ThemeKeys.TEXT_SECONDARY)
        );
        aplicarFondoBotonTema(button, backgroundColor, strokeColor);
        aplicarTextoBotonTema(button, backgroundColor, tm.color(ThemeKeys.BUTTON_TEXT_DARK),
                tm.color(ThemeKeys.BUTTON_TEXT_LIGHT), tm.color(ThemeKeys.TEXT_PRIMARY), tm.color(ThemeKeys.TEXT_SECONDARY));
    }

    private void aplicarBotonSecundario(@Nullable Button button, @NonNull ThemeManager tm) {
        aplicarFormaBotonMain(button, R.drawable.bg_btn_bubble_glass_secondary);
        int backgroundColor = tm.color(ThemeKeys.BUTTON_SECONDARY_BG);
        int strokeColor = elegirColorTextoBoton(
                backgroundColor,
                tm.color(ThemeKeys.BUTTON_TEXT_LIGHT),
                tm.color(ThemeKeys.BUTTON_TEXT_DARK),
                tm.color(ThemeKeys.TEXT_PRIMARY),
                tm.color(ThemeKeys.TEXT_SECONDARY)
        );
        aplicarFondoBotonTema(button, backgroundColor, strokeColor);
        aplicarTextoBotonTema(button, backgroundColor, tm.color(ThemeKeys.BUTTON_TEXT_LIGHT),
                tm.color(ThemeKeys.BUTTON_TEXT_DARK), tm.color(ThemeKeys.TEXT_PRIMARY), tm.color(ThemeKeys.TEXT_SECONDARY));
    }

    private void aplicarFormaBotonMain(@Nullable Button button, int backgroundRes) {
        if (button == null) return;
        button.setBackgroundResource(backgroundRes);
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
    }

    private void aplicarFondoBotonTema(@Nullable Button button, int backgroundColor, int textColor) {
        if (button == null) return;
        button.setBackgroundTintList(null);
        button.setBackground(crearFondoBubbleTema(backgroundColor, textColor));
        ThemeApplier.animatePress(button);
    }

    @NonNull
    private LayerDrawable crearFondoBubbleTema(int backgroundColor, int textColor) {
        float radius = dpToPx(40);

        GradientDrawable shadow = new GradientDrawable();
        shadow.setShape(GradientDrawable.RECTANGLE);
        shadow.setColor(ColorUtils.setAlphaComponent(Color.BLACK, 42));
        shadow.setCornerRadius(radius);

        GradientDrawable fill = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        ColorUtils.blendARGB(backgroundColor, Color.WHITE, 0.24f),
                        backgroundColor,
                        ColorUtils.blendARGB(backgroundColor, Color.BLACK, 0.10f)
                }
        );
        fill.setShape(GradientDrawable.RECTANGLE);
        fill.setCornerRadius(radius);
        fill.setStroke(dpToPx(1), ColorUtils.setAlphaComponent(textColor, 90));

        GradientDrawable highlight = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{ColorUtils.setAlphaComponent(Color.WHITE, 78), Color.TRANSPARENT}
        );
        highlight.setShape(GradientDrawable.RECTANGLE);
        highlight.setCornerRadius(dpToPx(30));

        LayerDrawable drawable = new LayerDrawable(new android.graphics.drawable.Drawable[]{shadow, fill, highlight});
        drawable.setLayerInset(0, 0, dpToPx(7), 0, 0);
        drawable.setLayerInset(2, dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(34));
        return drawable;
    }

    private void aplicarTextoBotonTema(@Nullable Button button, int backgroundColor, int preferredTextColor, int... themeCandidates) {
        if (button == null) return;
        button.setTextColor(elegirColorTextoBotonTema(backgroundColor, preferredTextColor, themeCandidates));
    }

    private int elegirColorTextoBotonTema(int backgroundColor, int preferredTextColor, int... themeCandidates) {
        if (ColorUtils.calculateContrast(preferredTextColor, backgroundColor) >= 3.0d) {
            return preferredTextColor;
        }
        int selected = preferredTextColor;
        double bestThemeContrast = ColorUtils.calculateContrast(preferredTextColor, backgroundColor);
        for (int candidate : themeCandidates) {
            double contrast = ColorUtils.calculateContrast(candidate, backgroundColor);
            if (contrast > bestThemeContrast) {
                bestThemeContrast = contrast;
                selected = candidate;
            }
        }
        if (bestThemeContrast >= 3.0d) {
            return selected;
        }
        return elegirColorTextoBoton(backgroundColor, selected);
    }

    private int elegirColorTextoBoton(int backgroundColor, int preferredTextColor, int... themeCandidates) {
        if (ColorUtils.calculateContrast(preferredTextColor, backgroundColor) >= 4.5d) {
            return preferredTextColor;
        }
        int selected = preferredTextColor;
        double bestContrast = ColorUtils.calculateContrast(preferredTextColor, backgroundColor);
        for (int candidate : themeCandidates) {
            double contrast = ColorUtils.calculateContrast(candidate, backgroundColor);
            if (contrast > bestContrast) {
                bestContrast = contrast;
                selected = candidate;
            }
        }
        if (bestContrast >= 4.5d) {
            return selected;
        }
        double contrastWhite = ColorUtils.calculateContrast(Color.WHITE, backgroundColor);
        double contrastBlack = ColorUtils.calculateContrast(Color.BLACK, backgroundColor);
        return contrastWhite >= contrastBlack ? Color.WHITE : Color.BLACK;
    }

    private void cargarObraParaEditar() {
        int idUsuario = obtenerIdUsuarioLogueado();
        Log.d(TAG_CRUD, "Cargar obra edicion start GET obras/{id}?usuarioId="
                + idUsuario + " idObra=" + idObraEditar);
        if (idUsuario <= 0 || idObraEditar <= 0) {
            Log.w(TAG_CRUD, "Cargar obra edicion abort idUsuario=" + idUsuario + " idObra=" + idObraEditar);
            Toast.makeText(getContext(), "No se pudo cargar la obra.", Toast.LENGTH_LONG).show();
            return;
        }

        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        api.obtenerObraPorId(idObraEditar, idUsuario).enqueue(new Callback<ObraDTO>() {
            @Override
            public void onResponse(@NonNull Call<ObraDTO> call, @NonNull Response<ObraDTO> response) {
                if (!isAdded()) {
                    return;
                }
                Log.d(TAG_CRUD, "Cargar obra edicion response obras/{id} code=" + response.code()
                        + " successful=" + response.isSuccessful()
                        + " bodyId=" + (response.body() != null ? response.body().getIdObra() : null)
                        + " idObra=" + idObraEditar
                        + " usuarioId=" + idUsuario);
                if (!response.isSuccessful() || response.body() == null) {
                    cargarObraParaEditarDesdePortafolio(idUsuario, response.code());
                    return;
                }

                obraActual = response.body();
                precargarObra(obraActual);
            }

            @Override
            public void onFailure(@NonNull Call<ObraDTO> call, @NonNull Throwable t) {
                Log.e(TAG_CRUD, "Cargar obra edicion failure obras/{id} idObra=" + idObraEditar + " usuarioId=" + idUsuario, t);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error de red al cargar la obra.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void precargarObra(ObraDTO obra) {
        etTituloObra.setText(obra.getTitulo());
        etDescripcion.setText(obra.getDescripcion());
        cargarCamposDeMedidas(obra.getMedidas());
        etTecnicas.setText(obra.getTecnicas());

        String estado = obra.getEstado();
        String estadoNormalizado = estado != null ? estado.trim().toLowerCase().replace("_", " ") : "";
        if (obra.getPrecio() != null) {
            etPrecio.setText(String.valueOf(obra.getPrecio()));
        } else {
            etPrecio.setText("");
        }

        if (estadoNormalizado.contains("venta")) {
            rgOpciones.check(R.id.rbdventa);
            actualizarBloquePrecioSegunEstado(R.id.rbdventa);
        } else {
            rgOpciones.check(R.id.rbexhibicion);
            actualizarBloquePrecioSegunEstado(R.id.rbexhibicion);
        }

        imagenActualUrl = obra.getImagen1();
        if (imagenActualUrl != null && !imagenActualUrl.isEmpty()) {
            Glide.with(this)
                    .load(imagenActualUrl)
                    .placeholder(R.drawable.imagensubirobra)
                    .into(imgPreviewObra);
        }

        categoriaPendienteId = obra.getIdCategoria();
        categoriaPendienteNombre = obra.getNombreCategoria();
        seleccionarCategoriaPendiente();
    }

    private void seleccionarCategoriaPendiente() {
        if (listaCategorias.isEmpty()) {
            return;
        }

        for (int i = 0; i < listaCategorias.size(); i++) {
            CategoriaDTO categoria = listaCategorias.get(i);
            boolean coincideId = categoriaPendienteId != null && categoriaPendienteId.equals(categoria.getIdCategoria());
            boolean coincideNombre = categoriaPendienteNombre != null
                    && categoria.getNombreCategoria() != null
                    && categoria.getNombreCategoria().equalsIgnoreCase(categoriaPendienteNombre);
            if (coincideId || coincideNombre) {
                spinnerCategoria.setSelection(i + 1);
                categoriaPendienteId = null;
                categoriaPendienteNombre = null;
                return;
            }
        }
    }

    private void cargarCamposDeMedidas(String medidas) {
        if (medidas == null || medidas.trim().isEmpty()) {
            limpiarCamposMedidas();
            if (rbMedida2d != null) {
                rbMedida2d.setChecked(true);
            }
            actualizarCamposMedidas();
            return;
        }

        String limpio = medidas
                .replaceAll("(?i)cm", "")
                .trim();
        String[] partes = limpio.split("(?i)\\s*x\\s*");

        if (partes.length >= 3) {
            if (rbMedida3d != null) {
                rbMedida3d.setChecked(true);
            }
            etMedidaAncho.setText(normalizarCampoMedida(partes[0]));
            etMedidaAlto.setText(normalizarCampoMedida(partes[1]));
            etMedidaProfundidad.setText(normalizarCampoMedida(partes[2]));
            actualizarCamposMedidas();
            return;
        }

        if (rbMedida2d != null) {
            rbMedida2d.setChecked(true);
        }
        if (partes.length >= 2) {
            etMedidaAncho.setText(normalizarCampoMedida(partes[0]));
            etMedidaAlto.setText(normalizarCampoMedida(partes[1]));
        } else {
            etMedidaAncho.setText(normalizarCampoMedida(limpio));
            etMedidaAlto.setText("");
        }
        etMedidaProfundidad.setText("");
        actualizarCamposMedidas();
    }

    private String construirMedidas() {
        String ancho = normalizarCampoMedida(etMedidaAncho.getText().toString());
        String alto = normalizarCampoMedida(etMedidaAlto.getText().toString());
        if (ancho.isEmpty() || alto.isEmpty()) {
            return "";
        }
        if (esMedida3DSeleccionada()) {
            String profundidad = normalizarCampoMedida(etMedidaProfundidad.getText().toString());
            if (profundidad.isEmpty()) {
                return "";
            }
            return ancho + " x " + alto + " x " + profundidad + " cm";
        }
        return ancho + " x " + alto + " cm";
    }

    private String normalizarCampoMedida(String valor) {
        if (valor == null) return "";
        return valor
                .replaceAll("(?i)cm", "")
                .trim();
    }

    private void actualizarCamposMedidas() {
        if (layoutMedidaProfundidad == null || etMedidaProfundidad == null) {
            return;
        }

        boolean es3D = esMedida3DSeleccionada();
        layoutMedidaProfundidad.setVisibility(es3D ? View.VISIBLE : View.GONE);
        if (!es3D) {
            etMedidaProfundidad.setError(null);
            etMedidaProfundidad.setText("");
        }
    }

    private boolean esMedida3DSeleccionada() {
        return rgTipoMedida != null && rgTipoMedida.getCheckedRadioButtonId() == R.id.rbMedida3d;
    }

    private void limpiarCamposMedidas() {
        if (etMedidaAncho != null) etMedidaAncho.setText("");
        if (etMedidaAlto != null) etMedidaAlto.setText("");
        if (etMedidaProfundidad != null) etMedidaProfundidad.setText("");
    }

    private CategoriaDTO obtenerCategoriaSeleccionada() {
        int pos = spinnerCategoria.getSelectedItemPosition();
        if (pos > 0 && pos <= listaCategorias.size()) {
            return listaCategorias.get(pos - 1);
        }

        if (modoEdicion && obraActual != null && obraActual.getIdCategoria() != null) {
            CategoriaDTO categoria = new CategoriaDTO();
            categoria.setIdCategoria(obraActual.getIdCategoria());
            categoria.setNombreCategoria(obraActual.getNombreCategoria());
            return categoria;
        }

        return null;
    }

    private int obtenerIdUsuarioLogueado() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("idUsuario", prefs.getInt("id", -1));
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ScrollView scrollView = view.findViewById(R.id.fragScrollSubirObra);
        contentContainer = view.findViewById(R.id.subirObraContentContainer);
        topBarFrame = requireActivity().findViewById(R.id.topBarFrame);

        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    imeHeight
            );
            return insets;
        });

        if (topBarFrame != null && contentContainer != null) {
            contentContainer.post(this::actualizarOffsetTopDinamico);
        }

        View menuInferior = requireActivity().findViewById(R.id.MenuInferiorFrame);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }

        btnRegresar = view.findViewById(R.id.btnRegresar);
        btnRegresar.setOnClickListener(this);

        new BotonesMenuSuperior(this);
        feedbackDialog = new LottieFeedbackDialog(requireContext());
    }

    @Override
    public void onDestroyView() {
        if (feedbackDialog != null) {
            feedbackDialog.release();
            feedbackDialog = null;
        }
        envioEnCurso = false;
        super.onDestroyView();
        topBarFrame = null;
        contentContainer = null;
        if (getActivity() == null) return;
        View menuInferior = getActivity().findViewById(R.id.MenuInferiorFrame);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.VISIBLE);
        }
        if (!resultadoRegresoNotificado && isRemoving()) {
            Log.d(TAG_BACK_STACK, "Salida FragSubirObra por back sistema sin guardar modo="
                    + (modoEdicion ? "editar" : "crear")
                    + " idObra=" + idObraEditar);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnRegresar) {
            Log.d(TAG_BACK_STACK, "Boton regresar FragSubirObra sin guardar modo="
                    + (modoEdicion ? "editar" : "crear")
                    + " idObra=" + idObraEditar);
            notificarRegresoPortafolio(false);
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    private void actualizarOffsetTopDinamico() {
        if (topBarFrame == null || contentContainer == null) {
            return;
        }
        int topBarHeight = topBarFrame.getHeight();
        int topPadding = Math.max(topBarHeight, dpToPx(56)) + dpToPx(14);
        contentContainer.setPadding(
                contentContainer.getPaddingLeft(),
                topPadding,
                contentContainer.getPaddingRight(),
                contentContainer.getPaddingBottom()
        );
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }


    private void validarYMostrarDialogoObra() {
        limpiarErroresValidacionObra();

        if (uriImagenObra == null && (!modoEdicion || imagenActualUrl == null || imagenActualUrl.trim().isEmpty())) {
            Toast.makeText(getContext(), "Selecciona una imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarConfirmacionAutoriaObligatoria()) {
            return;
        }

        int idUsuario = obtenerIdUsuarioLogueado();
        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error de usuario.", Toast.LENGTH_LONG).show();
            return;
        }

        String titulo = etTituloObra.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String medidas = construirMedidas();
        String tecnica = etTecnicas.getText().toString().trim();

        int radioId = rgOpciones.getCheckedRadioButtonId();
        if (!validarCamposObligatoriosObra(titulo, descripcion, tecnica, medidas, radioId)) {
            return;
        }

        CategoriaDTO categoria = obtenerCategoriaSeleccionada();
        if (categoria == null) {
            marcarErrorCategoria("Selecciona una categoría.");
            return;
        }

        RadioButton rb = rgOpciones.findViewById(radioId);
        String estado = rb.getText().toString();
        String estadoNormalizado = estado.trim().toLowerCase().replace("_", " ");
        boolean esVenta = estadoNormalizado.contains("venta");

        Double precio = modoEdicion && obraActual != null ? obraActual.getPrecio() : null;
        boolean puedeAsignarPrecioEnEdicion = puedeAsignarPrecioPrimeraVezEnEdicion();
        if (esVenta && (!modoEdicion || puedeAsignarPrecioEnEdicion)) {
            if (precioStr.isEmpty()) {
                etPrecio.setError("Debes ingresar un precio para obras en venta.");
                etPrecio.requestFocus();
                return;
            }
            try {
                precio = Double.parseDouble(precioStr);
                if (precio < 0) {
                    etPrecio.setError("El precio no puede ser negativo.");
                    etPrecio.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etPrecio.setError("Precio inválido.");
                etPrecio.requestFocus();
                return;
            }
        }

        mostrarDialogConfirmacionObra(
                idUsuario, titulo, descripcion, estado,
                tecnica, medidas, precio, categoria
        );
    }

    private void mostrarDialogConfirmacionObra(
            int idUsuario,
            String titulo,
            String descripcion,
            String estado,
            String tecnica,
            String medidas,
            Double precio,
            CategoriaDTO categoria
    ) {

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_confirmar_obra, null);

        ThemeManager tm = new ThemeManager(requireContext());
        TextView txtResumen = view.findViewById(R.id.txtResumenObra);
        Button btnEditar = view.findViewById(R.id.btnEditar);
        Button btnPublicar = view.findViewById(R.id.btnConfirmarPublicar);

        ThemeApplier.applyTextPrimary(txtResumen, tm);
        ThemeApplier.applySecondaryButton(btnEditar, tm);
        ThemeApplier.applyPrimaryButton(btnPublicar, tm);

        String resumen =
                "Título:\n" + titulo + "\n\n" +
                        "Descripción:\n" + descripcion + "\n\n" +
                        "Estado:\n" + estado + "\n\n" +
                        "Técnica:\n" + tecnica + "\n\n" +
                        (esEstadoVenta(estado)
                                ? "Precio (este campo no se puede actualizar):\n"
                                + (precio != null ? "$" + precio : "Sin precio")
                                + "\n\n"
                                : ""
                        ) +
                        (!medidas.isEmpty()
                                ? "Medidas:\n" + medidas + "\n\n"
                                : ""
                        ) +
                        "Categoría:\n" + categoria.getNombreCategoria();

        txtResumen.setText(resumen);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(false)
                .create();

        btnEditar.setOnClickListener(v -> dialog.dismiss());

        btnPublicar.setOnClickListener(v -> {
            if (envioEnCurso) {
                return;
            }
            btnPublicar.setEnabled(false);
            btnPublicar.setText(modoEdicion ? "Guardando..." : "Publicando...");

            dialog.dismiss();

            guardarObra();
        });

        dialog.show();
        DialogThemeHelper.styleDialogWindow(dialog, requireContext());
        DialogThemeHelper.styleButtonPair(btnPublicar, btnEditar, requireContext());
        if (dialog.getWindow() != null && dialog.getWindow().getDecorView() != null) {
            ThemeEffectsApplier.applyPanelGlass(dialog.getWindow().getDecorView(), tm);
        }
    }

    private void limpiarErroresValidacionObra() {
        if (etTituloObra != null) etTituloObra.setError(null);
        if (etDescripcion != null) etDescripcion.setError(null);
        if (etTecnicas != null) etTecnicas.setError(null);
        if (etMedidaAncho != null) etMedidaAncho.setError(null);
        if (etMedidaAlto != null) etMedidaAlto.setError(null);
        if (etMedidaProfundidad != null) etMedidaProfundidad.setError(null);
        if (etPrecio != null) etPrecio.setError(null);
        if (cbAutoriaObra != null) cbAutoriaObra.setError(null);

        View root = getView();
        if (root != null) {
            RadioButton rbVenta = root.findViewById(R.id.rbdventa);
            RadioButton rbExhibicion = root.findViewById(R.id.rbexhibicion);
            if (rbVenta != null) rbVenta.setError(null);
            if (rbExhibicion != null) rbExhibicion.setError(null);
        }

        View selected = spinnerCategoria != null ? spinnerCategoria.getSelectedView() : null;
        if (selected instanceof TextView) {
            ((TextView) selected).setError(null);
        }
    }

    private boolean validarCamposObligatoriosObra(
            String titulo,
            String descripcion,
            String tecnica,
            String medidas,
            int radioId
    ) {
        boolean hayError = false;

        if (titulo.isEmpty()) {
            etTituloObra.setError("Ingresa un t\u00EDtulo");
            etTituloObra.requestFocus();
            hayError = true;
        }

        if (descripcion.isEmpty()) {
            etDescripcion.setError("Ingresa una descripci\u00F3n");
            if (!hayError) etDescripcion.requestFocus();
            hayError = true;
        }

        if (tecnica.isEmpty()) {
            etTecnicas.setError("Ingresa una t\u00E9cnica");
            if (!hayError) etTecnicas.requestFocus();
            hayError = true;
        }

        if (medidas.isEmpty()) {
            String ancho = normalizarCampoMedida(etMedidaAncho.getText().toString());
            String alto = normalizarCampoMedida(etMedidaAlto.getText().toString());
            String profundidad = normalizarCampoMedida(
                    etMedidaProfundidad != null ? etMedidaProfundidad.getText().toString() : ""
            );
            if (ancho.isEmpty()) {
                etMedidaAncho.setError("Ingresa el ancho");
                if (!hayError) etMedidaAncho.requestFocus();
                hayError = true;
            }
            if (alto.isEmpty()) {
                etMedidaAlto.setError("Ingresa el alto");
                if (!hayError) etMedidaAlto.requestFocus();
                hayError = true;
            }
            if (esMedida3DSeleccionada() && profundidad.isEmpty()) {
                etMedidaProfundidad.setError("Ingresa la profundidad");
                if (!hayError) etMedidaProfundidad.requestFocus();
                hayError = true;
            }
        }

        if (radioId == -1) {
            View root = getView();
            if (root != null) {
                RadioButton rbVenta = root.findViewById(R.id.rbdventa);
                if (rbVenta != null) {
                    rbVenta.setError("Selecciona un estado");
                    if (!hayError) rbVenta.requestFocus();
                }
            }
            hayError = true;
        }

        return !hayError;
    }

    private void marcarErrorCategoria(String mensaje) {
        if (spinnerCategoria == null) return;
        View selected = spinnerCategoria.getSelectedView();
        if (selected instanceof TextView) {
            TextView selectedText = (TextView) selected;
            selectedText.setError(mensaje);
            selectedText.requestFocus();
            return;
        }
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    private boolean esEstadoVenta(String estado) {
        if (estado == null) return false;
        String estadoNormalizado = estado.trim().toLowerCase().replace("_", " ");
        return estadoNormalizado.contains("venta");
    }


    private void subirObraCompleta() {
        if (uriImagenObra == null) {
            Toast.makeText(getContext(), "Primero selecciona una imagen para la obra.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));

        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error: No se encontró ID de usuario.", Toast.LENGTH_LONG).show();
            return;
        }

        String titulo = etTituloObra.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String medidas = construirMedidas();
        String tecnica = etTecnicas.getText().toString().trim();

        int pos = spinnerCategoria.getSelectedItemPosition();
        if (pos == 0) {
            Toast.makeText(getContext(), "Selecciona una categoría válida.", Toast.LENGTH_LONG).show();
            return;
        }

        int categoriaId = listaCategorias.get(pos - 1).getIdCategoria();

        int radioId = rgOpciones.getCheckedRadioButtonId();

        if (titulo.isEmpty() || descripcion.isEmpty()  || radioId == -1 || tecnica.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos obligatorios", Toast.LENGTH_LONG).show();
            return;
        }

        Double precioDouble = null;

        if (!precioStr.isEmpty()) {
            try {
                precioDouble = Double.parseDouble(precioStr);
                if (precioDouble < 0) {
                    Toast.makeText(getContext(), "El precio no puede ser negativo.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Formato de precio inválido.", Toast.LENGTH_LONG).show();
                return;
            }
        }


        RadioButton rb = rgOpciones.findViewById(radioId);
        String estado = rb.getText().toString();

        Toast.makeText(getContext(), "Cargando...", Toast.LENGTH_SHORT).show();

        final Double precioFinal = precioDouble;
        firebaseRepo.subirImagenSolo(idUsuario, uriImagenObra, new FirebaseImageRepository.ImagenListener() {

            @Override
            public void onSuccess(String imageUrl) {

                ObraDTO nuevaObra = new ObraDTO();
                nuevaObra.setTitulo(titulo);
                nuevaObra.setDescripcion(descripcion);
                nuevaObra.setEstado(estado);
                nuevaObra.setTecnicas(tecnica);

                if (precioFinal != null) {
                    nuevaObra.setPrecio(precioFinal);
                }

                nuevaObra.setMedidas(medidas);
                nuevaObra.setIdCategoria(categoriaId);
                nuevaObra.setImagen1(imageUrl);
                nuevaObra.setLikes(0);

                insertarObraEnBD(idUsuario, nuevaObra);
            }

            @Override
            public void onError(String mensajeError) {
                Toast.makeText(getContext(), "Error al subir imagen: " + mensajeError, Toast.LENGTH_LONG).show();
            }
        });
    }

        private void guardarObra() {
        if (!validarConfirmacionAutoriaObligatoria()) {
            return;
        }
        int idUsuario = obtenerIdUsuarioLogueado();
        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error: No se encontró ID de usuario.", Toast.LENGTH_LONG).show();
            return;
        }
        if (envioEnCurso) {
            return;
        }
        envioEnCurso = true;
        actualizarAccionesEnvio(false);

        if (!modoEdicion && uriImagenObra != null) {
            mostrarFeedbackCarga("Publicando obra...");
            firebaseRepo.subirImagenSolo(idUsuario, uriImagenObra, new FirebaseImageRepository.ImagenListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    if (!persistirObra(idUsuario, imageUrl)) {
                        cerrarFeedbackYRestaurarEnvio();
                    }
                }

                @Override
                public void onError(String mensajeError) {
                    mostrarFeedbackError("No se pudo publicar la obra");
                    Toast.makeText(getContext(), "Error al subir imagen: " + mensajeError, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        mostrarFeedbackCarga(modoEdicion ? "Actualizando obra..." : "Publicando obra...");
        if (!persistirObra(idUsuario, imagenActualUrl)) {
            cerrarFeedbackYRestaurarEnvio();
        }
    }

    private boolean persistirObra(int idUsuario, String imageUrl) {
        String titulo = etTituloObra.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String medidas = construirMedidas();
        String tecnica = etTecnicas.getText().toString().trim();
        int radioId = rgOpciones.getCheckedRadioButtonId();

        CategoriaDTO categoria = obtenerCategoriaSeleccionada();
        if (categoria == null || categoria.getIdCategoria() <= 0) {
            Toast.makeText(getContext(), "Selecciona una categor\u00EDa v\u00E1lida.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (titulo.isEmpty() || descripcion.isEmpty() || medidas.isEmpty() || radioId == -1 || tecnica.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos obligatorios", Toast.LENGTH_LONG).show();
            return false;
        }

        RadioButton rb = rgOpciones.findViewById(radioId);
        String estado = rb.getText().toString();
        String estadoNormalizado = estado.trim().toLowerCase().replace("_", " ");
        boolean esVenta = estadoNormalizado.contains("venta");

        Double precioDouble = null;
        boolean puedeAsignarPrecioEnEdicion = puedeAsignarPrecioPrimeraVezEnEdicion();
        if (!modoEdicion || puedeAsignarPrecioEnEdicion) {
            if (esVenta && precioStr.isEmpty()) {
                Toast.makeText(getContext(), "Debes ingresar un precio para obras en venta.", Toast.LENGTH_LONG).show();
                return false;
            }
            if (!precioStr.isEmpty()) {
                try {
                    precioDouble = Double.parseDouble(precioStr);
                    if (precioDouble < 0) {
                        Toast.makeText(getContext(), "El precio no puede ser negativo.", Toast.LENGTH_LONG).show();
                        return false;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Formato de precio inv\u00E1lido.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        Integer categoriaIdParaEnviar = null;
        if (!modoEdicion) {
            categoriaIdParaEnviar = categoria.getIdCategoria();
        } else if (obraActual == null || obraActual.getIdCategoria() == null
                || !obraActual.getIdCategoria().equals(categoria.getIdCategoria())) {
            categoriaIdParaEnviar = categoria.getIdCategoria();
        }

        ObraDTO obra = new ObraDTO();
        obra.setTitulo(titulo);
        obra.setDescripcion(descripcion);
        obra.setEstado(estado);
        obra.setTecnicas(tecnica);
        if (!modoEdicion || puedeAsignarPrecioEnEdicion) {
            obra.setPrecio(precioDouble);
        }
        obra.setMedidas(medidas);
        if (categoriaIdParaEnviar != null) {
            obra.setIdCategoria(categoriaIdParaEnviar);
        }
        obra.setImagen1(imageUrl);
        obra.setIdUsuario(idUsuario);

        if (obraActual != null) {
            obra.setImagen2(obraActual.getImagen2());
            obra.setImagen3(obraActual.getImagen3());
            obra.setLikes(obraActual.getLikes() != null ? obraActual.getLikes() : 0);
        } else {
            obra.setLikes(0);
        }

        if (modoEdicion) {
            actualizarObraEnBD(idUsuario, obra);
        } else {
            insertarObraEnBD(idUsuario, obra);
        }
        return true;
    }

    private void actualizarBloquePrecioSegunEstado(int checkedId) {
        boolean esVenta = checkedId == R.id.rbdventa;
        if (esVenta) {
            txtPrecio.setVisibility(View.VISIBLE);
            etPrecio.setVisibility(View.VISIBLE);

            boolean editable = !modoEdicion || puedeAsignarPrecioPrimeraVezEnEdicion();
            etPrecio.setEnabled(editable);
            etPrecio.setFocusable(editable);
            etPrecio.setFocusableInTouchMode(editable);
            aplicarIndicadorBloqueoPrecio(!editable);

            etPrecio.animate().alpha(1f).setDuration(200);
            txtPrecio.animate().alpha(1f).setDuration(200);
            return;
        }

        txtPrecio.setVisibility(View.GONE);
        etPrecio.setVisibility(View.GONE);
        aplicarIndicadorBloqueoPrecio(false);
        if (!modoEdicion) {
            etPrecio.setText("");
        }
        etPrecio.animate().alpha(0f).setDuration(200);
        txtPrecio.animate().alpha(0f).setDuration(200);
    }

    private boolean puedeAsignarPrecioPrimeraVezEnEdicion() {
        if (!modoEdicion || obraActual == null) {
            return false;
        }
        Double precioActual = obraActual.getPrecio();
        return precioActual == null || precioActual <= 0d;
    }

    private void aplicarIndicadorBloqueoPrecio(boolean bloqueado) {
        if (txtPrecio == null || etPrecio == null) {
            return;
        }
        txtPrecio.setText(bloqueado
                ? "Precio en pesos mxn (bloqueado)"
                : "Precio en pesos mxn");
        etPrecio.setAlpha(bloqueado ? 0.65f : 1f);
    }

    private void actualizarAccionesEnvio(boolean habilitado) {
        if (btnSubirObra != null) {
            btnSubirObra.setEnabled(habilitado);
        }
        if (btnSubirImg != null) {
            btnSubirImg.setEnabled(habilitado && !modoEdicion);
        }
        if (btnRegresar != null) {
            btnRegresar.setEnabled(habilitado);
        }
    }

    private void mostrarFeedbackCarga(String mensaje) {
        if (feedbackDialog != null) {
            feedbackDialog.showLoading(mensaje);
        }
    }

    private void mostrarFeedbackExito(String mensaje, Runnable onDismiss) {
        envioEnCurso = false;
        actualizarAccionesEnvio(true);
        if (feedbackDialog != null) {
            feedbackDialog.showSuccess(mensaje, onDismiss);
        } else if (onDismiss != null) {
            onDismiss.run();
        }
    }

    private void mostrarFeedbackError(String mensaje) {
        envioEnCurso = false;
        actualizarAccionesEnvio(true);
        if (feedbackDialog != null) {
            feedbackDialog.showError(mensaje);
        }
    }

    private void cerrarFeedbackYRestaurarEnvio() {
        envioEnCurso = false;
        actualizarAccionesEnvio(true);
        if (feedbackDialog != null) {
            feedbackDialog.dismiss();
        }
    }

    private void insertarObraEnBD(int idUsuario, ObraDTO obra) {
        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        Log.d(TAG_CRUD, "Crear obra POST obrasDeUsuario/{usuarioId} usuarioId=" + idUsuario);
        Call<ObraDTO> call = api.subirObra(idUsuario, obra);

        call.enqueue(new Callback<ObraDTO>() {
            @Override
            public void onResponse(@NonNull Call<ObraDTO> call, @NonNull Response<ObraDTO> response) {
                Log.d(TAG_CRUD, "Crear obra response code=" + response.code()
                        + " successful=" + response.isSuccessful()
                        + " bodyId=" + (response.body() != null ? response.body().getIdObra() : null));
                if (response.isSuccessful() && response.body() != null) {
                    FragMiArte.invalidarCacheUsuario(idUsuario);
                    notificarRefreshPortafolio();
                    mostrarFeedbackExito("Obra publicada", () ->
                            NavHostFragment.findNavController(FragSubirObra.this).popBackStack()
                    );
                } else {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    mostrarFeedbackError("No se pudo publicar la obra");
                    Toast.makeText(getContext(),
                            backendMessage != null ? backendMessage : "Error al insertar obra. C\u00F3digo " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ObraDTO> call, @NonNull Throwable t) {
                Log.e(TAG_CRUD, "Crear obra failure usuarioId=" + idUsuario, t);
                mostrarFeedbackError("No se pudo publicar la obra");
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void actualizarObraEnBD(int idUsuario, ObraDTO obra) {
        if (idObraEditar <= 0) {
            Log.w(TAG_CRUD, "Actualizar obra abort idObra=" + idObraEditar + " usuarioId=" + idUsuario);
            mostrarFeedbackError("No se pudo actualizar la obra");
            Toast.makeText(getContext(), "No se pudo actualizar la obra.", Toast.LENGTH_LONG).show();
            return;
        }

        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        Log.d(TAG_CRUD, "Actualizar obra PUT obrasDeUsuario/{usuarioId}/{obraId}"
                + " usuarioId=" + idUsuario
                + " idObra=" + idObraEditar);
        api.actualizarObraDeUsuario(idUsuario, idObraEditar, obra).enqueue(new Callback<ObraDTO>() {
            @Override
            public void onResponse(@NonNull Call<ObraDTO> call, @NonNull Response<ObraDTO> response) {
                if (!isAdded()) {
                    return;
                }
                Log.d(TAG_CRUD, "Actualizar obra response code=" + response.code()
                        + " successful=" + response.isSuccessful()
                        + " bodyId=" + (response.body() != null ? response.body().getIdObra() : null)
                        + " idObra=" + idObraEditar);
                if (response.isSuccessful()) {
                    FragMiArte.invalidarCacheUsuario(idUsuario);
                    notificarRefreshPortafolio();
                    mostrarFeedbackExito("Obra actualizada", () ->
                            NavHostFragment.findNavController(FragSubirObra.this).popBackStack()
                    );
                } else {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    mostrarFeedbackError("No se pudo actualizar la obra");
                    Toast.makeText(getContext(),
                            backendMessage != null ? backendMessage : "Error al actualizar obra. C\u00F3digo " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ObraDTO> call, @NonNull Throwable t) {
                Log.e(TAG_CRUD, "Actualizar obra failure idObra=" + idObraEditar + " usuarioId=" + idUsuario, t);
                if (isAdded()) {
                    mostrarFeedbackError("No se pudo actualizar la obra");
                    Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void cargarObraParaEditarDesdePortafolio(int idUsuario, int codigoDetalle) {
        Log.w(TAG_CRUD, "Fallback cargar obra edicion GET obrasDeUsuario/{idUsuario}"
                + " usuarioId=" + idUsuario
                + " idObra=" + idObraEditar
                + " detalleCode=" + codigoDetalle);
        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        api.obtenerObrasDeUsuario(idUsuario, idUsuario).enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ObraDTO>> call, @NonNull Response<List<ObraDTO>> response) {
                if (!isAdded()) {
                    return;
                }
                Log.d(TAG_CRUD, "Fallback obra edicion response code=" + response.code()
                        + " successful=" + response.isSuccessful()
                        + " bodySize=" + (response.body() != null ? response.body().size() : -1)
                        + " idObra=" + idObraEditar);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "No se pudo cargar la obra.", Toast.LENGTH_LONG).show();
                    return;
                }
                for (ObraDTO obra : response.body()) {
                    if (obra != null && obra.getIdObra() != null && obra.getIdObra() == idObraEditar) {
                        obraActual = obra;
                        precargarObra(obraActual);
                        return;
                    }
                }
                Log.w(TAG_CRUD, "Fallback obra edicion no encontro idObra=" + idObraEditar
                        + " usuarioId=" + idUsuario
                        + " size=" + response.body().size());
                Toast.makeText(getContext(), "No se encontró la obra para editar.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<List<ObraDTO>> call, @NonNull Throwable t) {
                Log.e(TAG_CRUD, "Fallback obra edicion failure idObra=" + idObraEditar + " usuarioId=" + idUsuario, t);
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error de red al cargar la obra.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validarConfirmacionAutoriaObligatoria() {
        if (cbAutoriaObra != null && cbAutoriaObra.isChecked()) {
            cbAutoriaObra.setError(null);
            return true;
        }
        if (cbAutoriaObra != null) {
            cbAutoriaObra.setError("Debes confirmar la autoría de la obra.");
            cbAutoriaObra.requestFocus();
        }
        Toast.makeText(getContext(), "Confirma la autoría de la obra para continuar.", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void notificarRefreshPortafolio() {
        FragPortafolio.marcarRefreshPendiente(FragPortafolio.TARGET_OBRAS);
        notificarRegresoPortafolio(true);
    }

    private void notificarRegresoPortafolio(boolean guardado) {
        if (resultadoRegresoNotificado && !guardado) {
            return;
        }
        resultadoRegresoNotificado = true;
        String modo = modoEdicion ? "editar_obra" : "crear_obra";
        Log.d(guardado ? TAG_CRUD : TAG_BACK_STACK, "Notificar regreso portafolio target=obras"
                + " guardado=" + guardado
                + " modo=" + modo
                + " idObra=" + idObraEditar);
        Bundle result = new Bundle();
        result.putString(FragPortafolio.RESULT_EXTRA_TARGET, FragPortafolio.TARGET_OBRAS);
        result.putBoolean(FragPortafolio.RESULT_EXTRA_GUARDADO, guardado);
        result.putString(FragPortafolio.RESULT_EXTRA_MODO, modo);
        getParentFragmentManager().setFragmentResult(FragPortafolio.RESULT_KEY_PORTAFOLIO_REFRESH, result);

        androidx.navigation.NavController navController = NavHostFragment.findNavController(this);
        androidx.navigation.NavBackStackEntry previousEntry = navController.getPreviousBackStackEntry();
        if (previousEntry != null) {
            previousEntry.getSavedStateHandle().set(
                    FragPortafolio.RESULT_EXTRA_TARGET,
                    FragPortafolio.TARGET_OBRAS
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







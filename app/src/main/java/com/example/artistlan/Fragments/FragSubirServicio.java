package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragSubirServicio extends Fragment {
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_frag_subir_servicio, container, false);

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

        categoriasAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        categoriasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoriaServicio.setAdapter(categoriasAdapter);

        ArrayAdapter<String> tipoContactoAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                Arrays.asList("Seleccione tipo de contacto", "EMAIL", "WHATSAPP", "INSTAGRAM", "TELEFONO", "OTRO"));
        tipoContactoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoContacto.setAdapter(tipoContactoAdapter);

        cargarCategoriasDesdeBD();
        configurarModoPantalla(view);
        if (modoEdicion) {
            cargarServicioParaEditar();
        }

        btnPublicarServicio.setOnClickListener(v -> validarYMostrarDialogo());

        btnRegresarServicio.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ScrollView scrollView = view.findViewById(R.id.fragScrollSubirServicio);
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
        View menuInferior = requireActivity().findViewById(R.id.MenuInferior);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() == null) return;
        View menuInferior = getActivity().findViewById(R.id.MenuInferior);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.VISIBLE);
        }
    }

    private int dpToPx(int dp) { return Math.round(dp * getResources().getDisplayMetrics().density); }

    private void configurarModoPantalla(View view) {
        if (!modoEdicion) return;
        ((TextView) view.findViewById(R.id.lsTxtTitulo)).setText("Editar Servicio");
        ((TextView) view.findViewById(R.id.lsTxtDesc)).setText("Actualiza la informacion de tu servicio. El precio no se puede editar.");
        btnPublicarServicio.setText("GUARDAR CAMBIOS");
        etPrecioMinServicio.setEnabled(false);
        etPrecioMaxServicio.setEnabled(false);
        etPrecioMinServicio.setFocusable(false);
        etPrecioMaxServicio.setFocusable(false);
        etPrecioMinServicio.setFocusableInTouchMode(false);
        etPrecioMaxServicio.setFocusableInTouchMode(false);
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
        if (idUsuario <= 0 || idServicioEditar <= 0) return;

        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        api.obtenerPorId(idServicioEditar, idUsuario).enqueue(new Callback<ServicioDTO>() {
            @Override public void onResponse(@NonNull Call<ServicioDTO> call, @NonNull Response<ServicioDTO> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                ServicioDTO s = response.body();
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
            @Override public void onFailure(@NonNull Call<ServicioDTO> call, @NonNull Throwable t) {}
        });
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

        if (TextUtils.isEmpty(titulo)) { etTituloServicio.setError("Ingresa un titulo"); return; }
        if (TextUtils.isEmpty(descripcion)) { etDescripcionServicio.setError("Ingresa una descripcion"); return; }
        if (TextUtils.isEmpty(tecnica)) { etTecnicaServicio.setError("Indica tecnica"); return; }

        int posCategoria = spinnerCategoriaServicio.getSelectedItemPosition();
        if (posCategoria <= 0 || posCategoria > listaCategoriasProfesiones.size()) {
            if (!(modoEdicion && servicioActual != null && servicioActual.getIdCategoria() != null)) {
                Toast.makeText(requireContext(), "Selecciona una categoria valida", Toast.LENGTH_SHORT).show();
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
            min = parsePrecio(minTxt, etPrecioMinServicio, "Precio minimo invalido");
            max = parsePrecio(maxTxt, etPrecioMaxServicio, "Precio maximo invalido");
            if ((min != null && max != null) && min >= max) {
                etPrecioMaxServicio.setError("El precio maximo debe ser mayor al minimo");
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
                if (v.length() < 2) { etContactoServicio.setError("Contacto invÃ¡lido"); return false; }
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

        TextView txtResumen = view.findViewById(R.id.txtResumenServicio);
        Button btnEditar = view.findViewById(R.id.btnEditar);
        Button btnPublicar = view.findViewById(R.id.btnConfirmarPublicar);

        String categoriaTxt = categoria != null ? categoria.getNombreCategoria() : "Sin cambio";
        String precioTxt = (min == null && max == null) ? "A convenir" : ((min != null ? min : "-") + " / " + (max != null ? max : "-"));
        txtResumen.setText("Titulo:\n" + titulo
                + "\n\nDescripcion:\n" + descripcion
                + "\n\nTecnica:\n" + tecnica
                + "\n\nContacto:\n" + tipoContacto + " - " + contacto
                + "\n\nPrecio:\n" + precioTxt
                + "\n\nCategoria:\n" + categoriaTxt);

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
    }
    private void guardarServicio(int idUsuario, ServicioDTO servicio) {
        if (modoEdicion) actualizarServicioEnBD(idUsuario, servicio);
        else guardarServicioEnBD(idUsuario, servicio);
    }

    private void guardarServicioEnBD(int idUsuario, ServicioDTO servicio) {
        ServicioApi servicioApi = RetrofitClient.getClient().create(ServicioApi.class);
        servicioApi.crearServicioDeUsuario(idUsuario, servicio).enqueue(new Callback<ServicioDTO>() {
            @Override public void onResponse(@NonNull Call<ServicioDTO> call, @NonNull Response<ServicioDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Servicio subido con exito", Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(FragSubirServicio.this).popBackStack();
                    return;
                }
                String backendMessage = ApiErrorParser.extractMessage(response);
                Toast.makeText(getContext(),
                        backendMessage != null ? backendMessage : "Error al insertar servicio " + response.code(),
                        Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(@NonNull Call<ServicioDTO> call, @NonNull Throwable t) { Toast.makeText(getContext(), "Error de red", Toast.LENGTH_LONG).show(); }
        });
    }

    private void actualizarServicioEnBD(int idUsuario, ServicioDTO servicio) {
        if (idServicioEditar <= 0) return;
        ServicioApi servicioApi = RetrofitClient.getClient().create(ServicioApi.class);
        servicioApi.actualizarServicioUsuario(idUsuario, idServicioEditar, servicio).enqueue(new Callback<ServicioDTO>() {

            @Override public void onResponse(@NonNull Call<ServicioDTO> call, @NonNull Response<ServicioDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Servicio actualizado con exito", Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(FragSubirServicio.this).popBackStack();
                    return;
                }
                String backendMessage = ApiErrorParser.extractMessage(response);
                Toast.makeText(getContext(),
                        backendMessage != null ? backendMessage : "Error al actualizar servicio " + response.code(),
                        Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(@NonNull Call<ServicioDTO> call, @NonNull Throwable t) { Toast.makeText(getContext(), "Error de red", Toast.LENGTH_LONG).show(); }
        });
    }
}




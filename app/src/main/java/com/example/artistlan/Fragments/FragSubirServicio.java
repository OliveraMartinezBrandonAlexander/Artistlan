package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.CategoriaApi;
import com.example.artistlan.Conector.api.CategoriaServiciosApi;
import com.example.artistlan.Conector.api.ServicioApi;
import com.example.artistlan.Conector.model.CategoriaDTO;
import com.example.artistlan.Conector.model.CategoriaServiciosDTO;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragSubirServicio extends Fragment {

    private Spinner spinnerCategoriaServicio;
    private EditText etTituloServicio;
    private EditText etDescripcionServicio;
    private EditText etTecnicaServicio;
    private EditText etContactoServicio;
    private Button btnPublicarServicio;
    private Button btnRegresarServicio;

    private final List<CategoriaDTO> listaCategoriasProfesiones = new ArrayList<>();
    private ArrayAdapter<String> categoriasAdapter;

    public FragSubirServicio() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_frag_subir_servicio, container, false);

        spinnerCategoriaServicio = view.findViewById(R.id.spinnerCategoriaServicio);
        etTituloServicio        = view.findViewById(R.id.etTituloServicio);
        etDescripcionServicio   = view.findViewById(R.id.etDescripcionServicio);
        etTecnicaServicio       = view.findViewById(R.id.etTecnicaServicio);
        etContactoServicio      = view.findViewById(R.id.etContactoServicio);
        btnPublicarServicio     = view.findViewById(R.id.btnPublicarServicio);
        btnRegresarServicio     = view.findViewById(R.id.btnRegresarServicio);

        categoriasAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        categoriasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoriaServicio.setAdapter(categoriasAdapter);

        spinnerCategoriaServicio.setEnabled(true);
        spinnerCategoriaServicio.setClickable(true);
        spinnerCategoriaServicio.setFocusable(true);
        spinnerCategoriaServicio.setFocusableInTouchMode(false);

        cargarCategoriasDesdeBD();

        btnPublicarServicio.setOnClickListener(v -> validarYPublicarServicio());

        btnRegresarServicio.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View bottomBar = requireActivity().findViewById(R.id.bottomBar);
        if (bottomBar != null) {
            bottomBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        View bottomBar = requireActivity().findViewById(R.id.bottomBar);
        if (bottomBar != null) {
            bottomBar.setVisibility(View.VISIBLE);
        }
    }

    private void cargarCategoriasDesdeBD() {
        CategoriaApi api = RetrofitClient.getClient().create(CategoriaApi.class);

        api.obtenerCategorias().enqueue(new Callback<List<CategoriaDTO>>() {
            @Override
            public void onResponse(Call<List<CategoriaDTO>> call,
                                   Response<List<CategoriaDTO>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "No se pudieron obtener las categorías", Toast.LENGTH_LONG).show();
                    return;
                }

                List<CategoriaDTO> todas = response.body();

                List<String> profesiones = Arrays.asList(
                        "pintor", "escultor", "fotógrafo", "ilustrador",
                        "diseñador gráfico", "diseñador industrial", "diseñador de moda",
                        "caricaturista", "animador", "artesano", "ceramista", "grabador",
                        "artista digital", "artista plástico", "maquetador", "decorador",
                        "restaurador de arte", "graffitero", "modelador 3d"
                );

                listaCategoriasProfesiones.clear();

                for (CategoriaDTO c : todas) {
                    String nombre = c.getNombreCategoria();
                    if (nombre == null) continue;

                    String nombreNormalizado = nombre.trim().toLowerCase();
                    if (profesiones.contains(nombreNormalizado)) {
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

        for (CategoriaDTO c : listaCategoriasProfesiones) {
            nombres.add(c.getNombreCategoria());
        }

        categoriasAdapter.clear();
        categoriasAdapter.addAll(nombres);
        categoriasAdapter.notifyDataSetChanged();
    }

    private void validarYPublicarServicio() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("id", -1);

        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error: No se encontró ID de usuario.", Toast.LENGTH_LONG).show();
            return;
        }

        String titulo      = etTituloServicio.getText().toString().trim();
        String descripcion = etDescripcionServicio.getText().toString().trim();
        String tecnica     = etTecnicaServicio.getText().toString().trim();
        String contacto    = etContactoServicio.getText().toString().trim();

        if (listaCategoriasProfesiones.isEmpty()) {
            Toast.makeText(requireContext(), "Primero carga las categorías", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(titulo)) {
            etTituloServicio.setError("Ingresa un título");
            etTituloServicio.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(descripcion)) {
            etDescripcionServicio.setError("Ingresa una descripción");
            etDescripcionServicio.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(tecnica)) {
            etTecnicaServicio.setError("Indica la técnica que manejas");
            etTecnicaServicio.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(contacto)) {
            etContactoServicio.setError("Ingresa un medio de contacto");
            etContactoServicio.requestFocus();
            return;
        }

        int posicionSeleccionada = spinnerCategoriaServicio.getSelectedItemPosition();
        if (posicionSeleccionada <= 0 || posicionSeleccionada > listaCategoriasProfesiones.size()) {
            Toast.makeText(requireContext(), "Selecciona una categoría válida", Toast.LENGTH_SHORT).show();
            return;
        }

        CategoriaDTO categoriaSeleccionada = listaCategoriasProfesiones.get(posicionSeleccionada - 1);

        ServicioDTO servicio = new ServicioDTO();
        servicio.setTitulo(titulo);
        servicio.setDescripcion(descripcion);
        servicio.setTecnicas(tecnica);
        servicio.setContacto(contacto);
        servicio.setIdUsuario(idUsuario);
        servicio.setCategoria(categoriaSeleccionada.getNombreCategoria());

        guardarServicioEnBD(idUsuario, categoriaSeleccionada.getIdCategoria(), servicio);
    }


    private void guardarServicioEnBD(int idUsuario, int idCategoria, ServicioDTO servicio) {
        ServicioApi servicioApi = RetrofitClient.getClient().create(ServicioApi.class);

        Call<ServicioDTO> call = servicioApi.crearServicioDeUsuario(idUsuario, servicio);

        call.enqueue(new Callback<ServicioDTO>() {
            @Override
            public void onResponse(@NonNull Call<ServicioDTO> call,
                                   @NonNull Response<ServicioDTO> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ServicioDTO creado = response.body();
                    Toast.makeText(getContext(),
                            "Servicio publicado. ID: " + creado.getIdServicio(),
                            Toast.LENGTH_LONG).show();

                    crearRelacionCategoriaServicio(creado.getIdServicio(), idCategoria);
                } else {
                    Toast.makeText(getContext(),
                            "Error al insertar servicio " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ServicioDTO> call, @NonNull Throwable t) {
                Toast.makeText(getContext(),
                        "Error de red: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void crearRelacionCategoriaServicio(Integer idServicio, Integer idCategoria) {
        CategoriaServiciosApi api = RetrofitClient.getClient().create(CategoriaServiciosApi.class);

        CategoriaServiciosDTO dto = new CategoriaServiciosDTO();
        dto.setIdServicio(idServicio);
        dto.setIdCategoria(idCategoria);

        api.crear(dto).enqueue(new Callback<CategoriaServiciosDTO>() {
            @Override
            public void onResponse(@NonNull Call<CategoriaServiciosDTO> call,
                                   @NonNull Response<CategoriaServiciosDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(),
                            "Relación servicio-categoría guardada.",
                            Toast.LENGTH_SHORT).show();
                    limpiarFormulario();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(),
                            "Servicio ok, pero falló al guardar la categoría. Código " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoriaServiciosDTO> call, @NonNull Throwable t) {
                Toast.makeText(getContext(),
                        "Servicio ok, pero error de red al guardar categoría: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void limpiarFormulario() {
        etTituloServicio.setText("");
        etDescripcionServicio.setText("");
        etTecnicaServicio.setText("");
        etContactoServicio.setText("");
        if (!listaCategoriasProfesiones.isEmpty()) {
            spinnerCategoriaServicio.setSelection(0);
        }
    }
}

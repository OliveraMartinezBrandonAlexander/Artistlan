package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.CategoriaApi;
import com.example.artistlan.Conector.api.ServicioApi;
import com.example.artistlan.Conector.model.CategoriaDTO;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragSubirServicio extends Fragment {

    private EditText etTituloServicio, etDescripcionServicio, etTecnicaServicio, etContactoServicio;
    private Spinner spinnerCategoriaServicio;
    private Button btnPublicarServicio, btnRegresarServicio;

    private List<CategoriaDTO> listaCategoriasProfesiones = new ArrayList<>();

    public FragSubirServicio() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subir_servicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this, view);

        etTituloServicio      = view.findViewById(R.id.etTituloServicio);
        etDescripcionServicio = view.findViewById(R.id.etDescripcionServicio);
        etTecnicaServicio     = view.findViewById(R.id.etTecnicaServicio);
        etContactoServicio    = view.findViewById(R.id.etContactoServicio);

        spinnerCategoriaServicio = view.findViewById(R.id.spinnerCategoriaServicio);

        btnPublicarServicio   = view.findViewById(R.id.btnPublicarServicio);
        btnRegresarServicio   = view.findViewById(R.id.btnRegresarServicio);

        btnPublicarServicio.setOnClickListener(v -> subirServicio());
        btnRegresarServicio.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        cargarCategoriasProfesiones();
    }

    private void cargarCategoriasProfesiones() {
        CategoriaApi api = RetrofitClient.getClient().create(CategoriaApi.class);

        api.obtenerCategorias().enqueue(new Callback<List<CategoriaDTO>>() {
            @Override
            public void onResponse(Call<List<CategoriaDTO>> call, Response<List<CategoriaDTO>> response) {
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

                listaCategoriasProfesiones = new ArrayList<>();

                for (CategoriaDTO c : todas) {
                    String nombre = c.getNombreCategoria().trim().toLowerCase();
                    if (profesiones.contains(nombre)) {
                        listaCategoriasProfesiones.add(c);
                    }
                }

                List<String> nombres = new ArrayList<>();
                nombres.add("Seleccione una categoría");
                for (CategoriaDTO c : listaCategoriasProfesiones) {
                    nombres.add(c.getNombreCategoria());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_spinner_item,
                        nombres
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategoriaServicio.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<CategoriaDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void subirServicio() {
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

        int pos = spinnerCategoriaServicio.getSelectedItemPosition();

        if (pos == 0) {
            Toast.makeText(getContext(), "Selecciona una categoría válida.", Toast.LENGTH_LONG).show();
            return;
        }

        if (titulo.isEmpty() || descripcion.isEmpty() || tecnica.isEmpty() || contacto.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos.", Toast.LENGTH_LONG).show();
            return;
        }

        CategoriaDTO categoriaSeleccionada = listaCategoriasProfesiones.get(pos - 1);
        int idCategoria = categoriaSeleccionada.getIdCategoria();
        String nombreCategoria = categoriaSeleccionada.getNombreCategoria();

        ServicioDTO nuevoServicio = new ServicioDTO();
        nuevoServicio.setTitulo(titulo);
        nuevoServicio.setDescripcion(descripcion);
        nuevoServicio.setTecnicas(tecnica);
        nuevoServicio.setContacto(contacto);
        nuevoServicio.setIdUsuario(idUsuario);
        nuevoServicio.setIdCategoria(idCategoria);
        nuevoServicio.setCategoria(nombreCategoria);

        insertarServicioEnBD(idUsuario, nuevoServicio);
    }

    private void insertarServicioEnBD(int idUsuario, ServicioDTO servicio) {
        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);

        Call<ServicioDTO> call = api.crearServicioDeUsuario(idUsuario, servicio);

        call.enqueue(new Callback<ServicioDTO>() {
            @Override
            public void onResponse(@NonNull Call<ServicioDTO> call,
                                   @NonNull Response<ServicioDTO> response) {

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(),
                            "¡Servicio publicado con éxito! ID: " + response.body().getIdServicio(),
                            Toast.LENGTH_LONG).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(),
                            "Error al insertar servicio. Código " + response.code(),
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
}

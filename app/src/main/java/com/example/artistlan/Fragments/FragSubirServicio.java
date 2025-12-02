package com.example.artistlan.Fragments;

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

import com.example.artistlan.R;

import java.util.ArrayList;
import java.util.List;

public class FragSubirServicio extends Fragment {

    private Spinner spinnerCategoriaServicio;
    private EditText etTituloServicio;
    private EditText etDescripcionServicio;
    private EditText etTecnicaServicio;
    private EditText etContactoServicio;
    private Button btnPublicarServicio;
    private Button btnRegresarServicio;

    // Lista de categorías que llega de la BD
    private List<CategoriaServicio> listaCategorias = new ArrayList<>();
    private ArrayAdapter<String> categoriasAdapter;

    public FragSubirServicio() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_subirservicios, container, false);

        // Referencias a la UI
        spinnerCategoriaServicio = view.findViewById(R.id.spinnerCategoriaServicio);
        etTituloServicio        = view.findViewById(R.id.etTituloServicio);
        etDescripcionServicio   = view.findViewById(R.id.etDescripcionServicio);
        etTecnicaServicio       = view.findViewById(R.id.etTecnicaServicio);
        etContactoServicio      = view.findViewById(R.id.etContactoServicio);
        btnPublicarServicio     = view.findViewById(R.id.btnPublicarServicio);
        btnRegresarServicio     = view.findViewById(R.id.btnRegresarServicio);

        // Inicializar adapter del spinner (de momento vacío)
        categoriasAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );
        categoriasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoriaServicio.setAdapter(categoriasAdapter);

        // Cargar categorías desde la BD / API
        cargarCategoriasDesdeBD();

        // Click en Publicar
        btnPublicarServicio.setOnClickListener(v -> validarYPublicarServicio());

        // Click en Regresar (ejemplo: popBackStack)
        btnRegresarServicio.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    // ==============================
    // 1. CARGAR CATEGORÍAS (SPINNER)
    // ==============================
    /**
     * Aquí haces la llamada a tu BD (Firebase, API REST, MySQL vía backend, etc.)
     * y cuando obtengas la lista de categorías, llamas a actualizarSpinnerCategorias().
     */
    private void cargarCategoriasDesdeBD() {
        // TODO: Reemplaza esto por tu lógica real
        // Ejemplo MOCK para que veas la idea:

        listaCategorias.clear();
        listaCategorias.add(new CategoriaServicio(1, "Retratos"));
        listaCategorias.add(new CategoriaServicio(2, "Ilustración Digital"));
        listaCategorias.add(new CategoriaServicio(3, "Fotografía"));
        listaCategorias.add(new CategoriaServicio(4, "Diseño de portadas"));

        actualizarSpinnerCategorias();
    }

    private void actualizarSpinnerCategorias() {
        List<String> nombres = new ArrayList<>();
        for (CategoriaServicio cat : listaCategorias) {
            nombres.add(cat.getNombre());
        }

        categoriasAdapter.clear();
        categoriasAdapter.addAll(nombres);
        categoriasAdapter.notifyDataSetChanged();
    }

    // ==============================
    // 2. VALIDAR FORMULARIO Y PUBLICAR
    // ==============================
    private void validarYPublicarServicio() {
        String titulo     = etTituloServicio.getText().toString().trim();
        String descripcion= etDescripcionServicio.getText().toString().trim();
        String tecnica    = etTecnicaServicio.getText().toString().trim();
        String contacto   = etContactoServicio.getText().toString().trim();

        // Validaciones básicas
        if (listaCategorias.isEmpty()) {
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
        if (posicionSeleccionada < 0 || posicionSeleccionada >= listaCategorias.size()) {
            Toast.makeText(requireContext(), "Selecciona una categoría válida", Toast.LENGTH_SHORT).show();
            return;
        }

        CategoriaServicio categoriaSeleccionada = listaCategorias.get(posicionSeleccionada);

        // Armar el objeto Servicio listo para enviar a tu backend
        ServicioRequest servicio = new ServicioRequest();
        servicio.setIdCategoria(categoriaSeleccionada.getIdCategoria());
        servicio.setTitulo(titulo);
        servicio.setDescripcion(descripcion);
        servicio.setTecnica(tecnica);
        servicio.setContacto(contacto);

        // Aquí mandas a guardar (API / Firebase / lo que uses)
        guardarServicioEnBD(servicio);
    }

    // ==============================
    // 3. GUARDAR EN BD / API
    // ==============================
    /**
     * Implementa aquí la lógica real para guardar el servicio:
     * - Llamada a Retrofit (Spring Boot)
     * - Inserción en Firebase Firestore / Realtime Database
     * - etc.
     */
    private void guardarServicioEnBD(ServicioRequest servicio) {
        // TODO: Reemplazar por tu implementación real

        // EJEMPLO de "éxito simulado"
        Toast.makeText(requireContext(), "Servicio publicado correctamente", Toast.LENGTH_SHORT).show();

        // Opcional: limpiar campos
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        etTituloServicio.setText("");
        etDescripcionServicio.setText("");
        etTecnicaServicio.setText("");
        etContactoServicio.setText("");

        if (!listaCategorias.isEmpty()) {
            spinnerCategoriaServicio.setSelection(0);
        }
    }

    // ==============================
    // 4. CLASES DE APOYO
    // ==============================
    // Representa una categoría que viene de la BD
    public static class CategoriaServicio {
        private int idCategoria;
        private String nombre;

        public CategoriaServicio(int idCategoria, String nombre) {
            this.idCategoria = idCategoria;
            this.nombre = nombre;
        }

        public int getIdCategoria() {
            return idCategoria;
        }

        public String getNombre() {
            return nombre;
        }
    }

    // DTO para mandar al backend
    public static class ServicioRequest {
        private int idCategoria;
        private String titulo;
        private String descripcion;
        private String tecnica;
        private String contacto;

        public int getIdCategoria() { return idCategoria; }
        public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public String getTecnica() { return tecnica; }
        public void setTecnica(String tecnica) { this.tecnica = tecnica; }

        public String getContacto() { return contacto; }
        public void setContacto(String contacto) { this.contacto = contacto; }
    }
}

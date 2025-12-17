//package com.example.artistlan.Fragments;
//
//import android.os.Bundle;
//import android.view.View;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.artistlan.Conector.RetrofitClient;
//import com.example.artistlan.Conector.api.ServicioApi;
//import com.example.artistlan.Conector.model.ServicioDTO;
//import com.example.artistlan.R;
//import com.example.artistlan.TarjetaTextoServicio.adapter.TarjetaTextoServicioAdapter;
//import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class FragPerfilServiciosPublico extends Fragment {
//
//    private static final String ARG_USUARIO = "usuario";
//    private String usuario;
//
//    private RecyclerView recycler;
//    private TarjetaTextoServicioAdapter adapter;
//
//    public FragPerfilServiciosPublico() {
//        super(R.layout.fragment_frag_perfil_servicios_publico);
//    }
//
//    public static FragPerfilServiciosPublico newInstance(String usuario) {
//        FragPerfilServiciosPublico f = new FragPerfilServiciosPublico();
//        Bundle b = new Bundle();
//        b.putString(ARG_USUARIO, usuario);
//        f.setArguments(b);
//        return f;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        usuario = getArguments() != null ? getArguments().getString(ARG_USUARIO, "") : "";
//
//        recycler = view.findViewById(R.id.recyclerServiciosPerfilPublico);
//        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
//
//        adapter = new TarjetaTextoServicioAdapter(new ArrayList<>(), requireContext());
//        recycler.setAdapter(adapter);
//
//        cargarServicios();
//    }
//
//    private void cargarServicios() {
//        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
//        Call<List<ServicioDTO>> call = api.obtenerTodos();
//
//        call.enqueue(new Callback<List<ServicioDTO>>() {
//            @Override
//            public void onResponse(@NonNull Call<List<ServicioDTO>> call, @NonNull Response<List<ServicioDTO>> response) {
//                if (!isAdded()) return;
//
//                if (!response.isSuccessful() || response.body() == null) {
//                    adapter.actualizarLista(new ArrayList<>());
//                    return;
//                }
//
//                List<TarjetaTextoServicioItem> items = new ArrayList<>();
//                for (ServicioDTO dto : response.body()) {
//
//                    // Filtramos por usuario dueño del servicio
//                    String dueño = dto.getNombreUsuario();
//                    if (usuario != null && !usuario.isEmpty()) {
//                        if (dueño == null || !usuario.equalsIgnoreCase(dueño)) continue;
//                    }
//
//                    items.add(new TarjetaTextoServicioItem(
//                            dto.getTitulo(),
//                            dto.getDescripcion(),
//                            dto.getContacto(),
//                            dto.getTecnicas(),
//                            dto.getNombreUsuario(),
//                            dto.getCategoria(),
//                            dto.getFotoPerfilAutor(),
//                            false
//                    ));
//                }
//
//                adapter.actualizarLista(items);
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<List<ServicioDTO>> call, @NonNull Throwable t) {
//                if (!isAdded()) return;
//                adapter.actualizarLista(new ArrayList<>());
//            }
//        });
//    }
//}

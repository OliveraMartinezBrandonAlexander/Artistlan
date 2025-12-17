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
//import com.example.artistlan.Conector.api.ObraApi;
//import com.example.artistlan.Conector.model.ObraDTO;
//import com.example.artistlan.R;
//import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
//import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class FragPerfilObrasPublico extends Fragment {
//
//    private static final String ARG_ID = "idUsuario";
//    private int idUsuario;
//
//    private RecyclerView recycler;
//    private TarjetaTextoObraAdapter adapter;
//
//    public FragPerfilObrasPublico() {
//        super(R.layout.fragment_frag_perfil_obras_publico);
//    }
//
//    public static FragPerfilObrasPublico newInstance(int idUsuario) {
//        FragPerfilObrasPublico f = new FragPerfilObrasPublico();
//        Bundle b = new Bundle();
//        b.putInt(ARG_ID, idUsuario);
//        f.setArguments(b);
//        return f;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        idUsuario = getArguments() != null ? getArguments().getInt(ARG_ID, -1) : -1;
//
//        recycler = view.findViewById(R.id.recyclerObrasPerfilPublico);
//        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
//
//        adapter = new TarjetaTextoObraAdapter(new ArrayList<>(), requireContext());
//        recycler.setAdapter(adapter);
//
//        cargarObras();
//    }
//
//    private void cargarObras() {
//        if (idUsuario <= 0) {
//            adapter.actualizarLista(new ArrayList<>());
//            return;
//        }
//
//        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
//        Call<List<ObraDTO>> call = api.obtenerObrasDeUsuario(idUsuario);
//
//        call.enqueue(new Callback<List<ObraDTO>>() {
//            @Override
//            public void onResponse(@NonNull Call<List<ObraDTO>> call, @NonNull Response<List<ObraDTO>> response) {
//                if (!isAdded()) return;
//                if (!response.isSuccessful() || response.body() == null) {
//                    adapter.actualizarLista(new ArrayList<>());
//                    return;
//                }
//
//                List<TarjetaTextoObraItem> items = new ArrayList<>();
//                for (ObraDTO dto : response.body()) {
//                    items.add(new TarjetaTextoObraItem(
//                            dto.getIdObra(),
//                            dto.getTitulo(),
//                            dto.getDescripcion(),
//                            dto.getEstado(),
//                            dto.getPrecio(),
//                            dto.getImagen1(),
//                            dto.getImagen2(),
//                            dto.getImagen3(),
//                            dto.getTecnicas(),
//                            dto.getMedidas(),
//                            dto.getLikes() != null ? dto.getLikes() : 0,
//                            dto.getNombreAutor(),
//                            dto.getNombreCategoria(),
//                            dto.getFotoPerfilAutor(),
//                            false,
//                            false
//                    ));
//                }
//
//                adapter.actualizarLista(items);
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<List<ObraDTO>> call, @NonNull Throwable t) {
//                if (!isAdded()) return;
//                adapter.actualizarLista(new ArrayList<>());
//            }
//        });
//    }
//}

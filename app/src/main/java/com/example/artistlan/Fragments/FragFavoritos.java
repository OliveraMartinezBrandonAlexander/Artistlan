package com.example.artistlan.Fragments;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.artistlan.Activitys.ActFragmentoPrincipal;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;

public class FragFavoritos extends Fragment implements View.OnClickListener {
    Button btnRegresar, btnArte,btnArtista;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_favoritos, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().findViewById(R.id.MenuInferior).setVisibility(View.GONE);

        btnRegresar = view.findViewById(R.id.btnRegresar);
        btnRegresar.setOnClickListener(this);
        btnArte = view.findViewById(R.id.btnArte);
        btnArte.setOnClickListener(this);
        btnArtista= view.findViewById(R.id.btnArtista);
        btnArtista.setOnClickListener(this);
        new BotonesMenuSuperior(this, view);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().findViewById(R.id.MenuInferior).setVisibility(View.VISIBLE);
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnRegresar)
        {
            Intent irActivity = new Intent(getContext(), ActFragmentoPrincipal.class);
            startActivity(irActivity);
        }
    }
}
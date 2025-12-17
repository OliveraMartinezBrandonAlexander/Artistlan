package com.example.artistlan.Fragments;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.artistlan.Activitys.ActFragmentoPrincipal;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;

public class FragFavoritos extends Fragment implements View.OnClickListener {

    private Button btnArte, btnArtista;
    private View segmentIndicatorFavoritos;
    private ViewGroup segmentContainerFavoritos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_favoritos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().findViewById(R.id.MenuInferior).setVisibility(View.GONE);
        new BotonesMenuSuperior(this, view);

        // Referencias
        btnArte = view.findViewById(R.id.btnArte);
        btnArtista = view.findViewById(R.id.btnArtista);
        segmentIndicatorFavoritos = view.findViewById(R.id.segmentIndicatorFavoritos);
        segmentContainerFavoritos = view.findViewById(R.id.segmentContainerFavoritos);

        // Botón regresar
        view.findViewById(R.id.btnRegresar).setOnClickListener(v -> {
            Intent irActivity = new Intent(getContext(), ActFragmentoPrincipal.class);
            startActivity(irActivity);
        });

        btnArte.setOnClickListener(this);
        btnArtista.setOnClickListener(this);

        // Mostrar "ARTE" por defecto
        view.post(() -> {
            moverIndicador(btnArte, true);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentsFavoritos, new FragFavoritosObras())
                    .commit();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().findViewById(R.id.MenuInferior).setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnArte) {
            moverIndicador(btnArte, true);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentsFavoritos, new FragFavoritosObras())
                    .commit();
        } else if (v.getId() == R.id.btnArtista) {
            moverIndicador(btnArtista, false);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentsFavoritos, new FragFavoritosArtistas())
                    .commit();
        }
    }

    /** Animación del indicador */
    private void moverIndicador(Button destino, boolean izquierda) {
        int contWidth = segmentContainerFavoritos.getWidth();
        int mitad = contWidth / 2;

        int nuevoInicio = izquierda ? 0 : mitad;
        ValueAnimator anim = ValueAnimator.ofInt(segmentIndicatorFavoritos.getLeft(), nuevoInicio);
        anim.setDuration(250);
        anim.addUpdateListener(a -> {
            int val = (int) a.getAnimatedValue();
            segmentIndicatorFavoritos.setX(val);
            segmentIndicatorFavoritos.getLayoutParams().width = mitad;
            segmentIndicatorFavoritos.requestLayout();
        });
        anim.start();

        // Cambiar colores del texto
        btnArte.setTextColor(izquierda ? 0xFFFFFFFF : 0xFF1E3A8A);
        btnArtista.setTextColor(izquierda ? 0xFF1E3A8A : 0xFFFFFFFF);
    }
}

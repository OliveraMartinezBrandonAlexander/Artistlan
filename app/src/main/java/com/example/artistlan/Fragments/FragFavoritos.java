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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import com.example.artistlan.Activitys.ActFragmentoPrincipal;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;

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
        ThemeModuleStyler.styleFragment(this, view);

        requireActivity().findViewById(R.id.MenuInferior).setVisibility(View.GONE);
        new BotonesMenuSuperior(this);

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
        anim.setDuration(220);
        anim.setInterpolator(new FastOutSlowInInterpolator());
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
        int selected = ContextCompat.getColor(requireContext(), R.color.artistlan_menu_text_primary);
        int unselected = ContextCompat.getColor(requireContext(), R.color.artistlan_menu_text_secondary);
        btnArte.setTextColor(izquierda ? selected : unselected);
        btnArtista.setTextColor(izquierda ? unselected : selected);

        Button activo = izquierda ? btnArte : btnArtista;
        activo.animate().scaleX(1.02f).scaleY(1.02f).setDuration(120)
                .withEndAction(() -> activo.animate().scaleX(1f).scaleY(1f).setDuration(120).start())
                .start();
    }
}

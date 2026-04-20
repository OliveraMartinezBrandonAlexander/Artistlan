package com.example.artistlan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
// Importaciones de la nueva biblioteca
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FragPortafolio extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionsMenu fabMenu;
    private FloatingActionButton fabSubirObra, fabSubirServicio;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_frag_portafolio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);

        new com.example.artistlan.BotonesMenuSuperior(this);

        tabLayout = view.findViewById(R.id.tabLayoutPortafolio);
        viewPager = view.findViewById(R.id.viewPagerPortafolio);
        fabMenu = view.findViewById(R.id.fabMenuSubir);
        fabSubirObra = view.findViewById(R.id.fabSubirObraMenu);
        fabSubirServicio = view.findViewById(R.id.fabSubirServicioMenu);

        viewPager.setAdapter(new PortafolioPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "MIS OBRAS" : "MIS SERVICIOS");
        }).attach();

        fabSubirObra.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.fragSubirObra);
            fabMenu.collapse();
        });

        fabSubirServicio.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.fragSubirServicio);
            fabMenu.collapse();
        });
    }

    private static class PortafolioPagerAdapter extends FragmentStateAdapter {
        public PortafolioPagerAdapter(@NonNull Fragment fragment) { super(fragment); }
        @NonNull @Override public Fragment createFragment(int position) {
            return position == 0 ? new FragMiArte() : new FragMisServicios();
        }
        @Override public int getItemCount() { return 2; }
    }
}
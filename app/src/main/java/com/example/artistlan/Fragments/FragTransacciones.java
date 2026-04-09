package com.example.artistlan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class FragTransacciones extends Fragment {

    public static final String ARG_TAB_INICIAL = "tab_transacciones_inicial";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_transacciones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this);
        View menuInferior = requireActivity().findViewById(R.id.MenuInferior);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }

        tabLayout = view.findViewById(R.id.tabLayoutTransacciones);
        viewPager = view.findViewById(R.id.viewPagerTransacciones);

        viewPager.setAdapter(new TransaccionesPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? R.string.tab_mis_compras : R.string.tab_mis_ventas);
        }).attach();

        int tabInicial = 0;
        Bundle args = getArguments();
        if (args != null) {
            tabInicial = args.getInt(ARG_TAB_INICIAL, 0);
        }
        seleccionarTab(tabInicial);
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

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() == null) {
            return;
        }
        View menuInferior = getActivity().findViewById(R.id.MenuInferior);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }
    }

    public void recargarDespuesDePago() {
        if (!isAdded()) {
            return;
        }
        List<Fragment> children = getChildFragmentManager().getFragments();
        for (Fragment fragment : children) {
            if (fragment instanceof BaseTransaccionesFragment) {
                ((BaseTransaccionesFragment) fragment).recargarDespuesDePago();
            }
        }
    }

    public void seleccionarTab(int tabIndex) {
        if (viewPager == null) {
            return;
        }
        int safeIndex = Math.max(0, Math.min(1, tabIndex));
        viewPager.setCurrentItem(safeIndex, false);
    }

    private static class TransaccionesPagerAdapter extends FragmentStateAdapter {

        public TransaccionesPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new FragMisCompras() : new FragMisVentas();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}

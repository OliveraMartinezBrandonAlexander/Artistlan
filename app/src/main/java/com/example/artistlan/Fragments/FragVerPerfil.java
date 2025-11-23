package com.example.artistlan.Fragments;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.R;

public class FragVerPerfil extends Fragment implements View.OnClickListener {
    TextView txvNombrePrincipal,txvDescripcion,txvTelefono,txvCorreo,txvNombre,txvRedes,txvFecNac;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_ver_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //menu de arriba
        new BotonesMenuSuperior(this, view);
        //Logica para el boton favoritos
        ImageView btnFavoritos = view.findViewById(R.id.btnFavoritos);
        btnFavoritos.setVisibility(View.VISIBLE);
        btnFavoritos.setOnClickListener(this);
        //Logica para los campos personales de usuario

        txvNombrePrincipal = view.findViewById(R.id.VrpTxvUsuario);
        txvDescripcion = view.findViewById(R.id.VrpTxvDescripcion);
        txvTelefono = view.findViewById(R.id.VrpTxvTelefono);
        txvCorreo = view.findViewById(R.id.VrpTxvCorreo);
        txvNombre = view.findViewById(R.id.VrpTxvNombre);
        txvRedes = view.findViewById(R.id.VrpTxvRedes);
        txvFecNac = view.findViewById(R.id.VrpTxvFecNac);

        UsuariosDTO usuario = obtenerUsuarioGuardado();

        txvNombrePrincipal.setText(usuario.getUsuario());
        txvDescripcion.setText(usuario.getDescripcion());
        txvTelefono.setText(usuario.getTelefono());
        txvCorreo.setText(usuario.getCorreo());
        txvNombre.setText(usuario.getNombreCompleto());
        txvRedes.setText(usuario.getRedesSociales());
        txvFecNac.setText(usuario.getFechaNacimiento());

    }
    private UsuariosDTO obtenerUsuarioGuardado() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);

        UsuariosDTO u = new UsuariosDTO();
        u.setIdUsuario(prefs.getInt("id", 0));
        u.setUsuario(prefs.getString("usuario", ""));
        u.setCorreo(prefs.getString("correo", ""));
        u.setNombreCompleto(prefs.getString("nombre", ""));
        u.setTelefono(prefs.getString("telefono", ""));
        u.setDescripcion(prefs.getString("descripcion", ""));
        u.setRedesSociales(prefs.getString("redes", ""));
        u.setFechaNacimiento(prefs.getString("fechaNac", ""));
        u.setFotoPerfil(prefs.getString("fotoPerfil", ""));

        return u;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btnFavoritos)
        {
            Navigation.findNavController(v).navigate(R.id.fragFavoritos);
        }

    }
}
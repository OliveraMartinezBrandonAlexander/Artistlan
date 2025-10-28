package com.example.artistlan.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.artistlan.Fragments.FragMain;
import com.example.artistlan.R;

public class ActIniciarSesion extends AppCompatActivity implements View.OnClickListener {

    Button btnIniciarSesion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_act_iniciar_sesion);

        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
       // btnXIniciar = findViewById(R.id.btnXIniciar);

        btnIniciarSesion.setOnClickListener(this);
       // btnXIniciar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int idClick = v.getId();
        Intent irActivity = null;

       if(idClick == R.id.btnIniciarSesion){
            irActivity = new Intent(this, ActFragmentoPrincipal.class);
        }
        if (irActivity != null) {
            startActivity(irActivity);
        }
    }
}
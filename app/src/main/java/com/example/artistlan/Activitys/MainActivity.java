package com.example.artistlan.Activitys;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.artistlan.R;
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnInicioSesion, btnCrearCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnInicioSesion = findViewById(R.id.btnInicioSesion);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);

        btnInicioSesion.setOnClickListener(this);
        btnCrearCuenta.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        int idClick = v.getId();
        Intent irActivity = null;

        if(idClick == R.id.btnInicioSesion){
            irActivity = new Intent(this, ActIniciarSesion.class);
        }
        else if(idClick == R.id.btnCrearCuenta){
            irActivity = new Intent(this, ActCrearCuenta.class);
        }
        if (irActivity != null) {
            startActivity(irActivity);
        }
    }
}
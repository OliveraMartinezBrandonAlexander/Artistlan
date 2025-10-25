package com.example.artistlan.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.artistlan.R;

public class ActCrearCuenta extends AppCompatActivity implements View.OnClickListener {
    Button btnCrear,btnXCrear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_act_crear_cuenta);

        btnCrear = findViewById(R.id.btnCrear);
        //btnXCrear = findViewById(R.id.btnXCrear);

       btnCrear.setOnClickListener(this);
      //  btnXCrear.setOnClickListener(this);
    }


    //  @Override
    public void onClick(View v) {
       int idClick = v.getId();
       Intent irActivity = null;

       // if(idClick == R.id.btnXCrear){
        //    irActivity = new Intent(this, MainActivity.class);
      //  }
       if(idClick == R.id.btnCrear){Toast.makeText(this, "Cuenta Creada", Toast.LENGTH_SHORT).show();
       }
       if (irActivity != null) {startActivity(irActivity);
      }
    }
}
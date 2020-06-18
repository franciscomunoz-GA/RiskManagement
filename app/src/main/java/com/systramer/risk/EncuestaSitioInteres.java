package com.systramer.risk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class EncuestaSitioInteres extends AppCompatActivity {
    String IdUsuario;
    String IdCita;
    String Tipo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuesta_sitio_interes);

        Intent intent = getIntent();
        IdUsuario = intent.getStringExtra("IdUsuario");
        IdCita = intent.getStringExtra("IdCita");
        Tipo = intent.getStringExtra("Tipo");

        Toast.makeText(getBaseContext(), IdUsuario, Toast.LENGTH_SHORT).show();
    }
}
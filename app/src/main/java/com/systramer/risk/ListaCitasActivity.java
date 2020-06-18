package com.systramer.risk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListaCitasActivity extends AppCompatActivity {
    ListView ListViewCita;
    List<Cita> list;
    String IdUsuario;
    String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_citas);
        Intent intent = getIntent();
        IdUsuario = intent.getStringExtra("IdUsuario");
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        ListViewCita = findViewById(R.id.ListaCitas);

        Custom_Adapter  adapter = new Custom_Adapter(this,GetData());
        ListViewCita.setAdapter(adapter);

        ListViewCita.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cita c = list.get(position);
                Toast.makeText(getBaseContext(), c.Titulo, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Cita> GetData() {
        Toast.makeText(getApplicationContext(), IdUsuario+" "+currentDate, Toast.LENGTH_SHORT).show();
        list = new ArrayList<>();
        list.add(new Cita(1, R.drawable.baseline_format_list_bulleted_black_18dp,"Titulo1","Descipcion1","2020-06-17", "11:00:00"));
        list.add(new Cita(2, R.drawable.baseline_format_list_bulleted_black_18dp,"Titulo2","Descipcion2","2020-06-17", "11:00:00"));
        list.add(new Cita(3, R.drawable.baseline_format_list_bulleted_black_18dp,"Titulo3","Descipcion3","2020-06-17", "11:00:00"));
        list.add(new Cita(4, R.drawable.baseline_format_list_bulleted_black_18dp,"Titulo4","Descipcio4n","2020-06-17", "11:00:00"));
        return list;


    }
}
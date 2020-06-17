package com.systramer.risk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class ListaCitasActivity extends AppCompatActivity {
    ListView ListViewCita;
    List<Cita> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_citas);
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
        list = new ArrayList<>();
        list.add(new Cita(1, R.drawable.baseline_format_list_bulleted_black_18dp,"Titulo1","Descipcion1","2020-06-17", "11:00:00"));
        list.add(new Cita(2, R.drawable.baseline_format_list_bulleted_black_18dp,"Titulo2","Descipcion2","2020-06-17", "11:00:00"));
        list.add(new Cita(3, R.drawable.baseline_format_list_bulleted_black_18dp,"Titulo3","Descipcion3","2020-06-17", "11:00:00"));
        list.add(new Cita(4, R.drawable.baseline_format_list_bulleted_black_18dp,"Titulo4","Descipcio4n","2020-06-17", "11:00:00"));
        return list;
    }
}
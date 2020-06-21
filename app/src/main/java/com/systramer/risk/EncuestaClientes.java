package com.systramer.risk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.systramer.risk.Utilidades.Utilidades;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class EncuestaClientes extends AppCompatActivity {
    //DIALOG
    String Impacto, Probabilidad;
    TextView TImpacto, TProbabilidad;
    Button Guardar, Cerrar;

    String IdUsuario;
    String IdCliente;
    String IdArea;
    String Titulo;

    ProgressBar progressBar;
    ConexionSQLiteHelper conexionSQLiteHelper;
    ListView ListViewRiesgos;
    List<SitioInteresRiesgos> list;
    private RequestQueue requestQueue;
    ImageView imageView;
    TextView textView;
    private Snackbar snackbar;
    ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuesta_clientes);
        Intent intent = getIntent();
        IdUsuario = intent.getStringExtra("IdUsuario");
        IdCliente = intent.getStringExtra("IdCliente");
        IdArea    = intent.getStringExtra("IdArea");
        Titulo    = intent.getStringExtra("Titulo");
        imageView = (ImageView) findViewById(R.id.imageView2);
        textView = (TextView) findViewById(R.id.textView3);
        layout = (ConstraintLayout) findViewById(R.id.container);
        ListViewRiesgos = findViewById(R.id.ListaRiesgos);
        progressBar = findViewById(R.id.progressBarRiesgos);
        progressBar.setVisibility(View.VISIBLE);
        conexionSQLiteHelper = new ConexionSQLiteHelper(this, "bd_encuestas", null, 3);

        MostrarLista(Integer.parseInt(IdCliente), Integer.parseInt(IdArea));
    }
    public void MostrarLista(final int IdCliente, final  int IdArea){
        SQLiteDatabase select = conexionSQLiteHelper.getReadableDatabase();

        String[] parameters = { String.valueOf(IdArea) };
        String[] campos = {
                Utilidades.IdClienteAreasRiesgo,
                Utilidades.FKIdClienteArea,
                Utilidades.NombreClienteRiesgo,
                Utilidades.ClienteImpacto,
                Utilidades.ClienteProbabilidad,
                Utilidades.ClienteRespondido
        };

        Cursor cursor = select.query(Utilidades.TablaClienteAreasRiesgos,campos, Utilidades.FKIdClienteArea+"=?", parameters, null, null, null);

        list = new ArrayList<>();
        if(cursor.moveToFirst()){
            do {
                int Id            = cursor.getInt(0);
                String Nombre     = cursor.getString(2);
                int Impacto       = cursor.getInt(3);
                int Probabilidad  = cursor.getInt(4);
                String Respondido = cursor.getString(5);
                int Imagen;
                if(Impacto > 0 && Probabilidad > 0){
                    Imagen = R.drawable.baseline_done_black_18dp;
                }
                else{
                    Imagen = R.drawable.baseline_cancel_black_18dp;
                }

                list.add(new SitioInteresRiesgos(Id, Imagen, Nombre, Impacto, Probabilidad, Respondido));

            }while (cursor.moveToNext());
            cursor.close();
        }
        else{
            Toast.makeText(getBaseContext(), "No hay riesgos por mostrar",Toast.LENGTH_LONG).show();
        }
        progressBar.setVisibility(View.INVISIBLE);
        SitioInteresAdapter adapter = new SitioInteresAdapter(getApplicationContext(),list);
        ListViewRiesgos.setAdapter(adapter);

        ListViewRiesgos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SitioInteresRiesgos Riesgo = list.get(position);
                if(Riesgo.Respondido.equals("Concretado")){
                    snackbar.make(layout, "El riesgo ya fue evaluado", Snackbar.LENGTH_LONG).show();
                }
                else{
                    OpenDialog(Riesgo.Id, Riesgo.Riesgo, IdCliente, IdArea);
                }
            }
        });
    }
    public void OpenDialog(final int Id, final String Riesgo, final int IdCliente, final  int IdArea){
        final Dialog dialog = new Dialog(EncuestaClientes.this, R.style.Dialog);

        LayoutInflater layoutInflater = this.getLayoutInflater();
        View custom_dialog = layoutInflater.inflate(R.layout.dialog,null);
        custom_dialog.setElevation(2);


        //crear dropdown

        TImpacto = custom_dialog.findViewById(R.id.Impacto);
        TProbabilidad = custom_dialog.findViewById(R.id.Probabilidad);

        Guardar = custom_dialog.findViewById(R.id.btn_Guardar);
        Cerrar = custom_dialog.findViewById(R.id.btn_Cerrar);

        Guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Impacto = TImpacto.getText().toString();
                Probabilidad = TProbabilidad.getText().toString();
                SQLiteDatabase update = conexionSQLiteHelper.getReadableDatabase();
                ContentValues values = new ContentValues();
                values.put(Utilidades.SitioInteresImpacto, Impacto);
                values.put(Utilidades.SitioInteresProbabilidad, Probabilidad);
                values.put(Utilidades.SitioInteresRespondido, "Concretado");

                update.update(Utilidades.TablaSitioInteresRiesgos, values, Utilidades.IdSitioInteresRiesgo+"=?", new String[]{String.valueOf(Id)});
                update.close();
                MostrarLista(IdCliente, IdArea);
                dialog.dismiss();
            }
        });

        Cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.setContentView(custom_dialog);

        dialog.setTitle(Riesgo);

        dialog.show();
    }
}
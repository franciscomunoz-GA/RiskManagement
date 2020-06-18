package com.systramer.risk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.systramer.risk.Utilidades.Utilidades;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class EncuestaSitioInteres extends AppCompatActivity {
    String IdUsuario;
    String IdCita;
    String Tipo;
    ConexionSQLiteHelper conexionSQLiteHelper;
    ListView ListViewCita;
    List<SitioInteresRiesgos> list;
    private RequestQueue requestQueue;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuesta_sitio_interes);

        Intent intent = getIntent();
        IdCita    = intent.getStringExtra("IdCita");
        IdUsuario = intent.getStringExtra("IdUsuario");
        Tipo      = intent.getStringExtra("Tipo");
        progressBar = findViewById(R.id.progressBar);
        ListViewCita = findViewById(R.id.ListaCitas);

        conexionSQLiteHelper = new ConexionSQLiteHelper(this, "bd_encuestas", null, 1);

        TraerInformacion();
    }
    private void TraerInformacion(){
        progressBar.setVisibility(View.VISIBLE);

        final JSONObject Parametros = new JSONObject();
        try {
            Parametros.put("Id", IdCita);
            Parametros.put("Tipo", Tipo);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final String savedata = Parametros.toString();
        String URL = "http://cuenta-cuentas.com/backend/public/api/Seleccionar/EncuestaD";
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    progressBar.setVisibility(View.INVISIBLE);
                    JSONObject obj = new JSONObject(response);
                    String Mensaje = obj.getString("Message");
                    if(Mensaje.equals("Consulta Exitosa")){
                        String Data = obj.getString("Data");
                        JSONArray Informacion = new JSONArray(Data);
                        for (int i = 0; i < Informacion.length(); i++) {
                            JSONObject object = Informacion.getJSONObject(i);
                            //Creamos la encuesta en SQLite
                            String IdEncuesta = InsertarRegistro(Utilidades.TablaSitioInteres, object);
                            //recorremos todos los tiesgos que tiene la encuesta
                            String Riesgos = object.getString("Riesgos");
                            JSONArray ListaRiesgos = new JSONArray(Riesgos);
                            for (int j = 0; j < ListaRiesgos.length(); j++) {
                                JSONObject riesgo = ListaRiesgos.getJSONObject(j);
                                riesgo.put("IdSitioInteres", IdEncuesta);
                                //Creamos el riesgo en SQLite
                                String IdRiesgo = InsertarRegistro(Utilidades.TablaSitioInteresRiesgos, riesgo);

                                Toast.makeText(getApplicationContext(), "IdRiesgo: "+ IdRiesgo, Toast.LENGTH_SHORT).show();
                            }

                        }
                        MostrarLista();
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public String getBodyContentType(){ return "application/json; charset=utf-8";}
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return savedata == null ? null: savedata.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        requestQueue.add(stringRequest);
    }
    private String InsertarRegistro(String Tabla, JSONObject Parametros){
        SQLiteDatabase select = conexionSQLiteHelper.getReadableDatabase();
        SQLiteDatabase insert = conexionSQLiteHelper.getWritableDatabase();
        String Resultado = "";
        ContentValues values = new ContentValues();
        switch (Tabla){
            case Utilidades.TablaSitioInteres:
                try {

                    int IdEncuesta = Parametros.getInt("IdEncuesta");
                    String Titulo  = Parametros.getString("Titulo");

                    String[] parameters = { String.valueOf(IdEncuesta) };
                    String[] campos = { Utilidades.IdSitioInteres };

                    Cursor cursor = select.query(Utilidades.TablaSitioInteres,campos, Utilidades.IdSitioInteres+"=?", parameters, null, null, null);
                    cursor.moveToFirst();
                    try {
                        return cursor.getString(0);
                    }
                     catch (Exception e){
                         values.put(Utilidades.IdSitioInteres, IdEncuesta);
                         values.put(Utilidades.NombreSitioInteres, Titulo);

                         Long idResultante = insert.insert(Utilidades.TablaSitioInteres, Utilidades.IdSitioInteres, values);
                         insert.close();
                         return String.valueOf(idResultante);

                     }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case Utilidades.TablaSitioInteresRiesgos:
                try {

                    int IdRiesgo = Parametros.getInt("IdRSI");
                    int IdSitioInteres  = Parametros.getInt("IdSitioInteres");
                    String Nombre  = Parametros.getString("Riesgo");


                    Toast.makeText(getBaseContext(), "IdEncuesta: "+IdRiesgo + "IdSitioInteres: "+IdSitioInteres+" Titulo: "+Nombre, Toast.LENGTH_LONG).show();

                    String[] parameters = { String.valueOf(IdRiesgo) };
                    String[] campos = { Utilidades.IdSitioInteresRiesgo };

                    Cursor cursor = select.query(Utilidades.TablaSitioInteresRiesgos,campos, Utilidades.IdSitioInteresRiesgo+"=?", parameters, null, null, null);
                    cursor.moveToFirst();
                    try {
                        return cursor.getString(0);
                    }
                    catch (Exception e){
                        values.put(Utilidades.IdSitioInteresRiesgo, IdRiesgo);
                        values.put(Utilidades.FKIdSitioInteres, IdSitioInteres);
                        values.put(Utilidades.NombreSitioInteresRiesgo, Nombre);

                        Long idResultante = insert.insert(Utilidades.TablaSitioInteresRiesgos, Utilidades.IdSitioInteresRiesgo, values);
                        insert.close();
                        return String.valueOf(idResultante);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                Resultado = "";
                break;
        };
        //
        return Resultado;
    }
    private void MostrarLista(){
        /*
        list = new ArrayList<>();

        list.add(new SitioInteresRiesgos());
        list.add(new Cita(Id, Imagen,Tipo,NombreTipo,Descripcion, Fecha, Hora, Titulo));
        Custom_Adapter  adapter = new Custom_Adapter(getApplicationContext(),list);
        ListViewCita.setAdapter(adapter);

        ListViewCita.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cita cita = list.get(position);
                    Intent intent = new Intent(ListaCitasActivity.this, EncuestaSitioInteres.class);
                    intent.putExtra("IdUsuario", IdUsuario);
                    intent.putExtra("IdCita", String.valueOf(cita.Id));
                    intent.putExtra("Tipo", String.valueOf(cita.Tipo));
                    startActivity(intent);
            }
        });
         */
    }
}
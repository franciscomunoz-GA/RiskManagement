package com.systramer.risk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
    ListView ListViewRiesgos;
    List<SitioInteresRiesgos> list;

    private RequestQueue requestQueue;
    ProgressBar progressBar;
    SitioInteresAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuesta_sitio_interes);

        Intent intent = getIntent();
        IdCita    = intent.getStringExtra("IdCita");
        IdUsuario = intent.getStringExtra("IdUsuario");
        Tipo      = intent.getStringExtra("Tipo");
        progressBar = findViewById(R.id.progressBarRiesgos);
        ListViewRiesgos = findViewById(R.id.ListaRiesgos);
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
                                String R = InsertarRegistro(Utilidades.TablaSitioInteresRiesgos, riesgo);
                                Toast.makeText(getApplicationContext(), "Riesgo: "+ R, Toast.LENGTH_SHORT).show();
                            }
                            MostrarLista(IdEncuesta);
                        }
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

                    String[] parameters = { String.valueOf(IdRiesgo) };
                    String[] campos = { Utilidades.IdSitioInteresRiesgo };

                    Cursor cursor = select.query(Utilidades.TablaSitioInteresRiesgos,campos, Utilidades.IdSitioInteresRiesgo+"=?", parameters, null, null, null);
                    cursor.moveToFirst();
                    try {
                        return "encontrado "+cursor.getString(0);
                    }
                    catch (Exception e){
                        values.put(Utilidades.IdSitioInteresRiesgo, IdRiesgo);
                        values.put(Utilidades.FKIdSitioInteres, IdSitioInteres);
                        values.put(Utilidades.NombreSitioInteresRiesgo, Nombre);
                        values.put(Utilidades.SitioInteresProbabilidad, 0);
                        values.put(Utilidades.SitioInteresImpacto, 0);
                        Long idResultante = insert.insert(Utilidades.TablaSitioInteresRiesgos, Utilidades.IdSitioInteresRiesgo, values);
                        insert.close();
                        return "creado "+idResultante;

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
    public void MostrarLista(String IdSitioInteres){
        SQLiteDatabase select = conexionSQLiteHelper.getReadableDatabase();
        //Cursor cursor = select.rawQuery("SELECT * FROM "+Utilidades.TablaSitioInteresRiesgos+" WHERE "+Utilidades.FKIdSitioInteres+" = "+ Integer.parseInt(IdSitioInteres), null);
        Cursor cursor = select.rawQuery("SELECT * FROM "+Utilidades.TablaSitioInteresRiesgos, null);

        list = new ArrayList<>();

        if(cursor.moveToFirst()){
            do {
                int Id           = cursor.getInt(0);
                int IdSitioIntere   = cursor.getInt(1);
                String Nombre    = cursor.getString(2);
                int Impacto      = cursor.getInt(3);
                int Probabilidad = cursor.getInt(4);
                int Imagen;
                if(Impacto > 0 && Probabilidad > 0){
                    Imagen = R.drawable.baseline_done_black_18dp;
                }
                else{
                    Imagen = R.drawable.baseline_cancel_black_18dp;
                }
                list.add(new SitioInteresRiesgos(Id,Imagen, Nombre, Impacto, Probabilidad));

            }while (cursor.moveToNext());
        }
        SitioInteresAdapter adapter = new SitioInteresAdapter(getApplicationContext(),list);
        ListViewRiesgos.setAdapter(adapter);

        ListViewRiesgos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SitioInteresRiesgos sitioInteresRiesgos = list.get(position);
                Toast.makeText(getBaseContext(), sitioInteresRiesgos.Id, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
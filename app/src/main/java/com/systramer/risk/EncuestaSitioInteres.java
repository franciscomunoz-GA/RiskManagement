package com.systramer.risk;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.systramer.risk.Utilidades.Utilidades;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class EncuestaSitioInteres extends AppCompatActivity {
    String Impacto, Probabilidad;
    TextView TImpacto, TProbabilidad;
    Button Guardar, Cerrar;

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
        conexionSQLiteHelper = new ConexionSQLiteHelper(this, "bd_encuestas", null, 2);

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
                                int IdSitioInteres = object.getInt("IdEncuesta");
                                riesgo.put("IdSitioInteres", IdSitioInteres);
                                //Creamos el riesgo en SQLite
                                String R = InsertarRegistro(Utilidades.TablaSitioInteresRiesgos, riesgo);
                                MostrarLista(object.getInt("IdEncuesta"));
                            }
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
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

                    int IdRiesgo        = Parametros.getInt("IdRSI");
                    int IdSitioInteres  = Parametros.getInt("IdSitioInteres");
                    String Nombre       = Parametros.getString("Riesgo");

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
    public void MostrarLista(final int IdSitioInteres){
        SQLiteDatabase select = conexionSQLiteHelper.getReadableDatabase();

        String[] parameters = { String.valueOf(IdSitioInteres) };
        String[] campos = { Utilidades.IdSitioInteresRiesgo, Utilidades.FKIdSitioInteres, Utilidades.NombreSitioInteresRiesgo, Utilidades.SitioInteresImpacto, Utilidades.SitioInteresProbabilidad };

        Cursor cursor = select.query(Utilidades.TablaSitioInteresRiesgos,campos, Utilidades.FKIdSitioInteres+"=?", parameters, null, null, null);

        list = new ArrayList<>();
        if(cursor.moveToFirst()){
            do {
                int Id            = cursor.getInt(0);
                int IdSitioIntere = cursor.getInt(1);
                String Nombre     = cursor.getString(2);
                int Impacto       = cursor.getInt(3);
                int Probabilidad  = cursor.getInt(4);
                int Imagen;
                if(Impacto > 0 && Probabilidad > 0){
                    Imagen = R.drawable.baseline_done_black_18dp;
                }
                else{
                    Imagen = R.drawable.baseline_cancel_black_18dp;
                }
                list.add(new SitioInteresRiesgos(Id,Imagen, Nombre, Impacto, Probabilidad));

            }while (cursor.moveToNext());
            cursor.close();
        }
        else{
            Toast.makeText(getBaseContext(), "No hay riesgos por mostrar",Toast.LENGTH_LONG).show();
            finish();
        }
        SitioInteresAdapter adapter = new SitioInteresAdapter(getApplicationContext(),list);
        ListViewRiesgos.setAdapter(adapter);

        ListViewRiesgos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SitioInteresRiesgos sitioInteresRiesgos = list.get(position);
                OpenDialog(sitioInteresRiesgos.Id, sitioInteresRiesgos.Riesgo, IdSitioInteres);
            }
        });
    }
    public void OpenDialog(final int Id, final String Riesgo, final int IdSitioInteres){
        final Dialog dialog = new Dialog(EncuestaSitioInteres.this, R.style.CustomDialogTheme);

        LayoutInflater layoutInflater = this.getLayoutInflater();
        View custom_dialog = layoutInflater.inflate(R.layout.dialog,null);

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

                update.update(Utilidades.TablaSitioInteresRiesgos, values, Utilidades.IdSitioInteresRiesgo+"=?", new String[]{String.valueOf(Id)});
                update.close();
                MostrarLista(IdSitioInteres);
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
    public void Cerrar(View view){
        finish();
    }
    public void Guardar(View view) {
        boolean Resultado = false;
        SQLiteDatabase select = conexionSQLiteHelper.getReadableDatabase();

        String[] parameters = {IdCita};
        String[] campos = {Utilidades.IdSitioInteresRiesgo, Utilidades.FKIdSitioInteres, Utilidades.NombreSitioInteresRiesgo, Utilidades.SitioInteresImpacto, Utilidades.SitioInteresProbabilidad};

        Cursor cursor = select.query(Utilidades.TablaSitioInteresRiesgos, campos, Utilidades.FKIdSitioInteres + "=?", parameters, null, null, null);
        final JSONArray Riesgos = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                int Id = cursor.getInt(0);
                int IdSitioIntere = cursor.getInt(1);
                String Nombre = cursor.getString(2);
                int Impacto = cursor.getInt(3);
                int Probabilidad = cursor.getInt(4);
                int Imagen;

                final JSONObject Calificacion = new JSONObject();

                if(Impacto > 0 && Probabilidad > 0){

                    Resultado = true;
                    try {
                        Calificacion.put("IdRSI", Id);
                        Calificacion.put("Probabilidad", Probabilidad);
                        Calificacion.put("Impacto", Impacto);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Riesgos.put(Calificacion);
                }
                else{
                    Toast.makeText(getBaseContext(), "El riesgo: "+Nombre+" no ha sido evaluado",Toast.LENGTH_LONG).show();
                    Resultado = false;
                    break;
                }

            } while (cursor.moveToNext());
            cursor.close();

            if(Resultado){
                MandarResultados(Riesgos);
            }
        }
    }
    private void  MandarResultados(JSONArray Riesgos){
        final JSONObject Parametros = new JSONObject();
        try {
        Parametros.put("IdEncuesta", IdCita);
        Parametros.put("Tipo", 1);
        Parametros.put("IdUsuario", IdUsuario);
        Parametros.put("Riesgos", Riesgos);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String savedata = Parametros.toString();
        String URL = "http://cuenta-cuentas.com/backend/public/api/Responder/Encuesta";
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
                        if(Data == "true"){
                            Toast.makeText(getApplicationContext(), Mensaje, Toast.LENGTH_LONG).show();
                            finish();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Error al insertar", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
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
}
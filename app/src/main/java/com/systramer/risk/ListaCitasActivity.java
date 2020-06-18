package com.systramer.risk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.systramer.risk.ui.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListaCitasActivity extends AppCompatActivity {
    ProgressBar progressBar;
    ListView ListViewCita;
    List<Cita> list;
    String IdUsuario;
    String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    private RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_citas);
        Intent intent = getIntent();
        IdUsuario = intent.getStringExtra("IdUsuario");
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        ListViewCita = findViewById(R.id.ListaCitas);
        progressBar = findViewById(R.id.progressBar);
        TraerInformacion();

    }
    private void TraerInformacion(){
        progressBar.setVisibility(View.VISIBLE);
        final JSONObject Parametros = new JSONObject();
        try {
            Parametros.put("Id", IdUsuario);
            Parametros.put("Fecha", currentDate);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final String savedata = Parametros.toString();
        String URL = "http://cuenta-cuentas.com/backend/public/api/Seleccionar/Encuestas";
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
                        list = new ArrayList<>();
                        for (int i = 0; i < Informacion.length(); i++){
                            JSONObject object = Informacion.getJSONObject(i);
                            int Id             = object.getInt("Id");
                            int Tipo           = object.getInt("Tipo");
                            String Descripcion = object.getString("Descripcion");
                            String Fecha       = object.getString("Fecha");
                            String Hora        = object.getString("Hora");
                            String Titulo      = object.getString("Titulo");
                            String NombreTipo;
                            int Imagen;
                            if(Tipo == 1){
                                NombreTipo = "Sitio de interés";
                                Imagen = R.drawable.baseline_business_black_18dp;
                            }
                            else{
                                NombreTipo = "Cliente";
                                Imagen = R.drawable.baseline_face_black_18dp;
                            }

                            list.add(new Cita(Id, Imagen,Tipo,NombreTipo,Descripcion, Fecha, Hora, Titulo));
                            Custom_Adapter  adapter = new Custom_Adapter(getApplicationContext(),list);
                            ListViewCita.setAdapter(adapter);

                            ListViewCita.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Cita cita = list.get(position);
                                    if(cita.Tipo == 1){
                                        Intent intent = new Intent(ListaCitasActivity.this, EncuestaSitioInteres.class);
                                        intent.putExtra("IdUsuario", IdUsuario);
                                        intent.putExtra("IdCita", cita.Id);
                                        intent.putExtra("Tipo", cita.Tipo);
                                        startActivity(intent);
                                    }
                                    else{
                                        //Activity para Clientes
                                        Intent intent = new Intent(ListaCitasActivity.this, EncuestaSitioInteres.class);
                                        intent.putExtra("IdUsuario", IdUsuario);
                                        intent.putExtra("IdCita", cita.Id);
                                        intent.putExtra("Tipo", cita.Tipo);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    TraerInformacion();
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
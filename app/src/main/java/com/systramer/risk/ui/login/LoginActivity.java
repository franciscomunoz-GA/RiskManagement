package com.systramer.risk.ui.login;

import android.Manifest;
import android.app.Activity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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
import com.systramer.risk.R;
import com.systramer.risk.ui.login.LoginViewModel;
import com.systramer.risk.ui.login.LoginViewModelFactory;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class LoginActivity extends AppCompatActivity {
    private RequestQueue requestQueue;
    private LoginViewModel loginViewModel;

    public TelephonyManager manager;
    public String IMEI;

    ConstraintLayout layout;
    private Snackbar snackbar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    String user = usernameEditText.getText().toString().trim();
                    String pass = passwordEditText.getText().toString().trim();
                    ValidarSesion(user, pass);
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                //finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
        layout = (ConstraintLayout) findViewById(R.id.container);
        //OBTENER IMEI
        manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        int permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if(permiso == PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                IMEI = manager.getImei().toString();
            }
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},123);
        }
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        //Toast.makeText(getApplicationContext(), IMEI, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();

    }
    private void ValidarSesion(String User, String Pass){
        Toast.makeText(getApplicationContext(), "User: "+User+" Pass: "+Pass+" IMEI: "+IMEI, Toast.LENGTH_LONG).show();
        final JSONObject Parametros = new JSONObject();
        try {
            Parametros.put("Correo", User);
            Parametros.put("Password", Pass);
            Parametros.put("Imei", IMEI);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final String savedata = Parametros.toString();
        String URL = "http://cuenta-cuentas.com/backend/public/api/Sesion/UsuarioAplicacion";
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    String Mensaje = obj.getString("Message");
                    if(Mensaje.equals("Consulta Exitosa")){
                        String Data = obj.getString("Data");
                        //for (int i = 0; i < Data.length(); i++){
                        //JSONObject Fila = Data.getJSONObject(i);
                        switch (Data){
                            case "Usuario incorrecto":
                                snackbar.make(layout, "Usuario y/o contraseña incorrecto(s)", Snackbar.LENGTH_LONG).show();
                                break;
                            case "Contraseña incorrecta":
                                snackbar.make(layout, "Usuario y/o contraseña incorrecto(s)", Snackbar.LENGTH_LONG).show();
                                break;
                            case "IMEI incorrecto":
                                snackbar.make(layout, "IMEI incorrecto", Snackbar.LENGTH_LONG).show();
                                break;
                            default:
                                JSONObject Informacion = new JSONObject(Data);
                                int Id        = Informacion.getInt("Id");
                                String Nombre = Informacion.getString("Nombre");
                                String Telefono  = Informacion.getString("Telefono");
                                Toast.makeText(getApplicationContext(), "Bienvenido "+Nombre, Toast.LENGTH_SHORT).show();
                                snackbar.make(layout, "Bienvenido "+Nombre, Snackbar.LENGTH_LONG).show();
                                break;
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
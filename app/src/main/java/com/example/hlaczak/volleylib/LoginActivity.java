package com.example.hlaczak.volleylib;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class LoginActivity extends AppCompatActivity {

     EditText login, password;
     Button btnLogin;
     
     String loginS, passwordS;
     private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mContext = getApplicationContext();

        login = findViewById(R.id.login);
        login.setText("login");
        password = findViewById(R.id.password);
        password.setText("password");
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginS = login.getText().toString();
                passwordS = password.getText().toString();
                if(!loginS.equals("") && !passwordS.equals("")){
                    tryToLogin(loginS, passwordS);
                } else {
                    Toast.makeText(LoginActivity.this, "Uzupełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                    login.setText("");
                    password.setText("");
                }
            }
        });

    }

    private void tryToLogin(String loginS, String passwordS) {
        JSONObject payload = null;
        try {
            payload = new JSONObject();
            payload.put("login", loginS);
            payload.put("password", passwordS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String url = "https://xxx.xxx.xxx.xxx/api/Account/Login";
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, payload,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        JsonElement json = new JsonParser().parse(String.valueOf(response));
                        String jsonWebToken = json.getAsJsonObject().get("jsonWebToken").getAsString();
                        String sessionID = json.getAsJsonObject().get("sessionId").getAsString();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("jsonWebToken", jsonWebToken);
                        intent.putExtra("sessionID", sessionID);

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }},
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "Zły login lub hasło!", Toast.LENGTH_SHORT).show();
                        login.setText("");
                        password .setText("");
                    }
                });
        VolleySingleton.getInstance(mContext).addToRequestQueue(jsonObjReq, new HurlStack(null, getSocketFactory()));

    }

    private SSLSocketFactory getSocketFactory() {
        CertificateFactory cf = null;
        try {

            cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = getResources().openRawResource(R.raw.server);
            Certificate ca;
            try {

                ca = cf.generateCertificate(caInput);
            } finally {
                caInput.close();
            }

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);


            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);


            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {

                    Log.e("CipherUsed", session.getCipherSuite());
//                    return hostname.compareTo("10.10.10.10") == 0; //The Hostname of your server.
                    return true;

                }
            };

            //handleSSLHandshake
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            SSLContext context = null;
            context = SSLContext.getInstance("SSL");

            context.init(null, tmf.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

            SSLSocketFactory sf = context.getSocketFactory();
            return sf;

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return  null;
    }

}

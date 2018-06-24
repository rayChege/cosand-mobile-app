package wizag.com.supa;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static wizag.com.supa.MainActivity.ACCESS_TOKEN;


public class Activity_Spinner extends AppCompatActivity {
    Spinner spinner;
    String URL = "http://sduka.wizag.biz/api/material";
    String POST_MATERIAL_URL = "http://sduka.wizag.biz/api/material";

    RelativeLayout buy_layout;
    ArrayList<String> CountryName;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    SessionManager sessionManager;
    String token;
    Button proceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spinner);
        CountryName = new ArrayList<>();
        spinner = (Spinner) findViewById(R.id.buy_spinner);
        buy_layout = (RelativeLayout) findViewById(R.id.buy_layout);
        proceed = (Button) findViewById(R.id.proceed_buy);
        sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        token = user.get("access_token");
        // Toast.makeText(this, "Data" + token, Toast.LENGTH_SHORT).show();


        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //post material to db
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(Activity_Spinner.this);


                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.POST, POST_MATERIAL_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    String success_message = data.getString("message");
                                    Snackbar.make(buy_layout,""+success_message,Snackbar.LENGTH_LONG).show();


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                //Toast.makeText(Activity_Spinner.this, "", Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                        Snackbar.make(buy_layout,""+error.getMessage(),Snackbar.LENGTH_LONG).show();
                    }
                }) {
                    //adding parameters to the request
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("name", spinner.getSelectedItem().toString());
                        params.put("code", "blst786");
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        String bearer = "Bearer ".concat(token);
                        Map<String, String> headersSys = super.getHeaders();
                        Map<String, String> headers = new HashMap<String, String>();
                        headersSys.remove("Authorization");
                        headers.put("Authorization", bearer);
                        headers.putAll(headersSys);
                        return headers;
                    }
                };
// Add the request to the RequestQueue.
                queue.add(stringRequest);
            }


        });


        loadSpinnerData(URL);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String country = spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString();
                // Toast.makeText(getApplicationContext(), country, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // DO Nothing here
            }
        });

    }

    private void loadSpinnerData(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject != null) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        JSONArray materials = data.getJSONArray("materials");

                        if (materials != null) {
                            for (int i = 0; i < materials.length(); i++) {
                                JSONObject material_items = materials.getJSONObject(i);
                                if (material_items != null) {
                                    int id = material_items.getInt("id");
                                    String name = material_items.getString("name");
                                    CountryName.add(name);

                                }

                            }
                        }


                    }


                    spinner.setAdapter(new ArrayAdapter<String>(Activity_Spinner.this, android.R.layout.simple_spinner_dropdown_item, CountryName));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }


        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String bearer = "Bearer ".concat(token);
                Map<String, String> headersSys = super.getHeaders();
                Map<String, String> headers = new HashMap<String, String>();
                headersSys.remove("Authorization");
                headers.put("Authorization", bearer);
                headers.putAll(headersSys);
                return headers;
            }

        };
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);


        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);


    }


}

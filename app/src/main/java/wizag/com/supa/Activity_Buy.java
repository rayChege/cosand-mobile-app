package wizag.com.supa;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Activity_Buy extends AppCompatActivity {
    Spinner spinner, buy_spinner_size, buy_spinner_quality;
    String URL = "http://sduka.wizag.biz/api/material";
    String POST_MATERIAL_URL = "http://sduka.wizag.biz/api/order-request";

    LinearLayout buy_layout;
    ArrayList<String> CountryName;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    SessionManager sessionManager;
    String token;
    Button proceed;
    GPSLocation gps;

    ArrayList<String> CategoryName;
    ArrayList<String> QualityName;
    ArrayList<String> SizeName;
    ArrayList<String> PriceName;


    HashMap<String, String> map_values;
    HashMap<String, String> quality_values;
    HashMap<String, String> size_values;

    String id_material;
    String id_quality;
    String id_size;

    EditText quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        CountryName = new ArrayList<>();

        spinner = (Spinner) findViewById(R.id.buy_spinner);
        buy_spinner_size = (Spinner) findViewById(R.id.buy_spinner_size);
        buy_spinner_quality = (Spinner) findViewById(R.id.buy_spinner_quality);

        map_values = new HashMap<String, String>();
        quality_values = new HashMap<String, String>();
        size_values = new HashMap<String, String>();
        quantity = (EditText) findViewById(R.id.buy_quantity);

        CategoryName = new ArrayList<>();
        QualityName = new ArrayList<>();
        SizeName = new ArrayList<>();
        PriceName = new ArrayList<>();


        buy_layout = (LinearLayout) findViewById(R.id.buy_layout);
        proceed = (Button) findViewById(R.id.proceed_buy);
        sessionManager = new SessionManager(getApplicationContext());
        HashMap<String, String> user = sessionManager.getUserDetails();
        token = user.get("access_token");
        // Toast.makeText(this, "Data" + token, Toast.LENGTH_SHORT).show();
        gps = new GPSLocation(this);
        if (gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            // Toast.makeText(this, "data\n"+longitude, Toast.LENGTH_SHORT).show();

            //String coordinates = latitude+ ","+longitude;
            //  Toast.makeText(gps, "data\n\n"+latitude, Toast.LENGTH_SHORT).show();
        } else {
            gps.showSettingsAlert();
        }

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);


            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String value = spinner.getSelectedItem().toString();
                id_material = map_values.get(value);

                //Toast.makeText(getApplicationContext(), ""+id_material, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        buy_spinner_size.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String value = buy_spinner_size.getSelectedItem().toString();
                id_size = size_values.get(value);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        buy_spinner_quality.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String value = buy_spinner_quality.getSelectedItem().toString();
                id_quality = quality_values.get(value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //post material to db
                // Instantiate the RequestQueue.
                //validate quantity
                String quantity_txt = quantity.getText().toString();
                if (quantity_txt.isEmpty()) {
                    Snackbar.make(buy_layout, "Enter Quantity to continue", Snackbar.LENGTH_LONG).show();

                } else if (Integer.parseInt(quantity_txt) < 12) {
                    Snackbar.make(buy_layout, "Quantity value should be more than 12", Snackbar.LENGTH_LONG).show();

                } else if ((Integer.parseInt(quantity_txt))% 2 != 0) {
                    Snackbar.make(buy_layout, "Quantity value should be an even number", Snackbar.LENGTH_LONG).show();

                } else {
                    postOrder();
                }


                loadSpinnerData(URL);


            }

            private void postOrder() {
                RequestQueue queue = Volley.newRequestQueue(Activity_Buy.this);


                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.POST, POST_MATERIAL_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    String success_message = data.getString("message");
                                    Snackbar.make(buy_layout, "Order request made successfully", Snackbar.LENGTH_LONG).show();
                                    startActivity(new Intent(getApplicationContext(), MenuActivity.class));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                //Toast.makeText(Activity_Buy.this, "", Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                        Snackbar.make(buy_layout, "An Error Occurred", Snackbar.LENGTH_LONG).show();
                    }
                }) {
                    //adding parameters to the request
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("material-id", "1");
                        params.put("quality-id", "1");
                        params.put("size-id", "1");
                        params.put("quantity", quantity.getText().toString());
                        //  params.put("")
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
                                String material_name = material_items.getString("name");
                                String material_id = material_items.getString("id");
                                map_values.put(material_name, material_id);

                                if (material_items != null) {
                                    if (CountryName.contains(materials.getJSONObject(i).getString("name"))) {

                                    } else {

                                        CountryName.add(materials.getJSONObject(i).getString("name"));

                                    }


                                }

                                //categories
                                /*loop through categories*/

                                JSONArray category = material_items.getJSONArray("category");
                                for (int k = 0; k < category.length(); k++) {

                                    JSONObject category_items = category.getJSONObject(k);

                                    /*loop thro quality*/


                                    JSONArray quality = category_items.getJSONArray("quality");
                                    for (int l = 0; l < quality.length(); l++) {
                                        JSONObject quality_object = quality.getJSONObject(l);
                                        String quality_name = quality_object.getString("value");
                                        String quality_id = quality_object.getString("id");
                                        quality_values.put(quality_name, quality_id);
                                        /*loop thro size*/
                                        JSONObject size_items = quality.getJSONObject(k);
                                        JSONArray size = size_items.getJSONArray("size");

                                        for (int m = 0; m < size.length(); m++) {


                                            if (size != null) {

                                                if (SizeName.contains(size.getJSONObject(m).getString("size"))) {


                                                } else {

                                                    SizeName.add(size.getJSONObject(m).getString("size"));
                                                    //Toast.makeText(getApplicationContext(), "data\n" + size.getJSONObject(m).getString("size"), Toast.LENGTH_SHORT).show();


                                                }

                                            }
                                            buy_spinner_size.setAdapter(new ArrayAdapter<String>(Activity_Buy.this, android.R.layout.simple_spinner_dropdown_item, SizeName));

                                        }


                                        if (quality != null) {

                                            if (QualityName.contains(quality.getJSONObject(l).getString("value"))) {


                                            } else {

                                                QualityName.add(quality.getJSONObject(l).getString("value"));
                                                // Toast.makeText(getApplicationContext(), "data\n" + quality.getJSONObject(l).getString("value"), Toast.LENGTH_SHORT).show();


                                            }

                                        }
                                        buy_spinner_quality.setAdapter(new ArrayAdapter<String>(Activity_Buy.this, android.R.layout.simple_spinner_dropdown_item, QualityName));

                                    }


                                    if (category != null) {
                                        if (CategoryName.contains(category.getJSONObject(k).getString("name"))) {


                                        } else {

                                            CategoryName.add(category.getJSONObject(k).getString("name"));
                                            //Toast.makeText(getApplicationContext(), "category\n" + category.getJSONObject(k).getString("name"), Toast.LENGTH_SHORT).show();


                                        }


                                    }


                                }


                            }
                        }


                    }


                    spinner.setAdapter(new ArrayAdapter<String>(Activity_Buy.this, android.R.layout.simple_spinner_dropdown_item, CountryName));

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

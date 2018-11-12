package wizag.com.supa.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import wizag.com.supa.MySingleton;
import wizag.com.supa.R;

public class Activity_Register extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    ProgressDialog progressDialog;
    EditText edit_firstname, edit_lastname, edit_id, edit_phone, email_address, create_password, confirm_password;
    private static final String SHARED_PREF_NAME = "profile";
    Button button_register, upload_image;
    String RegisterUrl = "http://sduka.wizag.biz/api/v1/auth/signup";
    private int SELECT_FILE = 1;
    private int REQUEST_CAMERA = 0;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    LinearLayout image_layout;
    String photo_path = "";

    ImageView id_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(this);

        image_layout = findViewById(R.id.image_layout);
        id_image = findViewById(R.id.id_image);
        edit_firstname = (EditText) findViewById(R.id.edit_firstname);
        edit_lastname = (EditText) findViewById(R.id.edit_lastname);
        edit_id = (EditText) findViewById(R.id.edit_id);
        edit_phone = (EditText) findViewById(R.id.edit_phone);
        email_address = (EditText) findViewById(R.id.email_address);
        create_password = (EditText) findViewById(R.id.create_password);
        confirm_password = (EditText) findViewById(R.id.confirm_password);
        button_register = (Button) findViewById(R.id.button_register);
        upload_image = (Button) findViewById(R.id.upload_image);
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*store reg details on shared prefs*/
                SharedPreferences sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();

                final String f_name = edit_firstname.getText().toString();
                final String l_name = edit_lastname.getText().toString();
                final String edit_id_txt = edit_id.getText().toString();
                final String edit_phone_txt = edit_phone.getText().toString();
                final String email_address_txt = email_address.getText().toString();
                final String create_password_txt = create_password.getText().toString();
                final String confirm_password_txt = confirm_password.getText().toString();

                editor.putString("reg_fname", f_name);
                editor.putString("reg_lname", l_name);
                editor.putString("reg_id", edit_id_txt);
                editor.putString("reg_phone", edit_phone_txt);
                editor.putString("reg_email", email_address_txt);
                editor.apply();


                if (TextUtils.isEmpty(f_name)) {
                    edit_firstname.setError("Please enter first name");
                    edit_firstname.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(l_name)) {
                    edit_lastname.setError("Please enter last name");
                    edit_lastname.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(edit_id_txt)) {
                    edit_id.setError("Please enter ID or Passport number");
                    edit_id.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(edit_phone_txt) || !edit_phone_txt.startsWith("07") || edit_phone_txt.length() < 10) {
                    edit_phone.setError("Please enter a valid Phone number");
                    edit_phone.requestFocus();
                    return;
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email_address_txt).matches()) {
                    email_address.setError("Enter a valid email");
                    email_address.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(create_password_txt)) {
                    create_password.setError("Enter a password");
                    create_password.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(confirm_password_txt)) {
                    confirm_password.setError("Confirm password");
                    confirm_password.requestFocus();
                    return;
                } else if (!create_password_txt.equals(confirm_password_txt)) {
                    confirm_password.setError("Both passwords should match");
                    confirm_password.requestFocus();
                    return;
                }
                /*else if(id_image.getDrawable() == null){
                    Toast.makeText(Activity_Register.this, "", Toast.LENGTH_SHORT).show();
                }*/

                else if (!isNetworkConnected()) {

                    Toast.makeText(Activity_Register.this, "Ensure you have internet connection", Toast.LENGTH_SHORT).show();

                } else {
                    registerUser();
                }


            }
        });


    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    private void registerUser() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing in...");
        progressDialog.show();
        //getText

        StringRequest stringRequest = new StringRequest(Request.Method.POST, RegisterUrl, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                try {
                    //converting response to json object

                    JSONObject obj = new JSONObject(response);

                    String message = obj.getString("message");
                    String status = obj.getString("status");
//                            JSONObject data = new JSONObject("data");
                    if (status.equalsIgnoreCase("success")) {
                        Toast.makeText(Activity_Register.this, message, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), Activity_Login.class));
                        finish();
                    } else if (status.equalsIgnoreCase("error")) {

                        Toast.makeText(Activity_Register.this, message, Toast.LENGTH_SHORT).show();
                    }
                    JSONArray jsonArray = obj.getJSONArray("data");
                    for (int k = 0; k < jsonArray.length(); k++) {
                        String data_message = jsonArray.getString(k);

                        if (status.equalsIgnoreCase("fail")) {
                            Toast.makeText(Activity_Register.this, data_message, Toast.LENGTH_SHORT).show();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(Activity_Register.this, "An error occurred" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }


        ) {

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                params.put("email", email_address.getText().toString());
                params.put("id_no", edit_id.getText().toString());
                params.put("password", create_password.getText().toString());
                params.put("password_confirmation", confirm_password.getText().toString());
                params.put("fname", edit_firstname.getText().toString());
                params.put("lname", edit_lastname.getText().toString());
                params.put("phone", edit_phone.getText().toString());

//                params.put("role_id", "2");
                return params;
            }


        };

        MySingleton.getInstance(this).addToRequestQueue(stringRequest);


    }

    public void showOptions(View view) {


        PopupMenu popup = new PopupMenu(this, view);

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.truck_menu);
        popup.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.existing:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);//
                startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);

                return true;
            case R.id.camera_photo:

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_PERMISSION_CODE);
                }


               /* if (ContextCompat.checkSelfPermission(Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            MY_CAMERA_PERMISSION_CODE);
                }*/
                else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, REQUEST_CAMERA);
                }

                return true;
            default:
                return false;
        }
    }
    public String getPath(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(contentUri, proj, "", null, "");
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        String path = Environment.getExternalStorageDirectory() + res.replace("/storage/emulated/0", "");
        return res;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == SELECT_FILE) {
                photo_path = null;
                if (data != null) {
                    image_layout.setVisibility(View.VISIBLE);
                    try {
                        photo_path = getPath(data.getData());
                        Toast.makeText(this, photo_path, Toast.LENGTH_LONG).show();

                        File imgFile = new File(photo_path);
                        if(imgFile.exists())
                        {


                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            ImageView imageView=findViewById(R.id.id_image);
                            imageView.setImageBitmap(myBitmap);

                            Toast.makeText(this, "I set", Toast.LENGTH_LONG).show();
                        }else {


                            Toast.makeText(this, "Cant", Toast.LENGTH_LONG).show();
                        }




//                        photo = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            } else if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {

                image_layout.setVisibility(View.VISIBLE);

                //photo = (Bitmap) data.getExtras().get("data");
//                id_image.setImageBitmap(photo);

//                encodedCameraImage = encodeImage(photo);


            }


        }
    }


}

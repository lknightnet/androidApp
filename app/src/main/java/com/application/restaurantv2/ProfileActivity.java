package com.application.restaurantv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress;
    private EditText edit_name, edit_phone, edit_password, edit_address;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);


        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);

        edit_name = findViewById(R.id.edit_name);
        edit_phone = findViewById(R.id.edit_phone);
        edit_password = findViewById(R.id.edit_password);
        edit_address = findViewById(R.id.edit_address);

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                String token = prefs.getString("access_token", null);
                if (token != null) {
                    changeInformationByAccessToken(token);
                } else {
                    Toast.makeText(ProfileActivity.this, "Токен верификации отсутствует", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token != null) {
            getInformationByAccessToken(token);
        }
    }

    public void getInformationByAccessToken(String token) {
        String url = "http://185.192.247.23:8080/api/user/get";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String name = response.getString("name");
                        String phone = response.getString("phone");
                        String address = response.getString("address");
                        String bonuses = response.getString("bonuses");

                        etName.setText(name);
                        etPhone.setText(phone);

                        if (!address.equals("null")) {
                            etAddress.setText(address);
                        }

                        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("bonuses", bonuses);
                        editor.putString("name", name);
                        editor.putString("phone", phone);
                        editor.putString("address", address);
                        editor.apply();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Ошибка обработки данных", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 404) {
                            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                            prefs.edit().clear().apply();
                            Toast.makeText(this, "Профиль не найден", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Ошибка: " + statusCode, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Сетевой сбой или нет ответа от сервера", Toast.LENGTH_SHORT).show();
                    }

                    error.printStackTrace();
//                    Toast.makeText(this, "Ошибка запроса", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    public void changeInformationByAccessToken(String token) {
        String url = "http://185.192.247.23:8080/api/user/change";

        RequestQueue queue = Volley.newRequestQueue(this);

        String name = edit_name.getText().toString().trim();
        String phone = edit_phone.getText().toString().trim();
        String password = edit_password.getText().toString().trim();
        String address = edit_address.getText().toString().trim();

        JSONObject jsonBody = new JSONObject();

        try {
            if (!name.isEmpty()) jsonBody.put("name", name);
            if (!phone.isEmpty()) jsonBody.put("phone", phone);
            if (!password.isEmpty()) jsonBody.put("password", password);
            if (!address.isEmpty()) jsonBody.put("address", address);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка формирования запроса", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Toast.makeText(this, "Данные успешно обновлены", Toast.LENGTH_SHORT).show();
                    SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    if (!name.isEmpty()) {
                        etName.setText(name);
                        editor.putString("name", name);
                    }
                    if (!phone.isEmpty()) {
                        etPhone.setText(phone);
                        editor.putString("phone", phone);
                    }
                    if (!address.isEmpty()) {
                        etAddress.setText(address);
                        editor.putString("address", address);
                    }
                    editor.apply();
                    edit_name.setText("");
                    edit_phone.setText("");
                    edit_password.setText("");
                    edit_address.setText("");
                },
                error -> {
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 404) {
                            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                            prefs.edit().clear().apply();
                            Toast.makeText(this, "Профиль не найден", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                        } else {
                            Toast.makeText(this, "Ошибка: " + statusCode, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Сетевой сбой или нет ответа от сервера", Toast.LENGTH_SHORT).show();
                    }

                    error.printStackTrace();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    public void setNewActivity(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else if (itemId == R.id.cart) {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.more) {
            Intent intent = new Intent(this, OtherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}

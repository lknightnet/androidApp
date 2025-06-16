package com.application.restaurantv2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);


        etLogin = findViewById(R.id.etLogin);
        setupPhoneMask(etLogin);



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textView = findViewById(R.id.tvRegister);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });


        EditText etLogin = findViewById(R.id.etLogin);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = etLogin.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (!login.isEmpty() && !password.isEmpty()) {
                    JSONObject jsonBody = new JSONObject();
                    try {
                        jsonBody.put("phone", login);
                        jsonBody.put("password", password);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Ошибка создания запроса", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String url = "http://185.192.247.23:8080/api/auth/signin"; // <-- замени на свой URL

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.POST, url, jsonBody,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String accessToken = response.getString("access_token");
                                        String refreshToken = response.getString("refresh_token");

                                        // Сохраняем токены в SharedPreferences
                                        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("access_token", accessToken);
                                        editor.putString("refresh_token", refreshToken);
                                        editor.apply();

                                        Toast.makeText(getApplicationContext(), "Успешный вход", Toast.LENGTH_SHORT).show();

                                        // Переход в главное окно или другое действие
                                        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);
                                        finish();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "Ошибка обработки ответа", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("LoginError", "Ошибка входа: " + error.toString());
                                    Toast.makeText(getApplicationContext(), "Ошибка входа", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );

                    // Добавляем запрос в очередь
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    requestQueue.add(jsonObjectRequest);

                } else {
                    Toast.makeText(getApplicationContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }


    private void setupPhoneMask(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            boolean isUpdating = false;
            String oldText = "";
            int cursorPosition = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isUpdating) {
                    oldText = s.toString();
                    cursorPosition = editText.getSelectionStart();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Не используем здесь логику, всё в afterTextChanged
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;

                isUpdating = true;

                String digits = s.toString().replaceAll("[^\\d]", "");

                if (digits.isEmpty()) {
                    // Если нет цифр — очистить поле
                    editText.setText("");
                    editText.setSelection(0);
                    isUpdating = false;
                    return;
                }

                // Ограничение длины
                if (digits.length() > 11) {
                    digits = digits.substring(0, 11);
                }

                // Формируем номер с обязательным "7" в начале
                if (!digits.startsWith("7")) {
                    digits = "7" + digits;
                }

                // Вторая цифра всегда 9, если длина > 1
                if (digits.length() > 1 && digits.charAt(1) != '9') {
                    digits = digits.substring(0, 1) + "9" + digits.substring(2);
                }

                // Форматирование
                StringBuilder formatted = new StringBuilder("+7 ");

                if (digits.length() > 1) {
                    formatted.append("(");
                    if (digits.length() >= 4) {
                        formatted.append(digits.substring(1, 4));
                    } else {
                        formatted.append(digits.substring(1));
                    }
                    formatted.append(") ");
                }

                if (digits.length() >= 7) {
                    formatted.append(digits.substring(4, 7));
                    formatted.append("-");
                } else if (digits.length() > 4) {
                    formatted.append(digits.substring(4));
                }

                if (digits.length() >= 9) {
                    formatted.append(digits.substring(7, 9));
                    formatted.append("-");
                } else if (digits.length() > 7) {
                    formatted.append(digits.substring(7));
                }

                if (digits.length() >= 11) {
                    formatted.append(digits.substring(9, 11));
                } else if (digits.length() > 9) {
                    formatted.append(digits.substring(9));
                }

                // Обновляем текст
                editText.setText(formatted.toString());

                // Ставим курсор в правильное место
                // Попытка вычислить курсор после изменений:
                int newCursorPosition = calculateCursorPosition(formatted.toString(), cursorPosition, oldText);
                if (newCursorPosition > formatted.length()) newCursorPosition = formatted.length();
                if (newCursorPosition < 0) newCursorPosition = 0;

                editText.setSelection(newCursorPosition);

                isUpdating = false;
            }

            private int calculateCursorPosition(String formattedText, int oldCursorPos, String oldText) {
                // Простая логика: если курсор был в конце, ставим в конец.
                if (oldCursorPos == oldText.length()) {
                    return formattedText.length();
                }
                // Иначе пытаемся сместить курсор с учётом добавленных символов
                int digitsBeforeCursor = 0;
                for (int i = 0; i < oldCursorPos; i++) {
                    if (Character.isDigit(oldText.charAt(i))) {
                        digitsBeforeCursor++;
                    }
                }

                int pos = 0;
                int digitsCounted = 0;
                while (pos < formattedText.length() && digitsCounted < digitsBeforeCursor) {
                    if (Character.isDigit(formattedText.charAt(pos))) {
                        digitsCounted++;
                    }
                    pos++;
                }
                return pos;
            }
        });
    }

}

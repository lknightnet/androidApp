package com.application.restaurantv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextWatcher;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

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


        setupPhoneMask(edit_phone);
        setupAddressMask(edit_address);

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


    private void setupAddressMask(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            boolean isUpdating = false;
            String oldText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isUpdating) {
                    oldText = s.toString();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;

                isUpdating = true;

                String input = s.toString();

                // Убираем префиксы и знаки препинания
                input = input.replaceAll("(?i)ул\\.?", "")
                        .replaceAll("(?i)д\\.?", "")
                        .replaceAll("(?i)кв\\.?", "")
                        .replaceAll("[^А-Яа-я0-9 ]", "")
                        .trim();

                // Разделяем на слова
                String[] parts = input.split("\\s+");

                String street = parts.length > 0 ? parts[0] : "";
                String house = parts.length > 1 ? parts[1] : "";
                String flat = parts.length > 2 ? parts[2] : "";

                // Ограничим длины
                street = street.length() > 15 ? street.substring(0, 15) : street;
                house = house.length() > 6 ? house.substring(0, 6) : house;
                flat = flat.length() > 6 ? flat.substring(0, 6) : flat;

                // Фильтруем дом и квартиру — только цифры
                house = house.replaceAll("\\D", "");
                flat = flat.replaceAll("\\D", "");

                // Первая буква улицы — заглавная
                street = capitalizeFirstLetter(street);

                // Формируем форматированный текст
                StringBuilder formatted = new StringBuilder();
                if (!street.isEmpty()) {
                    formatted.append("ул. ").append(street);
                }
                if (!house.isEmpty()) {
                    if (formatted.length() > 0) formatted.append(", ");
                    formatted.append("д. ").append(house);
                }
                if (!flat.isEmpty()) {
                    if (formatted.length() > 0) formatted.append(", ");
                    formatted.append("кв. ").append(flat);
                }

                String result = formatted.toString();

                // Обновляем текст
                if (!result.equals(oldText)) {
                    editText.setText(result);
                    editText.setSelection(result.length());
                }

                isUpdating = false;
            }

            private String capitalizeFirstLetter(String str) {
                if (str == null || str.isEmpty()) return "";
                return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
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


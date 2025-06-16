package com.application.restaurantv2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;

public class OrderActivity extends AppCompatActivity {
    private int bonuses;
    private RadioGroup paymentMethods;
    private String paymentMethod;
    private int checkedId;
    private EditText cardNumber;
    private EditText cardDate;
    private EditText cardCVV;
    private EditText inputComment;
    private Button btnDelivery;
    private String instrumentationQuantity;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order);


        cardNumber = findViewById(R.id.card_number);
        cardDate = findViewById(R.id.card_date);
        cardCVV = findViewById(R.id.card_cvv);
        inputComment = findViewById(R.id.input_comment);
        btnDelivery = findViewById(R.id.btn_delivery);
        instrumentationQuantity = getIntent().getStringExtra("instrumentation_quantity");



        EditText cardCVV = findViewById(R.id.card_cvv);
        InputFilter lengthFilter = new InputFilter.LengthFilter(3);
        InputFilter digitsFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        cardCVV.setFilters(new InputFilter[]{lengthFilter, digitsFilter});

        //
        EditText cardDate = findViewById(R.id.card_date);
        cardDate.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Добавляем маску **/**
        cardDate.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    isUpdating = false;
                    return;
                }

                String digitsOnly = s.toString().replaceAll("[^\\d]", "");

                if (digitsOnly.length() > 4) {
                    digitsOnly = digitsOnly.substring(0, 4);
                }

                String month = "";
                String year = "";

                if (digitsOnly.length() >= 1) {
                    month = digitsOnly.substring(0, Math.min(2, digitsOnly.length()));

                    // Проверяем месяц, если длина 2
                    if (month.length() == 2) {
                        int monthNum = Integer.parseInt(month);
                        if (monthNum == 0) {
                            month = "01";
                        } else if (monthNum > 12) {
                            month = "12";
                        }
                    }
                }

                if (digitsOnly.length() > 2) {
                    year = digitsOnly.substring(2);
                }

                StringBuilder formatted = new StringBuilder();
                formatted.append(month);
                if (digitsOnly.length() > 2) {
                    formatted.append('/');
                    formatted.append(year);
                }

                isUpdating = true;
                cardDate.setText(formatted.toString());
                cardDate.setSelection(formatted.length());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


        EditText cardNumber = findViewById(R.id.card_number);

        cardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private int previousLength;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                // Удаляем все символы, кроме цифр
                String digitsOnly = s.toString().replaceAll("[^\\d]", "");

                // Ограничиваем ввод до 16 символов
                if (digitsOnly.length() > 16) {
                    digitsOnly = digitsOnly.substring(0, 16);
                }

                // Добавляем пробелы после каждой четвёрки
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digitsOnly.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(digitsOnly.charAt(i));
                }

                // Обновляем поле
                cardNumber.removeTextChangedListener(this);
                cardNumber.setText(formatted.toString());
                cardNumber.setSelection(formatted.length());
                cardNumber.addTextChangedListener(this);

                isFormatting = false;
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.order), (v, insets) -> {
            Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            View bottomNav = findViewById(R.id.bottomNavigationView2);
            bottomNav.setPadding(0, 0, 0, navBarInsets.bottom); // добавляет отступ вниз
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        setUserinfo(prefs);

        String bonuses = prefs.getString("bonuses", null);
        EditText inputBonus = findViewById(R.id.input_bonus);
        setMaxBonusFilter(bonuses, inputBonus);

        String totalPrice = getIntent().getStringExtra("total_price");
        String instrumentationQuantity = getIntent().getStringExtra("instrumentation_quantity");
        setTotalPrice(totalPrice);

        Button btnDelivery = findViewById(R.id.btn_delivery);
        Button btnPickup = findViewById(R.id.btn_pickup);
        setBtnDesign(btnDelivery, btnPickup);

        setRadioGroup();

        TextView total_amount = findViewById(R.id.total_amount);
        Button btnApplyBonus = findViewById(R.id.btn_apply_bonus);
        setPriceReduction(btnApplyBonus, inputBonus, total_amount, totalPrice);
        btnBack();

        Button btn_pay = findViewById(R.id.btn_pay);
        paymentMethods = findViewById(R.id.payment_methods); // если это поле класса
        checkedId = paymentMethods.getCheckedRadioButtonId(); // если это поле класса
        paymentMethod = ""; // просто присваивание
        if (checkedId == R.id.radio_card) {
            paymentMethod = "card";
        } else if (checkedId == R.id.radio_cash) {
            paymentMethod = "cash";
        } else {
            paymentMethod = "";
        }
        EditText comment = findViewById(R.id.input_comment);

        btn_pay.setOnClickListener(v -> {
            checkedId = paymentMethods.getCheckedRadioButtonId(); // уже объявлены выше

            if (checkedId == R.id.radio_card) {
                String cardNumberStr = cardNumber.getText().toString().replaceAll("\\s", "");
                String cardDateStr = cardDate.getText().toString();
                String cardCVVStr = cardCVV.getText().toString();

                if (cardNumberStr.length() != 16) {
                    Drawable errorIcon = ContextCompat.getDrawable(this, R.drawable.ic_error_custom);
                    if (errorIcon != null) {
                        errorIcon.setBounds(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight());
                    }
                    cardNumber.setError("Введите номер карты", errorIcon);
                    cardNumber.requestFocus();
                    return;
                }
                if (!cardDateStr.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                    Drawable errorIcon = ContextCompat.getDrawable(this, R.drawable.ic_error_custom);
                    if (errorIcon != null) {
                        errorIcon.setBounds(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight());
                    }
                    cardDate.setError("Введите дату", errorIcon);
                    cardDate.requestFocus();
                    return;

                }
                if (cardCVVStr.length() != 3) {
                    Drawable errorIcon = ContextCompat.getDrawable(this, R.drawable.ic_error_custom);
                    if (errorIcon != null) {
                        errorIcon.setBounds(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight());
                    }
                    cardCVV.setError("Введите код ", errorIcon);
                    cardCVV.requestFocus();
                    return;
                }

            }

            paymentMethod = ""; // просто переиспользуем переменную

            if (checkedId == R.id.radio_card) {
                paymentMethod = "card";
            } else if (checkedId == R.id.radio_cash) {
                paymentMethod = "cash";
            }

            sendPostRequest(this, instrumentationQuantity, btnDelivery.isSelected(), paymentMethod, inputComment.getText().toString().trim());
        });
    }


    public void sendPostRequest(Context context, String instrumentationQuantity, boolean isDelivery, String paymentMethod, String comment) {
        String url = "http://185.192.247.23:8080/api/order/create";
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("instrumentation_quantity", Integer.parseInt(instrumentationQuantity));
            jsonBody.put("is_delivery", isDelivery);
            jsonBody.put("payment_method", paymentMethod);
            //jsonBody.put("city", "Уфа");
            jsonBody.put("bonuses", bonuses);
            jsonBody.put("comment", comment);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int orderId = 0;
                        try {
                            orderId = response.getInt("id");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        Toast.makeText(context, "Заказ успешно оформлен! ID: " + orderId, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(OrderActivity.this, OrderFinishActivity.class);
                        intent.putExtra("order_id", String.valueOf(orderId));
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Обработка ошибки
                        error.printStackTrace();
                        Toast.makeText(OrderActivity.this, "Ошибка загрузки корзины", Toast.LENGTH_SHORT).show();
                        Log.e("ERROR", String.valueOf(error));

                    }
                }
        ){            @Override
        public Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + token);
            return headers;
        }};

        requestQueue.add(jsonObjectRequest);
    }

    public void btnBack() {

        Button btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    public void setPriceReduction(Button btnApplyBonus, EditText inputBonus, TextView totalAmount, String totalPrice) {
        btnApplyBonus.setOnClickListener(v -> {
            // Получаем бонусы
            String bonusesStr = inputBonus.getText().toString().trim();
            int bonusesValue = 0;
            try {
                bonusesValue = Integer.parseInt(bonusesStr);
            } catch (NumberFormatException e) {
                bonusesValue = 0;
            }

            // Очищаем цену от символов (₽ и т.п.)
            String cleanTotalPrice = totalPrice.replaceAll("[^\\d]", "");
            int totalPriceValue = 0;
            try {
                totalPriceValue = Integer.parseInt(cleanTotalPrice);
            } catch (NumberFormatException e) {
                totalPriceValue = 0;
            }

            // Считаем и обновляем
            int reducedPrice = Math.max(totalPriceValue - bonusesValue, 0);
            totalAmount.setText(reducedPrice + " ₽");
            bonuses = bonusesValue;
        });
    }

    public void setMaxBonusFilter(String bonuses, EditText inputBonus) {
        int maxBonus = 0;
        try {
            maxBonus = Integer.parseInt(bonuses);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        int finalMaxBonus = maxBonus;
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                try {
                    String result = dest.subSequence(0, dstart)
                            + source.toString()
                            + dest.subSequence(dend, dest.length());
                    int input = Integer.parseInt(result);
                    if (input > 100 || input > finalMaxBonus) return "";
                } catch (NumberFormatException e) {
                    return "";
                }
                return null;
            }
        };
        inputBonus.setFilters(new InputFilter[]{filter});
    }

    public void setBtnDesign(Button btnDelivery, Button btnPickup) {
        btnDelivery.setSelected(true);
        btnPickup.setSelected(false);
        btnDelivery.setTextColor(Color.WHITE);
        btnPickup.setTextColor(Color.parseColor("#990033"));
        btnDelivery.setBackgroundColor(Color.parseColor("#990033"));
        btnPickup.setBackgroundColor(Color.WHITE);

        btnDelivery.setOnClickListener(v -> {
            btnDelivery.setSelected(true);
            btnPickup.setSelected(false);
            btnDelivery.setTextColor(Color.WHITE);
            btnPickup.setTextColor(Color.parseColor("#990033"));
            btnDelivery.setBackgroundColor(Color.parseColor("#990033"));
            btnPickup.setBackgroundColor(Color.WHITE);
        });

        btnPickup.setOnClickListener(v -> {
            btnPickup.setSelected(true);
            btnDelivery.setSelected(false);
            btnPickup.setTextColor(Color.WHITE);
            btnDelivery.setTextColor(Color.parseColor("#990033"));
            btnPickup.setBackgroundColor(Color.parseColor("#990033"));
            btnDelivery.setBackgroundColor(Color.WHITE);
        });
    }

    public void setRadioGroup() {
        RadioGroup paymentMethods = findViewById(R.id.payment_methods);
        EditText cardNumber = findViewById(R.id.card_number);
        EditText cardDate = findViewById(R.id.card_date);
        EditText cardCVV = findViewById(R.id.card_cvv);
        RadioButton radioCard = findViewById(R.id.radio_card);
        RadioButton radioCash = findViewById(R.id.radio_cash);

        cardNumber.setVisibility(View.VISIBLE);
        cardDate.setVisibility(View.VISIBLE);
        cardCVV.setVisibility(View.VISIBLE);

        paymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_card) {
                cardNumber.setVisibility(View.VISIBLE);
                cardDate.setVisibility(View.VISIBLE);
                cardCVV.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radio_cash) {
                cardNumber.setVisibility(View.GONE);
                cardDate.setVisibility(View.GONE);
                cardCVV.setVisibility(View.GONE);
            }
        });
    }

    public void setTotalPrice(String totalPrice) {
        if (totalPrice != null) {
            TextView total_amount = findViewById(R.id.total_amount);
            total_amount.setText(totalPrice);
        }
    }

    public void setUserinfo(SharedPreferences prefs) {
        String address = prefs.getString("address", null);
        String name = prefs.getString("name", null);
        String phone = prefs.getString("phone", null);
        if (address != null && !address.equals("null")) {
            TextView textView3 = findViewById(R.id.text_address);
            textView3.setText(address);
        } else {
            TextView textView3 = findViewById(R.id.text_address);
            textView3.setText("ул. Колотушкина 99/3");
        }
        if (name != null) {
            TextView textView3 = findViewById(R.id.text_name);
            Log.e("NAME1", name);
            textView3.setText(name);
        } else {
            TextView textView3 = findViewById(R.id.text_name);
            Log.e("NAME2", name);
            textView3.setText("Имя не указано");
        }
        if (phone != null) {
            TextView textView3 = findViewById(R.id.text_phone);
            Log.e("PHONE1", phone);
            textView3.setText(phone);
        } else {
            TextView textView3 = findViewById(R.id.text_phone);
            Log.e("PHONE2", phone);
            textView3.setText("Телефон не указан");
        }
    }

    public void setNewActivity(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else if (itemId == R.id.cart) {
            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
            String token = prefs.getString("access_token", null);
            if (token != null) {
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(OrderActivity.this, "Пройдите процедуру авторизации", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.more) {
            Intent intent = new Intent(this, OtherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}

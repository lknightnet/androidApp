package com.application.restaurantv2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.Spannable;
import android.graphics.Color;


public class OrderInformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_information);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.orderInformation), (v, insets) -> {
            Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            View bottomNav = findViewById(R.id.bottomNavigationView2);
            bottomNav.setPadding(0, 0, 0, navBarInsets.bottom); // добавляет отступ вниз
            return insets;
        });

        int orderId = getIntent().getIntExtra("order_id", -1);
        loadOrderInformation(this, orderId);

        Button btnGoToHome = findViewById(R.id.btnGoToMenu);
        setBtnOnMainActivity(btnGoToHome);
    }

    public void setBtnOnMainActivity(Button btnGoToHome) {
        btnGoToHome.setOnClickListener(v -> {
            Intent intent;
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    public void loadOrderInformation(Context context, int id) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String url = "http://185.192.247.23:8080/api/order/get/" + id;
        String token = prefs.getString("access_token", null);
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject orderObject = response.getJSONObject("order");
                        ViewOrderByIDList order = new ViewOrderByIDList();

                        // Парсим order_info
                        JSONObject info = orderObject.getJSONObject("order_info");
                        ViewOrderByID orderInfo = new ViewOrderByID();
                        orderInfo.id = info.getInt("id");
                        orderInfo.total_price = info.getDouble("total_price");
                        orderInfo.instrumentation_quantity = info.getInt("instrumentation_quantity");
                        orderInfo.address = info.isNull("address") ? null : info.getString("address");
                        orderInfo.user_phone = info.getString("user_phone");
                        orderInfo.city = info.getString("city");
                        orderInfo.status = info.getString("status");
                        orderInfo.is_delivery = info.getBoolean("is_delivery");
                        orderInfo.payment_method = info.getString("payment_method");
                        orderInfo.bonuses = info.getInt("bonuses");
                        orderInfo.comment = info.getString("comment");

                        order.order_info = orderInfo;

                        // Парсим order_product_list
                        JSONArray productsArray = orderObject.getJSONArray("order_product_list");
                        List<ViewOrderProductList> productList = new ArrayList<>();

                        for (int i = 0; i < productsArray.length(); i++) {
                            JSONObject productObj = productsArray.getJSONObject(i);
                            ViewOrderProductList product = new ViewOrderProductList();
                            product.price = productObj.getDouble("price");
                            product.quantity = productObj.getInt("quantity");
                            product.product_name = productObj.getString("product_name");
                            productList.add(product);
                        }

                        order.order_product_list = productList;

                        setInfo(order);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Обработка ошибки
                    error.printStackTrace();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }


    public void setInfo(ViewOrderByIDList order) {
        LinearLayout itemsContainer = findViewById(R.id.itemsContainer);
        for (ViewOrderProductList item : order.order_product_list) {
            TextView textView = new TextView(this);
            textView.setText(item.product_name + " : " + item.quantity);
            textView.setTextColor(Color.parseColor("#A10035"));
            textView.setTextSize(16);
            textView.setPadding(0, 4, 0, 4);
            itemsContainer.addView(textView);
        }

        TextView orderNumber = findViewById(R.id.orderNumber);
        Log.e("ORDER_INFO_ID", String.valueOf(order.order_info.id));

        String part1 = "Заказ ";
        String part2 = "#" + order.order_info.id;

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(part1);
        ssb.append(part2);

        // Цвет для "Заказ "
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#A10035")),
                0, part1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Цвет для номера заказа "#123456"
        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")),
                part1.length(), part1.length() + part2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        orderNumber.setText(ssb);

        TextView orderStatus = findViewById(R.id.orderStatus);
        orderStatus.setText(order.order_info.status);

        TextView orderCutlery = findViewById(R.id.orderCutlery);
        orderCutlery.setText("Приборы : " + order.order_info.instrumentation_quantity);


        //TextView orderCity = findViewById(R.id.orderCity);
        //orderCity.setText("Город : " + order.order_info.city);
        TextView orderAddress = findViewById(R.id.orderAddress);
        orderAddress.setText("Адрес : " + order.order_info.address);


        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String name = prefs.getString("name", null);
        TextView orderName = findViewById(R.id.orderName);
        orderName.setText("Имя : " + name);

        TextView orderPhone = findViewById(R.id.orderPhone);
        orderPhone.setText("Номер телефона : " + order.order_info.user_phone);

        TextView orderPayment = findViewById(R.id.orderPayment);
        if (order.order_info.payment_method.equals("card")) {
            orderPayment.setText("Способ оплаты : Банковская карта");
        } else if (order.order_info.payment_method.equals("cash")) {
            orderPayment.setText("Способ оплаты : Наличные");
        }

        TextView orderDelivery = findViewById(R.id.orderDelivery);
        if (order.order_info.is_delivery) {
            orderDelivery.setText("Способ получения : Доставка");
        } else if (!order.order_info.is_delivery) {
            orderDelivery.setText("Способ получения : Самовывоз");
        }

        TextView orderTotal = findViewById(R.id.orderTotal);
        double total = order.order_info.total_price - order.order_info.bonuses;
        orderTotal.setText("Сумма : " + String.valueOf(total) + " ₽");

        TextView orderComment = findViewById(R.id.orderComment);
        orderComment.setText("Комментарий к заказу : " + order.order_info.comment);

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
                Toast.makeText(OrderInformationActivity.this, "Пройдите процедуру авторизации", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.more) {
            Intent intent = new Intent(this, OtherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}

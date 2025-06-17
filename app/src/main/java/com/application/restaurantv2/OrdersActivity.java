package com.application.restaurantv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<OrderSmall> orderSmallList = new ArrayList<>();
    private OrdersAdapter adapter;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_orders);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.orders), (v, insets) -> {
            Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            View bottomNav = findViewById(R.id.bottomNavigationView2);
            bottomNav.setPadding(0, 0, 0, navBarInsets.bottom); // добавляет отступ вниз
            return insets;
        });

        recyclerView = findViewById(R.id.ordersRecyclerView);
        Button btnGoToHome = findViewById(R.id.btnGoToMenu);
        setBtnOnMainActivity(btnGoToHome);

        // Инициализируем RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Настраиваем LayoutManager для RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Создаем адаптер и ставим в RecyclerView
        adapter = new OrdersAdapter(this, orderSmallList);
        recyclerView.setAdapter(adapter);

        // Загружаем заказы с API
        loadOrdersFromApi();
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

    private void loadOrdersFromApi() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                "http://185.192.247.23:8080/api/order/list",
                null,
                response -> {
                    try {
                        orderSmallList.clear();

                        JSONArray orders = response.getJSONArray("order_list");
                        for (int i = 0; i < orders.length(); i++) {
                            JSONObject obj = orders.getJSONObject(i);

                            OrderSmall order = new OrderSmall(
                                    obj.getInt("id"),
                                    obj.getString("status")
                            );

                            orderSmallList.add(order);
                        }
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Ошибка обработки данных", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, ProductActivity.class);
                        startActivity(intent);
                    }
                },
                error -> {
                    error.printStackTrace();
                    String errorMsg = "Ошибка загрузки заказов";

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            JSONObject errorObj = new JSONObject(responseBody);
                            if (errorObj.has("error")) {
                                errorMsg = errorObj.getString("error");
                                Log.e("API_ERROR", "Ошибка с сервера: " + errorMsg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("API_ERROR", "Ошибка парсинга тела ошибки");
                        }
                    }

                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(request);
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
                Toast.makeText(OrdersActivity.this, "Пройдите процедуру авторизации", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.more) {
            Intent intent = new Intent(this, OtherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}

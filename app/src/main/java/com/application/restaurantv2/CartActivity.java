package com.application.restaurantv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView totalPriceText;
    private CartAdapter adapter;
    private int fixedQuantity = 1;
    private List<CartItem> cartItems = new ArrayList<>();
    private RequestQueue requestQueue;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cartRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceText = findViewById(R.id.totalPrice);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestQueue = Volley.newRequestQueue(this);

        adapter = new CartAdapter(this, cartItems);
        recyclerView.setAdapter(adapter);


        Button btnOpenMenu = findViewById(R.id.btnOpenMenu);
        btnOpenMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, ProductActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        Button btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                String address = prefs.getString("address", null);
                if (address != null && !address.equals("null")) {
                    Intent intent = new Intent(CartActivity.this, OrderActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("total_price", totalPriceText.getText());
                    intent.putExtra("instrumentation_quantity", String.valueOf(fixedQuantity));
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CartActivity.this, "Отсутствует адрес", Toast.LENGTH_SHORT).show();
                }


            }
        });

        loadCartFromApi();
    }

    public void updateFixedItemQuantity(int newQuantity) {
        fixedQuantity = newQuantity;
    }


    private void loadCartFromApi() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                "http://185.192.247.23:8080/api/cart/get",
                null,
                response -> {
                    try {
                        cartItems.clear();

                        int totalPrice = response.getInt("total_price");
                        totalPriceText.setText(totalPrice + " ₽");

                        JSONArray products = response.getJSONArray("product_list");
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject obj = products.getJSONObject(i);

                            CartItem item = new CartItem(
                                    obj.getInt("id"),
                                    obj.getString("name"),
                                    String.valueOf(obj.getInt("weight")),
                                    obj.getString("price"),
                                    obj.getInt("quantity"),
                                    "http://185.192.247.23:8080" + obj.getString("image_url")
                            );
                            cartItems.add(item);
                        }

                        cartItems.sort(new Comparator<CartItem>() {
                            @Override
                            public int compare(CartItem o1, CartItem o2) {
                                return Integer.compare(o1.getId(), o2.getId());
                            }
                        });
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
                    Toast.makeText(this, "Ошибка загрузки корзины", Toast.LENGTH_SHORT).show();
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

    public void sendAddToCartRequest(int productId) {
        String url = "http://185.192.247.23:8080/api/cart/plus";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("productID", productId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        cartItems.clear();

                        int totalPrice = response.getInt("total_price");
                        totalPriceText.setText(totalPrice + " ₽");

                        JSONArray products = response.getJSONArray("product_list");
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject obj = products.getJSONObject(i);

                            CartItem item = new CartItem(
                                    obj.getInt("id"),
                                    obj.getString("name"),
                                    String.valueOf(obj.getInt("weight")),
                                    obj.getString("price"),
                                    obj.getInt("quantity"),
                                    "http://185.192.247.23:8080" + obj.getString("image_url")
                            );
                            Log.e("ERROR", obj.getString("image_url"));
                            cartItems.add(item);
                        }

                        cartItems.sort(new Comparator<CartItem>() {
                            @Override
                            public int compare(CartItem o1, CartItem o2) {
                                return Integer.compare(o1.getId(), o2.getId());
                            }
                        });
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Ошибка обработки данных", Toast.LENGTH_SHORT).show();
                        Log.e("ERROR", e.toString());
                        Intent intent = new Intent(this, ProductActivity.class);
                        startActivity(intent);
                    }
                },
                error -> Toast.makeText(this, "Ошибка добавления в корзину", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    public void sendRemoveToCartRequest(int productId) {
        String url = "http://185.192.247.23:8080/api/cart/minus";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("productID", productId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        cartItems.clear();

                        int totalPrice = response.getInt("total_price");
                        totalPriceText.setText(totalPrice + " ₽");

                        JSONArray products = response.getJSONArray("product_list");
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject obj = products.getJSONObject(i);

                            CartItem item = new CartItem(
                                    obj.getInt("id"),
                                    obj.getString("name"),
                                    String.valueOf(obj.getInt("weight")),
                                    obj.getString("price"),
                                    obj.getInt("quantity"),
                                    "http://185.192.247.23:8080" + obj.getString("image_url")
                            );
                            Log.e("ERROR", obj.getString("image_url"));
                            cartItems.add(item);
                        }

                        cartItems.sort(new Comparator<CartItem>() {
                            @Override
                            public int compare(CartItem o1, CartItem o2) {
                                return Integer.compare(o1.getId(), o2.getId());
                            }
                        });
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Ошибка обработки данных", Toast.LENGTH_SHORT).show();
                        Log.e("ERROR", e.toString());
                        Intent intent = new Intent(this, ProductActivity.class);
                        startActivity(intent);
                    }
                },
                error -> Toast.makeText(this, "Ошибка удаления из корзины", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
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
                Toast.makeText(CartActivity.this, "Пройдите процедуру авторизации", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.more) {
            Intent intent = new Intent(this, OtherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

}

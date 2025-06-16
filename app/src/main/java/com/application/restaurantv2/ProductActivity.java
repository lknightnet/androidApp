package com.application.restaurantv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import androidx.appcompat.widget.SearchView;

public class ProductActivity extends AppCompatActivity implements OnProductClickListener {
    String[] countries = {"Уфа", "Новый Уренгой", "Санкт-Петербург", "Сеул"};
    int activeCategoryId;


    private List<ProductListItem> originalItems = new ArrayList<>();
    private CatalogAdapter adapter;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product);


        // Поиск
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterCatalog(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // При очистке поиска показываем все элементы с категориями
                    adapter.updateItems(originalItems);
                } else {
                    filterCatalog(newText);
                }
                return true;
            }
        });




        activeCategoryId = getIntent().getIntExtra("category_id", -1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.product), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        //сохранение выбранного города
        SharedPreferences cityPrefs = getSharedPreferences("city_pref", MODE_PRIVATE);
        String selectedCity = cityPrefs.getString("selected_city", "Уфа");
        int selectedIndex = Arrays.asList(countries).indexOf(selectedCity);
        if (selectedIndex >= 0) {
            spinner.setSelection(selectedIndex);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String city = countries[position];
                SharedPreferences.Editor editor = cityPrefs.edit();
                editor.putString("selected_city", city);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String bonuses = prefs.getString("bonuses", null);
//        String address = prefs.getString("address", null);
        if (bonuses != null) {
            Button btnPoints = findViewById(R.id.button2);
            btnPoints.setText(bonuses);
        } else {
            Button btnPoints = findViewById(R.id.button2);
            btnPoints.setText("0");
        }
//        if (address != null && !address.equals("null")) {
//            TextView textView3 = findViewById(R.id.textView3);
//            textView3.setText(address);
//        } else {
//            TextView textView3 = findViewById(R.id.textView3);
//            textView3.setText("ул. Колотушкина 99/3");
//        }

        HorizontalScrollView scrollView = findViewById(R.id.horizontalScrollView);
        View leftGradient = findViewById(R.id.left_gradient);
        View rightGradient = findViewById(R.id.right_gradient);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            boolean atStart = scrollView.getScrollX() == 0;
            boolean atEnd = scrollView.getChildAt(0).getRight() <=
                    scrollView.getWidth() + scrollView.getScrollX();
            leftGradient.setVisibility(atStart ? View.INVISIBLE : View.VISIBLE);
            rightGradient.setVisibility(atEnd ? View.INVISIBLE : View.VISIBLE);
        });

        Button button = findViewById(R.id.button_profile);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                String token = prefs.getString("access_token", null);
                Intent intent;
                if (token != null) {
                    intent = new Intent(ProductActivity.this, ProfileActivity.class);
                } else {
                    intent = new Intent(ProductActivity.this, LoginActivity.class);
                }
                startActivity(intent);
            }
        });

        loadCatalog();
    }

    private void loadCatalog() {
        String url = "http://185.192.247.23:8080/api/catalog/catalog"; // пример

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        parseCatalog(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }

    private void parseCatalog(JSONArray response) throws JSONException {
        List<ProductListItem> items = new ArrayList<>();
        List<String> categoryNames = new ArrayList<>();
        Map<Integer, Integer> categoryPositionMap = new HashMap<>();

        List<JSONObject> categoryList = new ArrayList<>();

        for (int i = 0; i < response.length(); i++) {
            categoryList.add(response.getJSONObject(i));
        }


        categoryList.sort(new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                try {
                    return Integer.compare(a.getInt("sort"), b.getInt("sort"));
                } catch (JSONException e) {
                    return 0;
                }
            }
        });

        for (JSONObject categoryObj : categoryList) {
            int categoryId = categoryObj.getInt("id");
            String categoryName = categoryObj.getString("name");
            categoryNames.add(categoryName);

            int position = items.size();
            categoryPositionMap.put(categoryId, position);

            // Добавляем заголовок категории
            items.add(new CategoryTitleItem(categoryName));

            // Добавляем продукты этой категории
            JSONArray productArray = categoryObj.getJSONArray("product_list");
            for (int j = 0; j < productArray.length(); j++) {
                JSONObject productObj = productArray.getJSONObject(j);
                ProductSmall product = new ProductSmall();
                product.setId(productObj.getInt("id"));
                product.setName(productObj.getString("name"));
                product.setWeight(productObj.getInt("weight") + " г");
                product.setPrice(String.valueOf(productObj.getInt("price")));
                product.setImageUrl("http://185.192.247.23:8080" + productObj.getString("image_url"));
                items.add(product);
            }
        }

        //RecyclerView recyclerView = findViewById(R.id.productRecyclerView);
        //CatalogAdapter adapter = new CatalogAdapter(this, items, this);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setAdapter(adapter);


        RecyclerView recyclerView = findViewById(R.id.productRecyclerView);

        originalItems.clear();
        originalItems.addAll(items);

        adapter = new CatalogAdapter(this, items, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);



        // Настройка TabLayout
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        for (String name : categoryNames) {
            tabLayout.addTab(tabLayout.newTab().setText(name));
        }

        if (activeCategoryId != -1 && categoryPositionMap.containsKey(activeCategoryId)) {
            int targetPosition = categoryPositionMap.get(activeCategoryId);
            recyclerView.scrollToPosition(targetPosition);

            // Найди и выбери нужный Tab
            String activeCategoryName = ((CategoryTitleItem) items.get(targetPosition)).getCategoryName();
            for (int t = 0; t < tabLayout.getTabCount(); t++) {
                if (tabLayout.getTabAt(t).getText().equals(activeCategoryName)) {
                    tabLayout.selectTab(tabLayout.getTabAt(t));
                    break;
                }
            }
        }

        // Обновление названия категории при скролле
//        TextView categoryText = findViewById(R.id.textView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                assert lm != null;
                int first = lm.findFirstVisibleItemPosition();
                for (int i = first; i >= 0; i--) {
                    if (items.get(i).getType() == ProductListItem.TYPE_CATEGORY) {
                        String name = ((CategoryTitleItem) items.get(i)).getCategoryName();
//                        categoryText.setText(name);
                        // Обнови активный таб
                        for (int t = 0; t < tabLayout.getTabCount(); t++) {
                            if (tabLayout.getTabAt(t).getText().equals(name)) {
                                tabLayout.selectTab(tabLayout.getTabAt(t));
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        });

        // Скролл к категории при выборе таба
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedCategory = tab.getText().toString();

                for (int i = 0; i < items.size(); i++) {
                    ProductListItem item = items.get(i);
                    if (item.getType() == ProductListItem.TYPE_CATEGORY &&
                            ((CategoryTitleItem) item).getCategoryName().equals(selectedCategory)) {

                        // Мгновенный скролл к позиции
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if (layoutManager != null) {
                            // Прокручиваем к позиции и выравниваем по верху
                            layoutManager.scrollToPositionWithOffset(i, 0);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public void onProductClick(ProductSmall product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.product_dialog, null);
        builder.setView(dialogView);
        ImageView productImage = dialogView.findViewById(R.id.productImageDialog);
        TextView titleView = dialogView.findViewById(R.id.productTitleDialog);
        TextView descView = dialogView.findViewById(R.id.productDescriptionDialog);
        TableLayout nutritionTable = dialogView.findViewById(R.id.productNutritionTableDialog);
        Button btnAdd = dialogView.findViewById(R.id.btn_add_to_cart);
        btnAdd.setOnClickListener(v -> {
            sendAddToCartRequest(product.getId());
        });

        loadProductByID(product.getId(), new ProductCallback() {
            @Override
            public void onSuccess(Product product) {
                Glide.with(ProductActivity.this)
                        .load("http://185.192.247.23:8080" + product.getImageUrl())
                        .circleCrop()
                        .into(productImage);
                titleView.setText(product.getName());
                descView.setText("заглушка");
                String[] nutrition = {product.getCalorie(), product.getSquirrels(), product.getFats(), product.getCarbohydrates()};
                addNutritionRows(nutritionTable, nutrition);

                btnAdd.setText(product.getPrice() + " ₽");

                AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNutritionRows(TableLayout table, String[] nutrition) {
        String[] labels = {"Калорийность", "Белки", "Жиры", "Углеводы"};

        for (int i = 0; i < labels.length; i++) {
            TableRow row = new TableRow(this);

            TextView label = new TextView(this);
            label.setText(labels[i]);
            label.setTextSize(15);
            row.addView(label);

            TextView dots = new TextView(this);
            //dots.setText("......");
            dots.setTextSize(15);
            row.addView(dots);

            TextView value = new TextView(this);
            value.setText(nutrition[i]);
            value.setTextSize(15);
            row.addView(value);

            table.addView(row);
        }
    }

    private void loadProductByID(int productID, ProductCallback callback) {
        String url = "http://185.192.247.23:8080/api/catalog/product/" + productID;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Product product = parseProduct(response);
                        callback.onSuccess(product);
                    } catch (JSONException e) {
                        callback.onError("Ошибка парсинга JSON");
                        e.printStackTrace();
                    }
                },
                error -> {
                    callback.onError("Ошибка загрузки данных");
                }
        );

        queue.add(request);
    }

    private Product parseProduct(JSONObject json) throws JSONException {
        int id = json.getInt("id");
        String name = json.getString("name");
        String imageUrl = json.getString("image_url");
        String calorie = json.getString("calorie");
        String weight = json.getString("weight");
        String price = json.getString("price");
        String squirrels = json.getString("squirrels");
        String fats = json.getString("fats");
        String carbohydrates = json.getString("carbohydrates");

        return new Product(id, name, weight, calorie, price, imageUrl, squirrels, fats, carbohydrates);
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
                Toast.makeText(ProductActivity.this, "Пройдите процедуру авторизации", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.more) {
            Intent intent = new Intent(this, OtherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
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
                response -> Toast.makeText(this, "Товар добавлен в корзину", Toast.LENGTH_SHORT).show(),
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



    private void filterCatalog(String query) {
        List<ProductListItem> filtered = new ArrayList<>();

        for (ProductListItem item : originalItems) {
            if (item.getType() == ProductListItem.TYPE_PRODUCT) {
                ProductSmall product = (ProductSmall) item;
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(product);
                }
            }
            // категории не добавляются при фильтрации
        }

        adapter.updateItems(filtered);
    }


}

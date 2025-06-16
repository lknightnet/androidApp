package com.application.restaurantv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.viewpager2.widget.ViewPager2;


public class MainActivity extends AppCompatActivity implements OnCategoryClickListener {

    private RecyclerView recyclerView;
    private CategoryMainAdapter categoryAdapter;
    private List<Category> categoryList;
    private final String[] city = {"Уфа", "Новый Уренгой", "Санкт-Петербург", "Сеул"};

    private ViewPager2 bannerViewPager;
    private int[] bannerImages = {R.drawable.stocks, R.drawable.stocks2};
    private Handler bannerHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //imageViewBanner = findViewById(R.id.imageView5);

// Слайд-шоу баннеров
        bannerViewPager = findViewById(R.id.bannerViewPager);
        BannerAdapter bannerAdapter = new BannerAdapter(bannerImages);
        bannerViewPager.setAdapter(bannerAdapter);

// Автопрокрутка
        Runnable bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = (bannerViewPager.getCurrentItem() + 1) % bannerImages.length;
                bannerViewPager.setCurrentItem(nextItem, true);
                bannerHandler.postDelayed(this, 2000); // каждые 7 секунды
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 2000);

        //HorizontalScrollView scrollView = findViewById(R.id.horizontalImageScrollView);
        //View leftGradient = findViewById(R.id.left_gradient);
        //View rightGradient = findViewById(R.id.right_gradient);
        //ViewTreeObserver.OnScrollChangedListener listener = new ViewTreeObserver.OnScrollChangedListener() {
        //    @Override
        //    public void onScrollChanged() {
        //        updateGradientsVisibility(scrollView, leftGradient, rightGradient);
        //    }
        //};
        //scrollView.getViewTreeObserver().addOnScrollChangedListener(listener);

            // Вызови обновление сразу после layout завершен:
        //scrollView.post(new Runnable() {
        //    @Override
        //    public void run() {
        //        updateGradientsVisibility(scrollView, leftGradient, rightGradient);
        //    }
        //});



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String bonuses = prefs.getString("bonuses", null);
        String address = prefs.getString("address", null);
        if (bonuses != null){
            Button btnPoints = findViewById(R.id.profile_points);
            btnPoints.setText(bonuses);
        } else {
            Button btnPoints = findViewById(R.id.profile_points);
            btnPoints.setText("0");
        }
        if (address != null && !address.equals("null")){
            TextView textView3 = findViewById(R.id.textView3);
            textView3.setText(address);
        } else {
            TextView textView3 = findViewById(R.id.textView3);
            textView3.setText("ул. Колотушкина 99/3");
        }

        Button button = findViewById(R.id.btn_profile);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                String token = prefs.getString("access_token", null);
                Intent intent;
                if (token != null){
                    intent = new Intent(MainActivity.this, ProfileActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                }
                startActivity(intent);
            }
        });

        // Spinner (выбор города)
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, city);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Восстановление сохранённого города
        SharedPreferences cityPrefs = getSharedPreferences("city_pref", MODE_PRIVATE);
        String savedCity = cityPrefs.getString("selected_city", "Уфа"); // по умолчанию "Уфа"
        int selectedIndex = Arrays.asList(city).indexOf(savedCity);
        if (selectedIndex >= 0) {
            spinner.setSelection(selectedIndex);
        }

        // Слушатель выбора города
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = city[position];

                // Сохраняем выбранный город
                SharedPreferences.Editor editor = getSharedPreferences("city_pref", MODE_PRIVATE).edit();
                editor.putString("selected_city", selectedCity);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // ничего не делаем
            }
        });

        // RecyclerView
        recyclerView = findViewById(R.id.categoryRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 колонки

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryMainAdapter(this, categoryList, this);
        recyclerView.setAdapter(categoryAdapter);

        // Загрузка категорий из API
        loadCategories();
    }

    //private void updateGradientsVisibility(HorizontalScrollView scrollView, View leftGradient, View rightGradient) {
    //    boolean canScrollLeft = scrollView.getScrollX() > 0;
    //    boolean canScrollRight = scrollView.getChildAt(0).getRight() > (scrollView.getScrollX() + scrollView.getWidth());

    //    leftGradient.setVisibility(canScrollLeft ? View.VISIBLE : View.INVISIBLE);
    //    rightGradient.setVisibility(canScrollRight ? View.VISIBLE : View.INVISIBLE);
    //}

    private void loadCategories() {
        String url = "http://185.192.247.23:8080/api/catalog/categories";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> parseCategories(response),
                error -> Log.e("VolleyError", "Ошибка при загрузке категорий", error)
        );

        queue.add(request);
    }

    private void parseCategories(JSONArray response) {
        try {
            categoryList.clear();
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                String name = obj.getString("name");
                String imageUrl = "http://185.192.247.23:8080" + obj.getString("imageUrl");
                int id = obj.getInt("id");
                int sort = obj.getInt("sort");

                Category category = new Category(id, name, imageUrl, sort);
                categoryList.add(category);
            }

            categoryList.sort(new Comparator<Category>() {
                @Override
                public int compare(Category c1, Category c2) {
                    return Integer.compare(c1.getSort(), c2.getSort());
                }
            });

            categoryAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка обработки данных", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onCategoryClick(Category category) {
        Intent intent;
        intent = new Intent(this, ProductActivity.class);
        intent.putExtra("category_name", category.getName());
        intent.putExtra("category_id", category.getId());
        startActivity(intent);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        bannerHandler.removeCallbacksAndMessages(null);
    }



}
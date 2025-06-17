package com.application.restaurantv2;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private int id;
    private String name;
    private String imageUrl;
    private int sort;

    private List<ProductSmall> products = new ArrayList<>();;

    public Category(int id, String name, String imageUrl, int sort) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.sort = sort;
    }

    public Category() {
        products = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public int getSort() {
        return sort;
    }
    public void setSort(int sort) {
        this.sort = sort;
    }
    public List<ProductSmall> getProducts() {
        return products;
    }
    public void setProducts(List<ProductSmall> products) {
        this.products = products;
    }
}

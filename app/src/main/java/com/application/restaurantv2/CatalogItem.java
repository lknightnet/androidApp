package com.application.restaurantv2;

public class CatalogItem {
    public static final int TYPE_CATEGORY_TITLE = 0;
    public static final int TYPE_PRODUCT = 1;

    int type;
    String categoryName; // если type == CATEGORY_TITLE
    ProductSmall product; // если type == PRODUCT

    public CatalogItem(int type, String categoryName) {
        this.type = type;
        this.categoryName = categoryName;
    }

    public CatalogItem(int type, ProductSmall product) {
        this.type = type;
        this.product = product;
    }
}

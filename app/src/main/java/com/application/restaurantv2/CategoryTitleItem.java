package com.application.restaurantv2;

public class CategoryTitleItem implements ProductListItem {
    private String categoryName;

    public CategoryTitleItem(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public int getType() {
        return TYPE_CATEGORY;
    }
}

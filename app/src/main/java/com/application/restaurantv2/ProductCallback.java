package com.application.restaurantv2;

public interface ProductCallback {
    void onSuccess(Product product);
    void onError(String errorMessage);
}

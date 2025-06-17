package com.application.restaurantv2;

public class CartItem {

    private int id;
    private String name;
    private String weight;
    private String price;
    private int quantity;
    private String imageUrl;

    public CartItem(int id, String name, String weight, String price, int quantity, String imageUrl) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public String getWeight() { return weight; }
    public String getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

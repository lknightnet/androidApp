package com.application.restaurantv2;

public class Product {
    private int id;
    private String name;
    private String price;
    private String weight;
    private String calorie;
    private String imageUrl;

    private String squirrels;

    private String fats;

    private String carbohydrates;

    public Product(int id, String name, String weight,String calorie, String price, String imageUrl, String squirrels, String fats, String carbohydrates) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.price = price;
        this.imageUrl = imageUrl;
        this.squirrels = squirrels;
        this.fats = fats;
        this.carbohydrates = carbohydrates;
        this.calorie = calorie;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSquirrels() {
        return squirrels;
    }

    public void setSquirrels(String squirrels) {
        this.squirrels = squirrels;
    }

    public String getFats() {
        return fats;
    }

    public void setFats(String fats) {
        this.fats = fats;
    }

    public String getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(String carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public String getCalorie() {
        return calorie;
    }

    public void setCalorie(String calorie) {
        this.calorie = calorie;
    }
}

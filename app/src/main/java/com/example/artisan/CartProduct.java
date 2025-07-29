package com.example.artisan;

public class CartProduct {
    private String name;
    private Double price;
    private String imageUrl;
    private String id;

    public CartProduct(String name, Double price, String imageUrl, String id) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.id=id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getId() {
        return id;
    }
}

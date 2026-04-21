package com.shopping.model;

import java.io.Serializable;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private String name;
    private String category;
    private double price;
    private int stock;
    private String description;

    public Product(String productId, String name, String category, double price, int stock, String description) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.description = description;
    }

    // Getters
    public String getProductId()   { return productId; }
    public String getName()        { return name; }
    public String getCategory()    { return category; }
    public double getPrice()       { return price; }
    public int getStock()          { return stock; }
    public String getDescription() { return description; }

    // Setters
    public void setName(String name)           { this.name = name; }
    public void setCategory(String category)   { this.category = category; }
    public void setPrice(double price)         { this.price = price; }
    public void setStock(int stock)            { this.stock = stock; }
    public void setDescription(String desc)    { this.description = desc; }

    public void reduceStock(int qty) { this.stock -= qty; }
    public void increaseStock(int qty) { this.stock += qty; }

    @Override
    public String toString() {
        return String.format("%-10s %-25s %-15s ₹%-10.2f Stock: %-5d %s",
                productId, name, category, price, stock, description);
    }

    public String toTableRow() {
        return String.format("| %-8s | %-24s | %-14s | ₹%-9.2f | %-5d |",
                productId, name, category, price, stock);
    }
}

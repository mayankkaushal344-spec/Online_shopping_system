package com.shopping.model;

import com.shopping.exception.InsufficientStockException;
import com.shopping.exception.ProductNotFoundException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private List<CartItem> items;
    private double discountPercent;
    private String appliedCoupon;

    public Cart(String userId) {
        this.userId          = userId;
        this.items           = new ArrayList<>();
        this.discountPercent = 0.0;
        this.appliedCoupon   = null;
    }

    public void addItem(Product product, int quantity) throws InsufficientStockException {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");

        Optional<CartItem> existing = items.stream()
                .filter(i -> i.getProduct().getProductId().equals(product.getProductId()))
                .findFirst();

        int currentInCart = existing.map(CartItem::getQuantity).orElse(0);
        int totalNeeded   = currentInCart + quantity;

        if (totalNeeded > product.getStock()) {
            throw new InsufficientStockException(
                "Only " + product.getStock() + " units available for '" + product.getName() + "'.");
        }

        if (existing.isPresent()) {
            existing.get().setQuantity(totalNeeded);
        } else {
            items.add(new CartItem(product, quantity));
        }
    }

    public void removeItem(String productId) throws ProductNotFoundException {
        boolean removed = items.removeIf(i -> i.getProduct().getProductId().equals(productId));
        if (!removed) throw new ProductNotFoundException("Product not found in cart: " + productId);
    }

    public void updateQuantity(String productId, int newQty) throws ProductNotFoundException, InsufficientStockException {
        CartItem item = items.stream()
                .filter(i -> i.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product not in cart: " + productId));

        if (newQty <= 0) {
            removeItem(productId);
        } else if (newQty > item.getProduct().getStock()) {
            throw new InsufficientStockException(
                "Only " + item.getProduct().getStock() + " units available.");
        } else {
            item.setQuantity(newQty);
        }
    }

    public void clear() {
        items.clear();
        discountPercent = 0.0;
        appliedCoupon   = null;
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public double getDiscountAmount() {
        return getSubtotal() * discountPercent / 100.0;
    }

    public double getTotal() {
        return getSubtotal() - getDiscountAmount();
    }

    public void applyDiscount(double percent, String coupon) {
        this.discountPercent = percent;
        this.appliedCoupon   = coupon;
    }

    public void removeDiscount() {
        this.discountPercent = 0.0;
        this.appliedCoupon   = null;
    }

    public boolean isEmpty() { return items.isEmpty(); }

    public List<CartItem> getItems()       { return items; }
    public String getUserId()              { return userId; }
    public double getDiscountPercent()     { return discountPercent; }
    public String getAppliedCoupon()       { return appliedCoupon; }
    public int getTotalItemCount()         { return items.stream().mapToInt(CartItem::getQuantity).sum(); }
}

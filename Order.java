package com.shopping.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { PLACED, PROCESSING, SHIPPED, DELIVERED, CANCELLED }

    private String orderId;
    private String userId;
    private String username;
    private List<CartItem> items;
    private double subtotal;
    private double discountAmount;
    private double totalAmount;
    private String paymentMethod;
    private Status status;
    private LocalDateTime orderTime;
    private String deliveryAddress;
    private String couponApplied;

    public Order(String orderId, String userId, String username, List<CartItem> items,
                 double subtotal, double discountAmount, double total,
                 String paymentMethod, String deliveryAddress, String couponApplied) {
        this.orderId         = orderId;
        this.userId          = userId;
        this.username        = username;
        this.items           = new ArrayList<>(items);
        this.subtotal        = subtotal;
        this.discountAmount  = discountAmount;
        this.totalAmount     = total;
        this.paymentMethod   = paymentMethod;
        this.status          = Status.PLACED;
        this.orderTime       = LocalDateTime.now();
        this.deliveryAddress = deliveryAddress;
        this.couponApplied   = couponApplied;
    }

    // Getters
    public String getOrderId()        { return orderId; }
    public String getUserId()         { return userId; }
    public String getUsername()       { return username; }
    public List<CartItem> getItems()  { return items; }
    public double getSubtotal()       { return subtotal; }
    public double getDiscountAmount() { return discountAmount; }
    public double getTotalAmount()    { return totalAmount; }
    public String getPaymentMethod()  { return paymentMethod; }
    public Status getStatus()         { return status; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public String getDeliveryAddress(){ return deliveryAddress; }
    public String getCouponApplied()  { return couponApplied; }

    public void setStatus(Status status) { this.status = status; }

    public String getFormattedTime() {
        return orderTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    public void printReceipt() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              ORDER RECEIPT / CONFIRMATION            ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.printf("║  Order ID   : %-37s║%n", orderId);
        System.out.printf("║  Customer   : %-37s║%n", username);
        System.out.printf("║  Date/Time  : %-37s║%n", getFormattedTime());
        System.out.printf("║  Payment    : %-37s║%n", paymentMethod);
        System.out.printf("║  Status     : %-37s║%n", status);
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  ITEMS ORDERED:                                      ║");
        System.out.println("║  ─────────────────────────────────────────────────  ║");
        for (CartItem item : items) {
            System.out.printf("║  %-24s x%-3d  ₹%-12.2f║%n",
                    item.getProduct().getName(), item.getQuantity(), item.getSubtotal());
        }
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.printf("║  Subtotal   :                         ₹%-12.2f║%n", subtotal);
        if (discountAmount > 0) {
            System.out.printf("║  Coupon     : %-15s Discount: -₹%-10.2f║%n", couponApplied, discountAmount);
        }
        System.out.printf("║  TOTAL PAID :                         ₹%-12.2f║%n", totalAmount);
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.printf("║  Deliver To : %-37s║%n",
                deliveryAddress.length() > 37 ? deliveryAddress.substring(0, 34) + "..." : deliveryAddress);
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    @Override
    public String toString() {
        return String.format("Order[%s | %s | ₹%.2f | %s | %s]",
                orderId, username, totalAmount, status, getFormattedTime());
    }
}

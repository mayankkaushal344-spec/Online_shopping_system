package com.shopping.ui;

import com.shopping.exception.AuthenticationException;
import com.shopping.exception.InsufficientStockException;
import com.shopping.exception.InvalidCouponException;
import com.shopping.exception.ProductNotFoundException;
import com.shopping.model.*;
import com.shopping.payment.*;
import com.shopping.service.OrderService;
import com.shopping.service.ProductService;
import com.shopping.service.UserService;
import com.shopping.util.ConsoleHelper;
import com.shopping.util.CouponManager;

import java.util.List;
import java.util.Scanner;

public class UserMenu {
    private final ProductService productService;
    private final UserService    userService;
    private final OrderService   orderService;
    private final Scanner        scanner;

    private User currentUser;
    private Cart cart;

    public UserMenu(ProductService ps, UserService us, OrderService os, Scanner sc) {
        this.productService = ps;
        this.userService    = us;
        this.orderService   = os;
        this.scanner        = sc;
    }

    // ── Auth ─────────────────────────────────────────────────────────────────

    public boolean showAuthMenu() {
        ConsoleHelper.printHeader("USER PORTAL");
        ConsoleHelper.printMenuOption(1, "Login");
        ConsoleHelper.printMenuOption(2, "Register New Account");
        ConsoleHelper.printMenuOption(0, "Back to Main Menu");
        ConsoleHelper.printDivider();
        System.out.print("  Choice: ");
        return switch (scanner.nextLine().trim()) {
            case "1" -> login();
            case "2" -> register();
            default  -> false;
        };
    }

    private boolean login() {
        ConsoleHelper.printHeader("LOGIN");
        System.out.print("  Username : "); String u = scanner.nextLine().trim();
        System.out.print("  Password : "); String p = scanner.nextLine().trim();
        try {
            currentUser = userService.login(u, p);
            cart        = new Cart(currentUser.getUserId());
            ConsoleHelper.printSuccess("Welcome back, " + currentUser.getUsername() + "!");
            return true;
        } catch (AuthenticationException e) {
            ConsoleHelper.printError(e.getMessage());
            return false;
        }
    }

    private boolean register() {
        ConsoleHelper.printHeader("CREATE ACCOUNT");
        try {
            System.out.print("  Username    : "); String username = scanner.nextLine().trim();
            System.out.print("  Password    : "); String password = scanner.nextLine().trim();
            System.out.print("  Email       : "); String email    = scanner.nextLine().trim();
            System.out.print("  Phone       : "); String phone    = scanner.nextLine().trim();
            System.out.print("  Address     : "); String address  = scanner.nextLine().trim();

            currentUser = userService.register(username, password, email, phone, address);
            cart        = new Cart(currentUser.getUserId());
            ConsoleHelper.printSuccess("Account created! Welcome, " + currentUser.getUsername() + "!");
            return true;
        } catch (AuthenticationException e) {
            ConsoleHelper.printError(e.getMessage());
            return false;
        }
    }

    // ── Main shopping menu ───────────────────────────────────────────────────

    public void showShoppingMenu() {
        boolean running = true;
        while (running) {
            ConsoleHelper.printHeader("SHOPPING MENU  ─  " + currentUser.getUsername());
            System.out.println("  BROWSE");
            ConsoleHelper.printMenuOption(1, "Browse All Products");
            ConsoleHelper.printMenuOption(2, "Browse by Category");
            ConsoleHelper.printMenuOption(3, "Search Products");
            System.out.println("\n  CART");
            ConsoleHelper.printMenuOption(4, "View Cart  (" + cart.getTotalItemCount() + " items)");
            ConsoleHelper.printMenuOption(5, "Add Product to Cart");
            ConsoleHelper.printMenuOption(6, "Remove Item from Cart");
            ConsoleHelper.printMenuOption(7, "Update Item Quantity");
            ConsoleHelper.printMenuOption(8, "Apply Coupon Code");
            System.out.println("\n  CHECKOUT");
            ConsoleHelper.printMenuOption(9, "Checkout & Place Order");
            System.out.println("\n  ACCOUNT");
            ConsoleHelper.printMenuOption(10, "View My Orders");
            ConsoleHelper.printMenuOption(11, "My Profile");
            ConsoleHelper.printMenuOption(0,  "Logout");
            ConsoleHelper.printDivider();
            System.out.print("  Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1"  -> browseAllProducts();
                case "2"  -> browseByCategory();
                case "3"  -> searchProducts();
                case "4"  -> viewCart();
                case "5"  -> addToCart();
                case "6"  -> removeFromCart();
                case "7"  -> updateCartQuantity();
                case "8"  -> applyCoupon();
                case "9"  -> checkout();
                case "10" -> viewMyOrders();
                case "11" -> viewProfile();
                case "0"  -> { running = false; currentUser = null; cart = null; }
                default   -> ConsoleHelper.printError("Invalid option.");
            }
        }
    }

    // ── Browse ───────────────────────────────────────────────────────────────

    private void browseAllProducts() {
        ConsoleHelper.printHeader("ALL PRODUCTS");
        List<Product> all = productService.getAllProducts();
        all.sort((a, b) -> a.getCategory().compareToIgnoreCase(b.getCategory()));
        productService.printProductTable(all);
        System.out.printf("  %d products available. Use option 5 to add to cart.%n", all.size());
    }

    private void browseByCategory() {
        ConsoleHelper.printHeader("BROWSE BY CATEGORY");
        List<String> cats = productService.getCategories();
        for (int i = 0; i < cats.size(); i++)
            ConsoleHelper.printMenuOption(i + 1, cats.get(i));
        System.out.print("  Select category number: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= cats.size()) { ConsoleHelper.printError("Invalid selection."); return; }
            String cat = cats.get(idx);
            ConsoleHelper.printHeader(cat.toUpperCase());
            productService.printProductTable(productService.getByCategory(cat));
        } catch (NumberFormatException e) {
            ConsoleHelper.printError("Please enter a valid number.");
        }
    }

    private void searchProducts() {
        ConsoleHelper.printHeader("SEARCH PRODUCTS");
        System.out.print("  Enter search query: ");
        String q = scanner.nextLine().trim();
        if (q.isBlank()) { ConsoleHelper.printError("Search query cannot be empty."); return; }
        List<Product> results = productService.search(q);
        ConsoleHelper.printInfo("Found " + results.size() + " result(s) for \"" + q + "\":");
        productService.printProductTable(results);
    }

    // ── Cart ─────────────────────────────────────────────────────────────────

    private void viewCart() {
        ConsoleHelper.printHeader("YOUR CART");
        if (cart.isEmpty()) {
            ConsoleHelper.printInfo("Your cart is empty. Browse products and add some items!");
            return;
        }
        System.out.println("  ┌──────────────────────────┬──────────┬────────────┬─────────────┐");
        System.out.println("  │ Product                  │ Quantity │ Unit Price │ Subtotal    │");
        System.out.println("  ├──────────────────────────┼──────────┼────────────┼─────────────┤");
        for (CartItem item : cart.getItems())
            System.out.println("  " + item);
        System.out.println("  └──────────────────────────┴──────────┴────────────┴─────────────┘");
        ConsoleHelper.printDivider();
        System.out.printf("  Subtotal    :  ₹%.2f%n", cart.getSubtotal());
        if (cart.getAppliedCoupon() != null) {
            System.out.printf("  Coupon      :  %s (%.0f%% OFF) → -₹%.2f%n",
                    cart.getAppliedCoupon(), cart.getDiscountPercent(), cart.getDiscountAmount());
        }
        System.out.printf("  TOTAL       :  ₹%.2f%n", cart.getTotal());
        ConsoleHelper.printDivider();
    }

    private void addToCart() {
        ConsoleHelper.printHeader("ADD TO CART");
        System.out.print("  Enter Product ID: ");
        String id = scanner.nextLine().trim();
        try {
            Product p = productService.getById(id);
            System.out.printf("  %s  ─  ₹%.2f  (In stock: %d)%n", p.getName(), p.getPrice(), p.getStock());
            System.out.print("  Quantity: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());
            cart.addItem(p, qty);
            ConsoleHelper.printSuccess(qty + "x " + p.getName() + " added to cart.");
        } catch (ProductNotFoundException e) {
            ConsoleHelper.printError(e.getMessage());
        } catch (InsufficientStockException e) {
            ConsoleHelper.printError("Stock error: " + e.getMessage());
        } catch (NumberFormatException e) {
            ConsoleHelper.printError("Invalid quantity.");
        } catch (IllegalArgumentException e) {
            ConsoleHelper.printError(e.getMessage());
        }
    }

    private void removeFromCart() {
        ConsoleHelper.printHeader("REMOVE FROM CART");
        viewCart();
        if (cart.isEmpty()) return;
        System.out.print("  Enter Product ID to remove: ");
        String id = scanner.nextLine().trim();
        try {
            cart.removeItem(id);
            ConsoleHelper.printSuccess("Item removed from cart.");
        } catch (ProductNotFoundException e) {
            ConsoleHelper.printError(e.getMessage());
        }
    }

    private void updateCartQuantity() {
        ConsoleHelper.printHeader("UPDATE QUANTITY");
        viewCart();
        if (cart.isEmpty()) return;
        System.out.print("  Enter Product ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("  New quantity (0 to remove): ");
        try {
            int qty = Integer.parseInt(scanner.nextLine().trim());
            cart.updateQuantity(id, qty);
            ConsoleHelper.printSuccess("Cart updated.");
        } catch (ProductNotFoundException e) {
            ConsoleHelper.printError(e.getMessage());
        } catch (InsufficientStockException e) {
            ConsoleHelper.printError(e.getMessage());
        } catch (NumberFormatException e) {
            ConsoleHelper.printError("Invalid number.");
        }
    }

    private void applyCoupon() {
        ConsoleHelper.printHeader("APPLY COUPON");
        CouponManager.listCoupons();
        if (cart.getAppliedCoupon() != null) {
            ConsoleHelper.printInfo("Currently applied: " + cart.getAppliedCoupon()
                    + " (" + cart.getDiscountPercent() + "% OFF)");
            System.out.print("  Enter new coupon (or press Enter to remove): ");
            String code = scanner.nextLine().trim();
            if (code.isBlank()) {
                cart.removeDiscount();
                ConsoleHelper.printSuccess("Coupon removed.");
                return;
            }
            applyCode(code);
        } else {
            System.out.print("  Enter coupon code: ");
            String code = scanner.nextLine().trim();
            applyCode(code);
        }
    }

    private void applyCode(String code) {
        try {
            double pct = CouponManager.validateAndGetDiscount(code);
            cart.applyDiscount(pct, code.toUpperCase().trim());
            ConsoleHelper.printSuccess("Coupon '" + code.toUpperCase().trim() + "' applied! "
                    + (int) pct + "% discount → Saving ₹" + String.format("%.2f", cart.getDiscountAmount()));
        } catch (InvalidCouponException e) {
            ConsoleHelper.printError(e.getMessage());
        }
    }

    // ── Checkout ─────────────────────────────────────────────────────────────

    private void checkout() {
        ConsoleHelper.printHeader("CHECKOUT");
        if (cart.isEmpty()) {
            ConsoleHelper.printError("Cart is empty. Add products before checkout.");
            return;
        }
        viewCart();

        // Confirm delivery address
        System.out.println("  Delivery Address on file: " + currentUser.getAddress());
        System.out.print("  Use this address? (yes/no): ");
        String useAddr = scanner.nextLine().trim();
        String deliveryAddress;
        if (useAddr.equalsIgnoreCase("yes")) {
            deliveryAddress = currentUser.getAddress();
        } else {
            System.out.print("  Enter delivery address: ");
            deliveryAddress = scanner.nextLine().trim();
            if (deliveryAddress.isBlank()) deliveryAddress = currentUser.getAddress();
        }

        // Validate stock for all cart items before proceeding
        for (CartItem item : cart.getItems()) {
            if (item.getQuantity() > item.getProduct().getStock()) {
                ConsoleHelper.printError("Insufficient stock for '" + item.getProduct().getName()
                        + "'. Only " + item.getProduct().getStock() + " left.");
                return;
            }
        }

        // Payment selection
        ConsoleHelper.printHeader("SELECT PAYMENT METHOD");
        ConsoleHelper.printMenuOption(1, "Credit / Debit Card");
        ConsoleHelper.printMenuOption(2, "UPI");
        ConsoleHelper.printMenuOption(3, "Cash on Delivery");
        ConsoleHelper.printMenuOption(4, "Net Banking");
        ConsoleHelper.printMenuOption(0, "Cancel Checkout");
        System.out.print("  Choice: ");
        String pmChoice = scanner.nextLine().trim();

        PaymentMethod payment;
        try {
            payment = switch (pmChoice) {
                case "1" -> new CreditCardPayment(scanner);
                case "2" -> new UPIPayment(scanner);
                case "3" -> new CashOnDeliveryPayment();
                case "4" -> new NetBankingPayment(scanner);
                case "0" -> { ConsoleHelper.printInfo("Checkout cancelled."); yield null; }
                default  -> { ConsoleHelper.printError("Invalid choice."); yield null; }
            };
        } catch (Exception e) {
            ConsoleHelper.printError("Payment setup error: " + e.getMessage());
            return;
        }

        if (payment == null) return;

        // Process payment
        System.out.println();
        boolean paymentSuccess = payment.processPayment(cart.getTotal());
        if (!paymentSuccess) {
            ConsoleHelper.printError("Payment failed. Order not placed. Please try again.");
            return;
        }

        // Deduct stock for each item
        try {
            for (CartItem item : cart.getItems()) {
                productService.deductStock(item.getProduct().getProductId(), item.getQuantity());
            }
        } catch (ProductNotFoundException | InsufficientStockException e) {
            ConsoleHelper.printError("Stock error during order placement: " + e.getMessage());
            return;
        }

        // Place order
        Order order = orderService.placeOrder(currentUser, cart, payment.getMethodName(), deliveryAddress);
        userService.addOrderToHistory(currentUser.getUsername(), order.getOrderId());

        // Show receipt
        order.printReceipt();
        ConsoleHelper.printSuccess("Order placed successfully! Order ID: " + order.getOrderId());

        // Clear cart
        cart.clear();
    }

    // ── Orders & Profile ─────────────────────────────────────────────────────

    private void viewMyOrders() {
        ConsoleHelper.printHeader("MY ORDER HISTORY");
        List<Order> myOrders = orderService.getOrdersByUser(currentUser.getUserId());
        if (myOrders.isEmpty()) {
            ConsoleHelper.printInfo("You have no orders yet.");
            return;
        }
        orderService.printOrderSummaryTable(myOrders);

        System.out.print("\n  View details of an order? Enter Order ID (or Enter to skip): ");
        String id = scanner.nextLine().trim();
        if (!id.isBlank()) {
            Order o = orderService.findById(id);
            if (o != null && o.getUserId().equals(currentUser.getUserId())) {
                o.printReceipt();
            } else {
                ConsoleHelper.printError("Order not found or doesn't belong to you.");
            }
        }
    }

    private void viewProfile() {
        ConsoleHelper.printHeader("MY PROFILE");
        System.out.printf("  User ID   : %s%n", currentUser.getUserId());
        System.out.printf("  Username  : %s%n", currentUser.getUsername());
        System.out.printf("  Email     : %s%n", currentUser.getEmail());
        System.out.printf("  Phone     : %s%n", currentUser.getPhone());
        System.out.printf("  Address   : %s%n", currentUser.getAddress());
        System.out.printf("  Orders    : %d placed%n", currentUser.getOrderHistory().size());
    }
}

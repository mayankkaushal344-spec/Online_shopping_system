package com.shopping.admin;

import com.shopping.exception.InsufficientStockException;
import com.shopping.exception.ProductNotFoundException;
import com.shopping.model.Order;
import com.shopping.model.Product;
import com.shopping.service.OrderService;
import com.shopping.service.ProductService;
import com.shopping.service.UserService;
import com.shopping.util.ConsoleHelper;

import java.util.List;
import java.util.Scanner;

public class Admin {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private final ProductService productService;
    private final OrderService   orderService;
    private final UserService    userService;
    private final Scanner        scanner;

    public Admin(ProductService ps, OrderService os, UserService us, Scanner sc) {
        this.productService = ps;
        this.orderService   = os;
        this.userService    = us;
        this.scanner        = sc;
    }

    public boolean authenticate() {
        ConsoleHelper.printHeader("ADMIN LOGIN");
        System.out.print("  Admin Username : ");
        String u = scanner.nextLine().trim();
        System.out.print("  Admin Password : ");
        String p = scanner.nextLine().trim();
        if (ADMIN_USERNAME.equals(u) && ADMIN_PASSWORD.equals(p)) {
            ConsoleHelper.printSuccess("Admin authenticated.");
            return true;
        }
        ConsoleHelper.printError("Invalid admin credentials.");
        return false;
    }

    public void showAdminMenu() {
        boolean running = true;
        while (running) {
            ConsoleHelper.printHeader("ADMIN DASHBOARD");
            System.out.println("  PRODUCT MANAGEMENT");
            ConsoleHelper.printMenuOption(1, "Add New Product");
            ConsoleHelper.printMenuOption(2, "Update Product");
            ConsoleHelper.printMenuOption(3, "Delete Product");
            ConsoleHelper.printMenuOption(4, "View All Products");
            ConsoleHelper.printMenuOption(5, "Manage Inventory (Stock)");
            System.out.println();
            System.out.println("  ORDER MANAGEMENT");
            ConsoleHelper.printMenuOption(6, "View All Orders");
            ConsoleHelper.printMenuOption(7, "Update Order Status");
            ConsoleHelper.printMenuOption(8, "View Order Details");
            System.out.println();
            System.out.println("  USER MANAGEMENT");
            ConsoleHelper.printMenuOption(9, "View All Users");
            System.out.println();
            ConsoleHelper.printMenuOption(0, "Logout Admin");
            ConsoleHelper.printDivider();
            System.out.print("  Select option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addProduct();
                case "2" -> updateProduct();
                case "3" -> deleteProduct();
                case "4" -> viewAllProducts();
                case "5" -> manageInventory();
                case "6" -> viewAllOrders();
                case "7" -> updateOrderStatus();
                case "8" -> viewOrderDetails();
                case "9" -> viewAllUsers();
                case "0" -> running = false;
                default  -> ConsoleHelper.printError("Invalid option.");
            }
        }
    }

    // ── Product operations ───────────────────────────────────────────────────

    private void addProduct() {
        ConsoleHelper.printHeader("ADD NEW PRODUCT");
        try {
            System.out.print("  Name        : "); String name = scanner.nextLine().trim();
            System.out.print("  Category    : "); String cat  = scanner.nextLine().trim();
            System.out.print("  Price (₹)   : "); double price = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("  Stock       : "); int stock    = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("  Description : "); String desc  = scanner.nextLine().trim();

            if (name.isBlank() || cat.isBlank()) { ConsoleHelper.printError("Name/Category cannot be empty."); return; }
            if (price < 0 || stock < 0)           { ConsoleHelper.printError("Price/Stock must be non-negative."); return; }

            productService.addProduct(name, cat, price, stock, desc);
            ConsoleHelper.printSuccess("Product '" + name + "' added successfully!");
        } catch (NumberFormatException e) {
            ConsoleHelper.printError("Invalid number format.");
        }
    }

    private void updateProduct() {
        ConsoleHelper.printHeader("UPDATE PRODUCT");
        viewAllProducts();
        System.out.print("  Enter Product ID to update: ");
        String id = scanner.nextLine().trim();
        try {
            Product p = productService.getById(id);
            System.out.println("  Updating: " + p.getName() + " (press Enter to keep current value)");
            System.out.print("  New Name [" + p.getName() + "]: ");
            String name = scanner.nextLine().trim();
            System.out.print("  New Category [" + p.getCategory() + "]: ");
            String cat  = scanner.nextLine().trim();
            System.out.print("  New Price [" + p.getPrice() + "]: ");
            String priceStr = scanner.nextLine().trim();
            System.out.print("  New Stock [" + p.getStock() + "]: ");
            String stockStr = scanner.nextLine().trim();
            System.out.print("  New Description [" + p.getDescription() + "]: ");
            String desc = scanner.nextLine().trim();

            double price = priceStr.isBlank() ? -1 : Double.parseDouble(priceStr);
            int    stock = stockStr.isBlank() ? -1 : Integer.parseInt(stockStr);

            productService.updateProduct(id,
                    name.isBlank()  ? null : name,
                    cat.isBlank()   ? null : cat,
                    price, stock,
                    desc.isBlank()  ? null : desc);
            ConsoleHelper.printSuccess("Product updated successfully!");
        } catch (ProductNotFoundException e) {
            ConsoleHelper.printError(e.getMessage());
        } catch (NumberFormatException e) {
            ConsoleHelper.printError("Invalid number input.");
        }
    }

    private void deleteProduct() {
        ConsoleHelper.printHeader("DELETE PRODUCT");
        System.out.print("  Enter Product ID to delete: ");
        String id = scanner.nextLine().trim();
        System.out.print("  Are you sure? (yes/no): ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equalsIgnoreCase("yes")) { ConsoleHelper.printInfo("Deletion cancelled."); return; }
        try {
            productService.deleteProduct(id);
            ConsoleHelper.printSuccess("Product deleted.");
        } catch (ProductNotFoundException e) {
            ConsoleHelper.printError(e.getMessage());
        }
    }

    private void viewAllProducts() {
        ConsoleHelper.printHeader("ALL PRODUCTS");
        List<Product> all = productService.getAllProducts();
        all.sort((a, b) -> a.getCategory().compareToIgnoreCase(b.getCategory()));
        productService.printProductTable(all);
        System.out.printf("  Total: %d products%n", all.size());
    }

    private void manageInventory() {
        ConsoleHelper.printHeader("MANAGE INVENTORY");
        System.out.print("  Enter Product ID: ");
        String id = scanner.nextLine().trim();
        try {
            Product p = productService.getById(id);
            System.out.printf("  Product: %s | Current Stock: %d%n", p.getName(), p.getStock());
            System.out.println("  [1] Add Stock   [2] Reduce Stock   [3] Set Exact Stock");
            System.out.print("  Choice: ");
            String c = scanner.nextLine().trim();
            System.out.print("  Quantity: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());
            switch (c) {
                case "1" -> { productService.adjustStock(id, qty);  ConsoleHelper.printSuccess("Stock increased by " + qty); }
                case "2" -> { productService.adjustStock(id, -qty); ConsoleHelper.printSuccess("Stock reduced by " + qty); }
                case "3" -> {
                    productService.updateProduct(id, null, null, -1, qty, null);
                    ConsoleHelper.printSuccess("Stock set to " + qty);
                }
                default  -> ConsoleHelper.printError("Invalid choice.");
            }
        } catch (ProductNotFoundException | InsufficientStockException e) {
            ConsoleHelper.printError(e.getMessage());
        } catch (NumberFormatException e) {
            ConsoleHelper.printError("Invalid quantity.");
        }
    }

    // ── Order operations ─────────────────────────────────────────────────────

    private void viewAllOrders() {
        ConsoleHelper.printHeader("ALL ORDERS");
        orderService.printOrderSummaryTable(orderService.getAllOrders());
    }

    private void updateOrderStatus() {
        ConsoleHelper.printHeader("UPDATE ORDER STATUS");
        System.out.print("  Enter Order ID: ");
        String orderId = scanner.nextLine().trim();
        Order order = orderService.findById(orderId);
        if (order == null) { ConsoleHelper.printError("Order not found."); return; }

        System.out.println("  Current Status: " + order.getStatus());
        System.out.println("  [1] PLACED  [2] PROCESSING  [3] SHIPPED  [4] DELIVERED  [5] CANCELLED");
        System.out.print("  New Status: ");
        String s = scanner.nextLine().trim();
        Order.Status newStatus = switch (s) {
            case "1" -> Order.Status.PLACED;
            case "2" -> Order.Status.PROCESSING;
            case "3" -> Order.Status.SHIPPED;
            case "4" -> Order.Status.DELIVERED;
            case "5" -> Order.Status.CANCELLED;
            default  -> null;
        };
        if (newStatus == null) { ConsoleHelper.printError("Invalid status."); return; }
        orderService.updateStatus(orderId, newStatus);
        ConsoleHelper.printSuccess("Order " + orderId + " status updated to " + newStatus);
    }

    private void viewOrderDetails() {
        ConsoleHelper.printHeader("ORDER DETAILS");
        System.out.print("  Enter Order ID: ");
        String orderId = scanner.nextLine().trim();
        Order order = orderService.findById(orderId);
        if (order == null) { ConsoleHelper.printError("Order not found."); return; }
        order.printReceipt();
    }

    // ── User operations ──────────────────────────────────────────────────────

    private void viewAllUsers() {
        ConsoleHelper.printHeader("ALL REGISTERED USERS");
        var users = userService.getAllUsers();
        if (users.isEmpty()) { System.out.println("  No users registered."); return; }
        System.out.println("  ┌──────────┬──────────────────┬───────────────────────┬──────────────┬────────┐");
        System.out.println("  │ User ID  │ Username         │ Email                 │ Phone        │ Orders │");
        System.out.println("  ├──────────┼──────────────────┼───────────────────────┼──────────────┼────────┤");
        users.values().forEach(u -> System.out.printf(
                "  | %-8s | %-16s | %-21s | %-12s | %-6d |%n",
                u.getUserId(), u.getUsername(), u.getEmail(), u.getPhone(), u.getOrderHistory().size()));
        System.out.println("  └──────────┴──────────────────┴───────────────────────┴──────────────┴────────┘");
        System.out.printf("  Total users: %d%n", users.size());
    }
}

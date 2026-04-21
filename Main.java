package com.shopping;

import com.shopping.admin.Admin;
import com.shopping.service.OrderService;
import com.shopping.service.ProductService;
import com.shopping.service.UserService;
import com.shopping.ui.UserMenu;
import com.shopping.util.ConsoleHelper;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Bootstrap services
        ProductService productService = new ProductService();
        UserService    userService    = new UserService();
        OrderService   orderService   = new OrderService();

        UserMenu userMenu = new UserMenu(productService, userService, orderService, scanner);
        Admin    admin    = new Admin(productService, orderService, userService, scanner);

        ConsoleHelper.printBanner();
        System.out.println("  Data loaded. Welcome to the Java Shopping Platform!");
        System.out.println();

        boolean running = true;
        while (running) {
            ConsoleHelper.printHeader("MAIN MENU");
            ConsoleHelper.printMenuOption(1, "User Login / Register");
            ConsoleHelper.printMenuOption(2, "Admin Panel");
            ConsoleHelper.printMenuOption(0, "Exit");
            ConsoleHelper.printDivider();
            System.out.print("  Select: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    boolean loggedIn = userMenu.showAuthMenu();
                    if (loggedIn) userMenu.showShoppingMenu();
                }
                case "2" -> {
                    boolean adminAuth = admin.authenticate();
                    if (adminAuth) admin.showAdminMenu();
                }
                case "0" -> {
                    running = false;
                    ConsoleHelper.printInfo("Thank you for shopping with us. Goodbye!");
                }
                default -> ConsoleHelper.printError("Invalid option. Please enter 0, 1, or 2.");
            }
        }
        scanner.close();
    }
}

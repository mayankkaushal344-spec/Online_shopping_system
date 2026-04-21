package com.shopping.payment;

public interface PaymentMethod {
    /**
     * Process the payment for the given amount.
     * @param amount The amount to be charged.
     * @return true if payment succeeded, false otherwise.
     */
    boolean processPayment(double amount);

    /**
     * Returns the display name of the payment method.
     */
    String getMethodName();

    /**
     * Returns a short transaction reference or ID after a successful payment.
     */
    String getTransactionId();
}

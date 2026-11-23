package com.qbanking.controller;

import com.qbanking.model.BankingException;
import com.qbanking.model.Customer;
import com.qbanking.model.Role;
import com.qbanking.model.User;

/**
 * Remembers WHO is logged in. Every controller receives the session and uses it
 * to make sure the caller is allowed to do what they ask (FR2 - Authentication).
 */
public class Session {

    private final User user;
    private Customer customer;   // null for staff

    public Session(User user, Customer customer) {
        this.user = user;
        this.customer = customer;
    }

    public User getUser() { return user; }

    public boolean isStaff() { return user.role() == Role.STAFF; }

    /** The logged-in customer, or an error if a staff member (or nobody) is logged in. */
    public Customer requireCustomer() {
        if (customer == null) {
            throw new BankingException("Please log in as a customer to do this.");
        }
        return customer;
    }

    /** Throws unless a bank staff member is logged in. */
    public void requireStaff() {
        if (!isStaff()) {
            throw new BankingException("Only bank staff can do this.");
        }
    }

    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getDisplayName() {
        return customer != null ? customer.getFullName() : "Bank Staff (" + user.username() + ")";
    }
}

package com.qbanking.controller;

import com.qbanking.dao.CustomerDAO;
import com.qbanking.dao.Database;
import com.qbanking.model.Customer;

/** Lets a logged-in customer view and update their own details (FR12). */
public class CustomerController {

    private final Session session;

    public CustomerController(Session session) {
        this.session = session;
    }

    public Customer getMyDetails() {
        return session.requireCustomer();
    }

    public void updateMyDetails(String firstName, String surname, String address) {
        Customer current = session.requireCustomer();

        // Build a new object first: if the input is invalid it throws before anything is changed.
        Customer updated = new Customer(current.getId(), firstName, surname, address);

        Database.inTransaction(connection -> {
            new CustomerDAO(connection).update(updated);
            return null;
        });
        session.setCustomer(updated);
    }
}

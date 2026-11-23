package com.qbanking.controller;

import com.qbanking.dao.CustomerDAO;
import com.qbanking.dao.Database;
import com.qbanking.dao.UserDAO;
import com.qbanking.model.BankingException;
import com.qbanking.model.Customer;
import com.qbanking.model.Role;
import com.qbanking.model.User;
import com.qbanking.model.Validation;
import com.qbanking.util.PasswordHasher;

/**
 * Handles Login and Registration (FR1, FR2).
 * The Views call these methods; this class checks the input, then uses the
 * model and DAOs to do the work.
 */
public class AuthController {

    /** Checks the username + password and returns a Session for the logged-in person. */
    public Session login(String username, String password) {
        String name = Validation.requireText(username, "Username");
        if (password == null || password.isEmpty()) {
            throw new BankingException("Password is required.");
        }

        return Database.read(connection -> {
            User user = new UserDAO(connection).findByUsername(name);

            // Same message for "no such user" and "wrong password" so attackers learn nothing.
            if (user == null || !PasswordHasher.matches(password, user.passwordHash())) {
                throw new BankingException("Invalid username or password.");
            }
            if (user.role() == Role.CUSTOMER) {
                Customer customer = new CustomerDAO(connection).findById(user.customerId());
                return new Session(user, customer);
            }
            return new Session(user, null);
        });
    }

    /** Registers a new customer together with their login details. */
    public void register(String firstName, String surname, String address,
                         String username, String password, String confirmPassword) {
        // Creating the Customer object validates names and address.
        Customer customer = new Customer(firstName, surname, address);

        String name = Validation.requireText(username, "Username");
        if (!name.matches("[A-Za-z0-9_]{4,20}")) {
            throw new BankingException("Username must be 4-20 letters, digits or underscores.");
        }
        checkPasswordStrength(password);
        if (!password.equals(confirmPassword)) {
            throw new BankingException("The two passwords do not match.");
        }

        Database.inTransaction(connection -> {
            UserDAO userDAO = new UserDAO(connection);
            if (userDAO.usernameExists(name)) {
                throw new BankingException("That username is already taken.");
            }
            new CustomerDAO(connection).insert(customer);
            userDAO.insert(new User(0, name, PasswordHasher.hash(password), Role.CUSTOMER, customer.getId()));
            return null;
        });
    }

    private void checkPasswordStrength(String password) {
        if (password == null || password.length() < 8
                || password.chars().noneMatch(Character::isLetter)
                || password.chars().noneMatch(Character::isDigit)) {
            throw new BankingException("Password must be at least 8 characters and contain a letter and a digit.");
        }
    }
}

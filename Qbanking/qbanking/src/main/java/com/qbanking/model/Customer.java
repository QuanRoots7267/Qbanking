package com.qbanking.model;

/**
 * A person who banks with Qbanking.
 * A customer can own many accounts (see the accounts table: accounts.customer_id).
 */
public final class Customer {

    private long id;                 // 0 until the database assigns one
    private String firstName;
    private String surname;
    private String address;

    /** Constructor for a brand-new customer (no id yet). */
    public Customer(String firstName, String surname, String address) {
        this(0, firstName, surname, address);
    }

    /** Constructor used when a customer is loaded from the database (overloading). */
    public Customer(long id, String firstName, String surname, String address) {
        this.id = id;
        setFirstName(firstName);
        setSurname(surname);
        setAddress(address);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) {
        this.firstName = Validation.requireText(firstName, "First name");
    }

    public String getSurname() { return surname; }
    public void setSurname(String surname) {
        this.surname = Validation.requireText(surname, "Surname");
    }

    public String getAddress() { return address; }
    public void setAddress(String address) {
        this.address = Validation.requireText(address, "Address");
    }

    public String getFullName() {
        return firstName + " " + surname;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}

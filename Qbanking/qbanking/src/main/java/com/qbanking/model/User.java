package com.qbanking.model;

/**
 * Login details. A CUSTOMER user is linked to a {@link Customer} through customerId;
 * a STAFF user has customerId = 0.
 *
 * Only a hash of the password is ever stored - never the password itself.
 */
public record User(long id, String username, String passwordHash, Role role, long customerId) {
}

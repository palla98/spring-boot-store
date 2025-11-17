package com.codewithmosh.store.users;

public class UserAlreadyExists extends RuntimeException {
    public UserAlreadyExists() {
        super("User already exist");
    }
}

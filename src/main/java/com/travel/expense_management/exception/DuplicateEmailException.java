package com.travel.expense_management.exception;

public class DuplicateEmailException extends RuntimeException{

    public DuplicateEmailException(String email){
        
        super("User already exist with email: " + email);
    }
}

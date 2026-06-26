package com.travel.expense_management.exception;

public class InvalidCredentialsException extends RuntimeException{

    public InvalidCredentialsException(){

        super("Invalid email or password");
    }
}

package com.expenseshare.demo.exception;
public class InvalidSettlementException extends RuntimeException {
    public InvalidSettlementException(String message) {
        super(message);
    }
}

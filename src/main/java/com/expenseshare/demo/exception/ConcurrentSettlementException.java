package com.expenseshare.demo.exception;
public class ConcurrentSettlementException extends RuntimeException {
    public ConcurrentSettlementException(String message) {
        super(message);
    }
}

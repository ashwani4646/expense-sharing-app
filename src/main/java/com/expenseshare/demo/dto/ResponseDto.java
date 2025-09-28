package com.expenseshare.demo.dto;

import com.fasterxml.jackson.databind.json.JsonMapper;

public class ResponseDto {
    String message;
    Number code;

    public ResponseDto(String message, Number code) {
        this.message = message;
        this.code = code;
    }

    @Override
    public String toString() {
        return "{" +
                "message:'" + message + '\'' +
                ", code:" + code +
                '}';
    }
}

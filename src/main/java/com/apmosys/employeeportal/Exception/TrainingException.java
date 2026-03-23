package com.apmosys.employeeportal.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TrainingException extends RuntimeException {

    public TrainingException(String message) {
        super(message);
    }
    
}

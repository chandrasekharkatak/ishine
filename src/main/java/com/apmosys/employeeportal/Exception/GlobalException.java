package com.apmosys.employeeportal.Exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {
	
	@ExceptionHandler(EmployeeNotFoundException.class)
	public ResponseEntity<String> handleNotFound(EmployeeNotFoundException ex){
		return ResponseEntity.badRequest().body("Exception : " + ex.getMessage());
	}

}

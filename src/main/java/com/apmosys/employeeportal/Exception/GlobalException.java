package com.apmosys.employeeportal.Exception;

import javax.persistence.OptimisticLockException;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.apmosys.employeeportal.utility.ExceptionLogContext;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestControllerAdvice
public class GlobalException {
	
	
	@ExceptionHandler(TimesheetValidationFailedException.class)
	public ResponseEntity<ServiceResponse> timesheetValidationFailedException(TimesheetValidationFailedException ex){
        ServiceResponse response = new ServiceResponse();
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        ExceptionLogContext.add(ex.getLocalizedMessage());
        response.setServiceResponse(ex.getMessage());
	    return ResponseEntity
	            .status(HttpStatus.EXPECTATION_FAILED)
	            .body(response);
	}

    @ExceptionHandler(TimesheetApproveValidationFailedException.class)
	public ResponseEntity<ServiceResponse> TimesheetApproveValidationFailedException(TimesheetApproveValidationFailedException ex){
        ServiceResponse response = new ServiceResponse();
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        ExceptionLogContext.add(ex.getLocalizedMessage());
        response.setServiceResponse(ex.getMessage());
	    return ResponseEntity
	            .status(HttpStatus.BAD_REQUEST)
	            .body(response);
	}
	
	@ExceptionHandler(EmployeeNotFoundException.class)
	public ResponseEntity<String> handleNotFound(EmployeeNotFoundException ex){
		return ResponseEntity.badRequest().body("Exception : " + ex.getMessage());
	}
	
	@ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ServiceResponse> handleOptimisticLock(Exception e) {
        ServiceResponse response = new ServiceResponse();
        response.setServiceStatus("Version conflict: another update occurred. Please refresh and try again.");
        response.setServiceResponse(null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ServiceResponse> handleBadRequestException(HttpMessageNotReadableException ex){
        ServiceResponse response = new ServiceResponse();
        System.out.println("=========Test-101==========");
        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        ExceptionLogContext.add(ex.getLocalizedMessage());
        response.setServiceResponse("Invalid Request Type");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceResponse> handleGeneralError(Exception e) {
        ServiceResponse response = new ServiceResponse();
        System.out.println("=========Test-102=========="+e.getClass());

        response.setServiceStatus("Unexpected error: " + e.getMessage());
        response.setServiceResponse(null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ServiceResponse> handleBadRequestException(BadRequestException ex){
        ServiceResponse response = new ServiceResponse();
        response.setServiceStatus("Unexpected error: " + ex.getMessage());
        response.setServiceResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

}

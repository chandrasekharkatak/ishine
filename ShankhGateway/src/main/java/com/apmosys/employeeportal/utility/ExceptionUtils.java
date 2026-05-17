package com.apmosys.employeeportal.utility;

public class ExceptionUtils {

    public static String getExceptionMessage(Throwable e) {
        if (e instanceof org.springframework.dao.DataIntegrityViolationException) {
            return "Data integrity violation occurred.";
        } else if (e instanceof org.hibernate.exception.ConstraintViolationException) {
            return "Database constraint violated.";
        } else if (e instanceof org.springframework.dao.DuplicateKeyException) {
            return "Duplicate record found.";
        } else if (e instanceof javax.persistence.EntityNotFoundException) {
            return "Requested entity not found.";
        } else if (e instanceof javax.persistence.OptimisticLockException) {
            return "Concurrent update conflict occurred. Please retry.";
        } else if (e instanceof org.springframework.web.bind.MethodArgumentNotValidException) {
            return "Validation failed for one or more fields.";
        } else if (e instanceof org.springframework.validation.BindException) {
            return "Invalid request data provided.";
        }
       else if (e instanceof IllegalArgumentException) {
            return "Invalid input provided.";
        } else if (e instanceof org.springframework.transaction.TransactionSystemException) {
            return "Transaction failed unexpectedly.";
        } else if (e instanceof javax.persistence.PersistenceException) {
            return "Persistence error occurred while saving data.";
        } else if (e instanceof NullPointerException) {
            return "Unexpected null value encountered.";
        } else if (e instanceof IndexOutOfBoundsException) {
            return "Invalid index or list access.";
        } else if (e instanceof NumberFormatException) {
            return "Invalid number format provided.";
        }else {
            return "Unexpected error occurred.";
        }
    }
}

package com.blueant_crm_erp.exception.common;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when a database operation
 * fails unexpectedly.
 *
 * Used In:
 * - Insert Operations
 * - Update Operations
 * - Delete Operations
 * - Database Transactions
 * - Repository Layer
 * - JPA/Hibernate Operations
 *
 * Examples:
 * - Database Connection Failure
 * - SQL Exception
 * - Constraint Violation
 * - Transaction Rollback
 * - Deadlock
 * - Data Integrity Violation
 *
 * HTTP Status : 500 INTERNAL_SERVER_ERROR
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class DatabaseException extends BaseException {

    /**
     * Default Constructor
     */
    public DatabaseException() {
        super(
                ErrorCode.DATABASE_ERROR,
                ErrorType.DATABASE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ExceptionMessage.DATABASE_ERROR
        );
    }

    /**
     * Constructor with custom message
     */
    public DatabaseException(String message) {
        super(
                ErrorCode.DATABASE_ERROR,
                ErrorType.DATABASE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                message
        );
    }

    /**
     * Constructor with additional details
     */
    public DatabaseException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.DATABASE_ERROR,
                ErrorType.DATABASE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                message,
                details
        );
    }

    /**
     * Constructor with root cause
     */
    public DatabaseException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.DATABASE_ERROR,
                ErrorType.DATABASE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause
     */
    public DatabaseException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.DATABASE_ERROR,
                ErrorType.DATABASE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                message,
                details,
                cause
        );
    }

}
package com.blueant_crm_erp.exception.common;

import com.blueant_crm_erp.exception.base.BaseException;
import com.blueant_crm_erp.exception.base.ErrorCode;
import com.blueant_crm_erp.exception.base.ErrorType;
import com.blueant_crm_erp.exception.base.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when the system fails to
 * store, upload, update, move, or delete a file.
 *
 * Used In:
 * - Client Document Upload
 * - PAN Card Upload
 * - Aadhaar Upload
 * - Service Request Attachments
 * - Employee Documents
 * - Agreement Files
 * - Report Export
 * - Excel Import
 * - AWS S3 Storage
 * - Local File Storage
 *
 * HTTP Status : 500 INTERNAL_SERVER_ERROR
 *
 * @author BlueAnt CRM ERP
 * @version 1.0
 */
public class FileStorageException extends BaseException {

    /**
     * Default Constructor
     */
    public FileStorageException() {
        super(
                ErrorCode.FILE_STORAGE_ERROR,
                ErrorType.FILE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ExceptionMessage.FILE_STORAGE_ERROR
        );
    }

    /**
     * Constructor with custom message.
     */
    public FileStorageException(String message) {
        super(
                ErrorCode.FILE_STORAGE_ERROR,
                ErrorType.FILE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                message
        );
    }

    /**
     * Constructor with additional details.
     */
    public FileStorageException(
            String message,
            Map<String, Object> details) {

        super(
                ErrorCode.FILE_STORAGE_ERROR,
                ErrorType.FILE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                message,
                details
        );
    }

    /**
     * Constructor with root cause.
     */
    public FileStorageException(
            String message,
            Throwable cause) {

        super(
                ErrorCode.FILE_STORAGE_ERROR,
                ErrorType.FILE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                message,
                cause
        );
    }

    /**
     * Constructor with details and root cause.
     */
    public FileStorageException(
            String message,
            Map<String, Object> details,
            Throwable cause) {

        super(
                ErrorCode.FILE_STORAGE_ERROR,
                ErrorType.FILE,
                HttpStatus.INTERNAL_SERVER_ERROR,
                message,
                details,
                cause
        );
    }

}
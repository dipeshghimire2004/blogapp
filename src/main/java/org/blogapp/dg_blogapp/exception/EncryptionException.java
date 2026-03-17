package org.blogapp.dg_blogapp.exception;

import org.blogapp.dg_blogapp.enums.ErrorCode;

public class EncryptionException extends BaseException {
    
    public EncryptionException(String message) {
        super(ErrorCode.ENCRYPTION_ERROR, message);
    }
    
    public EncryptionException(String message, Throwable cause) {
        super(ErrorCode.ENCRYPTION_ERROR, message);
        initCause(cause);
    }
}

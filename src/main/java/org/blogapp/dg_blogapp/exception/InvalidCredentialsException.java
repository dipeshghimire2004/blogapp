package org.blogapp.dg_blogapp.exception;

import org.blogapp.dg_blogapp.enums.ErrorCode;

public class InvalidCredentialsException extends BaseException{

    public InvalidCredentialsException(String message) {
        super(ErrorCode.INVALID_CREDENTIALS, message);
    }

    public InvalidCredentialsException() {super(ErrorCode.INVALID_CREDENTIALS);}
}

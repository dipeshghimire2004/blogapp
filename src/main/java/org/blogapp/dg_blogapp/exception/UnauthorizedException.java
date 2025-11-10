package org.blogapp.dg_blogapp.exception;

import org.blogapp.dg_blogapp.enums.ErrorCode;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED,message);
    }

    public  UnauthorizedException(){
        super(ErrorCode.UNAUTHORIZED);
    }
}
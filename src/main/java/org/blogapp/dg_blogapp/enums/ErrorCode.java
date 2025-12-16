package org.blogapp.dg_blogapp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    RESOURCE_NOT_FOUND(404, "RESOURCE_NOT_FOUND", "The requested resource was not found."),
    DUPLICATE_ENTITY(409, "DUPLICATE_ENTITY", "Entity already exists."),
    INVALID_CREDENTIALS(401, "INVALID_CREDENTIALS", "Invalid username or password."),
    BAD_REQUEST(400, "BAD_REQUEST", "Invalid request parameters."),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "Something went wrong."),
    OAUTH_ACCOUNT_LINKED_EXCEPTION(401, "OAUTH_ACCOUNT_LINKED_EXCEPTION", "This account is already linked to oauth provider so this service is not available."),
    UNAUTHORIZED(403, "UNAUTHORIZED", "Access denied.");

    private final int httpStatus;
    private final String code;
    private final String message;

}
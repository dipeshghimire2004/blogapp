package org.blogapp.blogapp.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetails {
    private int statusCode;
    private String message;
    private String details;

}
// This is needed to maintain Standardized Error Response format
//without this , every exception response would look different
//with this, all error messages follow a consistent JSON format
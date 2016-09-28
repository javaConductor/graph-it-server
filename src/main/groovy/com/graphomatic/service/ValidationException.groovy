package com.graphomatic.service

/**
 * Created by lee on 9/28/16.
 */
class ValidationException extends UpdateItemException {

    ValidationException(String message, Throwable cause) {
        super(message, cause)
    }

    ValidationException(String message) {
        super(message)
    }
}

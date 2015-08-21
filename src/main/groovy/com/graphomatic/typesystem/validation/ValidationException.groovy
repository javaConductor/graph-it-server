package com.graphomatic.typesystem.validation

/**
 * Created by lcollins on 8/13/2015.
 */
class ValidationException extends  Exception{

    ValidationException(String msg, Throwable e) {
        super(msg, e)
    }

    ValidationException(String msg) {
        super(msg)
    }
}

package com.graphomatic.service

/**
 * Created by lee on 9/28/16.
 */
class UpdateItemException  extends Exception{
    UpdateItemException(String message, Throwable cause) {
        super(message, cause)
    }

    UpdateItemException(String message) {
        super(message)
    }
}

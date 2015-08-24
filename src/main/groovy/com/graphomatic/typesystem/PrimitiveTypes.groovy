package com.graphomatic.typesystem

import com.graphomatic.typesystem.validation.ValidationException
import org.codehaus.groovy.util.StringUtil
import org.springframework.util.StringUtils

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by lcollins on 8/12/2015.
 */
class PrimitiveTypes {
    static final public String  Number = "number"
    static final public String  Text= "text"
    static final public String  Boolean= "boolean"
    static final public String  URL= "url"
    static final public String  Link= URL
    static final public String  DateTime= "dateTime"
    static final public String  EmailAddress = "emailAddress"
    static final public String  Map = "map"
    static final public String  List = "list"
    static final public List<String>  All = [Number,Text,Boolean,URL,Link,DateTime,EmailAddress]

    static final public boolean isPrimitiveType(String typeName){
        All.contains(typeName)
    }

    static boolean validate(String type, String  value) {

            switch (type){
                case Number :
                    return value.isNumber()
                    break
                case Text :
                    return true
                    break
                case Boolean :
                    try {
                        java.lang.Boolean.parseBoolean(value)
                        true
                    }catch(Exception e){
                        return false
                    }
                    break
                case Link :
                    try {
                    new java.net.URL(value)
                        true
                    }catch(Exception e){
                        return false
                    }
                    break
                case DateTime :
                    SimpleDateFormat sdf = new SimpleDateFormat("YYYYmmDDHHMMSS")
                    try {
                        sdf.parse(value)
                        return true
                    }
                    catch(Exception e){
                        return false
                    }
                    break
                case EmailAddress :
                        isEmail(value)
                    break
            }
    }

    private static final String EMAIL_PATTERN = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})"
    static Pattern emailPattern = Pattern.compile(EMAIL_PATTERN)
    static boolean isEmail(String text){
        Matcher m = emailPattern.matcher(text)
        m.matches()
    }

    static Object fromString(String typeName, String value) {
        switch (typeName){
            case Number :
                return value.toDouble()//.isNumber()
            case Text :
                return value
            case Boolean :
                    return java.lang.Boolean.parseBoolean(value)
            case Link :
                try {
                    new java.net.URL(value)
                }catch(Exception e){
                    throw new ValidationException("Value: $value not a valid URL")
                }
                break
            case DateTime :
                SimpleDateFormat sdf = new SimpleDateFormat("YYYYmmDDHHMMSS")
                try {
                    sdf.parse(value)
                }
                catch(Exception e){
                    throw new ValidationException("Value: $value not a valid URL")
                }
                return value
            case EmailAddress :
                isEmail(value) ? value : null
                break
            default:
                throw new ValidationException("Type: $typeName not a valid primitive type.")
        }
    }
}
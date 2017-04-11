package com.gmail.dleemcewen.tandemfieri;

public final class FormConstants {
    /* Regular Expressions */
    public static final String REG_EX_FIRSTNAME = "^[a-zA-Z]+[\\sa-zA-Z]*$";
    public static final String REG_EX_LASTNAME = "^[a-zA-Z]+[\\sa-zA-Z]*$";
    public static final String REG_EX_ADDRESS = "^[\\w]+[\\s\\w-]*";
    public static final String REG_EX_CITY = "^[a-zA-Z]+[\\sa-zA-Z-]*$";
    public static final String REG_EX_ZIP = "\\d{5}|\\d{9}";
    public static final String REG_EX_PHONE = "\\d{10}";
    public static final String REG_EX_EMAIL = "([\\w-\\.]+)@((?:[\\w]+\\.)+)([a-zA-Z]{2,4})";
    public static final String REG_EX_PASSWORD = "(.*\\w.*){6,}";
    public static final String REG_EX_MONETARY = "^(0*[1-9][0-9]*(\\.[0-9]{0,2})?|0+\\.[0-9][1-9])$";

    /*Error Tags */
    public static final String ERROR_TAG_FIRSTNAME = "Invalid first name. Must be non-empty and contain only letters and spaces.";
    public static final String ERROR_TAG_LASTNAME = "Invalid last name. Must be non-empty and contain only letters and spaces.";
    public static final String ERROR_TAG_ADDRESS = "Invalid address. Must be non-empty and begin with an alphanumeric character.";
    public static final String ERROR_TAG_CITY = "Invalid city. Must be non-empty and contain only letters and spaces.";
    public static final String ERROR_TAG_ZIP = "Invalid ZIP code. Must be non-empty and in the form '12345' or '123456789' if ZIP+4.";
    public static final String ERROR_TAG_PHONE = "Invalid phone number. Must be non-empty and in the form '1234567890'.";
    public static final String ERROR_TAG_EMAIL = "Invalid email. Must be of format 'name@example.com'.";
}

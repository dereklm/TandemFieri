package com.gmail.dleemcewen.tandemfieri.Formatters;

/**
 * StringFormatter contains methods and logic for string formatting
 */

public class StringFormatter {
    /**
     * toProperCase converts a string to proper case
     * @param sourceString indicates the source string to convert
     * @return string converted to proper case
     */
    public static String toProperCase(String sourceString) {
        StringBuilder properCaseString = new StringBuilder();
        String[] splitString = sourceString.split(" ");
        for (String splitPart : splitString) {
            if (splitPart.length() > 0) {
                properCaseString
                        .append(splitPart.substring(0, 1).toUpperCase() + splitPart.substring(1).toLowerCase());
                properCaseString.append(" ");
            }
        }

        return properCaseString.toString().trim();
    }
}

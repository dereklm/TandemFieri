package com.gmail.dleemcewen.tandemfieri.Validator;

import android.widget.EditText;

import java.util.regex.Pattern;

/**
 * Validator exposes basic validation methods for common scenarios
 */

public class Validator {
    /**
     * check that provided edittext control contains a valid value
     * @param editTextControl identifies the edittext control to validate
     * @param errorMessage indicates the errormessage to display if the control value is not valid
     * @return true or false
     */
    public static boolean isValid(EditText editTextControl, String errorMessage) {
        return checkForValidValue(editTextControl, "", errorMessage);
    }

    /**
     * check that provided edittext control contains a valid value
     * @param editTextControl identifies the edittext control to validate
     * @param regex indicates a regex expression to apply to the edittext value
     * @param errorMessage indicates the errormessage to display if the control value is not valid
     * @return true or false
     */
    public static boolean isValid(EditText editTextControl, String regex, String errorMessage) {
        return checkForValidValue(editTextControl, regex, errorMessage);
    }

    /**
     * check the provided edittext control for valid values
     * @param editTextControl identifies the edittext control to validate
     * @param regex indicates a regex expression to apply to the edittext value
     * @param errorMessage indicates the errormessage to display if the control value is not valid
     * @return true or false
     */
    private static boolean checkForValidValue(EditText editTextControl, String regex, String errorMessage) {
        boolean controlValueIsValid = false;

        //clear any pre-existing error messages
        editTextControl.setError(null);

        //get the text from the edittext control
        String controlText = editTextControl.getText().toString().trim();

        //check to see if controltext is empty
        if (controlText != null && !controlText.isEmpty()) {
            if (!regex.isEmpty()) {
                if (Pattern.matches(regex, controlText)) {
                    controlValueIsValid = true;
                }
            } else {
                controlValueIsValid = true;
            }
        }

        if (!controlValueIsValid) {
            editTextControl.setError(errorMessage);
        }

        return controlValueIsValid;
    }
}

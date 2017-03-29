package com.gmail.dleemcewen.tandemfieri.Utility;

import android.widget.EditText;

public class General {
    public static boolean isEditTextEmpty(EditText et) {
        return et.getText().toString().trim().length() == 0;
    }
}
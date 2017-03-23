package com.gmail.dleemcewen.tandemfieri.Utility;

import android.content.Context;

import com.github.rtoshiro.secure.SecureSharedPreferences;

public class BraintreeUtil {
    public static String getClientToken(Context context) {
        SecureSharedPreferences settings = new SecureSharedPreferences(context);

        return settings.getString("braintreeClientToken", "");
    }

    public static void setClientToken(Context context, String token) {
        SecureSharedPreferences settings = new SecureSharedPreferences(context);
        SecureSharedPreferences.Editor editor = settings.edit();
        editor.putString("braintreeClientToken", token);
    }
}
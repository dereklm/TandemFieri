package com.gmail.dleemcewen.tandemfieri.Utility;

import android.content.Context;
import android.widget.Toast;

public class Util {
    public static void JellyToast (Context context, String msg, int length) {
        Toast.makeText(context, msg, length).show();
    }
}
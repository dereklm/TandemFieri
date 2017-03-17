package com.gmail.dleemcewen.tandemfieri.Events;

public class ToastEvent {
    public final String activity;
    public final String message;
    public final int level;

    public ToastEvent(String activity, String message, int level) {
        this.activity = activity;
        this.message = message;
        this.level = level;
    }
}
package com.gmail.dleemcewen.tandemfieri.Events;

public class ActivityEvent {
    public enum Result {
        REFRESH_RESTAURANT_LIST;
    }

    public final Result result;

    public ActivityEvent(Result result) {
        this.result = result;
    }
}
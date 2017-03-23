package com.gmail.dleemcewen.tandemfieri.Utility;

public class Conversions {
    private static final double MILES_TO_METERS = 1609.344;

    public static double milesToMeters(Integer miles) {
        return MILES_TO_METERS * miles;
    }
}
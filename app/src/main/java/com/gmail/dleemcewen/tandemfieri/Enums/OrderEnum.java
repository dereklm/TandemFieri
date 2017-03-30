package com.gmail.dleemcewen.tandemfieri.Enums;

import java.io.Serializable;

/**
 * Created by Derek on 3/10/2017.
 */

public enum OrderEnum implements Serializable {
    CREATING,
    PAYMENT_PENDING,
    EN_ROUTE,
    COMPLETE,
    REFUNDED
}
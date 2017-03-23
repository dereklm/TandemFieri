package com.gmail.dleemcewen.tandemfieri.Logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ToastLogger defines a logger that uses the ToastLogHandler
 */

public class ToastLogger extends Logger {
    /**
     * Default constructor
     */
    public ToastLogger() {
        super("ToastLogger", null);

        setLevel(Level.FINE);

        ToastLogHandler handler = new ToastLogHandler();
        handler.setLevel(getLevel());
        addHandler(handler);
    }
}

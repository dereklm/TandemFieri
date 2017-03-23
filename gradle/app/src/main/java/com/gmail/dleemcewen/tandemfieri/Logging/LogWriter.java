package com.gmail.dleemcewen.tandemfieri.Logging;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * LogWriter provides a singleton instance that exposes logging capabilities
 */
public class LogWriter {
    private static final String TAG = "Tandem Fieri";
    private static LogWriter logWriterInstance = new LogWriter();
    private static Level minimumLoggingLevel;
    private static List<Logger> addedLoggers;

    /**
     * Default constructor, intentionally private so it cannot be directly accessed
     */
    private LogWriter() {
        LogManager.getLogManager().reset();
        minimumLoggingLevel = Level.SEVERE;
        addedLoggers = new ArrayList<>();
    }

    /**
     * getInstance returns the instance of the LogWriter
     * @return
     */
    public static LogWriter getInstance() {
        return logWriterInstance;
    }

    /**
     * addLogger checks to ensure that a logger doesn't already exist,
     * and then adds the new logger
     * @param logger indicates the new logger to add
     */
    public static void addLogger(Logger logger) {
        LogManager logManager = LogManager.getLogManager();

        if (!Collections.list(logManager.getLoggerNames()).contains(logger.getName()))
        {
            logManager.addLogger(logger);
            addedLoggers.add(logger);
        }
    }

    /**
     * setMinimumLoggingLevel sets the minimum logging level
     * any messages whose logging level falls below the minimum logging level
     * that has been set will not be logged
     * @param level indicates the string defining the minimum logging level
     */
    public static void setMinimumLoggingLevel(String level) {
        Level levelEnum = Level.parse(level);
        setMinimumLoggingLevel(levelEnum);
    }

    /**
     * setMinimumLoggingLevel sets the minimum logging level
     * any messages whose logging level falls below the minimum logging level
     * that has been set will not be logged
     * @param level indicates the minimum logging level
     */
    public static void setMinimumLoggingLevel(Level level) {
        minimumLoggingLevel = level;
    }

    /**
     * log a message to all of the loggers and handlers that have the minimum logging level
     * or higher set
     * @param context indicates the current activity context
     * @param level identifies the logging level of the message
     * @param message indicates the message to log
     */
    public static void log(Context context, Level level, String message) {
        //First check to make sure the log level is greater than or equal to the minimumlogginglevel
        if (level.intValue() >= minimumLoggingLevel.intValue())
        {
            LogManager logManager = LogManager.getLogManager();

            //get the logger names
            Enumeration<String> loggerNames = logManager.getLoggerNames();

            //loop through the logger names
            while (loggerNames.hasMoreElements()){
                String loggerName = loggerNames.nextElement();

                Logger logger = logManager.getLogger(loggerName);

                //if the logger is not null and the logger level is greater than
                //or equal to the level of the log message, log it
                if (logger != null) {
                    Level loggerLevel = logger.getLevel();
                    if (loggerLevel != null && level.intValue() >= loggerLevel.intValue()) {
                        logger.log(level, message, context);
                    }
                }
            }
        }
    }
}

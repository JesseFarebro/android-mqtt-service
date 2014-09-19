package com.jessefarebro.mqtt;

import android.util.Log;

/**
 * Created by Jesse on 2/5/14.
 */
public class MqttLog {
    private final String mTag;
    private final LogLevel mLevel;

    public static enum LogLevel {
        NONE(0),
        BASIC(1),
        FULL(2);

        private final int value;
        private LogLevel(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }

        public static LogLevel fromValue(int value) {
            for (LogLevel l : LogLevel.values()) {
                if (value == l.getValue()) {
                    return l;
                }
            }
            return LogLevel.NONE;
        }
    }

    public MqttLog(String tag) {
        this(tag, LogLevel.BASIC);
    }

    public MqttLog(String tag, LogLevel level) {
        mTag = tag;
        mLevel = level;
    }

    public final void log(LogLevel level, String message) {
        Log.i(getTag(), "Wanting to log: " + message);
        if (mLevel.getValue() >= level.getValue()) {
            Log.d(getTag(), message);
        }
    }

    public String getTag() {
        return mTag;
    }

    public LogLevel getLevel() {
        return mLevel;
    }

}

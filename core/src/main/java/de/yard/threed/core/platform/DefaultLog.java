package de.yard.threed.core.platform;

public class DefaultLog implements Log {

    static public int LEVEL_ERROR = 200;
    static public int LEVEL_WARN = 300;
    static public int LEVEL_INFO = 400;
    static public int LEVEL_DEBUG = 500;

    private Log nativeLog;
    private int level;

    public DefaultLog(int level, Log nativeLog) {
        this.level = level;
        this.nativeLog = nativeLog;
    }

    @Override
    public void debug(String msg) {
        if (level >= LEVEL_DEBUG) {
            nativeLog.debug(msg);
        }
    }

    @Override
    public void info(String msg) {
        if (level >= LEVEL_INFO) {
            nativeLog.info(msg);
        }
    }

    @Override
    public void warn(String msg) {
        if (level >= LEVEL_WARN) {
            nativeLog.warn(msg);
        }
    }

    @Override
    public void error(String msg) {
        if (level >= LEVEL_ERROR) {
            nativeLog.error(msg);
        }
    }

    @Override
    public void error(String msg, Exception e) {
        if (level >= LEVEL_ERROR) {
            nativeLog.error(msg, e);
        }
    }

    @Override
    public void warn(String msg, Exception e) {
        if (level >= LEVEL_WARN) {
            nativeLog.warn(msg, e);
        }
    }

    public int getLevel() {
        return level;
    }
}

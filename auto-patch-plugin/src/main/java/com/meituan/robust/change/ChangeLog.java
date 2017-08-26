package com.meituan.robust.change;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hedingxu on 17/8/24.
 */

public class ChangeLog {

    private static HashMap<String, Log> classChangeLog = new HashMap<String, Log>();

    public static void log(String className, String log) {
        Log changeLog = classChangeLog.get(className);
        if (changeLog == null) {
            changeLog = new Log(className);
            classChangeLog.put(className, changeLog);
        }
        changeLog.recordLog(log);
    }

    public static void print() {
        if (classChangeLog.size() == 0) {
            System.err.println("ChangeLog: nothing changed");
        }
        System.err.println("=====ChangeLog: start print ");
        for (Log log : classChangeLog.values()) {
            log.printLogs();
        }
        System.err.println("=====ChangeLog: end print ");
    }

    static class Log {
        public Log(String className) {
            this.className = className;
        }

        private String className;
        private List<String> logs = new ArrayList<>();

        public void recordLog(String log) {
            logs.add(log);
        }

        public void printLogs() {
            System.err.println(className + ": ");
            int index = 0;
            for (String log : logs) {
                System.err.println(index + " : " + log);
                index++;
            }
            System.err.println();
        }
    }

}

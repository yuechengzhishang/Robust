package com.meituan.robust.utils;

import com.meituan.robust.common.TxtFileReaderAndWriter;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hedingxu on 17/9/6.
 */

public class RobustLog {

    public static void log(String exceptionName, Throwable throwable) {
        logPatching();
        String line1 = exceptionName + ":";
        String line2 = getStackTraceString(throwable);
//        System.err.println("robust log -> " + line1);
//        System.err.println(line2);
        write2FileLineByLine(line1);
        write2FileLineByLine(line2);
    }

    public static void log(String info) {
        logPatching();
        String line = info + "";
//        System.err.println("robust log -> " + line);
        write2FileLineByLine(line);
    }

    private static String logPath;

    public static void setRobustLogFilePath(String path) {
        logPath = path;
    }

    private static List<String> logCacheList = new ArrayList<>();
    public static synchronized void write2FileLineByLine(String line) {
        if (true){
            return;
        }
        if (null == logPath || "".equals(logPath)) {
            logCacheList.add(line);
            return;
        }
        if (logCacheList.size() > 0) {
            for (String cacheLine : logCacheList) {
                String writeString = cacheLine;
                if (new File(logPath).exists()) {
                    String readString = TxtFileReaderAndWriter.readFileAsString(logPath);
                    if (null == readString || "".equals(readString)) {

                    } else {
                        writeString = readString + "\n" + writeString;
                    }
                }
                TxtFileReaderAndWriter.writeFile(logPath, writeString);
            }
            logCacheList.clear();
        }
        String writeString = line;
        if (new File(logPath).exists()) {
            String readString = TxtFileReaderAndWriter.readFileAsString(logPath);
            if (null == readString || "".equals(readString)) {

            } else {
                writeString = readString + "\n" + writeString;
            }
        }
        TxtFileReaderAndWriter.writeFile(logPath, writeString);
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     *
     * @param tr An exception to log
     */
    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    private static int count = 0;
    public static void logPatching(){
        if (count < 2 ){
            System.err.println("robust log -> " + "generating patch ...");
            count ++;
        }
    }
}

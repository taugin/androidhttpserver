package com.chukong.apwebauthentication.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.text.TextUtils;

public class Log {

    public static final String TAG = "taugin";
    private static final boolean DEBUG = true;

    private static Log sLog;
    private Context mContext;
    public static Log getLog(Context context) {
        if (sLog == null) {
            sLog = new Log(context);
        }
        return sLog;
    }
    private Log(Context context) {
        mContext = context;
    }

    public static void d(String tag, String message) {
        if (DEBUG) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.d(tag, extraString + message);
        }
    }

    public static void v(String tag, String message) {
        if (DEBUG) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.v(tag, extraString + message);
        }
    }

    public static void i(String tag, String message) {
        if (DEBUG) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.i(tag, extraString + message);
        }
    }

    public static void w(String tag, String message) {
        if (DEBUG) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.w(tag, extraString + message);
        }
    }

    public static void e(String tag, String message) {
        if (DEBUG) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.e(tag, extraString + message);
        }
    }

    private static boolean privateTag() {
        String tag = System.getProperty("persist.sys.log", "1");
        if (TextUtils.isEmpty(tag)) {
            return true;
        } else {
            return tag.equals("1") ? true : false;
        }
    }
    private static String getMethodNameAndLineNumber() {
        StackTraceElement element[] = Thread.currentThread().getStackTrace();
        if (element != null && element.length >= 4) {
            String methodName = element[4].getMethodName();
            int lineNumber = element[4].getLineNumber();
            long threadId = Thread.currentThread().getId();
            return String.format("T:%d : %s : %d ---> ", threadId, methodName, lineNumber);
        }
        return null;
    }
    
    private static String getTag() {
        StackTraceElement element[] = Thread.currentThread().getStackTrace();
        if (element != null && element.length >= 4) {
            String className = element[4].getClassName();
            if (className == null) {
                return null;
            }
            int index = className.lastIndexOf(".");
            if (index != -1) {
                className = className.substring(index + 1);
            }
            index = className.indexOf('$');
            if (index != -1) {
                className = className.substring(0, index);
            }
            //android.util.Log.d("taugin", "className = " + className);
            return className;
        }
        return null;
    }
    public void recordOperation(String operation) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String time = sdf.format(new Date(System.currentTimeMillis())) + " : ";
        d("taugin1", time + operation);
        //Runtime.getRuntime().exec("cat " + operation + " > ")
    }
}
package com.chukong.apwebauthentication.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.net.wifi.WifiManager;
import com.chukong.apwebauthentication.util.Log;

import com.chukong.apwebauthentication.util.CmdExecutor;
import com.chukong.apwebauthentication.util.CommonUtil;
import com.chukong.apwebauthentication.util.IptableSet;

public class RedirectSwitch {
    private boolean mRedirected = false;
    private Context mContext;
    private RedirectSwitch(Context context) {
        mContext = context;
    }

    private static RedirectSwitch sRedirectSwitch = null;
    public static RedirectSwitch getInstance(Context context) {
        if (sRedirectSwitch == null) {
            sRedirectSwitch = new RedirectSwitch(context);
        }
        return sRedirectSwitch;
    }

    public boolean setRedirectState(boolean redirect) {
        String script = null;
        if (redirect) {
            script = IptableSet.generateClearIpRule() + IptableSet.generateIpCheckRule();
        } else {
            script = IptableSet.generateClearIpRule();
        }
        StringBuilder builder = new StringBuilder();
        int exitCode = -1;
        try {
            exitCode = CmdExecutor.runScriptAsRoot(script, builder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(Log.TAG, builder.toString());
        if (exitCode != 0) {
            return false;
        }
        return true;
    }
    public boolean hasRedirected() {
        String script = IptableSet.NAT_RULE_LIST;
        StringBuilder builder = new StringBuilder();
        int exitCode = -1;
        try {
            exitCode = CmdExecutor.runScriptAsRoot(script, builder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exitCode == 0) {
            int index = builder.indexOf("IP_CHECK");
            if (index != -1) {
                return true;
            }
        }
        return false;
    }
}

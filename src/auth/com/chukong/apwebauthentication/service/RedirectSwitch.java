package com.chukong.apwebauthentication.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.join.ws.Constants;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

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
            String tmpAddr = getSubnet();
            if (tmpAddr == null) {
                return false;
            }
            String subNet = tmpAddr + ".0/24";
            Log.d(Log.TAG, "subNet = " + subNet);
            script = IptableSet.generateClearIpRule() + IptableSet.generateIpCheckRule(subNet);
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
        String iptablesResult = builder.toString();
        Log.d(Log.TAG, iptablesResult);
        if (exitCode != 0) {
            return false;
        }
        if (iptablesResult != null) {
            if (iptablesResult.contains(Constants.IPTABLES_SUCCESS)) {
                PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(Constants.REDIRECT_STATUS, redirect).apply();
                return true;
            }
        }
        return false;
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
    
    private String getSubnet() {
        String addr = null;
        int count = 0;
        do {
            addr = CommonUtil.getLocalIpAddress();
            count++;
            if (addr == null) {
                try {
                    Log.d(Log.TAG, "sleep 500ms-------------------------------------------------------");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while(addr == null && count < 3);
        if (addr == null) {
            return null;
        }
        int lastIndex = addr.lastIndexOf(".");
        if (lastIndex == -1) {
            return null;
        }
        String tmpAddr = addr.substring(0, lastIndex);
        Log.d(Log.TAG, "tmpAddr = " + tmpAddr + " , count = " + count);
        return tmpAddr;
    }
}

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
    public static final int WIFI_AP_STATE_UNKNOWN = -1;
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
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

    public boolean getWifiApState() {
        boolean enabled = false;
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            enabled = (Boolean) method.invoke(wifiManager);
        } catch (NoSuchMethodException e) {
            Log.d("taugin", "e = " + e);
        } catch (IllegalAccessException e) {
            Log.d("taugin", "e = " + e);
        } catch (IllegalArgumentException e) {
            Log.d("taugin", "e = " + e);
        } catch (InvocationTargetException e) {
            Log.d("taugin", "e = " + e);
            e.printStackTrace();
        }
        return enabled;
    }
    public void openRedirectIfWifiApEnabled() {
        Log.d("taugin", "openRedirectIfWifiApEnabled");
        if (!getWifiApState()) {
            return ;
        }
        Log.d("taugin", "openRedirect");
        StringBuilder builder = new StringBuilder();
        String addr = null;
        while((addr = CommonUtil.getLocalIpAddress()) == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d("taugin", "add = " + addr);
        try {
            CmdExecutor.runScriptAsRoot("netcfg", builder);
            String addrMast = CommonUtil.pickIpAndMask(builder.toString(), addr);
            Log.d("taugin", "addrMast = " + addrMast);
            builder.delete(0, builder.length());
            mRedirected = CmdExecutor.runScriptAsRoot(IptableSet.generateIpCheckRule(addrMast), builder) == 0;
            Log.d("taugin", "builder = " + builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void closeRedirect() {
        Log.d("taugin", "closeRedirect");
        if (mRedirected) {
            StringBuilder builder = new StringBuilder();
            try {
                CmdExecutor.runScriptAsRoot(IptableSet.generateClearIpRule(), builder);
                Log.d("taugin", "builder = " + builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

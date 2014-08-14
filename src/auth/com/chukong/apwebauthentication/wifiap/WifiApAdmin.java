package com.chukong.apwebauthentication.wifiap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.chukong.apwebauthentication.util.Log;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.RemoteException;

public class WifiApAdmin {

    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";

    private Context mContext;
    private WifiApAdmin(Context context) {
        mContext = context;
    }

    public boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Class<? extends WifiManager> wifiManagerClass = wifiManager.getClass();
            if (wifiManagerClass == null) {
                return false;
            }
            Method method = wifiManagerClass.getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            if (method == null) {
                return false;
            }
            Boolean result = (Boolean) method.invoke(wifiManager, wifiConfig, enabled);
            return result.booleanValue();
        } catch (NoSuchMethodException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalAccessException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (InvocationTargetException e) {
            Log.d(Log.TAG, "e = " + e);
        }
        return false;
    }
    
    public int getWifiApState() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Class<? extends WifiManager> wifiManagerClass = wifiManager.getClass();
            if (wifiManagerClass == null) {
                return WIFI_AP_STATE_FAILED;
            }

            Method method = wifiManagerClass.getMethod("getWifiApState");
            if (method == null) {
                return WIFI_AP_STATE_FAILED;
            }
            Integer result = (Integer) method.invoke(wifiManager);
            return result.intValue();
        } catch (NoSuchMethodException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalAccessException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (InvocationTargetException e) {
            Log.d(Log.TAG, "e = " + e);
        }
        return WIFI_AP_STATE_FAILED;
    }
    
    public boolean isWifiApEnabled() {
        boolean enabled = false;
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Class<? extends WifiManager> wifiManagerClass = wifiManager.getClass();
            if (wifiManagerClass == null) {
                return false;
            }
            Method method = wifiManagerClass.getDeclaredMethod("isWifiApEnabled");
            if (method == null) {
                return false;
            }
            enabled = (Boolean) method.invoke(wifiManager);
        } catch (NoSuchMethodException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalAccessException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (InvocationTargetException e) {
            Log.d(Log.TAG, "e = " + e);
            e.printStackTrace();
        }
        return enabled;
    }
    
    public WifiConfiguration getWifiApConfiguration() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Class<? extends WifiManager> wifiManagerClass = wifiManager.getClass();
            if (wifiManagerClass == null) {
                return null;
            }
            Method method = wifiManagerClass.getDeclaredMethod("getWifiApConfiguration");
            if (method == null) {
                return null;
            }
            WifiConfiguration wifiConfiguration = (WifiConfiguration) method.invoke(wifiManager);
            return wifiConfiguration;
        } catch (NoSuchMethodException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalAccessException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (InvocationTargetException e) {
            Log.d(Log.TAG, "e = " + e);
            e.printStackTrace();
        }
        return null;
    }

    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Class<? extends WifiManager> wifiManagerClass = wifiManager.getClass();
            if (wifiManagerClass == null) {
                return false;
            }
            Method method = wifiManagerClass.getDeclaredMethod("setWifiApConfiguration", WifiConfiguration.class);
            if (method == null) {
                return false;
            }
            Boolean result = (Boolean) method.invoke(wifiManager, wifiConfig);
            return result.booleanValue();
        } catch (NoSuchMethodException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalAccessException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (IllegalArgumentException e) {
            Log.d(Log.TAG, "e = " + e);
        } catch (InvocationTargetException e) {
            Log.d(Log.TAG, "e = " + e);
            e.printStackTrace();
        }
        return false;
    }
}

package com.chukong.apwebauthentication.wifiap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.chukong.apwebauthentication.receiver.WifiApStateReceiver;

import com.chukong.apwebauthentication.util.Log;

public class WifiApManager {

    private Context mContext;
    
    private static WifiApManager sWifiApAdmin = null;
    public static WifiApManager getInstance(Context context) {
        if (sWifiApAdmin == null) {
            sWifiApAdmin = new WifiApManager(context);
        }
        return sWifiApAdmin;
    }
    private WifiApManager(Context context) {
        mContext = context;
    }

    private boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Class<? extends WifiManager> wifiManagerClass = wifiManager.getClass();
            if (wifiManagerClass == null) {
                return false;
            }
            Method method = wifiManagerClass.getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
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
            Log.d(Log.TAG, "e = " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public void setSoftApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiManager.getWifiState();
        if (enabled && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||  
                (wifiState == WifiManager.WIFI_STATE_ENABLED))) {  
            wifiManager.setWifiEnabled(false);  
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("wifi_saved_state", 1).commit();
        } 
        setWifiApEnabled(wifiConfig, enabled);
        if (!enabled) {  
            int wifiSavedState = 0;  
            wifiSavedState = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("wifi_saved_state", 0);
            if (wifiSavedState == 1) {  
                wifiManager.setWifiEnabled(true);  
                PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("wifi_saved_state", 0).commit();
            }  
        }  
    }
    public int getWifiApState() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Class<? extends WifiManager> wifiManagerClass = wifiManager.getClass();
            if (wifiManagerClass == null) {
                return WifiApStateReceiver.WIFI_AP_STATE_FAILED;
            }

            Method method = wifiManagerClass.getMethod("getWifiApState");
            if (method == null) {
                return WifiApStateReceiver.WIFI_AP_STATE_FAILED;
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
        return WifiApStateReceiver.WIFI_AP_STATE_FAILED;
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

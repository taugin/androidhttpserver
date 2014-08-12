package com.chukong.apwebauthentication.dns;

import org.join.ws.util.CommonUtil;

import android.util.Log;

public class DnsServer extends Thread {
    private static boolean isShutDown = false;
    public void run() {
        String localAddress = CommonUtil.getSingleton().getLocalIpAddress();
        Log.d("taugin", "localAddress = " + localAddress);
        UDPSocketMonitor monitor = new UDPSocketMonitor(localAddress, 7755);
        monitor.start();
        
        while (!isShutDown) {
            try {
                Thread.sleep(10000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

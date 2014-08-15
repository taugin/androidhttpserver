package org.join.ws.service;

import java.util.Timer;
import java.util.TimerTask;

import org.join.web.serv.R;
import org.join.ws.Constants;
import org.join.ws.Constants.Config;
import org.join.ws.receiver.WSReceiver;
import org.join.ws.serv.WebServer;
import org.join.ws.serv.WebServer.OnWebServListener;
import org.join.ws.ui.WSActivity;
import org.join.ws.ui.WebServActivity;
import org.join.ws.util.CommonUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;

import com.chukong.apwebauthentication.util.Log;

import com.chukong.apwebauthentication.dns.UDPSocketMonitor;

/**
 * @brief Web Service后台
 * @author join
 */
public class WebService extends Service implements OnWebServListener {

    static final String TAG = "WebService";
    static final boolean DEBUG = false || Config.DEV_MODE;

    /** 错误时自动恢复的次数。如果仍旧异常，则继续传递。 */
    private static final int RESUME_COUNT = 3;
    /** 错误时重置次数的时间间隔。 */
    private static final int RESET_INTERVAL = 3000;
    private int errCount = 0;
    private Timer mTimer = new Timer(true);
    private TimerTask resetTask;

    private WebServer webServer;
    private OnWebServListener mListener;

    private boolean isRunning = false;

    private NotificationManager mNM;

    private int NOTI_SERV_RUNNING = R.string.noti_serv_running;

    private UDPSocketMonitor uDPSocketMonitor;
    private LocalBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public WebService getService() {
            return WebService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG)
            Log.d(TAG,
                    String.format("create server: port=%d, root=%s", Config.PORT, Config.WEBROOT));
        String localAddress = CommonUtil.getSingleton().getLocalIpAddress();
        Log.d("taugin", "localAddress = " + localAddress);

        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Log.TAG, "intent = " + intent);
        //openWebServer();
        return mBinder;
    }

    private void openWebServer() {
        if (webServer == null) {
            webServer = new WebServer(Config.PORT, Config.WEBROOT);
            webServer.setOnWebServListener(this);
            webServer.setDaemon(true);
            webServer.start();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(Log.TAG, "intent = " + intent);
        //closeWebServer();
        return true;
    }

    private void closeWebServer() {
        if (webServer != null) {
            webServer.close();
            webServer = null;
        }
    }

    @Override
    public void onDestroy() {
        //closeDnsServer();
        //closeWebServer();
        super.onDestroy();
    }

    @Override
    public void onStarted() {
        if (DEBUG)
            Log.d(TAG, "onStarted");
        showNotification(NOTI_SERV_RUNNING, R.drawable.ic_noti_running);
        if (mListener != null) {
            mListener.onStarted();
        }
        onWebServerStart();
        isRunning = true;
    }

    @Override
    public void onStopped() {
        if (DEBUG)
            Log.d(TAG, "onStopped");
        mNM.cancel(NOTI_SERV_RUNNING);
        if (mListener != null) {
            mListener.onStopped();
        }
        onWebServerStop();
        isRunning = false;
    }

    @Override
    public void onError(int code) {
        if (DEBUG)
            Log.d(TAG, "onError");
        if (code != WebServer.ERR_UNEXPECT) {
            if (mListener != null) {
                mListener.onError(code);
            }
            onWebServerError(code);
            return;
        }
        errCount++;
        restartResetTask(RESET_INTERVAL);
        if (errCount <= RESUME_COUNT) {
            if (DEBUG)
                Log.d(TAG, "Retry times: " + errCount);
            openWebServer();
        } else {
            if (mListener != null) {
                mListener.onError(code);
            }
            errCount = 0;
            cancelResetTask();
        }
    }

    private void cancelResetTask() {
        if (resetTask != null) {
            resetTask.cancel();
            resetTask = null;
        }
    }

    private void restartResetTask(long delay) {
        cancelResetTask();
        resetTask = new TimerTask() {
            @Override
            public void run() {
                errCount = 0;
                resetTask = null;
                if (DEBUG)
                    Log.d(TAG, "ResetTask executed.");
            }
        };
        mTimer.schedule(resetTask, delay);
    }

    @SuppressWarnings("deprecation")
    private void showNotification(int resId, int iconId) {
        CharSequence text = getText(resId);

        Notification notification = new Notification(iconId, text, System.currentTimeMillis());

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                WSActivity.class), 0);

        notification.setLatestEventInfo(this, getText(R.string.app_name), text, contentIntent);
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        mNM.notify(resId, notification);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setOnWebServListener(OnWebServListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Log.TAG, "WebService intent = " + intent);
        if (intent != null) {
            int op = intent.getIntExtra("op", -1);
            Log.d(Log.TAG, "WebService op = " + op);
            if (op == Constants.OP_START_DNSSERVER) {
                openDnsServer();
            } else if (op == Constants.OP_STOP_DNSSERVER) {
                closeDnsServer();
            } else if (op == Constants.OP_START_WEBSERVER) {
                openWebServer();
            } else if (op == Constants.OP_STOP_WEBSERVER) {
                closeWebServer();
            } else if (op == Constants.OP_QUERY_WEBSERVER) {
                onWebServerState();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void openDnsServer() {
        if (uDPSocketMonitor == null) {
            String localAddress = CommonUtil.getSingleton().getLocalIpAddress();
            uDPSocketMonitor = new UDPSocketMonitor(localAddress, 7755);
            uDPSocketMonitor.start();
        }
    }
    public void closeDnsServer() {
        if (uDPSocketMonitor != null) {
            uDPSocketMonitor.close();
            uDPSocketMonitor = null;
        }
    }

    private void onWebServerStart() {
        Intent intent = new Intent(WSReceiver.ACTION_WEBSERVER_START);
        sendBroadcast(intent, WSReceiver.PERMIT_WS_RECEIVER);
    }
    private void onWebServerStop() {
        Intent intent = new Intent(WSReceiver.ACTION_WEBSERVER_STOP);
        sendBroadcast(intent, WSReceiver.PERMIT_WS_RECEIVER);
    }
    private void onWebServerError(int code) {
        Intent intent = new Intent(WSReceiver.ACTION_WEBSERVER_ERROR);
        intent.putExtra("error_code", code);
        sendBroadcast(intent, WSReceiver.PERMIT_WS_RECEIVER);
    }
    private void onWebServerState() {
        Intent intent = new Intent(WSReceiver.ACTION_WEBSERVER_RUNNING);
        intent.putExtra("server_running", isRunning());
        sendBroadcast(intent, WSReceiver.PERMIT_WS_RECEIVER);
    }
}

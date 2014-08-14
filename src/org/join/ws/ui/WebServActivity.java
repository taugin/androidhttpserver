package org.join.ws.ui;

import org.join.ws.serv.WebServer.OnWebServListener;
import org.join.ws.service.WebService;

import com.chukong.apwebauthentication.util.Log;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

/**
 * @brief 绑定Web Service的抽象Activity
 * @author join
 */
public abstract class WebServActivity extends Activity implements OnWebServListener {

    static final String TAG = "WebServActivity";

    protected Intent webServIntent;
    protected WebService webService;
    private boolean isBound = false;

    private ServiceConnection servConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            webService = ((WebService.LocalBinder) service).getService();
            webService.setOnWebServListener(WebServActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            webService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webServIntent = new Intent(this, WebService.class);
    }

    protected boolean isBound() {
        return this.isBound;
    }

    protected void doBindService() {
        Log.d(Log.TAG, "isBound11111 = " + isBound);
        // Restore configs of port and root here.
        PreferActivity.restore(PreferActivity.KEY_SERV_PORT, PreferActivity.KEY_SERV_ROOT);
        bindService(webServIntent, servConnection, 0);
        Log.d(Log.TAG, "isBound22222 = " + isBound);
        isBound = true;
    }

    protected void doUnbindService() {
        Log.d(Log.TAG, "isBound = " + isBound);
        if (isBound) {
            unbindService(servConnection);
            isBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

}

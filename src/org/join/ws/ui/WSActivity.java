package org.join.ws.ui;

import org.join.web.serv.R;
import org.join.ws.Constants;
import org.join.ws.Constants.Config;
import org.join.ws.WSApplication;
import org.join.ws.receiver.OnWsListener;
import org.join.ws.receiver.WSReceiver;
import org.join.ws.serv.WebServer;
import org.join.ws.service.WebService;
import org.join.ws.util.CommonUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.chukong.apwebauthentication.receiver.OnWifiApStateChangeListener;
import com.chukong.apwebauthentication.receiver.WifiApStateReceiver;
import com.chukong.apwebauthentication.service.RedirectSwitch;
import com.chukong.apwebauthentication.util.Log;
import com.chukong.apwebauthentication.wifiap.WifiApManager;

/**
 * @brief 主活动界面
 * @details If you want a totally web server, <a href="https://code.google.com/p/i-jetty/">i-jetty</a> may be your choice.
 * @author join
 */
@SuppressWarnings("deprecation")
public class WSActivity extends WebServActivity implements OnClickListener, OnWsListener, OnWifiApStateChangeListener {

    static final String TAG = "WSActivity";
    static final boolean DEBUG = false || Config.DEV_MODE;

    private CommonUtil mCommonUtil;

    private ToggleButton toggleBtn;
    private ToggleButton toggleBtnAp;
    private ToggleButton toggleBtnRedirect;
    private TextView urlText;
    private TextView wifiApText;

    private String ipAddr;

    private boolean needResumeServer = false;

    private static final int W_START = 0x0101;
    private static final int W_STOP = 0x0102;
    private static final int W_ERROR = 0x0103;

    private static final int DLG_SERV_USELESS = 0x0201;
    private static final int DLG_PORT_IN_USE = 0x0202;
    private static final int DLG_TEMP_NOT_FOUND = 0x0203;
    private static final int DLG_SCAN_RESULT = 0x0204;

    private String lastResult;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case W_START: {
                setUrlText(ipAddr);
                /*
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toggleBtn
                        .getLayoutParams();
                params.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                */
                break;
            }
            case W_STOP: {
                urlText.setText("");
                /*
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toggleBtn
                        .getLayoutParams();
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                */
                break;
            }
            case W_ERROR:
                switch (msg.arg1) {
                case WebServer.ERR_PORT_IN_USE: {
                    showDialog(DLG_PORT_IN_USE);
                    break;
                }
                case WebServer.ERR_TEMP_NOT_FOUND: {
                    showDialog(DLG_TEMP_NOT_FOUND);
                    break;
                }
                case WebServer.ERR_UNEXPECT:
                default:
                    Log.e(TAG, "ERR_UNEXPECT");
                    break;
                }
                doStopClick();
                return;
            }
            toggleBtn.setEnabled(true);
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initObjs(savedInstanceState);
        initViews(savedInstanceState);

        WSApplication.getInstance().startWsService();
        WSReceiver.register(this, this);
        WifiApStateReceiver.register(this, this);
    }

    private void initObjs(Bundle state) {
        mCommonUtil = CommonUtil.getSingleton();
    }

    private void initViews(Bundle state) {
        toggleBtn = (ToggleButton) findViewById(R.id.toggleBtn);
        toggleBtn.setOnClickListener(this);
        urlText = (TextView) findViewById(R.id.urlText);
        wifiApText = (TextView) findViewById(R.id.wifiApssid);

        toggleBtnAp = (ToggleButton) findViewById(R.id.toggleBtnAp);
        toggleBtnAp.setOnClickListener(this);
        toggleBtnAp.setChecked(WifiApManager.getInstance(this).isWifiApEnabled());
        toggleBtnRedirect = (ToggleButton) findViewById(R.id.toggleBtnRedirect);
        boolean redirect = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.REDIRECT_STARTED, false);
        toggleBtnRedirect.setChecked(redirect);
        toggleBtnRedirect.setOnClickListener(this);
        if (state != null) {
            ipAddr = state.getString("ipAddr");
            needResumeServer = state.getBoolean("needResumeServer", false);
            boolean isRunning = state.getBoolean("isRunning", false);
            if (isRunning) {
                toggleBtn.setChecked(true);
                setUrlText(ipAddr);
            }
        }
    }

    private void setUrlText(String ipAddr) {
        String url = "http://" + ipAddr + ":" + Config.PORT + "/";
        urlText.setText(url);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("ipAddr", ipAddr);
        outState.putBoolean("needResumeServer", needResumeServer);
        boolean isRunning = webService != null && webService.isRunning();
        outState.putBoolean("isRunning", isRunning);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (DEBUG)
            Log.d(TAG,
                    newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "ORIENTATION_LANDSCAPE"
                            : "ORIENTATION_PORTRAIT");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WifiApStateReceiver.unregister(this);
        WSReceiver.unregister(this);
        WSApplication.getInstance().stopWsService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_preferences:
            toPreferActivity();
            break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onClick(View v) {
        if (R.id.toggleBtn == v.getId()) {
            boolean isChecked = toggleBtn.isChecked();
            if (isChecked) {
                // 取消验证本地网络
                /*
                if (!isWebServAvailable()) {
                    toggleBtn.setChecked(false);
                    urlText.setText("");
                    showDialog(DLG_SERV_USELESS);
                    return;
                }*/
                doStartClick();
            } else {
                doStopClick();
            }
        } else if (R.id.toggleBtnAp == v.getId()) {
            Log.d(Log.TAG, "toggleBtnAp");
            setWifiApEnabled(toggleBtnAp.isChecked());
        } else if (R.id.toggleBtnRedirect == v.getId()) {
            setRedirect(toggleBtnRedirect.isChecked());
        }
        needResumeServer = false;
    }

    private void doStartClick() {
        ipAddr = mCommonUtil.getLocalIpAddress();
        if (ipAddr == null) {
            toggleBtn.setChecked(false);
            urlText.setText("");
            toast(getString(R.string.info_net_off));
            return;
        }
        toggleBtn.setChecked(true);
        //toggleBtn.setEnabled(false);
        //doBindService();
        Intent intent = new Intent(this, WebService.class);
        intent.putExtra("op", Constants.OP_START_WEBSERVER);
        startService(intent);
    }

    private void doStopClick() {
        toggleBtn.setChecked(false);
        //toggleBtn.setEnabled(false);
        //doUnbindService();
        Intent intent = new Intent(this, WebService.class);
        intent.putExtra("op", Constants.OP_STOP_WEBSERVER);
        startService(intent);
        ipAddr = null;
    }

    private boolean isWebServAvailable() {
        return mCommonUtil.isNetworkAvailable() && mCommonUtil.isExternalStorageMounted();
    }

    @Override
    public void onServAvailable() {
        if (needResumeServer) {
            doStartClick();
            needResumeServer = false;
        }
    }

    @Override
    public void onServUnavailable() {
        if (webService != null && webService.isRunning()) {
            doStopClick();
            needResumeServer = true;
        }
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    // DialogFragment needs android-support.jar in API-8.
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
        case DLG_SERV_USELESS:
            return createConfirmDialog(android.R.drawable.ic_dialog_info,
                    R.string.tit_serv_useless, R.string.msg_serv_useless, null);
        case DLG_PORT_IN_USE:
            return createConfirmDialog(android.R.drawable.ic_dialog_info, R.string.tit_port_in_use,
                    R.string.msg_port_in_use, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toPreferActivity();
                        }
                    });
        case DLG_TEMP_NOT_FOUND:
            return createConfirmDialog(android.R.drawable.ic_dialog_info,
                    R.string.tit_temp_not_found, R.string.tit_temp_not_found,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toPreferActivity();
                        }
                    });
        case DLG_SCAN_RESULT:
            AlertDialog dialog = createConfirmDialog(android.R.drawable.ic_dialog_info,
                    R.string.tit_scan_result, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            copy2Clipboard(lastResult);
                        }
                    });
            dialog.setMessage(lastResult);
            return dialog;
        }
        return super.onCreateDialog(id, args);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        switch (id) {
        case DLG_SCAN_RESULT:
            ((AlertDialog) dialog).setMessage(lastResult);
            break;
        }
        super.onPrepareDialog(id, dialog, args);
    }

    private AlertDialog createConfirmDialog(int iconId, int titleId, int messageId,
            DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (iconId > 0)
            builder.setIcon(iconId);
        if (titleId > 0)
            builder.setTitle(titleId);
        if (messageId > 0)
            builder.setMessage(messageId);
        builder.setPositiveButton(android.R.string.ok, positiveListener);
        return builder.create();
    }

    private void toPreferActivity() {
        try {
            Intent intent = new Intent(this, PreferActivity.class);
            intent.putExtra("isRunning", webService == null ? false : webService.isRunning());
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void copy2Clipboard(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setText(text);
    }

    private void setWifiApEnabled(boolean enabled) {
        WifiApManager.getInstance(this).setSoftApEnabled(null, enabled);
    }

    @Override
    public void onWifiApStateChanged(int state) {
        if (state == WifiApStateReceiver.WIFI_AP_STATE_DISABLING || state == WifiApStateReceiver.WIFI_AP_STATE_ENABLING) {
            toggleBtnAp.setEnabled(false);
        } else if(state == WifiApStateReceiver.WIFI_AP_STATE_DISABLED || state == WifiApStateReceiver.WIFI_AP_STATE_ENABLED) {
            toggleBtnAp.setEnabled(true);
        }
        if (state == WifiApStateReceiver.WIFI_AP_STATE_ENABLED) {
            WifiConfiguration config = WifiApManager.getInstance(this).getWifiApConfiguration();
            if (config != null) {
                wifiApText.setText("ssid : " + config.SSID + "\n" + config.preSharedKey);
            }
        } else if (state == WifiApStateReceiver.WIFI_AP_STATE_DISABLED) {
            wifiApText.setText("");
        }
    }
    private void setRedirect(boolean redirect) {
        Log.d(Log.TAG, "CommonUtil.isRooted() = " + CommonUtil.isRooted());
        boolean wifiApEnabled = WifiApManager.getInstance(this).isWifiApEnabled();
        if (redirect && !wifiApEnabled) {
            Toast.makeText(this, "建议先开启WifiAp", Toast.LENGTH_SHORT).show();
            toggleBtnRedirect.setChecked(false);
            return ;
        }
        if (CommonUtil.isRooted()) {
            boolean result = RedirectSwitch.getInstance(this).setRedirectState(redirect);
            toggleBtnRedirect.setChecked(result ? redirect : false);
            if (result && redirect) {
                Intent intent = new Intent(this, WebService.class);
                intent.putExtra("op", Constants.OP_START_DNSSERVER);
                startService(intent);
            } else if (result && !redirect) {
                Intent intent = new Intent(this, WebService.class);
                intent.putExtra("op", Constants.OP_STOP_DNSSERVER);
                startService(intent);
            } else {
                
            }
        } else {
            toggleBtnRedirect.setChecked(false);
        }
    }

    @Override
    public void onWebServerStart() {
        mHandler.sendEmptyMessage(W_START);
    }

    @Override
    public void onWebServerStop() {
        mHandler.sendEmptyMessage(W_STOP);
    }

    @Override
    public void onWebServerError(int code) {
        Message msg = mHandler.obtainMessage(W_ERROR);
        msg.arg1 = code;
        mHandler.sendMessage(msg);
    }
}
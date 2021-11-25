package ru.sanddev.rfidservice;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ControlRfidService extends AppCompatActivity {

    // UI elements
    private TextView elementServiceStatus;
    private TextView elementDeviceStatus;
    private TextView elementTextRfid;

    private Boolean isServiceCall = false;
    private RfidSettings params;

    // Inner broadcast receiver class
    private Receiver receiver;

    private class Receiver extends BroadcastReceiver {
        private Boolean isRegistered = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case RfidService.BROADCAST_ANSWER_SERVICE_STATUS:
                    String serviceStatus = intent.getStringExtra("status");
                    onReceiveServiceStatus(serviceStatus);
                    break;

                case RfidService.BROADCAST_ANSWER_DEVICE_STATUS:
                    String deviceStatus = intent.getStringExtra("status");
                    onReceiveDeviceStatus(deviceStatus);
                    break;

                case RfidService.BROADCAST_ANSWER_TAGDATA:
                    String tagData = intent.getStringExtra("data");
                    elementTextRfid.setText(tagData);
                    break;

                case RfidService.BROADCAST_ANSWER_INFO:
                    String text = intent.getStringExtra("text");
                    elementTextRfid.setText(text);
                    break;
                default:
            }

        }

        // Interface methods
        public void Register() {
            if (!isRegistered) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(RfidService.BROADCAST_ANSWER_SERVICE_STATUS);
                intentFilter.addAction(RfidService.BROADCAST_ANSWER_DEVICE_STATUS);
                intentFilter.addAction(RfidService.BROADCAST_ANSWER_INFO);
                intentFilter.addAction(RfidService.BROADCAST_ANSWER_TAGDATA);
                registerReceiver(this, intentFilter);
            }
            isRegistered = true;
        }
        public void UnRegister() {
            if (isRegistered)
                unregisterReceiver(this);
            isRegistered = false;
        }
    }

    protected void onReceiveServiceStatus(String status) {
        elementServiceStatus.setText(status);

        switch(status) {
            case "online":
                elementServiceStatus.setTextColor(getColor(R.color.TextSuccess));
                Toast.makeText(this, getString(R.string.TextDeviceConnected), Toast.LENGTH_SHORT).show();
                break;
            case "offline":
                elementServiceStatus.setTextColor(getColor(R.color.TextError));
                Toast.makeText(this, getString(R.string.TextDeviceDisconnected), Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        }
    }
    protected void onReceiveDeviceStatus(String status) {
        elementDeviceStatus.setText(status);

        switch(status) {
            case "connected":
                elementDeviceStatus.setTextColor(getColor(R.color.TextSuccess));
                Toast.makeText(this, getString(R.string.TextDeviceConnected), Toast.LENGTH_SHORT).show();
                break;
            case "not connected":
                elementDeviceStatus.setTextColor(getColor(R.color.TextError));
                Toast.makeText(this, getString(R.string.TextDeviceDisconnected), Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        }
    }

    // Service methods
    private void startService() {
        if (RfidService.isStarted()) {
            Toast.makeText(this, "Сервис уже запущен", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, RfidService.class);
        startForegroundService(intent);
    }
    private void stopService() {
        Intent intent = new Intent(this, RfidService.class);
        stopService(intent);
    }
    private void checkRfidService() {
        ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50);
        String serviceName = RfidService.class.getName();

        for (int i=0; i<rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            if(serviceName.equalsIgnoreCase(rsi.service.getClassName())){
                onReceiveServiceStatus("online");
                return;
            }
        }
        onReceiveServiceStatus("offline");
    }
    private void getDeviceStatus() {
        Intent intent = new Intent();
        intent.setAction(RfidService.BROADCAST_ACTION);
        intent.putExtra("action", "GetDeviceStatus");
        sendBroadcast(intent);
    }
    private void sendParams() {
        Intent intent = new Intent();
        intent.setAction(RfidService.BROADCAST_ACTION);
        intent.putExtra("action", "SetParams");
        intent.putExtra("params", params.getJSON());
        sendBroadcast(intent);
    }

    // Constructor & destructor
    public ControlRfidService() {
        receiver = new Receiver();
        params = new RfidSettings();
    }

    // Parrent methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_rfid_service);

        // UI
        elementServiceStatus = findViewById(R.id.ServiceStatus);
        elementDeviceStatus = findViewById(R.id.DeviceStatus);
        elementTextRfid = findViewById(R.id.TextRfid);

        elementServiceStatus.setText("");
        elementDeviceStatus.setText("");
        elementTextRfid.setText("");

        // Check calling source
        Intent intent = getIntent();
        isServiceCall = intent.getBooleanExtra("IsServiceCall", false);
        if (isServiceCall) {
            // Init
            checkRfidService();
            getDeviceStatus();
        }
        else {
            startService();
        }
    }

    @Override
    protected void onDestroy() {
        receiver.UnRegister();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isServiceCall) {
            receiver.Register();
        }
        else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        receiver.UnRegister();
    }

    // Interface methods
    public void onButtonStartServiceClick(View view){
        startService();
    }
    public void onButtonStopServiceClick(View view){
        stopService();
    }
    public void onButtonGetInfoClick(View view) {
        Intent intent = new Intent();
        intent.setAction(RfidService.BROADCAST_ACTION);
        intent.putExtra("action", "GetInfo");
        sendBroadcast(intent);

        elementTextRfid.setText("");
    }
    public void onButtonConnectClick(View view) {
        Intent intent = new Intent();
        intent.setAction(RfidService.BROADCAST_ACTION);
        intent.putExtra("action", "Connect");
        sendBroadcast(intent);
    }
    public void onButtonDisconnectClick(View view) {
        Intent intent = new Intent();
        intent.setAction(RfidService.BROADCAST_ACTION);
        intent.putExtra("action", "Disconnect");
        sendBroadcast(intent);
    }

    // Other methods

}
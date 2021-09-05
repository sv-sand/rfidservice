package ru.sanddev.rfidservice;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ControlRfidService extends AppCompatActivity {

    private Receiver receiver;

    // UI elements
    private TextView elementServiceStatus;
    private TextView elementDeviceStatus;
    private TextView elementTextRfid;

    // Construtors & destructors
    public ControlRfidService (){
        receiver = new Receiver();
    }

    // Inner broadcast receiver class
    private class Receiver extends BroadcastReceiver {
        public static final String BROADCAST_ACTION = "ru.sanddev.rfidservice.answer";
        private Boolean isRegistered=false;

        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            switch (type){
                case "ServiceStatus":
                    String serviceStatus = intent.getStringExtra("status");
                    elementServiceStatus.setText(serviceStatus);
                    break;

                case "DeviceStatus":
                    String deviceStatus = intent.getStringExtra("status");
                    elementDeviceStatus.setText(deviceStatus);
                    break;

                case "TagData":
                    String tagData = intent.getStringExtra("data");
                    elementTextRfid.setText(tagData);
                    break;

                case "Info":
                    String text = intent.getStringExtra("text");
                    elementTextRfid.setText(text);
                    break;
                default:
            }

        }

        // Interface methods
        public void Register() {
            if (!isRegistered)
                registerReceiver(this, new IntentFilter(BROADCAST_ACTION));
            isRegistered = true;
        }
        public void UnRegister() {
            if (isRegistered)
                unregisterReceiver(this);
            isRegistered = false;
        }
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

        // Init
        receiver.Register();
        checkRfidService();
    }

    @Override
    protected void onDestroy() {
        receiver.UnRegister();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        receiver.Register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        receiver.UnRegister();
    }

    // Interface methods
    public void onButtonStartServiceClick(View view){
        Intent intent = new Intent(this, RfidService.class);
        //startService(intent);
        startForegroundService(intent);

//        Intent intent = new Intent();
//        intent.setAction("ru.sanddev.rfidservice.START");
//        sendBroadcast(intent);

    }
    public void onButtonStopServiceClick(View view){
        Intent intent = new Intent(ControlRfidService.this, RfidService.class);
        stopService(intent);
    }
    public void onButtonGetInfoClick(View view) {
        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.action");
        intent.putExtra("action", "GetInfo");
        sendBroadcast(intent);

        elementTextRfid.setText("");
    }
    public void onButtonConnectClick(View view) {
        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.action");
        intent.putExtra("action", "Connect");
        sendBroadcast(intent);
    }
    public void onButtonDisconnectClick(View view) {
        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.action");
        intent.putExtra("action", "Disconnect");
        sendBroadcast(intent);
    }

    // Other methods
    private void checkRfidService() {
        ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50);
        String serviceName = RfidService.class.getName();

        for (int i=0; i<rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            if(serviceName.equalsIgnoreCase(rsi.service.getClassName())){
                elementServiceStatus.setText("online");
                return;
            }
        }
        elementServiceStatus.setText("offline");
    }

    private void getDeviceStatus() {
        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.action");
        intent.putExtra("action", "GetDeviceStatus");
        sendBroadcast(intent);
    }

}
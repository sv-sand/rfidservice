package ru.sanddev.rfidservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ControlRfidService extends AppCompatActivity {

    private Receiver receiver;

    // UI elements
    private TextView elementServiceStatus;
    private TextView elementReaderStatus;
    private TextView elementTextRfid;

    // Construtors & destructors
    public ControlRfidService (){
        receiver = new Receiver();
    }

    // Inner broadcast receiver class
    private class Receiver extends BroadcastReceiver {
        public static final String BROADCAST_ACTION = "ru.sanddev.rfidservice.answer";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            switch (action){
                case "getStatus":
                case "statusChange":
                    String status = intent.getStringExtra("status");
                    elementReaderStatus.setText(status);
                    break;

                case "tagData":
                    String tagData = intent.getStringExtra("tagData");
                    elementTextRfid.setText(tagData);
                    break;

                case "getInfo":
                    String text = intent.getStringExtra("text");
                    elementTextRfid.setText(text);
                    break;
            }

        }
        public void Register() {
            registerReceiver(this, new IntentFilter(BROADCAST_ACTION));
        }
        public void UnRegister() {
            unregisterReceiver(receiver);
        }
    }

    // Parrent methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_rfid_service);

        receiver.Register();

        // UI
        elementServiceStatus = findViewById(R.id.ServiceStatus);
        elementReaderStatus = findViewById(R.id.ReaderStatus);
        elementTextRfid = findViewById(R.id.TextRfid);
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
        Intent intent = new Intent(ControlRfidService.this, RfidService.class);
        startService(intent);
    }
    public void onButtonStopServiceClick(View view){
        Intent intent = new Intent(ControlRfidService.this, RfidService.class);
        stopService(intent);
    }
    public void onButtonGetInfoClick(View view) {
        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.action");
        intent.putExtra("action", "getInfo");
        sendBroadcast(intent);
    }
    public void onButtonConnectClick(View view) {
        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.action");
        intent.putExtra("action", "connect");
        sendBroadcast(intent);
    }
    public void onButtonDisconnectClick(View view) {
        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.action");
        intent.putExtra("action", "disconnect");
        sendBroadcast(intent);
    }
}
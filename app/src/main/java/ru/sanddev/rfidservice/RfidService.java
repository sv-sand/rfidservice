package ru.sanddev.rfidservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.zebra.rfid.api3.TagData;

public class RfidService extends Service implements RFIDHandler.ResponseHandlerInterface {

    private static final String BROADCAST_ACTION = "ru.sanddev.rfidservice.action";
    private static final String BROADCAST_ANSWER = "ru.sanddev.rfidservice.answer";
    private static final String INFO_NAME = "Zebra RFD8500 control service";
    private static final String INFO_VERSION = "1.0";
    private static final String INFO_AUTHOR = "Sand, http://sanddev.ru";

    private RFIDHandler rfidHandler;
    private Receiver receiver;
    private String tagData;

    // Construtors & destructors
    public RfidService() {
        receiver = new Receiver();
        rfidHandler = new RFIDHandler();
    }

    // Parrent methods
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        receiver.Register();
        rfidHandler.onCreate(this);
        rfidHandler.Connect();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        receiver.UnRegister();
        rfidHandler.Disconnect();
        rfidHandler.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    // RFIDHandler methods
    @Override
    public void handleTagdata(TagData[] tag) {
        final StringBuilder sb = new StringBuilder();
        for (int index = 0; index < tag.length; index++) {
            sb.append(tag[index].getTagID() + "\n");
        }
        tagData += sb.toString();
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            tagData = "";
            rfidHandler.performInventory();
        }
        else {
            rfidHandler.stopInventory();
            sendData();
        }
    }

    @Override
    public void handleStatusEvents(String status) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_ANSWER);
        intent.putExtra("action", "statusChange");
        intent.putExtra("status", status);
        sendBroadcast(intent);
    }

    // Inner broadcast receiver class
    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            switch(action){
                case ("getInfo"):       getInfo();         break;
                case ("getStatus"):     getStatus();       break;
                case ("connect"):       Connect();         break;
                case ("disconnect"):    Disconnect();      break;
            }
        }
        public void Register() {
            registerReceiver(this, new IntentFilter(BROADCAST_ACTION));
        }
        public void UnRegister() {
            unregisterReceiver(receiver);
        }
    }

    private void getInfo() {
        String text = INFO_NAME + "\n" + "version " + INFO_VERSION + "\n" + INFO_AUTHOR;

        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.answer");
        intent.putExtra("action", "getInfo");
        intent.putExtra("name", INFO_NAME);
        intent.putExtra("version", INFO_VERSION);
        intent.putExtra("author", INFO_AUTHOR);
        intent.putExtra("text", text);
        sendBroadcast(intent);
    }

    private void getStatus() {
        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.answer");
        intent.putExtra("action", "getStatus");
        intent.putExtra("status", rfidHandler.getStatus());
        sendBroadcast(intent);
    }

    private void sendData() {
        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.answer");
        intent.putExtra("action", "tagData");
        intent.putExtra("tagData", tagData);
        sendBroadcast(intent);

        tagData = "";
    }

    private void Connect() {
        rfidHandler.Connect();
    }

    private void Disconnect() {
        rfidHandler.Disconnect();
    }
}
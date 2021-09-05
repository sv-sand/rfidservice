package ru.sanddev.rfidservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import com.zebra.rfid.api3.TagData;

public class RfidService extends Service implements RFIDHandler.ResponseHandlerInterface {

    private static final String BROADCAST_ACTION = "ru.sanddev.rfidservice.action";
    private static final String BROADCAST_ANSWER = "ru.sanddev.rfidservice.answer";
    private static final String INFO_NAME = "Zebra RFD8500 control service";
    private static final String INFO_VERSION = "1.0";
    private static final String INFO_AUTHOR = "Sand, http://sanddev.ru";

    // Notification
    private static final Integer NOTIFICATION_CHANNEL_ID = 100;
    private static final String NOTIFICATION_TITLE = "RFID сервис";
    private static final String NOTIFICATION_TEXT = "Не закрывайте сервис";

    private static Boolean isStarted = false;

    private RFIDHandler rfidHandler;
    private Receiver receiver;
    private String tagData;

    // Construtors & destructors
    public RfidService() {
        if (isStarted) return;

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
        if (isStarted) return;

        receiver.Register();
        rfidHandler.onCreate(this);
        rfidHandler.Connect();

        sendServiceStatus("online");

        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "Service is running", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        receiver.UnRegister();
        rfidHandler.Disconnect();
        rfidHandler.onDestroy();

        sendServiceStatus("offline");

        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "Service is stopped", Toast.LENGTH_SHORT).show();
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isStarted) {
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        isStarted = true;

        CreateNotification();

        return super.onStartCommand(intent, flags, startId);
    }

    // Inner broadcast class
    private class Receiver extends BroadcastReceiver {
        private Boolean isRegistered=false;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            switch(action){
                case "GetServiceStatus":
                    sendServiceStatus("online");
                    break;
                case "GetInfo":
                    sendInfo();
                    break;
                case "GetDeviceStatus":
                    sendDeviceStatus();
                    break;
                case "Connect":
                    Connect();
                    break;
                case "Disconnect":
                    Disconnect();
                    break;
                case "StopService":
                    stopForeground(true);
                    stopSelf();
                    break;
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
                unregisterReceiver(receiver);
            isRegistered = false;
        }
    }

    // Service foreground methods
    private void CreateNotification() {
        final String STRING_CHANNEL_ID = String.valueOf(NOTIFICATION_CHANNEL_ID);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(STRING_CHANNEL_ID, NOTIFICATION_TITLE, NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(notificationChannel);

        Notification notification = new Notification.Builder(this, STRING_CHANNEL_ID)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(NOTIFICATION_TEXT)
                .build();

        startForeground(NOTIFICATION_CHANNEL_ID, notification);
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
        intent.putExtra("type", "DeviceStatus");
        intent.putExtra("status", status);
        sendBroadcast(intent);
    }

    // Other methods
    private void sendServiceStatus(String status) {
        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.answer");
        intent.putExtra("type", "ServiceStatus");
        intent.putExtra("status", status);
        sendBroadcast(intent);
    }

    private void sendInfo() {
        String text = INFO_NAME + "\n" + "version " + INFO_VERSION + "\n" + INFO_AUTHOR;

        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.answer");
        intent.putExtra("type", "Info");
        intent.putExtra("name", INFO_NAME);
        intent.putExtra("version", INFO_VERSION);
        intent.putExtra("author", INFO_AUTHOR);
        intent.putExtra("text", text);
        sendBroadcast(intent);
    }

    private void sendDeviceStatus() {
        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.answer");
        intent.putExtra("type", "DeviceStatus");
        intent.putExtra("status", rfidHandler.getStatus());
        sendBroadcast(intent);
    }

    private void sendData() {
        Intent intent = new Intent();
        intent.setAction("ru.sanddev.rfidservice.answer");
        intent.putExtra("type", "TagData");
        intent.putExtra("data", tagData);
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
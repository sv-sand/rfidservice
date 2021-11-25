package ru.sanddev.rfidservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.zebra.rfid.api3.TagData;

public class RfidService extends Service implements RFIDHandler.ResponseHandlerInterface {

    public static final String BROADCAST_ACTION = "ru.sanddev.rfidservice.action";
    public static final String BROADCAST_ANSWER_SERVICE_STATUS = "ru.sanddev.rfidservice.answer.ServiceStatus";
    public static final String BROADCAST_ANSWER_DEVICE_STATUS = "ru.sanddev.rfidservice.answer.DeviceStatus";
    public static final String BROADCAST_ANSWER_INFO = "ru.sanddev.rfidservice.answer.Info";
    public static final String BROADCAST_ANSWER_TAGDATA = "ru.sanddev.rfidservice.answer.TagData";
    private static final String INFO_NAME = "Zebra RFD8500 control service";
    private static final String INFO_VERSION = "1.0";
    private static final String INFO_AUTHOR = "Sand, http://sanddev.ru";

    private static Boolean mIsStarted = false;
    private RfidSettings params;

    private RFIDHandler rfidHandler;
    private String tagData="";
    private int tagCount=0;

    // Notification
    public static final Integer NOTIFICATION_CHANNEL_ID = 100;

    // Inner broadcast class
    private Receiver receiver;

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
                    sendDeviceStatus(rfidHandler.getStatus());
                    break;
                case "SetParams":
                    String paramsString = intent.getStringExtra("params");
                    setParams(paramsString);
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

    // Construtors & destructors
    public RfidService() {
        receiver = new Receiver();
        rfidHandler = new RFIDHandler();
        params = new RfidSettings();
    }

    // Getters & setters
    public static boolean isStarted() {
        return mIsStarted;
    }

    // Notification
    private void CreateNotification() {
        final String STRING_CHANNEL_ID = String.valueOf(RfidService.NOTIFICATION_CHANNEL_ID);

        // Create a notificationNotificationManager
        String channelTitle = getString(R.string.NotificationTitle);

        NotificationChannel notificationChannel = new NotificationChannel(
                STRING_CHANNEL_ID,
                channelTitle,
                NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);

        // Create notification
        String notificationTitle = getString(R.string.NotificationTitle);
        String notificationText = getString(R.string.NotificationText);

        PendingIntent piLaunchMainActivity = getLaunchActivityPI();
        Notification notification = new Notification.Builder(this, STRING_CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(piLaunchMainActivity)
                .build();

        startForeground(RfidService.NOTIFICATION_CHANNEL_ID, notification);
    }

    private PendingIntent getLaunchActivityPI() {

        Intent intent = new Intent(this, ControlRfidService.class);
        intent.putExtra("IsServiceCall", true);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent getStopServicePI() {
        PendingIntent pendingIntent;

        Intent intent = new Intent(this, RfidService.class);
        pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        return pendingIntent;
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

        sendServiceStatus("online");
    }

    @Override
    public void onDestroy() {
        receiver.UnRegister();
        rfidHandler.Disconnect();
        rfidHandler.onDestroy();

        sendServiceStatus("offline");

        mIsStarted = false;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CreateNotification();

        mIsStarted = true;

        return super.onStartCommand(intent, flags, startId);
    }

    // RFIDHandler methods
    @Override
    public void handleTagdata(TagData[] tag) {
        final StringBuilder sb = new StringBuilder();
        for (int index = 0; index < tag.length; index++) {
            sb.append(tag[index].getTagID() + "\n");
            tagCount += 1;
        }
        tagData += sb.toString();
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            tagData = "";
            tagCount = 0;
            rfidHandler.performInventory();
        }
        else {
            rfidHandler.stopInventory();
            sendData();
        }
    }

    @Override
    public void handleStatusEvents(String status) {
        sendDeviceStatus(status);
    }

    // Other methods
    private void sendServiceStatus(String status) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_ANSWER_SERVICE_STATUS);
        intent.putExtra("status", status);
        sendBroadcast(intent);
    }

    private void sendInfo() {
        String text = INFO_NAME + "\n" + "version " + INFO_VERSION + "\n" + INFO_AUTHOR;

        Intent intent = new Intent();
        intent.setAction(BROADCAST_ANSWER_INFO);
        intent.putExtra("name", INFO_NAME);
        intent.putExtra("version", INFO_VERSION);
        intent.putExtra("author", INFO_AUTHOR);
        intent.putExtra("text", text);
        sendBroadcast(intent);
    }

    private void sendDeviceStatus(String status) {
        String statusLC = status.toLowerCase();
        if(statusLC.equals("connected") | statusLC.equals("not connected")) {
            status = statusLC;
        }

        Intent intent = new Intent();
        intent.setAction(BROADCAST_ANSWER_DEVICE_STATUS);
        intent.putExtra("status", status);
        sendBroadcast(intent);
    }

    private void sendData() {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_ANSWER_TAGDATA);
        intent.putExtra("data", tagData);
        sendBroadcast(intent);

        tagData = "";
        tagCount = 0;
    }

    private void Connect() {
        rfidHandler.Connect();
    }

    private void Disconnect() {
        rfidHandler.Disconnect();
    }

    private void setParams(String paramsString) {
        if(params==null)
            return;
        params.setJson(paramsString);
    }

}
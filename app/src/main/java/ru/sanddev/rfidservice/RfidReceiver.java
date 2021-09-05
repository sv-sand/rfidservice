package ru.sanddev.rfidservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class RfidReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show();
        Log.d("TAG", "________________________ Hello");
        context.startService(new Intent(context, RfidService.class));
    }
}
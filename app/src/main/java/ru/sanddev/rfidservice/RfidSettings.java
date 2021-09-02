package ru.sanddev.rfidservice;

import android.content.Context;
import android.content.SharedPreferences;

public class RfidSettings {
    private Context appContext;
    public static final String PREFERENCE_NAME = "ServicePreference";
    private SharedPreferences settings;

    // Preference set
    public String DeviceName;

    // Construtors & destructors
    public RfidSettings(Context context) {
        this.appContext = context;
        Read();
    }

    // Methods
    public void Read() {
        settings = appContext.getSharedPreferences(PREFERENCE_NAME, appContext.MODE_PRIVATE);
        DeviceName = settings.getString("DeviceName", "");
    }

    public void Write() {
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("DeviceName", DeviceName);

        editor.apply();
    }

}

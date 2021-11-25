package ru.sanddev.rfidservice;

import org.json.JSONException;
import org.json.JSONObject;

public class RfidSettings {

    private boolean showToast = false;

    // Constructor & destructor

    // Getters & setters
    public boolean getShowToast() {
        return showToast;
    }
    public void setShowToast(boolean value) {
        showToast = value;
    }

    // JSON
    public String getJSON() {
        JSONObject paramsJson = new JSONObject();

        try {
            paramsJson.put("ShowToast", showToast);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return paramsJson.toString();
    }
    public void setJson(String paramsString) {
        try {
            JSONObject paramsJson = new JSONObject(paramsString);
            showToast = paramsJson.getBoolean("ShowToast");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

package com.worldhelper.covidtracker.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LocalVariables {

    private static String trimmedLat = "";
    private static String trimmedLng = "";

    public static void setTrimmedLat(String trimmedLat) {
        LocalVariables.trimmedLat = trimmedLat;
    }

    public static void setTrimmedLng(String trimmedLng) {
        LocalVariables.trimmedLng = trimmedLng;
    }

    public static String getTrimmedLat() {
        return trimmedLat;
    }

    public static String getTrimmedLng() {
        return trimmedLng;
    }

    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }
}

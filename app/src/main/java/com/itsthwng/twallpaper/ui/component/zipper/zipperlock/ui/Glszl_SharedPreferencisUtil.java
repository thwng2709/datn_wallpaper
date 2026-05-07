package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui;

import android.content.Context;
import android.content.SharedPreferences;

public class Glszl_SharedPreferencisUtil {
    SharedPreferences packagePrefs;
    SharedPreferences.Editor prefsEditor;

    public Glszl_SharedPreferencisUtil(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), 0);
        this.packagePrefs = sharedPreferences;
        this.prefsEditor = sharedPreferences.edit();
    }

    public void setShowAd(int i) {
        this.prefsEditor.putInt("showInterstitial", i).commit();
        this.prefsEditor.apply();
    }

    public int getShowAd() {
        SharedPreferences sharedPreferences = this.packagePrefs;
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("showInterstitial", 0);
        }
        return 0;
    }


    public void setPinIsActive(boolean z) {
        this.prefsEditor.putBoolean("pinIsActive", z).commit();
        this.prefsEditor.apply();
    }

    public boolean getPinIsActive() {
        SharedPreferences sharedPreferences = this.packagePrefs;
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean("pinIsActive", false);
        }
        return false;
    }

    public void setSecurityQuestionIsActive(boolean z) {
        this.prefsEditor.putBoolean("securityQuestionIsActive", z).commit();
        this.prefsEditor.apply();
    }

    public boolean getSecurityQuestionIsActive() {
        SharedPreferences sharedPreferences = this.packagePrefs;
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean("securityQuestionIsActive", false);
        }
        return false;
    }

    public void setPin(String str) {
        this.prefsEditor.putString("pinCode", str).commit();
        this.prefsEditor.apply();
    }

    public String getPin() {
        SharedPreferences sharedPreferences = this.packagePrefs;
        if (sharedPreferences != null) {
            return sharedPreferences.getString("pinCode", null);
        }
        return null;
    }

    public void setSecurityQuestion(String str) {
        this.prefsEditor.putString("securityQuestion", str).commit();
        this.prefsEditor.apply();
    }

    public String getSecurityQuestion() {
        SharedPreferences sharedPreferences = this.packagePrefs;
        if (sharedPreferences != null) {
            return sharedPreferences.getString("securityQuestion", "");
        }
        return "";
    }

    public void setSequrityQIndex(int i) {
        this.prefsEditor.putInt("sequrityQIndex", i).commit();
        this.prefsEditor.apply();
    }

    public int getSequrityQIndex() {
        SharedPreferences sharedPreferences = this.packagePrefs;
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("sequrityQIndex", 0);
        }
        return 0;
    }
}

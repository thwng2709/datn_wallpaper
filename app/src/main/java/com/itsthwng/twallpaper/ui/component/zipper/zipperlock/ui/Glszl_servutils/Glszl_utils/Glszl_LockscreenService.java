package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_servutils.Glszl_utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import com.itsthwng.twallpaper.R;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_GameAdapter;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ZipLockMainActivity;


public class Glszl_LockscreenService extends Service {
    public static Glszl_LockscreenService heart_zipper_instance;
    public static BroadcastReceiver heart_zipper_mReceiver;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        heart_zipper_instance = this;
    }

    public static void Start(Context context) {
        if (heart_zipper_instance != null) return;

        Context appCtx = context.getApplicationContext();
        Intent intent = new Intent(appCtx, Glszl_LockscreenService.class);

        if (Build.VERSION.SDK_INT >= 26) {
            androidx.core.content.ContextCompat.startForegroundService(appCtx, intent);
        } else {
            appCtx.startService(intent);
        }
    }

    @SuppressLint("ForegroundServiceType")
    public int onStartCommand(Intent intent, int i, int i2) {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
//        Glszl_LockscreenIntentReceiver lockscreenIntentReceiver = new Glszl_LockscreenIntentReceiver();
//        heart_zipper_mReceiver = lockscreenIntentReceiver;
//        registerReceiver(lockscreenIntentReceiver, intentFilter);
        startForeground();
        return Service.START_STICKY;
    }

    @SuppressLint("ForegroundServiceType")
    private void startForeground() {
        Intent intent = new Intent(this, ZipLockMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startForeground(9999,
                new NotificationCompat.Builder(this)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setTicker(getResources().getString(R.string.app_name))
                        .setContentText(getResources().getString(R.string.service_running))
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentIntent(
                                PendingIntent.getActivity(this, 0,
                                       intent, PendingIntent.FLAG_UPDATE_CURRENT
                                )
                        )
                        .setOngoing(true).build());
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(heart_zipper_mReceiver);
    }

    public void finish() {
        Glszl_GameAdapter.close();
        WindowManager windowManager = (WindowManager) heart_zipper_instance.getSystemService(Context.WINDOW_SERVICE);
        stopForeground(true);
        stopSelf();
        heart_zipper_instance = null;
    }
}

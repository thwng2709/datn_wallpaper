package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.itsthwng.twallpaper.R;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_GameAdapter;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ZipLockMainActivity;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_servutils.Glszl_utils.Glszl_LockscreenService;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.PrefKey;
import com.itsthwng.twallpaper.utils.Logger;

public class Glszl_LiveService extends Service {
    public static Glszl_LiveService service;
    private boolean receiverRegistered = false;
    BroadcastReceiver mybroadcast = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            @SuppressLint("WrongConstant") KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService("keyguard");
            if (action.equals("android.intent.action.SCREEN_OFF")) {
                // Handle preview mode first
                if(Glszl_LockScreenService.IsPreview){
                    if (Glszl_GameAdapter.notNugat) {
                        Glszl_LockScreenService service1 = Glszl_LockScreenService.f10546cc;
                        if(service1 != null) try {
                            service1.finish();
                        } catch (Exception ignored) {
                            System.out.println("Finish preview error" + ignored.getMessage());
                        }
                    } else {
                        Glszl_LockscreenService service1 = Glszl_LockscreenService.heart_zipper_instance;
                        if(service1 != null) try {
                            service1.finish();
                        } catch (Exception ignored) {
                            System.out.println("Finish preview error" + ignored.getMessage());
                        }
                    }
                    Log.e("finish!!!123", "run: finish game adapter");
                    Glszl_GameAdapter.Inicialed = false;
                }
                
                // Check if Zipper lock screen is enabled
                boolean zipperEnabled = Glszl_UserDataAdapter.LoadPref(PrefKey.ACTIVE, context) == 1;
                Glszl_SharedPreferencisUtil sharedPrefs = new Glszl_SharedPreferencisUtil(context);
                boolean pinEnabled = sharedPrefs.getPinIsActive();

                if (zipperEnabled) {
                    // Check if service is not already running (f10546cc would be null after unlock)
                    if (Glszl_LockScreenService.f10546cc == null) {
                        // Delay to prevent screen wake issues
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            // Re-check conditions before starting
                            if (Glszl_LockScreenService.f10546cc == null) {
                                Log.d("LiveService", "Starting LockScreenService on SCREEN_OFF");
                                Glszl_LockScreenService.Start(Glszl_LiveService.this);
                            }
                        }, 300); // Shorter delay to improve UX
                    } else {
                        Log.d("LiveService", "LockScreenService already running");
                    }
                } else {
                    // Zipper is disabled, let system handle the lock screen
                    Log.d("LiveService", "Zipper disabled, using system lock");
                }
                return;
            }
            else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                // DO NOT start Zipper when screen turns ON to prevent auto wake-up issue
                // Zipper should only be started when screen turns OFF
                // This prevents the screen from automatically turning back on after timeout

                // Log current state for debugging
                boolean zipperEnabled = Glszl_UserDataAdapter.LoadPref(PrefKey.ACTIVE, context) == 1;
                Glszl_SharedPreferencisUtil sharedPrefs = new Glszl_SharedPreferencisUtil(context);
                boolean pinEnabled = sharedPrefs.getPinIsActive();
                boolean serviceRunning = Glszl_LockScreenService.f10546cc != null;

                Log.d("LiveService", "Screen turned ON - Zipper enabled: " + zipperEnabled +
                      ", PIN set: " + pinEnabled + ", Service running: " + serviceRunning);

                // Important: Do NOT call Glszl_LockScreenService.Start() here
                // Starting the service on SCREEN_ON can cause the screen to wake up unexpectedly
            }
        }
    };

    public IBinder onBind(Intent intent) {
        service = this;
        return null;
    }

    public boolean checkPermissionOverlay() {
        try {
            if (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this)) {
                return true;
            }
            return false;
        } catch (Exception unused) {
            return true;
        }
    }

    @SuppressLint({"WrongConstant", "ForegroundServiceType"})
    public int onStartCommand(Intent intent, int i, int i2) {
        Log.e("LiveService", "LiveService on startCommand");
        service = this;
        RegiserScreenLock();
        startForeground();
        Log.e("startForeground", "InitialForeground: 4");
        return 1;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent restartIntent = new Intent(getApplicationContext(), Glszl_LiveService.class);
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= 26) {
                pendingIntent = PendingIntent.getForegroundService(
                        getApplicationContext(),
                        0,
                        restartIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getService(getApplicationContext(),
                        0,
                        restartIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            }
            long triggerAt = System.currentTimeMillis() + 1500;
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= 23) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                }
            }
        } catch (Exception ignored) {}
        super.onTaskRemoved(rootIntent);
    }

    @SuppressLint("ForegroundServiceType")
    private void startForeground() {
        Intent intent = new Intent(this, ZipLockMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent activity = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getPackageName());
            builder.setAutoCancel(false).setContentTitle(getResources().getString(R.string.app_name)).setTicker(getResources().getString(R.string.app_name)).setContentText(getResources().getString(R.string.service_running)).setSmallIcon(R.drawable.applogo).setContentIntent(activity).setOngoing(true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                builder.setChannelId(getPackageName());
                @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(getPackageName(), "Test", 4);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(-16776961);
                notificationChannel.setSound(null, null);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
            if (Build.VERSION.SDK_INT >= 34) {
                // type khớp với manifest
                startForeground(9999, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(9999, builder.build());
            }
        } else {
            startForeground(9999, new NotificationCompat.Builder(this).setContentTitle(getResources().getString(R.string.app_name)).setTicker(getResources().getString(R.string.app_name)).setContentText(getResources().getString(R.string.service_running)).setSmallIcon(R.drawable.applogo).setContentIntent(activity).setOngoing(true).build());
        }
    }


    public void RegiserScreenLock() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);

        try {
            if (Build.VERSION.SDK_INT >= 33) {
                // API 33+ cần flags → dùng ContextCompat
                ContextCompat.registerReceiver(
                        this,
                        this.mybroadcast,
                        filter,
                        ContextCompat.RECEIVER_NOT_EXPORTED
                );
            } else {
                registerReceiver(mybroadcast, filter);
            }
            receiverRegistered = true;
            Log.d("LiveService", "BroadcastReceiver registered");
        } catch (Throwable t) {
            // Nếu thất bại, giữ receiverRegistered = false để onDestroy không unregister
            Log.w("LiveService", "registerReceiver failed", t);
        }
//        try {
//            registerReceiver(this.mybroadcast, new IntentFilter("android.intent.action.SCREEN_ON"));
//            registerReceiver(this.mybroadcast, new IntentFilter("android.intent.action.SCREEN_OFF"));
//        } catch (Exception unused) {
//        }
    }

    public void onDestroy() {
        unRegisterReceiver();
        super.onDestroy();
    }

    private void unRegisterReceiver(){
        if (!receiverRegistered) return;
        try {
            unregisterReceiver(this.mybroadcast);
            Logger.INSTANCE.d("LiveService", "BroadcastReceiver unregistered");
        } catch (IllegalArgumentException e) {
            // Trường hợp hiếm do race-condition / hệ thống đã cleanup
            Logger.INSTANCE.e("LiveService", "Receiver was not registered (ignored)", e.getMessage());
        } finally {
            receiverRegistered = false;
        }
    }

    public void onCreate() {
        super.onCreate();
    }

    public static void StartServiceIfNotNull(Context context) {
        if (service == null) {
            try {
                if (Build.VERSION.SDK_INT >= 31) {
                    // Android 12+ has strict foreground service restrictions
                    // Only start foreground service if app is in foreground or has special permission
                    if (Build.VERSION.SDK_INT >= 26) {
                        context.startForegroundService(new Intent(context, Glszl_LiveService.class));
                    } else {
                        context.startService(new Intent(context, Glszl_LiveService.class));
                    }
                } else if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(new Intent(context, Glszl_LiveService.class));
                } else {
                    context.startService(new Intent(context, Glszl_LiveService.class));
                }
            } catch (Exception e) {
                // Handle ForegroundServiceStartNotAllowedException and other restrictions
                Log.e("LiveService", "Failed to start foreground service: " + e.getMessage());
                // Fallback to regular service start
                try {
                    context.startService(new Intent(context, Glszl_LiveService.class));
                } catch (Exception fallbackException) {
                    Log.e("LiveService", "Failed to start regular service: " + fallbackException.getMessage());
                }
            }
            return;
        }
    }

    public static void StartService(Context context) {
        context.startService(new Intent(context, Glszl_LiveService.class));
    }

    public static void StopService(Context context) {
        if (service != null) {
            service.stopSelf();
        }
        try {
            context.stopService(new Intent(context, Glszl_LiveService.class));
        } catch (Exception unused) {
        }
        service = null;
    }
}

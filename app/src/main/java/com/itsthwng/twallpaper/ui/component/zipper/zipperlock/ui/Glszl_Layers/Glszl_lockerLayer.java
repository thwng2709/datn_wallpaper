package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Layers;

import static android.content.Context.WIFI_SERVICE;

import static com.itsthwng.twallpaper.ui.component.setting.view.SettingFragment.DEV_PAGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.itsthwng.twallpaper.R;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Animations.Glszl_Deplace;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_GameAdapter;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_Screen;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_Timer;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_ULabel;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Uimage;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_UimagePart;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Urect;
import com.itsthwng.twallpaper.ui.component.zipper.lib.transition.C0034Transition;
import com.itsthwng.twallpaper.ui.component.zipper.lib.transition.TransitionType;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_LockScreenService;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Media.Glszl_C0013Media;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_SharedPreferencisUtil;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_servutils.Glszl_utils.Glszl_LockscreenService;

public class Glszl_lockerLayer {
    public static Glszl_Uimage heart_zipper_Bluetouth;
    static boolean heart_zipper_ClickDown = false;
    public static double DeviceHeight = Glszl_Screen.Height;
    public static double DeviceWidth = Glszl_Screen.Width;
    public static int heart_zipper_Duration = 550;
    static double heart_zipper_LastY = 0.0d;
    static double heart_zipper_MaxSpikeRotation = 40.0d;
    public static Glszl_Uimage heart_zipper_MoreApps;
    public static boolean heart_zipper_PasswordCorrect = false;
    public static Glszl_Uimage heart_zipper_RightPanelFlech;
    public static Glszl_Urect RightPanelHolcer;
    public static Glszl_Uimage Wifi;

    public static Activity f3at;
    public static Glszl_Uimage camera;
    private static Context cont;
    static Glszl_Deplace deplace;
    public static Glszl_Uimage locker;
    public static boolean notNugat = true;
    public static Glszl_Uimage rightPanel;
    public static TransitionType transitiontype = TransitionType.EaseInOutBounce;
    public static UnlockType unlocktype = UnlockType.Type3;

    public enum UnlockType {
        Type1,
        Type2,
        Type3
    }

    public static void Activate(boolean z) {
    }

    public static void Disactivate() {
    }

    static void HideSettings() {
    }

    static void ShowSettings() {
    }

    public static void CleareMemory() {
        locker = null;
        deplace = null;
        RightPanelHolcer = null;
        rightPanel = null;
        heart_zipper_RightPanelFlech = null;
        Wifi = null;
        heart_zipper_Bluetouth = null;
        heart_zipper_MoreApps = null;
        camera = null;
    }

    public Glszl_lockerLayer(Context context) {
        cont = context;
    }

    public static Activity getActivity(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    public static int dpToPx(int i, Context context) {
        return Math.round(((float) i) * context.getResources().getDisplayMetrics().density);
    }

    public static void Inicial() {
        if (Build.VERSION.SDK_INT < 26 && Build.VERSION.SDK_INT >= 23) {
            notNugat = true;
        }
        if (notNugat) {
            if (Glszl_LockScreenService.f10546cc.getApplicationContext().getResources().getConfiguration().orientation == 2) {
                DeviceWidth = Glszl_Screen.Height;
                DeviceHeight = Glszl_Screen.Width;
            }
        } else if (Glszl_LockscreenService.heart_zipper_instance.getApplicationContext().getResources().getConfiguration().orientation == 2) {
            DeviceWidth = Glszl_Screen.Height;
            DeviceHeight = Glszl_Screen.Width;
        }
        heart_zipper_PasswordCorrect = false;
        if (notNugat) {
            if (isTablet(Glszl_LockScreenService.f10546cc.getApplicationContext())) {
                double d = DeviceWidth;
                locker = new Glszl_Uimage(0.0d, 0.0d, d * 0.2d, d * 0.4d, Glszl_C0013Media.heart_zipper_zipper);
            } else {
                double d2 = DeviceWidth;
                locker = new Glszl_Uimage(0.0d, 0.0d, d2 * 0.28d, d2 * 0.5d, Glszl_C0013Media.heart_zipper_zipper);
            }
        } else if (isTablet(Glszl_LockscreenService.heart_zipper_instance.getApplicationContext())) {
            double d3 = DeviceWidth;
            locker = new Glszl_Uimage(0.0d, 0.0d, d3 * 0.2d, d3 * 0.4d, Glszl_C0013Media.heart_zipper_zipper);
        } else {
            double d4 = DeviceWidth;
            locker = new Glszl_Uimage(0.0d, 0.0d, d4 * 0.28d, d4 * 0.5d, Glszl_C0013Media.heart_zipper_zipper);
        }
        Glszl_GameAdapter.GetMainRect().AddChild(locker);
        if (notNugat) {
            if (isTablet(Glszl_LockScreenService.f10546cc.getApplicationContext())) {
                Glszl_Uimage uimage = locker;
                uimage.setLeft((DeviceWidth / 2.0d) - (uimage.Width() * 0.535d));
            } else {
                Glszl_Uimage uimage2 = locker;
                uimage2.setLeft(((DeviceWidth / 2.0d) - (uimage2.Width() / 2.0d)) - (DeviceWidth * 0.01d));
            }
        } else if (isTablet(Glszl_LockscreenService.heart_zipper_instance.getApplicationContext())) {
            Glszl_Uimage uimage3 = locker;
            uimage3.setLeft((DeviceWidth / 2.0d) - (uimage3.Width() * 0.535d));
        } else {
            Glszl_Uimage uimage4 = locker;
            uimage4.setLeft(((DeviceWidth / 2.0d) - (uimage4.Width() / 2.0d)) - (DeviceWidth * 0.01d));
        }
        locker.addOnClickDownListner(new Glszl_Urect.ClickDownListner() {

            @Override
            public void OnClickDownDo(double d, double d2) {
                Glszl_lockerLayer.heart_zipper_ClickDown = true;
                Glszl_lockerLayer.ShowRightPanel(true);
                if (Glszl_lockerLayer.deplace != null) {
                    Glszl_lockerLayer.deplace.remove();
                }
            }
        });
        locker.addOnClickUpListner(new Glszl_Urect.ClickUpListner() {

            @Override 
            public void OnClickUpDo(double d, double d2) {
                boolean z = false;
                Glszl_lockerLayer.heart_zipper_ClickDown = false;
                if (!Glszl_lockerLayer.notNugat ? Glszl_LockscreenService.heart_zipper_instance.getApplicationContext().getResources().getConfiguration().orientation == 2 : Glszl_LockScreenService.f10546cc.getApplicationContext().getResources().getConfiguration().orientation == 2) {
                    z = true;
                }
                if (Glszl_lockerLayer.locker.getTop() > Glszl_lockerLayer.DeviceHeight * 0.5d && Glszl_lockerLayer.locker.getTop() >= Glszl_lockerLayer.heart_zipper_LastY) {
                    Glszl_lockerLayer.SllideDown();
                } else if (!z) {
                    Glszl_lockerLayer.SllideUp();
                } else if (Glszl_lockerLayer.locker.getTop() <= Glszl_lockerLayer.DeviceWidth * 0.55d || Glszl_lockerLayer.locker.getTop() < Glszl_lockerLayer.heart_zipper_LastY) {
                    Glszl_lockerLayer.SllideUp();
                } else {
                    Glszl_lockerLayer.SllideDown();
                }
            }
        });
        locker.addOnTouchMoveListner(new Glszl_Urect.TouchMoveListner() {

            @Override
            public void OnMoveDo(Glszl_Urect urect, double d, double d2) {
                boolean z = true;
                if (!Glszl_lockerLayer.notNugat ? Glszl_LockscreenService.heart_zipper_instance.getApplicationContext().getResources().getConfiguration().orientation != 2 : Glszl_LockScreenService.f10546cc.getApplicationContext().getResources().getConfiguration().orientation != 2) {
                    z = false;
                }
                if (Glszl_lockerLayer.heart_zipper_ClickDown && d2 > urect.Height() / 2.0d) {
                    Glszl_lockerLayer.heart_zipper_LastY = urect.getTop();
                    double Height = d2 - (urect.Height() / 2.0d);
                    if (!Glszl_lockerLayer.heart_zipper_PasswordCorrect && Height >= Glszl_lockerLayer.DeviceHeight * 0.8d) {
                        Height = Glszl_lockerLayer.DeviceHeight * 0.8d;
                    }
                    urect.setTop(Height);
                }
                Log.e("touchmovelistner", "locker touch move listner" + d2 + "  DeviceHeight: " + (Glszl_lockerLayer.DeviceHeight * 0.95d) + "  currentObject: " + urect.getCenterY());
                if (d2 >= Glszl_lockerLayer.DeviceHeight * 0.95d && urect.getCenterY() >= d2) {
                    Log.e("touchmovelistner2", "locker touch move listner" + d2 + "  DeviceHeight: " + (Glszl_lockerLayer.DeviceHeight * 0.95d));
                    Glszl_lockerLayer.locker.removeOnTouchMoveListner(this);
                    Glszl_lockerLayer.SllideDown();
                } else if (z) {
                    if (d2 >= Glszl_lockerLayer.DeviceWidth * 0.95d && urect.getCenterY() >= d2) {
                        Log.e("touchmovelistner2", "locker touch move listner" + d2 + "  DeviceHeight: " + (Glszl_lockerLayer.DeviceHeight * 0.95d));
                        Glszl_lockerLayer.locker.removeOnTouchMoveListner(this);
                        Glszl_lockerLayer.SllideDown();
                    }
                    Log.e("touchmovelistner3", "locker touch move listner" + d2 + "  DeviceHeight: " + (Glszl_lockerLayer.DeviceHeight * 0.95d));
                }
                Log.e("touch move listner", "locker touch move listner" + d2);
            }
        });
        locker.OnUpdateListner(new Glszl_Urect.UpdateListner() {

            @Override 
            public void Update(Glszl_Urect urect) {
                if (Glszl_lockerLayer.unlocktype == UnlockType.Type1) {
                    Glszl_lockerLayer.UpdateType1(urect.getTop());
                } else if (Glszl_lockerLayer.unlocktype == UnlockType.Type2) {
                    Glszl_lockerLayer.UpdateType2(urect.getTop());
                } else if (Glszl_lockerLayer.unlocktype == UnlockType.Type3) {
                    Glszl_lockerLayer.UpdateType3(urect.getTop());
                }
                int i = ((((urect.getTop() / (Glszl_lockerLayer.DeviceHeight / 2.0d)) * 175.0d) + 50.0d) > 255.0d ? 1 : ((((urect.getTop() / (Glszl_lockerLayer.DeviceHeight / 2.0d)) * 175.0d) + 50.0d) == 255.0d ? 0 : -1));
            }
        });
        InitalRightPanel();
    }

    public static boolean isTablet(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float f = ((float) displayMetrics.heightPixels) / displayMetrics.ydpi;
        float f2 = ((float) displayMetrics.widthPixels) / displayMetrics.xdpi;
        return Math.sqrt((double) ((f2 * f2) + (f * f))) >= 7d;
    }

    @SuppressLint("WrongConstant")
    public static void InitalRightPanel() {
        double d = DeviceWidth / 4.5d;
        double d2 = 4.5d * d;
        double d3 = 0.5d * d;
        double d4 = d * 0.8d;
        double d5 = d * 0.33d;
        double topStart = d * 0.3d;
        double labelOffset = 0.33d * d;
        double paddingBottom = 0.5d * d;
        int count = 2;
        boolean showBt   = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R);
        boolean showWifi = (Build.VERSION.SDK_INT <  Build.VERSION_CODES.Q);
        if (showBt)   count++;
        if (showWifi) count++;
        double contentIconsHeight = topStart + count * d3 + Math.max(0, (count - 1)) * d4;
        double panelHeight = contentIconsHeight + labelOffset + d3 + paddingBottom;

        RightPanelHolcer = new Glszl_Urect(0.0d, 0.0d, d, panelHeight);
        heart_zipper_RightPanelFlech = new Glszl_Uimage(0.0d, 0.0d, d * 0.75d, d * 1.15d, Glszl_C0013Media.heart_zipper_r_panel_flech);
        Glszl_Uimage uimage = new Glszl_Uimage(0.0d, 0.0d, d, panelHeight, Glszl_C0013Media.heart_zipper_r_panel);
        rightPanel = uimage;
        heart_zipper_RightPanelFlech.setTop((rightPanel.Height() / 2.0d) - (heart_zipper_RightPanelFlech.Height() / 2.0d));
        Glszl_Uimage uimage2 = heart_zipper_RightPanelFlech;
        uimage2.setLeft((-uimage2.Width()) / 1.7d);
        Glszl_Urect urect = RightPanelHolcer;
        urect.setLeft(DeviceWidth - (urect.Width() / 9.0d));
        Glszl_Urect urect2 = RightPanelHolcer;
        urect2.setTop((DeviceHeight / 2.0d) - (urect2.Height() / 2.0d));

        heart_zipper_Bluetouth = new Glszl_Uimage(d5, 0, d3, d3, Glszl_C0013Media.heart_zipper_torch_off);
        Wifi                   = new Glszl_Uimage(d5, 0, d3, d3, Glszl_C0013Media.heart_zipper_wifi_on);
        camera                 = new Glszl_Uimage(d5, 0, d3, d3, Glszl_C0013Media.heart_zipper_camera);
        heart_zipper_MoreApps  = new Glszl_Uimage(d5, 0, d3, d3, Glszl_C0013Media.heart_zipper_more_apps);

        double d6 = d4 * 3.0d;
        double y = topStart;
//        heart_zipper_MoreApps = new Glszl_Uimage(d5, (1.05d * d) + d6, d3, d3, Glszl_C0013Media.heart_zipper_more_apps);
//        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.R){
//            heart_zipper_Bluetouth.setTop(y);
//            rightPanel.AddChild(heart_zipper_Bluetouth);
//            y += d3 + d4;
//        }
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
//            Wifi.setTop(y);
//            rightPanel.AddChild(Wifi);
//            y += d3 + d4;
//        }
        camera.setTop(y);
        rightPanel.AddChild(camera);
        y += d3 + d4;
        heart_zipper_MoreApps.setTop(y);
        rightPanel.AddChild(heart_zipper_MoreApps);
        Glszl_ULabel uLabel = new Glszl_ULabel(d5, y + labelOffset, d3, d3, "more apps");
        uLabel.setColor(Color.rgb(255, 255, 255));
        uLabel.SetTextSize(DeviceWidth / 40.0d);
        uLabel.setFont(Glszl_C0013Media.heart_zipper_font1);
        rightPanel.AddChild(uLabel);
        RightPanelHolcer.AddChild(heart_zipper_RightPanelFlech);
        RightPanelHolcer.AddChild(rightPanel);
        heart_zipper_RightPanelFlech.addOnClickDownListner(new Glszl_Urect.ClickDownListner() {

            @Override 
            public void OnClickDownDo(double d, double d2) {
                Glszl_lockerLayer.ShowRightPanel(false);
            }
        });
        heart_zipper_Bluetouth.addOnClickDownListner(new Glszl_Urect.ClickDownListner() {

            @Override 
            public void OnClickDownDo(double d, double d2) {
                Glszl_lockerLayer.TurnBluetouth();
            }
        });
        Wifi.addOnClickDownListner(new Glszl_Urect.ClickDownListner() {

            @Override 
            public void OnClickDownDo(double d, double d2) {
                Glszl_lockerLayer.TurnWifi();
            }
        });
        heart_zipper_MoreApps.addOnClickDownListner(new Glszl_Urect.ClickDownListner() {

            @Override 
            public void OnClickDownDo(double d, double d2) {
                Glszl_lockerLayer.M_Intent2developerpage();
            }
        });
        camera.addOnClickDownListner(new Glszl_Urect.ClickDownListner() {

            @SuppressLint("WrongConstant")
            @Override
            public void OnClickDownDo(double d, double d2) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.addFlags(268435456);
                MyStartActivity(intent);
                SllideDown();
            }
        });
        Context context = Glszl_GameAdapter.ctx;
        Context context2 = Glszl_GameAdapter.ctx;
//        if (!((WifiManager) context.getSystemService("wifi")).isWifiEnabled()) {
//            Wifi.setImage(Glszl_C0013Media.heart_zipper_wifi_on);
//        } else {
//            Wifi.setImage(Glszl_C0013Media.heart_zipper_wifi_off);
//        }
//        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (defaultAdapter == null || defaultAdapter.isEnabled()) {
//            heart_zipper_Bluetouth.setImage(Glszl_C0013Media.heart_zipper_torch_on);
//        } else {
//            heart_zipper_Bluetouth.setImage(Glszl_C0013Media.heart_zipper_torch_off);
//        }
        Glszl_GameAdapter.GetMainRect().AddChild(RightPanelHolcer);
    }

    static void TurnWifi() {
        Context context = Glszl_GameAdapter.ctx;
        Context context2 = Glszl_GameAdapter.ctx;
        @SuppressLint("WrongConstant") WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Wifi.setImage(Glszl_C0013Media.heart_zipper_wifi_off);
            wifiManager.setWifiEnabled(true);
            return;
        }
        Wifi.setImage(Glszl_C0013Media.heart_zipper_wifi_on);
        wifiManager.setWifiEnabled(false);
    }

    static void M_Intent2developerpage() {
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/dev?id=" + DEV_PAGE)
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!MyStartActivity(intent)) {
            intent.setData(Uri.parse("https://play.google.com/store/apps/dev?id=" + DEV_PAGE));
            if (!MyStartActivity(intent)) {
                Toast.makeText(Glszl_GameAdapter.ctx, Glszl_GameAdapter.ctx.getString(R.string.playstore_not_found), Toast.LENGTH_SHORT).show();
            } else {
                SllideDown();
            }
        } else {
            SllideDown();
        }
    }

    static boolean MyStartActivity(Intent intent) {
        try {
            Glszl_GameAdapter.ctx.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException unused) {
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    public static void TurnBluetouth() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null && !defaultAdapter.isEnabled()) {
            defaultAdapter.enable();
            Log.e("BlueTouth", "turned on");
            heart_zipper_Bluetouth.setImage(Glszl_C0013Media.heart_zipper_torch_on);
            return;
        }
        defaultAdapter.disable();
        Log.e("BlueTouth", "turned off");
        heart_zipper_Bluetouth.setImage(Glszl_C0013Media.heart_zipper_torch_off);
    }

    static boolean isFlashSupported(PackageManager packageManager) {
        return packageManager.hasSystemFeature("android.hardware.camera.flash");
    }

    public static void ShowRightPanel(boolean z) {
        double right = RightPanelHolcer.getRight();
        double d = DeviceWidth;
        if (right <= d || z) {
            Glszl_Urect urect = RightPanelHolcer;
            new Glszl_Deplace(urect, DeviceWidth - (urect.Width() / 9.0d), RightPanelHolcer.getTop(), 100.0d, TransitionType.EaseInOutQuad, 0.0d);
            return;
        }
        Glszl_Urect urect2 = RightPanelHolcer;
        new Glszl_Deplace(urect2, d - urect2.Width(), RightPanelHolcer.getTop(), 100.0d, TransitionType.EaseInOutQuad, 0.0d);
    }

    public static void SllideUp() {
        Log.e("Inicial", "lockerlayer2!");
        Glszl_Uimage uimage = locker;
        deplace = new Glszl_Deplace(uimage, uimage.getLeft(), 0.0d, (double) 500, TransitionType.EaseOutQuad, 0.0d);
    }

    public static void SlideDownAfterTime(int i) {
        Glszl_Timer timer = new Glszl_Timer(i, 0);
        timer.start();
        timer.OnTimerFinishCounting(new Glszl_Timer.TimerFinishListner() {

            @Override 
            public void DoWork(Glszl_Timer timer) {
                Glszl_lockerLayer.SllideDown();
                Log.e("finish!!!123", "DoWork: lockerLayer123");
            }
        });
    }

    public static void SllideDown() {

        double d = DeviceHeight * 2.0d;
        int i = 300 * 2;
        if (!heart_zipper_PasswordCorrect) {
            d = DeviceHeight * 0.9d;
            i = 500;
        } else {
            if(Glszl_LockScreenService.f10546cc != null){
                Glszl_LockScreenService.f10546cc.runOnUiThread(new Runnable() {

                    public void run() {
                        Glszl_LockScreenService.f10546cc.PlayUnzipSound();
                    }
                });
            }
        }
        // Add null check to prevent crash when service is destroyed
        if (locker != null) {
            Glszl_Uimage uimage = locker;
            deplace = new Glszl_Deplace(uimage, uimage.getLeft(), d, (double) i, TransitionType.EaseOutQuad, 0.0d);
        } else {
            Log.w("Glszl_lockerLayer", "Cannot create animation - locker is null");
        }
        ShowRightPanel(true);
        int i2 = Build.VERSION.SDK_INT;
    }

    public static void UpdateType3(final double d) {
        int i;
        Glszl_actionLayer.UpdateWidgets(d);
        Glszl_LockScreenService.f10546cc.runOnUiThread(new Runnable() {

            public void run() {
                Glszl_SharedPreferencisUtil sharedPreferencisUtil;
                if (Glszl_LockScreenService.f10546cc != null && Glszl_LockScreenService.f10546cc.PasswordHolder != null) {
                    if (d < Glszl_lockerLayer.DeviceHeight * 0.8d || d > Glszl_lockerLayer.DeviceHeight || Glszl_lockerLayer.heart_zipper_PasswordCorrect) {
                        Glszl_LockScreenService.f10546cc.PasswordHolder.setVisibility(View.GONE);
                        Glszl_LockScreenService.f10546cc.forgotPassword.setVisibility(View.GONE);
                        Glszl_LockScreenService.f10546cc.indicatorDots.setVisibility(View.GONE);
                        Glszl_LockScreenService.f10546cc.f14469pinLockView.setVisibility(View.GONE);
                    } else if (!Glszl_LockScreenService.f10546cc.showForrgotDialog) {
                        Glszl_LockScreenService.f10546cc.PasswordHolder.setVisibility(View.VISIBLE);
                        Glszl_LockScreenService.f10546cc.indicatorDots.setVisibility(View.VISIBLE);
                        Glszl_LockScreenService.f10546cc.f14469pinLockView.setVisibility(View.VISIBLE);
                        if (Glszl_lockerLayer.notNugat) {
                            sharedPreferencisUtil = new Glszl_SharedPreferencisUtil(Glszl_LockScreenService.f10546cc.getApplicationContext());
                        } else {
                            sharedPreferencisUtil = new Glszl_SharedPreferencisUtil(Glszl_LockscreenService.heart_zipper_instance.getApplicationContext());
                        }
                        if (sharedPreferencisUtil.getSecurityQuestionIsActive()) {
                            Glszl_LockScreenService.f10546cc.forgotPassword.setVisibility(View.VISIBLE);
                        } else {
                            Glszl_LockScreenService.f10546cc.forgotPassword.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Glszl_LockScreenService.f10546cc.PasswordHolder.setVisibility(View.INVISIBLE);
                        Glszl_LockScreenService.f10546cc.forgotPassword.setVisibility(View.INVISIBLE);
                        Glszl_LockScreenService.f10546cc.indicatorDots.setVisibility(View.INVISIBLE);
                        Glszl_LockScreenService.f10546cc.f14469pinLockView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        int i2 = 0;
        for (int i3 = 0; i3 < Glszl_actionLayer.left.size(); i3++) {
            Glszl_UimagePart uimagePart = Glszl_actionLayer.left.get(i3);
            if (d > uimagePart.getTop()) {
                double d2 = DeviceWidth / 2.0d;
                double d3 = Glszl_actionLayer.heart_zipper_space2;
                double top = d - uimagePart.getTop();
                double d4 = DeviceHeight / 2.0d;
                double GetValue = C0034Transition.getValue(transitiontype, top, d2, d3, d4);
                if (top <= d4) {
                    d3 = GetValue;
                }
                uimagePart.setWidth(d3);
                uimagePart.heart_zipper_ImageRect.setWidth((uimagePart.Width() / DeviceWidth) * ((double) uimagePart.getImage().getWidth()));
                Glszl_Uimage uimage = (Glszl_Uimage) uimagePart.getChildrens().get(0);
                uimage.setLeft((uimagePart.getRight() - uimage.Width()) + Glszl_actionLayer.heart_zipper_space + Glszl_actionLayer.heart_zipper_space2);
                if (uimage.getLeft() > DeviceWidth / 4.0d) {
                    uimage.setRotate(C0034Transition.getValue(transitiontype, top, 0.0d, -heart_zipper_MaxSpikeRotation, d4 / 2.0d));
                } else if (top < d4) {
                    double d5 = d4 / 2.0d;
                    uimage.setRotate(C0034Transition.getValue(transitiontype, top - d5, -heart_zipper_MaxSpikeRotation, 0.0d, d5));
                } else {
                    uimage.setRotate(0.0d);
                }
            } else {
                uimagePart.setWidth(DeviceWidth / 2.0d);
                Glszl_Uimage uimage2 = (Glszl_Uimage) uimagePart.getChildrens().get(0);
                uimagePart.heart_zipper_ImageRect.setWidth((double) (uimagePart.getImage().getWidth() / 2));
                uimage2.setLeft((uimagePart.getRight() - uimage2.Width()) + Glszl_actionLayer.heart_zipper_space + Glszl_actionLayer.heart_zipper_space2);
                uimage2.setRotate(0.0d);
            }
        }
        int i4 = 0;
        while (i4 < Glszl_actionLayer.right.size()) {
            Glszl_UimagePart uimagePart2 = Glszl_actionLayer.right.get(i4);
            Glszl_Uimage uimage3 = (Glszl_Uimage) uimagePart2.getChildrens().get(i2);
            if (d > uimagePart2.getTop()) {
                double d6 = (DeviceWidth / 2.0d) + Glszl_actionLayer.heart_zipper_space2;
                double d7 = DeviceWidth;
                double top2 = d - uimagePart2.getTop();
                double d8 = DeviceHeight / 2.0d;
                double GetValue2 = C0034Transition.getValue(transitiontype, top2, d6, d7, d8);
                if (top2 <= d8) {
                    d7 = GetValue2;
                }
                uimagePart2.setLeft(d7);
                i = i4;
                uimagePart2.heart_zipper_ImageRect.setLeft(((uimagePart2.getLeft() / DeviceWidth) * ((double) uimagePart2.getImage().getWidth())) - 1.0d);
                if (uimagePart2.GetCenterX() < (DeviceWidth / 4.0d) * 3.0d) {
                    uimage3.setRotate(C0034Transition.getValue(transitiontype, top2, 0.0d, heart_zipper_MaxSpikeRotation, d8 / 2.0d));
                } else if (uimagePart2.getLeft() < DeviceWidth) {
                    double d9 = d8 / 2.0d;
                    uimage3.setRotate(C0034Transition.getValue(transitiontype, top2 - d9, heart_zipper_MaxSpikeRotation, 0.0d, d9));
                } else {
                    uimage3.setRotate(0.0d);
                }
            } else {
                i = i4;
                uimagePart2.setLeft((DeviceWidth / 2.0d) + Glszl_actionLayer.heart_zipper_space2);
                uimagePart2.setWidth(DeviceWidth / 2.0d);
                uimagePart2.heart_zipper_ImageRect.setLeft(((uimagePart2.getLeft() / DeviceWidth) * ((double) uimagePart2.getImage().getWidth())) - 1.0d);
                uimage3.setRotate(0.0d);
            }
            i4 = i + 1;
            i2 = 0;
        }
    }

    public static void UpdateType1(double d) {
        for (int i = 0; i < Glszl_actionLayer.left.size(); i++) {
            Glszl_UimagePart uimagePart = Glszl_actionLayer.left.get(i);
            if (d > uimagePart.getTop()) {
                uimagePart.setLeft((uimagePart.getTop() - d) - Glszl_actionLayer.heart_zipper_space2);
            } else {
                uimagePart.setLeft(-Glszl_actionLayer.heart_zipper_space2);
                uimagePart.heart_zipper_skewX = 0.0d;
            }
        }
        for (int i2 = 0; i2 < Glszl_actionLayer.right.size(); i2++) {
            Glszl_UimagePart uimagePart2 = Glszl_actionLayer.right.get(i2);
            if (d > uimagePart2.getTop()) {
                uimagePart2.setLeft((((DeviceWidth / 2.0d) + d) - uimagePart2.getTop()) + Glszl_actionLayer.heart_zipper_space2);
            } else {
                uimagePart2.setLeft((DeviceWidth / 2.0d) + Glszl_actionLayer.heart_zipper_space2);
            }
        }
    }

    public static void UpdateType2(double d) {
        double size = DeviceHeight / ((double) Glszl_actionLayer.left.size());
        for (int i = 0; i < Glszl_actionLayer.left.size(); i++) {
            Glszl_UimagePart uimagePart = Glszl_actionLayer.left.get(i);
            if (d > uimagePart.getTop()) {
                uimagePart.setHeight(size - (((d - uimagePart.getTop()) / (DeviceHeight - uimagePart.getTop())) * size));
            } else {
                uimagePart.setHeight(size);
            }
        }
        for (int i2 = 0; i2 < Glszl_actionLayer.right.size(); i2++) {
            Glszl_UimagePart uimagePart2 = Glszl_actionLayer.right.get(i2);
            if (d > uimagePart2.getTop() + (uimagePart2.Height() / 2.0d)) {
                uimagePart2.setHeight(size - (((d - uimagePart2.getTop()) / (DeviceHeight - uimagePart2.getTop())) * size));
            } else {
                uimagePart2.setHeight(size);
            }
        }
    }
}

package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Layers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.DisplayMetrics;

import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_GameAdapter;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_Screen;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_ULabel;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Uimage;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_UimagePart;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Urect;
import com.itsthwng.twallpaper.ui.component.zipper.lib.transition.C0034Transition;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_AppAdapter;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.PrefKey;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_LockScreenService;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Media.Glszl_C0013Media;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_UserDataAdapter;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_servutils.Glszl_utils.Glszl_LockscreenService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class Glszl_actionLayer {
    public static Glszl_Uimage heart_zipper_BateryCover;
    public static Glszl_Urect heart_zipper_BateryHolder;
    public static Glszl_ULabel heart_zipper_BateryLabel;
    public static Glszl_UimagePart heart_zipper_BateryLevel;
    public static Glszl_ULabel heart_zipper_Date;
    public static double DeviceHeight = Glszl_Screen.Height;
    public static double DeviceWidth = Glszl_Screen.Width;
    public static Glszl_ULabel heart_zipper_HoursMinutes;
    public static Glszl_ULabel heart_zipper_Seconds;
    public static Glszl_Urect heart_zipper_TimeHolder;
    static double heart_zipper_baterryWidth;

    static Calendar f1c;
    public static int count = 30;
    public static SimpleDateFormat curFormater;
    public static List<Glszl_UimagePart> left;
    public static boolean notNugat = true;
    public static Glszl_UimagePart prt;
    public static List<Glszl_UimagePart> right;
    public static double heart_zipper_space;
    public static double heart_zipper_space2;

    // Layout zip locker
    public static void Inicial() {
        Context context;
        Context context2;
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
        if (notNugat) {
            context = Glszl_LockScreenService.f10546cc.getApplicationContext();
        } else {
            context = Glszl_LockscreenService.heart_zipper_instance.getApplicationContext();
        }
        double d = DeviceWidth;
        int i = count;
        heart_zipper_space2 = d / ((double) i);
        double d2 = d / ((double) (i * 3));
        left = new ArrayList();
        right = new ArrayList();
        Bitmap bitmap = Glszl_C0013Media.scaleCenterCrop(
                Glszl_C0013Media.heart_zipper_SelectedBg,
                (int) DeviceHeight,
                (int) (DeviceWidth)
        );
        double d3 = DeviceWidth / 2.0d;
        double d4 = DeviceHeight / ((double) count);
        double width = (double) (bitmap.getWidth() / 2);
        double height = (double) (bitmap.getHeight() / count);

        // Tính toán dựa trên kích thước thực tế của chain images
        double chainImageWidth = Glszl_C0013Media.heart_zipper_ChainLeft.getWidth();
        double totalChainWidth = chainImageWidth * 2;  // 2 chains
        double gap = DeviceWidth * 0.02d;             // 2% màn hình

        double startX = (DeviceWidth - totalChainWidth - gap) / 2.0d;
        double leftX = startX;
        double rightX = startX + chainImageWidth + gap;

        int chainWidth = Glszl_C0013Media.heart_zipper_ChainLeft.getWidth();
        int chainHeight = Glszl_C0013Media.heart_zipper_ChainLeft.getHeight();

        int i2 = 0;

        int type = Glszl_AppAdapter.getSelectedChainType(context);
        int valueTemp = 0;
        while (i2 < count) {
            double d5 = (double) i2;
            Glszl_UimagePart uimagePart = new Glszl_UimagePart(
                    0.0d, d5 * d4, d3, d4,
                    new Glszl_Urect(0.0d, d5 * height, width, height),
                    bitmap
            );
            if(type == 0){

            } else if(type == 1){
                uimagePart = new Glszl_UimagePart(
                        0.0d, (d5 * 2) * d4, d3, d4 * 2,
                        new Glszl_Urect(0.0d, (d5 * 2) * height, width, height * 2),
                        bitmap
                );
            } else if(type == 2){
                uimagePart = new Glszl_UimagePart(
                        0.0d, d5 * d4, d3, d4,
                        new Glszl_Urect(0.0d, d5 * height, width, height),
                        bitmap
                );
            }
            prt = uimagePart;
            left.add(uimagePart);
            Glszl_GameAdapter.GetMainRect().AddChild(prt);
            Glszl_Uimage uimage = new Glszl_Uimage(0.0d, 0.0d, d4 * 2.0d, d4, Glszl_C0013Media.heart_zipper_ChainLeft);
            if (notNugat) {
                context2 = Glszl_LockScreenService.f10546cc.getApplicationContext();
            } else {
                context2 = Glszl_LockscreenService.heart_zipper_instance.getApplicationContext();
            }
            prt.AddChild(uimage);
            i2++;
            valueTemp = 1;
            height = height;
            width = width;
        }
        int i3 = 0;
        int i5 = -1;
        for (int i4 = 1; i3 < count + i4; i4 = 1) {
            double d9 = (double) i5;
            double d6 = (double) i3;
            Glszl_UimagePart uimagePart2 = new Glszl_UimagePart(
                    d3, (d6 * d4) - (d4 / 2.0d), d3, d4,
                    new Glszl_Urect(0.0d, (d6 * height) - (height / 2.0d), width, height),
                    bitmap
            );
            if(type == 0){
                uimagePart2 = new Glszl_UimagePart(
                        d3, (d6 * d4) - (d4 / 2.0d), d3, d4,
                        new Glszl_Urect(0.0d, (d6 * height) - (height / 2.0d), width, height),
                        bitmap
                );
            } else if(type == 1){
                uimagePart2 = new Glszl_UimagePart(
                        d3, ((d6 + d9) * d4), d3, d4 * 2,
                        new Glszl_Urect(0.0d, ((d6 + d9) * height), width, height * 2),
                        bitmap
                );
            } else if(type == 2){
                uimagePart2 = new Glszl_UimagePart(
                        d3, (d6 * d4) + (d4 / 8.0d), d3, d4,
                        new Glszl_Urect(0.0d, (d6 * height) + (height / 5.0d), width, height),
                        bitmap
                );
            }
            prt = uimagePart2;
            right.add(uimagePart2);
            Glszl_GameAdapter.GetMainRect().AddChild(prt);
            Glszl_Uimage uimage2 = new Glszl_Uimage(0.0d, 0.0d, d4 * 2.0d, d4, Glszl_C0013Media.heart_zipper_ChainRight);
            if(type == 0){
                if (isTablet(context)) {
                    double d7 = heart_zipper_space2;
                    uimage2.setLeft(((-d2) - d7) - (d7 * 0.3d));
                } else {
                    double d8 = heart_zipper_space2;
//                uimage2.setLeft(((-d2) - d8) - (d8));
                    uimage2.setLeft(-d4 + 10.0d);
                }
            } else if(type == 1) {
                if (isTablet(context)) {
                    double d7 = heart_zipper_space2;
                    uimage2.setLeft(((-d2) - d7) - (d7 * 0.3d));
                } else {
                    double d8 = heart_zipper_space2;
//                uimage2.setLeft(((-d2) - d8) - (d8));
                    uimage2.setLeft(-d4);
                }
            } else if(type == 2){
                if (isTablet(context)) {
                    double d7 = heart_zipper_space2;
                    uimage2.setLeft(((-d2) - d7) - (d7 * 0.3d));
                } else {
                    double d8 = heart_zipper_space2;
//                uimage2.setLeft(((-d2) - d8) - (d8));
                    uimage2.setLeft((d4 / 10.0d));
                }
            }
            prt.AddChild(uimage2);
            i3++;
            i5++;
        }
        CreateWidgets();
    }

    public static boolean isTablet(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float f = ((float) displayMetrics.heightPixels) / displayMetrics.ydpi;
        float f2 = ((float) displayMetrics.widthPixels) / displayMetrics.xdpi;
        return Math.sqrt((double) ((f2 * f2) + (f * f))) >= 76d;
    }

    public static void CleareMemory() {
        List<Glszl_UimagePart> list = left;
        if (list != null) {
            list.clear();
            right.clear();
            left = null;
            right = null;
        }
    }

    private static void CreateWidgets() {
        curFormater = new SimpleDateFormat("dd/MM/yyyy");
        double d = DeviceWidth;
        heart_zipper_TimeHolder = new Glszl_Urect(d / 100.0d, d / 4.0d, 0.0d, 0.0d);
        double d2 = DeviceWidth;
        heart_zipper_HoursMinutes = new Glszl_ULabel(d2 / 50.0d, d2 / 10.0d, 0.0d, d2 / 4.0d, "");
        double d3 = DeviceWidth;
        heart_zipper_Seconds = new Glszl_ULabel(d3 / 36.0d, d3 / 10.0d, 0.0d, d3 / 4.0d, "10:05");
        double d4 = DeviceWidth;
        heart_zipper_Date = new Glszl_ULabel(d4 / 36.0d, d4 / 6.0d, 0.0d, d4 / 4.0d, "");
        heart_zipper_HoursMinutes.SetTextSize(DeviceWidth / 7.0d);
        heart_zipper_HoursMinutes.setColor(Color.rgb(255, 255, 255));
        heart_zipper_HoursMinutes.setTextAlign(Paint.Align.LEFT);
        heart_zipper_HoursMinutes.setFont(Glszl_C0013Media.heart_zipper_font1);
        heart_zipper_Seconds.SetTextSize(DeviceWidth / 10.0d);
        heart_zipper_Seconds.setTextAlign(Paint.Align.LEFT);
        heart_zipper_Seconds.setColor(Color.rgb(255, 255, 255));
        heart_zipper_Seconds.setFont(Glszl_C0013Media.heart_zipper_font1);
        heart_zipper_Date.SetTextSize(DeviceWidth / 18.0d);
        heart_zipper_Date.setColor(Color.rgb(255, 255, 255));
        heart_zipper_Date.setTextAlign(Paint.Align.LEFT);
        heart_zipper_Date.setFont(Glszl_C0013Media.heart_zipper_font1);
        if (Glszl_UserDataAdapter.LoadPref(PrefKey.DATE_ACTIVE, Glszl_GameAdapter.ctx) != 0) {
            heart_zipper_TimeHolder.AddChild(heart_zipper_Date);
        }
        if (Glszl_UserDataAdapter.LoadPref(PrefKey.TIME_ACTIVE, Glszl_GameAdapter.ctx) != 0) {
            heart_zipper_TimeHolder.AddChild(heart_zipper_HoursMinutes);
        }
        Glszl_GameAdapter.GetMainRect().AddChild(heart_zipper_TimeHolder);
        heart_zipper_TimeHolder.OnUpdateListner(new Glszl_Urect.UpdateListner() {

            @Override
            public void Update(Glszl_Urect urect) {
                Glszl_actionLayer.UpdateDateAndTime();
            }
        });
        heart_zipper_baterryWidth = DeviceWidth * 0.15d;
        double d5 = DeviceWidth;
        heart_zipper_BateryHolder = new Glszl_Urect(d5 / 12.0d, 0.8d * d5, d5 / 2.0d, 0.0d);
        if (Glszl_UserDataAdapter.LoadPref(PrefKey.BATTERY_ACTIVE, Glszl_GameAdapter.ctx) != 0) {
            Glszl_GameAdapter.GetMainRect().AddChild(heart_zipper_BateryHolder);
        }
        double d6 = heart_zipper_baterryWidth;
        Glszl_Uimage uimage = new Glszl_Uimage(0.0d, 0.0d, d6, d6 / 2.0d, Glszl_C0013Media.heart_zipper_BateryCover);
        heart_zipper_BateryCover = uimage;
        heart_zipper_BateryHolder.AddChild(uimage);
        Bitmap bitmap = Glszl_C0013Media.heart_zipper_batteryLevel;
        Glszl_UimagePart uimagePart = new Glszl_UimagePart(0.0d, 0.0d, heart_zipper_BateryCover.Width(), heart_zipper_BateryCover.Height(), new Glszl_Urect(0.0d, 0.0d, (double) bitmap.getWidth(), (double) bitmap.getHeight()), bitmap);
        heart_zipper_BateryLevel = uimagePart;
        heart_zipper_BateryHolder.AddChild(uimagePart);
        Glszl_ULabel uLabel = new Glszl_ULabel(DeviceWidth / 25.0d, 1.5d * heart_zipper_BateryLevel.getRelativeBottom(), heart_zipper_BateryCover.Width(), heart_zipper_BateryCover.Height(), "50%");
        heart_zipper_BateryLabel = uLabel;
        uLabel.setColor(Color.rgb(255, 255, 255));
        heart_zipper_BateryLabel.SetTextSize(DeviceWidth / 10.0d);
        heart_zipper_BateryLabel.setFont(Glszl_C0013Media.heart_zipper_font1);
        Glszl_ULabel uLabel2 = new Glszl_ULabel(DeviceWidth / 20.0d, heart_zipper_BateryLevel.getRelativeBottom(), heart_zipper_BateryCover.Width(), heart_zipper_BateryCover.Height(), "50%");
        uLabel2.setColor(Color.rgb(255, 255, 255));
        uLabel2.SetTextSize(DeviceWidth / 10.0d);
        uLabel2.setFont(Glszl_C0013Media.heart_zipper_font1);
        heart_zipper_BateryHolder.AddChild(heart_zipper_BateryLabel);
    }

    protected static void UpdateDateAndTime() {
        f1c = Calendar.getInstance();
        String str = f1c.getTime().getHours() + "";
        String str2 = f1c.getTime().getMinutes() + "";
        String str3 = f1c.getTime().getSeconds() + "";
        if (str2.length() < 2) {
            str2 = "0" + str2;
        }
        if (str.length() < 2) {
            str = "0" + str;
        }
        if (str3.length() < 2) {
            str3 = "0" + str3;
        }
        heart_zipper_HoursMinutes.setText(str + ":" + str2);
        heart_zipper_Seconds.setText(str3);
        heart_zipper_Date.setText(new SimpleDateFormat("d MMM yyyy").format(Calendar.getInstance().getTime()));
    }

    public static void FixBateryLevelWidth(double d) {
        double d2 = d / 100.0d;
        Glszl_ULabel uLabel = heart_zipper_BateryLabel;
        if(uLabel == null) {
            return;
        }
        uLabel.setText(((int) d) + "%");
        heart_zipper_BateryLevel.heart_zipper_ImageRect.setWidth((((double) heart_zipper_BateryLevel.getImage().getWidth()) * 0.97d * d2) + (((double) heart_zipper_BateryLevel.getImage().getWidth()) * 0.03d));
        Glszl_UimagePart uimagePart = heart_zipper_BateryLevel;
        double d3 = heart_zipper_baterryWidth;
        uimagePart.setWidth((0.97d * d3 * d2) + (d3 * 0.03d));
    }

    private static void updateFadeSlideFor(Glszl_ULabel label, double d, double startLeft, double offscreenLeft, double duration, double fadeDuration) {
        double top = label.getTop();
        if (d < top) {
            label.setLeft(startLeft);
            label.setAlpha(255.0d);
            return;
        }
        double t = d - top;
        if (t >= duration) {
            label.setLeft(offscreenLeft);
            label.setAlpha(0.0d);
            return;
        }
        label.setLeft(C0034Transition.getValue(Glszl_lockerLayer.transitiontype, t, startLeft, offscreenLeft, duration));
        label.setAlpha(C0034Transition.getValue(Glszl_lockerLayer.transitiontype, t, 255.0d, 0.0d, fadeDuration));
    }

    public static void UpdateWidgets(double d) {
        double duration = (double) Glszl_lockerLayer.heart_zipper_Duration;
        double offscreenLeft = (-DeviceWidth) / 2.0d;
        updateFadeSlideFor(heart_zipper_HoursMinutes, d, DeviceWidth / 50.0d, offscreenLeft, duration, duration / 1.4d);
        updateFadeSlideFor(heart_zipper_Seconds, d, DeviceWidth / 36.0d, offscreenLeft, duration, duration / 1.4d);
        updateFadeSlideFor(heart_zipper_Date, d, DeviceWidth / 36.0d, offscreenLeft, duration, duration / 1.4d);
        if (d >= heart_zipper_BateryCover.getTop()) {
            double top4 = d - heart_zipper_BateryCover.getTop();
            double d14 = DeviceWidth;
            double d15 = d14 / 36.0d;
            double d16 = (-d14) / 2.0d;
            double d17 = (double) Glszl_lockerLayer.heart_zipper_Duration;
            if (top4 < d17) {
                double GetValue = C0034Transition.getValue(Glszl_lockerLayer.transitiontype, top4, d15, d16, d17);
                heart_zipper_BateryCover.setLeft(GetValue);
                heart_zipper_BateryLevel.setLeft(GetValue);
                if (top4 < d17 / 2.0d) {
                    double GetValue2 = C0034Transition.getValue(Glszl_lockerLayer.transitiontype, top4, 255.0d, 0.0d, d17 / 1.4d);
                    heart_zipper_BateryCover.setAlpha(GetValue2);
                    heart_zipper_BateryLevel.setAlpha(GetValue2);
                }
            }
        } else {
            heart_zipper_BateryCover.setLeft(DeviceWidth / 36.0d);
            heart_zipper_BateryCover.setAlpha(255.0d);
            heart_zipper_BateryLevel.setLeft(DeviceWidth / 36.0d);
            heart_zipper_BateryLevel.setAlpha(255.0d);
        }
        if (d >= heart_zipper_BateryLabel.getTop()) {
            double top5 = d - heart_zipper_BateryCover.getTop();
            double d18 = DeviceWidth;
            double d19 = d18 / 36.0d;
            double d20 = (-d18) / 2.0d;
            double d21 = (double) Glszl_lockerLayer.heart_zipper_Duration;
            if (top5 < d21) {
                heart_zipper_BateryLabel.setLeft(C0034Transition.getValue(Glszl_lockerLayer.transitiontype, top5, d19, d20, d21));
                if (top5 < d21 / 2.0d) {
                    heart_zipper_BateryLabel.setAlpha(C0034Transition.getValue(Glszl_lockerLayer.transitiontype, top5, 255.0d, 0.0d, d21 / 1.4d));
                    return;
                }
                return;
            }
            return;
        }
        heart_zipper_BateryLabel.setLeft(DeviceWidth / 36.0d);
        heart_zipper_BateryLabel.setAlpha(255.0d);
    }
}

package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.internal.view.SupportMenu;

import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Animations.Glszl_AnimationAdapter;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_ULabel;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Urect;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Layers.Glszl_BackgroundLayer;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Layers.Glszl_actionLayer;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Layers.Glszl_lockerLayer;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_LockScreenService;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Media.Glszl_C0013Media;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_UserDataAdapter;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_servutils.Glszl_utils.Glszl_LockscreenService;
import com.itsthwng.twallpaper.utils.Logger;


public class Glszl_GameAdapter {
    public static Glszl_Urect Background;
    public static Glszl_ULabel ObjCount;
    public static Glszl_Urect Scene;
    public static Context ctx;
    public static Glszl_Drawer drawer;
    public static Glszl_ULooper looper;
    public static boolean notNugat = true;
    public static boolean Inicialed = false;
    public static boolean Ispaused = true;
    static boolean closing = false;
    static boolean readyToClose = false;

    public static Glszl_Urect GetMainRect() {
        return Background;
    }

    @SuppressLint("RestrictedApi")
    public static boolean StartGame(Context context) {
        if (Build.VERSION.SDK_INT < 26 && Build.VERSION.SDK_INT >= 23) {
            notNugat = true;
        }
        Log.e("StartGame", "StartGame!");
        Ispaused = true;
        closing = false;
        ctx = context;
        readyToClose = false;
        Inicialed = true;
        Glszl_Screen.Inicialize();
        Glszl_C0013Media.inicial();
        Background = new Glszl_Urect(0.0d, 0.0d, Glszl_Screen.Width, Glszl_Screen.Height, 0);
        Glszl_Urect urect = new Glszl_Urect(0.0d, 0.0d, Glszl_Screen.Width, Glszl_Screen.Height, 0);
        Scene = urect;
        urect.AddChild(Background);
        Scene.setColor(0);
        Scene.setAlpha(255.0d);
        Glszl_ULabel uLabel = new Glszl_ULabel(0.0d, 0.0d, Glszl_Screen.Width, Glszl_Screen.Height, "0");
        ObjCount = uLabel;
        uLabel.SetTextSize(Glszl_Screen.Width / 10.0d);
        ObjCount.setColor(SupportMenu.CATEGORY_MASK);
        ObjCount.OnUpdateListner(new Glszl_Urect.UpdateListner() {
            @Override
            public void Update(Glszl_Urect urect2) {
            }
        });
        Glszl_AnimationAdapter.Init();
        drawer.setSoundEffectsEnabled(false);
        looper = new Glszl_ULooper();
        Glszl_BackgroundLayer.Inicial();
        Glszl_actionLayer.Inicial();

        Glszl_lockerLayer.Inicial();
        drawer.addOnDrawListner(new Glszl_Drawer.DrawListner() {
            @Override
            public void OnDraw(Canvas canvas) {
                Glszl_GameAdapter.Draw(canvas);
            }
        });
        Background.addOnTouchMoveListner(new Glszl_Urect.TouchMoveListner() {
            @Override
            public void OnMoveDo(Glszl_Urect urect2, double d, double d2) {
            }
        });
        drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glszl_Urect.CheckRectsClickUp();
            }
        });
        drawer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == 0) {
                    Glszl_GameAdapter.GetMainRect().checkClickDown(motionEvent.getX(), motionEvent.getY());
                    return false;
                } else if (motionEvent.getAction() == 2) {
                    Glszl_Urect.CheckRectTouchMove(motionEvent.getX(), motionEvent.getY());
                    return false;
                } else {
                    return false;
                }
            }
        });
        Glszl_UserDataAdapter.setIsPreview(false);
        Resume();
        Background.addOnClickDownListner(new Glszl_Urect.ClickDownListner() {
            @Override
            public void OnClickDownDo(double d, double d2) {
            }
        });
        return true;
    }

    public static boolean Pause() {
        if (Ispaused) {
            Log.i("GAdapter pause skiped", "gameAdapter pause skiped");
            return false;
        }
        Log.i("gameAdapter pause", "gameAdapter pause");
        Glszl_Drawer drawer2 = drawer;
        if (drawer2 == null) {
            Log.i("drawer null 1", "drawer null 1");
            return false;
        }
        drawer2.pause();
        Glszl_ULooper uLooper = looper;
        if (uLooper != null) {
            uLooper.Pause();
        }
        looper = null;
        Ispaused = true;
        return false;
    }

    public static boolean Resume() {
        if (!Ispaused) {
            Log.i("GAdapter resume skiped", "gameAdapter resume skiped");
            return false;
        }
        Log.i("gameAdapter Resume", "gameAdapter Resume");
        Glszl_Drawer drawer2 = drawer;
        if (drawer2 == null) {
            Log.i("drawer null 1", "drawer null 1");
            return false;
        }
        Ispaused = false;
        drawer2.resume();
        Glszl_ULooper uLooper = new Glszl_ULooper();
        looper = uLooper;
        uLooper.Resume();
        return true;
    }

    public static void Update() {
        double d;
        try {
            Glszl_AnimationAdapter.Update();
            Glszl_Timer.Update();
            GetMainRect().CheckObjUpdates();
        } catch (Exception e) {
            Log.i("Update prblm", "Update probliiim a3chiri" + e.getMessage());
        }
        boolean z = false;
        if (!notNugat) {
            Glszl_LockscreenService.Start(ctx);
        }
        
        // Add null checks before accessing service instances
        try {
            if (!notNugat) {
                // Check if heart_zipper_instance is not null before accessing getApplicationContext
                if (Glszl_LockscreenService.heart_zipper_instance != null) {
                    z = Glszl_LockscreenService.heart_zipper_instance.getApplicationContext()
                            .getResources().getConfiguration().orientation == 2;
                } else {
                    Log.w("GameAdapter", "heart_zipper_instance is null, using default orientation");
                    z = false;
                }
            } else {
                // Check if f10546cc is not null before accessing getApplicationContext
                if (Glszl_LockScreenService.f10546cc != null) {
                    z = Glszl_LockScreenService.f10546cc.getApplicationContext()
                            .getResources().getConfiguration().orientation == 2;
                } else {
                    Log.w("GameAdapter", "f10546cc is null, using default orientation");
                    z = false;
                }
            }
        } catch (Exception e) {
            Log.e("GameAdapter", "Error checking orientation: " + e.getMessage());
            z = false; // Default to portrait
        }
        
        if (z) {
            d = Glszl_Screen.Width;
        } else {
            d = Glszl_Screen.Height;
        }
        try {
            if (closing || Glszl_lockerLayer.locker.getTop() < d * 1.75d) {
                return;
            }
        } catch (Exception e) {
            Logger.INSTANCE.e("GameAdapter Update: ", e.getMessage());
        }
        closing = true;
        closeThatShet();
    }

    static void closeThatShet() {
        Glszl_Urect urect = Scene;
        Glszl_Timer timer = new Glszl_Timer(220, 0);
        timer.start();
        timer.OnTimerFinishCounting(new Glszl_Timer.TimerFinishListner() {
            @Override
            public void DoWork(Glszl_Timer timer2) {
                new Handler(Glszl_GameAdapter.ctx.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (Glszl_GameAdapter.notNugat) {
                            Glszl_LockScreenService.f10546cc.finish();
                        } else {
                            Glszl_LockscreenService.heart_zipper_instance.finish();
                        }
                        Log.e("finish!!!123", "run: finish game adapter");
                        Glszl_GameAdapter.Inicialed = false;
                        int i = Build.VERSION.SDK_INT;
                    }
                });
            }
        });
    }

    public static void Draw(Canvas canvas) {
        Glszl_Urect urect = Scene;
        if (urect != null) {
            urect.Draw(canvas);
        }
    }

    public static void close() {
        Inicialed = false;
        Glszl_ULooper uLooper = looper;
        if (uLooper != null) {
            uLooper.Pause();
        }
        Glszl_actionLayer.CleareMemory();
        Glszl_lockerLayer.CleareMemory();
        Glszl_AnimationAdapter.CleareMemory();
        Glszl_Drawer drawer2 = drawer;
        if (drawer2 != null) {
            drawer2.pause();
            drawer.stopDrawing();
            drawer.heart_zipper_IsFinished = true;
        }
        GetMainRect().clearChilds();
        Glszl_Timer.Clear();
        GetMainRect().Delete();
        Glszl_C0013Media.Clear();
        Background = null;
        Scene = null;
        drawer = null;
        looper = null;
        ObjCount = null;
        ctx = null;
    }

}

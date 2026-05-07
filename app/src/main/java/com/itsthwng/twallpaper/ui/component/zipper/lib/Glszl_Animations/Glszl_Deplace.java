package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Animations;


import android.util.Log;

import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_Timer;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Urect;
import com.itsthwng.twallpaper.ui.component.zipper.lib.transition.C0034Transition;
import com.itsthwng.twallpaper.ui.component.zipper.lib.transition.TransitionType;

import java.util.ArrayList;
import java.util.List;


public class Glszl_Deplace {
    public static List<Glszl_Deplace> heart_zipper_list = new ArrayList();
    public Glszl_Timer heart_zipper_AnimationTimer;
    public int heart_zipper_CurentTime;
    public boolean heart_zipper_Done;
    public double heart_zipper_f4Fx;
    public double heart_zipper_f5Fy;
    public boolean heart_zipper_JustX;
    public boolean heart_zipper_JustY;
    public Glszl_Urect heart_zipper_Obj;
    public int heart_zipper_Time;
    public double heart_zipper_f6Tx;
    public double heart_zipper_f7Ty;
    public int heart_zipper_Wait;
    public Glszl_Timer heart_zipper_WaitTimer;
    public TransitionType heart_zipper_transition_type;

    public boolean ManipulateTimers() {
        Glszl_Urect urect = this.heart_zipper_Obj;
        if (urect != null) {
            urect.setDeplaceAnnimation(this);
        }
        if (this.heart_zipper_AnimationTimer != null) {
            return false;
        }
        this.heart_zipper_AnimationTimer = new Glszl_Timer(this.heart_zipper_Time, 0);
        Glszl_Timer timer = new Glszl_Timer(this.heart_zipper_Wait, 0);
        this.heart_zipper_WaitTimer = timer;
        timer.start();
        this.heart_zipper_AnimationTimer.OnTimerFinishCounting(new Glszl_Timer.TimerFinishListner() {
            @Override
            public void DoWork(Glszl_Timer timer2) {
                Glszl_Deplace.this.Calc();
                Glszl_Deplace.this.heart_zipper_Done = true;
                timer2.Remove();
            }
        });
        this.heart_zipper_AnimationTimer.OnEveryStep(new Glszl_Timer.TimerStepListner() {
            @Override
            public void DoWork(int i, Glszl_Timer timer2) {
                Glszl_Deplace.this.Calc();
            }
        });
        this.heart_zipper_WaitTimer.OnTimerFinishCounting(new Glszl_Timer.TimerFinishListner() {
            @Override
            public void DoWork(Glszl_Timer timer2) {
                Glszl_Deplace.this.heart_zipper_AnimationTimer.start();
                timer2.Remove();
            }
        });
        return true;
    }

    public Glszl_Deplace(Glszl_Urect urect, double d, double d2, double d3, double d4, double d5, TransitionType transition_Type, double d6) {
        this.heart_zipper_Wait = 0;
        this.heart_zipper_JustX = false;
        this.heart_zipper_JustY = false;
        this.heart_zipper_Done = false;
        Glszl_Urect urect2 = this.heart_zipper_Obj;
        if (urect2 != null) {
            urect2.setDeplaceAnnimation(this);
        }
        this.heart_zipper_Obj = urect;
        this.heart_zipper_f4Fx = d;
        this.heart_zipper_f5Fy = d2;
        this.heart_zipper_f6Tx = d3;
        this.heart_zipper_f7Ty = d4;
        this.heart_zipper_Time = (int) d5;
        this.heart_zipper_CurentTime = 0;
        this.heart_zipper_transition_type = transition_Type;
        if (heart_zipper_list == null) {
            heart_zipper_list = new ArrayList();
        }
        heart_zipper_list.add(this);
        this.heart_zipper_Wait = (int) d6;
        this.heart_zipper_JustX = this.heart_zipper_JustX;
        this.heart_zipper_JustY = this.heart_zipper_JustY;
        ManipulateTimers();
    }

    public Glszl_Deplace(Glszl_Urect urect, double d, double d2, double d3, TransitionType transition_Type, double d4) {
        this.heart_zipper_Wait = 0;
        this.heart_zipper_JustX = false;
        this.heart_zipper_JustY = false;
        this.heart_zipper_Done = false;
        Glszl_Urect urect2 = this.heart_zipper_Obj;
        if (urect2 != null) {
            urect2.setDeplaceAnnimation(this);
        }
        this.heart_zipper_Obj = urect;
        this.heart_zipper_f4Fx = urect.getRelativeLeft();
        this.heart_zipper_f5Fy = urect.getRelativeTop();
        this.heart_zipper_f6Tx = d;
        this.heart_zipper_f7Ty = d2;
        this.heart_zipper_Time = (int) d3;
        this.heart_zipper_CurentTime = 0;
        this.heart_zipper_transition_type = transition_Type;
        if (heart_zipper_list == null) {
            heart_zipper_list = new ArrayList();
        }
        heart_zipper_list.add(this);
        this.heart_zipper_Wait = (int) d4;
        ManipulateTimers();
    }

    public Glszl_Deplace(Glszl_Urect urect, double d, double d2, double d3, TransitionType transition_Type, double d4, boolean z, boolean z2) {
        this.heart_zipper_Wait = 0;
        this.heart_zipper_JustX = false;
        this.heart_zipper_JustY = false;
        this.heart_zipper_Done = false;
        Glszl_Urect urect2 = this.heart_zipper_Obj;
        if (urect2 != null) {
            urect2.setDeplaceAnnimation(this);
        }
        this.heart_zipper_Obj = urect;
        this.heart_zipper_f4Fx = urect.getRelativeLeft();
        this.heart_zipper_f5Fy = urect.getRelativeTop();
        this.heart_zipper_f6Tx = d;
        this.heart_zipper_f7Ty = d2;
        this.heart_zipper_Time = (int) d3;
        this.heart_zipper_CurentTime = 0;
        this.heart_zipper_transition_type = transition_Type;
        if (heart_zipper_list == null) {
            heart_zipper_list = new ArrayList();
        }
        heart_zipper_list.add(this);
        this.heart_zipper_Wait = (int) d4;
        this.heart_zipper_JustX = z;
        this.heart_zipper_JustY = z2;
        ManipulateTimers();
    }

    public static boolean DeleteIfExist(Glszl_Urect urect) {
        for (int i = 0; i < heart_zipper_list.size(); i++) {
            if (heart_zipper_list.get(i).heart_zipper_Obj == urect) {
                heart_zipper_list.get(i).heart_zipper_Done = true;
                return true;
            }
        }
        return false;
    }

    public static boolean Exist(Glszl_Urect urect) {
        for (int i = 0; i < heart_zipper_list.size(); i++) {
            if (heart_zipper_list.get(i).heart_zipper_Obj == urect) {
                return true;
            }
        }
        return false;
    }

    public void Calc() {
        if (!this.heart_zipper_JustY && !this.heart_zipper_JustX) {
            this.heart_zipper_Obj.setLeft(C0034Transition.getValue(this.heart_zipper_transition_type, this.heart_zipper_AnimationTimer.CurentTime, this.heart_zipper_f4Fx, this.heart_zipper_f6Tx, this.heart_zipper_AnimationTimer.MaxTime));
            this.heart_zipper_Obj.setTop(C0034Transition.getValue(this.heart_zipper_transition_type, this.heart_zipper_AnimationTimer.CurentTime, this.heart_zipper_f5Fy, this.heart_zipper_f7Ty, this.heart_zipper_AnimationTimer.MaxTime));
        } else if (this.heart_zipper_JustX) {
            this.heart_zipper_Obj.setLeft(C0034Transition.getValue(this.heart_zipper_transition_type, this.heart_zipper_AnimationTimer.CurentTime, this.heart_zipper_f4Fx, this.heart_zipper_f6Tx, this.heart_zipper_AnimationTimer.MaxTime));
        } else if (this.heart_zipper_JustY) {
            this.heart_zipper_Obj.setTop(C0034Transition.getValue(this.heart_zipper_transition_type, this.heart_zipper_AnimationTimer.CurentTime, this.heart_zipper_f5Fy, this.heart_zipper_f7Ty, this.heart_zipper_AnimationTimer.MaxTime));
        }
    }

    public static void update() {
        if (heart_zipper_list == null) {
            return;
        }
        int i = 0;
        while (i < heart_zipper_list.size()) {
            try {
                if (!heart_zipper_list.get(i).heart_zipper_Done) {
                    heart_zipper_list.remove(i);
                    i--;
                }
            } catch (Exception unused) {
            }
            i++;
        }
    }

    public void finish() {
        Log.e("finish!!!123", "finish: deplace123");
        Glszl_Timer timer = this.heart_zipper_WaitTimer;
        if (timer != null) {
            timer.pause();
            this.heart_zipper_WaitTimer.Remove();
        }
        Glszl_Timer timer2 = this.heart_zipper_AnimationTimer;
        if (timer2 != null) {
            timer2.pause();
            this.heart_zipper_AnimationTimer.Remove();
        }
    }

    public void remove() {
        Glszl_Timer timer = this.heart_zipper_AnimationTimer;
        if (timer != null) {
            timer.Remove();
            this.heart_zipper_WaitTimer.Remove();
        }
        this.heart_zipper_Done = true;
        heart_zipper_list.remove(this);
    }
}

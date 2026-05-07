package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Animations;

import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_Timer;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Urect;
import com.itsthwng.twallpaper.ui.component.zipper.lib.transition.C0034Transition;
import com.itsthwng.twallpaper.ui.component.zipper.lib.transition.TransitionType;

import java.util.ArrayList;
import java.util.List;

public class Glszl_Rotation {
    public static List<Glszl_Rotation> heart_zipper_list = new ArrayList();
    public Glszl_Timer heart_zipper_AnimationTimer;
    public double heart_zipper_FromRot;
    public Glszl_Urect heart_zipper_Obj;
    public int heart_zipper_Time;
    public double heart_zipper_ToRot;
    public int heart_zipper_WaitTime;
    public Glszl_Timer heart_zipper_WaitTimer;
    public TransitionType heart_zipper_transition_type;
    public boolean heart_zipper_Done = false;

    public Glszl_Rotation(Glszl_Urect urect, double d, double d2, double d3, TransitionType transition_Type, int i) {
        this.heart_zipper_WaitTime = 0;
        if (urect != null) {
            urect.setRotationAnimation(this);
        }
        this.heart_zipper_Obj = urect;
        this.heart_zipper_WaitTime = i;
        this.heart_zipper_FromRot = d;
        this.heart_zipper_ToRot = d2;
        this.heart_zipper_Time = (int) d3;
        this.heart_zipper_transition_type = transition_Type;
        heart_zipper_list.add(this);
        ManipulateTimers();
    }

    public boolean ManipulateTimers() {
        if (this.heart_zipper_AnimationTimer != null) {
            return false;
        }
        this.heart_zipper_AnimationTimer = new Glszl_Timer(this.heart_zipper_Time, 0);
        Glszl_Timer timer = new Glszl_Timer(this.heart_zipper_WaitTime, 0);
        this.heart_zipper_WaitTimer = timer;
        timer.start();
        this.heart_zipper_AnimationTimer.OnTimerFinishCounting(new Glszl_Timer.TimerFinishListner() {
            @Override
            public void DoWork(Glszl_Timer timer2) {
                Glszl_Rotation.this.Calc();
                Glszl_Rotation.this.heart_zipper_Done = true;
                timer2.Remove();
            }
        });
        this.heart_zipper_AnimationTimer.OnEveryStep(new Glszl_Timer.TimerStepListner() {
            @Override
            public void DoWork(int i, Glszl_Timer timer2) {
                Glszl_Rotation.this.Calc();
            }
        });
        this.heart_zipper_WaitTimer.OnTimerFinishCounting(new Glszl_Timer.TimerFinishListner() {
            @Override
            public void DoWork(Glszl_Timer timer2) {
                Glszl_Rotation.this.heart_zipper_AnimationTimer.start();
                timer2.Remove();
            }
        });
        return true;
    }

    public void Calc() {
        this.heart_zipper_Obj.setRotate(C0034Transition.getValue(this.heart_zipper_transition_type, this.heart_zipper_AnimationTimer.CurentTime, this.heart_zipper_FromRot, this.heart_zipper_ToRot, this.heart_zipper_AnimationTimer.MaxTime));
    }

    public static void update() {
        int i = 0;
        while (i < heart_zipper_list.size()) {
            if (!heart_zipper_list.get(i).heart_zipper_Done) {
                heart_zipper_list.remove(i);
                i--;
            }
            i++;
        }
    }

}

package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Animations;


import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_Timer;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Urect;
import com.itsthwng.twallpaper.ui.component.zipper.lib.transition.C0034Transition;
import com.itsthwng.twallpaper.ui.component.zipper.lib.transition.TransitionType;

import java.util.ArrayList;
import java.util.List;


public class Glszl_Fade {
    public static List<Glszl_Fade> heart_zipper_list = new ArrayList();
    public Glszl_Timer heart_zipper_AnimationTimer;
    public double heart_zipper_FromApha;
    public Glszl_Urect heart_zipper_Obj;
    public int heart_zipper_Time;
    public double heart_zipper_ToAlpha;
    public int heart_zipper_WaitTime;
    public Glszl_Timer heart_zipper_WaitTimer;
    public TransitionType heart_zipper_transition_type;
    public boolean heart_zipper_Done = false;

    public Glszl_Fade(Glszl_Urect urect, double d, double d2, double d3, TransitionType transition_Type) {
        this.heart_zipper_WaitTime = 0;
        Glszl_Urect urect2 = this.heart_zipper_Obj;
        if (urect2 != null) {
            urect2.setFadeAnnimation(this);
        }
        this.heart_zipper_Obj = urect;
        d = d > 255.0d ? 255.0d : d;
        d2 = d2 < 0.0d ? 0.0d : d2;
        this.heart_zipper_FromApha = d;
        this.heart_zipper_ToAlpha = d2;
        this.heart_zipper_WaitTime = 0;
        this.heart_zipper_Time = (int) d3;
        this.heart_zipper_transition_type = transition_Type;
        heart_zipper_list.add(this);
        ManipulateTimers();
    }

    public Glszl_Fade(Glszl_Urect urect, double d, double d2, double d3, TransitionType transition_Type, int i) {
        this.heart_zipper_WaitTime = 0;
        Glszl_Urect urect2 = this.heart_zipper_Obj;
        if (urect2 != null) {
            urect2.setFadeAnnimation(this);
        }
        this.heart_zipper_Obj = urect;
        this.heart_zipper_WaitTime = i;
        d = d > 255.0d ? 255.0d : d;
        d2 = d2 < 0.0d ? 0.0d : d2;
        this.heart_zipper_FromApha = d;
        this.heart_zipper_ToAlpha = d2;
        this.heart_zipper_Time = (int) d3;
        this.heart_zipper_transition_type = transition_Type;
        heart_zipper_list.add(this);
        ManipulateTimers();
    }

    public boolean ManipulateTimers() {
        Glszl_Urect urect = this.heart_zipper_Obj;
        if (urect != null) {
            urect.setFadeAnnimation(this);
        }
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
                Glszl_Fade.this.Calc();
                Glszl_Fade.this.heart_zipper_Done = true;
                timer2.Remove();
            }
        });
        this.heart_zipper_AnimationTimer.OnEveryStep(new Glszl_Timer.TimerStepListner() {
            @Override
            public void DoWork(int i, Glszl_Timer timer2) {
                Glszl_Fade.this.Calc();
            }
        });
        this.heart_zipper_WaitTimer.OnTimerFinishCounting(new Glszl_Timer.TimerFinishListner() {
            @Override
            public void DoWork(Glszl_Timer timer2) {
                Glszl_Fade.this.heart_zipper_AnimationTimer.start();
                timer2.Remove();
            }
        });
        return true;
    }

    public void Calc() {
        this.heart_zipper_Obj.setAlpha(C0034Transition.getValue(this.heart_zipper_transition_type, this.heart_zipper_AnimationTimer.CurentTime, this.heart_zipper_FromApha, this.heart_zipper_ToAlpha, this.heart_zipper_AnimationTimer.MaxTime));
    }

    public static void update() {
        int i = 0;
        while (i < heart_zipper_list.size()) {
            Glszl_Fade fade = heart_zipper_list.get(i);
            if (!fade.heart_zipper_Done) {
                heart_zipper_list.remove(fade);
                i--;
            }
            i++;
        }
    }

    public void Remove() {
        Glszl_Timer timer = this.heart_zipper_AnimationTimer;
        if (timer != null) {
            timer.Remove();
            this.heart_zipper_WaitTimer.Remove();
        }
        this.heart_zipper_Done = true;
        heart_zipper_list.remove(this);
    }
}

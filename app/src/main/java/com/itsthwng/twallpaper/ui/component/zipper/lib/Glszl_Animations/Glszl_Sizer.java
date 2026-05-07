package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Animations;

import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_Timer;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Urect;
import com.itsthwng.twallpaper.ui.component.zipper.lib.transition.C0034Transition;
import com.itsthwng.twallpaper.ui.component.zipper.lib.transition.TransitionType;

import java.util.ArrayList;
import java.util.List;

public class Glszl_Sizer {
    public static List<Glszl_Sizer> heart_zipper_list = new ArrayList();
    public double heart_zipper_CurentTime;
    public double heart_zipper_f11Fh;
    public double heart_zipper_f12Fw;
    public Glszl_Urect heart_zipper_Obj;
    public double heart_zipper_f13Th;
    public double heart_zipper_Time;
    public double heart_zipper_f14Tw;
    public TransitionType heart_zipper_transition_type;
    public Glszl_Timer heart_zipper_waitTimer;

    public Glszl_Sizer(Glszl_Urect urect, double d, double d2, double d3, double d4, double d5, TransitionType transition_Type, int i) {
        Glszl_Timer timer = new Glszl_Timer(i, 0);
        this.heart_zipper_waitTimer = timer;
        timer.start();
        this.heart_zipper_Obj = urect;
        this.heart_zipper_f12Fw = d;
        this.heart_zipper_f11Fh = d2;
        this.heart_zipper_f14Tw = d3;
        this.heart_zipper_f13Th = d4;
        this.heart_zipper_Time = d5;
        this.heart_zipper_CurentTime = 0.0d;
        this.heart_zipper_transition_type = transition_Type;
        heart_zipper_list.add(this);
        this.heart_zipper_Obj.setSizerAnnimation(this);
    }

    public boolean Calc() {
        if (this.heart_zipper_waitTimer.IsFinished()) {
            this.heart_zipper_Obj.setWidth(C0034Transition.getValue(this.heart_zipper_transition_type, this.heart_zipper_CurentTime, this.heart_zipper_f12Fw, this.heart_zipper_f14Tw, this.heart_zipper_Time));
            this.heart_zipper_Obj.setHeight(C0034Transition.getValue(this.heart_zipper_transition_type, this.heart_zipper_CurentTime, this.heart_zipper_f11Fh, this.heart_zipper_f13Th, this.heart_zipper_Time));
            double d = this.heart_zipper_CurentTime;
            if (d == this.heart_zipper_Time) {
                return false;
            }
            this.heart_zipper_CurentTime = d + 1.0d;
            return true;
        }
        return true;
    }

    public static void update() {
        int i = 0;
        while (i < heart_zipper_list.size()) {
            try {
                if (!heart_zipper_list.get(i).Calc()) {
                    heart_zipper_list.get(i).heart_zipper_waitTimer.Remove();
                    heart_zipper_list.remove(i);
                    i--;
                }
            } catch (Exception unused) {
            }
            i++;
        }
    }
}

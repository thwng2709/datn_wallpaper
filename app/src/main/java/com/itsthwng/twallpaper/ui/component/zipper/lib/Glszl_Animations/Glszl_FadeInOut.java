package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Animations;

import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes.Glszl_Urect;
import com.itsthwng.twallpaper.ui.component.zipper.lib.transition.TransitionType;

import java.util.ArrayList;
import java.util.List;


public class Glszl_FadeInOut {
    public static List<Glszl_FadeInOut> heart_zipper_list = new ArrayList();
    public double heart_zipper_f8FA;
    public double heart_zipper_FlashNumber;
    Glszl_Urect heart_zipper_Obj;
    public double heart_zipper_f9TA;
    public double heart_zipper_Time;
    TransitionType heart_zipper_f10tr;
    public double heart_zipper_CurentTime = 0.0d;
    public double heart_zipper_CurentFlash = 0.0d;

    public Glszl_FadeInOut(int i, Glszl_Urect urect, double d, double d2, double d3, TransitionType transition_Type) {
        this.heart_zipper_Time = d3;
        this.heart_zipper_Obj = urect;
        this.heart_zipper_FlashNumber = i;
        heart_zipper_list.add(this);
        this.heart_zipper_f10tr = transition_Type;
        this.heart_zipper_f8FA = d;
        this.heart_zipper_f9TA = d2;
        new Glszl_Fade(this.heart_zipper_Obj, this.heart_zipper_f8FA, this.heart_zipper_f9TA, this.heart_zipper_Time, this.heart_zipper_f10tr);
    }

    public static boolean DeleteIfExist(Glszl_Urect urect) {
        for (int i = 0; i < heart_zipper_list.size(); i++) {
            if (heart_zipper_list.get(i).heart_zipper_Obj == urect) {
                heart_zipper_list.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean Calc() {
        if (this.heart_zipper_CurentTime == this.heart_zipper_Time) {
            double d = this.heart_zipper_FlashNumber;
            if (d != -1.0d) {
                double d2 = this.heart_zipper_CurentFlash;
                if (d2 >= d) {
                    return false;
                }
                this.heart_zipper_CurentFlash = d2 + 1.0d;
                this.heart_zipper_CurentTime = 0.0d;
                double d3 = this.heart_zipper_f8FA;
                this.heart_zipper_f8FA = this.heart_zipper_f9TA;
                this.heart_zipper_f9TA = d3;
                new Glszl_Fade(this.heart_zipper_Obj, this.heart_zipper_f8FA, this.heart_zipper_f9TA, this.heart_zipper_Time, this.heart_zipper_f10tr);
            } else {
                double d4 = this.heart_zipper_f8FA;
                this.heart_zipper_f8FA = this.heart_zipper_f9TA;
                this.heart_zipper_f9TA = d4;
                this.heart_zipper_CurentTime = 0.0d;
                new Glszl_Fade(this.heart_zipper_Obj, this.heart_zipper_f8FA, this.heart_zipper_f9TA, this.heart_zipper_Time, this.heart_zipper_f10tr);
            }
        }
        this.heart_zipper_CurentTime += 1.0d;
        return true;
    }

    public static void update() {
        int i = 0;
        while (i < heart_zipper_list.size()) {
            try {
                if (!heart_zipper_list.get(i).Calc()) {
                    heart_zipper_list.remove(i);
                    i--;
                }
                i++;
            } catch (Exception unused) {
                return;
            }
        }
    }
}

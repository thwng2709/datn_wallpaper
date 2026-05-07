package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class Glszl_Timer {
    public static List<Glszl_Timer> heart_zipper_list = new ArrayList();
    public int CurentTime;
    public int MaxTime;
    List<TimerFinishListner> TimerFinishListner = new ArrayList();
    List<TimerStepListner> TimerSteps = new ArrayList();
    public boolean Pause = true;
    public boolean Done = false;
    public boolean AlreadyFinished = false;

    
    public interface TimerFinishListner {
        void DoWork(Glszl_Timer timer);
    }

    
    public interface TimerStepListner {
        void DoWork(int i, Glszl_Timer timer);
    }

    public void OnTimerFinishCounting(TimerFinishListner timerFinishListner) {
        this.TimerFinishListner.add(timerFinishListner);
    }

    public void OnEveryStep(TimerStepListner timerStepListner) {
        this.TimerSteps.add(timerStepListner);
    }

    public Glszl_Timer(int i, int i2) {
        if (heart_zipper_list == null) {
            heart_zipper_list = new ArrayList();
        }
        this.MaxTime = i;
        this.CurentTime = i2;
        heart_zipper_list.add(this);
    }

    public boolean IsFinished() {
        return this.CurentTime >= this.MaxTime || this.Done;
    }

    public boolean isPaused() {
        return this.Pause;
    }

    public boolean update() {
        if (!IsFinished()) {
            if (isPaused()) {
                return false;
            }
            this.CurentTime++;
            for (TimerStepListner timerStepListner : this.TimerSteps) {
                timerStepListner.DoWork(this.CurentTime, this);
            }
            return false;
        }
        if (!this.AlreadyFinished) {
            this.AlreadyFinished = true;
            for (TimerFinishListner timerFinishListner : this.TimerFinishListner) {
                timerFinishListner.DoWork(this);
            }
        }
        return true;
    }

    public void Restart() {
        this.AlreadyFinished = false;
        this.Pause = false;
        this.CurentTime = 0;
    }

    public static void Update() {
        if (heart_zipper_list == null) {
            Log.e("timer list null", "timer list null");
            return;
        }
        int i = 0;
        while (i < heart_zipper_list.size()) {
            if (heart_zipper_list.get(i).Done) {
                heart_zipper_list.remove(i);
                i--;
            } else {
                heart_zipper_list.get(i).update();
            }
            i++;
        }
    }

    public void start() {
        this.Pause = false;
        if (heart_zipper_list.indexOf(this) == -1) {
            heart_zipper_list.add(this);
        }
    }

    public void Remove() {
        this.Done = true;
    }

    public void pause() {
        this.Pause = true;
    }

    public static void Clear() {
        if (heart_zipper_list != null) {
            for (int i = 0; i < heart_zipper_list.size(); i++) {
                heart_zipper_list.get(i).Delete();
            }
            heart_zipper_list.clear();
        }
    }

    private void Delete() {
        this.TimerFinishListner.clear();
        this.TimerSteps.clear();
    }
}

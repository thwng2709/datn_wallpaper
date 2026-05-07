package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class Glszl_ULooper implements Runnable {

    
    public static int f15ct = 0;
    public static int sleep = 1;
    public Thread Gamethread;
    public boolean isRunning;
    public List<UpdateListner> updateListner;

    
    public interface UpdateListner {
    }

    public void Resume() {
        this.isRunning = true;
        this.updateListner = new ArrayList();
        Thread thread = new Thread(this);
        this.Gamethread = thread;
        thread.start();
    }

    public void Pause() {
        this.isRunning = false;
    }

    public Glszl_ULooper() {
        this.isRunning = true;
    }

    @Override 
    public void run() {
        while (this.isRunning) {
            Update();
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                Log.i("Looper Error", "Looper Error :" + e.getMessage());
            }
            f15ct++;
        }
        Log.i("Looper run out", "Looper run out");
    }

    public void Update() {
        Glszl_GameAdapter.Update();
    }

}

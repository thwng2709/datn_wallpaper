package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;


public class Glszl_Drawer extends SurfaceView implements Runnable {
    public static boolean Share = false;
    public static Canvas heart_zipper_canvas;
    public boolean heart_zipper_IsFinished;
    public List<DrawListner> heart_zipper_drawListners;
    public boolean heart_zipper_isRunning;
    SurfaceHolder heart_zipper_myHolder;
    public Thread heart_zipper_myThread;

    
    public interface DrawListner {
        void OnDraw(Canvas canvas);
    }

    public Glszl_Drawer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.heart_zipper_IsFinished = false;
        this.heart_zipper_myThread = null;
        this.heart_zipper_drawListners = new ArrayList();
        SurfaceHolder holder = getHolder();
        this.heart_zipper_myHolder = holder;
        holder.setFormat(-2);
        setDrawingCacheEnabled(true);
    }

    public Glszl_Drawer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.heart_zipper_IsFinished = false;
        this.heart_zipper_myThread = null;
        this.heart_zipper_drawListners = new ArrayList();
        SurfaceHolder holder = getHolder();
        this.heart_zipper_myHolder = holder;
        holder.setFormat(-2);
        setDrawingCacheEnabled(true);
    }

    public Glszl_Drawer(Context context) {
        super(context);
        this.heart_zipper_IsFinished = false;
        this.heart_zipper_myThread = null;
        this.heart_zipper_drawListners = new ArrayList();
        SurfaceHolder holder = getHolder();
        this.heart_zipper_myHolder = holder;
        holder.setFormat(-2);
        setDrawingCacheEnabled(true);
    }

    public void pause() {
        this.heart_zipper_isRunning = false;
    }

    public void resume() {
        if (this.heart_zipper_isRunning) {
            return;
        }
        this.heart_zipper_isRunning = true;
        Thread thread = new Thread(this);
        this.heart_zipper_myThread = thread;
        thread.start();
    }

    @Override 
    public void run() {
        while (this.heart_zipper_isRunning && !this.heart_zipper_IsFinished) {
            if (this.heart_zipper_myHolder.getSurface().isValid()) {
                Draw();
            }
        }
    }

    private void Draw() {
        heart_zipper_canvas = this.heart_zipper_myHolder.lockCanvas();
        UpdateDrawing();
        try {
            this.heart_zipper_myHolder.unlockCanvasAndPost(heart_zipper_canvas);
        } catch (Exception unused) {
            Log.i("Canvas problem", "Canvas problem");
        }
    }

    public void UpdateDrawing() {
        Canvas canvas2 = heart_zipper_canvas;
        if (canvas2 != null) {
            canvas2.drawColor(0, PorterDuff.Mode.CLEAR);
            for (DrawListner drawListner : this.heart_zipper_drawListners) {
                drawListner.OnDraw(heart_zipper_canvas);
            }
        }
    }

    public void addOnDrawListner(DrawListner drawListner) {
        this.heart_zipper_drawListners.add(drawListner);
    }

    public void stopDrawing() {
        pause();
    }
}

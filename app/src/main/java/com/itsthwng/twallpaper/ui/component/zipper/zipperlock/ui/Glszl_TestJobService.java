package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;

public class Glszl_TestJobService extends JobService {
    SharedPreferences packagePrefs;

    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }

    public boolean isRunning() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getApplicationContext().getPackageName(), 0);
        this.packagePrefs = sharedPreferences;
        return sharedPreferences.getBoolean("lockRunning", false);
    }

    @SuppressLint("SpecifyJobSchedulerIdRange")
    public boolean onStartJob(JobParameters jobParameters) {
        if (!isRunning()) {
            return true;
        }
        
        // On Android 12+, avoid starting foreground service from JobService
        // as it's restricted and will cause ForegroundServiceStartNotAllowedException
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            android.util.Log.w("TestJobService", "Skipping foreground service start from JobService on Android 12+");
            // Schedule next job but don't start service from background
            Glszl_JobUtil.scheduleJob(getApplicationContext());
            return true;
        }
        
        Glszl_LiveService.StartServiceIfNotNull(getApplicationContext());
        Glszl_JobUtil.scheduleJob(getApplicationContext());
        return true;
    }

}

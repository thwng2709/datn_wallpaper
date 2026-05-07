package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

public class Glszl_JobUtil {
    public static void scheduleJob(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(context, Glszl_TestJobService.class));
            builder.setMinimumLatency(1000);
            builder.setOverrideDeadline(3000);
            ((JobScheduler) context.getSystemService(JobScheduler.class)).schedule(builder.build());
        }
    }
}

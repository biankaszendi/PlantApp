package com.example.plantmanager;

import android.content.Context;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class Worker {
    public static void schedulePeriodicCheck(Context context) {
        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(PeriodicCheckWorker.class, 7, TimeUnit.DAYS)
                        .build();

        WorkManager.getInstance(context).enqueue(periodicWorkRequest);
    }
}


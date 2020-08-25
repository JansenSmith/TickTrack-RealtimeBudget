package com.theflopguyproductions.ticktrack.receivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.theflopguyproductions.ticktrack.TimerManagementHelper;
import com.theflopguyproductions.ticktrack.stopwatch.StopwatchData;
import com.theflopguyproductions.ticktrack.stopwatch.service.StopwatchNotificationService;
import com.theflopguyproductions.ticktrack.utils.database.TickTrackDatabase;

import java.util.ArrayList;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        TimerManagementHelper timerManagementHelper = new TimerManagementHelper(context);

        timerManagementHelper.reestablishTimers();

        TickTrackDatabase tickTrackDatabase = new TickTrackDatabase(context);
        ArrayList<StopwatchData> stopwatchData = tickTrackDatabase.retrieveStopwatchData();
        if(stopwatchData.get(0).getStopwatchTimerStartTimeInMillis()!=-1){
            long timeElapsedOnBoot = System.currentTimeMillis() - stopwatchData.get(0).getStopwatchTimerStartTimeInMillis();
            stopwatchData.get(0).setStopwatchTimerStartTimeInRealTimeMillis(SystemClock.elapsedRealtime()-timeElapsedOnBoot);

            if(stopwatchData.get(0).isPause() && stopwatchData.get(0).isRunning()){
                long pauseElapsedOnBoot = System.currentTimeMillis() - stopwatchData.get(0).getLastPauseTimeInMillis();
                stopwatchData.get(0).setLastPauseTimeRealTimeInMillis(SystemClock.elapsedRealtime()-pauseElapsedOnBoot);
            }

            if(tickTrackDatabase.retrieveStopwatchLapData().size()>0){
                long lapElapsedOnBoot = System.currentTimeMillis() - stopwatchData.get(0).getProgressSystemValue();
                stopwatchData.get(0).setProgressValue(SystemClock.elapsedRealtime()-lapElapsedOnBoot);
            }

            tickTrackDatabase.storeStopwatchData(stopwatchData);

            if(!isMyServiceRunning(StopwatchNotificationService.class, context)){
                startNotificationService(context);
            }
        }
    }

    public void startNotificationService(Context context) {
        Intent intent = new Intent(context, StopwatchNotificationService.class);
        intent.setAction(StopwatchNotificationService.ACTION_START_STOPWATCH_SERVICE);
        context.startService(intent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}

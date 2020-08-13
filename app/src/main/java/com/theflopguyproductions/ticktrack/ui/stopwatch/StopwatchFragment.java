package com.theflopguyproductions.ticktrack.ui.stopwatch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theflopguyproductions.ticktrack.R;
import com.theflopguyproductions.ticktrack.application.TickTrack;
import com.theflopguyproductions.ticktrack.counter.CounterAdapter;
import com.theflopguyproductions.ticktrack.stopwatch.StopwatchAdapter;
import com.theflopguyproductions.ticktrack.stopwatch.StopwatchData;
import com.theflopguyproductions.ticktrack.stopwatch.StopwatchLapData;
import com.theflopguyproductions.ticktrack.ui.utils.TickTrackAnimator;
import com.theflopguyproductions.ticktrack.ui.utils.TickTrackProgressBar;
import com.theflopguyproductions.ticktrack.utils.TickTrackDatabase;
import com.theflopguyproductions.ticktrack.utils.TickTrackStopwatchTimer;
import com.theflopguyproductions.ticktrack.utils.TickTrackThemeSetter;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.Context.MODE_PRIVATE;

public class StopwatchFragment extends Fragment {

    private ConstraintLayout stopwatchRootLayout, stopwatchLapLayout;
    private TextView stopwatchValueText, stopwatchLapTitleText, stopwatchMillisText;
    private RecyclerView stopwatchLapRecyclerView;
    private TickTrackProgressBar foregroundProgressBar, backgroundProgressBar;
    private FloatingActionButton playPauseFAB, flagFAB, resetFAB;

    private Activity activity;
    private TickTrackDatabase tickTrackDatabase;

    private ArrayList<StopwatchData> stopwatchDataArrayList = new ArrayList<>();
    private ArrayList<StopwatchLapData> stopwatchLapDataArrayList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private TickTrackStopwatchTimer tickTrackStopwatchTimer;

    private static StopwatchAdapter stopwatchAdapter;
    private Handler progressHandler = new Handler();

    private boolean isRunning = false;
    private float getCurrentStep(long currentValue, long maxLength){
        return ((currentValue-0f)/(maxLength-0f)) *(1f-0f)+0f;
    }
    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, s) ->  {
        stopwatchDataArrayList = tickTrackDatabase.retrieveStopwatchData();
        stopwatchLapDataArrayList = tickTrackDatabase.retrieveStopwatchLapData();
        if (s.equals("StopwatchLapData")){
            if(stopwatchLapDataArrayList.size()>0){
                Collections.sort(stopwatchLapDataArrayList);
                stopwatchAdapter.diffUtilsChangeData(stopwatchLapDataArrayList);
            }
            buildRecyclerView(activity);
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        activity = getActivity();
        initVariables(root);
        initValues();

        checkConditions();

        buildRecyclerView(activity);

        setupClickListeners();

        sharedPreferences = activity.getSharedPreferences("TickTrackData", MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        return root;
    }

    private void buildRecyclerView(Activity activity) {

        stopwatchAdapter = new StopwatchAdapter(stopwatchLapDataArrayList);

        if(stopwatchLapDataArrayList.size()>0){

            stopwatchLapLayout.setVisibility(View.VISIBLE);

            Collections.sort(stopwatchLapDataArrayList);

            stopwatchLapRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            stopwatchLapRecyclerView.setItemAnimator(new DefaultItemAnimator());
            stopwatchLapRecyclerView.setAdapter(stopwatchAdapter);

            stopwatchAdapter.diffUtilsChangeData(stopwatchLapDataArrayList);

        } else {
            stopwatchLapLayout.setVisibility(View.GONE);
        }

    }

    private void checkConditions() {
        if(stopwatchLapDataArrayList.size()>0){
            stopwatchLapLayout.setVisibility(View.VISIBLE);
        } else {
            stopwatchLapLayout.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {

        playPauseFAB.setOnClickListener(view -> {
            if(!isRunning){
                startStopwatch();
                isRunning = true;
            } else {
                pauseStopwatch();
                isRunning = false;
            }
        });

        resetFAB.setOnClickListener(view -> resetStopwatch());
        flagFAB.setOnClickListener(view -> lapStopwatch());
    }

    private void lapStopwatch() {
        if(tickTrackStopwatchTimer.isStarted()){
            tickTrackStopwatchTimer.lap();
            if(progressHandler!=null)
                progressHandler.removeCallbacks(refreshRunnable);

            startProgressBar();
            currentValue=0;
        }
    }

    private void startProgressBar() {
        foregroundProgressBar.setInstantProgress(0);
        progressHandler.post(refreshRunnable);
    }
    private void startProgressBar(long currentValue) {
        foregroundProgressBar.setInstantProgress(getCurrentStep(currentValue, maxProgressDurationInMillis));
        progressHandler.post(refreshRunnable);
    }

    private long currentValue = 0, maxProgressDurationInMillis = 200;
    final Runnable refreshRunnable = new Runnable() {
        public void run() {
            if(currentValue<=maxProgressDurationInMillis){
                foregroundProgressBar.setProgress(getCurrentStep(currentValue, maxProgressDurationInMillis));
                currentValue = currentValue + 1;
            } else {
                currentValue = 0;
            }
            progressHandler.post(refreshRunnable);
        }
    };

    private void stopProgressBar() {
        progressHandler.removeCallbacks(refreshRunnable);
        foregroundProgressBar.setProgress(0);
    }

    private void resetStopwatch() {
        if (tickTrackStopwatchTimer.isStarted()){
            tickTrackStopwatchTimer.stop();
            stopProgressBar();
        }
    }

    private void pauseStopwatch() {
        TickTrackAnimator.fabBounce(playPauseFAB, ContextCompat.getDrawable(activity, R.drawable.ic_round_play_white_24));
        TickTrackAnimator.fabUnDissolve(resetFAB);
        TickTrackAnimator.fabDissolve(flagFAB);
        if(tickTrackStopwatchTimer.isStarted() && !tickTrackStopwatchTimer.isPaused()){
            tickTrackStopwatchTimer.pause();
        }
        if(progressHandler!=null)
            progressHandler.removeCallbacks(refreshRunnable);
    }

    private void startStopwatch() {
        TickTrackAnimator.fabBounce(playPauseFAB, ContextCompat.getDrawable(activity, R.drawable.ic_round_pause_white_24));
        TickTrackAnimator.fabDissolve(resetFAB);
        TickTrackAnimator.fabUnDissolve(flagFAB);
        if(!tickTrackStopwatchTimer.isStarted()){
            tickTrackStopwatchTimer.start();
        } else if (tickTrackStopwatchTimer.isPaused()){
            tickTrackStopwatchTimer.resume();
            if(!(currentValue > 0) && tickTrackDatabase.retrieveStopwatchLapData().size()>0){
                currentValue=0;
                startProgressBar();
            } else {
                startProgressBar(currentValue);
            }
        }
    }

    private void initValues() {
        tickTrackDatabase = new TickTrackDatabase(activity);
        stopwatchDataArrayList = tickTrackDatabase.retrieveStopwatchData();
        stopwatchLapDataArrayList = tickTrackDatabase.retrieveStopwatchLapData();
        tickTrackStopwatchTimer = new TickTrackStopwatchTimer(tickTrackDatabase);
        tickTrackStopwatchTimer.setTextView(stopwatchValueText, stopwatchMillisText);

        TickTrackAnimator.fabBounce(playPauseFAB, ContextCompat.getDrawable(activity, R.drawable.ic_round_play_white_24));
        TickTrackAnimator.fabDissolve(resetFAB);
        TickTrackAnimator.fabDissolve(flagFAB);
    }

    private void initVariables(View parent) {

        stopwatchRootLayout = parent.findViewById(R.id.stopwatchRootLayout);
        stopwatchLapLayout = parent.findViewById(R.id.stopwatchFragmentLapLayout);
        stopwatchValueText = parent.findViewById(R.id.stopwatchFragmentTimeTextView);
        stopwatchMillisText = parent.findViewById(R.id.stopwatchFragmentMillisTextView);
        stopwatchLapTitleText = parent.findViewById(R.id.stopwatchFragmentLapTextView);
        stopwatchLapRecyclerView = parent.findViewById(R.id.stopwatchFragmentRecyclerView);
        foregroundProgressBar = parent.findViewById(R.id.stopwatchFragmentProgressForeground);
        backgroundProgressBar = parent.findViewById(R.id.stopwatchFragmentProgressBackground);
        playPauseFAB = parent.findViewById(R.id.stopwatchFragmentPlayPauseFAB);
        flagFAB = parent.findViewById(R.id.stopwatchFragmentFlagFAB);
        resetFAB = parent.findViewById(R.id.stopwatchFragmentResetFAB);

        backgroundProgressBar.setInstantProgress(1);
        foregroundProgressBar.setLinearProgress(true);
        foregroundProgressBar.setSpinSpeed(2.500f);
    }

    @Override
    public void onStart() {
        super.onStart();
        TickTrackThemeSetter.stopwatchFragmentTheme(activity, stopwatchRootLayout, stopwatchLapTitleText, stopwatchValueText,
                tickTrackDatabase, backgroundProgressBar, stopwatchMillisText);
        sharedPreferences = activity.getSharedPreferences("TickTrackData", MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        sharedPreferences = activity.getSharedPreferences("TickTrackData", MODE_PRIVATE);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }
}
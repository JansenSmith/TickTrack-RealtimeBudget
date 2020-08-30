package com.theflopguyproductions.ticktrack.startup.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.theflopguyproductions.ticktrack.R;
import com.theflopguyproductions.ticktrack.dialogs.ProgressBarDialog;
import com.theflopguyproductions.ticktrack.service.BackupRestoreService;
import com.theflopguyproductions.ticktrack.settings.SettingsActivity;
import com.theflopguyproductions.ticktrack.startup.StartUpActivity;
import com.theflopguyproductions.ticktrack.utils.database.TickTrackDatabase;
import com.theflopguyproductions.ticktrack.utils.database.TickTrackFirebaseDatabase;
import com.theflopguyproductions.ticktrack.utils.helpers.FirebaseHelper;

public class RestoreFragment extends Fragment {

    private TickTrackDatabase tickTrackDatabase;
    private FirebaseHelper firebaseHelper;
    private TickTrackFirebaseDatabase tickTrackFirebaseDatabase;

    private TextView mainTitle, subTitle, dataReadyTitle, preferencesText, timersText, countersText, restoreQuestionText;
    private Button restoreDataButton, startFreshButton;
    private CheckBox preferencesCheck, timersCheck, countersCheck;
    private SharedPreferences sharedPreferences;

    private ProgressBarDialog progressBarDialog;

    private Activity activity;

    private String receivedAction;

    public RestoreFragment(String receivedAction) {
        this.receivedAction = receivedAction;
    }

    @Override
    public void onStart() {
        super.onStart();
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        tickTrackDatabase.storeCurrentFragmentNumber(3);
    }

    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, s) -> databaseChangeListener();

    private void databaseChangeListener() {
        setupChanges();
        checkRestoreMode();
    }

    private void checkRestoreMode() {
        if(!tickTrackFirebaseDatabase.isRestoreInitMode()){
            stopRestoreService();
            setupOptionsDisplay();
        }
    }

    private void setupOptionsDisplay() {
        restoreQuestionText.setVisibility(View.VISIBLE);
        restoreDataButton.setVisibility(View.VISIBLE);
        startFreshButton.setVisibility(View.VISIBLE);
        if(tickTrackFirebaseDatabase.hasPreferencesDataBackup()){
            preferencesCheck.setVisibility(View.VISIBLE);
        }
        if(tickTrackFirebaseDatabase.getRetrievedCounterCount()!=-1){
            countersCheck.setVisibility(View.VISIBLE);
        }
        if(tickTrackFirebaseDatabase.getRetrievedTimerCount()!=-1){
            timersCheck.setVisibility(View.VISIBLE);
        }
    }

    private void setupChanges() {
        if(tickTrackFirebaseDatabase.hasPreferencesDataBackup()){
            preferencesText.setVisibility(View.VISIBLE);
            preferencesText.setText("Preferences retrieved");
            dataReadyTitle.setVisibility(View.VISIBLE);
        }
        if(tickTrackFirebaseDatabase.getRetrievedCounterCount()!=-1){
            dataReadyTitle.setVisibility(View.VISIBLE);
            countersText.setVisibility(View.VISIBLE);
            countersText.setText("Retrieved "+tickTrackFirebaseDatabase.getRetrievedCounterCount()+" counter data");
        }
        if(tickTrackFirebaseDatabase.getRetrievedTimerCount()!=-1){
            dataReadyTitle.setVisibility(View.VISIBLE);
            timersText.setVisibility(View.VISIBLE);
            timersText.setText("Retrieved "+tickTrackFirebaseDatabase.getRetrievedTimerCount()+" timer data");
        }
    }

    private void initVariables(View root) {
        mainTitle = root.findViewById(R.id.restoreFragmentTitleText);
        subTitle = root.findViewById(R.id.restoreFragmentSubtitleText);
        dataReadyTitle = root.findViewById(R.id.restoreFragmentDataReadyText);
        preferencesText = root.findViewById(R.id.restoreFragmentPreferencesText);
        timersText = root.findViewById(R.id.restoreFragmentTimerText);
        countersText = root.findViewById(R.id.restoreFragmentCounterText);
        restoreQuestionText = root.findViewById(R.id.restoreFragmentRestoreOptionsTitle);
        restoreDataButton = root.findViewById(R.id.restoreFragmentRestoreDataButton);
        startFreshButton = root.findViewById(R.id.restoreFragmentStartFreshButton);
        preferencesCheck = root.findViewById(R.id.restoreFragmentPreferencesCheckBox);
        countersCheck = root.findViewById(R.id.restoreFragmentCounterCheckBox);
        timersCheck = root.findViewById(R.id.restoreFragmentTimerCheckBox);

        activity = getActivity();

        progressBarDialog = new ProgressBarDialog(activity);
        firebaseHelper = new FirebaseHelper(getActivity());
        firebaseHelper.setAction(receivedAction);
        System.out.println("RESTORE ACTIVITY RECEIVED "+receivedAction);

        tickTrackDatabase = new TickTrackDatabase(getContext());
        tickTrackFirebaseDatabase = new TickTrackFirebaseDatabase(getContext());
        sharedPreferences = tickTrackDatabase.getSharedPref(getContext());

        restoreDataButton.setOnClickListener(view -> {
            startRestoreDataService();
            if(receivedAction.equals(StartUpActivity.ACTION_SETTINGS_ACCOUNT_ADD)){
                Intent intent = new Intent(requireContext(), SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                requireContext().startActivity(intent);
            } else {
                startFreshListener.onStartFreshClickListener(false);
            }
        });
        startFreshButton.setOnClickListener(view -> {
            stopRestoreService();
            startFreshListener.onStartFreshClickListener(true);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_ticktrack_restore, container, false);

        initVariables(root);

        progressBarDialog = new ProgressBarDialog(activity);
        progressBarDialog.show();
        progressBarDialog.setContentText("Checking for backup");
        progressBarDialog.titleText.setVisibility(View.GONE);
        startRestoreInitService();

        return root;
    }

    private void stopRestoreService() {
        progressBarDialog.dismiss();
        tickTrackFirebaseDatabase.setRestoreInitMode(true);
        Intent intent = new Intent(activity, BackupRestoreService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(BackupRestoreService.RESTORE_SERVICE_STOP_FOREGROUND);
        intent.putExtra("receivedAction", receivedAction);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(intent);
        } else {
            activity.startService(intent);
        }
    }

    private void startRestoreInitService() {
        tickTrackFirebaseDatabase.setRestoreInitMode(true);
        Intent intent = new Intent(activity, BackupRestoreService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(BackupRestoreService.RESTORE_SERVICE_START_INIT_RETRIEVE);
        intent.putExtra("receivedAction", receivedAction);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(intent);
        } else {
            activity.startService(intent);
        }
    }

    private void startRestoreDataService() {
        Intent intent = new Intent(activity, BackupRestoreService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(BackupRestoreService.RESTORE_SERVICE_START_RESTORE);
        intent.putExtra("receivedAction", receivedAction);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(intent);
        } else {
            activity.startService(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPreferences = tickTrackDatabase.getSharedPref(activity);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        tickTrackDatabase.storeCurrentFragmentNumber(3);
    }

    private StartFreshListener startFreshListener;

    public interface StartFreshListener {
        void onStartFreshClickListener(boolean nextFragment);
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            startFreshListener = (StartFreshListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + StartFreshListener.class.getName());
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        startFreshListener = null;
    }
}
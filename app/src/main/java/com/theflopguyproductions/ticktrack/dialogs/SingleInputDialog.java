package com.theflopguyproductions.ticktrack.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.theflopguyproductions.ticktrack.R;
import com.theflopguyproductions.ticktrack.utils.database.TickTrackDatabase;

import java.util.Objects;

public class SingleInputDialog extends Dialog {

    private Activity activity;
    public EditText inputText;
    public Button okButton, cancelButton;
    public TextView helperText, saveChangesText;
    private String currentLabel;
    private TickTrackDatabase tickTrackDatabase;
    private ConstraintLayout rootLayout;
    int themeSet = 1;

    public SingleInputDialog(Activity activity, int style, String currentLabel){
        super(activity, style);
        this.activity = activity;
        this.currentLabel = currentLabel;
    }

    public SingleInputDialog(Activity activity, String currentLabel){
        super(activity);
        this.activity = activity;
        this.currentLabel = currentLabel;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = activity.getLayoutInflater().inflate(R.layout.dialog_text_item, new ConstraintLayout(activity), false);
        setContentView(view);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

//        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        DisplayMetrics metrics = new DisplayMetrics();
//        display.getMetrics(metrics);
//        int width = Math.min(metrics.widthPixels, 1280);
//        int height = -1; // MATCH_PARENT
//        getWindow().setLayout(width, height);

        initVariables(view);

        tickTrackDatabase = new TickTrackDatabase(activity);
        themeSet = tickTrackDatabase.getThemeMode();

        setupTheme();

    }

    private void setupTheme() {
        if(themeSet==1){
            rootLayout.setBackgroundResource(R.color.LightGray);
            helperText.setTextColor(activity.getResources().getColor(R.color.DarkText));
            helperText.setBackgroundResource(R.color.LightGray);
            okButton.setBackgroundResource(R.drawable.button_selector_light);
            cancelButton.setBackgroundResource(R.drawable.button_selector_light);
            inputText.setTextColor(activity.getResources().getColor(R.color.DarkText));
            inputText.setHintTextColor(activity.getResources().getColor(R.color.GrayOnDark));
        } else {
            rootLayout.setBackgroundResource(R.color.Gray);
            helperText.setBackgroundResource(R.color.Gray);
            helperText.setTextColor(activity.getResources().getColor(R.color.LightText));
            okButton.setBackgroundResource(R.drawable.button_selector_dark);
            cancelButton.setBackgroundResource(R.drawable.button_selector_dark);
            inputText.setTextColor(activity.getResources().getColor(R.color.LightText));
            inputText.setHintTextColor(activity.getResources().getColor(R.color.GrayOnLight));
        }
    }

    private void initVariables(View view) {
        okButton = view.findViewById(R.id.labelDialogOkButton);
        cancelButton = view.findViewById(R.id.labelDialogCancelButton);
        inputText = view.findViewById(R.id.labelDialogInputText);
        helperText = view.findViewById(R.id.labelDialogHelpText);
        inputText.setHint(currentLabel);
        saveChangesText = findViewById(R.id.labelDialogSaveChangesText);
        rootLayout = findViewById(R.id.singleItemDialogRootLayout);
    }

}

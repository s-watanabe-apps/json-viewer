package com.swapps.jsonviewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {

    SharedPreferences preferences;
    int background;
    int storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        background = preferences.getInt("background", 0);
        storage = preferences.getInt("storage", 0);

        // 背景の設定
        RadioGroup radioGroupBackground = findViewById(R.id.radioGroupBackground);
        radioGroupBackground.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioBackgroundWhite) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("background", 0);
                    editor.apply();
                } else{
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("background", 1);
                    editor.apply();
                }
            }
        });

        final RadioButton radioButtonBackgroundWhite = findViewById(R.id.radioBackgroundWhite);
        final RadioButton radioButtonBackgroundBlack = findViewById(R.id.radioBackgroundBlack);
        if(background == 0) {
            radioButtonBackgroundWhite.setChecked(true);
        } else{
            radioButtonBackgroundBlack.setChecked(true);
        }


        ImageView imageBackgroundWhite = findViewById(R.id.imageBackgroundWhite);
        imageBackgroundWhite.setBackgroundResource(R.drawable.border);
        imageBackgroundWhite.setImageResource(R.drawable.background_white);
        imageBackgroundWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonBackgroundWhite.setChecked(true);
            }
        });

        ImageView imageBackgroundBlack = findViewById(R.id.imageBackgroundBlack);
        imageBackgroundBlack.setBackgroundResource(R.drawable.border);
        imageBackgroundBlack.setImageResource(R.drawable.background_black);
        imageBackgroundBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonBackgroundBlack.setChecked(true);
            }
        });

        // 検索する場所の設定
        RadioGroup radioGroupStorage = findViewById(R.id.radioGroupStorage);
        radioGroupStorage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioInternalStorage) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("storage", 0);
                    editor.apply();
                } else{
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("storage", 1);
                    editor.apply();
                }
            }
        });

        final RadioButton radioButtonInternalStorage = findViewById(R.id.radioInternalStorage);
        final RadioButton radioButtonExternalStorage = findViewById(R.id.radioExternalStorage);
        if(storage == 0) {
            radioButtonInternalStorage.setChecked(true);
        } else{
            radioButtonExternalStorage.setChecked(true);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            int result = RESULT_CANCELED;
            if(storage != preferences.getInt("storage", 0)) {
                result = RESULT_OK;
            }
            Intent intent = new Intent();
            setResult(result, intent);
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}

package com.ihewro.focus.activity;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.ihewro.focus.R;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.task.AppStartTask;

import skin.support.utils.SkinPreference;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(SkinPreference.getInstance().getSkinName().equals("night")){
            super.setTheme(R.style.AppTheme_DayNight);
        }else {
            super.setTheme(R.style.AppTheme_DayNight);
        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏

        setContentView(R.layout.activity_welcome);


        if (UserPreference.queryValueByKey(UserPreference.FIST_USE_NEW_COMER, "0").equals("0")){
            finish();
            NewComerActivity.Companion.activityStart(this);
        }else {
            Intent intent = getIntent();

            if (!isTaskRoot()) {
                finish();
                return;
            }

            ImageView imageview= findViewById(R.id.imageView);
            AnimatedVectorDrawable animatedVectorDrawable =  ((AnimatedVectorDrawable) imageview.getDrawable());
            animatedVectorDrawable.start();

            //跳转到 {@link MainActivity}
            new AppStartTask(jsonString -> {

                finish();
                MainActivity.activityStart(SplashActivity.this);

            }).execute();
        }
    }
}

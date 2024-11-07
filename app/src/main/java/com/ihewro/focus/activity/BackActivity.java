package com.ihewro.focus.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ihewro.focus.R;
import com.ihewro.focus.util.StatusBarUtil;
import com.parfoismeng.slidebacklib.SlideBackKt;

import skin.support.utils.SkinPreference;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/05/06
 *     desc   :
 *     version: 1.0
 * </pre>
 */
@SuppressLint("Registered")
public class BackActivity extends AppCompatActivity {


    @Override
    public void setContentView(int layoutResID) {
        if(SkinPreference.getInstance().getSkinName().equals("night")){
            setTheme(R.style.AppTheme_DayNight);
        }else {
            setTheme(R.style.AppTheme_DayNight);
        }
        super.setContentView(layoutResID);

        if(!SkinPreference.getInstance().getSkinName().equals("night")){
//            StatusBarUtil.setLightMode(this);
            StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary),0);
        }
    }



    /**
     * 点击toolbar上的按钮事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SlideBackKt.registerSlideBack(this, true, () -> {
            getOnBackPressedDispatcher().onBackPressed();
            return null;
        });


    }

    @Override
    protected void onDestroy() {
        // 解绑
        SlideBackKt.unregisterSlideBack(this);
        super.onDestroy();
    }
}

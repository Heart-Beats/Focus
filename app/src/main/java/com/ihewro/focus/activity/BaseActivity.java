package com.ihewro.focus.activity;

import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.appcompat.app.AppCompatActivity;

import com.ihewro.focus.R;
import com.ihewro.focus.util.StatusBarUtil;

import skin.support.utils.SkinPreference;

/**
 * <pre>
 *     author : hewro
 *     e-mail : ihewro@163.com
 *     time   : 2019/05/14
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    public void setContentView(int layoutResID) {
        if(SkinPreference.getInstance().getSkinName().equals("night")){
            super.setTheme(R.style.AppTheme_DayNight);
        }else {
            super.setTheme(R.style.AppTheme_DayNight);
            StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary),0);

        }
        super.setContentView(layoutResID);
    }

    /**
     * 设置 app 字体不随系统字体设置改变
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res != null) {
            Configuration config = res.getConfiguration();
            if (config != null && config.fontScale != 1.0f) {
                config.fontScale = 1.0f;
                res.updateConfiguration(config, res.getDisplayMetrics());
            }
        }
        return res;
    }
}

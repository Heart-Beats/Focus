package com.ihewro.focus.fragemnt.setting;


import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.text.InputType;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.ALog;
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.R;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.task.TimingService;

import java.util.List;

import es.dmoral.toasty.Toasty;

/**
 * 同步的设置
 */
public class SynchroFragment extends SettingFragment {


    private SwitchPreferenceCompat use_internet_while_open;
    private Preference choose_rsshub;
    private SwitchPreferenceCompat auto_name;

    private Preference ownrsshub;
    private Preference time_interval;
    private SwitchPreferenceCompat only_wifi;


    public SynchroFragment() {
    }


    @Override
    public void initView() {
        addPreferencesFromResource(R.xml.pref_synchro_setting);
    }

    @Override
    public void initPreferenceComponent() {
        use_internet_while_open = (SwitchPreferenceCompat)findPreference(getString(R.string.pref_key_use_internet_while_open));
        choose_rsshub = findPreference(getString(R.string.pref_key_rsshub_choice));
        auto_name = (SwitchPreferenceCompat) findPreference(getString(R.string.pref_key_auto_name));

        ownrsshub = findPreference(getString(R.string.pref_key_own_rsshub));
        time_interval = findPreference(getString(R.string.pref_key_refresh_interval));

        only_wifi = (SwitchPreferenceCompat) findPreference(getString(R.string.pref_key_only_use_wifi));
    }


    @Override
    public void initPreferencesData() {
        //查询数据库
        if (UserPreference.queryValueByKey(UserPreference.USE_INTERNET_WHILE_OPEN,"0").equals("0")){
            use_internet_while_open.setChecked(false);
        }else {
            use_internet_while_open.setChecked(true);
        }

        if (UserPreference.queryValueByKey(UserPreference.AUTO_SET_FEED_NAME,"0").equals("0")){
            auto_name.setChecked(false);
        }else {
            auto_name.setChecked(true);
        }


        if (UserPreference.queryValueByKey(UserPreference.notWifi,"0").equals("0")){
            only_wifi.setChecked(false);
        }else {
            only_wifi.setChecked(true);
        }


        final int pos = GlobalConfig.rssHub.indexOf(UserPreference.queryValueByKey(UserPreference.RSS_HUB,GlobalConfig.OfficialRSSHUB));
        if (pos != 2){//如果不是自定义源，则自定义源应该禁止操作
            ownrsshub.setEnabled(false);
        }else {//否则自定义源可以操作
            ownrsshub.setEnabled(true);
        }


    }

    @Override
    public void initListener() {
        time_interval.setOnPreferenceClickListener(preference -> {
            final int select = GlobalConfig.refreshIntervalInt.indexOf(Integer.parseInt(UserPreference.queryValueByKey(UserPreference.tim_interval, String.valueOf(-1))));//默认选择最后一项，即-1

            new MaterialDialog.Builder(getActivity())
                    .title("选择刷新间隔")
                    .items(GlobalConfig.refreshInterval)
                    .itemsCallbackSingleChoice(select, (dialog, view, which, text) -> {
                        UserPreference.updateOrSaveValueByKey(UserPreference.tim_interval, String.valueOf(GlobalConfig.refreshIntervalInt.get(which)));
                        TimingService.startService(getActivity(),true);
                        return false;
                    })

                    .positiveText("选择")
                    .show();


            return false;
        });

        ownrsshub.setOnPreferenceClickListener(preference -> {

            new MaterialDialog.Builder(getActivity())
                    .title("填写自定义RSSHub源")
                    .content("输入你的地址：")
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input("", UserPreference.queryValueByKey(UserPreference.OWN_RSSHUB, GlobalConfig.OfficialRSSHUB), (dialog, input) -> {
                        String name = dialog.getInputEditText().getText().toString().trim();
                        if (name.equals("")) {
                            Toasty.info(getActivity(), "请勿为空😯").show();
                        } else {
                            UserPreference.updateOrSaveValueByKey(UserPreference.OWN_RSSHUB, dialog.getInputEditText().getText().toString().trim());
                            Toasty.success(getActivity(), "填写成功").show();
                        }
                    }).show();


            return false;
        });

        use_internet_while_open.setOnPreferenceClickListener(preference -> {
            if (use_internet_while_open.isChecked()){
                UserPreference.updateOrSaveValueByKey(UserPreference.USE_INTERNET_WHILE_OPEN,"1");
            }else {
                UserPreference.updateOrSaveValueByKey(UserPreference.USE_INTERNET_WHILE_OPEN,"0");
            }
            return false;
        });


        only_wifi.setOnPreferenceClickListener(preference -> {
            if (only_wifi.isChecked()){
                UserPreference.updateOrSaveValueByKey(UserPreference.notWifi,"1");
            }else {
                UserPreference.updateOrSaveValueByKey(UserPreference.notWifi,"0");
            }
            return false;
        });


        choose_rsshub.setOnPreferenceClickListener(preference -> {
            //显示弹窗
            //之前选择的位置
            final int select = GlobalConfig.rssHub.indexOf(UserPreference.queryValueByKey(UserPreference.RSS_HUB,GlobalConfig.OfficialRSSHUB));
            ALog.d(UserPreference.getRssHubUrl());
            List<String> list = GlobalConfig.rssHub;
            new MaterialDialog.Builder(getActivity())
                    .title("rsshub源选择")
                    .items(list)
                    .itemsCallbackSingleChoice(select, (dialog, view, which, text) -> {
                        if (which>=0 && which<3){
                            UserPreference.updateOrSaveValueByKey(UserPreference.RSS_HUB,GlobalConfig.rssHub.get(which));
                            if (which!=2){
                                ownrsshub.setEnabled(false);
                            }else {
                                ownrsshub.setEnabled(true);
                            }
                            return true;
                        }
                        return false;
                    })
                    .positiveText("选择")
                    .show();

            return false;
        });

        auto_name.setOnPreferenceClickListener(preference -> {
            if (auto_name.isChecked()){
                UserPreference.updateOrSaveValueByKey(UserPreference.AUTO_SET_FEED_NAME,"1");
            }else {
                UserPreference.updateOrSaveValueByKey(UserPreference.AUTO_SET_FEED_NAME,"0");
            }
            return false;
        });
    }



}

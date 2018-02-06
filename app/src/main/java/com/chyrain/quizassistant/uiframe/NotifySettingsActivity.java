package com.chyrain.quizassistant.uiframe;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.R;
import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.util.Logger;
import com.tencent.android.tpush.XGPushConfig;

import java.util.Calendar;

public class NotifySettingsActivity extends BaseSettingsActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String key = XGPushConfig.getToken(this);
        if (TextUtils.isEmpty(key)) {
            key = "AD1303753897" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        } else if (key.length() > 16) {
            key = key.substring(0, 16);
        }
        Logger.i("", "Test :" + "AD1303753897" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        Logger.i("", "Test key = " + key);
//        // [广告]在线参数(广告key为token或者密码+日期)
//        AdManager.getInstance(this).asyncGetOnlineConfig(key, new OnlineConfigCallBack() {
//            @Override
//            public void onGetOnlineConfigSuccessful(String key, String value) {
//                // TODO Auto-generated method stub
//                // 获取在线参数成功
//                Logger.i("", "获取在线参数成功:" + key + "->" + value);
//                if (key != null) {
//                    boolean ad = Boolean.valueOf(value);
//                    if (!ad) {
//                        Config.getConfig(getApplicationContext()).saveBoolean("controlAd", true);
//                    }
//                }
//            }
//
//            @Override
//            public void onGetOnlineConfigFailed(String key) {
//                // TODO Auto-generated method stub
//                // 获取在线参数失败，可能原因有：键值未设置或为空、网络异常、服务器异常
//                Logger.e("", "获取在线参数失败:" + key);
//            }
//        });
    }

    @Override
    public Fragment getSettingsFragment() {
        return new NotifySettingsFragment();
    }

    public static class NotifySettingsFragment extends BaseSettingsFragment {

        Config config;
        SwitchPreference adPref;

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            config = Config.getConfig(getActivity());

            addPreferencesFromResource(R.xml.notify_settings);
            final PreferenceCategory categoryPref = (PreferenceCategory) getPreferenceScreen().getPreference(0);

            //西瓜视频开关
            adPref = (SwitchPreference) findPreference(Config.KEY_ENABLE_AD);
            adPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    Config.getConfig(getActivity()).setEnableAd((Boolean) newValue);
                    // 显示广告去除提示
                    if(!(Boolean) newValue && !Config.getConfig(getActivity()).readBoolean("controlAd")) {
                        ((NotifySettingsActivity)getActivity()).showOpenOverlayAdtipsDialog();
                        return false;
                    }
                    return true;
                }
            });

            findPreference(Config.KEY_NOTIFY_SOUND).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    V5Application.eventStatistics(getActivity(), "notify_sound", String.valueOf(newValue));
                    return true;
                }
            });

            findPreference(Config.KEY_NOTIFY_VIBRATE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    V5Application.eventStatistics(getActivity(), "notify_vibrate", String.valueOf(newValue));
                    return true;
                }
            });

            final EditTextPreference delayEditTextPre = (EditTextPreference) findPreference(Config.KEY_THROTTLE_TIME);
            delayEditTextPre.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int time = 0;
                    try {
                        time = Integer.valueOf(String.valueOf(newValue));
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    if(time < 500 || time > 3000) {
                        ((NotifySettingsActivity) getActivity()).ShowToast("设置失败，时间间隔建议在500-3000ms范围");
                        return false;
                    }
                    preference.setSummary("间隔" + time + "毫秒" +
                    "（循环监控当前题号的答案，时间间隔建议在500-3000范围，频率太高耗流量多，太低获取答案时效慢）");
                    V5Application.eventStatistics(getActivity(), "pull_delay_time", String.valueOf(newValue));
                    return true;
                }
            });
            String delay = delayEditTextPre.getText();
            delayEditTextPre.setSummary("间隔" + delay  + "毫秒" +
                    "（循环监控当前题号的答案，时间间隔建议在500-3000范围，频率太高耗流量多，太低获取答案时效慢）");

            // 答不上时处理模式
            final ListPreference wxMode = (ListPreference) findPreference(Config.KEY_NOANSWER_MODE);
            wxMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int value = Integer.parseInt(String.valueOf(newValue));
                    preference.setSummary(wxMode.getEntries()[value]);
                    V5Application.eventStatistics(getActivity(), "noanswer_mode", String.valueOf(newValue));
                    return true;
                }
            });
            wxMode.setSummary(wxMode.getEntries()[Integer.parseInt(wxMode.getValue())]);
        }
    }

    private void showOpenOverlayAdtipsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.dialog_title);
        builder.setMessage(getString(R.string.overlay_ad_tips));
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO
            }
        });
        builder.show();
    }
}

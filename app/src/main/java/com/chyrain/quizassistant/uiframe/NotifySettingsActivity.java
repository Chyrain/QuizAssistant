package com.chyrain.quizassistant.uiframe;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.R;
import com.chyrain.quizassistant.V5Application;
import com.chyrain.quizassistant.service.WxBotService;
import com.chyrain.quizassistant.util.Util;

public class NotifySettingsActivity extends BaseSettingsActivity {

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
                    Config.getConfig(getActivity()).setEnableXigua((Boolean) newValue);
                    if(!(Boolean) newValue) {
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

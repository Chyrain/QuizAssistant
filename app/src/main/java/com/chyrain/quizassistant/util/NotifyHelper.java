package com.chyrain.quizassistant.util;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.WindowManager.LayoutParams;

import com.chyrain.quizassistant.Config;
import com.chyrain.quizassistant.R;

/**
 * <p>Created 16/2/5 下午9:48.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public class NotifyHelper {

    private static Vibrator sVibrator;
    private static KeyguardManager sKeyguardManager;
    private static PowerManager sPowerManager;
    public static SoundPool sSoundPool;// = new SoundPool(10, AudioManager.STREAM_RING,5);

    /** 播放声音*/
    public static void sound(Context context, int resId) {
        try {
//        	if (null == sSoundPool) {
//        		sSoundPool = new SoundPool(10, AudioManager.STREAM_RING, 0);
//            	sSoundPool.load(context, resId,1);
//        	}
            sSoundPool = new SoundPool(10, AudioManager.STREAM_RING, 0);
            sSoundPool.load(context, resId,1);
        	int rst = sSoundPool.play(1, 1, 1, 0, 0, 1);
        	Logger.i("NotifyHelper", "播放提示音"+rst);
        	if (rst == 0) {
	            MediaPlayer player = 
	                    MediaPlayer.create(context, resId > 0 ? resId : R.raw.start);
	            player.start();
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 振动*/
    public static void vibrator(Context context) {
        if(sVibrator == null) {
            sVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        sVibrator.vibrate(new long[]{100, 10, 100, 1000}, -1);
    }

    /** 是否为夜间*/
    public static  boolean isNightTime(Context context) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(hour >= Config.getConfig(context).getNotifyNightStart() || hour < Config.getConfig(context).getNotifyNightEnd()) {
            return true;
        }
        return false;
    }

    public static KeyguardManager getKeyguardManager(Context context) {
        if(sKeyguardManager == null) {
            sKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        }
        return sKeyguardManager;
    }

    public static PowerManager getPowerManager(Context context) {
        if(sPowerManager == null) {
            sPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        }
        return sPowerManager;
    }

    /** 是否为锁屏或黑屏状态*/
    public static boolean isLockScreen(Context context) {
        KeyguardManager km = getKeyguardManager(context);

        return km.inKeyguardRestrictedInputMode() || !isScreenOn(context);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH) 
    public static boolean isScreenOn(Context context) {
        PowerManager pm = getPowerManager(context);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return pm.isInteractive();
        } else {
            return pm.isScreenOn();
        }
    }
    
    /**
     * 解锁屏幕
     * @param context
     */
	public static void wakeUpAndUnlock(Context context){  
        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);  
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");  
        //解锁  
        kl.disableKeyguard();  
        //获取电源管理器对象  
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);  
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag  
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");  
        //点亮屏幕  
        wl.acquire();  
        //释放  
        wl.release();  
    }
	
	public static void UnlockMe(Activity context) {
		if (null == context) {
			return;
		}
		Logger.i("NotifyHelper", "[UnlockMe]");
		// try to unlock the phone 
		context.getWindow().addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
		context.getWindow().addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		context.getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
  }

    /** 播放效果、声音与震动*/
    public static void playEffect(Context context, Config config, int sound) {
        //夜间模式，不处理
        if(NotifyHelper.isNightTime(context) && config.isNotifyNight()) {
            return;
        }
        // 播放声音
        if(config.isNotifySound() && sound >= 0) {
            sound(context, sound);
        }
        if(config.isNotifyVibrate()) {
            vibrator(context);
        }
    }

    /** 显示通知*/
    public static void showNotify(Context context, String title, PendingIntent pendingIntent) {
    	// TODO
    }

    /** 执行PendingIntent事件*/
    public static void send(PendingIntent pendingIntent) {
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}

package com.chyrain.quizassistant.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Util {

    /* 权限申请返回码 */
    public static final int REQUEST_PERMISSION_CAMERA = 101; // 拍照权限
    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 102; // 存储权限
    public static final int REQUEST_PERMISSION_RECORD_AUDIO = 103; // 录音权限
    public static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 104; // GPS位置权限
    public static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 105; // 网络位置权限
    public static final int REQUEST_PERMISSION_SYSTEM_ALERT_WINDOW = 106; // 网络位置权限
    public static final int REQUEST_PERMISSION_ALL = 100; // 全部权限

    /**
     * 内容是否包含答题信息
     * @param ticker 内容
     * @return
     */
    public static boolean shouldResponseToNotifyContent(String ticker) {
        return ticker.contains("答题开始") || ticker.contains("开始答题") || ticker.contains("答题就要开始")
                || ticker.contains("答题即将开始") || ticker.contains("答题马上开始")
                || ticker.contains("答题狂欢马上开始") || ticker.contains("本场奖金");
    }

    public static boolean isMatchChannel(String channel, String key) {
        if (key.equals(channel)) {
            return true;
        }
        switch (channel) {
            case "hj":
            case "xigua":
            case "cddh":
            case "zscr":
            case "hjsm":
                return true;
        }
        return false;
    }

    /**
     * dp转 px.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    public static int dp2px(float value, Context context) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }

    /**
     * px转dp.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    public static int px2dp(float value, Context context) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) ((value * 160) / scale + 0.5f);
    }

    /**
     * 获得文件大小
     * @param file 文件对象
     * @return filesize
     */
    public static long getFileSize(File file) {
        if ((file == null) || (!file.exists()))
            return 0L;
        if (!file.isDirectory())
            return file.length();
        List<File> dirs = new LinkedList<File>();
        dirs.add(file);
        long result = 0L;
        while (!dirs.isEmpty()) {
            File dir = (File)dirs.remove(0);
            if (!dir.exists())
                continue;
            File[] listFiles = dir.listFiles();
            if ((listFiles == null) || (listFiles.length == 0))
                continue;
            for (File child : listFiles) {
                result += child.length();
                if (child.isDirectory()) {
                    dirs.add(child);
                }
            }
        }
        return result;
    }

    /**
     * Bitmap图片转jpeg字节流
     * @param image
     * @param maxSize 单位为KB
     * @return
     */
    public static byte[] compressImageToByteArray(Bitmap image, int maxSize)
    {
        Logger.d("V5Util", "compressImage before>>:" + image.getRowBytes() * image.getHeight());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int options = 95;
        image.compress(Bitmap.CompressFormat.JPEG, options, bos);
        // Compress by loop
        while (bos.toByteArray().length / 1024 > maxSize) {
            // Clean up os
            bos.reset();
            // interval 10
            options -= 5;
            image.compress(Bitmap.CompressFormat.JPEG, options, bos);
        }
        Logger.d("V5Util", "compressImage(" + options + ")>>:" + bos.size());
        image.recycle();
        return bos.toByteArray();
    }

    /**
     * 读取图片文件并对150Kb以上图片进行像素压缩，返回Bitmap对象
     * @param imagePath
     * @return
     */
    public static Bitmap getCompressBitmap(String imagePath)
    {
        long size_file;
        try
        {
            size_file = getFileSize(new File(imagePath));
        } catch (Exception e) {
            size_file = 0L;
        }
        if (size_file == 0L) {
            return null;
        }
        size_file /= 1000L;
        Logger.d("Util", new StringBuilder().append("FileSize= ").append(size_file).toString());
        int ample_size = 1;

        if ((size_file <= 800L) && (size_file >= 400L))
        {
            ample_size = 2;
        }
        else if ((size_file > 801L) && (size_file < 1600L))
        {
            ample_size = 2;
        }
        else if ((size_file >= 1600L) && (size_file < 3200L))
        {
            ample_size = 4;
        }
        else if ((size_file >= 3200L) && (size_file <= 4800L))
        {
            ample_size = 4;
        }
        else if (size_file >= 4800L)
        {
            ample_size = 8;
        } else {
//	    	BitmapFactory.Options newOpts = new BitmapFactory.Options();
//	        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容
//	        newOpts.inJustDecodeBounds = false;
//	    	Bitmap bitmap = BitmapFactory.decodeFile(imagePath, newOpts);
//	    	return bitmap;
        }

        BitmapFactory.Options bitoption = new BitmapFactory.Options();
        bitoption.inJustDecodeBounds = true;
        Bitmap bitmapPhoto = BitmapFactory.decodeFile(imagePath, bitoption);
        bitoption.inJustDecodeBounds = false;
        bitoption.inSampleSize = ample_size;
        bitoption.inDither = false;    /*不进行图片抖动处理*/
        bitoption.inPreferredConfig = Bitmap.Config.RGB_565;  // 一个像素占两字节

        bitmapPhoto = BitmapFactory.decodeFile(imagePath, bitoption);
        if (null == bitmapPhoto) {
            return null;
        }
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = 0;
        if (exif != null) {
            orientation = exif.getAttributeInt("Orientation", 1);
        }
        Matrix matrix = new Matrix();
        Bitmap bitmap;
        if (orientation == 3) {
            matrix.postRotate(180.0F);
            bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);
        }
        else
        {
            if (orientation == 6) {
                matrix.postRotate(90.0F);
                bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);
            }
            else
            {
                if (orientation == 8) {
                    matrix.postRotate(270.0F);
                    bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);
                } else {
                    matrix.postRotate(0.0F);
                    bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0, bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix, true);
                }
            }
        }
        return bitmap;
    }


    private static final String TAG = "DeviceUtil";
    private static final int PERMISSION_REQUEST_CODE = Util.REQUEST_PERMISSION_SYSTEM_ALERT_WINDOW;
    private static final String KEY_EMUI_VERSION_CODE = "ro.build.version.emui";

    public static boolean isMIUI() {
        return android.os.Build.MODEL.toUpperCase(Locale.getDefault()).contains("MIUI") ||
                android.os.Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("xiaomi");
    }
    public static boolean isFlyme() {
        return android.os.Build.MODEL.toLowerCase(Locale.getDefault()).contains("flyme") ||
                android.os.Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("meizu");
    }
    public static boolean isHuawei() {
        return android.os.Build.MODEL.toLowerCase(Locale.getDefault()).contains("huawei") ||
                android.os.Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("huawei") ||
                isEMUI();
    }

    /**
     * 华为rom
     * @return
     */
    public static boolean isEMUI() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            return prop.getProperty(KEY_EMUI_VERSION_CODE, null) != null;
        } catch (final IOException e) {
            return false;
        }
    }

    /**
     * 小米设置状态栏字体颜色
     * @param darkmode
     * @param activity
     */
    public static void setStatusBarDarkModeOfMIUI(boolean darkmode, Activity activity) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            Logger.e("MIUI", "setStatusBarDarkIcon: failed");
            e.printStackTrace();
        }
    }

    /**
     * 魅族设置状态栏字体颜色
     * @param window
     * @param dark
     * @return
     */
    public static boolean setStatusBarDarkIconOfFlyme(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {
                Logger.e("MeiZu", "setStatusBarDarkIcon: failed");
            }
        }
        return result;
    }

    // 判断悬浮窗权限

    /**
     * 判断MIUI悬浮窗权限
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isFloatWindowOpAllowed(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(context, 24);  // AppOpsManager.OP_SYSTEM_ALERT_WINDOW
        } else {
            if ((context.getApplicationInfo().flags & 1 << 27) == 1 << 27) {
                return true;
            } else {
                return false;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;

        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {

                Class<?> spClazz = Class.forName(manager.getClass().getName());
                Method method = manager.getClass().getDeclaredMethod("checkOp", int.class, int.class, String.class);
                int property = (Integer) method.invoke(manager, op,
                        Binder.getCallingUid(), context.getPackageName());
                Log.e(TAG," property: " + property);

                if (AppOpsManager.MODE_ALLOWED == property) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG,"Below API 19 cannot invoke!");
        }
        return false;
    }

    /**
     * 打开权限设置界面
     */
    public static void gotoXiaomiPermission(Activity context) {
        try {
            Intent localIntent = new Intent(
                    "miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", context.getPackageName());
            context.startActivityForResult(localIntent,11);

        } catch (ActivityNotFoundException localActivityNotFoundException) {
            Intent intent1 = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent1.setData(uri);
            context.startActivityForResult(intent1,11);
        }
    }

    /**
     * 跳转到魅族的权限管理系统
     */
    public static void gotoMeizuPermission(Activity activity) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", activity.getPackageName());
        try {
            activity.startActivityForResult(intent, PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            gotoHuaweiPermission(activity);
        }
    }

    /**
     * 华为的权限管理页面
     */
    public static void gotoHuaweiPermission(Activity activity) {
        try {
            Intent intent = new Intent(activity.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity");
            intent.setComponent(comp);
            activity.startActivityForResult(intent, PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            activity.startActivityForResult(getAppDetailSettingIntent(activity), PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * 获取应用详情页面intent
     *
     * @return
     */
    public static Intent getAppDetailSettingIntent(Activity activity) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
        }
        return localIntent;
    }

    public static void gotoPermission(Activity mActivity) {
        if (isMIUI()) {
            gotoXiaomiPermission(mActivity);
        } else if (isFlyme()) {
            gotoMeizuPermission(mActivity);
        } else if (isHuawei()) {
            gotoHuaweiPermission(mActivity);
        }
    }

    /**
     * 检查是否具有特定权限
     * @param context
     * @param permission
     * @return
     */
    public static boolean hasPermission(Activity context, String permission) {
        int requestCode = 0;
        switch (permission) {
            case "android.permission.CAMERA":
                requestCode = REQUEST_PERMISSION_CAMERA;
                break;
            case "android.permission.RECORD_AUDIO":
                requestCode = REQUEST_PERMISSION_RECORD_AUDIO;
                break;
            case "android.permission.WRITE_EXTERNAL_STORAGE":
                requestCode = REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;
                break;
            case "android.permission.SYSTEM_ALERT_WINDOW":
                requestCode = REQUEST_PERMISSION_SYSTEM_ALERT_WINDOW;
                break;
            case "android.permission.ACCESS_FINE_LOCATION":
                requestCode = REQUEST_PERMISSION_ACCESS_FINE_LOCATION;
            case "android.permission.ACCESS_COARSE_LOCATION":
                requestCode = REQUEST_PERMISSION_ACCESS_COARSE_LOCATION;
                break;
        }
        return checkAndRequestPermission(context, permission, requestCode);
//    	DevUtils.checkAndRequestPermission(this, "android.permission.CAMERA", Config.REQUEST_PERMISSION_CAMERA);
//		DevUtils.checkAndRequestPermission(this, "android.permission.RECORD_AUDIO", Config.REQUEST_PERMISSION_RECORD_AUDIO);
//		DevUtils.checkAndRequestPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE", Config.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
//		DevUtils.checkAndRequestPermission(this, "android.permission.ACCESS_FINE_LOCATION", Config.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
//		DevUtils.checkAndRequestPermission(this, "android.permission.ACCESS_COARSE_LOCATION", Config.REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
//    	return context.checkPermission(permission, android.os.Process.myPid(), context.getApplicationInfo().uid) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkAndRequestPermission(Activity context, String permission, int requestCode) {
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(context, permission);
        if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(context, new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }

    /**
     * API23以上检查悬浮窗权限
     * @param activity
     * @return
     */
    @TargetApi(23)
    public static boolean checkDrawOverlayPermission(Activity activity) {
        /** check if we already  have permission to draw over other apps */
        if (!Settings.canDrawOverlays(activity)) {
            Logger.w("UIUtil", "checkDrawOverlayPermission canDrawOverlays:" + false);
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            /** request permission via start activity for result */
            activity.startActivityForResult(intent, REQUEST_PERMISSION_SYSTEM_ALERT_WINDOW);
            return false;
        } else {
            Logger.i("UIUtil", "checkDrawOverlayPermission canDrawOverlays:" + true);
        }
        Logger.d(TAG, "[checkDrawOverlayPermission]: true");
        return true;
    }

    /**
     * 各个操作系统检查浮动窗口权限汇总
     * @param activity
     * @return
     */
    public static boolean checkOverlayPermission(Activity activity) {
        boolean permitForFV = false;
        if (isMIUI() || isFlyme() || isHuawei()) {
            permitForFV = isFloatWindowOpAllowed(activity);
            // 需要启动辅助服务开启MIUI浮动窗口权限
        } else if (Build.VERSION.SDK_INT >= 23 && checkDrawOverlayPermission(activity)) {
            permitForFV = true;
        } else if (hasPermission(activity, "android.permission.SYSTEM_ALERT_WINDOW")) {
            permitForFV = true;
        } else {
            Logger.e(TAG, "android.permission.SYSTEM_ALERT_WINDOW no permission");
        }
        return permitForFV;
    }
}

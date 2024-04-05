package com.xue.autoshutdown;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    private static final String TAG = "XueShutdownUtils";
    static boolean debug = true;

    public static Date getSmartShutTime(Context context, Calendar shutTimeToday) {
        Calendar calendarNow =Calendar.getInstance(); //当前时间
        if (shutTimeToday != null) {
            //如果已经过了这个时间，设置为明天关机。一定要判断，否则AlermManager会立即发送intent让机器关机
            // 另外距离关机只有不到1min，也设置为明天关机。
            if (calendarNow.getTimeInMillis() + 60000 >= shutTimeToday.getTimeInMillis()) {
                shutTimeToday.add(Calendar.DATE, 1);
            }
            return shutTimeToday.getTime();
        }
        return null;
    }
    public static Calendar getPrefTime(Context context) {
        SharedPreferences spref = context.getSharedPreferences("shutdown", Context.MODE_PRIVATE);
        int hour = spref.getInt("hour", -1);
        int minute = spref.getInt("minute", -1);

        if (hour != -1 && minute != -1) {
            Calendar calendar =Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            return  calendar;
        }
        return null;
    }
    public static int[] getPrefVal(Context context) {
        SharedPreferences spref = context.getSharedPreferences("shutdown", Context.MODE_PRIVATE);
        int hour = spref.getInt("hour", -1);
        int minute = spref.getInt("minute", -1);
        return new int[]{hour, minute};
    }

    public static void setPrefTime(Context context, int hour, int minute) {
        SharedPreferences spref = context.getSharedPreferences("shutdown", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = spref.edit();
        edit.putInt("hour", hour);
        edit.putInt("minute", minute);
        edit.commit();
    }
    public static void setShutdownAlarm(Context context, Date date) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStr = sdf.format(date);
            Log.i(TAG, "设置下次关机时间：" + timeStr);
            Intent intent = new Intent(context, AlarmBroadcastReciver.class);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            //Log.e("PreferenceWithPower","date:"+mYear+"-"+Month+"-"+mDay+"  "+houroftime+":"+hourofminute+"---result:"+result+"----timer:"+date.getTime());
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);


            alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), pi);//设定时间启动定时器

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String toTimeStr(long millis){
        String str = "";
        long seconds = millis/1000;
        long hour = seconds / 3600;
        if (hour > 0) {
            str += hour + "h ";
        }
        seconds = seconds%3600;
        long minute = seconds / 60;
        if (minute > 0) {
            str += minute + "m ";
        }
        seconds = seconds%60;
        str += seconds + "s";
        return str;
    }
}

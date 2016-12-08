package com.nahomebefekadu.android.veggietimer;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;

public class Veggie
{
  public final static String ACTION_VEGGIE_ALERT="com.nahomebefekadu.android.veggietimer.action.VEGGIE_ALERT";
  public final static String EXTRA_ALARM_START="com.nahomebefekadu.android.veggietimer.extra.ALARM_START";
  public final static String EXTRA_ALARM_DURATION="com.nahomebefekadu.android.veggietimer.extra.ALARM_DURATION";
  public final static String EXTRA_ALARM_TYPE="com.nahomebefekadu.android.veggietimer.extra.ALARM_TYPE";

  public final static int ALARM_TYPE_NONE=0;
  public final static int ALARM_TYPE_VEGGIE=1;
  public final static int ALARM_TYPE_REST=2;

  public final static String PREFERENCES="com.nahomebefekadu.android.veggietimer_preferences";
  public final static String PREF_ALARM_TYPE="alarm_type";
  public final static String PREF_ALARM_START="alarm_start";
  public final static String PREF_ALARM_DURATION="alarm_duration";
  public final static String PREF_VEGGIE_COUNT="veggie_count";

  public final static String PREF_NOTIFICATION_TIMER="notification_timer";
  public final static boolean PREF_NOTIFICATION_TIMER_DEFAULT=true;

  public final static String PREF_VEGGIE_DURATION="veggie_duration";
  public final static int PREF_VEGGIE_DURATION_DEFAULT=25;
  public final static String PREF_VEGGIE_RINGTONE="veggie_ringtone";
  public final static String PREF_VEGGIE_RINGTONE_DEFAULT=null;
  public final static String PREF_VEGGIE_VIBRATE="veggie_vibrate";
  public final static boolean PREF_VEGGIE_VIBRATE_DEFAULT=false;
  public final static String PREF_VEGGIE_VOLUME="veggie_volume";
  public final static int PREF_VEGGIE_VOLUME_DEFAULT=100;
  public final static String PREF_VEGGIE_DELAY="veggie_delay";
  public final static int PREF_VEGGIE_DELAY_DEFAULT=0;
  public final static String PREF_REST_DURATION="rest_duration";
  public final static int PREF_REST_DURATION_DEFAULT=10;

  public static void startVeggie(Context context)
  {
    Veggie.pendingAlert(context, Veggie.ALARM_TYPE_VEGGIE, 0, 0);
  }
  public static void startRest(Context context)
  {
    Veggie.pendingAlert(context, Veggie.ALARM_TYPE_REST, 0, 0);
  }
  public static void stopVeggie(Context context)
  {
    SharedPreferences pref = context.getSharedPreferences(Veggie.PREFERENCES, 0);
    pref.edit()
      .putInt(Veggie.PREF_ALARM_TYPE, 0)
      .putLong(Veggie.PREF_ALARM_DURATION, 0)
      .putLong(Veggie.PREF_ALARM_START, 0)
      .apply();

    AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(ACTION_VEGGIE_ALERT);
    PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    am.cancel(sender);

    CountDownNotification notification = CountDownNotification.instance(context);
    notification.cancel();
  }

  static void pendingAlert(Context context, int type, long durationMillis, long startTime)
  {
    AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    SharedPreferences pref = context.getSharedPreferences(Veggie.PREFERENCES, 0);

    if (durationMillis == 0) {
      if (type == Veggie.ALARM_TYPE_REST)
        durationMillis = pref.getInt(Veggie.PREF_REST_DURATION, Veggie.PREF_REST_DURATION_DEFAULT)*60000;
      else
        durationMillis = pref.getInt(Veggie.PREF_VEGGIE_DURATION, Veggie.PREF_VEGGIE_DURATION_DEFAULT)*60000;
    }
    if (startTime == 0)
      startTime = System.currentTimeMillis();

    Intent intent = new Intent(ACTION_VEGGIE_ALERT);
    intent.putExtra(EXTRA_ALARM_TYPE, type);
    intent.putExtra(EXTRA_ALARM_START, startTime);
    intent.putExtra(EXTRA_ALARM_DURATION, durationMillis);
    PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

    long when = startTime + durationMillis;
    am.set(AlarmManager.RTC_WAKEUP, when, sender);


    pref.edit()
      .putInt(Veggie.PREF_ALARM_TYPE, type)
      .putLong(Veggie.PREF_ALARM_START, startTime)
      .putLong(Veggie.PREF_ALARM_DURATION, durationMillis)
      .apply();

    if (pref.getBoolean(Veggie.PREF_NOTIFICATION_TIMER, Veggie.PREF_NOTIFICATION_TIMER_DEFAULT)) {
      CountDownNotification notification = CountDownNotification.instance(context);
      notification.start(type, when);
    }
  }


  static int getVeggieCount(Context context)
  {
    SharedPreferences pref = context.getSharedPreferences(Veggie.PREFERENCES, 0);
    return pref.getInt(Veggie.PREF_VEGGIE_COUNT, 0);
  }
  static int setVeggieCount(Context context, int value) // if value < 0; count += abs(value)
  {
    SharedPreferences pref = context.getSharedPreferences(Veggie.PREFERENCES, 0);
    int veggie_count = pref.getInt(Veggie.PREF_VEGGIE_COUNT, 0);
    veggie_count = value < 0 ? Math.abs(value) + veggie_count : value;
    pref.edit().putInt(Veggie.PREF_VEGGIE_COUNT, veggie_count).apply();
    return veggie_count;
  }

  public static String format_time(long when)
  {
    long left = when - System.currentTimeMillis();
    long min = Math.abs((long)(left / 60000));
    long sec = Math.abs((long)((left - min) / 1000) % 60);
    return ((left < 0 ? "-" : "" )+ min+":"+sec);
  }

  public static class WakeLock
  {
    private static PowerManager.WakeLock sWakeLock;
    public static void acquire(Context context) {
      if (sWakeLock != null) {
        sWakeLock.release();
      }
      PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

      sWakeLock = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "Veggie");
      sWakeLock.acquire();
    }

    static void release() {
      if (sWakeLock != null) {
        sWakeLock.release();
        sWakeLock = null;
      }
    }
  };

  public static class CountDownNotification extends Notification
  {
    private static final int NOTIFICATION_ID=11235;
    private static CountDownNotification sInstance = null;

    private long mTickDuration = 15000;
    private boolean mTicking = false;
    private Handler mHandler;
    private Runnable mCallback; 

    private Context mContext;
    private NotificationManager mNotifyManager;
    private PendingIntent mIntent;
    private long mWhen;
    private int mType;

    public static CountDownNotification instance(Context context) { 
      if (sInstance == null)
        sInstance = new CountDownNotification(context);
      return sInstance;
    }
    private CountDownNotification(Context context)
    {
      super();

      mNotifyManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
      mContext = context;
      mIntent = PendingIntent.getActivity(context, 0, new Intent(context, VeggieActivity.class), 0);

      this.flags = Notification.FLAG_ONGOING_EVENT;

      mHandler = new Handler();
      mCallback = new Runnable() { 
        public void run() {
          if (mWhen < System.currentTimeMillis())
            CountDownNotification.this.cancel();
          if (mTicking) {
            CountDownNotification.this.tick();
            mHandler.postDelayed(mCallback, mTickDuration);
          }
        }
      };
    }

    public void start(int type, long when)
    {
      mWhen = this.when = when;
      mType = type;
      mTicking = true;
      mHandler.postDelayed(mCallback, 0);
    }

    public void tick()
    {
      String msg;
      if (mType == Veggie.ALARM_TYPE_VEGGIE) {
        this.icon = R.drawable.unripecorn;
        msg = mContext.getString(R.string.status_veggie);
      } else {
        this.icon = R.drawable.ripecorn;
        msg = mContext.getString(R.string.status_rest);
      }
      msg += " (" + Veggie.format_time(mWhen) + ")";
      Notification noti = new NotificationCompat.Builder(mContext)
              .setContentTitle("Veggie Timer")
              .setContentIntent(mIntent)
              .setContentText(msg)
              .setSmallIcon(this.icon)
              .build();
      mNotifyManager.notify(NOTIFICATION_ID, noti);
    }
    public void cancel()
    {
      mTicking = false;
      mNotifyManager.cancel(NOTIFICATION_ID);
    }
  }

    
}

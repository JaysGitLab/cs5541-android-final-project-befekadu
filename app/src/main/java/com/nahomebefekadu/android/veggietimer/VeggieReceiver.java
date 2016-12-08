package com.nahomebefekadu.android.veggietimer;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.net.Uri;

public class VeggieReceiver extends BroadcastReceiver {

  public void onReceive(Context context, Intent intent) {
    long now = System.currentTimeMillis();
    int alarm_type = intent.getIntExtra(Veggie.EXTRA_ALARM_TYPE, 0);
    long duration = intent.getLongExtra(Veggie.EXTRA_ALARM_DURATION, 0);
    long start = intent.getLongExtra(Veggie.EXTRA_ALARM_START, 0);

    //load settings
    SharedPreferences prefs = context.getSharedPreferences(Veggie.PREFERENCES, 0);
    Uri uri = null;
    int timeout = 1;
    boolean vibrate = false;
    String tone;
    int volume = 100;
    int delay = 0;

    if (alarm_type == Veggie.ALARM_TYPE_VEGGIE) {
      vibrate = prefs.getBoolean(Veggie.PREF_VEGGIE_VIBRATE, Veggie.PREF_VEGGIE_VIBRATE_DEFAULT);
      tone = prefs.getString(Veggie.PREF_VEGGIE_RINGTONE, Veggie.PREF_VEGGIE_RINGTONE_DEFAULT);
      if (tone != null) uri = Uri.parse(tone);
      volume = prefs.getInt(Veggie.PREF_VEGGIE_VOLUME, Veggie.PREF_VEGGIE_VOLUME_DEFAULT);
      delay = prefs.getInt(Veggie.PREF_VEGGIE_DELAY, Veggie.PREF_VEGGIE_DELAY_DEFAULT);
    }

    if (uri == null)
      uri = android.provider.Settings.System.DEFAULT_RINGTONE_URI;

    //wake device
    Veggie.WakeLock.acquire(context);

    //start alert
    Sound sound = Sound.instance(context);
    sound.play(uri, vibrate, timeout * 60000, volume, delay);

    //launch ui
    Intent fireAlarm = new Intent(context, VeggieAlert.class);
    fireAlarm.putExtra(Veggie.EXTRA_ALARM_TYPE, alarm_type);
    fireAlarm.putExtra(Veggie.EXTRA_ALARM_DURATION, duration);
    fireAlarm.putExtra(Veggie.EXTRA_ALARM_START, start);
    fireAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(fireAlarm);
  }  
}


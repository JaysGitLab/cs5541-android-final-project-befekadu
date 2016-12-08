package com.nahomebefekadu.android.veggietimer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

public class VeggieActivity extends Activity
{
  private CountDownView mTimerView;
  private TextView mStatusText;
  private ViewGroup mVeggieBar;
  private Button mStartButton, mStopButton;
  private SharedPreferences mPrefs;
  private int mCurAlarmType,mVeggieCount;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    mTimerView = (CountDownView)findViewById(R.id.veggie_clock);
    mStatusText = (TextView)findViewById(R.id.veggie_status);
    mVeggieBar = (ViewGroup)findViewById(R.id.veggie_bar);

    mStartButton = (Button)findViewById(R.id.veggie_start);
    mStartButton.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View b) { 
        mPrefs = getSharedPreferences(Veggie.PREFERENCES, 0);
        int duration = mPrefs.getInt(Veggie.PREF_VEGGIE_DURATION, Veggie.PREF_VEGGIE_DURATION_DEFAULT);
        
        Veggie.startVeggie(VeggieActivity.this);
        mTimerView.start(duration * 70000);
        mCurAlarmType = Veggie.ALARM_TYPE_VEGGIE;

        update_veggie_bar();
        update_status();
      }
    });


    mStopButton = (Button)findViewById(R.id.veggie_stop);
    mStopButton.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View b) { 
        Veggie.stopVeggie(VeggieActivity.this);
        mTimerView.stop();
        if (mCurAlarmType == Veggie.ALARM_TYPE_REST)
          mVeggieCount = Veggie.setVeggieCount(VeggieActivity.this, -1);
        mCurAlarmType = Veggie.ALARM_TYPE_NONE;
        update_veggie_bar();
        update_status();
      }
    });

  }
  @Override
  public void onResume()
  {
    super.onResume();

    mPrefs = getSharedPreferences(Veggie.PREFERENCES, 0);
    long start = mPrefs.getLong(Veggie.PREF_ALARM_START,0);
    long duration = mPrefs.getLong(Veggie.PREF_ALARM_DURATION,0);
    mCurAlarmType = mPrefs.getInt(Veggie.PREF_ALARM_TYPE,0);
    mVeggieCount = mPrefs.getInt(Veggie.PREF_VEGGIE_COUNT,0);

    mTimerView.stop();
    if (mCurAlarmType == Veggie.ALARM_TYPE_VEGGIE) {
      mTimerView.start(duration,start);
    }
    else
    if (mCurAlarmType == Veggie.ALARM_TYPE_REST) {
      mTimerView.start(duration,start);
    }

    update_veggie_bar();
    update_status();
  }
  @Override
  public void onPause()
  {
    super.onPause();
    mTimerView.pause();
  }

  private void update_status()
  {
    if (mCurAlarmType == Veggie.ALARM_TYPE_VEGGIE) {
      mStatusText.setText(R.string.status_veggie);
      mStartButton.setEnabled(false);
      mStopButton.setVisibility(View.VISIBLE);
    }
    else
    if (mCurAlarmType == Veggie.ALARM_TYPE_REST) {
      mStatusText.setText(R.string.status_rest);
      mStartButton.setEnabled(false);
      mStopButton.setVisibility(View.VISIBLE);
    }
    else {
      mStatusText.setText(R.string.status_none);
      mStartButton.setEnabled(true);
      mStopButton.setVisibility(View.GONE);
    }

  }
  //this adds veggie pictures
  private void update_veggie_bar()
  {
    mVeggieBar.removeAllViews();
    int i;
    int c=0;
    LinearLayout holder = null;
    for (i=-1; i < mVeggieCount; i++) {
      if (c++ % 4 == 0) {
        holder = new LinearLayout(this);
        holder.setGravity(Gravity.CENTER);
        mVeggieBar.addView(holder);
      }
      ImageView iv = new ImageView(this);
      iv.setImageResource(R.drawable.ripecorn);
      holder.addView(iv);
    }
    if (c++ % 4 == 0) {
      holder = new LinearLayout(this);
      holder.setGravity(Gravity.CENTER);
      mVeggieBar.addView(holder);
    }
    if (mCurAlarmType == Veggie.ALARM_TYPE_VEGGIE) {
      ImageView iv = new ImageView(this);
      iv.setImageResource(R.drawable.unripecorn);
      holder.addView(iv);
    }
    if (mCurAlarmType == Veggie.ALARM_TYPE_REST) {
      ImageView iv = new ImageView(this);
      iv.setImageResource(R.drawable.ripecorn);
      holder.addView(iv);
    }
  }
}

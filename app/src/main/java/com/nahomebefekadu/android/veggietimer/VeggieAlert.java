package com.nahomebefekadu.android.veggietimer;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.Button;
import android.view.KeyEvent;
import android.view.View;

public class VeggieAlert extends Activity
{
  private CountDownView mTimerView;
  private Sound mSound;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.alert);


    Intent intent = getIntent();
    final int alarm_type = intent.getIntExtra(Veggie.EXTRA_ALARM_TYPE, Veggie.ALARM_TYPE_VEGGIE);
    long start = intent.getLongExtra(Veggie.EXTRA_ALARM_START, 0L);
    long duration = intent.getLongExtra(Veggie.EXTRA_ALARM_DURATION, 0L);

    TextView tv = (TextView)findViewById(R.id.alert_message);
    Button b1 = (Button)findViewById(android.R.id.button1);
    Button b2 = (Button)findViewById(android.R.id.button2);
    Button b3 = (Button)findViewById(android.R.id.button3);

    mTimerView = (CountDownView)findViewById(R.id.veggie_clock);
    if (mTimerView != null)
      mTimerView.start(0,0);

    if (alarm_type == Veggie.ALARM_TYPE_VEGGIE) {
      tv.setText(R.string.alarm_message_veggie);
      b1.setText(R.string.start_rest);
      b2.setText(R.string.stop_working);
    } 
    else if (alarm_type == Veggie.ALARM_TYPE_REST) {
      tv.setText(R.string.alarm_message_rest);
      b1.setText(R.string.start_veggie);
      b2.setText(R.string.stop_working);
    }

    mSound = Sound.instance(VeggieAlert.this);

    b1.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View b) { //keep working
        mSound.stop();
        if (alarm_type == Veggie.ALARM_TYPE_VEGGIE)
          Veggie.startRest(VeggieAlert.this);
        else {
          Veggie.startVeggie(VeggieAlert.this);
          Veggie.setVeggieCount(VeggieAlert.this, -1);
        }
        finish();
      }
    });
    b2.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View b) { //stop working
        mSound.stop();
        Veggie.stopVeggie(VeggieAlert.this);
        Veggie.setVeggieCount(VeggieAlert.this, -1);
        finish();
      }
    });
    b3.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View b) { //silence
        mSound.stop();
        b.setEnabled(false);
      }
    });
  }
  @Override public boolean onKeyDown(int code, KeyEvent event)
  {
    if (code == KeyEvent.KEYCODE_BACK) {
      mSound.stop();
      Veggie.stopVeggie(VeggieAlert.this);
      Veggie.setVeggieCount(VeggieAlert.this, -1);
    }
    return super.onKeyDown(code, event);
  }

}

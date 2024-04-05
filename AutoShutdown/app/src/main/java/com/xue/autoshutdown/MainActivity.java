package com.xue.autoshutdown;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    TextView currentTimeText;
    Button  comfirmBt;
    TimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentTimeText = findViewById(R.id.currentTime);
        timePicker = findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        comfirmBt = findViewById(R.id.comfirm);
        comfirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = timePicker.getHour();;
                int minute = timePicker.getMinute();
                // 保存到SP
                Utils.setPrefTime(MainActivity.this, hour, minute);
                // 从SP读出，显示在text文本
                updateCurrentTimeText();

                Calendar calendar =Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                Date smartShutTime = Utils.getSmartShutTime(MainActivity.this, calendar);
                Utils.setShutdownAlarm(MainActivity.this, smartShutTime);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateCurrentTimeText();
        int[] prefVal = Utils.getPrefVal(this);
        if(prefVal != null && prefVal[0] != -1) {
            timePicker.setHour(prefVal[0]);
            timePicker.setMinute(prefVal[1]);
        }
    }

    private void updateCurrentTimeText() {
        int [] date = Utils.getPrefVal(this);
        int hour = date[0];
        int minute = date[1];
        if (date[0] == -1) {
            currentTimeText.setText("未设置");
        } else {
            currentTimeText.setText(hour + "点 " + minute + "分");
        }
    }

}
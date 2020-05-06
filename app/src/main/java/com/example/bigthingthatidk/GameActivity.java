package com.example.bigthingthatidk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.widget.TextView;

import java.sql.Time;

public class GameActivity extends AppCompatActivity {
    private TextView Timer;
    HomeWatcher mHomeWatcher2;

    private CountDownTimer countDownTimer;
    private long timeleft = 60000; //60000 = 1'
    private boolean timerRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Timer = (TextView) findViewById(R.id.Timer);

        StartTimer();

        doBindService();
        Intent music = new Intent();
        music.setClass(this, GameMusic.class);
        startService(music);

        mHomeWatcher2 = new HomeWatcher(this);
        mHomeWatcher2.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher2.startWatch();
    }

    public void StartTimer(){
        countDownTimer = new CountDownTimer(timeleft, 1000) {
            @Override
            public void onTick(long l) {
                timeleft = l;
                updateTimer();
            }

            @Override
            public void onFinish() {
                Intent timeendintent = new Intent(GameActivity.this, TimeEndActivity.class);
                startActivity(timeendintent);
            }
        }.start();
        timerRunning = true;
    }

    public void updateTimer(){
        int minutes = (int) timeleft/60000;
        int seconds = (int) timeleft%60000/1000;

        String TimeLeftText = "";
        TimeLeftText += ":";
        if(seconds<10) TimeLeftText += "0";
        TimeLeftText += seconds;

        Timer.setText(TimeLeftText);
    }

    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon =new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                Scon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(Scon);
            mIsBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mServ != null) {
            mServ.resumeMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

}

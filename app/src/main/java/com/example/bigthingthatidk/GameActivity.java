package com.example.bigthingthatidk;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Time;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private TextView Timer;
    HomeWatcher mHomeWatcher2;

    private CountDownTimer countDownTimer;
    private long timeleft = 60000; //60000 = 1'
    private boolean timerRunning;

    private String[] words; //Cau hoi
    private Random random; //random cau hoi
    private LinearLayout wordLayout;

    private TextView[] charViews; //text view cho tung chu cai
    private GridView letters; //cac chu cai
    private LetterAdapter letterAdapter; //adapter cac chu cai
    private String currWord;

    private ImageView[] bodyParts;
    private int numParts = 6;
    private int currPart;
    private int numChars;
    private int numCorr;

    private AlertDialog help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Resources resources = getResources();
        words = resources.getStringArray(R.array.words);

        random = new Random();
        currWord = "";

        wordLayout = (LinearLayout)findViewById(R.id.words);

        Timer = (TextView) findViewById(R.id.Timer);

        StartTimer();

        bodyParts = new ImageView[numParts];
        bodyParts[0] = (ImageView)findViewById(R.id.head);
        bodyParts[1] = (ImageView)findViewById(R.id.body);
        bodyParts[2] = (ImageView)findViewById(R.id.leftleg);
        bodyParts[3] = (ImageView)findViewById(R.id.rightleg);
        bodyParts[4] = (ImageView)findViewById(R.id.rightarm);
        bodyParts[5] = (ImageView)findViewById(R.id.leftarm);

        letters = (GridView) findViewById(R.id.letters);

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

        getActionBar().setDisplayHomeAsUpEnabled(true);

        playGame();
    }

    private void playGame() {
        String newWord = words[random.nextInt(words.length)]; //chon random cau hoi
        while (newWord.equals(currWord)) newWord = words[random.nextInt(words.length)]; //ko trung` cau hoi
        currWord = newWord;
        charViews = new TextView[currWord.length()];
        wordLayout.removeAllViews();

        for (int c=0;c<currWord.length();c++) //cho chu cai vao cac o
        {
            charViews[c] = new TextView(this);
            charViews[c].setText(""+currWord.charAt(c));

            charViews[c].setLayoutParams(new GridLayoutManager.LayoutParams(GridLayoutManager.LayoutParams.WRAP_CONTENT,
                    GridLayoutManager.LayoutParams.WRAP_CONTENT));
            charViews[c].setGravity(Gravity.CENTER);
            charViews[c].setTextColor(Color.WHITE);
            charViews[c].setBackgroundResource(R.drawable.letter_bg);

            wordLayout.addView(charViews[c]);
        }

        letterAdapter = new LetterAdapter(this);
        letters.setAdapter(letterAdapter);

        currPart = 0;
        numChars = currWord.length();
        numCorr = 0;

        for (int p=0;p<numParts;p++)
        {
            bodyParts[p].setVisibility(View.INVISIBLE);
        }
    }

    public void letterPressed(View view) {
        String lpressed = ((TextView)view).getText().toString();
        char letterChar = lpressed.charAt(0);
        view.setEnabled(false);
        view.setBackgroundResource(R.drawable.letter_down);

        boolean correct = false;
        for (int k=0;k<currWord.length();k++)
        {
            if(currWord.charAt(k) == letterChar)
            {
                correct = true;
                numCorr++;
                charViews[k].setTextColor(Color.BLACK);
            }
        }
        if(correct) //Neu doan dung
        {
            if (numCorr == numChars) //win
            {
                disableButton();
                Intent winIntent = new Intent(GameActivity.this,WinActivity.class);
                startActivity(winIntent);
                GameActivity.this.finish();
            }
        }
        else if (currPart<numParts) //doan sai nhung chua thua
        {
            bodyParts[currPart].setVisibility(View.VISIBLE);
            currPart++;
        }
        else
        {
            disableButton();
            Intent loserIntent = new Intent(GameActivity.this,LoserActivity.class);
            startActivity(loserIntent);
            GameActivity.this.finish();
        }
    }

    public void disableButton() {                       //khoa' cac nut bam khi win
        int numLetters = letters.getChildCount();
        for (int l=0;l<numLetters;l++)
        {
            letters.getChildAt(l).setEnabled(false);
        }
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
                GameActivity.this.finish();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_help:
                showHelp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showHelp() {
        AlertDialog.Builder helpbuild = new AlertDialog.Builder(this);
        helpbuild.setTitle("Trợ giúp");
        helpbuild.setMessage("Điền chữ cái còn sót vào chỗ trống. Bạn có 6 lần chọn sai");
        helpbuild.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                help.dismiss();
            }
        });
        help = helpbuild.create();
        helpbuild.show();
    }
}

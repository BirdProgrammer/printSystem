package com.ct.ti;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dongl on 2018/5/26.
 */

public class WelcomeActivity extends AppCompatActivity {

    private Timer timer;
    private TimerTask timerTask;
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.acitivity_welcome);
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        timerTask=new TimerTask() {
            @Override
            public void run() {
                Intent intent;
                intent = new Intent(WelcomeActivity.this,FirstActivity.class);
                startActivity(intent);
                finish();
            }
        };

        timer=new Timer();
        timer.schedule(timerTask,1500);
    }
}

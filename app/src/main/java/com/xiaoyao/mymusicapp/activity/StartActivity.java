package com.xiaoyao.mymusicapp.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.xiaoyao.mymusicapp.R;
import com.xiaoyao.mymusicapp.service.MyMusicService;

public class StartActivity extends AppCompatActivity {

    private final int START_DURATION = 1;
    private final int FINISH_ACTIVITY = 2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_DURATION:
                    if (!isDestroyed()){
                        sendEmptyMessageDelayed(FINISH_ACTIVITY, 2000);
                    }
                    break;
                case FINISH_ACTIVITY:
                    Intent intent = new Intent(StartActivity.this, MusicListActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Log.d("初始界面", "onCreate() end");
    }

    @Override
    protected void onStart(){
        super.onStart();
        Intent startServiceIntent = new Intent(getApplicationContext(), MyMusicService.class);
        startService(startServiceIntent);
        handler.sendEmptyMessage(START_DURATION); // 开启计时器
        Log.d("初始界面", "onStart() end");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("初始界面", "onResume() end");
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null); // 停止计时
        Log.d("初始界面", "onStop() end");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("初始界面", "onDestroy() end");
    }
}

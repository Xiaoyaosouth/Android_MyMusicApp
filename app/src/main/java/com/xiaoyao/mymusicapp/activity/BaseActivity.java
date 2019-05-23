package com.xiaoyao.mymusicapp.activity;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import com.xiaoyao.mymusicapp.pojo.MusicPojo;
import com.xiaoyao.mymusicapp.R;
import com.xiaoyao.mymusicapp.service.MyMusicService;

/**
 * 该类应是所有活动类的父类，在这里动态注册了一个监听结束活动的广播
 */
public class BaseActivity extends AppCompatActivity {
    private IntentFilter intentFilter;
    public ExitBroadcastReceiver exitBroadcastReceiver;

    /**
     * 内部类：广播接收器。用于调用finish()
     */
    public class ExitBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("广播接收器日志",
                    "活动："+context.getClass().getName()+"调用finish()");
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch (id){
            case R.id.menu_exit:
                intent = new Intent("com.xiaoyao.xiaoyaomusic.EXIT_BROADCAST");
                sendBroadcast(intent);
                break;
            case R.id.menu_scanMusic:
                intent = new Intent(getApplicationContext(), ChooseDirectoryActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_musicList:
                intent = new Intent(getApplicationContext(), MusicListActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_musicPlayer:
                intent = new Intent(getApplicationContext(), PlayerActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_setting:

                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("BaseActivity", "onDestroy()");

    }

    // 注册广播接收器
    public void registBroadcastReceiver(){
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.xiaoyao.xiaoyaomusic.EXIT_BROADCAST");
        exitBroadcastReceiver = new ExitBroadcastReceiver();
        registerReceiver(exitBroadcastReceiver, intentFilter);
    }

    /**
     * 结束音乐服务
     */
    public void stopService(Context context){
        Intent stopIntent = new Intent(context, MyMusicService.class);
        stopService(stopIntent);
    }

    /**
     * 以下为调用音乐服务所需，要使用音乐服务必须先启动和绑定服务。
     */
    public MyMusicService.MyMusicBinder myMusicBinder;
    public class MyMusicConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("服务连接日志", "服务连接，调用者："+getApplicationContext());
            myMusicBinder = (MyMusicService.MyMusicBinder)service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("服务连接日志", "服务断开连接");
        }
    }

    public MusicPojo getPlayState(){
        MusicPojo musicPojo = new MusicPojo();
        try{
            SharedPreferences spRead = getSharedPreferences("playState", MODE_PRIVATE);
            musicPojo.setMusicName(spRead.getString("musicName", null));
            musicPojo.setMusicPath(spRead.getString("musicPath", null));
            musicPojo.setMusicDuration(spRead.getInt("currentDuration", 0));
            Log.d("SharedPreferences", "读取音乐状态");
            Log.d("SharedPreferences", "读取到的音乐名："+musicPojo.getMusicName());
            Log.d("SharedPreferences", "播放到："+musicPojo.getMusicDuration());
        }catch (Exception e){ e.printStackTrace(); }
        return musicPojo;
    }

    public void setPlayState(MusicPojo musicPojo){
        SharedPreferences.Editor spEdit =
                getSharedPreferences("playState", MODE_PRIVATE).edit();
        try {
            spEdit.putString("musicName", musicPojo.getMusicName());
            spEdit.putString("musicPath", musicPojo.getMusicPath());
            spEdit.putInt("currentDuration", musicPojo.getMusicDuration());
            Log.d("SharedPreferences", "音乐名："+musicPojo.getMusicName());
            Log.d("SharedPreferences", "播放到"+musicPojo.getMusicDuration());
            spEdit.apply();
            Log.d("SharedPreferences", "保存音乐状态");
        }catch (Exception e){ e.printStackTrace(); }
    }

    /**
     * 获取读取SD卡权限
     */
    public boolean getReadStoragePermission(Activity activity){
        int checkPermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"授权成功",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this,"【错误】用户取消授权",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }
}
package com.xiaoyao.mymusicapp.activity;

import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.xiaoyao.mymusicapp.pojo.MusicPojo;
import com.xiaoyao.mymusicapp.R;
import com.xiaoyao.mymusicapp.service.MyMusicService;
import com.xiaoyao.mymusicapp.utils.MusicUtils;
import com.xiaoyao.mymusicapp.utils.ServiceUtils;

import org.litepal.crud.DataSupport;

public class PlayerActivity extends BaseActivity {

    private TextView currentMusicName,currentTime,totalTime;
    private Button playOrPause,prev,next,loop;
    private SeekBar seekBar;
    private final int START_HANDLER = 1;
    private MyMusicConnection myMusicConnection = new MyMusicConnection();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_HANDLER:
                    try {
                        if (myMusicBinder != null &&
                                myMusicBinder.getCurrentMusic() != null) {
                            updateSeekBar(); // 先更新进度条
                            updateMusicName();
                            updateTotalMusicTime();
                            updatePlayButtonBackground();
                            updateLoopBackground();
                        }
                    }catch (Exception e){ e.printStackTrace(); }
                    sendEmptyMessageDelayed(START_HANDLER, 1000); // 若已触发则定时1000毫秒执行
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        this.registBroadcastReceiver(); // 注册广播接收器
        Intent bindIntent = new Intent(PlayerActivity.this, MyMusicService.class);
        bindService(bindIntent, myMusicConnection, this.BIND_AUTO_CREATE);
        initComp();
    }



    @Override
    protected void onStart(){
        super.onStart();
        handler.sendEmptyMessage(START_HANDLER); // 开启计时器
        Log.d("PlayActivity","onStart() end");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("PlayActivity", "onResume() end");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d("PlayActivity","onStop()");
        handler.removeCallbacksAndMessages(null); // 停止计时
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("PlayActivity", "onDestroy()");

        this.unregisterReceiver(exitBroadcastReceiver); // 注销广播接收器
        try{
            if (myMusicBinder != null && myMusicBinder.isPlaying()) {
                myMusicBinder.pause();
            }
            if (myMusicBinder != null) {
                // 记录播放状态
                MusicPojo tempMpj = myMusicBinder.getCurrentMusic();
                if (tempMpj != null) {
                    tempMpj.setMusicDuration(myMusicBinder.getCurrentPostion());
                    setPlayState(tempMpj); // 写入到文件
                }
            }
            if (myMusicBinder != null){
                unbindService(myMusicConnection); // 关闭与服务的连接
            }
            // 关闭服务
            if (ServiceUtils.isMusicServiceRunning(this)) {
                this.stopService(this);
            }else{
                Log.d("PlayActivity", "音乐服务没有运行");
            }
        }catch (Exception e){}
    }

    private void initComp(){
        currentMusicName = (TextView)findViewById(R.id.currentMusicName);
        currentTime = (TextView)findViewById(R.id.currentMusicTime) ;
        totalTime = (TextView) findViewById(R.id.totalMusicTime);

        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());

        playOrPause = (Button)findViewById(R.id.play);
        playOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playOrPauseMusic();
            }
        });

        prev = (Button)findViewById(R.id.prevMusic);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevMusic();
            }
        });

        next = (Button)findViewById(R.id.nextMusic);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextMusic();
            }
        });

        loop = (Button)findViewById(R.id.repeatThis);
        loop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myMusicBinder.setReapting(true)){
                    loop.setBackgroundResource(R.drawable.isloop);
                    Toast.makeText(PlayerActivity.this, "【提示】单曲循环", Toast.LENGTH_LONG).show();
                }else{
                    loop.setBackgroundResource(R.drawable.loop);
                    Toast.makeText(PlayerActivity.this, "【提示】取消循环", Toast.LENGTH_LONG).show();
                }
            }
        });

        currentMusicName.setText("没有正在播放的音乐");
        currentTime.setText("0");
        totalTime.setText("0");
    }

    // 播放或暂停按钮事件
    private void playOrPauseMusic(){
        if (myMusicBinder != null &&
                myMusicBinder.getCurrentMusic() != null){
            if (myMusicBinder.isPlaying()){ // 若正在播放则暂停
                playOrPause.setBackgroundResource(R.drawable.play); // 背景设为播放
                myMusicBinder.pause();
            }else if (!myMusicBinder.isPlaying()){ // 若已暂停则继续播放
                playOrPause.setBackgroundResource(R.drawable.pause); // 背景设为暂停
                myMusicBinder.play();
            }
        }else{
            Log.d("PlayActivity", "【错误】Binder或MusicPojo不存在");
        }
    }

    private void playPrevMusic(){
        int currentId = myMusicBinder.getCurrentMusic().getId();
        MusicPojo prevMusic;
        if (currentId < 2){ // 如果当前ID为第一项
            prevMusic = DataSupport.findLast(MusicPojo.class); // 播放最后一项
            myMusicBinder.initMediaPlayer(prevMusic);
            myMusicBinder.play();
        }else{
            prevMusic = DataSupport.where("id = ?", Integer.toString(currentId - 1))
                    .find(MusicPojo.class).get(0);
            myMusicBinder.initMediaPlayer(prevMusic);
            myMusicBinder.play();
        }
        updateTotalMusicTime();
        Toast.makeText(PlayerActivity.this, "播放上一首："+prevMusic.getMusicName(), Toast.LENGTH_SHORT).show();
    }
    private void playNextMusic(){
        int totalMusic = DataSupport.findAll(MusicPojo.class).size(); // 音乐项总数
        int currentId = myMusicBinder.getCurrentMusic().getId();
        MusicPojo nextMusic;
        if (currentId == totalMusic){ // 如果当前ID为最后一项
            nextMusic = DataSupport.findFirst(MusicPojo.class); // 播放第一项
            myMusicBinder.initMediaPlayer(nextMusic);
            myMusicBinder.play();
        }else{
            nextMusic = DataSupport.where("id = ?", Integer.toString(currentId + 1))
                    .find(MusicPojo.class).get(0);
            myMusicBinder.initMediaPlayer(nextMusic);
            myMusicBinder.play();
        }
        updateTotalMusicTime();
        Toast.makeText(PlayerActivity.this, "播放下一首："+nextMusic.getMusicName(), Toast.LENGTH_SHORT).show();
    }
    // 更新当前音乐名
    private void updateMusicName(){
        if (myMusicBinder != null){
            String mName = myMusicBinder.getCurrentMusic().getMusicName();
            currentMusicName.setText(mName);
        }
    }
    // 更新当前音乐总时
    private void updateTotalMusicTime(){
        if (myMusicBinder != null) {
            int tTime = myMusicBinder.getDuration();
            totalTime.setText(MusicUtils.formatMusicTime(tTime));
        }
    }
    // 更新当前播放时间
    private void updateCurrentMusicTime(){
        if (myMusicBinder != null){
            int cTime = myMusicBinder.getCurrentPostion();
            currentTime.setText(MusicUtils.formatMusicTime(cTime));
        }
    }

    // 更新播放按钮的背景
    private void updatePlayButtonBackground(){
        if (myMusicBinder != null) {
            if (myMusicBinder.isPlaying()) { // 若正在播放则设为暂停
                playOrPause.setBackgroundResource(R.drawable.pause); // 背景设为暂停
            } else if (!myMusicBinder.isPlaying()) { // 若已暂停则继续播放
                playOrPause.setBackgroundResource(R.drawable.play); // 背景设为播放
            }
        }
    }
    // 更新进度条
    private void updateSeekBar(){
        if (myMusicBinder != null){
            seekBar.setMax(myMusicBinder.getDuration()); // 设置最大值
            seekBar.setProgress(myMusicBinder.getCurrentPostion()); // 设置当前值
        }
    }

    // 更新循环图
    private void updateLoopBackground(){
        if (myMusicBinder != null){
            if (myMusicBinder.isLooping()){
                loop.setBackgroundResource(R.drawable.isloop);
            }else{
                loop.setBackgroundResource(R.drawable.loop);
            }
        }
    }

    /**
     * 实现按返回键不销毁活动
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true); // 挂后台
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 内部类 进度条改变监听器
     */
    private class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            /**
             * 进度条改变时，应做如下操作：
             * 1、代替handler修改当前播放时间
             * 2、
             */
            if (fromUser){
                currentTime.setText(MusicUtils.formatMusicTime(seekBar.getProgress()));
                if (myMusicBinder != null){
                    myMusicBinder.seekTo(seekBar.getProgress());
                }

            }else{
                updateCurrentMusicTime();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            /**
             * 开始拖动进度条
             * 1、
             * 2、需要提前停止handle更新进度条，否则无法更新播放时间
             */
            handler.removeCallbacksAndMessages(null); // 停止计时
            currentTime.setTextSize(36);

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            /**
             * 停止拖动进度条
             * 1、
             * 2、继续让handler更新进度条
             */
            handler.sendEmptyMessage(START_HANDLER); // 开始计时
            currentTime.setTextSize(15); // TextView默认字体大小为15
        }
    }
}

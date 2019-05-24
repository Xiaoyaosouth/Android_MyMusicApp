package com.xiaoyao.mymusicapp.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.xiaoyao.mymusicapp.pojo.MusicPojo;

import java.io.IOException;

/**
 * 服务使用方法
 */
public class MyMusicService extends Service {
    
    private MediaPlayer myMediaPlayer;
    private MyMusicBinder myMusicBinder = new MyMusicBinder();

    public MyMusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        //当执行完了onCreate后，就会执行onBind把操作歌曲的方法返回
        Log.d("MyMusicService", "服务onBind()");
        return myMusicBinder;
    }

    /**
     * 重写onCreate()初始化播放器
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // 这里只执行一次，初始化播放器
        myMediaPlayer = new MediaPlayer();
        Log.e("MyMusicService", "服务onCreate()，调用者："+getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MyMusicService", "服务onStartCommand()");
        if (myMediaPlayer == null){
            myMediaPlayer = new MediaPlayer();
            Log.d("MyMusicService", "MediaPlayer不存在，初始化MediaPlayer");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 内部类，包含歌曲的操作
     */
    public class MyMusicBinder extends Binder {

        private MusicPojo currentMusic;

        public MusicPojo getCurrentMusic() throws NullPointerException{
            return currentMusic;
        }

        // 判断是否处于播放状态
        public boolean isPlaying(){
            return myMediaPlayer.isPlaying();
        }

        // 重置音乐状态，设置音乐文件的路径，并进入准备状态
        public void initMediaPlayer(MusicPojo music){
            try{
                this.currentMusic = music;
                myMediaPlayer.reset(); // 重置音乐状态
                myMediaPlayer.setDataSource(music.getMusicPath()); // 设置音乐源
                myMediaPlayer.prepare(); // 准备状态
            } catch (IOException e) {
                Log.d("音乐服务日志", "设置音乐路径错误");
                e.printStackTrace();
            }
        }
        
        //播放音乐
        public void play() {
            /**
             * BUG：MediaPlayer若没有指定过数据源并进入准备状态，不能播放音乐（没有记录播放状态）
             */
            if (!myMediaPlayer.isPlaying()) {
                myMediaPlayer.start();
                Log.d("音乐服务日志", "播放");
            }
        }

        // 暂停音乐
        public void pause(){
            if (myMediaPlayer.isPlaying()) {
                myMediaPlayer.pause();
                Log.d("音乐服务日志", "暂停");
            }
        }

        // 停止播放音乐
        public void stop(){
            if (myMediaPlayer.isPlaying()){
                myMediaPlayer.stop();
            }
            Log.d("音乐服务日志", "停止播放");
        }

        //返回歌曲的长度，单位为毫秒
        public int getDuration(){
            return myMediaPlayer.getDuration();
        }

        //返回歌曲目前的进度，单位为毫秒
        public int getCurrentPostion(){
            return myMediaPlayer.getCurrentPosition();
        }

        //设置歌曲播放的进度，单位为毫秒
        public void seekTo(int mesc){
            myMediaPlayer.seekTo(mesc);
        }

        // 设置单曲循环
        public boolean setReapting(boolean reapt){
            if (reapt){
                if (myMediaPlayer.isLooping()){
                    Log.d("音乐服务日志", "已经是循环播放状态，取消循环");
                    myMediaPlayer.setLooping(false);
                    return false;
                }else{
                    myMediaPlayer.setLooping(reapt);
                    Log.d("音乐服务日志", "设置重复播放");
                    return true;
                }
            }
            return false;
        }

        // 判断是否处于循环播放状态
        public boolean isLooping(){
            if (myMediaPlayer.isLooping()){
                return true;
            }else{
                return false;
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("音乐服务日志", "服务onUnbind()，解除绑定");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 关闭服务时结束播放并释放资源
        if (myMediaPlayer != null){
            if (myMediaPlayer.isPlaying()){
                myMediaPlayer.stop();
            }
            myMediaPlayer.reset();
            myMediaPlayer.release(); // 释放资源
            Log.d("音乐服务日志", "MediaPlayer不为空，释放资源");
        }
        Log.d("音乐服务日志", "服务onDestroy()");
    }
}

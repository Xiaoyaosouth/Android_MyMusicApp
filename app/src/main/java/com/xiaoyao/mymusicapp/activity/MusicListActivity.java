package com.xiaoyao.mymusicapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.xiaoyao.mymusicapp.MusicAdapter;
import com.xiaoyao.mymusicapp.pojo.MusicPojo;
import com.xiaoyao.mymusicapp.utils.MusicUtils;
import com.xiaoyao.mymusicapp.OnRecyclerItemsClickListener;
import com.xiaoyao.mymusicapp.R;
import com.xiaoyao.mymusicapp.service.MyMusicService;
import com.xiaoyao.mymusicapp.utils.ServiceUtils;

import org.litepal.LitePal;

import java.util.List;

public class MusicListActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private MusicAdapter musicAdapter;
    private List<MusicPojo> musicPojoList;
    private MyMusicConnection myMusicConnection = new MyMusicConnection();

    @Override
    protected void onStart(){
        super.onStart();
        Log.d("MusicListActivity", "onStart()");
        this.refreshMusicList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MusicListActivity", "onDestroy()");
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
            // 关闭与服务的连接
            unbindService(myMusicConnection);
            // 关闭服务
            if (ServiceUtils.isMusicServiceRunning(this)) {
                this.stopService(this);
            }else{
                Log.d("MusicListActivity", "音乐服务没有运行");
            }
        }catch (Exception e){ e.printStackTrace(); }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musiclist);

        this.registBroadcastReceiver(); // 注册广播接收器

        // 绑定音乐服务
        Intent bindIntent = new Intent(this, MyMusicService.class);
        bindService(bindIntent, myMusicConnection, BIND_AUTO_CREATE);

        // 清除列表按钮
        Button btn_clearList = (Button)findViewById(R.id.button_clearMusicList);
        btn_clearList.setOnClickListener(this.clearList);
        // 音乐列表
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView_musicList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        // 音乐列表数据初始化
        musicPojoList = MusicUtils.loadMusicList(); // 从数据库读取音乐列表
        try{
            Log.d("MusicListActivity", "从数据库读取的音乐数："+ musicPojoList.size());
            if (musicPojoList.size() == 0 || musicPojoList.isEmpty()) {
                Toast.makeText(MusicListActivity.this,
                        "【提示】音乐列表为空，请先扫描音乐", Toast.LENGTH_LONG).show();
            }
            initComp();
        }catch (Exception e){ e.printStackTrace(); }
    }

    private void initComp(){
        musicAdapter = new MusicAdapter(musicPojoList);
        // 列表项点击事件回调
        musicAdapter.setOnRecylerItemsClickListener(new OnRecyclerItemsClickListener<MusicPojo>() {
            @Override
            public void onRecyclerItemsClick(View view, MusicPojo info) {
                Toast.makeText(MusicListActivity.this,
                        "播放：" + info.getMusicName(),
                        Toast.LENGTH_SHORT).show();
                playMusic(info);
                Intent intent = new Intent(MusicListActivity.this, PlayerActivity.class);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(musicAdapter);
    }

    private void playMusic(MusicPojo musicPojo){
        if (myMusicBinder != null){
            myMusicBinder.initMediaPlayer(musicPojo);
            myMusicBinder.play();
        }else{
            Log.d("MusicListActivity", "Binder不存在，播放失败");
        }
    }

    /**
     * 清除列表（删除数据库）
     */
    private View.OnClickListener clearList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!MusicUtils.loadMusicList().isEmpty()){
                if (LitePal.deleteDatabase("Music")){ // 删除数据库
                    Log.d("数据库操作日志", "数据库Music已删除");
                    Toast.makeText(MusicListActivity.this, "已清空，请重新扫描", Toast.LENGTH_SHORT).show();
                    refreshMusicList();
                }else{
                    Log.d("数据库操作日志", "【错误】数据库Music删除失败");
                }
            }
        }
    };

    /**
     * 刷新适配器数据。如果适配器为空则初始化控件。
     */
    private void refreshMusicList(){
        if (musicAdapter != null){
            musicAdapter.refreshMusicList();
            Log.d("MusicListActivity", "刷新Adapter数据");
        }else{
            initComp();
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
}

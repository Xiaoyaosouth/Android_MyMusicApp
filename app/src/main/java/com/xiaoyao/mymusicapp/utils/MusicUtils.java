package com.xiaoyao.mymusicapp.utils;

import android.media.*;
import android.util.Log;

import com.xiaoyao.mymusicapp.pojo.MusicPojo;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.*;

public class MusicUtils {

    /**
     * 从音乐文件列表获取音乐信息
     * MediaMetadataRetriever类，解析媒体文件、获取媒体文件中取得帧和元数据（视频/音频包含的标题、格式、艺术家等信息）
     * @return 返回MusicPojo类的List
     */
    public List<MusicPojo> getMusicPojoList(List<File> fileList){
        List<MusicPojo> musicPojoList = new ArrayList<>();
        MusicPojo musicPojo;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        for (int i = 0; i < fileList.size(); i++) {
            musicPojo = new MusicPojo();
            // 设置文件名
            musicPojo.setMusicName(fileList.get(i).getName());
            // 设置文件路径
            musicPojo.setMusicPath(fileList.get(i).getPath());
            // 设置数据源
            mmr.setDataSource(fileList.get(i).getPath());
            // 设置标题
            //String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            // 设置艺术家
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            musicPojo.setMusicArtist(artist);
            // 设置播放时长，单位毫秒
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            musicPojo.setMusicDuration(Integer.parseInt(duration));
            // 设置是否收藏，默认否
            musicPojo.setLove(false);

            musicPojoList.add(musicPojo);
        }
        return musicPojoList;
    }

    /**
     * 保存音乐列表到数据库
     * @param musicPojoList
     */
    public static void saveMusicList(List<MusicPojo> musicPojoList) {
        LitePal.getDatabase(); // 创建数据库
        List<MusicPojo> oldList = loadMusicList(); // 数据库中原来的List
        if (oldList == null) {
            DataSupport.saveAll(musicPojoList);
            Log.d("数据库操作日志", "原音乐列表为空，直接全部添加");
        } else {
            /**
             *  【保存逻辑】每扫描到一个音乐文件，
             *  就在数据库判断这项是否存在，无则添加。*/
            for (int i = 0; i < musicPojoList.size(); i++) {
                // 如果新文件在数据库中不存在，则添加
                String tempPath = musicPojoList.get(i).getMusicPath();
                // 查询。若修改了MusicPojo则这里也要改。
                MusicPojo tempMPJ = DataSupport.select
                        ("id", "musicName", "musicPath", "isLove")
                        .where("musicPath = ?", tempPath)
                        .findFirst(MusicPojo.class);
                if (tempMPJ != null) {
                    Log.d("数据库操作日志", "【不添加】存在相同音乐文件：" + tempMPJ.getMusicName());
                } else {
                    musicPojoList.get(i).save();
                    Log.d("数据库操作日志", "【添加】" + musicPojoList.get(i).getMusicName());
                }
            }
        }
    }

    /**
     * 从数据库读取音乐列表
     * @return
     */
    public static List<MusicPojo> loadMusicList(){
        List<MusicPojo> musicPojoList = DataSupport.findAll(MusicPojo.class);
        if (musicPojoList.isEmpty()){
            Log.d("音乐工具类", "【错误】从数据库获得的音乐列表为空");
        }
        Log.d("音乐工具类", "从数据库读取音乐列表成功");
        return musicPojoList;
    }

    /**
     * 获得收藏的音乐
     * @return
     */
    public static List<MusicPojo> loadFavoriteMusicList(){
        // 从音乐表读取收藏的音乐。注：litepal中fasle为0，true为1
        List<MusicPojo> musicList = DataSupport
                .where("isLove = ?", "1").find(MusicPojo.class);
        if (musicList.isEmpty()){
            Log.d("音乐工具类", "【错误】读取失败，数据库中无收藏的音乐");
        }
        // 重置ID并添加到List
        for (int i = 0; i < musicList.size(); i++){
            musicList.get(i).setId(i + 1);
            Log.d("音乐工具类", "读取到收藏的音乐：【id】" + musicList.get(i).getId()
                    + " 【name】" + musicList.get(i).getMusicName());
        }
        return musicList;
    }

    /**
     * 格式化得到的音乐时间：毫秒转分:秒
     * @param time
     * @return
     */
    public static String formatMusicTime(int time){
        // 得到的time为毫秒
        StringBuilder sb = new StringBuilder();
        String min = (time / (1000 * 60))+"";
        String second = (time%(1000*60)/1000)+"";
        if(min.length()<2){
            min=0+min;
        }
        if(second.length()<2){
            second=0+second;
        }
        sb.append(min);
        sb.append(":");
        sb.append(second);
        return sb.toString();
    }
}
package com.xiaoyao.mymusicapp.pojo;

import org.litepal.crud.DataSupport;

import java.io.File;

public class MusicPojo extends DataSupport{
    private int id;
    private String musicName;
    private String musicPath;
    private String musicArtist;
    private int musicDuration;

    public MusicPojo(){}
    public MusicPojo(File file){
        musicName = file.getName();
        musicPath = file.getPath();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getMusicArtist() {
        return musicArtist;
    }

    public void setMusicArtist(String musicArtist) {
        this.musicArtist = musicArtist;
    }

    public int getMusicDuration() {
        return musicDuration;
    }

    public void setMusicDuration(int musicDuration) {
        this.musicDuration = musicDuration;
    }
}
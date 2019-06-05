package com.xiaoyao.mymusicapp.utils;

import android.util.Log;

import com.xiaoyao.mymusicapp.R;
import com.xiaoyao.mymusicapp.pojo.FilePojo;

import java.io.File;
import java.util.*;

public class FileUtils {

    public FileUtils(){}

    /**
     * 获取文件列表
     * @param sourceFile 目录路径
     * @return
     */
    public static List<FilePojo> getFileList(File sourceFile) {
        List<FilePojo> filePojoList = new ArrayList<>();
        List<File> fileList = new ArrayList<>();
        // 注意：先动态获取读取SD卡权限
        Collections.addAll(fileList, sourceFile.listFiles());
        for (File file : fileList) {
            String fileName = file.getName();
            //默认是文件图标
            int imageId = R.drawable.file;
            //下面开始判断文件是文件夹或音乐文件
            if (file.isDirectory()) {
                // 是文件夹
                imageId = R.drawable.folder;
            } else {
                //如果是文件，就从文件名的后缀名来判断是什么文件，从而添加对应图标
                //获取后缀名前的分隔符"."在fName中的位置。
                int dotIndex = fileName.lastIndexOf(".");
                if(dotIndex >= 0){
                    /* 获取文件的后缀名*/
                    String end= fileName.substring(dotIndex,fileName.length()).toLowerCase();
                    if(!Objects.equals(end, "")){
                        if (Objects.equals(end, ".mp3")||Objects.equals(end, ".ape")
                                ||Objects.equals(end, ".flac")||Objects.equals(end, ".m4a")
                                ||Objects.equals(end, ".ape")||Objects.equals(end, ".wav")
                                ||Objects.equals(end, ".aac")){
                            // 如果是音乐文件
                            imageId = R.drawable.music;
                        }
                    }
                }
            }
            String filePath = file.getPath();
            FilePojo myFile = new FilePojo(fileName, imageId, filePath);
            filePojoList.add(myFile);
        }
        return filePojoList;
    }

    private List<String> lstFile = new ArrayList<String>();  //结果 List
    private List<File> myMusicFileList = new ArrayList<>();

    /**
     * 自定义获取指定文件的列表
     * @param path 搜索目录
     * @param extension 扩展名
     * @param isIterative 是否进入子文件夹
     */
    public void searchMyFiles(String path, String extension, boolean isIterative)
    {
        File[] files = new File(path).listFiles(); // 目录下的所有文件
        // 遍历文件
        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            if (f.isFile()) {
                //判断扩展名
                if (f.getName().endsWith(extension)){
                    lstFile.add(f.getPath());
                }
                if (!isIterative){
                    break;
                }
            }else if (f.isDirectory() && f.getPath().indexOf("/.") == -1){
                //忽略点文件（隐藏文件/文件夹）并继续在子目录寻找
                searchMyFiles(f.getPath(), extension, isIterative);
            }
        }
    }

    /**
     * 扫描音乐文件
     * @param path 要扫描的目录
     * @param isIterative 是否扫描子目录
     * @return
     */
    public List<File> searchMusicFiles(String path, boolean isIterative)
    {
        List<File> fileList = new ArrayList<>();
        File file = new File(path);
        File[] files = file.listFiles(); // 目录下的所有文件
        Log.d("扫描日志", "开始扫描路径："+path);
        /**
         * 特殊情况：当目录为空时
         */
        try{
            if (files == null){
                Log.d("扫描日志","目录为空：【"+file.getName()+"】");
                throw new NullPointerException();
            }
        }catch (Exception e){
        }
        // 遍历文件
        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            if (f.isFile()) {
                String fileName = f.getName();
                // 获取扩展名
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                Log.d("扫描日志","文件名："+fileName+" 识别的扩展名："+extension);
                //判断扩展名
                if (extension.equalsIgnoreCase("mp3") || extension.equalsIgnoreCase("aac")
                        || extension.equalsIgnoreCase("3gp") || extension.equalsIgnoreCase("m4a")
                        || extension.equalsIgnoreCase("flac") || extension.equalsIgnoreCase("wav")
                        || extension.equalsIgnoreCase("ogg") || extension.equalsIgnoreCase("ape")){
                    fileList.add(f);
                }else{
                    Log.d("扫描日志","【"+fileName+"】不是音乐文件");
                }
            }else if (f.isDirectory() && isIterative){ // 若是文件夹,且确认扫描子目录
                Log.d("扫描日志","【递归】扫描子目录："+f.getName());
                searchMusicFiles(f.getPath(), isIterative);
            }else if (f.isDirectory() && !isIterative){
                Log.d("扫描日志","不扫描子目录："+f.getPath());
            }
        }
        /**
         * 特殊情况：当前扫描的目录中没有音乐文件，导致返回的音乐文件列表为空
         */
        try{
            if (fileList.isEmpty()){
                Log.d("扫描日志","目录中没有音乐文件："+file.getName());
                throw new NullPointerException();
            }
        }catch (Exception e){
        }
        return fileList;
    }

    public List<File> getMyMusicFileList(){
        if (myMusicFileList.isEmpty()){
            throw new NullPointerException("列表为空");
        }
    return myMusicFileList;
}
}

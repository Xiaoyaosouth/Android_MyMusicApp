package com.xiaoyao.mymusicapp.pojo;

public class FilePojo {
    private int imageId;
    private String fileName;
    private String filePath;

    public FilePojo(String fileName, int imageId, String filePath) {
        super();
        this.fileName = fileName;
        this.imageId = imageId;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public int getImageId() {
        return imageId;
    }

    public String getFilePath() {
        return filePath;
    }

}
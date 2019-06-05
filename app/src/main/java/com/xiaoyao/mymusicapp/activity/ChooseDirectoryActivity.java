package com.xiaoyao.mymusicapp.activity;

import android.content.DialogInterface;
import android.os.*;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.*;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.xiaoyao.mymusicapp.FileAdapter;
import com.xiaoyao.mymusicapp.utils.FileUtils;
import com.xiaoyao.mymusicapp.OnRecyclerItemsClickListener;
import com.xiaoyao.mymusicapp.R;
import com.xiaoyao.mymusicapp.pojo.FilePojo;
import com.xiaoyao.mymusicapp.pojo.MusicPojo;
import com.xiaoyao.mymusicapp.utils.MusicUtils;

import java.io.File;
import java.util.*;

public class ChooseDirectoryActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    public EditText editText_path;
    private Button button_upDir, button_scanDir;
    private CheckBox checkBox_scanChildDir;
    private String initPath; // 初始路径

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ChooseDirectoryActivity", "onDestroy()");
        this.unregisterReceiver(exitBroadcastReceiver); // 注销广播接收器
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosedirectory);

        this.registBroadcastReceiver(); // 注册广播接收器
        try {
            initPath = Environment.getExternalStorageDirectory().toString();
            initComp();
        }catch (Exception e){ e.printStackTrace(); }
    }

    @Override
    protected void onStart(){
        super.onStart();
        try {
            fileAdapter.notifyDataSetChanged();
        }catch (Exception e){}
        Log.d("ChooseDirectoryActivity", "onStart() end");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 初始化控件并设置事件处理
     */
    private void initComp(){
        editText_path = (EditText) this.findViewById(R.id.editText_path);
        editText_path.setText(initPath);
        // 实现按下回车不换行
        editText_path.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    return true;
                }
                return true;
            }
        });
        // 抬起回车键的事件：隐藏键盘，并跳转文件夹
        editText_path.setOnKeyListener(this.enterActionUp);
        // 复选框
        checkBox_scanChildDir = (CheckBox) this.findViewById(R.id.checkbox_scanChildDir);

        fileAdapter = new FileAdapter(FileUtils.getFileList(new File(initPath)));
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_directory);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(fileAdapter);

        fileAdapter.setOnRecylerItemsClickListener(new OnRecyclerItemsClickListener<FilePojo>() {
            @Override
            public void onRecyclerItemsClick(View view, FilePojo info) {
                inDir(info);
            }
        });
        // 上一级
        button_upDir = (Button)findViewById(R.id.button_upDirectory);
        button_upDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upDir();
            }
        });
        // 扫描当前目录
        button_scanDir = (Button)findViewById(R.id.button_scanDirectory);
        button_scanDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean scanChildDir = checkBox_scanChildDir.isChecked();
                final String dirPath = editText_path.getText().toString();
                if (scanChildDir){
                    scanDir(dirPath, true);
                }else{
                    scanDir(dirPath, false);
                }
            }
        });
    }

    /**
     * 点击子项时进入文件夹的事件
     * @param filePojo
     */
    private void inDir(FilePojo filePojo){
        File tempFile = new File(filePojo.getFilePath());
        if (tempFile.isFile()){
            // 当点击项是文件时
            Toast.makeText(ChooseDirectoryActivity.this,
                    "您点击的是文件：" + tempFile.getName(),
                    Toast.LENGTH_SHORT).show();
        }else{
            // 点击某目录时，重新获取该路径下的文件列表
            List<FilePojo> newFilePojoList = FileUtils.getFileList(new File(filePojo.getFilePath()));
            fileAdapter.setFilePojoList(newFilePojoList);
            editText_path.setText(filePojo.getFilePath());
        }
    }

    /**
     * 进入上一级目录
     */
    private void upDir(){
        // 从editText获取当前路径
        String filePath = editText_path.getText().toString();
        if(!filePath.equals(initPath)){
            /** BUG: 根目录的上一级目录不能访问 */
            // 获得上一级路径
            String parentPath = new File(filePath).getParent();
            editText_path.setText(parentPath);
            // 进入上一级目录
            fileAdapter.setFilePojoList(FileUtils.getFileList(new File(parentPath)));
        }
    }

    /**
     * 扫描当前目录
     * @param dirPath
     * @param isScanChildDir 是否扫描子目录
     */
    private void scanDir(final String dirPath, final boolean isScanChildDir){
        AlertDialog.Builder dialog = new AlertDialog.Builder(ChooseDirectoryActivity.this)
                .setTitle("确定扫描该目录？可能花费较多时间！")
                .setMessage(dirPath);
        dialog.setCancelable(false);
        dialog.setPositiveButton("扫描", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /** 确定扫描。先获取目录路径，然后利用MusicUtils扫描音乐文件并保存 */
                MusicUtils musicUtils = new MusicUtils();
                List<File> fileList = new FileUtils().searchMusicFiles(dirPath, isScanChildDir);
                if (fileList.isEmpty()){
                    Toast.makeText(ChooseDirectoryActivity.this, "【错误】要查找的目录没有音乐文件", Toast.LENGTH_SHORT).show();
                }else{
                    List<MusicPojo> musicPojoList = musicUtils.getMusicPojoList(fileList);
                    if (musicPojoList.isEmpty()){
                        Toast.makeText(ChooseDirectoryActivity.this, "【错误】音乐列表为空", Toast.LENGTH_SHORT).show();
                    }else{
                        musicUtils.saveMusicList(musicPojoList);
                        Toast.makeText(ChooseDirectoryActivity.this, "保存音乐列表", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 不扫描
            }
        });
        dialog.create().show();
    }

    /** 实现抬起回车键时隐藏键盘，并跳转文件夹 */
    private View.OnKeyListener enterActionUp = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER &&
                    event.getAction() == KeyEvent.ACTION_UP) {
                Log.d("编辑框操作日志", "抬起回车键");
                // 先隐藏键盘
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(ChooseDirectoryActivity.this.getCurrentFocus()
                                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                // 进入指定目录
                String tempPath = editText_path.getText().toString();
                File file = new File(tempPath);
                if (file.exists()){ // 如果目录存在
                    /** BUG:如果目录是/storage/emulated/0之前的目录，会无法跳转 */
                    Log.d("编辑框操作日志", "尝试进入指定目录：" + tempPath);
                    try {
                        fileAdapter.setFilePojoList(FileUtils.getFileList(new File(tempPath)));
                    }catch (Exception e){}
                }else{
                    editText_path.setText(initPath);
                    fileAdapter.setFilePojoList(FileUtils.getFileList(new File(initPath)));
                    Toast.makeText(ChooseDirectoryActivity.this,
                            "【错误】目录不存在", Toast.LENGTH_LONG).show();
                }
            }
            return false;
        }
    };

}

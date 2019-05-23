package com.xiaoyao.mymusicapp;

import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.TextView;

import com.xiaoyao.mymusicapp.pojo.MusicPojo;
import com.xiaoyao.mymusicapp.utils.MusicUtils;

import java.util.*;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder>{

    private List<MusicPojo> musicPojoList;
    // 用于回调
    private OnRecyclerItemsClickListener mOnRecyclerItemsClickListener;

    //构造方法
    public MusicAdapter(List<MusicPojo> musicPojoList) {
        this.musicPojoList = musicPojoList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View musicPojoView;
        TextView musicId;
        TextView musicName;

        public ViewHolder(View view) {
            super(view);
            musicPojoView = view;
            musicId = (TextView) view.findViewById(R.id.musicId);
            musicName = (TextView) view.findViewById(R.id.textView_musicName);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 载入子项布局
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        MusicPojo musicPojo = musicPojoList.get(position);
        holder.musicId.setText(Integer.toString(musicPojo.getId())); // 注意将获得的Int转String
        holder.musicName.setText(musicPojo.getMusicName());

        holder.musicPojoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 使用回调给Activity处理
                if (mOnRecyclerItemsClickListener != null){
                    // 将点击后的子项MusicPojo回调
                    mOnRecyclerItemsClickListener.onRecyclerItemsClick(v, musicPojoList.get(position));
                }
            }
        });
    }

    public void refreshMusicList(){
        try {
            musicPojoList = MusicUtils.loadMusicList();
        }catch (Exception e){
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return musicPojoList.size();
    }

    /**
     * 用于回调。给RecyclerView设置Adapter时调用它，参数里new一个OnRecyclerItemsClickListener类
     * @param onRecyclerItemsClickListener 要实现的接口
     */
    public void setOnRecylerItemsClickListener(OnRecyclerItemsClickListener onRecyclerItemsClickListener) {
        mOnRecyclerItemsClickListener = onRecyclerItemsClickListener;
    }
}
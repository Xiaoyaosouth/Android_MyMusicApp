package com.xiaoyao.mymusicapp;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.xiaoyao.mymusicapp.pojo.FavoriteMusic;
import com.xiaoyao.mymusicapp.pojo.MusicPojo;

import java.util.*;

public class FavoriteMusicAdapter extends RecyclerView.Adapter<FavoriteMusicAdapter.ViewHolder>{

    private List<FavoriteMusic> fMusicList;
    // 用于回调
    private OnRecyclerItemsClickListener mOnRecyclerItemsClickListener;

    //构造方法
    public FavoriteMusicAdapter(List<FavoriteMusic> fMusicList) {
        this.fMusicList = fMusicList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View fMusicView;
        TextView fMusicId; // 音乐ID
        TextView fMusicName; // 音乐名
        ImageButton isLove; // 是否收藏

        public ViewHolder(View view) {
            super(view);
            fMusicView = view;
            fMusicId = (TextView) view.findViewById(R.id.favorite_musicId);
            fMusicName = (TextView) view.findViewById(R.id.favorite_musicName);
            isLove = (ImageButton) view.findViewById(R.id.favorite_isLove);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 载入子项布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favorite_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final FavoriteMusic fMusic = fMusicList.get(position);
        holder.fMusicId.setText(Integer.toString(fMusic.getId())); // 注意将获得的Int转String
        holder.fMusicName.setText(fMusic.getMusicName());
        // 设置收藏按钮图片
        if (fMusic.isLove()){
            holder.isLove.setImageResource(R.drawable.love);
        }else{
            holder.isLove.setImageResource(R.drawable.unlove);
        }

        holder.fMusicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 使用回调给Activity处理
                if (mOnRecyclerItemsClickListener != null){
                    // 将点击后的子项MusicPojo回调
                    mOnRecyclerItemsClickListener.onRecyclerItemsClick(v, fMusicList.get(position));
                }
            }
        });
        // 用户点击收藏按钮，判断是否已收藏，并更新数据库对应项
        holder.isLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fMusic.isLove()){
                    fMusic.setLove(false);
                    holder.isLove.setImageResource(R.drawable.unlove);
                    Log.d("MusicAdapter","【取消收藏】"+fMusic.getMusicName());
                }else {
                    fMusic.setLove(true);
                    holder.isLove.setImageResource(R.drawable.love);
                    Log.d("MusicAdapter","【收藏】"+fMusic.getMusicName());
                }
                if (fMusic.isSaved()){
                    fMusic.save(); // 更新数据库
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return fMusicList.size();
    }

    /**
     * 用于回调。给RecyclerView设置Adapter时调用它，参数里new一个OnRecyclerItemsClickListener类
     * @param onRecyclerItemsClickListener 要实现的接口
     */
    public void setOnRecylerItemsClickListener(OnRecyclerItemsClickListener onRecyclerItemsClickListener) {
        mOnRecyclerItemsClickListener = onRecyclerItemsClickListener;
    }
}
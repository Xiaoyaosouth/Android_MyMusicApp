package com.xiaoyao.mymusicapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaoyao.mymusicapp.pojo.FilePojo;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder>{

    private List<FilePojo> filePojoList;

    public FileAdapter(List<FilePojo> fileList) {
        filePojoList = fileList;
    }

    public void setFilePojoList(List<FilePojo> filePojoList){
        this.filePojoList = filePojoList;
        notifyDataSetChanged(); // 更新数据
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;
        ImageView fileImage;
        TextView fileName;

        public ViewHolder(View view) {
            super(view);
            fileView = view;
            fileImage = (ImageView) view.findViewById(R.id.fileImage);
            fileName = (TextView) view.findViewById(R.id.fileName);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.file_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        // 已经在onBindViewHolder绑定事件
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        FilePojo filePojo = filePojoList.get(position);
        holder.fileImage.setImageResource(filePojo.getImageId());
        holder.fileName.setText(filePojo.getFileName());

        holder.fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnRecyclerItemsClickListener != null){
                    mOnRecyclerItemsClickListener.onRecyclerItemsClick(v, filePojoList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filePojoList.size();
    }

    // 用于回调
    private OnRecyclerItemsClickListener mOnRecyclerItemsClickListener;
    public void setOnRecylerItemsClickListener(OnRecyclerItemsClickListener onRecyclerItemsClickListener) {
        mOnRecyclerItemsClickListener = onRecyclerItemsClickListener;
    }

}
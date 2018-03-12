package com.example.administrator.barcode2ds;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by yangshuquan on 2018/3/10.
 */

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder> {
    private List<String> mDatas;

    public void setModels(List<String> data) {
        this.mDatas = data;
        this.notifyDataSetChanged();
    }

    public MyRecyclerAdapter(List<String> datas) {
        this.mDatas = datas;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tv.setText(mDatas.get(position));
        String s = (position + 1) + "";
        holder.item.setText(s);
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        TextView item;

        public MyViewHolder(View view) {
            super(view);
            tv = view.findViewById(R.id.tv_item);
            item = view.findViewById(R.id.item);
        }

    }
}

package com.turing.sample.ai.chat;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.turing.sample.R;

import java.util.List;


import androidx.recyclerview.widget.RecyclerView;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private List<String> responBeanList;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvResult;
        LinearLayout root;

        public ViewHolder(View itemView) {
            super(itemView);
            tvResult=(TextView) itemView.findViewById(R.id.tv_result);
            root = itemView.findViewById(R.id.root);
        }
    }

    public void updateList(List<String> responBeanList){
        this.responBeanList = responBeanList;
        notifyDataSetChanged();
    }
    public ResultAdapter(List<String> responBeanList) {
        this.responBeanList = responBeanList;
    }

    @Override
    public ResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item, parent,false);
        return new ResultAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResultAdapter.ViewHolder holder, int position) {
        String respon = responBeanList.get(position);
        if(position % 2 == 0){
            holder.root.setBackgroundColor(Color.parseColor("#CCCCCC"));
        }else {
            holder.root.setBackgroundColor(Color.parseColor("#dddddd"));
        }
        holder.tvResult.setText(respon);
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return responBeanList.size();
    }

    //点击 RecyclerView 某条的监听
    public interface OnItemClickListener{

        void onItemClick(View view, int position);

    }

    private OnItemClickListener onItemClickListener;

    /**
     * 设置RecyclerView某个的监听
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}

package com.mean.meanchateasemobapi.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mean.meanchateasemobapi.R;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.VH> {
    private List<ChatItem> items;
    private OnUserItemInteractionListener mListener;

    public static class ChatItem {
        public Bitmap icon;
        public String name;
        public Date date;
        public String message;
    }

    public static class VH extends RecyclerView.ViewHolder{
        ImageView iv_icon;
        TextView tv_name,tv_date,tv_message;
        RelativeLayout rl_item_chat;

        public VH(@NonNull View itemView) {
            super(itemView);
            this.iv_icon = itemView.findViewById(R.id.iv_icon);
            this.tv_name = itemView.findViewById(R.id.tv_name);
            this.tv_date = itemView.findViewById(R.id.tv_date);
            this.tv_message = itemView.findViewById(R.id.tv_message);
            this.rl_item_chat = itemView.findViewById(R.id.rl_item_chat);
        }
    }

    public ChatRecyclerViewAdapter(List<ChatItem> items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_list,viewGroup,false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH vh, final int i) {
        vh.iv_icon.setImageBitmap(items.get(i).icon);
        vh.tv_name.setText(items.get(i).name);
        DateFormat dateFormat = new SimpleDateFormat("MM月dd日 hh:mm");
        vh.tv_date.setText(dateFormat.format(items.get(i).date));
        vh.tv_message.setText(items.get(i).message);
        final String username = items.get(vh.getAdapterPosition()).name;
        vh.rl_item_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onUserItemClick(username);
            }
        });
        vh.rl_item_chat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onUserItemLongClick(username);
                return true;
            }
        });
    }
    public void setOnUserItemInteractionListener(OnUserItemInteractionListener listener){
        this.mListener = listener;
    }

    public interface OnUserItemInteractionListener{
        void onUserItemClick(String username);
        void onUserItemLongClick(String username);
    }
}

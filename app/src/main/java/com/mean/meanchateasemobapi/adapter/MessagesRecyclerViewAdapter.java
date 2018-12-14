package com.mean.meanchateasemobapi.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.mean.meanchateasemobapi.R;
import com.mean.meanchateasemobapi.controller.ClientMessageManager;
import com.mean.meanchateasemobapi.model.ClientMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessagesRecyclerViewAdapter extends RecyclerView.Adapter<MessagesRecyclerViewAdapter.VH> {

    private List<ClientMessage> messageList;

    public class VH extends RecyclerView.ViewHolder {
        ImageView iv_dot;
        TextView tv_title;
        ImageButton ib_close;
        TextView tv_content;
        TextView tv_agree;
        TextView tv_refuse;
        TextView tv_time;
        public VH(@NonNull View itemView) {
            super(itemView);
            iv_dot = itemView.findViewById(R.id.iv_dot);
            tv_title = itemView.findViewById(R.id.tv_title);
            ib_close = itemView.findViewById(R.id.ib_close);
            tv_content = itemView.findViewById(R.id.tv_content);
            tv_agree = itemView.findViewById(R.id.tv_agree);
            tv_refuse = itemView.findViewById(R.id.tv_refuse);
            tv_time = itemView.findViewById(R.id.tv_time);
        }
    }

    public void updateData(List<ClientMessage> list){
        messageList = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message,viewGroup,false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull VH vh, int i) {
        final ClientMessage message = messageList.get(messageList.size()-1-i);
        if(message.getState() == ClientMessage.State.NOT_READ){
            vh.iv_dot.setVisibility(View.VISIBLE);
        }else {
            vh.iv_dot.setVisibility(View.INVISIBLE);
        }

        vh.tv_title.setText(message.getTitle());

        vh.tv_content.setText(message.getContext());

        Date date = new Date(message.getTime());
        DateFormat format;
        if(DateUtils.isToday(message.getTime())){
            format = new SimpleDateFormat("hh:mm");
        }else{
            format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        }
        vh.tv_time.setText(format.format(date));

        vh.ib_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientMessageManager.getInstance().deleteMessage(messageList.size()-1-vh.getAdapterPosition());
                notifyDataSetChanged();
            }
        });

        if(message.getType() == ClientMessage.Type.FRIEND_REQUEST) { //未处理的好友请求
            vh.tv_agree.setVisibility(View.VISIBLE);
            vh.tv_refuse.setVisibility(View.VISIBLE);
            vh.tv_agree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (message.getExtra() == null || message.getExtra().isEmpty()) {
                        return;
                    }
                    try {
                        EMClient.getInstance().contactManager().acceptInvitation(message.getExtra());
                        vh.tv_agree.setVisibility(View.VISIBLE);
                        vh.tv_agree.setClickable(false);
                        vh.tv_agree.setText("已同意");
                        vh.tv_refuse.setVisibility(View.INVISIBLE);
                        //TODO update message type , use interface callback
                        ClientMessageManager.getInstance().markMessageAgree(messageList.size()-1-vh.getAdapterPosition());
                        message.setType(ClientMessage.Type.FRIEND_REQUEST_AGREED);
                        message.setState(ClientMessage.State.HAVE_READ);
                        notifyDataSetChanged();
                        //message.setType(ClientMessage.Type.FRIEND_REQUEST_AGREED);
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                    }
                }
            });
            vh.tv_refuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (message.getExtra() == null || message.getExtra().isEmpty()) {
                        return;
                    }
                    try {
                        EMClient.getInstance().contactManager().declineInvitation(message.getExtra());
                        vh.tv_refuse.setVisibility(View.VISIBLE);
                        vh.tv_refuse.setClickable(false);
                        vh.tv_refuse.setText("已拒绝");
                        vh.tv_agree.setVisibility(View.INVISIBLE);
                        //TODO update message type , use interface callback
                        ClientMessageManager.getInstance().markMessageRefuse(messageList.size()-1-vh.getAdapterPosition());
                        message.setType(ClientMessage.Type.FRIEND_REQUEST_REFUSED);
                        message.setState(ClientMessage.State.HAVE_READ);
                        notifyDataSetChanged();
                        //message.setType(ClientMessage.Type.FRIEND_REQUEST_REFUSED);
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else if(message.getType() == ClientMessage.Type.FRIEND_REQUEST_AGREED) { //已同意的好友请求
            vh.tv_agree.setVisibility(View.VISIBLE);
            vh.tv_agree.setClickable(false);
            vh.tv_agree.setText("已同意");
            vh.tv_refuse.setVisibility(View.INVISIBLE);
        }else if(message.getType() == ClientMessage.Type.FRIEND_REQUEST_REFUSED){ //已拒绝的好友请求
            vh.tv_refuse.setVisibility(View.VISIBLE);
            vh.tv_refuse.setClickable(false);
            vh.tv_refuse.setText("已拒绝");
            vh.tv_agree.setVisibility(View.INVISIBLE);
        }else {                                                                  //其它普通消息
            vh.tv_agree.setVisibility(View.INVISIBLE);
            vh.tv_refuse.setVisibility(View.INVISIBLE);
            message.setState(ClientMessage.State.HAVE_READ);
        }
    }



    @Override
    public int getItemCount() {
        return messageList.size();
    }
}

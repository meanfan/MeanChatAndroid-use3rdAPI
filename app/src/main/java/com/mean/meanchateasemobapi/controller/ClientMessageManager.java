package com.mean.meanchateasemobapi.controller;

import android.util.Log;

import com.mean.meanchateasemobapi.model.ClientMessage;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientMessageManager {
    private static final String TAG = "ClientMessageManager";
    private static volatile ClientMessageManager instance;
    private List<ClientMessage> messages;

    public static ClientMessageManager getInstance(){
        if(instance==null){
            synchronized (ClientMessageManager.class){
                if(instance==null){
                    instance = new ClientMessageManager();
                }
            }
        }
        return instance;
    }

    private ClientMessageManager(){
        messages = new CopyOnWriteArrayList<>(); //线程安全list
        loadMessagesLocal();
    }

    public void resetMessages(List<ClientMessage> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
    }

    private boolean checkHasUnreadMessage(){
        return findLatestUnreadMessage()!=null;
    }

    public ClientMessage findLatestUnreadMessage(){ //找到最新的未读消息
        for(int i=messages.size()-1;i>=0;i--){
            if(messages.get(i)!=null && messages.get(i).getState() == ClientMessage.State.NOT_READ){
                return messages.get(i);
            }
        }
        return null;
    }

    public ClientMessage addNewMessage(String title, String context, ClientMessage.Type type){
        ClientMessage message = new ClientMessage();
        message.setTime(new Date().getTime());
        message.setTitle(title);
        message.setContext(context);
        message.setType(type);
        message.setState(ClientMessage.State.NOT_READ);
        messages.add(message);
        Log.d(TAG, "addNewMessage: "+message.getContext());
        return message;
    }

    public ClientMessage addNewMessage(String title, String context, ClientMessage.Type type,String extra) {
        ClientMessage message = new ClientMessage();
        message.setTime(new Date().getTime());
        message.setTitle(title);
        message.setContext(context);
        message.setType(type);
        message.setState(ClientMessage.State.NOT_READ);
        message.setExtra(extra);
        messages.add(message);
        Log.d(TAG, "addNewMessage: "+message.getContext());
        return message;
    }


        public void markAllMessageRead(){
        for(ClientMessage message:messages) {
            message.setState(ClientMessage.State.HAVE_READ);
        }
    }

    public boolean markMessageAgree(int no){
        ClientMessage message;
        if((message = messages.get(no))==null){
            return false;
        }
        if(message.getType()!=ClientMessage.Type.FRIEND_REQUEST){
            return false;
        }
        message.setType(ClientMessage.Type.FRIEND_REQUEST_AGREED);
        message.setState(ClientMessage.State.HAVE_READ);
        return true;
    }

    public boolean markMessageRefuse(int no){
        ClientMessage message;
        if((message = messages.get(no))==null){
            return false;
        }
        if(message.getType()!=ClientMessage.Type.FRIEND_REQUEST){
            return false;
        }
        message.setType(ClientMessage.Type.FRIEND_REQUEST_REFUSED);
        message.setState(ClientMessage.State.HAVE_READ);
        return true;
    }

    public void deleteMessage(int no){
        messages.remove(no);
    }

    public List<ClientMessage> getMessages() {
        return messages;
    }

    public ClientMessage getLatestMessage(){
        if(messages.size()>0) {
            return messages.get(messages.size() - 1);
        }else {
            return null;
        }
    }

    private void loadMessagesLocal(){

    }

    public void saveMessagesLocal(){

    }

    public boolean isHasUnreadMessage() {
        return checkHasUnreadMessage();
    }
}

package com.mean.meanchateasemobapi.controller;

import com.mean.meanchateasemobapi.model.ClientMessage;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

public class ClientMessageManager {
    private static volatile ClientMessageManager instance;
    private Map<Integer,ClientMessage> messages;
    private OnMessageChangeListener mListener;
    private boolean hasUnreadMessage;

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
        messages = new Hashtable<>(); //线程安全Map
        hasUnreadMessage = false;
        loadMessagesLocal();
    }

    public void resetMessages(Map<Integer,ClientMessage> messages) {
        this.messages.clear();
        this.messages.putAll(messages);
        hasUnreadMessage = checkHasUnreadMessage();
        if(!hasUnreadMessage){
            if(mListener!=null) {
                mListener.onNoUnreadMessage();
            }
        }
    }

    private boolean checkHasUnreadMessage(){
        for(int i=0;i<messages.size();i++){
            ClientMessage message = messages.get(i);
            if(message!=null && message.getState() == ClientMessage.State.NOT_READ){
                return true;
            }
        }
        return false;
    }

    public ClientMessage addNewMessage(String title, String context, ClientMessage.Type type){
        ClientMessage message = new ClientMessage();
        message.setTime(new Date().getTime());
        message.setTitle(title);
        message.setContext(context);
        message.setType(type);
        message.setState(ClientMessage.State.NOT_READ);
        messages.put(messages.size(),message);
        hasUnreadMessage = true;
        if(mListener!=null){
            mListener.onNewMessage(context);
        }
        return message;
    }

    public boolean markMessageRead(int no){
        ClientMessage message;
        if((message = messages.get(no))==null){
            return false;
        }
        message.setState(ClientMessage.State.HAVE_READ);
        hasUnreadMessage = checkHasUnreadMessage();
        if(!hasUnreadMessage){
            if(mListener!=null) {
                mListener.onNoUnreadMessage();
            }
        }
        return true;
    }

    public Map<Integer, ClientMessage> getMessages() {
        return messages;
    }

    private void loadMessagesLocal(){

    }

    public void saveMessagesLocal(){

    }

    public boolean isHasUnreadMessage() {
        return hasUnreadMessage;
    }

    public void setOnMessageChangeListener(OnMessageChangeListener mListener) {
        this.mListener = mListener;
    }

    //暂时不用
    public interface OnMessageChangeListener{
        void onNewMessage(String message);
        void onNoUnreadMessage();
    }
}

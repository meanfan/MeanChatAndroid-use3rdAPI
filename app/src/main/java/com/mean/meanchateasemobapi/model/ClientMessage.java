package com.mean.meanchateasemobapi.model;

public class ClientMessage {
    public enum Type {
        SYSTEM_ANNOUNCEMENT,
        INFORMATION,
        FRIEND_REQUEST,
        FRIEND_REQUEST_AGREED,
        FRIEND_REQUEST_REFUSED,
        FRIEND_CHANGED,
        FRIEND_NEW,}
    public enum State {NOT_READ,HAVE_READ}
    private long time;
    private String title;
    private String context;
    private Type type;
    private State state;
    private String extra;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}

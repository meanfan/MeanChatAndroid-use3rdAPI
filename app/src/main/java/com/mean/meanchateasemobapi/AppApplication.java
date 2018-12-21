package com.mean.meanchateasemobapi;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.util.Log;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseUI;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;

import java.util.List;

public class AppApplication extends Application {
    public static final String TAG = "AppApplication";
    private volatile Activity currentActivity;
    public static Application instance;
    private static NotificationChannel notificationChannel;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // EMClient初始化
        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        // 是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载，如果设为 false，需要开发者自己处理附件消息的上传和下载
        // options.setAutoTransferMessageAttachments(true);
        // 是否自动下载附件类消息的缩略图等，默认为 true 这里和上边这个参数相关联
        //options.setAutoDownloadThumbnail(true);
        EMClient.getInstance().init(this, options);
        // 在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);

        // EaseUI初始化
        EMOptions uiOption = new EMOptions();
        uiOption.setAcceptInvitationAlways(false); //开启好友验证
        EaseUI.getInstance().init(this,uiOption);

        EMClient.getInstance().chatManager().addMessageListener(new EMMessageListener() {
            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                for(EMMessage message:messages){
                    Log.d(TAG, "onMessageReceived: "+message.toString());
                }
                if(currentActivity != null){
                    return;
                }

            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {

            }

            @Override
            public void onMessageRead(List<EMMessage> messages) {

            }

            @Override
            public void onMessageDelivered(List<EMMessage> messages) {

            }

            @Override
            public void onMessageRecalled(List<EMMessage> messages) {

            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {

            }
        });

        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
    }

    @TargetApi(26)
    public static NotificationChannel getNotificationChannel(){
        if(notificationChannel == null){
            notificationChannel = new NotificationChannel("chat_msg", "chat message background", NotificationManager.IMPORTANCE_HIGH);
        }
        return notificationChannel;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }

    class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks{
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            setCurrentActivity(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            setCurrentActivity(null);
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}

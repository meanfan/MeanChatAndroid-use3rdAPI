package com.mean.meanchateasemobapi;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.EaseUI;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseChatInputMenu;
import com.hyphenate.easeui.widget.EaseChatMessageList;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.easeui.widget.EaseVoiceRecorderView;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.hyphenate.easeui.EaseConstant.CHATTYPE_GROUP;

public class ChatActivity extends AppCompatActivity  implements EMMessageListener {
    private EaseTitleBar titleBar;
    private EaseChatMessageList messageList;
    private EaseChatInputMenu inputMenu;
    private SwipeRefreshLayout refreshLayout;
    private EaseVoiceRecorderView voiceRecorderView;
    private EMConversation conversation;
    private String toChatUsername;
    private boolean isRoaming = true;
    private int pageSize = 20;
    private boolean isMessageListInited = false;

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        toChatUsername = getIntent().getStringExtra("username");
        titleBar = findViewById(R.id.title_bar);
        titleBar.setTitle(toChatUsername);
        titleBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        handler = new Handler();
        EMClient.getInstance().chatManager().addMessageListener(this); //消息监听
        initMessageList();
        initInputMenu();
        initConversation();
    }


    private void initInputMenu() {
        inputMenu = findViewById(R.id.input_menu);
        voiceRecorderView = findViewById(R.id.voice_recorder);
        //注册底部菜单扩展栏item
        //传入item对应的文字，图片及点击事件监听，extendMenuItemClickListener实现EaseChatExtendMenuItemClickListener
        ///inputMenu.registerExtendMenuItem(R.string.attach_video, R.drawable.em_chat_video_selector, ITEM_VIDEO, extendMenuItemClickListener);
        //inputMenu.registerExtendMenuItem(R.string.attach_file, R.drawable.em_chat_file_selector, ITEM_FILE, extendMenuItemClickListener);
        //初始化，此操作需放在registerExtendMenuItem后
        inputMenu.init();
        //设置相关事件监听
        inputMenu.setChatInputMenuListener(new EaseChatInputMenu.ChatInputMenuListener() {

            @Override
            public void onSendMessage(String message) {
                sendTextMessage(message);
                messageList.refresh();
                messageList.refreshSelectLast();
            }

            @Override
            public void onTyping(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void onBigExpressionClicked(EaseEmojicon emojicon) {

            }

            @Override
            public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
                return voiceRecorderView.onPressToSpeakBtnTouch(v, event, new EaseVoiceRecorderView.EaseVoiceRecorderCallback() {
                    @Override
                    public void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength) {
                        sendVoiceMessage(voiceFilePath, voiceTimeLength);
                    }
                });
            }
        });
    }

    private void initMessageList() {
        messageList = findViewById(R.id.message_list);
        messageList.init(toChatUsername,EMMessage.ChatType.Chat.ordinal(),null);
        messageList.setItemClickListener(new EaseChatMessageList.MessageListItemClickListener() {
            @Override
            public boolean onBubbleClick(EMMessage message) {
                return false;
            }

            @Override
            public boolean onResendClick(EMMessage message) {
                return false;
            }

            @Override
            public void onBubbleLongClick(EMMessage message) {

            }

            @Override
            public void onUserAvatarClick(String username) {

            }

            @Override
            public void onUserAvatarLongClick(String username) {

            }

            @Override
            public void onMessageInProgress(EMMessage message) {

            }
        });
        refreshLayout = messageList.getSwipeRefreshLayout();
        messageList.refresh();
        messageList.refreshSeekTo(0);
        messageList.refreshSelectLast();
        isMessageListInited = true;
    }

    protected void initConversation(){
        conversation = EMClient.getInstance().chatManager()
                .getConversation(toChatUsername, EaseCommonUtils.getConversationType(EaseConstant.CHATTYPE_SINGLE), true);
        conversation.markAllMessagesAsRead();
        if (!isRoaming) {
            final List<EMMessage> msgs = conversation.getAllMessages();
            int msgCount = (msgs != null ? msgs.size() : 0);
            if (msgCount < conversation.getAllMsgCount() && msgCount < pageSize) {
                String msgId = null;
                if (msgs != null && msgs.size() > 0) {
                    msgId = msgs.get(0).getMsgId();
                }
                conversation.loadMoreMsgFromDB(msgId, pageSize - msgCount);
            }
        } else {
            Executor fetchQueue = Executors.newSingleThreadExecutor();
            fetchQueue.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        EMClient.getInstance().chatManager()
                                .fetchHistoryMessages(toChatUsername, EaseCommonUtils.getConversationType(EaseConstant.CHATTYPE_SINGLE), pageSize, "");
                        final List<EMMessage> msgs = conversation.getAllMessages();
                        int msgCount = (msgs != null ? msgs.size() : 0);
                        if (msgCount < conversation.getAllMsgCount() && msgCount < pageSize) {
                            String msgId = null;
                            if (msgs != null && msgs.size() > 0) {
                                msgId = msgs.get(0).getMsgId();
                            }
                            conversation.loadMoreMsgFromDB(msgId, pageSize - msgCount);
                        }
                        messageList.refreshSelectLast();
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void sendTextMessage(String content){
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        message.setChatType(EMMessage.ChatType.Chat);
        EMClient.getInstance().chatManager().sendMessage(message);
        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                if(isMessageListInited){
                    messageList.refresh();
                }
            }

            @Override
            public void onError(int code, String error) {
                showToast("消息发送出错：("+code+")"+error);
                if(isMessageListInited) {
                    messageList.refresh();
                }
            }

            @Override
            public void onProgress(int progress, String status) {
                if(isMessageListInited) {
                    messageList.refresh();
                }
            }
        });
    }

    private void sendVoiceMessage(String voiceFilePath, int voiceTimeLength){

    }

    private void showToast(String message){
        Toast.makeText(ChatActivity.this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isMessageListInited) {
            messageList.refresh();
        }
    }

    @Override
    public void onMessageReceived(List<EMMessage> messages) {
        for (EMMessage message : messages) {
            String username = message.getFrom();
            if (username.equals(toChatUsername)
                    || message.getTo().equals(toChatUsername)
                    || message.conversationId().equals(toChatUsername)) {
                messageList.refreshSelectLast();
                conversation.markMessageAsRead(message.getMsgId());
            }
            EaseUI.getInstance().getNotifier().vibrateAndPlayTone(message);
        }
    }

    @Override
    public void onCmdMessageReceived(List<EMMessage> messages) {

    }

    @Override
    public void onMessageRead(List<EMMessage> messages) {
        if(isMessageListInited) {
            messageList.refresh();
        }
    }

    @Override
    public void onMessageDelivered(List<EMMessage> messages) {
        if(isMessageListInited) {
            messageList.refresh();
        }
    }

    @Override
    public void onMessageRecalled(List<EMMessage> messages) {
        if(isMessageListInited) {
            messageList.refresh();
        }
    }

    @Override
    public void onMessageChanged(EMMessage message, Object change) {
        if(isMessageListInited) {
            messageList.refresh();
        }
    }
}

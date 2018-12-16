package com.mean.meanchateasemobapi;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
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
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseChatExtendMenu;
import com.hyphenate.easeui.widget.EaseChatInputMenu;
import com.hyphenate.easeui.widget.EaseChatMessageList;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.easeui.widget.EaseVoiceRecorderView;
import com.hyphenate.exceptions.HyphenateException;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity  implements EMMessageListener {
    private static final String PERMISSION_NAME_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static final int PERMISSION_CHECK_REQUEST_RECORD_AUDIO = 10;
    private static final int CHAT_INPUT_EXTEND_MENU_PHOTO = Menu.FIRST+1;
    private static final int CHAT_INPUT_EXTEND_MENU_VIDEO = Menu.FIRST+2;
    private static final int CHAT_INPUT_EXTEND_MENU_CALL_VOICE = Menu.FIRST+3;
    private static final int CHAT_INPUT_EXTEND_MENU_CALL_VIDEO = Menu.FIRST+4;
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
        titleBar.setRightLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(ChatActivity.this)
                        .setTitle("提示")
                        .setMessage("确定清除聊天记录么？\n删除后无法恢复。")
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EMClient.getInstance().chatManager().getConversation(conversation.conversationId()).clearAllMessages();
                                messageList.refresh();
                                showToast("聊天记录已清除");
                            }
                        })
                        .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        }).create();
                dialog.show();
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
        EaseChatExtendMenu.EaseChatExtendMenuItemClickListener extendMenuItemClickListener = new EaseChatExtendMenu.EaseChatExtendMenuItemClickListener() {
            @Override
            public void onClick(int itemId, View view) {

            }
        };
        inputMenu.registerExtendMenuItem("图片", R.drawable.ic_chat_extend_photo, CHAT_INPUT_EXTEND_MENU_PHOTO, extendMenuItemClickListener);
        inputMenu.registerExtendMenuItem("视频", R.drawable.ic_chat_extend_video, CHAT_INPUT_EXTEND_MENU_VIDEO, extendMenuItemClickListener);
        inputMenu.registerExtendMenuItem("语音通话", R.drawable.ic_chat_extend_call_voice, CHAT_INPUT_EXTEND_MENU_CALL_VOICE, extendMenuItemClickListener);
        inputMenu.registerExtendMenuItem("视频通话", R.drawable.ic_chat_extend_call_video, CHAT_INPUT_EXTEND_MENU_CALL_VIDEO, extendMenuItemClickListener);
        inputMenu.init();
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
                if(checkRecordAudioPermissions()) {
                    return voiceRecorderView.onPressToSpeakBtnTouch(v, event, new EaseVoiceRecorderView.EaseVoiceRecorderCallback() {
                        @Override
                        public void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength) {
                            sendVoiceMessage(voiceFilePath, voiceTimeLength);
                            messageList.refresh();
                            messageList.refreshSelectLast();
                        }
                    });
                }else {
                    return false;
                }
            }
        });
    }

    private boolean checkRecordAudioPermissions(){
        if (ContextCompat.checkSelfPermission(this, PERMISSION_NAME_RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{PERMISSION_NAME_RECORD_AUDIO},
                    PERMISSION_CHECK_REQUEST_RECORD_AUDIO);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CHECK_REQUEST_RECORD_AUDIO:
                if(grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED){
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("您拒绝了录音权限，无法发送音频信息。")
                            .setPositiveButton("重新授权", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkRecordAudioPermissions();
                                }
                            })
                            .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).setCancelable(false).create();
                    dialog.show();
                }
        }
    }

    private void initMessageList() {
        messageList = findViewById(R.id.message_list);
        messageList.init(toChatUsername,EMMessage.ChatType.Chat.ordinal(),null);
        messageList.setShowUserNick(false);
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
        refreshLayout.setEnabled(false);
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
        EMMessage message = EMMessage.createVoiceSendMessage(voiceFilePath, voiceTimeLength, toChatUsername);
        EMClient.getInstance().chatManager().sendMessage(message);
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
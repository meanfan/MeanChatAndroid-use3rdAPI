package com.mean.meanchateasemobapi;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.easeui.ui.EaseChatRoomListener;
import com.hyphenate.easeui.widget.EaseChatInputMenu;
import com.hyphenate.easeui.widget.EaseChatMessageList;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.easeui.widget.EaseVoiceRecorderView;

import static com.hyphenate.easeui.EaseConstant.CHATTYPE_GROUP;

public class ChatActivity extends AppCompatActivity {
    private EaseTitleBar titleBar;
    private EaseChatMessageList messageList;
    private EaseChatInputMenu inputMenu;
    private SwipeRefreshLayout refreshLayout;
    private EaseVoiceRecorderView voiceRecorderView;
    private EaseUser toChatUser;
    private String toChatUsername;
    private int chatType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //toChatUser = getIntent().getParcelableExtra("user");
        //toChatUsername = toChatUser.getUsername();
        toChatUsername = getIntent().getStringExtra("username");
        chatType = getIntent().getIntExtra("chatType",0);
        titleBar = findViewById(R.id.title_bar);
        titleBar.setTitle(toChatUsername);
        titleBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        initMessageList();
        initInputMenu();
    }

    private void initInputMenu() {
        inputMenu = findViewById(R.id.input_menu);
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
    }

    private void sendTextMessage(String content){
        //创建一条文本消息
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        //如果是群聊，设置chattype，默认是单聊
        if (chatType == CHATTYPE_GROUP)
            message.setChatType(EMMessage.ChatType.GroupChat);
        //发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
    }

    private void sendVoiceMessage(String voiceFilePath, int voiceTimeLength){

    }

}

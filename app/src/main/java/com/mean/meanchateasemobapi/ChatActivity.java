package com.mean.meanchateasemobapi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
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
import com.hyphenate.easeui.model.EaseCompat;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseChatExtendMenu;
import com.hyphenate.easeui.widget.EaseChatInputMenu;
import com.hyphenate.easeui.widget.EaseChatMessageList;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.easeui.widget.EaseVoiceRecorderView;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.PathUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity  implements EMMessageListener {
    private static final String PERMISSION_NAME_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static final String PERMISSION_NAME_CAMERA = Manifest.permission.CAMERA;
    private static final int PERMISSION_CHECK_REQUEST_RECORD_AUDIO = 10;
    private static final int PERMISSION_CHECK_REQUEST_CAMERA = 11;
    private static final int CHAT_INPUT_EXTEND_MENU_CAMERA = Menu.FIRST+1;
    private static final int CHAT_INPUT_EXTEND_MENU_PHOTO = Menu.FIRST+2;
    private static final int CHAT_INPUT_EXTEND_MENU_VIDEO = Menu.FIRST+3;
    private static final int CHAT_INPUT_EXTEND_MENU_CALL_VOICE = Menu.FIRST+4;
    private static final int CHAT_INPUT_EXTEND_MENU_CALL_VIDEO = Menu.FIRST+5;
    public static final int REQUEST_CODE_CAMERA = 1;
    public static final int REQUEST_CODE_PHOTO = 2;
    public static final int REQUEST_CODE_VIDEO = 3;

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
    private File cameraFile;
    private Handler handler;
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
                switch (itemId){
                    case CHAT_INPUT_EXTEND_MENU_CAMERA:
                        if (!EaseCommonUtils.isSdcardExist()) {
                            showToast("无法访问存储卡");
                            return;
                        }
                        File imagePath = new File(getFilesDir(), "images");
                        cameraFile = new File(imagePath, EMClient.getInstance().getCurrentUser() + System.currentTimeMillis() + ".jpg");
                        cameraFile.getParentFile().mkdirs();
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    FileProvider.getUriForFile(ChatActivity.this,getPackageName()+".fileprovider", cameraFile));
                        }else {
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
                        }
                        if(checkCameraPermissions()) {
                            startActivityForResult(intent, REQUEST_CODE_CAMERA);
                        }
                        break;
                    case CHAT_INPUT_EXTEND_MENU_PHOTO:
                        Intent intent1;
                        if (Build.VERSION.SDK_INT < 19) {
                            intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                            intent1.setType("image/*");
                        } else {
                            intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        }
                        startActivityForResult(intent1, REQUEST_CODE_PHOTO);
                        break;
                    case CHAT_INPUT_EXTEND_MENU_CALL_VOICE:
                        //TODO
                        break;
                    case CHAT_INPUT_EXTEND_MENU_CALL_VIDEO:
                        //TODO
                        break;
                }
            }
        };
        inputMenu.registerExtendMenuItem("相机", R.drawable.ic_chat_extend_camera, CHAT_INPUT_EXTEND_MENU_CAMERA, extendMenuItemClickListener);
        inputMenu.registerExtendMenuItem("图片", R.drawable.ic_chat_extend_photo, CHAT_INPUT_EXTEND_MENU_PHOTO, extendMenuItemClickListener);
        //inputMenu.registerExtendMenuItem("视频", R.drawable.ic_chat_extend_video, CHAT_INPUT_EXTEND_MENU_VIDEO, extendMenuItemClickListener);
        //inputMenu.registerExtendMenuItem("语音通话", R.drawable.ic_chat_extend_call_voice, CHAT_INPUT_EXTEND_MENU_CALL_VOICE, extendMenuItemClickListener);
        //inputMenu.registerExtendMenuItem("视频通话", R.drawable.ic_chat_extend_call_video, CHAT_INPUT_EXTEND_MENU_CALL_VIDEO, extendMenuItemClickListener);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_CODE_CAMERA: {
                if (resultCode == Activity.RESULT_OK) {
                    if(cameraFile != null && cameraFile.exists())
                    sendImageMessage(cameraFile.getAbsolutePath());
                }
                break;
            }
            case REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri imageUri = data.getData();
                        if (imageUri != null) {
                            String[] filePaths = { MediaStore.Images.Media.DATA };
                            Cursor cursor = getContentResolver().query(imageUri, filePaths, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePaths[0]);
                                String picturePath = cursor.getString(columnIndex);
                                cursor.close();
                                cursor = null;
                                if (picturePath == null || picturePath.equals("null")) {
                                    showToast("无法读取图片");
                                }else {
                                    sendImageMessage(picturePath);
                                }
                            } else {
                                File file = new File(imageUri.getPath());
                                if (!file.exists()) {
                                    showToast("无法找到图片");

                                }else {
                                    sendImageMessage(file.getAbsolutePath());
                                }
                            }
                        }
                    }
                }

                break;
            case REQUEST_CODE_VIDEO:
                //TODO
                break;
        }
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

    private boolean checkCameraPermissions(){
        if (ContextCompat.checkSelfPermission(this, PERMISSION_NAME_CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{PERMISSION_NAME_CAMERA},
                    PERMISSION_CHECK_REQUEST_CAMERA);
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
                break;
            case PERMISSION_CHECK_REQUEST_CAMERA:
                if(grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED){
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("您拒绝了相机权限，无法拍摄图片。")
                            .setPositiveButton("重新授权", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkCameraPermissions();
                                }
                            })
                            .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).setCancelable(false).create();
                    dialog.show();
                }
                break;
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
                .getConversation(toChatUsername, EMConversation.EMConversationType.Chat, true);
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

    private void sendGeneralMessage(EMMessage message){
        message.setChatType(EMMessage.ChatType.Chat);
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
        EMClient.getInstance().chatManager().sendMessage(message);
    }

    private void sendTextMessage(String content){
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        sendGeneralMessage(message);
    }

    private void sendImageMessage(String path){
        EMMessage message = EMMessage.createImageSendMessage(path, false, toChatUsername);
        sendGeneralMessage(message);
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

package com.mean.meanchateasemobapi;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMConversationListener;
import com.hyphenate.EMError;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseContactList;
import com.hyphenate.easeui.widget.EaseConversationList;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.util.NetUtils;
import com.mean.meanchateasemobapi.controller.ClientMessageManager;
import com.mean.meanchateasemobapi.fragment.ChatFragment;
import com.mean.meanchateasemobapi.fragment.ContactsFragment;
import com.mean.meanchateasemobapi.fragment.MeFragment;
import com.mean.meanchateasemobapi.model.ClientMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity
        implements ChatFragment.OnChatFragmentInteractionListener,
        ContactsFragment.OnContactsFragmentInteractionListener,
        MeFragment.OnFragmentInteractionListener{
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final String PERMISSION_NAME_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int PERMISSION_CHECK_REQUEST_STORAGE = 10;
    public static final int REQUEST_CODE_SHOW_USER_MESSAGE = 1000;
    public static final int MENU_ID_DELETE = Menu.FIRST+1;
    private EaseTitleBar titleBar;
    private ChatFragment chatFragment;
    private ContactsFragment contactsFragment;
    private MeFragment meFragment;
    private List<Fragment> fragments;
    private int currentFragment;
    Handler handler = new Handler();

    int chatSizeIgnoreSort = 5; //一个大致的数即可


    private BottomNavigationView navigation;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if(fragments.size()<3){
                return false;
            }
            switch (item.getItemId()) {
                case R.id.navigation_chat: {
                    return switchFragment(currentFragment, 0);
                }
                case R.id.navigation_contacts: {
                    return switchFragment(currentFragment, 1);
                }
                case R.id.navigation_me: {
                    return switchFragment(currentFragment, 2);
                }
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragments = new ArrayList<>();

        titleBar = findViewById(R.id.title_bar);
        titleBar.setTitle("MeanChat");
        titleBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        chatFragment = ChatFragment.newInstance();
        EMClient.getInstance().chatManager().addMessageListener(chatFragment);
        chatFragment.setOnFragmentInteractionListener(this);
        fragments.add(chatFragment);

        contactsFragment = ContactsFragment.newInstance();
        contactsFragment.setContactsFragmentInteractionListener(this);
        fragments.add(contactsFragment);

        meFragment = MeFragment.newInstance(EMClient.getInstance().getCurrentUser());
        fragments.add(meFragment);

        navigation = findViewById(R.id.navigation);
        currentFragment = 0;
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.ll_main,chatFragment).show(chatFragment).commit();
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_chat);

        class MyEMConnectionListener implements EMConnectionListener {

            @Override
            public void onConnected() {

            }
            @Override
            public void onDisconnected(final int error) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if(error == EMError.USER_REMOVED){
                            showToast("帐号已经被移除");

                        }else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                            showToast("帐号在其他设备登录");
                        } else {
                            if (NetUtils.hasNetwork(MainActivity.this)) {
                                showToast("无法连接聊天服务器");
                            }else{
                                showToast("当前网络不可用，请检查网络设置");
                            }
                        }
                    }
                });
            }
        }
        EMClient.getInstance().addConnectionListener(new MyEMConnectionListener());
        EMClient.getInstance().chatManager().addConversationListener(new EMConversationListener() {
            @Override
            public void onCoversationUpdate() {
                refreshChatList(chatSizeIgnoreSort);
            }
        });
        class MyEMContactListener implements EMContactListener{
            @Override
            public void onContactAdded(String username) {
                final String content = String.format("%s 已成为你的好友",username);
                ClientMessageManager.getInstance().addNewMessage("新好友",content,ClientMessage.Type.FRIEND_NEW);
                if(contactsFragment!=null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshContactListFromServer(contactsFragment.getContactList());
                            contactsFragment.setMessageView(content);
                        }
                    });

                }
            }

            @Override
            public void onContactDeleted(String username) {

                final String content = String.format("好友 %s 已从您联系人列表移除",username);
                ClientMessageManager.getInstance().addNewMessage("好友信息",content,ClientMessage.Type.FRIEND_CHANGED);
                if(contactsFragment!=null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshContactListFromServer(contactsFragment.getContactList());
                            contactsFragment.setMessageView(content);
                        }
                    });

                }
            }

            @Override
            public void onContactInvited(String username, String reason) {
                String content = String.format("%s 请求加您为好友",username);
                if(!reason.isEmpty()){
                    content = content.concat(String.format(",申请理由:\n%s",reason));
                }
                ClientMessageManager.getInstance().addNewMessage("好友请求",content,ClientMessage.Type.FRIEND_REQUEST,username);
                if(contactsFragment!=null){
                    final String finalContent = content;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            contactsFragment.setMessageView(finalContent);
                        }
                    });
                }
            }

            @Override
            public void onFriendRequestAccepted(String username) {
                final String content = String.format("%s 已同意您的好友请求",username);
                //showToast(content);
                ClientMessageManager.getInstance().addNewMessage("好友信息",content,ClientMessage.Type.INFORMATION);
                if(contactsFragment!=null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            contactsFragment.setMessageView(content);
                        }
                    });
                }

            }

            @Override
            public void onFriendRequestDeclined(String username) {
                final String content = String.format(" %s 拒绝了你的好友请求",username);
                //showToast(content);
                ClientMessageManager.getInstance().addNewMessage("好友信息",content,ClientMessage.Type.INFORMATION);
                if(contactsFragment!=null){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            contactsFragment.setMessageView(content);
                        }
                    });
                }
            }
        }
        EMClient.getInstance().contactManager().setContactListener(new MyEMContactListener());
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkStoragePermissions();
    }

    private void checkStoragePermissions(){
            if (ContextCompat.checkSelfPermission(this, PERMISSION_NAME_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{PERMISSION_NAME_EXTERNAL_STORAGE},
                        PERMISSION_CHECK_REQUEST_STORAGE);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CHECK_REQUEST_STORAGE:
                if(grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED){
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("您拒绝了必要权限，应用将无法运行。")
                            .setPositiveButton("重新授权", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkStoragePermissions();
                                }
                            })
                            .setNegativeButton("退出应用", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).setCancelable(false).create();
                    dialog.show();
                }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,MENU_ID_DELETE,1,"删除");
    }

    private boolean switchFragment(int currentFragment, int index) {
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        if(currentFragment!=index && fragments.get(index)!=null)
        {
            transaction.hide(fragments.get(currentFragment));  //隐藏当前Fragment
            if(!fragments.get(index).isAdded()) {
                transaction.add(R.id.ll_main, fragments.get(index));
            }
            transaction.show(fragments.get(index)).commitAllowingStateLoss();
            this.currentFragment = index;
            if(index == 1){
                titleBar.setRightLayoutVisibility(View.VISIBLE);
                titleBar.setRightImageResource(R.drawable.ic_add_user_white);
                titleBar.setRightLayoutVisibility(View.VISIBLE);
                titleBar.setRightLayoutClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onContactsAddFriend();
                    }
                });
            }else {
                titleBar.setRightLayoutVisibility(View.INVISIBLE);
            }
            return true;
        }
        return false;
    }


    public void showToast(String msg){
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onChatFragmentStart(final EaseConversationList chatList) {
        refreshChatList(chatSizeIgnoreSort);
        registerForContextMenu(chatList);
    }

    @Override
    public void onChatFragmentResume(EaseConversationList chatList) {
        refreshChatList(chatSizeIgnoreSort);
    }

    private void refreshChatList(int sizeIgnoreSort){
        final EaseConversationList chatList = chatFragment.getChatList();
        chatList.init(loadChatListFromServer(sizeIgnoreSort));
        chatList.refresh();
        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EMConversation conversation = chatList.getItem(position);
                Log.d(TAG, "onItemClick: "+conversation.getType());
                //if(conversation.getType() == EMConversation.EMConversationType.Chat){
                    String username = conversation.conversationId();
                    EaseUser user = new EaseUser(username);
                    onChatUserClick(user);
                //}
            }
        });
    }

    private List<EMConversation> loadChatListFromServer(int sizeIgnoreSort){
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        List<Pair<Long, EMConversation>> sortList = new ArrayList<>();
        /*
         * lastMsgTime will change if there is new message during sorting
         * so use synchronized to make sure timestamp of last message won't change.
         */
        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    sortList.add(new Pair<>(conversation.getLastMessage().getMsgTime(), conversation));
                }
            }
        }
        if(conversations.size()>sizeIgnoreSort) {
            sortChatByLastChatTime(sortList);
        }
        List<EMConversation> list = new ArrayList<>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second);
        }
        return list;
    }

    private void sortChatByLastChatTime(List<Pair<Long, EMConversation>> list){
        Collections.sort(list,new Comparator<Pair<Long, EMConversation>>() {
            @Override
            public int compare(Pair<Long, EMConversation> o1, Pair<Long, EMConversation> o2) {
                //按时间倒序
                if(o2.first-o1.first>0)
                    return 1;
                else if(o2.first-o1.first<0)
                    return -1;
                else
                    return 0;
            }
        });
    }

    @Override
    public void onChatUserClick(EaseUser user) {
        Intent intent = new Intent(MainActivity.this,ChatActivity.class);
        //intent.putExtra("user",user);
        intent.putExtra("username",user.getUsername());
        startActivity(intent);
    }


    @Override
    public void onContactsFragmentStart(final EaseContactList contactList) {
        refreshContactListFromServer(contactList);
        registerForContextMenu(contactList.getListView());
        contactList.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EaseUser user = (EaseUser)contactList.getListView().getItemAtPosition(position);
                onChatUserClick(user);
            }
        });
        contactList.getListView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
    }

    private void refreshContactListFromServer(final EaseContactList contactList) {
        EMClient.getInstance().contactManager().aysncGetAllContactsFromServer(new EMValueCallBack<List<String>>() {
            @Override
            public void onSuccess(List<String> value) {
                final List<EaseUser> easeUserList = new ArrayList<>();
                for (String username : value) {
                    EaseUser easeUser = new EaseUser(username);
                    easeUserList.add(easeUser);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        contactList.init(easeUserList);
                        contactList.refresh();
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    @Override
    public void onContactsFragmentResume(EaseContactList contactList) {
        contactsFragment.refreshMessage();
        refreshContactListFromServer(contactList);
    }

    @Override
    public void onContactsFriendDeleteSuccess() {
        refreshContactListFromServer(contactsFragment.getContactList());
    }

    @Override
    public void onContactsFriendDeleteFailure() {
        showToast("删除失败，请重试");
    }

    @Override
    public void onContactsMessageClick() {
        Intent intent = new Intent(MainActivity.this,MessagesActivity.class);
        startActivity(intent);
    }

    public void onContactsAddFriend() {
        Intent intent = new Intent(MainActivity.this,AddFriendActivity.class);
        startActivityForResult(intent,REQUEST_CODE_SHOW_USER_MESSAGE);
    }

    @Override
    public void onMeFragmentInteraction(Uri uri) {
        final Handler handler = new Handler();
        if(uri.compareTo(MeFragment.URI_VIEW_BUTTON_LOGOUT)==0){
            EMClient.getInstance().logout(true, new EMCallBack() {
                void showToastSafe(final String message){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showToast(message);
                        }
                    });
                }

                @Override
                public void onSuccess() {
                    showToastSafe("注销成功");
                    finish();

                }

                @Override
                public void onProgress(int progress, String status) {

                }

                @Override
                public void onError(int code, String message) {
                    showToastSafe("注销失败");
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_CODE_SHOW_USER_MESSAGE:
                if(resultCode==MessagesActivity.RESULT_CODE_NO_UNREAD_MESSAGE){
                    if(contactsFragment!=null){
                        contactsFragment.clearMessageView();
                    }
                }
        }
    }
}

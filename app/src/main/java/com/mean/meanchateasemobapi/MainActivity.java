package com.mean.meanchateasemobapi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseContactList;
import com.hyphenate.easeui.widget.EaseConversationList;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.util.NetUtils;
import com.mean.meanchateasemobapi.adapter.ChatRecyclerViewAdapter;
import com.mean.meanchateasemobapi.fragment.ChatFragment;
import com.mean.meanchateasemobapi.fragment.ContactsFragment;
import com.mean.meanchateasemobapi.fragment.MeFragment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity
        implements ChatFragment.OnChatFragmentInteractionListener,
        ContactsFragment.OnContactsFragmentInteractionListener,
        MeFragment.OnFragmentInteractionListener{
    private EaseTitleBar titleBar;
    private ChatFragment chatFragment;
    private ContactsFragment contactsFragment;
    private MeFragment meFragment;
    private List<Fragment> fragments;
    private int currentFragment;
    private List<ChatRecyclerViewAdapter.ChatItem> chatItems;
    Handler handler = new Handler();


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
        chatItems = new ArrayList<>();
        fragments = new ArrayList<>();

        titleBar = findViewById(R.id.title_bar);
        titleBar.setTitle("MeanChat");
        titleBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        chatFragment = ChatFragment.newInstance();
        chatFragment.setChatItems(loadLocalChatList());
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
    }

    private boolean switchFragment(int currentFragment,int index)
    {
        FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
        if(currentFragment!=index && fragments.get(index)!=null)
        {
            transaction.hide(fragments.get(currentFragment));  //隐藏当前Fragment
            if(!fragments.get(index).isAdded()) {
                transaction.add(R.id.ll_main, fragments.get(index));
            }
            transaction.show(fragments.get(index)).commitAllowingStateLoss();
            this.currentFragment = index;
            return true;
        }
        return false;
    }

    public List<ChatRecyclerViewAdapter.ChatItem> loadLocalChatList(){
        //TODO load from cache
        List<ChatRecyclerViewAdapter.ChatItem> list = new ArrayList<>();
        ChatRecyclerViewAdapter.ChatItem chatItem = new ChatRecyclerViewAdapter.ChatItem();
        EaseUser user = new EaseUser("user1");
        chatItem.user = user;
        chatItem.date = new Date();
        chatItem.message = "Hello~~~";
        list.add(chatItem);
        return list;
    }

    public void showToast(String msg){
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onChatFragmentStart(final EaseConversationList chatList) {
        int sizeIgnoreSort = 5; //一个大致的数即可
        chatList.init(loadChatList(sizeIgnoreSort));
        chatList.refresh();
        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EMConversation conversation = chatList.getItem(position);
                if(conversation.getType() == EMConversation.EMConversationType.Chat){
                    String username = conversation.conversationId();
                    EaseUser user = new EaseUser(username);
                    onChatUserClick(user);
                }

            }
        });
    }

    private List<EMConversation> loadChatList(int sizeIgnoreSort){
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        List<Pair<Long, EMConversation>> sortList = new ArrayList<>();
        /**
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
        intent.putExtra("chatType",EMMessage.ChatType.Chat);
        startActivity(intent);
    }

    @Override
    public void onChatUserLongClick(EaseUser user) {

    }

    @Override
    public void onContactFragmentStart(final EaseContactList contactList) {
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
        contactList.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EaseUser user = (EaseUser)contactList.getListView().getItemAtPosition(position);
                onChatUserClick(user);
            }
        });
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
}

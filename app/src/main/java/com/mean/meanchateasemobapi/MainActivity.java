package com.mean.meanchateasemobapi;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.util.NetUtils;
import com.mean.meanchateasemobapi.adapter.ChatRecyclerViewAdapter;
import com.mean.meanchateasemobapi.fragment.ChatFragment;
import com.mean.meanchateasemobapi.fragment.ContactsFragment;
import com.mean.meanchateasemobapi.fragment.MeFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends FragmentActivity
        implements ChatFragment.OnChatFragmentInteractionListener,
        ContactsFragment.OnFragmentInteractionListener,
        MeFragment.OnFragmentInteractionListener{
    private ChatFragment chatFragment;
    private ContactsFragment contactsFragment;
    private MeFragment meFragment;
    private List<Fragment> fragments;
    private int currentFragment;
    private List<ChatRecyclerViewAdapter.ChatItem> chatItems;


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

        chatFragment = ChatFragment.newInstance();
        chatFragment.setChatItems(loadLocalChatList());
        chatFragment.setOnFragmentInteractionListener(this);
        fragments.add(chatFragment);

        contactsFragment = ContactsFragment.newInstance();
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
        chatItem.icon = BitmapFactory.decodeResource(getResources(),R.drawable.cool_guest);
        chatItem.name = "user1";
        chatItem.date = new Date();
        chatItem.message = "Hello~~~";
        list.add(chatItem);
        return list;
    }

    public void showToast(String msg){
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onChatUserClick(String username) {
        Intent intent = new Intent(MainActivity.this,ChatActivity.class);
        intent.putExtra("username",username);
        startActivity(intent);
    }

    @Override
    public void onChatUserLongClick(String username) {
        //TODO 长按菜单
    }

    @Override
    public void onContactsFragmentInteraction(Uri uri) {

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

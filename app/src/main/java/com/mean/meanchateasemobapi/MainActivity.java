package com.mean.meanchateasemobapi;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMConversationListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.util.NetUtils;
import com.mean.meanchateasemobapi.broadcast.CallReceiver;
import com.mean.meanchateasemobapi.fragment.ChatFragment;
import com.mean.meanchateasemobapi.fragment.ContactsFragment;
import com.mean.meanchateasemobapi.fragment.MeFragment;
import com.mean.meanchateasemobapi.util.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity
        implements ChatFragment.OnChatFragmentInteractionListener,
        ContactsFragment.OnContactsFragmentInteractionListener,
        MeFragment.OnFragmentInteractionListener{
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_CODE_SHOW_USER_MESSAGE = 1000;
    public static final int MENU_ID_DELETE = Menu.FIRST+1;
    private MyEMConnectionListener connectionListener;
    private MyMessageListener messageListener;
    private MyConversationListener conversationListener;
    private CallReceiver callReceiver;

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
        
        connectionListener = new MyEMConnectionListener();
        EMClient.getInstance().addConnectionListener(connectionListener);

        messageListener = new MyMessageListener();
        EMClient.getInstance().chatManager().addMessageListener(messageListener);

        conversationListener = new MyConversationListener();
        EMClient.getInstance().chatManager().addConversationListener(conversationListener);

        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        callReceiver = new CallReceiver();
        registerReceiver(callReceiver, callFilter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionChecker.checkStoragePermission(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().removeConnectionListener(connectionListener);
        unregisterReceiver(callReceiver);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PermissionChecker.PERMISSION_CHECK_REQUEST_STORAGE:
                if(grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED){
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle(R.string.dialog_title_notice)
                            .setMessage(getResources().getString(R.string.main_message_permission_refused))
                            .setPositiveButton(getResources().getString(R.string.dialog_button_grant_permission), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PermissionChecker.checkStoragePermission(MainActivity.this);
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.dialog_button_quit), new DialogInterface.OnClickListener() {
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
        menu.add(0,MENU_ID_DELETE,1,getString(R.string.menu_item_delete));
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
    public void onChatUserClick(EaseUser user) {
        startChatActivity(user.getUsername());
    }

    @Override
    public void startChatActivity(String username) {
        Intent intent = new Intent(MainActivity.this,ChatActivity.class);
        intent.putExtra("username",username);
        startActivity(intent);
    }

    @Override
    public void onContactsFriendDeleteFailure() {
        showToast(getString(R.string.action_message_delete_failure));
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
                    showToastSafe(getString(R.string.main_message_logout_success));
                    finish();

                }

                @Override
                public void onProgress(int progress, String status) {

                }

                @Override
                public void onError(int code, String message) {
                    showToastSafe(getString(R.string.main_message_logout_failure));
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
                        //contactsFragment.refreshContactListFromServer();
                    }
                }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //返回不退出
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

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
                        showToast(getString(R.string.main_message_account_removed));

                    }else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                        showToast(getString(R.string.main_message_account_login_another_device));
                    } else {
                        if (NetUtils.hasNetwork(MainActivity.this)) {
                            showToast(getString(R.string.main_message_chat_server_connect_failure));
                        }else{
                            showToast(getString(R.string.main_message_network_invalid));
                        }
                    }
                }
            });
        }
    }

    class MyMessageListener implements EMMessageListener{
        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            chatFragment.refreshChatListFromServer(chatSizeIgnoreSort);
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
            chatFragment.refreshChatListFromServer(chatSizeIgnoreSort);
        }
    }

    class MyConversationListener implements EMConversationListener {
        @Override
        public void onCoversationUpdate() {
            Log.d(TAG, "onCoversationUpdate: refreshChatListFromServer");
            chatFragment.refreshChatListFromServer(chatSizeIgnoreSort);
        }
    }
}

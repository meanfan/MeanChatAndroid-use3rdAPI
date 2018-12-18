package com.mean.meanchateasemobapi.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.EMContactListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseContactList;
import com.hyphenate.exceptions.HyphenateException;
import com.mean.meanchateasemobapi.MainActivity;
import com.mean.meanchateasemobapi.R;
import com.mean.meanchateasemobapi.controller.ClientMessageManager;
import com.mean.meanchateasemobapi.model.ClientMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ContactsFragment extends Fragment {
    public static final String TAG = ContactsFragment.class.getSimpleName();
    private EaseContactList contactList;
    private LinearLayout ll_message;
    private TextView tv_message;
    private ImageView iv_message_dot;

    private MyEMContactListener contactListener;
    private boolean isContactListInit =false;
    private String cachedUserMessage;
    private Handler handler;
    private OnContactsFragmentInteractionListener mListener;
    private List<EaseUser> easeUsers;
    private Map<String, EaseUser> contactsMap;
    private boolean isHidden;

    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ContactsFragment newInstance() {
        ContactsFragment fragment = new ContactsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactListener = new MyEMContactListener();
        EMClient.getInstance().contactManager().setContactListener(contactListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_contacts, container, false);
        contactList = view.findViewById(R.id.contact_list);
        ll_message = view.findViewById(R.id.ll_message);
        tv_message = view.findViewById(R.id.tv_message);
        iv_message_dot = view.findViewById(R.id.iv_message_dot);
        ll_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onContactsMessageClick();
            }
        });
        handler = new Handler();
        contactList.setShowSiderBar(false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        easeUsers = new ArrayList<>();
        refreshContactListFromServer();
        contactList.init(easeUsers);
        isContactListInit = true;
        contactList.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EaseUser user = (EaseUser)contactList.getListView().getItemAtPosition(position);
                mListener.startChatActivity(user.getUsername());
            }
        });
        getActivity().registerForContextMenu(contactList.getListView());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        isHidden = hidden;
        if(!isHidden){
            refreshUI();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isHidden){
            refreshUI();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        isContactListInit = false;
        cachedUserMessage = null;
    }

    public void refreshUI(){
        if(cachedUserMessage==null){
            clearMessageView();
        }else {
            setMessageView(cachedUserMessage);
        }
        refreshContactListFromServer();
    }

    public void refreshContactListFromServer() {
        EMClient.getInstance().contactManager().aysncGetAllContactsFromServer(new EMValueCallBack<List<String>>() {
            @Override
            public void onSuccess(final List<String> value) {
                easeUsers.clear();
                for(String username:value){
                    easeUsers.add(new EaseUser(username));
                }
                contactList.refresh();
                contactList.getListView().postInvalidate();
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo cmi=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int pos=cmi.position;
         EaseUser user = (EaseUser) contactList.getListView().getItemAtPosition(pos);
        if(item.getItemId() == MainActivity.MENU_ID_DELETE){
            try {
                EMClient.getInstance().contactManager().deleteContact(user.getUsername());
                //refreshContactListFromServer();
            } catch (HyphenateException e) {
                e.printStackTrace();
                mListener.onContactsFriendDeleteFailure();
            }
        }
        return super.onContextItemSelected(item);
    }

    public void clearMessageView(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(isContactListInit){
                    tv_message.setText(R.string.message_default);
                    iv_message_dot.setVisibility(View.INVISIBLE);
                }
                cachedUserMessage = null;
            }
        });
    }

    public void setMessageView(final String message){
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isContactListInit){
                    tv_message.setText(message);
                    iv_message_dot.setVisibility(View.VISIBLE);
                }else{
                    cachedUserMessage = message;
                }
            }
        });
    }

    public void setContactsFragmentInteractionListener(OnContactsFragmentInteractionListener mListener) {
        this.mListener = mListener;
    }

    public interface OnContactsFragmentInteractionListener {
        void onContactsMessageClick();
        void startChatActivity(String username);
        void onContactsFriendDeleteFailure();
    }

    class MyEMContactListener implements EMContactListener {
        @Override
        public void onContactAdded(String username) {
            final String content = String.format("%s 已成为你的好友",username);
            Log.d(TAG, "onContactAdded: "+username);
            ClientMessageManager.getInstance().addNewMessage("新好友",content,ClientMessage.Type.FRIEND_NEW);
            setMessageView(content);
            refreshContactListFromServer();
        }

        @Override
        public void onContactDeleted(String username) {
            final String content = String.format("好友 %s 已从您联系人列表移除",username);
            ClientMessageManager.getInstance().addNewMessage("好友信息",content,ClientMessage.Type.FRIEND_CHANGED);
            setMessageView(content);
            refreshContactListFromServer();
        }

        @Override
        public void onContactInvited(String username, String reason) {
            String content = String.format("%s 请求加您为好友",username);
            if(!reason.isEmpty()){
                content = content.concat(String.format(",申请理由:\n%s",reason));
            }
            Log.d(TAG, "onContactAdded: "+username);
            ClientMessageManager.getInstance().addNewMessage("好友请求",content,ClientMessage.Type.FRIEND_REQUEST,username);
            setMessageView(content);
        }

        @Override
        public void onFriendRequestAccepted(String username) {
            final String content = String.format("%s 已同意您的好友请求",username);
            ClientMessageManager.getInstance().addNewMessage("好友信息",content,ClientMessage.Type.INFORMATION);
            setMessageView(content);
        }

        @Override
        public void onFriendRequestDeclined(String username) {
            final String content = String.format(" %s 拒绝了你的好友请求",username);
            ClientMessageManager.getInstance().addNewMessage("好友信息",content,ClientMessage.Type.INFORMATION);
            setMessageView(content);
        }
    }
}

package com.mean.meanchateasemobapi.fragment;

import android.graphics.drawable.ColorDrawable;
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
import com.hyphenate.easeui.adapter.EaseContactAdapter;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseContactList;
import com.hyphenate.exceptions.HyphenateException;
import com.mean.meanchateasemobapi.MainActivity;
import com.mean.meanchateasemobapi.R;
import com.mean.meanchateasemobapi.controller.ClientMessageManager;
import com.mean.meanchateasemobapi.model.ClientMessage;

import java.util.ArrayList;
import java.util.List;


public class ContactsFragment extends Fragment {
    public static final String TAG = ContactsFragment.class.getSimpleName();
    private EaseContactList contactList;
    private LinearLayout ll_message;
    private TextView tv_message;
    private ImageView iv_message_dot;

    private MyEMContactListener contactListener;
    private boolean isContactListInit =false;
    private Handler handler;
    private OnContactsFragmentInteractionListener mListener;
    private List<EaseUser> easeUsers;
    private boolean isHidden;

    public ContactsFragment() {
        contactListener = new MyEMContactListener();
        EMClient.getInstance().contactManager().setContactListener(contactListener);
    }

    public static ContactsFragment newInstance() {
        ContactsFragment fragment = new ContactsFragment();
        return fragment;
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
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        easeUsers = new ArrayList<>();
        refreshContactListFromServer();
        contactList.init(easeUsers);
        contactList.setShowSiderBar(false);
        ((EaseContactAdapter)contactList.getListView().getAdapter()).setInitialLetterBg(
                new ColorDrawable(getResources().getColor(R.color.initial_letter_bg)));
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
    }

    public void refreshUI(){
        ClientMessage message = ClientMessageManager.getInstance().findLatestUnreadMessage();
        if(message!=null){
            setMessageView(message.getContext());
        }else {
            clearMessageView();
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
                    tv_message.setText(R.string.message_no_new_message);
                    iv_message_dot.setVisibility(View.INVISIBLE);
                }
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
            final String content = String.format(getString(R.string.contacts_new_friend_message_format),username);
            Log.d(TAG, "onContactAdded: "+username);
            ClientMessageManager.getInstance().addNewMessage(getString(R.string.contacts_new_friend_title),content,ClientMessage.Type.FRIEND_NEW);
            setMessageView(content);
            refreshContactListFromServer();
        }

        @Override
        public void onContactDeleted(String username) {
            final String content = String.format(getString(R.string.contacts_del_friend_message_format),username);
            ClientMessageManager.getInstance().addNewMessage(getString(R.string.contacts_del_friend_title),content,ClientMessage.Type.FRIEND_CHANGED);
            setMessageView(content);
            refreshContactListFromServer();
        }

        @Override
        public void onContactInvited(String username, String reason) {
            String content = String.format(getString(R.string.contacts_friend_request_message_format_1),username);
            if(!reason.isEmpty()){
                content = content.concat(String.format(getString(R.string.contacts_friend_request_message_format_2),reason));
            }
            Log.d(TAG, "onContactAdded: "+username);
            ClientMessageManager.getInstance().addNewMessage(getString(R.string.contacts_friend_request_title),content,ClientMessage.Type.FRIEND_REQUEST,username);
            setMessageView(content);
        }

        @Override
        public void onFriendRequestAccepted(String username) {
            final String content = String.format(getString(R.string.contacts_friend_accepted_message_format),username);
            ClientMessageManager.getInstance().addNewMessage(getString(R.string.contacts_friend_accepted_title),content,ClientMessage.Type.INFORMATION);
            setMessageView(content);
        }

        @Override
        public void onFriendRequestDeclined(String username) {
            final String content = String.format(getString(R.string.contacts_friend_refused_message_format),username);
            ClientMessageManager.getInstance().addNewMessage(getString(R.string.contacts_friend_refused_title),content,ClientMessage.Type.INFORMATION);
            setMessageView(content);
        }
    }
}

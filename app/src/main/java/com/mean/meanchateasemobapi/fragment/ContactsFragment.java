package com.mean.meanchateasemobapi.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseContactList;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.exceptions.HyphenateException;
import com.mean.meanchateasemobapi.MainActivity;
import com.mean.meanchateasemobapi.R;
import com.mean.meanchateasemobapi.controller.ClientMessageManager;


public class ContactsFragment extends Fragment {
    private EaseContactList contactList;
    private LinearLayout ll_message;
    private TextView tv_message;
    private ImageView iv_message_dot;
    private boolean isCreate =false;
    private String cachedUserMessage;
    private OnContactsFragmentInteractionListener mListener;

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
        isCreate =true;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mListener.onContactsFragmentStart(contactList);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(cachedUserMessage==null){
            clearMessageView();
        }else {
            setMessageView(cachedUserMessage);
        }
        mListener.onContactsFragmentResume(contactList);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        isCreate = false;
        cachedUserMessage = null;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo cmi=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int pos=cmi.position;
         EaseUser user = (EaseUser) contactList.getListView().getItemAtPosition(pos);
        if(item.getItemId() == MainActivity.MENU_ID_DELETE){
            try {
                EMClient.getInstance().contactManager().deleteContact(user.getUsername());
                mListener.onContactsFriendDeleteSuccess();
            } catch (HyphenateException e) {
                e.printStackTrace();
                mListener.onContactsFriendDeleteSuccess();
            }
        }
        return super.onContextItemSelected(item);
    }

    public void clearMessageView(){
        if(isCreate){
            tv_message.setText(R.string.message_default);
            iv_message_dot.setVisibility(View.INVISIBLE);
            cachedUserMessage = null;
        }
    }

    public void setMessageView(String message){
        if (isCreate){
            tv_message.setText(message);
            iv_message_dot.setVisibility(View.VISIBLE);
        }else{
            cachedUserMessage = message;
        }

    }

    public void setContactsFragmentInteractionListener(OnContactsFragmentInteractionListener mListener) {
        this.mListener = mListener;
    }

    public void refreshMessage(){
        if(!isCreate){
            return;
        }
        if(ClientMessageManager.getInstance().isHasUnreadMessage()){
            tv_message.setText(R.string.message_unhandled);
            iv_message_dot.setVisibility(View.VISIBLE);
        }else {
            tv_message.setText(R.string.message_default);
            iv_message_dot.setVisibility(View.INVISIBLE);
        }
    }

    public EaseContactList getContactList() {
        return contactList;
    }

    public interface OnContactsFragmentInteractionListener {
        void onContactsFragmentStart(EaseContactList contactList);
        void onContactsFragmentResume(EaseContactList contactList);
        void onContactsMessageClick();
        void onContactsFriendDeleteSuccess();
        void onContactsFriendDeleteFailure();
    }
}

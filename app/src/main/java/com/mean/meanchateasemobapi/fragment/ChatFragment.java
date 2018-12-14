package com.mean.meanchateasemobapi.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseConversationList;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.mean.meanchateasemobapi.MainActivity;
import com.mean.meanchateasemobapi.R;

import java.util.List;

public class ChatFragment extends Fragment implements EMMessageListener {
    private EaseConversationList chatList;

    private OnChatFragmentInteractionListener mListener;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_chat, container, false);
        chatList = view.findViewById(R.id.chat_list);
        mListener.onChatFragmentStart(chatList);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        chatList.refresh();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setOnFragmentInteractionListener(OnChatFragmentInteractionListener listener) {
        this.mListener = listener;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo cmi=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int posMenu=cmi.position;
        EMConversation  conversation = chatList.getItem(posMenu);
        if(item.getItemId() == MainActivity.MENU_ID_DELETE){
            EMClient.getInstance().chatManager().deleteConversation(conversation.conversationId(),false);
        }
        return super.onContextItemSelected(item);
    }

    public interface OnChatFragmentInteractionListener {
        void onChatUserClick(EaseUser user);
        void onChatFragmentStart(EaseConversationList conversationList);
        void onChatFragmentResume(EaseConversationList conversationList);
    }

    public EaseConversationList getChatList() {
        return chatList;
    }

    @Override
    public void onMessageReceived(List<EMMessage> messages) {
        chatList.refresh();
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

    }
}

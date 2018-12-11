package com.mean.meanchateasemobapi.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseConversationList;
import com.mean.meanchateasemobapi.R;

import java.util.List;

public class ChatFragment extends Fragment implements EMMessageListener {
    private EaseConversationList chatListView;

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
        chatListView = view.findViewById(R.id.chat_list);
        mListener.onChatFragmentStart(chatListView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        chatListView.refresh();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setOnFragmentInteractionListener(OnChatFragmentInteractionListener listener) {
        this.mListener = listener;
    }

    public interface OnChatFragmentInteractionListener {
        void onChatUserClick(EaseUser user);
        void onChatUserLongClick(EaseUser user);
        void onChatFragmentStart(EaseConversationList conversationList);
    }

    @Override
    public void onMessageReceived(List<EMMessage> messages) {
        chatListView.refresh();
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

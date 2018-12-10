package com.mean.meanchateasemobapi.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseConversationList;
import com.mean.meanchateasemobapi.R;
import com.mean.meanchateasemobapi.adapter.ChatRecyclerViewAdapter;

import java.util.List;

public class ChatFragment extends Fragment implements ChatRecyclerViewAdapter.OnUserItemInteractionListener{
    private List<ChatRecyclerViewAdapter.ChatItem> chatItems;
    private EaseConversationList conversationListView;

    private OnChatFragmentInteractionListener mListener;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    public void setChatItems(List<ChatRecyclerViewAdapter.ChatItem> chatItems){
        this.chatItems = chatItems;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_chat, container, false);
        conversationListView = view.findViewById(R.id.chat_list);
        mListener.onChatFragmentStart(conversationListView);

        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onUserItemClick(EaseUser user) {
        mListener.onChatUserClick(user);
    }

    @Override
    public void onUserItemLongClick(EaseUser user) {
        mListener.onChatUserLongClick(user);

    }

    public void setOnFragmentInteractionListener(OnChatFragmentInteractionListener listener) {
        this.mListener = listener;
    }

    public interface OnChatFragmentInteractionListener {
        void onChatUserClick(EaseUser user);
        void onChatUserLongClick(EaseUser user);
        void onChatFragmentStart(EaseConversationList conversationList);
    }
}

package com.mean.meanchateasemobapi.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mean.meanchateasemobapi.R;
import com.mean.meanchateasemobapi.adapter.ChatRecyclerViewAdapter;

import java.util.List;

public class ChatFragment extends Fragment implements ChatRecyclerViewAdapter.OnUserItemInteractionListener{
    private List<ChatRecyclerViewAdapter.ChatItem> chatItems;

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
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        ChatRecyclerViewAdapter adapter = new ChatRecyclerViewAdapter(chatItems);
        adapter.setOnUserItemInteractionListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.HORIZONTAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onUserItemClick(String username) {
        mListener.onChatUserClick(username);
    }

    @Override
    public void onUserItemLongClick(String username) {
        mListener.onChatUserLongClick(username);

    }

    public void setOnFragmentInteractionListener(OnChatFragmentInteractionListener listener) {
        this.mListener = listener;
    }

    public interface OnChatFragmentInteractionListener {
        void onChatUserClick(String username);
        void onChatUserLongClick(String username);
    }
}

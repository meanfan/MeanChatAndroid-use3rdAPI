package com.mean.meanchateasemobapi.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {
    public static final String TAG = ChatFragment.class.getSimpleName();
    private EaseConversationList chatList;
    private List<EMConversation> conversations;
    private OnChatFragmentInteractionListener mListener;
    private boolean isCreate = false;
    private boolean isChatListInit = false;

    int chatSizeIgnoreSort = 5; //暂时取一个大致的数
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
        conversations = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_chat, container, false);
        chatList = view.findViewById(R.id.chat_list);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        isCreate = true;
        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EMConversation conversation = chatList.getItem(position);
                Log.d(TAG, "onConversationItemClick: "+conversation.getType());
                String username = conversation.conversationId();
                EaseUser user = new EaseUser(username);
                mListener.onChatUserClick(user);
            }
        });
        getActivity().registerForContextMenu(chatList);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        refreshChatListFromServer(chatSizeIgnoreSort);
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        isCreate =false;
        super.onDestroy();
    }

    public void refreshChatListFromServer(int sizeIgnoreSort){
        if(!isCreate) {
            return;
        }
        if(conversations == null){
            conversations = new ArrayList<>();
        }
        conversations.clear();
        conversations.addAll(loadChatListFromServer(sizeIgnoreSort));
        if(!isChatListInit){
            Log.d(TAG, "refreshChatListFromServer: chatList.init");
            chatList.init(conversations);
            isChatListInit = true;
        }
        chatList.refresh();
    }

    private List<EMConversation> loadChatListFromServer(int sizeIgnoreSort){
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        List<Pair<Long, EMConversation>> sortList = new ArrayList<>();
        List<EMConversation> list = new ArrayList<>();
        /*
         * lastMsgTime will change if there is new message during sorting
         * so use synchronized to make sure timestamp of last message won't change.
         */
        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                Log.d(TAG, "loadChatListFromServer: id:"+conversation.conversationId()+",type:"+conversation.getType());
                if (conversation.getAllMessages().size() != 0) {
                    sortList.add(new Pair<>(conversation.getLastMessage().getMsgTime(), conversation));
                }
            }
            if(conversations.size()>sizeIgnoreSort) {
                sortChatByLastChatTime(sortList);
            }
            for (Pair<Long, EMConversation> sortItem : sortList) {
                list.add(sortItem.second);
            }
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
            chatList.refresh();
        }
        return super.onContextItemSelected(item);
    }

    public interface OnChatFragmentInteractionListener {
        void onChatUserClick(EaseUser user);
    }

}

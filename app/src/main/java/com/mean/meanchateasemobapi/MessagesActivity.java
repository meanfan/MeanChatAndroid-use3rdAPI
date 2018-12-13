package com.mean.meanchateasemobapi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hyphenate.easeui.widget.EaseTitleBar;
import com.mean.meanchateasemobapi.adapter.MessagesRecyclerViewAdapter;
import com.mean.meanchateasemobapi.controller.ClientMessageManager;

public class MessagesActivity extends AppCompatActivity {
    public static final int RESULT_CODE_NO_UNREAD_MESSAGE = 1000;
    public static final int RESULT_CODE_HAS_UNREAD_MESSAGE = 1001;
    private EaseTitleBar titleBar;
    private RecyclerView recyclerView;
    private MessagesRecyclerViewAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        titleBar = findViewById(R.id.title_bar);
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new MessagesRecyclerViewAdapter();
        //TODO init adapter data
        
        recyclerView.setAdapter(adapter);
        titleBar.setTitle("消息");
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ClientMessageManager.getInstance().isHasUnreadMessage()) {
                    setResult(RESULT_CODE_HAS_UNREAD_MESSAGE);
                }else {
                    setResult(RESULT_CODE_NO_UNREAD_MESSAGE);
                }
                finish();
            }
        });
    }
}

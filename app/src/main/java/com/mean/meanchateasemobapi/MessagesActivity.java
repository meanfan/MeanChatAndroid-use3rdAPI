package com.mean.meanchateasemobapi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.hyphenate.easeui.widget.EaseTitleBar;
import com.mean.meanchateasemobapi.adapter.MessagesRecyclerViewAdapter;
import com.mean.meanchateasemobapi.controller.ClientMessageManager;
import com.mean.meanchateasemobapi.model.ClientMessage;

import java.util.Map;

public class MessagesActivity extends AppCompatActivity {
    public static final String TAG = "MessagesActivity";
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        adapter = new MessagesRecyclerViewAdapter();
        adapter.updateData(ClientMessageManager.getInstance().getMessages());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "onCreate: "+adapter.getItemCount());
        titleBar.setTitle(getString(R.string.message_title));
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CODE_NO_UNREAD_MESSAGE);
                finish();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        ClientMessageManager.getInstance().markAllMessageRead();
    }
}

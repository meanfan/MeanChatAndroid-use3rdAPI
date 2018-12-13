package com.mean.meanchateasemobapi;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.exceptions.HyphenateException;

public class AddFriendActivity extends AppCompatActivity {
    private EaseTitleBar titleBar;
    private EditText et_username,et_reason;
    private Button btn_add;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        handler = new Handler();
        titleBar = findViewById(R.id.title_bar);
        et_username = findViewById(R.id.et_username);
        et_reason = findViewById(R.id.et_reason);
        btn_add = findViewById(R.id.btn_add);
        titleBar.setLeftImageResource(R.drawable.ic_left_array_white);
        titleBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = et_username.getText().toString().trim();
                if(username.isEmpty()){
                    showToast("请输入用户名");
                    return;
                }
                String reason = et_reason.getText().toString().trim();
                try {
                    EMClient.getInstance().contactManager().addContact(username, reason);
                    showToast("请求成功，请等待对方同意");
                    finish();
                } catch (HyphenateException e) {
                    showToast("请求失败，请检查用户名");
                    e.printStackTrace();
                }
            }
        });

    }

    private void showToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}

package com.mean.meanchateasemobapi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.mean.meanchateasemobapi.adapter.ChatRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity{
    public static final String TAG = "LoginActivity";

    private TextView et_username;
    private TextView et_password;
    private CheckBox cb_remember;
    private Button btn_login;
    private TextView tv_register;
    private TextView tv_forget;
    private List<View> loginViewList;
    private ProgressBar pb_logining;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        pb_logining = findViewById(R.id.pb_logining);
        pb_logining.setVisibility(View.INVISIBLE);
        cb_remember = findViewById(R.id.cb_remember);
        btn_login = findViewById(R.id.btn_login);
        tv_register = findViewById(R.id.tv_register);
        tv_forget = findViewById(R.id.tv_forget);
        loginViewList = new ArrayList<>();
        loginViewList.add(cb_remember);
        loginViewList.add(btn_login);
        loginViewList.add(tv_register);
        loginViewList.add(tv_forget);

        if(EMClient.getInstance().isLoggedInBefore()){
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
        }
        final Handler handler = new Handler();
        btn_login.setOnClickListener(new View.OnClickListener() {
            void showToastSafe(final String message){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast(message);
                    }
                });
            }
            @Override
            public void onClick(View view) {

                String username = et_username.getText().toString();
                String password = et_password.getText().toString();
                if(username.isEmpty() || password.isEmpty()){
                    showToast("username or password can't be empty");
                    return;
                }
                et_username.setEnabled(false);
                et_password.setEnabled(false);
                updateUI$Logging();
                EMClient.getInstance().login(username, password, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        showToastSafe("登录成功");
                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(int code, String error) {
                        showToastSafe("登录出错："+error);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateUI$AllowLogin();
                            }
                        });
                    }

                    @Override
                    public void onProgress(int progress, String status) { }
                });
            }
        });

    }

    private void updateUI$Logging(){
        et_username.setEnabled(false);
        et_password.setEnabled(false);
        for (View v:loginViewList){
            v.setVisibility(View.INVISIBLE);
        }
        pb_logining.setVisibility(View.VISIBLE);
    }

    private void updateUI$AllowLogin(){
        et_username.setEnabled(true);
        et_password.setEnabled(true);
        for (View v:loginViewList){
            v.setVisibility(View.VISIBLE);
        }
        pb_logining.setVisibility(View.INVISIBLE);

    }


    private void showToast(String msg){
        Toast.makeText(LoginActivity.this,msg,Toast.LENGTH_SHORT).show();
    }


}


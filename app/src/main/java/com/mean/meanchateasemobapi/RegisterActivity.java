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

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity{
    public static final String TAG = "RegisterActivity";
    public static final String RESULT_INTENT_EXTRA_USERNAME = "username";

    private TextView et_username;
    private TextView et_password,et_password_repeat;
    private Button btn_register;
    private TextView tv_login;
    private List<View> loginViewList;
    private ProgressBar pb_register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        et_password_repeat = findViewById(R.id.et_password_repeat);
        pb_register = findViewById(R.id.pb_register);
        pb_register.setVisibility(View.INVISIBLE);
        btn_register = findViewById(R.id.btn_register);
        tv_login = findViewById(R.id.tv_login);
        loginViewList = new ArrayList<>();
        loginViewList.add(btn_register);
        loginViewList.add(tv_login);

        final Handler handler = new Handler();
        btn_register.setOnClickListener(new View.OnClickListener() {
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

                final String username = et_username.getText().toString();
                final String password = et_password.getText().toString();
                String passwordRepeat = et_password_repeat.getText().toString();
                if(username.isEmpty() || password.isEmpty()) {
                    showToast(getString(R.string.register_message_input_empty));
                    return;
                }
                if(!passwordRepeat.equals(password)){
                    showToast(getString(R.string.register_message_two_password_not_match));
                    return;
                }
                et_username.setEnabled(false);
                et_password.setEnabled(false);
                et_password_repeat.setEnabled(false);
                updateUI$Logging();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EMClient.getInstance().createAccount(username,password);
                            showToastSafe(getString(R.string.register_message_success));
                            Intent intent = new Intent();
                            intent.putExtra(RESULT_INTENT_EXTRA_USERNAME,username);
                            setResult(RESULT_OK,intent);
                            finish();
                        } catch (HyphenateException e) {
                            showToastSafe(getString(R.string.register_message_failure));
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateUI$AllowLogin();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void updateUI$Logging(){
        et_username.setEnabled(false);
        et_password.setEnabled(false);
        et_password_repeat.setEnabled(false);
        for (View v:loginViewList){
            v.setVisibility(View.INVISIBLE);
        }
        pb_register.setVisibility(View.VISIBLE);
    }

    private void updateUI$AllowLogin(){
        et_username.setEnabled(true);
        et_password.setEnabled(true);
        et_password_repeat.setEnabled(true);
        for (View v:loginViewList){
            v.setVisibility(View.VISIBLE);
        }
        pb_register.setVisibility(View.INVISIBLE);
    }

    private void showToast(String msg){
        Toast.makeText(RegisterActivity.this,msg,Toast.LENGTH_SHORT).show();
    }


}


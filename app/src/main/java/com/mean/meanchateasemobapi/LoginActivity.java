package com.mean.meanchateasemobapi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity{
    public static final String TAG = "LoginActivity";
    private static final int REQUEST_CODE_REGISTER = 10;

    private TextView et_username;
    private TextView et_password;
    private Button btn_login;
    private TextView tv_register;
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
        btn_login = findViewById(R.id.btn_login);
        tv_register = findViewById(R.id.tv_register);
        loginViewList = new ArrayList<>();
        loginViewList.add(btn_login);
        loginViewList.add(tv_register);
        if(getIntent()!=null){
             String name =  getIntent().getStringExtra("username");
             if(name!=null && !name.isEmpty()){
                 et_username.setText(name);
                 et_username.clearFocus();
                 et_password.requestFocus();
             }
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
                    showToast(getString(R.string.login_message_empty));
                    return;
                }
                et_username.setEnabled(false);
                et_password.setEnabled(false);
                updateUI$Logging();
                EMClient.getInstance().login(username, password, new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        showToastSafe(getString(R.string.login_message_success));
                        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(intent);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                LoginActivity.this.finish();
                            }
                        });
                    }

                    @Override
                    public void onError(int code, String error) {
                        showToastSafe(getString(R.string.login_message_error)+": "+error);
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
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivityForResult(intent,REQUEST_CODE_REGISTER);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_CODE_REGISTER:
                if(resultCode == RESULT_OK){
                    if(data!=null) {
                        String username = data.getStringExtra(RegisterActivity.RESULT_INTENT_EXTRA_USERNAME);
                        if (username != null && !username.trim().isEmpty()) {
                            et_username.setText(username);
                            et_password.setText("");
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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


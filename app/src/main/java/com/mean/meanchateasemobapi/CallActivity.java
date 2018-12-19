package com.mean.meanchateasemobapi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMCallSession;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.mean.meanchateasemobapi.broadcast.CallReceiver;
import com.mean.meanchateasemobapi.view.CircleImageView;

import java.util.Timer;
import java.util.TimerTask;

public class CallActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = CallActivity.class.getSimpleName();
    private CircleImageView avadar;
    private TextView tvName;
    private TextView tvStatus;
    private ImageButton ibAnswer;
    private ImageButton ibHangup;

    private boolean isCallRequester;
    private String from;
    private String type = EMCallStateChangeListener.CallState.CONNECTING.toString();
    private EMCallStateChangeListener.CallState state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        from = getIntent().getStringExtra(CallReceiver.INTENT_EXTRA_FROM);
        type = getIntent().getStringExtra(CallReceiver.INTENT_EXTRA_TYPE);
        if(from==null||from.isEmpty()){
            errorQuit();
            return;
        }
        isCallRequester = from.equals(EMClient.getInstance().getCurrentUser());

        avadar =  findViewById(R.id.avadar);
        tvName = findViewById(R.id.tv_name);
        tvStatus =  findViewById(R.id.tv_status);
        ibHangup = findViewById(R.id.ib_hangup);
        ibHangup.setVisibility(View.INVISIBLE);
        ibHangup.setOnClickListener(this);
        ibAnswer = findViewById(R.id.ib_answer);
        ibAnswer.setOnClickListener(this);
        tvStatus.setText("连接中");
        if(isCallRequester){
            ibHangup.setVisibility(View.VISIBLE);
            ibAnswer.setVisibility(View.GONE); //主叫不显示接听按钮
        }else {
            ibHangup.setVisibility(View.VISIBLE);
            ibAnswer.setVisibility(View.VISIBLE);
        }

        tvName.setText(from);
        EMClient.getInstance().callManager().addCallStateChangeListener(new EMCallStateChangeListener() {
            @Override
            public void onCallStateChanged(CallState callState, CallError error) {
                Log.d(TAG, "onCallStateChanged: "+state+"->"+callState);
                state = callState;
                switch (callState) {
                    case CONNECTING: // 正在连接对方
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(isCallRequester){
                                    ibHangup.setVisibility(View.VISIBLE);
                                    ibAnswer.setVisibility(View.GONE); //主叫不显示接听按钮
                                }else {
                                    ibHangup.setVisibility(View.VISIBLE);
                                    ibAnswer.setVisibility(View.VISIBLE);
                                }
                                tvStatus.setText("连接中");
                            }
                        });
                        break;
                    case CONNECTED: // 双方已经建立连接
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvStatus.setText("连接成功，等待对方同意");
                            }
                        });
                        break;

                    case ACCEPTED: // 电话接通成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ibAnswer.setVisibility(View.GONE);
                                ibHangup.setVisibility(View.VISIBLE);
                                tvStatus.setText("通话中");
                                showToast("通话已建立");
                            }
                        });
                        break;
                    case DISCONNECTED: // 电话断了
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvStatus.setText("通话结束");
                                ibHangup.setVisibility(View.GONE);
                                ibAnswer.setVisibility(View.GONE);
                                showToast("通话结束");
                            }
                        });
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                finish();
                            }
                        },1000);

                        break;
                    case NETWORK_UNSTABLE: //网络不稳定
                        if(error == CallError.ERROR_NO_DATA){
                            //无通话数据
                        }else{

                        }
                        break;
                    case NETWORK_NORMAL: //网络恢复正常

                        break;
                    default:
                        break;
                }

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_hangup:
                if(state == EMCallStateChangeListener.CallState.ANSWERING
                        ||state == EMCallStateChangeListener.CallState.ACCEPTED){
                    try {
                        EMClient.getInstance().callManager().endCall();
                        ibHangup.setVisibility(View.GONE);
                        ibAnswer.setVisibility(View.GONE);
                        finish();
                    } catch (EMNoActiveCallException e) {
                        e.printStackTrace();
                    }
                }else if(state == EMCallStateChangeListener.CallState.CONNECTED) {
                    try {
                        EMClient.getInstance().callManager().rejectCall();
                        ibHangup.setVisibility(View.GONE);
                        ibAnswer.setVisibility(View.GONE);
                        finish();
                    } catch (EMNoActiveCallException e) {
                        e.printStackTrace();
                    }
                }else {
                    try {
                        EMClient.getInstance().callManager().endCall();
                    } catch (EMNoActiveCallException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
                break;
            case R.id.ib_answer:
                try {
                    EMClient.getInstance().callManager().answerCall();
                    ibAnswer.setVisibility(View.GONE);
                    ibHangup.setVisibility(View.VISIBLE);
                } catch (EMNoActiveCallException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    private void errorQuit(){
        //TODO EndActivity
        showToast("出现错误，会话关闭");
        finish();
    }

    public void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
}

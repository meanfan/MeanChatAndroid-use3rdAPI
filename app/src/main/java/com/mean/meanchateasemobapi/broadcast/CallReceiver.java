package com.mean.meanchateasemobapi.broadcast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.mean.meanchateasemobapi.CallActivity;

public class CallReceiver extends BroadcastReceiver {
    public static final String INTENT_EXTRA_FROM = "from";
    public static final String INTENT_EXTRA_TYPE = "type";

    @Override
    public void onReceive(Context context, final Intent intent) {
        // 拨打方username
        final String from = intent.getStringExtra(INTENT_EXTRA_FROM);
        // call type
        final String type = intent.getStringExtra(INTENT_EXTRA_TYPE);
        // 跳转到通话页面
        Intent callIntent = new Intent(context,CallActivity.class);
        callIntent.putExtra(INTENT_EXTRA_FROM,from);
        callIntent.putExtra(INTENT_EXTRA_TYPE,type);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(callIntent);


    }
}

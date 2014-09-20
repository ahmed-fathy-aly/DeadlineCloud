package asupt.deadlinecloud.web;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmIntentService will handle the intent.
    	Log.i("HERE","1");
    	ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
    	
        Log.i("HERE","2");
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        Log.i("HERE","3");
    }

}

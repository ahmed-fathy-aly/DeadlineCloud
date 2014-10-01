package asupt.deadlinecloud.data;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import asupt.deadlinecloud.web.AddOfflineDeadlinesService;
import asupt.deadlinecloud.web.WebMinion;

public class OfflineDeadlinesBroadcastReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(final Context context, Intent intent)
	{
		// start the add offline service
		Intent i= new Intent(context, AddOfflineDeadlinesService.class);
		context.startService(i);

		

	}

}

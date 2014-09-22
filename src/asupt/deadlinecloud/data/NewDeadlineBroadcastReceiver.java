package asupt.deadlinecloud.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import asupt.deadlinecloud.utils.MyUtils;

public class NewDeadlineBroadcastReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{

		// get the id
		if (intent.getExtras().containsKey(MyUtils.INTENT_DEADLINE_ID))
		{
			String id = intent.getStringExtra(MyUtils.INTENT_DEADLINE_ID);
			Log.e("Game", id);
			new DatabaseController(context).addToMyDeadlines(id);
		}
	}

}

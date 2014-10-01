package asupt.deadlinecloud.web;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import asupt.deadlinecloud.data.DatabaseController;
import asupt.deadlinecloud.data.Deadline;

public class AddOfflineDeadlinesService extends Service
{

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (!WebMinion.isConnected(this))
			return 0;
		
		Log.e("Game", "here we go");
		
		new AsyncTask<Boolean, Boolean, Boolean>()
		{

			protected Boolean doInBackground(Boolean... params)
			{
				// try to add all the deadlines waiting to be uploaded
				DatabaseController controller = new DatabaseController(AddOfflineDeadlinesService.this);
				ArrayList<Deadline> offlineDeadlines  = controller.getAllOfflineDeadliness();
				for (Deadline deadline : offlineDeadlines) 
				{
					WebMinion.postDeadline(deadline.getGroupId(), deadline.getPosterMail(), deadline);
					Log.e("Game", "Done");
					controller.deleteOfflineDeadline(deadline);
				}
				return true;
			}
		}.execute(true);
		return 1;
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

}

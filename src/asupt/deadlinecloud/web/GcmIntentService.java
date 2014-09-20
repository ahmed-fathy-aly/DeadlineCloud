package asupt.deadlinecloud.web;

import java.util.Random;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import asupt.deadlinecloud.activities.HomeActivity;
import asupt.deadlinecloud.activities.MyGroupsActivity;
import asupt.deadlinecloud.data.DatabaseController;
import asupt.deadlinecloud.data.Deadline;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService
{
	public static final int NOTIFICATION_ID = 1;
	public static final String TAG = "GCMM";

	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GcmIntentService()
	{
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.i("HERE", "10");
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty())
		{ // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
			{
				Log.i("HERE", "7");
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
			{
				Log.i("HERE", "8");
				sendNotification("Deleted messages on server: " + extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
			{
				Log.i("HERE", "9");
				Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
				Deadline d = new Deadline();
				d.SetFromWeb(extras);
				if (extras.getString("action") == "delete") {
					removeDeadlineFromDatabase(d);
				} else {
					notifyNewDeadline(d);
					addDeadlineToDatabase(d);
				}
				Log.i(TAG, "Received: " + extras.getString(("utc_time")));
				Log.i(TAG, "Received: " + extras.toString());
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void addDeadlineToDatabase(Deadline d)
	{
		// get reference to databaseController
		DatabaseController database = new DatabaseController(this);
		database.addDedaline(d);
	}
	
	private void removeDeadlineFromDatabase(Deadline d)
	{
		// TODO: Check if this is enough.
		// get reference to databaseController
		DatabaseController database = new DatabaseController(this);
		database.deleteDeadline(d);
	}

	private void notifyNewDeadline(Deadline deadline)
	{
		// get manager
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// intents
		Intent intent = new Intent(this, MyGroupsActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		// build notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(deadline.getTitle());
		builder.setContentText(deadline.getDescription());
		builder.setTicker("New deadline added to group " + deadline.getGroupName());
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		builder.setSmallIcon(asuspt.deadlinecloud.R.drawable.ic_menu_my_calendar);
		builder.setContentIntent(pIntent);
		builder.setAutoCancel(true);
		builder.addAction(asuspt.deadlinecloud.R.drawable.ic_checkmark_holo_light,
				"Add to Deadlines", pIntent);
		builder.addAction(asuspt.deadlinecloud.R.drawable.ic_checkmark_holo_light, "Remind me",
				pIntent);
		builder.addAction(asuspt.deadlinecloud.R.drawable.ic_checkmark_holo_light,
				"Add to Calendar", pIntent);

		// notify
		notificationManager.notify(new Random().nextInt(10000), builder.build());

	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg)
	{
		Log.i("HERE", "11");
		Log.i("HERE", msg);
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				HomeActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setContentTitle(
				"GCM Notification").setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

}
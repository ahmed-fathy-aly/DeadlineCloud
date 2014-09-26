package asupt.deadlinecloud.web;

import java.net.URI;
import java.util.Calendar;
import java.util.Random;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import asupt.deadlinecloud.activities.GroupDeadlineActivity;
import asupt.deadlinecloud.activities.HomeActivity;
import asupt.deadlinecloud.activities.MyGroupsActivity;
import asupt.deadlinecloud.data.DatabaseController;
import asupt.deadlinecloud.data.Deadline;
import asupt.deadlinecloud.utils.MyUtils;

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
				if (extras.getString("action").equals("delete"))
				{
					removeDeadlineFromDatabase(d);
				} else
				{
					addDeadlineToDatabase(d);
					notifyNewDeadline(d);
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
		// get reference to databaseController
		DatabaseController database = new DatabaseController(this);
		database.deleteDeadline(d);
	}

	private void notifyNewDeadline(Deadline deadline)
	{
		// check if we shouldn't make a notification
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean makeNotification = prefs.getBoolean("notifications_new_deadline", true);
		if (!makeNotification)
			return;

		// get ringtone and vibration preferences
		boolean vibration = prefs.getBoolean("notifications_new_deadlines_vibrate", false);
		String soundPref = prefs.getString("notifications_new_deadline_ringtone", "");
		Uri sound = Uri.parse(soundPref);

		// get manager
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// normal intent
		Intent intent = new Intent(this, GroupDeadlineActivity.class);
		intent.putExtra(MyUtils.INTENT_GROUP_ID, deadline.getGroupId());
		intent.putExtra(MyUtils.INTENT_GROUP_NAME, deadline.getGroupName());
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// mark deadline intent
		Intent addDeadlineIntenet = new Intent(MyUtils.NEW_DEADLINE_KEY);
		addDeadlineIntenet.putExtra(MyUtils.INTENT_DEADLINE_ID, deadline.getWebId());
		Log.e("Game", deadline.getTitle() + ":" + deadline.getWebId());
		PendingIntent addDeadlinePIntent = PendingIntent.getBroadcast(this, 0, addDeadlineIntenet,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// calendar intent
		Intent calendarIntent = new Intent(Intent.ACTION_EDIT);
		calendarIntent.setType("vnd.android.cursor.item/event");
		calendarIntent.putExtra("title", deadline.getTitle());
		calendarIntent.putExtra("description", deadline.getDescription());
		Calendar calendar = (Calendar) deadline.getCalendar().clone();
		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1900);
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
		calendarIntent.putExtra("beginTime", calendar.getTimeInMillis());
		calendarIntent.putExtra("endTime", calendar.getTimeInMillis() + 3600000);
		PendingIntent calendarPIntent = PendingIntent.getActivity(this, 0, calendarIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// build notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle("New Deadline at " + deadline.getGroupName());
		String contentText = deadline.getTitle() + " \n" + "in " + deadline.getRemainingDays()
				+ " days\n" + deadline.getDescription();
		builder.setContentText(contentText);
		builder.setTicker("New deadline added to group " + deadline.getGroupName());
		builder.setSound(sound);
		if (vibration)
		{
			builder.setDefaults(Notification.DEFAULT_VIBRATE);
			builder.setDefaults(Notification.DEFAULT_LIGHTS);
		}
		builder.setSmallIcon(asuspt.deadlinecloud.R.drawable.notifications_icon);
		builder.setContentIntent(pIntent);
		builder.setAutoCancel(true);
		builder.addAction(asuspt.deadlinecloud.R.drawable.ic_checkmark_holo_light, "Mark",
				addDeadlinePIntent);
		builder.addAction(asuspt.deadlinecloud.R.drawable.ic_menu_my_calendar, "Add to Calendar",
				calendarPIntent);

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
package asupt.deadlinecloud.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;
import asupt.deadlinecloud.adapters.RemindersListAdapter;
import asupt.deadlinecloud.adapters.RemindersListAdapter.RemindersListListener;
import asupt.deadlinecloud.data.DatabaseController;
import asupt.deadlinecloud.data.Reminder;
import asuspt.deadlinecloud.R;
import asuspt.deadlinecloud.R.layout;
import asuspt.deadlinecloud.R.menu;

public class RemindersActivity extends Activity implements RemindersListListener
 {

	private DatabaseController database;
	private ArrayList<Reminder> reminders;
	private ExpandableListView listView;
	private RemindersListAdapter listAdapter;

@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminders);
		setupActionBar();

		// set the list
		setRemindersList();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reminders, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setRemindersList()
	{
		// get the reminders from the database
		database = new DatabaseController(this);
		reminders = database.getAllReminders();

		// make the list stuff
		listView = (ExpandableListView) findViewById(R.id.expandableListReminders);
		listAdapter = new RemindersListAdapter(this, listView, this);
		listView.setAdapter(listAdapter);

	}

	public int getRemindersCount()
	{
		return reminders.size();
	}

	@Override
	public Reminder getReminder(int index)
	{
		return reminders.get(index);
	}

	@Override
	public void removeReminder(int index)
	{
		reminders.remove(index);
		listAdapter.notifyDataSetChanged();
	}
}

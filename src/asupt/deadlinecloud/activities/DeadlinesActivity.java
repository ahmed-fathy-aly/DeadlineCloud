package asupt.deadlinecloud.activities;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import asupt.deadlinecloud.adapters.DeadlineListAdapter;
import asupt.deadlinecloud.adapters.DeadlineListAdapter.DeadlineListListener;
import asupt.deadlinecloud.data.DatabaseController;
import asupt.deadlinecloud.data.Deadline;
import asupt.deadlinecloud.data.Group;
import asupt.deadlinecloud.data.Reminder;
import asupt.deadlinecloud.utils.MyUtils;
import asupt.deadlinecloud.web.WebMinion;
import asuspt.deadlinecloud.R;
import asuspt.deadlinecloud.R.layout;
import asuspt.deadlinecloud.R.menu;

public class DeadlinesActivity extends Activity implements DeadlineListListener
{

	private static DatabaseController database;
	private static ArrayList<Deadline> deadlines;
	private static DeadlineListAdapter listAdapter;
	private ExpandableListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deadlines);

		// Show the Up button in the action bar.
		setupActionBar();

		// set the deadline list stuff
		setDeadlinesList();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == MyUtils.ADD_DEADLINES_REQUEST_CODE)
		{
			if (resultCode == RESULT_OK)
			{
				refreshDeadline();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed()
	{
		NavUtils.navigateUpFromSameTask(this);
		super.onBackPressed();
	}

	private void setDeadlinesList()
	{
		// get the deadlines
		database = new DatabaseController(this);
		deadlines = database.getAllDeadlines();

		// manage the expandable list
		listView = (ExpandableListView) findViewById(R.id.expandableList);
		listAdapter = new DeadlineListAdapter(this, listView, this);
		listView.setAdapter(listAdapter);

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
		getMenuInflater().inflate(R.menu.deadlines, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.addDeadlineButton:
			Intent intent = new Intent(this, AddDeadlineActivity.class);
			startActivityForResult(intent, MyUtils.ADD_DEADLINES_REQUEST_CODE);
			return true;
		case R.id.refreshDeadlines:
			refreshDeadline();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void removeDeadline(int idx)
	{
		// remove the deadline
		database.deleteDeadline(deadlines.get(idx));
		this.deadlines.remove(idx);

		// collapse that deadline
		if (listAdapter.selectedIndex != -1)
		{
			listView.collapseGroup(listAdapter.selectedIndex);
			listAdapter.selectedIndex = -1;
		}

		// update view
		listAdapter.notifyDataSetChanged();

	}

	@Override
	public Deadline getDeadline(int idx)
	{
		return this.deadlines.get(idx);
	}

	public static void addDeadline(Deadline deadline)
	{
		// add it to the list
		deadlines.add(deadline);
		listAdapter.notifyDataSetChanged();

		// add it to the database
		database.addDedaline(deadline);
	}

	public int getDeadlinesCount()
	{
		return this.deadlines.size();
	}

	@Override
	public void addReminder(Reminder reminder)
	{
		Log.e("Game", reminder.getCalendar().toString());
		database.addReminder(reminder);
	}
	
	/**
	 * This method is called when a new deadline is added for a group you're following.
	 */
	public static void newDeadlineReceived(Deadline d) {
		//TODO: Fathy bro, do your magic.
		Log.i("Deadline",String.valueOf(d.getCalendar().getTime()));
	}
	
	private void refreshDeadline()
	{
		// get the deadlines from the server and add them
		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;

			@Override
			protected void onPreExecute()
			{
				progressDialog = ProgressDialog.show(DeadlinesActivity.this, "Downloading",
						"Downloading deadlines...");
			}

			@Override
			protected Boolean doInBackground(Boolean... params)
			{
				// Add the local deadlines
				ArrayList<Deadline> newDeadlines = new ArrayList<Deadline>();
				for (Deadline deadline : database.getAllDeadlines())
					if (deadline.getGroupName().equals(Deadline.localString))
						newDeadlines.add(deadline);

				// add all other deadlines
				for (Group group : database.getAllGroups())
				{
					ArrayList<Deadline> groupDeadlines = WebMinion.getAllDeadlines(group.getId());
					for (Deadline deadline : groupDeadlines)
						newDeadlines.add(deadline);
				}

				// remove all the deadlines in the database
				for (Deadline deadline : database.getAllDeadlines())
					database.deleteDeadline(deadline);

				// add the new deadlines
				deadlines.clear();
				for (Deadline deadline : newDeadlines)
				{
					database.addDedaline(deadline);
					deadlines.add(deadline);
				}

				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				// dissmiss
				progressDialog.dismiss();
				listAdapter.notifyDataSetChanged();
			}

		}.execute(true);
	}
}

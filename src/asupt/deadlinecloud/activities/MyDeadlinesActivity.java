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
import android.content.Context;
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

public class MyDeadlinesActivity extends Activity implements DeadlineListListener
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
		deadlines = database.getMyDeadlines();

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
			intent.putExtra(MyUtils.INTENT_GROUP_ID, -1);
			intent.putExtra(MyUtils.INTENT_GROUP_NAME, Deadline.localString);
			startActivity(intent);
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




}

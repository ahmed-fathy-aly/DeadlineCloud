package asupt.deadlinecloud.activities;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import asupt.deadlinecloud.adapters.AllGroupsListAdapter;
import asupt.deadlinecloud.adapters.AllGroupsListAdapter.AllGroupsListListener;
import asupt.deadlinecloud.adapters.MyGroupListAdapter;
import asupt.deadlinecloud.adapters.MyGroupListAdapter.MyGroupListListener;
import asupt.deadlinecloud.data.DatabaseController;
import asupt.deadlinecloud.data.Group;
import asupt.deadlinecloud.utils.MyUtils;
import asupt.deadlinecloud.web.WebMinion;
import asuspt.deadlinecloud.R;

public class SyncActivity extends Activity implements MyGroupListListener, AllGroupsListListener
{
	/* stuff about the other groups */
	private ArrayList<Group> myGroups;
	private MyGroupListAdapter myGroupsListAdapter;
	private ListView myGroupsListView;

	/* stuff about synced groups */
	private DatabaseController database;
	private ArrayList<Group> allgroups;
	private AllGroupsListAdapter allGroupsListAdapter;
	private ListView allGroupsListView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sync);
		setupActionBar();

		// set lists stuff
		setMyGroupsList();
		setAllGroupsList();

	}

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
		getMenuInflater().inflate(R.menu.sync, menu);
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
		case R.id.addNewGroupButton:
			Intent intent = new Intent(this, AddGroupActivity.class);
			startActivity(intent);
		}
		
		return super.onOptionsItemSelected(item);
	}

	/* my groups stuff */
	private void setMyGroupsList()
	{
		// get the groups
		database = new DatabaseController(this);
		myGroups = database.getAllGroups();

		// set the adapter
		myGroupsListAdapter = new MyGroupListAdapter(this, this);
		myGroupsListView = (ListView) findViewById(R.id.listViewMyGroups);
		myGroupsListView.setAdapter(myGroupsListAdapter);
	}

	@Override
	public int getGroupCount()
	{
		return myGroups.size();
	}

	@Override
	public Group getGroup(int index)
	{
		return myGroups.get(index);
	}

	/* all groups stuff */
	private void setAllGroupsList()
	{
		// a thread that loads all the groups from the server

		new AsyncTask<Boolean, Boolean, Boolean>()
		{

			ProgressDialog progressDialog;

			@Override
			protected void onPreExecute()
			{
				progressDialog = ProgressDialog
						.show(SyncActivity.this, "Loading", "Loading groups");
			}

			@Override
			protected Boolean doInBackground(Boolean... params)
			{
				allgroups = WebMinion.getAllGroups();

				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				progressDialog.dismiss();
				
				// set the adapter
				allGroupsListAdapter = new AllGroupsListAdapter(SyncActivity.this,
						SyncActivity.this);
				allGroupsListView = (ListView) findViewById(R.id.listViewAllGroups);
				allGroupsListView.setAdapter(allGroupsListAdapter);
			}
		}.execute(true);

	}

	@Override
	public int getAllGroupsCount()
	{
		return allgroups.size();
	}

	@Override
	public Group getUnsyncedGroup(int index)
	{
		return allgroups.get(index);
	}

	/* Syncing stuff */
	@Override
	public void syncGroup(final int index)
	{
		// make a thread that asks the server to sync with that group
		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;

			@Override
			protected void onPreExecute()
			{
				// make a progress dialoh
				progressDialog = ProgressDialog.show(SyncActivity.this, "Syncing", "Syncing...");
			}

			@Override
			protected Boolean doInBackground(Boolean... params)
			{

				// ask the web minion to subscribe
				Group group = allgroups.get(index);
				WebMinion.subscribe(group.getId(), MyUtils.getUserId(SyncActivity.this));

				// add that group to my groups
				myGroups.add(group);
				database.addGroup(group);

				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				// notify both lists
				myGroupsListAdapter.notifyDataSetChanged();
				allGroupsListAdapter.notifyDataSetChanged();
				myGroupsListView.setSelection(myGroups.size() - 1);

				// dismiss the progress dialog
				progressDialog.dismiss();
			}

		}.execute(true);

	}

	@Override
	public void unSync(int index)
	{
		database.deleteGroup(myGroups.get(index));
		myGroups.remove(index);
		myGroupsListAdapter.notifyDataSetChanged();
		allGroupsListAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean isSynced(Group group)
	{
		// search for the id
		for (Group syncedGroup : myGroups)
		{
			if (syncedGroup.getId().equals(group.getId()))
				return true;
		}
		return false;
	}

}

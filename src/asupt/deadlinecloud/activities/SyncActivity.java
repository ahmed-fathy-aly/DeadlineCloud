package asupt.deadlinecloud.activities;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;
import asupt.deadlinecloud.adapters.AllGroupsListAdapter;
import asupt.deadlinecloud.adapters.AllGroupsListAdapter.AllGroupsListListener;
import asupt.deadlinecloud.data.DatabaseController;
import asupt.deadlinecloud.data.Group;
import asupt.deadlinecloud.utils.MyUtils;
import asupt.deadlinecloud.web.WebMinion;
import asuspt.deadlinecloud.R;

public class SyncActivity extends Activity implements AllGroupsListListener
{
	/* stuff about the other groups */
	private DatabaseController database;
	private ArrayList<Group> myGroups;

	/* stuff about all groups groups */
	private ArrayList<Group> allgroups;
	private AllGroupsListAdapter allGroupsListAdapter;
	private ExpandableListView allGroupsListView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sync);
		setupActionBar();

		// set lists stuff
		setMyGroupsList();
		setAllGroupsList();

		// set search stuff
		setSearchableTags();

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
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	public void onBackPressed()
	{
		NavUtils.navigateUpFromSameTask(this);
		super.onBackPressed();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == MyUtils.ADD_GROUP_REQUEST_CODE)
		{
			if (resultCode == RESULT_OK)
			{
				setAllGroupsList();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/* the search edit texts */
	private void setSearchableTags()
	{

		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;
			ArrayList<String> graduationYearHints;
			ArrayList<String> departmentHints;
			ArrayList<String> tagsHints;
			String message = "";

			protected void onPreExecute()
			{
				progressDialog = ProgressDialog.show(SyncActivity.this, "Loading", "Loading...");

			}

			protected Boolean doInBackground(Boolean... params)
			{
				// check connection
				if (WebMinion.isConnected(SyncActivity.this) == false)
				{
					message = "No Connection";
					return false;
				}

				// load graduation year hints
				graduationYearHints = new ArrayList<String>();
				ArrayList<String> serverGraduationYear = WebMinion.getGraduationYears();
				for (String tag : serverGraduationYear)
					graduationYearHints.add(tag);

				// load departmentHint
				departmentHints = new ArrayList<String>();
				ArrayList<String> serverdepartments = WebMinion.getDeaprtments();
				for (String tag : serverdepartments)
					departmentHints.add(tag);

				// load tags
				tagsHints = new ArrayList<String>();
				ArrayList<String> serverTags = WebMinion.getTags();
				for (String tag : serverTags)
					tagsHints.add(tag);

				return true;
			}

			protected void onPostExecute(Boolean result)
			{
				// if failed
				if (result == false)
				{
					Toast.makeText(SyncActivity.this, message, Toast.LENGTH_SHORT).show();
					graduationYearHints = new ArrayList<String>();
					departmentHints = new ArrayList<String>();
					tagsHints = new ArrayList<String>();
				}

				// set graduation year edit text
				ArrayAdapter<String> graduationYearAdapter = new ArrayAdapter<String>(
						SyncActivity.this, android.R.layout.simple_dropdown_item_1line,
						graduationYearHints);
				AutoCompleteTextView graduationYear = (AutoCompleteTextView) SyncActivity.this
						.findViewById(R.id.autoCompleteTextViewGraduationYeaSearchr);
				graduationYear.setAdapter(graduationYearAdapter);

				// set departments
				ArrayAdapter<String> departmentAdapter = new ArrayAdapter<String>(
						SyncActivity.this, android.R.layout.simple_dropdown_item_1line,
						departmentHints);
				AutoCompleteTextView department = (AutoCompleteTextView) SyncActivity.this
						.findViewById(R.id.autoCompleteTextViewDepartmentSearch);
				department.setAdapter(departmentAdapter);

				// set tags
				ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(SyncActivity.this,
						android.R.layout.simple_dropdown_item_1line, tagsHints);
				AutoCompleteTextView tags = (AutoCompleteTextView) SyncActivity.this
						.findViewById(R.id.autoCompleteTextViewTagSearch);
				tags.setAdapter(tagsAdapter);

				progressDialog.dismiss();

			}

		}.execute();

	}

	/* my groups stuff */
	private void setMyGroupsList()
	{
		// get the groups
		database = new DatabaseController(this);
		myGroups = database.getAllGroups();
	}

	/* all groups stuff */
	private void setAllGroupsList()
	{

		// at first the list is empty
		allgroups = new ArrayList<Group>();

		// set the adapter
		allGroupsListAdapter = new AllGroupsListAdapter(SyncActivity.this, SyncActivity.this);
		allGroupsListView = (ExpandableListView) findViewById(R.id.listViewAllGroups);
		allGroupsListView.setAdapter(allGroupsListAdapter);
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

	public void onSearchButtonClicked(View v)
	{
		// get the groups that match those tags from the server
		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;
			String message = "";

			@Override
			protected void onPreExecute()
			{
				progressDialog = ProgressDialog.show(SyncActivity.this, "Searching",
						"Searching for groups");
			}

			@Override
			protected Boolean doInBackground(Boolean... params)
			{
				// check connection
				if (WebMinion.isConnected(SyncActivity.this) == false)
				{
					message = "No Connection";
					return false;
				}
				// reference to the edit texts
				AutoCompleteTextView graduationYearEditText = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewGraduationYeaSearchr);
				AutoCompleteTextView departmentEditText = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewDepartmentSearch);
				AutoCompleteTextView tagEditText = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewTagSearch);

				// get strings
				String graduationYear = graduationYearEditText.getText().toString();
				if (graduationYear.equals(""))
					graduationYear = MyUtils.TAG_ANY;
				String department = departmentEditText.getText().toString();
				if (department.equals(""))
					department = MyUtils.TAG_ANY;
				String tag = tagEditText.getText().toString();
				if (tag.equals(""))
					tag = MyUtils.TAG_ANY;

				// get groups
				allgroups = WebMinion.getAllGroups(graduationYear, department, tag);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				if (result == false)
				{
					Toast.makeText(SyncActivity.this, message, Toast.LENGTH_SHORT).show();
					allgroups = new ArrayList<Group>();
				}
				progressDialog.dismiss();
				allGroupsListAdapter.notifyDataSetChanged();
			}
		}.execute(true);
	}

	/* Syncing stuff */
	@Override
	public void syncGroup(final int index)
	{
		// make a thread that asks the server to sync with that group
		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;
			String message;

			@Override
			protected void onPreExecute()
			{
				// make a progress dialoh
				progressDialog = ProgressDialog.show(SyncActivity.this, "Syncing", "Syncing...");
			}

			@Override
			protected Boolean doInBackground(Boolean... params)
			{
				// check connection
				if (WebMinion.isConnected(SyncActivity.this) == false)
				{
					message = "No Connection";
					return false;
				}
				// ask the web minion to subscribe
				Group group = allgroups.get(index);
				String gmailId = WebMinion.getGmailId(SyncActivity.this);
				WebMinion.subscribe(group.getId(), gmailId);

				// add that group to my groups
				myGroups.add(group);
				database.addGroup(group);

				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				// if failed
				if (result == false)
				{
					Toast.makeText(SyncActivity.this, message, Toast.LENGTH_SHORT).show();
				}
				// notify both lists
				allGroupsListAdapter.notifyDataSetChanged();

				// dismiss the progress dialog
				progressDialog.dismiss();
			}

		}.execute(true);

	}

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

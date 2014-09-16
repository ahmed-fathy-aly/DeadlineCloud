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
import android.widget.ListView;
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
	private ListView allGroupsListView;

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
		case R.id.addNewGroupButton:
			onAddNewGroupButtonClicked();
		}

		return super.onOptionsItemSelected(item);
	}

	private void onAddNewGroupButtonClicked()
	{
		// make a dialog from which the user chooses his account
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose you gmail-account");

		// list of accounts
		final ArrayList<String> gUsernameList = MyUtils.getGmailAccounts(this);
		ListView lv = new ListView(this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, gUsernameList);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				// when one of them clicked open the admin tools activity
				String gmailAddress = gUsernameList.get(position);
				Intent intent = new Intent(SyncActivity.this, AddGroupActivity.class);
				intent.putExtra(MyUtils.INTENT_GMAIL_ADDRESS, gmailAddress);
				startActivityForResult(intent, MyUtils.ADD_GROUP_REQUEST_CODE);
			}
		});
		builder.setView(lv);

		// cancel button
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				dialog.dismiss();
			}
		});
		final Dialog dialog = builder.create();
		dialog.show();

	}

	@Override
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

			protected void onPreExecute()
			{
				progressDialog = ProgressDialog.show(SyncActivity.this, "Loading", "Loading...");
			}

			protected Boolean doInBackground(Boolean... params)
			{
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
		allGroupsListView = (ListView) findViewById(R.id.listViewAllGroups);
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

			@Override
			protected void onPreExecute()
			{
				progressDialog = ProgressDialog.show(SyncActivity.this, "Searching",
						"Searching for groups");
			}

			@Override
			protected Boolean doInBackground(Boolean... params)
			{
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

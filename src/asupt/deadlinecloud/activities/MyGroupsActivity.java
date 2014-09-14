package asupt.deadlinecloud.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;
import asupt.deadlinecloud.adapters.AllGroupsListAdapter;
import asupt.deadlinecloud.adapters.MyGroupListAdapter;
import asupt.deadlinecloud.adapters.MyGroupListAdapter.MyGroupListListener;
import asupt.deadlinecloud.data.DatabaseController;
import asupt.deadlinecloud.data.Group;
import asuspt.deadlinecloud.R;
import asuspt.deadlinecloud.R.layout;
import asuspt.deadlinecloud.R.menu;

public class MyGroupsActivity extends Activity implements MyGroupListListener
{
	/* stuff about my groups */
	private DatabaseController database;
	private ArrayList<Group> myGroups;
	private MyGroupListAdapter myGroupsListAdapter;
	private ListView myGroupsListView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_groups);

		setupActionBar();
		setMyGroupsList();
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
		getMenuInflater().inflate(R.menu.my_groups, menu);
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

	@Override
	public void onBackPressed()
	{
		NavUtils.navigateUpFromSameTask(this);
		super.onBackPressed();
	}

	private void setMyGroupsList()
	{
		// get the groups
		database = new DatabaseController(this);
		myGroups = database.getAllGroups();

		// set the adapter
		myGroupsListAdapter = new MyGroupListAdapter(this, this);
		myGroupsListView = (ListView) findViewById(R.id.listViewMyGroupslist);
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

	@Override
	public void unSync(int index)
	{
		database.deleteGroup(myGroups.get(index));
		myGroups.remove(index);
		myGroupsListAdapter.notifyDataSetChanged();
	}
}

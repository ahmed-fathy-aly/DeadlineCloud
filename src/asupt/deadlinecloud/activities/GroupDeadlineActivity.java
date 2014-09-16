package asupt.deadlinecloud.activities;

import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import asupt.deadlinecloud.adapters.DeadlineListAdapter;
import asupt.deadlinecloud.adapters.DeadlineListAdapter.DeadlineListListener;
import asupt.deadlinecloud.data.DatabaseController;
import asupt.deadlinecloud.data.Deadline;
import asupt.deadlinecloud.data.Group;
import asupt.deadlinecloud.data.Reminder;
import asupt.deadlinecloud.utils.MyUtils;
import asupt.deadlinecloud.web.WebMinion;
import asuspt.deadlinecloud.R;

public class GroupDeadlineActivity extends Activity implements DeadlineListListener
{

	/* stuff about the group */
	Group group;

	/* stuff about the deadlines */
	private static DatabaseController database;
	private static ArrayList<Deadline> deadlines;
	private static DeadlineListAdapter listAdapter;
	private ExpandableListView listView;

	private String groupId;
	private String groupName;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		setContentView(R.layout.activity_group_deadline);
		setupActionBar();

		// get the group from the intent
		groupId = getIntent().getExtras().getString(MyUtils.INTENT_GROUP_ID);
		groupName = getIntent().getExtras().getString(MyUtils.INTENT_GROUP_NAME);
		setTitle(groupName);

		setDeadlinesList();
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
		getMenuInflater().inflate(R.menu.group_deadline, menu);
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
		case R.id.addGroupDeadlineButton:
			onAddDeadlineButtonClicked();
			return true;
		case R.id.refreshGroupDeadlines:
			refreshDeadlines();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == MyUtils.ADD_DEADLINES_REQUEST_CODE)
		{
			if (resultCode == RESULT_OK)
			{
				refreshDeadlines();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.expandableListMyGroupDeadlinesList)
		{
			Log.e("Game", menuInfo.getClass() + " ");
			ExpandableListView.ExpandableListContextMenuInfo acmi = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
			menu.setHeaderTitle(deadlines.get(
					ExpandableListView.getPackedPositionGroup(acmi.packedPosition)).getTitle());
			menu.add(0, v.getId(), 0, "Add to my deadlines");
			menu.add(0, v.getId(), 0, "Add to Calendar");
			menu.add(0, v.getId(), 0, "Add Reminder");

		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		ExpandableListView.ExpandableListContextMenuInfo acmi = (ExpandableListView.ExpandableListContextMenuInfo) item
				.getMenuInfo();
		int idx = ExpandableListView.getPackedPositionGroup(acmi.packedPosition);

		if (item.getTitle().equals("Add to Calendar"))
		{
			addCalendar(deadlines.get(idx));
		} else if (item.getTitle().equals("Add Reminder"))
		{
			addReminder(deadlines.get(idx));
		}
		return super.onContextItemSelected(item);
	}

	/* stuff about the deadline list */
	private void setDeadlinesList()
	{
		// get the deadlines
		database = new DatabaseController(this);
		deadlines = database.getGroupDeadlines(groupName);

		// manage the expandable list
		listView = (ExpandableListView) findViewById(R.id.expandableListMyGroupDeadlinesList);
		listAdapter = new DeadlineListAdapter(this, listView, this);
		listView.setAdapter(listAdapter);

		// register for context menu
		registerForContextMenu(listView);
	}

	@Override
	public void removeDeadline(int idx)
	{
		deadlines.remove(idx);

	}

	public Deadline getDeadline(int idx)
	{
		return deadlines.get(idx);
	}

	public int getDeadlinesCount()
	{
		return deadlines.size();
	}

	public void addReminder(Reminder reminder)
	{

	}

	private void refreshDeadlines()
	{
		// get the deadlines from the server and add them
		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;

			@Override
			protected void onPreExecute()
			{
				progressDialog = ProgressDialog.show(GroupDeadlineActivity.this, "Downloading",
						"Downloading deadlines...");
			}

			@Override
			protected Boolean doInBackground(Boolean... params)
			{
				// get the deadlines for that group
				ArrayList<Deadline> groupDeadlines = WebMinion.getAllDeadlines(groupId);

				// remove all the deadlines in the database
				for (Deadline deadline : database.getAllDeadlines())
					if (deadline.getGroupName().equals(groupName))
						database.deleteDeadline(deadline);

				// add the new deadlines
				deadlines.clear();
				for (Deadline deadline : groupDeadlines)
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

	private void onAddDeadlineButtonClicked()
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
				Intent intent = new Intent(GroupDeadlineActivity.this, AddDeadlineActivity.class);
				intent.putExtra(MyUtils.INTENT_GROUP_ID, groupId);
				intent.putExtra(MyUtils.INTENT_GROUP_NAME, groupName);
				intent.putExtra(MyUtils.INTENT_GMAIL_ADDRESS, gUsernameList.get(position));
				startActivityForResult(intent, MyUtils.ADD_DEADLINES_REQUEST_CODE);
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

	/* context menu options */
	private void addCalendar(Deadline deadline)
	{
		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra("title", deadline.getTitle());
		intent.putExtra("description", deadline.getDescription());
		intent.putExtra("beginTime", deadline.getCalendar().getTimeInMillis());
		intent.putExtra("endTime", deadline.getCalendar().getTimeInMillis() + 3600000);
		startActivity(intent);
	}

	private void addReminder(Deadline deadline)
	{

	}

}

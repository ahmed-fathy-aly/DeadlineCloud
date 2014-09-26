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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;
import asupt.deadlinecloud.adapters.MyGroupGridAdapter;
import asupt.deadlinecloud.adapters.MyGroupGridAdapter.MyGroupListListener;
import asupt.deadlinecloud.data.DatabaseController;
import asupt.deadlinecloud.data.Group;
import asupt.deadlinecloud.utils.MyUtils;
import asupt.deadlinecloud.web.WebMinion;
import asuspt.deadlinecloud.R;

public class MyGroupsActivity extends Activity implements MyGroupListListener
{
	/* stuff about my groups */
	private DatabaseController database;
	private ArrayList<Group> myGroups;
	private MyGroupGridAdapter myGroupsGridAdapter;
	private GridView myGroupsGridView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_groups);

		setupActionBar();
		setMyGroupsList();
	}
	public void onBackPressed()
	{
		NavUtils.navigateUpFromSameTask(this);
		super.onBackPressed();
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
		case R.id.addNewGroupButton:
			onAddNewGroupButtonClicked();
			return true;
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.gridViewMyGroups)
		{
			AdapterView.AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(myGroups.get(acmi.position).getName());
			menu.add(0, v.getId(), 0, "UnSync");
			menu.add(0, v.getId(), 0, "Admin tools");

		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		final AdapterView.AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();

		if (item.getTitle().equals("UnSync"))
		{
			unSync(acmi.position);
		} else if (item.getTitle().equals("Admin tools"))
		{
			openAdminsTools(myGroups.get(acmi.position));
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void openAdminsTools(final Group group)
	{
		// make a dialog from which the user chooses his account
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose you gmail-account");

		// cancel button
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				dialog.dismiss();
			}
		});

		// list of accounts
		final ArrayList<String> gUsernameList = MyUtils.getGmailAccounts(this);
		ListView lv = new ListView(this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, gUsernameList);
		lv.setAdapter(adapter);
		builder.setView(lv);
		final Dialog dialog = builder.create();

		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
			{
				// when one of them clicked open the admin tools activity
				final String gmailAddress = gUsernameList.get(position);
				final String groupId = group.getId();

				// check this person has the right
				new AsyncTask<Boolean, Boolean, Boolean>()
				{
					ProgressDialog progressDialog;
					String message;

					@Override
					protected void onPreExecute()
					{
						progressDialog = ProgressDialog.show(MyGroupsActivity.this,
								"Checking If you're an admin",
								"web minion searching through admins' mail ");
					}

					@Override
					protected Boolean doInBackground(Boolean... params)
					{
						// check connection
						if (WebMinion.isConnected(MyGroupsActivity.this) == false)
						{
							message = "No Connection";
							return false;
						}

						// check adminstriship
						if (WebMinion.canManageGroup(groupId, gUsernameList.get(position)))
						{
							return true;
						} else
						{
							message = "Only admins can add deadlines";
							return false;
						}
					}

					@Override
					protected void onPostExecute(Boolean result)
					{
						if (result == false)
						{
							Toast.makeText(MyGroupsActivity.this, message, Toast.LENGTH_SHORT)
									.show();
						} else
						{
							// start the add deadline activity
							Intent intent = new Intent(MyGroupsActivity.this,
									AdminToolsActivity.class);
							intent.putExtra(MyUtils.INTENT_GMAIL_ADDRESS, gmailAddress);
							intent.putExtra(MyUtils.INTENT_GROUP_ID, groupId);
							startActivity(intent);
							dialog.dismiss();

						}
						progressDialog.dismiss();
					}

				}.execute(true);

			}
		});

		dialog.show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == RESULT_OK && requestCode == MyUtils.ADD_GROUP_REQUEST_CODE)
		{
			myGroups = database.getAllGroups();
			myGroupsGridAdapter.notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/* groups stuff */
	private void setMyGroupsList()
	{
		// get the groups
		database = new DatabaseController(this);
		myGroups = database.getAllGroups();

		// set the adapter
		myGroupsGridAdapter = new MyGroupGridAdapter(this, this);
		myGroupsGridView = (GridView) findViewById(R.id.gridViewMyGroups);
		myGroupsGridView.setAdapter(myGroupsGridAdapter);

		// register for context menu
		registerForContextMenu(myGroupsGridView);

		// onClick listener
		myGroupsGridView.setOnItemClickListener(new OnItemClickListener()
		{

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				// open the group deadline activity
				Intent intent = new Intent(MyGroupsActivity.this, GroupDeadlineActivity.class);
				intent.putExtra(MyUtils.INTENT_GROUP_ID, myGroups.get(arg2).getId());
				intent.putExtra(MyUtils.INTENT_GROUP_NAME, myGroups.get(arg2).getName());
				startActivity(intent);
			}
		});
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
		myGroupsGridAdapter.notifyDataSetChanged();
	}

	/* option items */
	private void onAddNewGroupButtonClicked()
	{
		// make a dialog from which the user chooses his account
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose you gmail-account");

		// cancel button
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				dialog.dismiss();
			}
		});

		// list of accounts
		final ArrayList<String> gUsernameList = MyUtils.getGmailAccounts(this);
		ListView lv = new ListView(this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, gUsernameList);
		lv.setAdapter(adapter);
		builder.setView(lv);

		// on click
		final Dialog dialog = builder.create();
		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				// when one of them clicked open the admin tools activity
				String gmailAddress = gUsernameList.get(position);
				Intent intent = new Intent(MyGroupsActivity.this, AddGroupActivity.class);
				intent.putExtra(MyUtils.INTENT_GMAIL_ADDRESS, gmailAddress);
				startActivityForResult(intent, MyUtils.ADD_GROUP_REQUEST_CODE);
				dialog.dismiss();
			}
		});

		dialog.show();

	}

}

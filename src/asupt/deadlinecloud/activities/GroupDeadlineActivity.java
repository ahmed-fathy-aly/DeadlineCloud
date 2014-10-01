package asupt.deadlinecloud.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.R.color;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Shader.TileMode;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;
import asupt.deadlinecloud.adapters.DeadlineListAdapter;
import asupt.deadlinecloud.adapters.DeadlineListAdapter.DeadlineListListener;
import asupt.deadlinecloud.data.DatabaseController;
import asupt.deadlinecloud.data.Deadline;
import asupt.deadlinecloud.data.Group;
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

	public void onBackPressed()
	{
		NavUtils.navigateUpFromSameTask(this);
		super.onBackPressed();
	}

	@Override
	protected void onResume()
	{
		if (deadlines != null)
			sortDeadlines();
		super.onResume();
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

	/* stuff about the deadline list */
	private void setDeadlinesList()
	{
		// get the deadlines
		database = new DatabaseController(this);
		deadlines = database.getGroupDeadlines(groupName);
		sortDeadlines();

		// manage the expandable list
		listView = (ExpandableListView) findViewById(R.id.expandableListMyGroupDeadlinesList);
		listAdapter = new DeadlineListAdapter(this, listView, this);
		listView.setAdapter(listAdapter);

		// register for context menu
		registerForContextMenu(listView);
	}

	private void sortDeadlines()
	{
		final String sortCriteria = PreferenceManager.getDefaultSharedPreferences(this).getString(
				"sort_criteria", "1");
		Collections.sort(deadlines, new Comparator<Deadline>()
		{
			public int compare(Deadline d1, Deadline d2)
			{
				int cmp1 = d2.getWebId().compareTo(d1.getWebId());
				int cmp2 = d1.getCalendar().compareTo(d2.getCalendar());
				Integer d1w = d1.getWebPriority();
				Integer d2w = d2.getWebPriority();
				int cmp3 = d2w.compareTo(d1w);

				if (sortCriteria.equals("1"))
				{
					return cmp1;
				} else if (sortCriteria.equals("2"))
				{
					if (cmp2 != 0)
						return cmp2;
					else
						return cmp3;
				} else
				{
					if (cmp3 != 0)
						return cmp3;
					else
						return cmp2;
				}
			}
		});

	}

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

	/* context menu options */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.expandableListMyGroupDeadlinesList)
		{
			ExpandableListView.ExpandableListContextMenuInfo acmi = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
			int idx = ExpandableListView.getPackedPositionGroup(acmi.packedPosition);
			menu.setHeaderTitle(deadlines.get(
					ExpandableListView.getPackedPositionGroup(acmi.packedPosition)).getTitle());
			if (deadlines.get(idx).getInMyDeadlines() == 0)
				menu.add(0, v.getId(), 0, "Add to my deadlines");
			menu.add(0, v.getId(), 0, "Add to Calendar");
			menu.add(0, v.getId(), 0, "Share Image of Deadline");

			menu.add(0, v.getId(), 0, "Delete");

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
		} else if (item.getTitle().equals("Delete"))
		{
			deleteDeadline(deadlines.get(idx));
		} else if (item.getTitle().equals("Add to my deadlines"))
		{
			addToMyDeadlines(deadlines.get(idx));
		} else if (item.getTitle().equals("Share Image of Deadline"))
		{
			shareDeadlineImage(idx);
		}

		return super.onContextItemSelected(item);
	}

	private void shareDeadlineImage(int idx)
	{
		// make bitmap of the view
		View view = listView.getChildAt(idx);
		// View view = listView;

		Bitmap viewBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(viewBitmap);
		view.draw(c);

		// paint background white
		int leftMargin = 10;
		int upMargin = 20;
		int totalWidth = view.getWidth() + leftMargin * 2;
		int totalHeight = view.getHeight() + upMargin * 2;
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth() + leftMargin * 2, view.getHeight()
				+ upMargin * 2, Bitmap.Config.ARGB_8888);
		c = new Canvas(bitmap);

		RadialGradient grad = new RadialGradient(totalWidth / 2, totalHeight / 2, Math.min(
				totalWidth / 2, totalHeight / 2), 0xffECF0F0, 0xffCCF0F0, TileMode.CLAMP);
		Paint paint = new Paint();
		paint.setShader(grad);
		c.drawRect(0, 0, c.getWidth(), c.getHeight(), paint);
		c.drawBitmap(viewBitmap, leftMargin, upMargin, new Paint());

		// get uri
		OutputStream output;

		// Find the SD Card path
		File filepath = Environment.getExternalStorageDirectory();

		// Create a new folder AndroidBegin in SD Card
		File dir = new File(filepath.getAbsolutePath() + "/SharedImagel/");
		dir.mkdirs();

		// Create a name for the saved image
		File file = new File(dir, "SharedDeadline.png");

		try
		{

			// Share Intent
			Intent share = new Intent(Intent.ACTION_SEND);

			// Type of file to share
			share.setType("image/jpeg");

			output = new FileOutputStream(file);

			// Compress into png format image from 0% - 100%
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
			output.flush();
			output.close();

			// Locate the image to Share
			Uri uri = Uri.fromFile(file);

			// Pass the image into an Intnet
			share.putExtra(Intent.EXTRA_STREAM, uri);

			// Show the social share chooser list
			startActivity(Intent.createChooser(share, "Share Deadline Image"));

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void addCalendar(Deadline deadline)
	{
		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra("title", deadline.getTitle());
		intent.putExtra("description", deadline.getDescription());
		Calendar calendar = (Calendar) deadline.getCalendar().clone();
		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1900);
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
		intent.putExtra("beginTime", calendar.getTimeInMillis());
		intent.putExtra("endTime", calendar.getTimeInMillis() + 3600000);
		startActivity(intent);
	}

	private void deleteDeadline(final Deadline deadline)
	{
		// take the dude's mail and ask the server to delete
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
				// when one of them clicked delete that deadline
				String gmailAddress = gUsernameList.get(position);
				confirmDeleteDeadline(deadline, gmailAddress);
				dialog.dismiss();
			}

		});

		dialog.show();

	}

	void confirmDeleteDeadline(final Deadline deadline, final String gmailAddress)
	{
		// make a thread that asks the web minion to delete the deadline
		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;
			String message = "";

			@Override
			protected void onPreExecute()
			{
				progressDialog = ProgressDialog.show(GroupDeadlineActivity.this, "Deleting",
						"Deleting deadline...");
			}

			@Override
			protected Boolean doInBackground(Boolean... params)
			{
				// check connection
				if (WebMinion.isConnected(GroupDeadlineActivity.this) == false)
				{
					message = "No Connection";
					return false;
				}

				// check rights
				if (!WebMinion.canManageGroup(groupId, gmailAddress))
				{
					message = "Only Admins can delete Deadlines";
					return false;
				}

				WebMinion.deleteDeadline(deadline, gmailAddress, groupId, groupName);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				progressDialog.dismiss();
				if (result == false)
					Toast.makeText(GroupDeadlineActivity.this, message, Toast.LENGTH_SHORT).show();
				else
					refreshDeadlines();
			}

		}.execute(true);
	}

	private void addToMyDeadlines(Deadline deadline)
	{
		database.addToMyDeadlines(deadline);
		deadline.setInMyDeadlines(1);
	}

	/* Options Item method */
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
			return true;

		case R.id.shareDeadlinesImage:
			try
			{
				shareDeadlinesList();

			} catch (Exception e)
			{
				Toast.makeText(this, "Couldn't share :( ", Toast.LENGTH_SHORT).show();
			}
			return true;

		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
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

		// cancel button
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				dialog.dismiss();
			}
		});
		builder.setView(lv);
		final Dialog dialog = builder.create();

		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
			{
				// check this person has the right
				new AsyncTask<Boolean, Boolean, Boolean>()
				{
					ProgressDialog progressDialog;
					String message = "";

					@Override
					protected void onPreExecute()
					{
						progressDialog = ProgressDialog.show(GroupDeadlineActivity.this,
								"Checking If you're an admin",
								"web minion searching through admins' mail ");
					}

					@Override
					protected Boolean doInBackground(Boolean... params)
					{
						// check connection
						if (WebMinion.isConnected(GroupDeadlineActivity.this) == false)
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
						if (result == false && !message.equals("No Connection"))
						{
							Toast.makeText(GroupDeadlineActivity.this, message, Toast.LENGTH_SHORT)
									.show();
						} else
						{
							// start the add deadline activity
							Intent intent = new Intent(GroupDeadlineActivity.this,
									AddDeadlineActivity.class);
							intent.putExtra(MyUtils.INTENT_GROUP_ID, groupId);
							intent.putExtra(MyUtils.INTENT_GROUP_NAME, groupName);
							intent.putExtra(MyUtils.INTENT_GMAIL_ADDRESS, gUsernameList
									.get(position));
							intent.putExtra(MyUtils.INTENT_ADD_OFFLINE_DEADLINE, message.equals("No Connection"));
							if (message.equals("No Connection"))
								Toast.makeText(GroupDeadlineActivity.this, "No Connection, the deadline will be saved and uploaded when a connection is made", Toast.LENGTH_LONG).show();
							startActivityForResult(intent, MyUtils.ADD_DEADLINES_REQUEST_CODE);
							dialog.dismiss();
						}
						progressDialog.dismiss();
					}

				}.execute(true);

			}
		});

		dialog.show();

	}

	private void refreshDeadlines()
	{
		// get the deadlines from the server and add them
		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;
			String message;

			@Override
			protected void onPreExecute()
			{
				progressDialog = ProgressDialog.show(GroupDeadlineActivity.this, "Downloading",
						"Downloading deadlines...");
			}

			@Override
			protected Boolean doInBackground(Boolean... params)
			{
				// check connection
				if (WebMinion.isConnected(GroupDeadlineActivity.this) == false)
				{
					message = "No Connection";
					return false;
				}
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
				sortDeadlines();

				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				// dissmiss
				if (result == false)
					Toast.makeText(GroupDeadlineActivity.this, message, Toast.LENGTH_SHORT).show();

				progressDialog.dismiss();
				listAdapter.notifyDataSetChanged();
			}

		}.execute(true);
	}

	private void shareDeadlinesList()
	{
		// calculate dimensions of image
		int upmargin = 30;
		int leftMargin = 10;
		int nDeadlines = deadlines.size();
		int totalHeight = (nDeadlines+1) * upmargin;
		for (int i = 0; i < deadlines.size(); i++)
			totalHeight += listView.getChildAt(0).getHeight();
		int totalWidth = listView.getChildAt(0).getWidth() + leftMargin*2;

		// make bitmap with a background
		Bitmap bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bitmap);
		RadialGradient grad = new RadialGradient(totalWidth/2, totalHeight/2, Math.min(totalWidth/2, totalHeight/2), 0xffECF0F0, 0xffCCF0F0, TileMode.CLAMP);
		Paint paint = new Paint();
		paint.setShader(grad);
		c.drawRect(0, 0, c.getWidth(), c.getHeight(), paint);
		
		// paint each view
		int currHeight = upmargin;
		for (int i = 0; i < deadlines.size(); i++)
		{
			// paint plain view
			View view = listView.getChildAt(i);
			Bitmap viewBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
					Bitmap.Config.ARGB_8888);
			Canvas viewC = new Canvas(viewBitmap);
			view.draw(viewC);
			c.drawBitmap(viewBitmap, leftMargin, currHeight, new Paint());

			// increment current position
			currHeight += view.getHeight() + upmargin;
		}

		// get uri
		OutputStream output;

		// Find the SD Card path
		File filepath = Environment.getExternalStorageDirectory();

		// Create a new folder AndroidBegin in SD Card
		File dir = new File(filepath.getAbsolutePath() + "/SharedImagel/");
		dir.mkdirs();

		// Create a name for the saved image
		File file = new File(dir, "SharedDeadline.png");

		try
		{

			// Share Intent
			Intent share = new Intent(Intent.ACTION_SEND);

			// Type of file to share
			share.setType("image/jpeg");

			output = new FileOutputStream(file);

			// Compress into png format image from 0% - 100%
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
			output.flush();
			output.close();

			// Locate the image to Share
			Uri uri = Uri.fromFile(file);

			// Pass the image into an Intnet
			share.putExtra(Intent.EXTRA_STREAM, uri);

			// Show the social share chooser list
			startActivity(Intent.createChooser(share, "Share Deadlines Image"));

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

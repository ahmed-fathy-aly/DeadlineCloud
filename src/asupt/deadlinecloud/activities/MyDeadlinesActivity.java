package asupt.deadlinecloud.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.os.Build;
import android.preference.PreferenceManager;
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
		// if a deadline was added
		if (resultCode == RESULT_OK)
			sortDeadlines();

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onBackPressed()
	{
		NavUtils.navigateUpFromSameTask(this);
		super.onBackPressed();
	}

	@Override
	protected void onResume()
	{
		try
		{
			sortDeadlines();
		} catch (Exception e)
		{
		}
		super.onResume();
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

		// register for context menu
		registerForContextMenu(listView);

		// sort
		sortDeadlines();

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

	/* methods for deadlines */
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
		listAdapter.notifyDataSetChanged();
	}

	/* context menu stuff */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.expandableList)
		{
			ExpandableListView.ExpandableListContextMenuInfo acmi = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
			int idx = ExpandableListView.getPackedPositionGroup(acmi.packedPosition);
			menu.setHeaderTitle(deadlines.get(
					ExpandableListView.getPackedPositionGroup(acmi.packedPosition)).getTitle());
			menu.add(0, v.getId(), 0, "Add to Calendar");
			menu.add(0, v.getId(), 0, "Share Image of Deadline");

			menu.add(0, v.getId(), 0, "Remove from my deadlines");

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
		} else if (item.getTitle().equals("Remove from my deadlines"))
		{
			removeFromMyDeadlines(deadlines.get(idx));
		} else if (item.getTitle().equals("Share Image of Deadline"))
		{
			shareDeadlineImage(idx);
		}
		return super.onContextItemSelected(item);
	}

	private void removeFromMyDeadlines(Deadline deadline)
	{
		// if it's local then delete it
		if (deadline.getGroupName().equals(Deadline.localString))
		{
			database.deleteDeadline(deadline);
			deadlines.remove(deadline);
			listAdapter.notifyDataSetChanged();
		}

		// if it's from a group then just un mark it
		else
		{
			database.removeFromMyDeadlines(deadline);
			deadlines.remove(deadline);
			listAdapter.notifyDataSetChanged();

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
			startActivity(Intent.createChooser(share, "Share Deadline	Image"));

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/* option items stuff */
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
			startActivityForResult(intent, MyUtils.ADD_DEADLINES_REQUEST_CODE);
			return true;
		case R.id.action_settings:
			Intent intentS = new Intent(this, SettingsActivity.class);
			startActivity(intentS);
			return true;
		case R.id.shareDeadlinesImage:
			shareDeadlinesList();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void shareDeadlinesList()
	{
		// calculate dimensions of image
		int upmargin = 30;
		int leftMargin = 10;
		int nDeadlines = deadlines.size();
		int totalHeight = (nDeadlines + 1) * upmargin;
		for (int i = 0; i < deadlines.size(); i++)
			totalHeight += listView.getChildAt(0).getHeight();
		int totalWidth = listView.getChildAt(0).getWidth() + leftMargin * 2;

		// make bitmap with a background
		Bitmap bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bitmap);
		RadialGradient grad = new RadialGradient(totalWidth / 2, totalHeight / 2, Math.min(
				totalWidth / 2, totalHeight / 2), 0xffECF0F0, 0xffCCF0F0, TileMode.CLAMP);
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
			startActivity(Intent.createChooser(share, "Share Deadlines	Images"));

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}

package asupt.deadlinecloud.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import asupt.deadlinecloud.web.WebMinion;
import asuspt.deadlinecloud.R;

public class AddGroupActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_group);
		// Show the Up button in the action bar.
		setupActionBar();
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
		getMenuInflater().inflate(R.menu.add_group, menu);
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

	public void onButtonAddGroupClicked(View v)
	{
		// make a thread that asks the web minion to add the group
		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;

			@Override
			protected void onPreExecute()
			{
				// make a progress dialoh
				progressDialog = ProgressDialog.show(AddGroupActivity.this, "Connecting",
						"Adding Group");
			}

			@Override
			protected Boolean doInBackground(Boolean... params)
			{
				// Ask the web minion to add this group
				EditText editText = (EditText) findViewById(R.id.editTextGroupTitle);
				String groupName = editText.getText().toString();
				WebMinion.addGroup(groupName);

				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				// dismiss
				progressDialog.dismiss();
				AddGroupActivity.this.finish();
			}
		}.execute(true);
	}
}

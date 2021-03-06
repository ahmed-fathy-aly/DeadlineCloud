package asupt.deadlinecloud.activities;

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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import asupt.deadlinecloud.data.Group;
import asupt.deadlinecloud.utils.MyUtils;
import asupt.deadlinecloud.web.WebMinion;
import asuspt.deadlinecloud.R;
import asuspt.deadlinecloud.R.id;
import asuspt.deadlinecloud.R.layout;
import asuspt.deadlinecloud.R.menu;

public class AdminToolsActivity extends Activity
{
	/* string got from the intent */
	String gmailAddress;
	String groupId;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_admin_tools);
		setupActionBar();

		// get string from the intent
		gmailAddress = getIntent().getExtras().getString(MyUtils.INTENT_GMAIL_ADDRESS);
		groupId = getIntent().getExtras().getString(MyUtils.INTENT_GROUP_ID);

	}
	public void onBackPressed()
	{
		NavUtils.navigateUpFromSameTask(this);
		super.onBackPressed();
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
		getMenuInflater().inflate(R.menu.activity_admin_tools, menu);
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
			
		case R.id.action_settings :
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onButtonAddAdminClicked(View v)
	{
		// make a thread that tells the server about the new admin
		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;
			String message = "";
			String newAdminMail = "";
			
			@Override
			protected void onPreExecute()
			{
				// make a progress dialog
				progressDialog = ProgressDialog.show(AdminToolsActivity.this, "Adding",
						"Adding new admin...");
			}

			@Override
			protected Boolean doInBackground(Boolean... params)
			{
				// check connection
				if (WebMinion.isConnected(AdminToolsActivity.this) == false)
				{
					message = "No Connection";
					return false;
				}
				
				// get the chosen email
				EditText emailEditText = (EditText) findViewById(R.id.editTextAdminEmailAddress);
				newAdminMail = emailEditText.getText().toString().trim();
				WebMinion.addAdmin(groupId, gmailAddress, newAdminMail);

				// check it added successfuly
				if (!WebMinion.canManageGroup(groupId, newAdminMail))
				{
					message = "Oops...Something went wrong";
					return false;
				}
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				// check it didn't fail
				if (result == false)
					Toast.makeText(AdminToolsActivity.this, message, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(AdminToolsActivity.this, "New Admin added", Toast.LENGTH_SHORT).show();
				// dismiss the progress dialog
				progressDialog.dismiss();
			}

		}.execute(true);

	}
}

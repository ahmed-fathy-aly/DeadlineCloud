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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import asupt.deadlinecloud.utils.MyUtils;
import asupt.deadlinecloud.web.WebMinion;
import asuspt.deadlinecloud.R;

public class AddGroupActivity extends Activity
{
	String gmailAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_group);

		// get gmail address from intent
		gmailAddress = getIntent().getExtras().getString(MyUtils.INTENT_GMAIL_ADDRESS);
		setupActionBar();
		setAutoCompete();

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

	@Override
	public void onBackPressed()
	{
		NavUtils.navigateUpFromSameTask(this);
		super.onBackPressed();
	}

	public void onButtonAddGroupClicked(View v)
	{
		// make a thread that asks the web minion to add the group
		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;
			String message = "";

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
				EditText groupNamEditText = (EditText) findViewById(R.id.editTextGroupTitle);
				EditText descriptionEditText = (EditText) findViewById(R.id.editTextGroupDescription);
				AutoCompleteTextView graduationYearEditText = (AutoCompleteTextView) findViewById(R.id.editTextGraduationYear);
				AutoCompleteTextView departmentEditText = (AutoCompleteTextView) findViewById(R.id.editTextDepartment);
				AutoCompleteTextView tagEditText = (AutoCompleteTextView) findViewById(R.id.editTextTag);

				String groupName = groupNamEditText.getText().toString();
				String graduationYear = graduationYearEditText.getText().toString();
				String department = departmentEditText.getText().toString();
				String tag = tagEditText.getText().toString();
				String desciption = descriptionEditText.getText().toString();
				Boolean is_public = false; // TODO: Ask the user if he wants it to be public.
				WebMinion.addGroup(groupName, gmailAddress, graduationYear, department, tag,
						desciption, is_public);
				message = "added " + groupName + " by " + gmailAddress;
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				// dismiss
				progressDialog.dismiss();
				Toast.makeText(AddGroupActivity.this, message, Toast.LENGTH_SHORT).show();
				Intent returnIntent = new Intent();
				setResult(RESULT_OK, returnIntent);
				AddGroupActivity.this.finish();
			}
		}.execute(true);
	}

	private void setAutoCompete()
	{
		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;
			ArrayList<String> graduationYearHints;
			private ArrayList<String> departmentHints;
			private ArrayList<String> tagsHints;

			protected void onPreExecute()
			{
				progressDialog = ProgressDialog
						.show(AddGroupActivity.this, "Loading", "Loading...");
			}

			protected Boolean doInBackground(Boolean... params)
			{
				// load graduation year hints
				graduationYearHints = new ArrayList<String>();
				graduationYearHints.add(MyUtils.TAG_ANY);
				ArrayList<String> serverGraduationYear = WebMinion.getGraduationYears();
				for (String tag : serverGraduationYear)
					graduationYearHints.add(tag);

				// load departmentHint
				departmentHints = new ArrayList<String>();
				departmentHints.add(MyUtils.TAG_ANY);
				ArrayList<String> serverdepartments = WebMinion.getDeaprtments();
				for (String tag : serverdepartments)
					departmentHints.add(tag);

				// load tags
				tagsHints = new ArrayList<String>();
				tagsHints.add(MyUtils.TAG_ANY);
				ArrayList<String> serverTags = WebMinion.getTags();
				for (String tag : serverTags)
					tagsHints.add(tag);

				return true;
			}

			protected void onPostExecute(Boolean result)
			{
				// set graduation year edit text
				ArrayAdapter<String> graduationYearAdapter = new ArrayAdapter<String>(
						AddGroupActivity.this, android.R.layout.simple_list_item_1,
						graduationYearHints);
				AutoCompleteTextView graduationYear = (AutoCompleteTextView) AddGroupActivity.this
						.findViewById(R.id.editTextGraduationYear);
				graduationYear.setAdapter(graduationYearAdapter);

				// set departments
				ArrayAdapter<String> departmentAdapter = new ArrayAdapter<String>(
						AddGroupActivity.this, android.R.layout.simple_list_item_1, departmentHints);
				AutoCompleteTextView department = (AutoCompleteTextView) AddGroupActivity.this
						.findViewById(R.id.editTextDepartment);
				department.setAdapter(departmentAdapter);

				// set tags
				ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(AddGroupActivity.this,
						android.R.layout.simple_list_item_1, tagsHints);
				AutoCompleteTextView tags = (AutoCompleteTextView) AddGroupActivity.this
						.findViewById(R.id.editTextTag);
				tags.setAdapter(tagsAdapter);

				progressDialog.dismiss();

			}

		}.execute();

	}
}

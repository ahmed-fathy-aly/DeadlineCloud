package asupt.deadlinecloud.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import asupt.deadlinecloud.data.DatabaseController;
import asupt.deadlinecloud.data.Deadline;
import asupt.deadlinecloud.data.Group;
import asupt.deadlinecloud.utils.MyUtils;
import asupt.deadlinecloud.web.WebMinion;
import asuspt.deadlinecloud.R;

public class AddDeadlineActivity extends Activity
{

	private ArrayList<Group> groups;
	String groupId;
	String groupName;
	String gmailAddress;
	boolean offLineDedaline;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_deadline);

		// check if we have a certain group called with the intent
		if (getIntent().getExtras().containsKey(MyUtils.INTENT_GROUP_ID))
			groupId = getIntent().getExtras().getString(MyUtils.INTENT_GROUP_ID);
		else
			groupId = "";

		if (getIntent().getExtras().containsKey(MyUtils.INTENT_GROUP_NAME))
			groupName = getIntent().getExtras().getString(MyUtils.INTENT_GROUP_NAME);
		else
			groupName = "";

		if (getIntent().getExtras().containsKey(MyUtils.INTENT_GMAIL_ADDRESS))
			gmailAddress = getIntent().getExtras().getString(MyUtils.INTENT_GMAIL_ADDRESS);
		else
			gmailAddress = "";

		if (getIntent().getExtras().containsKey(MyUtils.INTENT_ADD_OFFLINE_DEADLINE))
			offLineDedaline = getIntent().getExtras().getBoolean(
					MyUtils.INTENT_ADD_OFFLINE_DEADLINE);
		else
			offLineDedaline = false;

		setUpSpiiners();
	}

	public void onBackPressed()
	{
		NavUtils.navigateUpFromSameTask(this);
		super.onBackPressed();
	}

	private void setUpSpiiners()
	{
		// Priority spinner
		Spinner priority = (Spinner) findViewById(R.id.spinnerPriority);
		String[] priorities = new String[]
		{ "High Priority", "Medium Priority", "Low Priority" };
		ArrayAdapter<String> prioritySpinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, priorities);
		priority.setAdapter(prioritySpinnerAdapter);

		// group names
		ArrayList<String> groupNames = new ArrayList<String>();
		groupNames.add(groupName);

		// Group spinner
		Spinner groupSpinner = (Spinner) findViewById(R.id.spinnerGroup);
		ArrayAdapter<String> groupsSpinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, groupNames);
		groupSpinner.setAdapter(groupsSpinnerAdapter);

	}

	public void onAddDeadlineButtonClicked(View v)
	{
		// gather the deadline info
		Deadline deadline = new Deadline();

		// title
		EditText title = (EditText) findViewById(R.id.editTextDeadlineTitle);
		deadline.setTitle(title.getText().toString());

		// description
		EditText description = (EditText) findViewById(R.id.editTextDeadlineDescription);
		deadline.setDescription(description.getText().toString());

		// date
		DatePicker date = (DatePicker) findViewById(R.id.datePickerDeadlineDate);
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.YEAR, date.getYear() - 1900);
		calendar.set(Calendar.MONTH, date.getMonth());
		calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
		deadline.setCalendar(calendar);

		// priority
		Spinner prioritySpinner = (Spinner) findViewById(R.id.spinnerPriority);
		int idx = prioritySpinner.getSelectedItemPosition();
		switch (idx)
		{
		case 0:
			deadline.setPriority(Deadline.Priorirty.HIGH);
			break;

		case 1:
			deadline.setPriority(Deadline.Priorirty.MEDIUM);
			break;
		case 2:
			deadline.setPriority(Deadline.Priorirty.LOW);
			break;

		default:
			break;
		}

		// group
		if (groupName.equals(Deadline.localString))
		{
			// local string
			deadline.setGroupName(Deadline.localString);
			deadline.setInMyDeadlines(1);

			// find the id of the latest deadline and make the id of this one
			// higher
			int id = 0;
			for (Deadline localDeadline : new DatabaseController(this).getAllDeadlines())
				try
				{
					id = Math.max(id, Integer.parseInt(localDeadline.getWebId()));
				} catch (Exception e)
				{
					id = 0;
				}

			deadline.setWebId((id) + "");

			// add deadline and leave
			MyDeadlinesActivity.addDeadline(deadline);
			setResult(RESULT_OK);
			finish();
		} else
		{
			deadline.setGroupName(groupName);
			deadline.setGroupId(groupId);
			deadline.setPosterMail(gmailAddress);

			if (offLineDedaline)
			{
				new DatabaseController(this).addOfflinesDeadline(deadline);
				finish();
			}
			else
				addDeadline(deadline, groupId, groupName);
		}

	}

	private void addDeadline(final Deadline deadline, final String destGroupId,
			final String destGroupName)
	{
		// asks minion to add the deadline
		new AsyncTask<Boolean, Boolean, Boolean>()
		{
			ProgressDialog progressDialog;
			String message = "";

			@Override
			protected void onPreExecute()
			{
				progressDialog = ProgressDialog.show(AddDeadlineActivity.this, "Connecting",
						"Adding deadline to cloud");
			}

			@Override
			protected Boolean doInBackground(Boolean... params)
			{
				// check connection
				if (WebMinion.isConnected(AddDeadlineActivity.this) == false)
				{
					message = "No Connection";
					return false;
				}

				// ask the minion to add it
				message = "Deadline added to " + destGroupName + "\nby  " + gmailAddress;
				WebMinion.postDeadline(destGroupId, gmailAddress, deadline);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				// check result
				if (result == false)
				{
					Toast.makeText(AddDeadlineActivity.this, message, Toast.LENGTH_SHORT).show();
				} else
				{
					Intent returnIntent = new Intent();
					setResult(RESULT_OK, returnIntent);
					finish();
				}

				// dissmiss
				progressDialog.dismiss();

			}

		}.execute(true);

	}
}

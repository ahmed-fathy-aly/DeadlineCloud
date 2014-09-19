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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_deadline);

		// check if we have a certain group called with the intent
		if (getIntent().getExtras().containsKey(MyUtils.INTENT_GROUP_ID))
		{
			groupId = getIntent().getExtras().getString(MyUtils.INTENT_GROUP_ID);
			groupName = getIntent().getExtras().getString(MyUtils.INTENT_GROUP_NAME);
			gmailAddress = getIntent().getExtras().getString(MyUtils.INTENT_GMAIL_ADDRESS);
		} else
			groupId = MyUtils.TAG_ANY;

		setUpSpiiners();
	}

	@Override
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
		if (groupId.equals(MyUtils.TAG_ANY))
		{
			DatabaseController database = new DatabaseController(this);
			groups = database.getAllGroups();
			groupNames.add("Local");
			for (Group group : groups)
				groupNames.add(group.getName());
		} else
		{
			groupNames.add(groupName);
		}

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
		Spinner groupSpinner = (Spinner) findViewById(R.id.spinnerGroup);
		idx = groupSpinner.getSelectedItemPosition();
		if (groupId.equals(MyUtils.TAG_ANY))
		{
			if (idx == 0)
			{
				// local string
				deadline.setGroupName(Deadline.localString);

				// add deadline and leave
				DeadlinesActivity.addDeadline(deadline);
				finish();
			} else
			{
				Group group = groups.get(idx - 1);
				deadline.setGroupName(group.getName());
				addDeadline(deadline, group.getId(), group.getName());
			}
		} else
		{
			deadline.setGroupName(groupName);
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
				// ask the minion to add it
				message = "Deadline added to " + destGroupName + "\nby  " + gmailAddress;
				WebMinion.postDeadline(destGroupId, gmailAddress, deadline);
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				// dissmiss
				progressDialog.dismiss();
				Intent returnIntent = new Intent();
				setResult(RESULT_OK, returnIntent);
				finish();

			}

		}.execute(true);

	}
}

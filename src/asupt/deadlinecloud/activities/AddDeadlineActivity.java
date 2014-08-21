package asupt.deadlinecloud.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;
import asupt.deadlinecloud.data.Deadline;
import asuspt.deadlinecloud.R;
import asuspt.deadlinecloud.R.layout;

public class AddDeadlineActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_deadline);

		setUpSpiiners();
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

		// Group spinner
		Spinner groupSpinner = (Spinner) findViewById(R.id.spinnerGroup);
		String[] groups = new String[1];
		groups[0] = "Local";
		ArrayAdapter<String> groupsSpinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, groups);
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
		deadline.setDate(date.getYear(), date.getMonth(), date.getDayOfMonth());
		Log.e("Game", "" + date.getDayOfMonth());
		
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
		deadline.setGroupName(Deadline.localString);

		// add deadline and leave
		DeadlinesActivity.addDeadline(deadline);
		finish();

	}
}

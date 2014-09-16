package asupt.deadlinecloud.views;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import asupt.deadlinecloud.data.Deadline;
import asupt.deadlinecloud.data.Reminder;
import asuspt.deadlinecloud.R;

public class DeadlineView extends LinearLayout
{
	public interface DeadlineViewListener
	{
		void onViewClicked();

		void addReminder(Reminder reminder);
	}

	/* member variables */
	private Context context;
	private Deadline deadline;
	private DeadlineViewListener listener;

	public DeadlineView(Context contex)
	{
		super(contex);
	}

	public DeadlineView(Context context, Deadline deadline, View convertView,
			DeadlineViewListener listener)
	{
		super(context);
		this.context = context;
		this.deadline = deadline;
		this.listener = listener;

		fillDeadlineInfo(convertView);
		setOnTouchListener(convertView);
		setOnClickListeners();

	}

	private void setOnTouchListener(View convertView)
	{
		// get the background layout and set its touch stuff
		final LinearLayout backgroundLayout = (LinearLayout) this
				.findViewById(R.id.deadlineBackground);
		backgroundLayout.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View arg0, MotionEvent event)
			{

				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					backgroundLayout.setBackgroundResource(R.drawable.deadline_background_selected);
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP)
				{
					backgroundLayout.setBackgroundResource(R.drawable.deadline_background_normal);
					listener.onViewClicked();
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_CANCEL)
				{
					backgroundLayout.setBackgroundResource(R.drawable.deadline_background_normal);
					return true;
				}
				return false;

			}
		});

	}

	private void setOnClickListeners()
	{
		// // listener for the remind me button
		// Button reminderButton = (Button)
		// findViewById(R.id.buttonDeadlineRemind);
		// reminderButton.setOnClickListener(new OnClickListener()
		// {
		// @Override
		// public void onClick(View arg0)
		// {
		// // make a dialog
		// final ReminderDialog reminderDialog = new ReminderDialog(context);
		// reminderDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// reminderDialog.setContentView(R.layout.reminder_dialog);
		// reminderDialog.setDeadline(deadline);
		// reminderDialog.show();
		//
		// // cancel button listener
		// Button cancelButton = (Button) reminderDialog
		// .findViewById(R.id.buttonReminderCancel);
		// cancelButton.setOnClickListener(new OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// reminderDialog.dismiss();
		// }
		// });
		//
		// // add button listener
		// Button addReminderButton = (Button) reminderDialog
		// .findViewById(R.id.buttonReminderAdd);
		// addReminderButton.setOnClickListener(new OnClickListener()
		// {
		// @Override
		// public void onClick(View v)
		// {
		// // gather selected Date
		// Calendar c = reminderDialog.getSelectedTime();
		// addReminder(deadline, c);
		// reminderDialog.dismiss();
		// }
		//
		// });
		//
		// }
		// });
	}

	private void addReminder(Deadline deadline, Calendar c)
	{
		// make a reminder and add it
		Reminder reminder = new Reminder();
		reminder.setCalendar(c);
		reminder.setDeadline(deadline);
		listener.addReminder(reminder);

	}

	private void fillDeadlineInfo(View convertView)
	{
		// check if the view is null or inflated
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = inflater.inflate(R.layout.deadline_layout, this, true);

		// title
		TextView title = (TextView) convertView.findViewById(R.id.textViewDeadlineTitle);
		title.setText(deadline.getTitle());

		// date
		TextView date = (TextView) convertView.findViewById(R.id.textViewDeadlineDate);
		date.setText(deadline.getCalendar().get(Calendar.DAY_OF_MONTH) + "/"
				+ ((deadline.getCalendar().get(Calendar.MONTH)) + 1) + "/"
				+ (deadline.getCalendar().get(Calendar.YEAR) + 1900));

		// group
		TextView group = (TextView) convertView.findViewById(R.id.textViewDeadlineGroup);
		group.setText(deadline.getGroupName());

		// days rem
		TextView daysRem = (TextView) convertView.findViewById(R.id.textViewDeadlineDaysRem);
		daysRem.setText(deadline.getRemainingDays() + "");

		// priority
		View prorityIndicator = convertView.findViewById(R.id.deadlinePriorityIndicator);
		if (deadline.getPriority() == Deadline.Priorirty.HIGH)
			prorityIndicator.setBackgroundColor(Deadline.HIGH_COLOR);
		else if (deadline.getPriority() == Deadline.Priorirty.MEDIUM)
			prorityIndicator.setBackgroundColor(Deadline.MID_COLOR);
		else
			prorityIndicator.setBackgroundColor(Deadline.LOW_COLOR);

	}

}

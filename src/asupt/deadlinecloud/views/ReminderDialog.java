package asupt.deadlinecloud.views;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;
import asupt.deadlinecloud.data.Deadline;
import asuspt.deadlinecloud.R;

public class ReminderDialog extends Dialog
{
	/* member variables */
	private Deadline deadline;
	private Context context;

	public ReminderDialog(Context context)
	{
		super(context);
		this.context = context;
	}

	public Calendar getSelectedTime()
	{
		Calendar c = new GregorianCalendar();

		// set the date
		DatePicker datePicker = (DatePicker) findViewById(R.id.datePickerReminderDate);
		c.set(Calendar.YEAR, datePicker.getYear());
		c.set(Calendar.MONTH, datePicker.getMonth());
		c.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());

		// set the hour
		TimePicker timePicker = (TimePicker) this.findViewById(R.id.timePickerReminderTime);
		c.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
		c.set(Calendar.MINUTE, timePicker.getCurrentMinute());

		return c;
	}

	public void setDeadline(Deadline newDeadline)
	{
		this.deadline = newDeadline;
		setToDeadlineTime();
	}

	public ReminderDialog(Context context, Deadline deadline)
	{
		super(context);
		this.context = context;
		this.deadline = deadline;
	}

	private void setToDeadlineTime()
	{
		// set the date and hour to the deadline date
		DatePicker datePicker = (DatePicker) findViewById(R.id.datePickerReminderDate);
		datePicker.updateDate(deadline.getCalendar().get(Calendar.YEAR), deadline.getCalendar()
				.get(Calendar.MONTH), deadline.getCalendar().get(Calendar.DAY_OF_MONTH));
		TimePicker timePicker = (TimePicker) this.findViewById(R.id.timePickerReminderTime);
		timePicker.setCurrentHour(8);
		timePicker.setCurrentMinute(0);

	}

	private void addReminder()
	{
		// adds a reminder to the data base
		Log.e("Game", "adding ... ");

	}
}

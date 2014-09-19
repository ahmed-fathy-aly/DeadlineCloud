package asupt.deadlinecloud.adapters;

import java.util.Calendar;

import android.content.Context;
import android.hardware.Camera.PreviewCallback;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import asupt.deadlinecloud.data.Deadline;
import asupt.deadlinecloud.data.Reminder;
import asuspt.deadlinecloud.R;

public class RemindersListAdapter extends BaseExpandableListAdapter
{

	public interface RemindersListListener
	{

		int getRemindersCount();

		Reminder getReminder(int index);

		void removeReminder(int index);
	}

	/* member variables */
	private Context context;
	private ExpandableListView listView;
	public int selectedIndex = -1;
	private RemindersListListener listener;

	public RemindersListAdapter(Context context, ExpandableListView listView,
			RemindersListListener listener)
	{
		this.context = context;
		this.listView = listView;
		this.listener = listener;
	}

	@Override
	public Object getChild(int arg0, int arg1)
	{
		return null;
	}

	@Override
	public long getChildId(int arg0, int arg1)
	{
		return 0;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.reminder_child_layout, null);
		}
		// description
		TextView description = (TextView) convertView
				.findViewById(R.id.textViewReminderDescription);
		description.setText(listener.getReminder(groupPosition).getDeadline().getDescription());

		// delete button
		ImageButton deletebutton = (ImageButton) convertView
				.findViewById(R.id.buttonReminderDelete);
		deletebutton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				listener.removeReminder(groupPosition);
				selectedIndex = -1;
			}
		});

		return convertView;
	}

	@Override
	public int getChildrenCount(int arg0)
	{
		return 1;
	}

	@Override
	public Object getGroup(int arg0)
	{
		return null;
	}

	@Override
	public int getGroupCount()
	{
		return listener.getRemindersCount();
	}

	@Override
	public long getGroupId(int arg0)
	{
		return 0;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent)
	{

		// check if the view is null or inflated
		if (convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.reminder_layout, null);		
		}
		
		fillReminderData(groupPosition, convertView);
		setTouchListener(groupPosition, convertView);

		return convertView;
	}

	private void setTouchListener(final int groupPosition, View convertView)
	{
		// get the background layout and set its touch stuff
		final LinearLayout backgroundLayout = (LinearLayout) convertView
				.findViewById(R.id.reminderBackground);
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
					OnViewClicked(groupPosition);
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

	private void fillReminderData(final int groupPosition, View convertView)
	{
		// get the data
		Reminder reminder = listener.getReminder(groupPosition);
		Deadline deadline = reminder.getDeadline();

		// title
		TextView title = (TextView) convertView.findViewById(R.id.textViewReminderTitle);
		title.setText(deadline.getTitle());

		// date
		TextView date = (TextView) convertView.findViewById(R.id.textViewReminderDate);
		date.setText(deadline.getCalendar().get(Calendar.DAY_OF_MONTH) + "/"
				+ ((deadline.getCalendar().get(Calendar.MONTH)) + 1) + "/"
				+ (deadline.getCalendar().get(Calendar.YEAR) + 1900));

		// group
		TextView group = (TextView) convertView.findViewById(R.id.textViewReminderGroup);
		group.setText(deadline.getGroupName());

		// days rem
		TextView daysRem = (TextView) convertView.findViewById(R.id.textViewReminderDaysRem);
		daysRem.setText(deadline.getRemainingDays() + "");

		// priority
//		View prorityIndicator = convertView.findViewById(R.id.reminderPriorityIndicator);
//		if (deadline.getPriority() == Deadline.Priorirty.HIGH)
//			prorityIndicator.setBackgroundColor(Deadline.HIGH_COLOR);
//		else if (deadline.getPriority() == Deadline.Priorirty.MEDIUM)
//			prorityIndicator.setBackgroundColor(Deadline.MID_COLOR);
//		else
//			prorityIndicator.setBackgroundColor(Deadline.LOW_COLOR);

		// notificatiot date
		TextView notificationDate = (TextView) convertView
				.findViewById(R.id.textViewReminderNotificationDate);
		notificationDate.setText("notify at " + reminder.getCalendar().get(Calendar.DAY_OF_MONTH)
				+ "/" + ((reminder.getCalendar().get(Calendar.MONTH)) + 1) + "/"
				+ (reminder.getCalendar().get(Calendar.YEAR) + 1900));
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1)
	{
		return false;
	}

	private void OnViewClicked(int groupPosition)
	{
		if (groupPosition == selectedIndex)
		{
			listView.collapseGroup(groupPosition);
			selectedIndex = -1;
		} else
		{
			if (selectedIndex != -1)
				listView.collapseGroup(selectedIndex);
			listView.expandGroup(groupPosition);
			selectedIndex = groupPosition;
		}
	}
}

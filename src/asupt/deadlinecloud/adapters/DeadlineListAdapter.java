package asupt.deadlinecloud.adapters;

import java.util.Calendar;
import java.util.Collections;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import asupt.deadlinecloud.data.Deadline;
import asupt.deadlinecloud.data.Reminder;
import asupt.deadlinecloud.utils.MyUtils;
import asuspt.deadlinecloud.R;

public class DeadlineListAdapter extends BaseExpandableListAdapter
{

	public interface DeadlineListListener
	{
		void removeDeadline(int idx);

		Deadline getDeadline(int idx);

		int getDeadlinesCount();

			}

	/* member variables */
	private Context context;
	private ExpandableListView listView;
	public int selectedIndex = -1;
	private DeadlineListListener listener;

	public DeadlineListAdapter(Context context, ExpandableListView listView,
			DeadlineListListener listener)
	{
		this.context = context;
		this.listView = listView;
		this.listener = listener;

	}

	public Object getChild(int arg0, int arg1)
	{
		return null;
	}

	public long getChildId(int arg0, int arg1)
	{
		return 0;
	}

	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.deadline_child_layout, null);
		}

		// description
		TextView description = (TextView) convertView.findViewById(R.id.textViewDeadineDescription);
		description.setText(listener.getDeadline(groupPosition).getDescription());

		// delete button
		ImageButton deletebutton = (ImageButton) convertView
				.findViewById(R.id.buttonDeadlineDelete);
		deletebutton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				listener.removeDeadline(groupPosition);
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
		return listener.getDeadlinesCount();
	}

	@Override
	public long getGroupId(int arg0)
	{
		// TODO Auto-generated method stub
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
			convertView = inflater.inflate(R.layout.deadline_layout, null);
		}

		// title
		Deadline deadline = listener.getDeadline(groupPosition);
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
		
		return convertView;
	}

	@Override
	public boolean hasStableIds()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1)
	{
		return true;
	}

}

package asupt.deadlinecloud.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import asupt.deadlinecloud.data.Deadline;
import asupt.deadlinecloud.data.Reminder;
import asupt.deadlinecloud.views.DeadlineView;
import asupt.deadlinecloud.views.DeadlineView.DeadlineViewListener;
import asuspt.deadlinecloud.R;

public class DeadlineListAdapter extends BaseExpandableListAdapter
{

	public interface DeadlineListListener
	{
		void removeDeadline(int idx);

		Deadline getDeadline(int idx);

		int getDeadlinesCount();

		void addReminder(Reminder reminder);
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
		ImageButton deletebutton = (ImageButton) convertView.findViewById(R.id.buttonDeadlineDelete);
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
		// the listener
		DeadlineViewListener DeadlineViewListener = new DeadlineViewListener()
		{
			public void onViewClicked()
			{

				if (listView.isGroupExpanded(groupPosition))
				{

					listView.collapseGroup(groupPosition);
					selectedIndex = -1;
				} else
				{
					// collapse any other view
					if (selectedIndex != -1)
						listView.collapseGroup(selectedIndex);

					// expand this one
					listView.expandGroup(groupPosition);
					selectedIndex = groupPosition;
				}
			}

			@Override
			public void addReminder(Reminder reminder)
			{
				listener.addReminder(reminder);
				
			}
		};
		DeadlineView deadlineView = new DeadlineView(context, this.listener
				.getDeadline(groupPosition), convertView, DeadlineViewListener);
		return deadlineView;
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

package asupt.deadlinecloud.adapters;

import android.content.Context;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import asupt.deadlinecloud.data.Group;
import asuspt.deadlinecloud.R;

public class AllGroupsListAdapter extends BaseExpandableListAdapter
{
	public interface AllGroupsListListener
	{

		int getAllGroupsCount();

		Group getUnsyncedGroup(int index);

		void syncGroup(int index);

		boolean isSynced(Group group);

	}

	/* member variables */
	private Context context;
	private AllGroupsListListener listener;

	public AllGroupsListAdapter(Context context, AllGroupsListListener listener)
	{
		this.listener = listener;
		this.context = context;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		return null;
	}

	@Override
	
public long getChildId(int groupPosition, int childPosition)
	{
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent)
	{
		
		if (convertView == null)
			convertView = new TextView(context);
		
		Group group = listener.getUnsyncedGroup(groupPosition);
		String text = "";
		if (group.getGraduationYear() != null && group.getGraduationYear().equals("") == false)
			text += group.getGraduationYear() + "\n";
		if (group.getDepartment() != null && group.getDepartment().equals("") == false)
			text += group.getDepartment() + "\n";
		if (group.getTag() != null && group.getTag().equals("") == false)
			text += group.getTag() + "\n";
		if (group.getDescirption() != null && group.getDescirption().equals("") == false )
			text += group.getDescirption() + "\n";
		TextView textView = (TextView) convertView;
		textView.setText(text);
		return textView;
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		return 1;
	}
	

	@Override
	public Object getGroup(int groupPosition)
	{
		return null;
	}

	@Override
	public int getGroupCount()
	{
		return listener.getAllGroupsCount();
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		return 0;
	}

	@Override
	public View getGroupView(final int index, boolean isExpanded, View convertView, ViewGroup parent)
	{
		// check if we need to inflate
		if (convertView == null)
		{

			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.all_groups_layout, null);
		}

		// fill the info
		Group group = listener.getUnsyncedGroup(index);

		// name
		TextView groupName = (TextView) convertView.findViewById(R.id.textViewGroupName);
		groupName.setText(group.getName());

		// number of subscribers
		TextView nSubscriberss = (TextView) convertView
				.findViewById(R.id.textViewNmberOfSubscribers);
		nSubscriberss.setText(group.getNumberOfSubscribers() + " users");

		// check if it's already there
		if (listener.isSynced(group))
		{
			// disable the button
			Button syncButton = (Button) convertView.findViewById(R.id.buttonGroupSync);
			syncButton.setVisibility(View.GONE);

			// enable the image view
			ImageView checkImage = (ImageView) convertView.findViewById(R.id.imageViewSynced);
			checkImage.setVisibility(View.VISIBLE);
		} else
		{
			// disable the image view
			ImageView checkImage = (ImageView) convertView.findViewById(R.id.imageViewSynced);
			checkImage.setVisibility(View.GONE);

			// listener for the sync button
			Button syncButton = (Button) convertView.findViewById(R.id.buttonGroupSync);
			syncButton.setVisibility(View.VISIBLE);
			syncButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View arg0)
				{
					listener.syncGroup(index);
				}
			});
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return false;
	}

}

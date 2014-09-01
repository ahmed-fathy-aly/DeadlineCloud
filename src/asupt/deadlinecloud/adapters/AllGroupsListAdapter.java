package asupt.deadlinecloud.adapters;

import android.content.Context;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import asupt.deadlinecloud.data.Group;
import asuspt.deadlinecloud.R;

public class AllGroupsListAdapter extends BaseAdapter
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
	public int getCount()
	{
		return listener.getAllGroupsCount();
	}

	@Override
	public Object getItem(int arg0)
	{
		return null;
	}

	@Override
	public long getItemId(int arg0)
	{
		return 0;
	}

	@Override
	public View getView(final int index, View convertView, ViewGroup arg2)
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

}

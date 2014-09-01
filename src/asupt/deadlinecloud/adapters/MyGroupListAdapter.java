package asupt.deadlinecloud.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import asupt.deadlinecloud.data.Group;
import asuspt.deadlinecloud.R;

public class MyGroupListAdapter extends BaseAdapter
{
	public interface MyGroupListListener
	{
		int getGroupCount();

		Group getGroup(int index);

		void unSync(int index);
	}

	/* member variables */
	private Context context;
	private MyGroupListListener listener;

	public MyGroupListAdapter(Context context, MyGroupListListener listener)
	{
		this.context = context;
		this.listener = listener;
	}

	@Override
	public int getCount()
	{
		return listener.getGroupCount();
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
			convertView = inflater.inflate(R.layout.my_group_layout, null);
		}

		// fill info into the vew
		Group group = this.listener.getGroup(index);

		// group name
		TextView groupName = (TextView) convertView.findViewById(R.id.textViewMyGroupName);
		groupName.setText(group.getName());

		// unsync button
		Button unsyncButton = (Button) convertView.findViewById(R.id.buttonmyGroupUnSync);
		unsyncButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				listener.unSync(index);
			}
		});

		return convertView;
	}

}

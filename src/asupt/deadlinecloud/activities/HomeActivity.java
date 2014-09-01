package asupt.deadlinecloud.activities;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import asupt.deadlinecloud.data.Reminder;
import asuspt.deadlinecloud.R;
import asuspt.deadlinecloud.R.layout;
import asuspt.deadlinecloud.R.menu;

public class HomeActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	public void onButtonDeadlinesClicked(View v)
	{
		Intent intent = new Intent(this, DeadlinesActivity.class);
		startActivity(intent);
	}

	public void onButtonSyncClicked(View v)
	{
		Intent intent = new Intent(this, SyncActivity.class);
		startActivity(intent);
	}

	public void onButtonRemindersClickes(View v)
	{
		Intent intent = new Intent(this, RemindersActivity.class);
		startActivity(intent);
	}
}

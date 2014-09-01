package asupt.deadlinecloud.utils;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import asupt.deadlinecloud.data.Group;

public class MyUtils
{

	/* static stuff */
	public static final String PREFS_NAME = "asupt.deadlinecloud.prefs";
	
	
	public static void addGroupToPreferences(String groupName, String id, Context context)
	{
		
	}
	
	public static ArrayList<Group> getSyncedGroups()
	{
		ArrayList<Group> groups = new ArrayList<Group>();
		groups.add(new Group("Club", "123", 10));
		groups.add(new Group("Fun", "2233", 10));
		groups.add(new Group("Bla Bla", "123", 10));
		
		return groups;
	}

	
	public static String getUserId(Context context)
	{
		return Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID); 
	}
}


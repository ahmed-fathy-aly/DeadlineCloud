package asupt.deadlinecloud.utils;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import asupt.deadlinecloud.data.Group;

public class MyUtils
{

	/* static stuff */
	public static final String PREFS_NAME = "asupt.deadlinecloud.prefs";
	public static final int ADD_DEADLINES_REQUEST_CODE = 111;
	public static final int ADD_GROUP_REQUEST_CODE = 112;
	public static final String TAG_ANY = "";
	public static final String INTENT_GMAIL_ADDRESS = "gmailAddress";
	public static final String INTENT_GROUP_ID = "groupId";

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
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

	public static ArrayList<String> getGmailAccounts(Context context)
	{
		ArrayList<String> gUsernameList = new ArrayList<String>();
		AccountManager accountManager = AccountManager.get(context);
		Account[] accounts = accountManager.getAccountsByType("com.google");

		gUsernameList.clear();
		// loop
		for (Account account : accounts)
		{
			gUsernameList.add(account.name);
		}
		return gUsernameList;
	}
}

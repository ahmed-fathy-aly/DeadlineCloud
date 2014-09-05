package asupt.deadlinecloud.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.text.format.Time;
import android.util.Log;
import asupt.deadlinecloud.data.Deadline;
import asupt.deadlinecloud.data.Group;
import asupt.deadlinecloud.data.Deadline.Priorirty;

public class WebMinion
{

	static HttpClient client = new DefaultHttpClient();
	final static String initUrl = "http://mydeadlinecloud.herokuapp.com/";

	/**
	 * @add a new group. Note that when a user adds a group, he doesn't get
	 *      subscribed to it.
	 */
	public static boolean addGroup(String groupName)
	{
		boolean debug = false;
		if (debug)
			return true;
		
		HttpPost httppost = new HttpPost(initUrl + "groups.json");

		try
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("group[name]", groupName));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = client.execute(httppost);
			return (response.getStatusLine().getStatusCode() == 201);

		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}

		return false;
	}

	/**
	 * @return a list of all groups including each groups's name, id, number of
	 *         subscribers
	 */
	public static ArrayList<Group> getAllGroups()
	{
		// debug
		boolean web = true;
		if (!web)
		{
			ArrayList<Group> groups = new ArrayList<Group>();

			groups.add(new Group("CSE 2016", "123", 400));
			groups.add(new Group("Masa7in 2016", "56734", 111));
			groups.add(new Group("CSE 2017", "6734", 200));
			groups.add(new Group("Mech 2012", "734", 300));
			groups.add(new Group("CIVIL 2012", "34", 20));
			groups.add(new Group("bananas", "4", 5));

			return groups;
		}

		HttpGet httpget = new HttpGet(initUrl + "groups.json");
		ArrayList<Group> groups = new ArrayList<Group>();
		try
		{
			HttpResponse response = client.execute(httpget);
			HttpEntity resEntity = response.getEntity();
			final String response_str = EntityUtils.toString(resEntity);
			if (resEntity != null)
			{
				Log.i("RESPONSE", response_str);
				JSONArray res = new JSONArray(response_str);
				for (int i = 0; i < res.length(); i++)
				{
					JSONObject group = res.getJSONObject(i);
					groups.add(new Group(group.getString("name"), String
							.valueOf(group.getInt("id")), group.getInt("subscribers_count")));
				}
			}
		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}

		return groups;

	}

	/**
	 * try to subscribe to the group with that id and asks the server to
	 * increment the number of subscribers
	 */
	public static void subscribe(String groupId, String userId)
	{
		boolean debug = true;
		if (debug)
			return;
		
		HttpPost httppost = new HttpPost(initUrl + "groups/" + groupId + "/users.json");

		try
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("user[phone]", userId));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			client.execute(httppost);

		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}

	}

	/**
	 * asks the server to add a deadline to a certain group
	 */
	public static void postDeadline(String groupId, String userId, Deadline deadline)
	{
		boolean debug = false;
		if (debug)
			return;
		
		HttpPost httppost = new HttpPost(initUrl + "groups/" + groupId + "/deadlines.json");

		try
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("phone_id", userId));
			nameValuePairs.add(new BasicNameValuePair("deadline[name]", deadline.getTitle()));
			nameValuePairs.add(new BasicNameValuePair("deadline[description]", deadline
					.getDescription()));
			nameValuePairs.add(new BasicNameValuePair("deadline[priority]", String.valueOf(deadline
					.getWebPriority())));
			nameValuePairs.add(new BasicNameValuePair("deadline[time]", String.valueOf(deadline
					.getCalendar())));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			client.execute(httppost);

		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}
	}

	/**
	 * returns all the deadlines in the group with that id
	 */
	public static ArrayList<Deadline> getAllDeadlines(String groupId)
	{

		boolean debug = false;
		if (debug)
		{
			ArrayList<Deadline> deadlines = new ArrayList<Deadline>();
			Deadline deadline = new Deadline();
			deadline.setGroupName(groupId);
			Calendar calendar = new GregorianCalendar();
			calendar.set(Calendar.YEAR, 2014 - 1900);
			deadline.setCalendar(calendar);
			deadlines.add(deadline);
			return deadlines;
		}
		
		HttpGet httpget = new HttpGet(initUrl + "groups/" + groupId + "/deadlines.json");
		ArrayList<Deadline> deadlines = new ArrayList<Deadline>();
		try
		{
			HttpResponse response = client.execute(httpget);
			HttpEntity resEntity = response.getEntity();
			final String response_str = EntityUtils.toString(resEntity);
			if (resEntity != null)
			{
				Log.i("RESPONSE", response_str);
				JSONArray res = new JSONArray(response_str);
				for (int i = 0; i < res.length(); i++)
				{
					JSONObject deadline = res.getJSONObject(i);
					Deadline d = new Deadline();
					Calendar t = Calendar.getInstance();
					t.setTimeInMillis(deadline.getLong("utc_time"));
					d.setCalendar(t);
					d.setDescription(deadline.getString("description"));
					d.setWebPriority(deadline.getInt("priority"));
					d.setTitle(deadline.getString("name"));
					d.setGroupName(deadline.getString("group_name"));
					deadlines.add(d);
				}
			}
		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}

		return deadlines;
	}

}
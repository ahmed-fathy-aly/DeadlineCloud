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
				Log.i("RESPONSE", "1");
				for (int i = 0; i < res.length(); i++)
				{
					Log.i("RESPONSE", "2");
					JSONObject group = res.getJSONObject(i);
					Log.i("RESPONSE", "3");
					Log.i("RESPONSE", group.getString("name"));
					Log.i("RESPONSE", String.valueOf(group.getInt("id")));
					Log.i("RESPONSE", String.valueOf(group.getInt("subscribers_count")));
					groups.add(new Group(group.getString("name"), String.valueOf(group.getInt("id")), group.getInt("subscribers_count")));
					Log.i("RESPONSE", "4");
				}
				Log.i("RESPONSE", "5");
				return groups;
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
	
		
		HttpPost httppost = new HttpPost(initUrl + "groups/" + groupId + "/users.json");

		try
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("user[phone]", userId));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = client.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			final String response_str = EntityUtils.toString(resEntity);
			Log.e("RESPONSE",response_str);
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

		
		HttpPost httppost = new HttpPost(initUrl + "groups/" + groupId + "/deadlines.json");

		try
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("phone_id", userId));
			nameValuePairs.add(new BasicNameValuePair("deadline[name]", deadline.getTitle()));
			nameValuePairs.add(new BasicNameValuePair("deadline[description]", deadline.getDescription()));
			nameValuePairs.add(new BasicNameValuePair("deadline[priority]", String.valueOf(deadline.getWebPriority())));
			Calendar t = deadline.getCalendar();
			t.set(Calendar.YEAR, t.get(Calendar.YEAR) - 1900);
			Log.i("asasdfad",String.valueOf(t.getTimeInMillis()));
			nameValuePairs.add(new BasicNameValuePair("utc_time", String.valueOf(t.getTimeInMillis())));
			Log.i("RESPONSE",String.valueOf(deadline.getCalendar()));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = client.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			final String response_str = EntityUtils.toString(resEntity);
			Log.i("RESPONSE",response_str);

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
					t.set(Calendar.YEAR, t.get(Calendar.YEAR) - 1900);
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
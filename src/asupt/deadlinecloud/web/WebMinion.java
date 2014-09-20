package asupt.deadlinecloud.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;
import android.util.Patterns;
import asupt.deadlinecloud.data.Deadline;
import asupt.deadlinecloud.data.Group;
import asupt.deadlinecloud.utils.MyUtils;

public class WebMinion
{

	static HttpClient client = new DefaultHttpClient();
	final static String initUrl = "http://mydeadlinecloud.herokuapp.com/";

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	public static Boolean sendRegistrationId(String regId, String gmailId)
	{
		Log.i("STH", "SDH6");
		HttpPost httppost = new HttpPost(initUrl + "users.json");
		Log.i("STH", "SDH7");
		try
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("reg_id", regId));
			nameValuePairs.add(new BasicNameValuePair("user[phone]", gmailId));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			Log.i("STH", "SDH8");
			// Execute HTTP Post Request
			HttpResponse response = client.execute(httppost);
			Log.i("STH", "SDH9");
			Log.i("RESPONSE", response.toString());
			return (response.getStatusLine().getStatusCode() == 201);
		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}

		return false;
	}

	/**
	 * @return the Gmail ID of the user.
	 */
	public static String getGmailId(Context context)
	{
		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
		Account[] accounts = AccountManager.get(context).getAccounts();
		for (Account account : accounts)
		{
			if (emailPattern.matcher(account.name).matches())
			{
				return account.name;
			}
		}
		return "";
	}

	/**
	 * @param tag
	 * @param department
	 * @param graduationYear
	 * @param desciption
	 * @add a new group. Note that when a user adds a group, he doesn't get
	 *      subscribed to it.
	 */
	public static boolean addGroup(String groupName, String gmailId, String graduationYear,
			String department, String tag, String description)
	{
		HttpPost httppost = new HttpPost(initUrl + "groups.json");

		try
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("group[name]", groupName));
			nameValuePairs.add(new BasicNameValuePair("group[owner]", gmailId));
			nameValuePairs.add(new BasicNameValuePair("group[description]", description));
			nameValuePairs.add(new BasicNameValuePair("graduation_year", graduationYear));
			nameValuePairs.add(new BasicNameValuePair("department", department));
			nameValuePairs.add(new BasicNameValuePair("tag", tag));

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
	 * @param tag
	 * @param department
	 * @param graduationYear
	 * @return a list of all groups including each groups's name, id, number of
	 *         subscribers
	 */
	public static ArrayList<Group> getAllGroups(String graduationYear, String department, String tag)
	{
		String finalUrl = initUrl + "groups.json?";
		if (graduationYear != MyUtils.TAG_ANY)
			finalUrl += "&graduation_year=" + graduationYear;
		if (department != MyUtils.TAG_ANY)
			finalUrl += "&department=" + department;
		if (tag != MyUtils.TAG_ANY)
			finalUrl += "&tag=" + tag;
		HttpGet httpget = new HttpGet(finalUrl);
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
					Group g = new Group(group.getString("name"),
							String.valueOf(group.getInt("id")), group.getInt("subscribers_count"));
					g.setGraduationYear(group.getString("graduation_year_name"));
					g.setDescirption(group.getString("description"));
					g.setDepartment(group.getString("department_name"));
					g.setTag(group.getString("tag_name"));
					groups.add(g);
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
	public static void subscribe(String groupId, String gmailId)
	{

		HttpPost httppost = new HttpPost(initUrl + "groups/" + groupId + "/users.json");

		try
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("user[phone]", gmailId));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = client.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			final String response_str = EntityUtils.toString(resEntity);
			Log.e("RESPONSE", response_str);
		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}

	}

	/**
	 * asks the server to add a deadline to a certain group
	 */
	public static void postDeadline(String groupId, String gmailId, Deadline deadline)
	{

		HttpPost httppost = new HttpPost(initUrl + "groups/" + groupId + "/deadlines.json");

		try
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("phone_id", gmailId));
			nameValuePairs.add(new BasicNameValuePair("deadline[name]", deadline.getTitle()));
			nameValuePairs.add(new BasicNameValuePair("deadline[description]", deadline
					.getDescription()));
			nameValuePairs.add(new BasicNameValuePair("deadline[priority]", String.valueOf(deadline
					.getWebPriority())));
			Calendar t = deadline.getCalendar();
			t.set(Calendar.YEAR, t.get(Calendar.YEAR) - 1900);
			Log.i("asasdfad", String.valueOf(t.getTimeInMillis()));
			nameValuePairs.add(new BasicNameValuePair("utc_time", String.valueOf(t
					.getTimeInMillis())));
			Log.i("RESPONSE", String.valueOf(deadline.getCalendar()));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = client.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			final String response_str = EntityUtils.toString(resEntity);
			Log.i("RESPONSE", response_str);

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
					d.SetFromWeb(deadline);
					deadlines.add(d);
				}
			}
		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}

		return deadlines;
	}

	/**
	 * return all tags of graduation year
	 */
	public static ArrayList<String> getGraduationYears()
	{

		HttpGet httpget = new HttpGet(initUrl + "graduation_years.json");
		ArrayList<String> result = new ArrayList<String>();
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
					JSONObject grad_year = res.getJSONObject(i);
					result.add(grad_year.getString("name"));
					Log.i("RESPONSE", "4");
				}
				Log.i("RESPONSE", "5");
			}
		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}
		return result;
	}

	/**
	 * @return all departments
	 */
	public static ArrayList<String> getDeaprtments()
	{
		HttpGet httpget = new HttpGet(initUrl + "departments.json");
		ArrayList<String> result = new ArrayList<String>();
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
					JSONObject department = res.getJSONObject(i);
					result.add(department.getString("name"));
					Log.i("RESPONSE", "4");
				}
				Log.i("RESPONSE", "5");
			}
		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}
		return result;
	}

	/**
	 * @return all tags
	 */
	public static ArrayList<String> getTags()
	{
		HttpGet httpget = new HttpGet(initUrl + "tags.json");
		ArrayList<String> result = new ArrayList<String>();
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
					JSONObject tag = res.getJSONObject(i);
					result.add(tag.getString("name"));
					Log.i("RESPONSE", "4");
				}
				Log.i("RESPONSE", "5");
			}
		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}
		return result;
	}

	public static void addAdmin(String groupId, String gmailAddress, String newAdminMail)
	{
		HttpPost httppost = new HttpPost(initUrl + "groups/" + groupId + "/admins.json");

		try
		{
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("phone_id", gmailAddress));
			nameValuePairs.add(new BasicNameValuePair("new_phone_id", newAdminMail));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = client.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			final String response_str = EntityUtils.toString(resEntity);
			Log.i("RESPONSE", response_str);

		} catch (Exception ex)
		{
			Log.e("Game", "error: " + ex.getMessage(), ex);
		}

	}

	/**
	 * Deletes the deadline from the server if that mail is an admin returns
	 * true if deletion is successful
	 */
	public static boolean deleteDeadline(Deadline deadline, String gmailAddress, String groupId,
			String groupName)
	{
		HttpDelete httpdelete = new HttpDelete(initUrl + "group/" + groupId + "/deadlines/" + deadline.getWebId() + ".json?phone_id=" + gmailAddress);
		
		try
		{
			// Execute HTTP Delete Request
			HttpResponse response = client.execute(httpdelete);
			HttpEntity resEntity = response.getEntity();
			final String response_str = EntityUtils.toString(resEntity);
			Log.i("RESPONSE", response_str);
			return (response.getStatusLine().getStatusCode() == 201);
		} catch (Exception ex)
		{
			Log.e("Game", "error: " + ex.getMessage(), ex);
		}
		return false;
	}

}
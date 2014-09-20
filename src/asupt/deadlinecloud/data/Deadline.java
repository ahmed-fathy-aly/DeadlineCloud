package asupt.deadlinecloud.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

public class Deadline
{

	/* static stuff */
	public static enum Priorirty
	{
		HIGH, MEDIUM, LOW;
	}

	public static final int highPriorityColor = 0;

	public static String localString = "Local";
	public static int HIGH_COLOR = 0xaaff4444;
	public static int MID_COLOR = 0xaaffbb33;
	public static int LOW_COLOR = 0xaa99cc00;

	/* member variables */
	private String title;
	private String description;
	private Priorirty priority;
	private Calendar calendar;
	private String groupName;
	private long databaseId;
	private String webId;
	private String groupId;

	/* Constructors */
	public Deadline()
	{
		this.title = "";
		this.description = "";
		this.priority = Priorirty.LOW;
		this.groupName = "";
		this.calendar = new GregorianCalendar();
	}
	
	public String getGroupId() {
		return this.groupId;
	}
	
	public void SetFromWeb(Bundle d)
	{
		try
		{
			Calendar t = Calendar.getInstance();
			t.setTimeInMillis(Long.parseLong(d.getString("utc_time")));
			t.set(Calendar.YEAR, t.get(Calendar.YEAR) - 1900);
			this.calendar = t;
			this.description = d.getString("description");
			this.setWebPriority(d.getInt("priority"));
			this.title = d.getString("name");
			this.groupName = d.getString("group_name");
			this.webId = d.getString("id");
			this.groupId = d.getString("group_id");
		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}
	}

	public void SetFromWeb(JSONObject d)
	{
		try
		{
			Calendar t = Calendar.getInstance();
			t.setTimeInMillis(d.getLong("utc_time"));
			t.set(Calendar.YEAR, t.get(Calendar.YEAR) - 1900);
			this.calendar = t;
			this.description = d.getString("description");
			this.setWebPriority(d.getInt("priority"));
			this.title = d.getString("name");
			this.groupName = d.getString("group_name");
			this.webId = d.getString("id");
			this.groupId = d.getString("group_id");
		} catch (Exception ex)
		{
			Log.e("Debug", "error: " + ex.getMessage(), ex);
		}
	}

	/* getters and setter */
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Priorirty getPriority()
	{
		return priority;
	}

	public void setPriority(Priorirty priority)
	{
		this.priority = priority;
	}

	public String getGroupName()
	{
		return groupName;
	}

	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}

	public Calendar getCalendar()
	{
		return this.calendar;
	}

	public void setCalendar(Calendar calendar)
	{
		this.calendar = calendar;
	}

	public long getDatabaseId()
	{
		return databaseId;
	}

	public void setDatabaseId(long databaseId)
	{
		this.databaseId = databaseId;
	}

	public String getWebId()
	{
		return webId;
	}

	public void setWebId(String webId)
	{
		this.webId = webId;
	}

	/* Methods */
	public String toString()
	{
		StringBuilder str = new StringBuilder();

		str.append("Title : " + this.title + "\n");
		str.append("Descrition : " + this.description + "\n");
		str.append("Priority : " + this.priority + "\n");
		str.append("Group : " + this.groupName + "\n");
		str.append("Date : " + this.calendar.toString() + "\n");

		return str.toString();
	}

	public int getRemainingDays()
	{
		Calendar nowC = new GregorianCalendar();
		nowC.set(Calendar.YEAR, nowC.get(Calendar.YEAR) - 1900);

		Date now = nowC.getTime();
		Date then = calendar.getTime();

		Log.e("Game", "now" + now.toString());
		Log.e("Game", "then" + then.toString());

		long diff = then.getTime() - now.getTime();
		int daysRem = (int) (diff / (24 * 60 * 60 * 1000));
		if (daysRem < 0)
			daysRem = 0;
		return daysRem;
	}

	public int getWebPriority()
	{
		if (priority.equals(Priorirty.HIGH))
			return 3;
		else if (priority.equals(Priorirty.MEDIUM))
			return 2;
		else
			return 1;
	}

	public void setWebPriority(int p)
	{
		if (p == 3)
			this.priority = Priorirty.HIGH;
		else if (p == 2)
			this.priority = Priorirty.MEDIUM;
		else
			this.priority = Priorirty.LOW;
	}

	public static void main(String[] args)
	{
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.YEAR, 2014 - 1900);
		calendar.set(Calendar.MONTH, 9);
		calendar.set(Calendar.DAY_OF_MONTH, 9);

		Deadline deadline = new Deadline();
		deadline.setCalendar(calendar);

		System.out.println(deadline.getRemainingDays());

	}

}

package asupt.deadlinecloud.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
	public static int   HIGH_COLOR = 0xaaff4444;
	public static int   MID_COLOR = 0xaaffbb33;
	public static int   LOW_COLOR = 0xaa99cc00;
	
	
	/* member variables */
	private String title;
	private String description;
	private Priorirty priority;
	private Calendar calendar;
	private String groupName;

	/* Constructors */
	public Deadline()
	{
		this.title = "";
		this.description = "";
		this.priority = Priorirty.LOW;
		this.groupName = "";
		this.calendar = new GregorianCalendar();
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
	
public void setDate(int year, int month, int day)
	{
		GregorianCalendar calendar = new GregorianCalendar(year, month-1, day, 0, 0, 0);
		Log.e("Game", calendar.toString());
		this.calendar  = calendar;
		
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
		Date now = new Date();
		Date then = calendar.getTime();
		long diff = then.getTime() - now.getTime();
		int daysRem = (int) (diff / (24 * 60 * 60 * 1000));
		if (daysRem < 0)
			daysRem = 0;
		return daysRem;
	}

	public static void main(String[] args)
	{
		Deadline deadline = new Deadline();
		deadline.setTitle("Mesa7a");
		deadline.setDescription("bla bla bla");
		deadline.setGroupName(Deadline.localString);
		deadline.setDate(2014, 8, 15);
		System.out.println(deadline);

	}

}

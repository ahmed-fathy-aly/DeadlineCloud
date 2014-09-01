package asupt.deadlinecloud.data;

import java.util.Calendar;

public class Reminder
{
	private Deadline deadline;
	private Calendar calendar;
	private int id;

	public Reminder()
	{
	}

	public Deadline getDeadline()
	{
		return deadline;
	}

	public void setDeadline(Deadline deadline)
	{
		this.deadline = deadline;
	}

	public Calendar getCalendar()
	{
		return calendar;
	}

	public void setCalendar(Calendar calendar)
	{
		this.calendar = calendar;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

}

package asupt.deadlinecloud.data;

public class Group
{
	private String name;
	private String id;
	private String graduationYear;
	private String department;
	private String tag;
	private String descirption;
	private int numberOfSubscribers;
	private long databaseId;

	public Group(String name, String id, int numberOfSubscribers)
	{
		this.name = name;
		this.id = id;
		this.numberOfSubscribers = numberOfSubscribers;
	}

	public Group()
	{
		this.name = "NA";
		this.id = "NA";
		this.numberOfSubscribers = 0;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public int getNumberOfSubscribers()
	{
		return numberOfSubscribers;
	}

	public void setNumberOfSubscribers(int numberOfSubscribers)
	{
		this.numberOfSubscribers = numberOfSubscribers;
	}

	public long getDatabaseId()
	{
		return databaseId;
	}

	public void setDatabaseId(long databaseId)
	{
		this.databaseId = databaseId;
	}

	public String getGraduationYear()
	{
		return graduationYear;
	}

	public void setGraduationYear(String graduationYear)
	{
		this.graduationYear = graduationYear;
	}

	public String getDepartment()
	{
		return department;
	}

	public void setDepartment(String department)
	{
		this.department = department;
	}

	public String getTag()
	{
		return tag;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	public String getDescirption()
	{
		return descirption;
	}

	public void setDescirption(String descirption)
	{
		this.descirption = descirption;
	}

}

package asupt.deadlinecloud.data;

public class Group
{
	private String name;
	private String id;
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
	
	
	
	
	
	
	
	
	
}

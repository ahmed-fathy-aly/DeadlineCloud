package asupt.deadlinecloud.web;

import java.util.ArrayList;

import asupt.deadlinecloud.data.Deadline;
import asupt.deadlinecloud.data.Group;
import asupt.deadlinecloud.data.Deadline.Priorirty;

public class WebMinion
{

	/**
	 * @return a list of all groups including each groups's name, id, number of subscribers
	 */
	public static ArrayList<Group> getAllGroups()
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
	
	/**
	 * try to subscribe to the group with that id and asks the server to increment the number of subscribers
	 */
	public static void subscribe(String groupId)
	{
		
	}
	
	/**
	 * asks the server to add a deadline to a certain group
	 */
	public static void postDeadline(String groupId, String userId, Deadline deadline)
	{
		
	}
	
	/**
	 * returns all the deadlines in the group with that id
	 */
	public static ArrayList<Deadline> getAllDeadlines(String groupId)
	{
		ArrayList<Deadline> deadlines = new ArrayList<Deadline>();
		
		Deadline deadline1 = new Deadline();
		deadline1.setTitle("Mesa7a");
		deadline1.setDescription("Draw some pretty useless stuff and get really bored with them");
		deadline1.setGroupName("College");
		deadline1.setPriority(Priorirty.HIGH);
		deadline1.setDate(2014, 8, 28);
		deadlines.add(deadline1);
		
		Deadline deadline2 = new Deadline();
		deadline2.setTitle("Structure");
		deadline2.setDescription("load some stuff");
		deadline2.setGroupName("College");
		deadline2.setPriority(Priorirty.HIGH);
		deadline2.setDate(2014, 8, 25);
		deadlines.add(deadline2);
		
		Deadline deadline3 = new Deadline();
		deadline3.setTitle("CS");
		deadline3.setDescription("load some stuff");
		deadline3.setGroupName("College");
		deadline3.setPriority(Priorirty.LOW);
		deadline3.setDate(2014, 8, 25);
		deadlines.add(deadline3);
		
		Deadline deadline4 = new Deadline();
		deadline4.setTitle("Sleep");
		deadline4.setDescription("load some stuff");
		deadline4.setGroupName("College");
		deadline4.setPriority(Priorirty.MEDIUM);
		deadline4.setDate(2014, 8, 23);
		deadlines.add(deadline4);
		
		return deadlines;
	}
	
	
}

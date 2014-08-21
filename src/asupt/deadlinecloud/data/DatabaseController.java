package asupt.deadlinecloud.data;


import java.util.ArrayList;

import asupt.deadlinecloud.data.Deadline.Priorirty;

public class DatabaseController
{

	public ArrayList<Deadline> getAllDeadlines()
	{
		ArrayList<Deadline> result = new ArrayList<Deadline>();
		
		Deadline deadline1 = new Deadline();
		deadline1.setTitle("Mesa7a");
		deadline1.setDescription("Draw some pretty useless stuff and get really bored with them");
		deadline1.setGroupName("College");
		deadline1.setPriority(Priorirty.HIGH);
		deadline1.setDate(2014, 8, 28);
		result.add(deadline1);
		
		Deadline deadline2 = new Deadline();
		deadline2.setTitle("Structure");
		deadline2.setDescription("load some stuff");
		deadline2.setGroupName("College");
		deadline2.setPriority(Priorirty.HIGH);
		deadline2.setDate(2014, 8, 25);
		result.add(deadline2);
		
		Deadline deadline3 = new Deadline();
		deadline3.setTitle("CS");
		deadline3.setDescription("load some stuff");
		deadline3.setGroupName("College");
		deadline3.setPriority(Priorirty.LOW);
		deadline3.setDate(2014, 8, 25);
		result.add(deadline3);
		
		Deadline deadline4 = new Deadline();
		deadline4.setTitle("Sleep");
		deadline4.setDescription("load some stuff");
		deadline4.setGroupName("College");
		deadline4.setPriority(Priorirty.MEDIUM);
		deadline4.setDate(2014, 8, 23);
		result.add(deadline4);
		
		
		return result;
		
	}
	
}

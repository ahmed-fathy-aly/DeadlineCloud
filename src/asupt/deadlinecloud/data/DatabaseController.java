package asupt.deadlinecloud.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import asupt.deadlinecloud.data.Deadline.Priorirty;

public class DatabaseController extends SQLiteOpenHelper
{
	/* Data base constants */
	private static final String DATABASE_NAME = "asuptDeadlineCloudDatabase";
	private static final int DATABASE_VERSION = 17;

	/* Deadline table */
	private static final String DEADLINES_TABLE_NAME = "deadlinesTable";
	private static final String KEY_DEADLINE_ID = "deadlineId";
	private static final String KEY_DEADLINE_TITLE = "deadlineTitle";
	private static final String KEY_DEADLINE_GROUP = "deadlineGroup";
	private static final String KEY_DEADLINE_DESCRIPTION = "deadlineDescription";
	private static final String KEY_DAY = "deadlineDay";
	private static final String KEY_MONTH = "deadlineMonth";
	private static final String KEY_YEAR = "deadlineYear";
	private static final String KEY_PRIORITY = "deadlinePriority";
	private static final String KEY_DEADLINE_GROUP_ID = "deadlineGroupId";
	private static final String KEY_DEADLINE_WEB_ID = "deadlineWebId";
	private static final String KEY_DEADLINE_IN_MY_DEADLINES = "deadlineInMYDeadlines";

	/* Groups Table */
	private static final String GROUPS_TABLE_NAME = "groupsTable";
	private static final String KEY_Group_DATABASE_ID = "groupDatabaseId";
	private static final String KEY_Group_NAME = "groupName";
	private static final String KEY_Group_ID = "groupID";
	private static final String KEY_Group_YEAR = "groupYear";
	private static final String KEY_Group_DEPARTMENT = "groupDepartment";
	private static final String KEY_Group_TAG = "groupTag";

	public DatabaseController(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		String CREATE_DEADLINES_TABLE = "CREATE TABLE IF NOT EXISTS " + DEADLINES_TABLE_NAME + "("
				+ KEY_DEADLINE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
				+ KEY_DEADLINE_IN_MY_DEADLINES + " Integer," + KEY_DEADLINE_WEB_ID + " TEXT,"
				+ KEY_DEADLINE_GROUP_ID + " TEXT," + KEY_DEADLINE_TITLE + " TEXT,"
				+ KEY_DEADLINE_GROUP + " TEXT," + KEY_DEADLINE_DESCRIPTION + " TEXT,"
				+ KEY_PRIORITY + " TEXT," + KEY_YEAR + " TEXT," + KEY_MONTH + " TEXT," + KEY_DAY
				+ " TEXT" + ")";

		String CREATE_GROUPS_TABLE = "CREATE TABLE IF NOT EXISTS " + GROUPS_TABLE_NAME + "("
				+ KEY_Group_DATABASE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
				+ KEY_Group_ID + " TEXT," + KEY_Group_YEAR + " TEXT," + KEY_Group_DEPARTMENT
				+ " TEXT," + KEY_Group_TAG + " TEXT," + KEY_Group_NAME + " TEXT" + ")";

		db.execSQL(CREATE_DEADLINES_TABLE);
		db.execSQL(CREATE_GROUPS_TABLE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2)
	{
		db.execSQL("DROP TABLE IF EXISTS " + DEADLINES_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + GROUPS_TABLE_NAME);

		onCreate(db);
	}

	/* Deadlines */

	public ArrayList<Deadline> getAllDeadlines()
	{
		ArrayList<Deadline> result = new ArrayList<Deadline>();

		// get all the deadline
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from " + DEADLINES_TABLE_NAME, null);

		if (cursor.moveToFirst())
		{
			while (!cursor.isAfterLast())
			{
				// get the deadline info
				Deadline deadline = new Deadline();

				deadline.setTitle(cursor.getString(cursor.getColumnIndex(KEY_DEADLINE_TITLE)));
				deadline.setDescription(cursor.getString(cursor
						.getColumnIndex(KEY_DEADLINE_DESCRIPTION)));
				deadline.setGroupName(cursor.getString(cursor.getColumnIndex(KEY_DEADLINE_GROUP)));
				deadline.setWebId(cursor.getString(cursor.getColumnIndex(KEY_DEADLINE_WEB_ID)));
				deadline.setGroupId(cursor.getString(cursor.getColumnIndex(KEY_DEADLINE_GROUP_ID)));
				deadline.setInMyDeadlines(cursor.getInt(cursor
						.getColumnIndex(KEY_DEADLINE_IN_MY_DEADLINES)));

				String year, month, day;
				year = cursor.getString(cursor.getColumnIndex(KEY_YEAR));
				month = cursor.getString(cursor.getColumnIndex(KEY_MONTH));
				day = cursor.getString(cursor.getColumnIndex(KEY_DAY));

				Calendar calendar = new GregorianCalendar();
				calendar.set(Calendar.YEAR, Integer.parseInt(year));
				calendar.set(Calendar.MONTH, Integer.parseInt(month));
				calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
				deadline.setCalendar(calendar);

				deadline.setWebPriority(Integer.parseInt(cursor.getString(cursor
						.getColumnIndex(KEY_PRIORITY))));
				deadline.setDatabaseId(cursor.getInt(cursor.getColumnIndex(KEY_DEADLINE_ID)));

				result.add(deadline);

				cursor.moveToNext();
			}
		}

		return result;

	}

	public void addDedaline(Deadline deadline)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		// write the deadline
		ContentValues entry = new ContentValues();
		entry.put(KEY_DEADLINE_TITLE, deadline.getTitle());
		entry.put(KEY_DEADLINE_GROUP, deadline.getGroupName());
		entry.put(KEY_DEADLINE_IN_MY_DEADLINES, deadline.getInMyDeadlines());
		entry.put(KEY_DEADLINE_DESCRIPTION, deadline.getDescription());
		entry.put(KEY_PRIORITY, "" + deadline.getWebPriority());
		entry.put(KEY_YEAR, "" + deadline.getCalendar().get(Calendar.YEAR));
		entry.put(KEY_MONTH, "" + deadline.getCalendar().get(Calendar.MONTH));
		entry.put(KEY_DAY, "" + deadline.getCalendar().get(Calendar.DAY_OF_MONTH));
		entry.put(KEY_DEADLINE_GROUP_ID, deadline.getGroupId());
		entry.put(KEY_DEADLINE_WEB_ID, deadline.getWebId());
		entry.put(KEY_DAY, "" + deadline.getCalendar().get(Calendar.DAY_OF_MONTH));

		// insert and wrap it up
		long id = db.insert(DEADLINES_TABLE_NAME, null, entry);
		deadline.setDatabaseId(id);
		db.close();
	}

	public void deleteDeadline(Deadline deadline)
	{
		getWritableDatabase().delete(DEADLINES_TABLE_NAME,
				KEY_DEADLINE_ID + "=" + deadline.getDatabaseId(), null);
	}

	public ArrayList<Deadline> getGroupDeadlines(String groupName)
	{
		ArrayList<Deadline> result = new ArrayList<Deadline>();

		ArrayList<Deadline> allDeadlines = getAllDeadlines();
		for (Deadline deadline : allDeadlines)
			if (deadline.getGroupName().equals(groupName))
				result.add(deadline);

		return result;
	}

	public ArrayList<Deadline> getMyDeadlines()
	{
		// filter the deadlines which are marked as my deadlines
		ArrayList<Deadline> result = new ArrayList<Deadline>();

		for (Deadline deadline : getAllDeadlines())
			if (deadline.getInMyDeadlines() == 1)
				result.add(deadline);

		return result;
	}

	public void addToMyDeadlines(Deadline deadline)
	{
		ContentValues entry = new ContentValues();
		entry.put(KEY_DEADLINE_IN_MY_DEADLINES, 1);
		getWritableDatabase().update(DEADLINES_TABLE_NAME, entry,
				KEY_DEADLINE_ID + "=" + deadline.getDatabaseId(), null);
	}

	public void addToMyDeadlines(String deadlineWebId)
	{

		// find the deadline
		Deadline newDeadline = null;
		ArrayList<Deadline> allDeadlines = getAllDeadlines();

		for (Deadline deadline : allDeadlines)
			if (deadline.getWebId() != null && deadline.getWebId().equals(deadlineWebId))
				newDeadline = deadline;

		if (newDeadline != null)
			addToMyDeadlines(newDeadline);

	}

	public void removeFromMyDeadlines(Deadline deadline)
	{
		ContentValues entry = new ContentValues();
		entry.put(KEY_DEADLINE_IN_MY_DEADLINES, 0);
		getWritableDatabase().update(DEADLINES_TABLE_NAME, entry,
				KEY_DEADLINE_ID + "=" + deadline.getDatabaseId(), null);
	}

	/* Reminders */
	public ArrayList<Reminder> getAllReminders()
	{
		ArrayList<Reminder> result = new ArrayList<Reminder>();

		return result;
	}

	public void addReminder(Reminder reminder)
	{
		// TODO Auto-generated method stub

	}

	/* Groups */
	public ArrayList<Group> getAllGroups()
	{
		ArrayList<Group> groups = new ArrayList<Group>();

		// get all the groups
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from " + GROUPS_TABLE_NAME, null);

		if (cursor.moveToFirst())
		{
			while (!cursor.isAfterLast())
			{
				// get the group info
				Group group = new Group();

				group.setDatabaseId(cursor.getInt(cursor.getColumnIndex(KEY_Group_DATABASE_ID)));
				group.setId(cursor.getString(cursor.getColumnIndex(KEY_Group_ID)));
				group.setName(cursor.getString(cursor.getColumnIndex(KEY_Group_NAME)));
				group.setGraduationYear(cursor.getString(cursor.getColumnIndex(KEY_Group_YEAR)));
				group.setDepartment(cursor.getString(cursor.getColumnIndex(KEY_Group_DEPARTMENT)));
				group.setTag(cursor.getString(cursor.getColumnIndex(KEY_Group_TAG)));

				groups.add(group);
				cursor.moveToNext();
			}
		}

		return groups;
	}

	public void addGroup(Group group)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		// write the group
		ContentValues entry = new ContentValues();
		entry.put(KEY_Group_ID, group.getId());
		entry.put(KEY_Group_NAME, group.getName());
		entry.put(KEY_Group_YEAR, group.getGraduationYear());
		entry.put(KEY_Group_DEPARTMENT, group.getDepartment());
		entry.put(KEY_Group_TAG, group.getTag());

		// insert and wrap it up
		long id = db.insert(GROUPS_TABLE_NAME, null, entry);
		group.setDatabaseId(id);
		db.close();
	}

	public void deleteGroup(Group group)
	{
		getWritableDatabase().delete(GROUPS_TABLE_NAME,
				KEY_Group_DATABASE_ID + "=" + group.getDatabaseId(), null);
	}

}

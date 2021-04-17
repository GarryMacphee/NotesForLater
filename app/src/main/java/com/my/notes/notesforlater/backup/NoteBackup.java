package com.my.notes.notesforlater.backup;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.my.notes.notesforlater.data.NotesForLaterProviderContract;

public class NoteBackup
{
	public static final String ALL_COURSES = "all_courses";
	private static final String TAG = "NoteBackup";

	public static void doBackup(Context context, String backupCourseId)
	{
		String[] columns = {
				NotesForLaterProviderContract.Notes.COLUMNS_COURSES_ID,
				NotesForLaterProviderContract.Notes.COLUMN_NOTE_TITLE,
				NotesForLaterProviderContract.Notes.COLUMN_NOTE_TEXT
		};


		String selection = null;
		String[] selectionArgs = null;

		if (!backupCourseId.equals((ALL_COURSES)))
		{
			selection = NotesForLaterProviderContract.Notes.COLUMNS_COURSES_ID + " = ?";
			selectionArgs = new String[]{backupCourseId};
		}

		Cursor cursor = context.getContentResolver()
							   .query(NotesForLaterProviderContract.Notes.CONTENT_URI, columns, selection, selectionArgs, null);

		int courseIdPos = cursor
				.getColumnIndex(NotesForLaterProviderContract.Notes.COLUMNS_COURSES_ID);
		int noteTitlePos = cursor
				.getColumnIndex(NotesForLaterProviderContract.Notes.COLUMN_NOTE_TITLE);
		int noteTextPos = cursor
				.getColumnIndex(NotesForLaterProviderContract.Notes.COLUMN_NOTE_TEXT);


		Log.i(TAG, ">>>*** BACKUP START - Thread: " + Thread.currentThread().getId() + " ***<<<");

		while (cursor.moveToNext())
		{
			String courseId = cursor.getString(courseIdPos);
			String noteTitle = cursor.getString(noteTitlePos);
			String noteText = cursor.getString(noteTextPos);

			if (!noteTitle.isEmpty())
			{
				Log.i(TAG, ">>>Backing up Note<<< : courseId:" + courseId + " noteTitle: " + noteTitle + " noteText: " + noteText);
				simulateLongRunningTask();
			}
		}
		Log.i(TAG, ">>>*** BACKUP COMPLETE ***<<<");
	}

	private static void simulateLongRunningTask()
	{
		try
		{
			Thread.sleep(1000);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}

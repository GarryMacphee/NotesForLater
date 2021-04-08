package com.my.notes.notesforlater;

import android.net.Uri;
import android.provider.BaseColumns;


public final class NotesForLaterProviderContract
{
	private NotesForLaterProviderContract()
	{
	}

	public static final String AUTHORITY = "com.my.notes.notesforlater.provider";
	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);


	protected interface CoursesIdColumns
	{
		String COLUMNS_COURSES_ID = "course_id";
	}

	protected interface CoursesColumns
	{
		String COLUMN_COURSE_TITLE = "course_title";
	}

	protected interface NotesColumns
	{
		String COLUMN_NOTE_TITLE = "note_title";
		String COLUMN_NOTE_TEXT = "note_text";
	}


	public static final class Courses implements BaseColumns, CoursesColumns, CoursesIdColumns
	{
		public static final String PATH = "courses";
		public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
	}

	public static final class Notes implements BaseColumns, NotesColumns, CoursesIdColumns
	{
		public static final String PATH = "notes";
		public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
		public static final String PATH_EXPANDED = "notes_expanded";
		public static final Uri CONTENT_EXPANDED_URI = Uri
				.withAppendedPath(AUTHORITY_URI, PATH_EXPANDED);
	}
}

package com.my.notes.notesforlater.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.my.notes.notesforlater.data.NotesForLaterDatabaseContract.CourseInfoEntry;
import static com.my.notes.notesforlater.data.NotesForLaterDatabaseContract.NoteInfoEntry;

public class NotesProvider extends ContentProvider
{
	private NotesForLaterDBHelper mNotesForLaterDBHelper;
	public static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final String MIME_VENDOR_TYPE = "vnd." + NotesForLaterProviderContract.AUTHORITY + ".";

	private static final int COURSES = 0;
	private static final int NOTES = 1;
	private static final int NOTES_EXPANDED = 2;
	private static final int NOTES_ROW = 3;
	private static final int COURSES_ROW = 4;
	private static final int NOTES_EXPANDED_ROW = 5;

	static
	{
		sUriMatcher
				.addURI(NotesForLaterProviderContract.AUTHORITY, NotesForLaterProviderContract.Courses.PATH, COURSES);
		sUriMatcher
				.addURI(NotesForLaterProviderContract.AUTHORITY, NotesForLaterProviderContract.Notes.PATH, NOTES);
		sUriMatcher
				.addURI(NotesForLaterProviderContract.AUTHORITY, NotesForLaterProviderContract.Notes.PATH_EXPANDED, NOTES_EXPANDED);
		sUriMatcher
				.addURI(NotesForLaterProviderContract.AUTHORITY, NotesForLaterProviderContract.Courses.PATH + "/#", COURSES_ROW);
		sUriMatcher
				.addURI(NotesForLaterProviderContract.AUTHORITY, NotesForLaterProviderContract.Notes.PATH + "/#", NOTES_ROW);
		sUriMatcher
				.addURI(NotesForLaterProviderContract.AUTHORITY, NotesForLaterProviderContract.Notes.PATH_EXPANDED + "/#", NOTES_EXPANDED_ROW);
	}

	public NotesProvider()
	{
	}

	@Override
	public boolean onCreate()
	{
		mNotesForLaterDBHelper = new NotesForLaterDBHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		long rowId = -1;
		String rowSelection = null;
		String[] rowSelectionArgs = null;
		int nRows = -1;
		SQLiteDatabase db = mNotesForLaterDBHelper.getReadableDatabase();

		int uriMatch = sUriMatcher.match(uri);
		switch (uriMatch)
		{
			case COURSES:
				nRows = db.delete(CourseInfoEntry.TABLE_NAME, selection, selectionArgs);
				break;
			case NOTES:
				nRows = db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
				break;
			case NOTES_EXPANDED:
				// throw exception - read-only table
			case COURSES_ROW:
				rowId = ContentUris.parseId(uri);
				rowSelection = CourseInfoEntry._ID + " = ?";
				rowSelectionArgs = new String[]{Long.toString(rowId)};
				nRows = db.delete(CourseInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
				break;
			case NOTES_ROW:
				rowId = ContentUris.parseId(uri);
				rowSelection = NoteInfoEntry._ID + " = ?";
				rowSelectionArgs = new String[]{Long.toString(rowId)};
				nRows = db.delete(NoteInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
				break;
			case NOTES_EXPANDED_ROW:
				// throw exception - read-only table
				break;
		}

		return nRows;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		SQLiteDatabase db = mNotesForLaterDBHelper.getWritableDatabase();
		long rowId = -1;
		Uri rowUri = null;

		int uriMatch = sUriMatcher.match(uri);

		switch (uriMatch)
		{
			case NOTES:
				rowId = db.insert(NoteInfoEntry.TABLE_NAME, null, values);
				//content://com.my.notes.notesforlater.provider/notes/1
				rowUri = ContentUris
						.withAppendedId(NotesForLaterProviderContract.Notes.CONTENT_URI, rowId);
				break;
			case COURSES:
				rowId = db.insert(CourseInfoEntry.TABLE_NAME, null, values);
				rowUri = ContentUris
						.withAppendedId(NotesForLaterProviderContract.Courses.CONTENT_URI, rowId);
				break;
			case NOTES_EXPANDED:
				//throw new Exception(new Throwable());
				break;
		}

		return rowUri;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		long rowId = -1;
		String rowSelection = null;
		String[] rowSelectionArgs = null;
		int nRows = -1;
		SQLiteDatabase db = mNotesForLaterDBHelper.getReadableDatabase();

		int uriMatch = sUriMatcher.match(uri);
		switch (uriMatch)
		{
			case COURSES:
				nRows = db.update(CourseInfoEntry.TABLE_NAME, values, selection, selectionArgs);
				break;
			case NOTES:
				nRows = db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
				break;
			case NOTES_EXPANDED:
				// throw exception - read-only table
			case COURSES_ROW:
				rowId = ContentUris.parseId(uri);
				rowSelection = CourseInfoEntry._ID + " = ?";
				rowSelectionArgs = new String[]{Long.toString(rowId)};
				nRows = db
						.update(CourseInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
				break;
			case NOTES_ROW:
				rowId = ContentUris.parseId(uri);
				rowSelection = NoteInfoEntry._ID + " = ?";
				rowSelectionArgs = new String[]{Long.toString(rowId)};
				nRows = db.update(NoteInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
				break;
			case NOTES_EXPANDED_ROW:
				// throw exception - read-only table
				break;
		}

		return nRows;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		Cursor cursor = null;
		SQLiteDatabase db = mNotesForLaterDBHelper.getReadableDatabase();

		int uriMatch = sUriMatcher.match(uri);
		switch (uriMatch)
		{
			case COURSES:
				cursor = db
						.query(CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
				break;
			case NOTES:
				cursor = db
						.query(NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
				break;
			case NOTES_EXPANDED:
				cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder);
				break;
			case NOTES_ROW:
				long rowId = ContentUris.parseId(uri);
				String rowSelection = NoteInfoEntry._ID + " = ?";
				String[] rowSelectionArgs = new String[]{Long.toString(rowId)};
				cursor = db
						.query(NoteInfoEntry.TABLE_NAME, projection, rowSelection, rowSelectionArgs, null, null, sortOrder);
				break;
			case NOTES_EXPANDED_ROW:
				rowId = ContentUris.parseId(uri);
				rowSelection = NoteInfoEntry.getQName(NoteInfoEntry._ID) + " = ?";
				rowSelectionArgs = new String[]{Long.toString(rowId)};
				cursor = notesExpandedQuery(db, projection, rowSelection, rowSelectionArgs, null);
				break;
		}
		return cursor;
	}

	@Override
	public String getType(Uri uri)
	{
		String mimeType = null;
		int uriMatch = sUriMatcher.match(uri);
		switch (uriMatch)
		{
			case COURSES:
				//content://com.my.notes.notesforlater.provider.courses
				mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
						MIME_VENDOR_TYPE + NotesForLaterProviderContract.Courses.PATH;
				break;
			case NOTES:
				mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NotesForLaterProviderContract.Notes.PATH;
				break;
			case NOTES_EXPANDED:
				mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NotesForLaterProviderContract.Notes.PATH_EXPANDED;
				break;
			case COURSES_ROW:
				mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NotesForLaterProviderContract.Courses.PATH;
				break;
			case NOTES_ROW:
				mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NotesForLaterProviderContract.Notes.PATH;
				break;
			case NOTES_EXPANDED_ROW:
				mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NotesForLaterProviderContract.Notes.PATH_EXPANDED;
				break;
		}
		return mimeType;
	}

	private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		String[] columns = new String[projection.length];
		for (int idx = 0, idy = projection.length; idx < idy; idx++)
		{
			columns[idx] = projection[idx].equals(BaseColumns._ID) ||
					projection[idx]
							.equals(NotesForLaterProviderContract.CoursesIdColumns.COLUMNS_COURSES_ID) ?
					NoteInfoEntry.getQName(projection[idx]) : projection[idx];
		}


		String tableWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " +
				CourseInfoEntry.TABLE_NAME + " ON " +
				NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
				CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);

		return db.query(tableWithJoin, columns, selection, selectionArgs, null, null, sortOrder);
	}
}
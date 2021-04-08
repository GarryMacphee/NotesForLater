package com.my.notes.notesforlater;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class NotesProvider extends ContentProvider
{
	private NotesForLaterDBHelper mNotesForLaterDBHelper;

	public static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	private static final int COURSES = 0;
	private static final int NOTES = 1;
	private static final int NOTES_EXPANDED = 2;

	static
	{
		sUriMatcher
				.addURI(NotesForLaterProviderContract.AUTHORITY, NotesForLaterProviderContract.Courses.PATH, COURSES);
		sUriMatcher
				.addURI(NotesForLaterProviderContract.AUTHORITY, NotesForLaterProviderContract.Notes.PATH, NOTES);
		sUriMatcher
				.addURI(NotesForLaterProviderContract.AUTHORITY, NotesForLaterProviderContract.Notes.PATH_EXPANDED, NOTES_EXPANDED);
	}

	public NotesProvider()
	{
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		// Implement this to handle requests to delete one or more rows.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public String getType(Uri uri)
	{
		// TODO: Implement this to handle requests for the MIME type of the data
		// at the given URI.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		// TODO: Implement this to handle requests to insert a new row.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public boolean onCreate()
	{
		mNotesForLaterDBHelper = new NotesForLaterDBHelper(getContext());
		return true;
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
						.query(NotesForLaterDatabaseContract.CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
				break;
			case NOTES:
				cursor = db
						.query(NotesForLaterDatabaseContract.NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
				break;
		}
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs)
	{
		// TODO: Implement this to handle requests to update one or more rows.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
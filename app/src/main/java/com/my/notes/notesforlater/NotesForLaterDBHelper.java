package com.my.notes.notesforlater;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class NotesForLaterDBHelper extends SQLiteOpenHelper
{
	public static final String DATABASE_NAME = "NotesForLater.db";
	public static final int DATABASE_VERSION = 2;

	public NotesForLaterDBHelper(@Nullable Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(NotesForLaterDatabaseContract.CourseInfoEntry.SQL_CREATE_TABLE);
		db.execSQL(NotesForLaterDatabaseContract.NoteInfoEntry.SQL_CREATE_TABLE);

		db.execSQL(NotesForLaterDatabaseContract.CourseInfoEntry.SQL_CREATE_INDEX1);
		db.execSQL(NotesForLaterDatabaseContract.NoteInfoEntry.SQL_CREATE_INDEX1);

		DatabaseDataWorker worker = new DatabaseDataWorker(db);
		worker.insertCourses();
		worker.insertSampleNotes();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		if (oldVersion < 2)
		{
			db.execSQL((NotesForLaterDatabaseContract.CourseInfoEntry.SQL_CREATE_INDEX1));
			db.execSQL((NotesForLaterDatabaseContract.NoteInfoEntry.SQL_CREATE_INDEX1));
		}
	}
}

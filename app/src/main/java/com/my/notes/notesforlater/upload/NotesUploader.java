package com.my.notes.notesforlater.upload;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.my.notes.notesforlater.data.NotesForLaterProviderContract;

public class NotesUploader
{
	private final String TAG = getClass().getSimpleName();

	private final Context mContext;
	private boolean mCanceled;

	public NotesUploader(Context context)
	{
		mContext = context;
	}

	public boolean isCanceled()
	{
		return mCanceled;
	}

	public void cancel()
	{
		mCanceled = true;
	}

	public void doUpload(Uri dataUri)
	{
		String[] columns = {
				NotesForLaterProviderContract.Notes.COLUMNS_COURSES_ID,
				NotesForLaterProviderContract.Notes.COLUMN_NOTE_TITLE,
				NotesForLaterProviderContract.Notes.COLUMN_NOTE_TEXT
		};

		Uri uri;
		Cursor cursor = mContext.getContentResolver().query(dataUri, columns, null, null, null);

		int courseIdPos = cursor
				.getColumnIndex(NotesForLaterProviderContract.Notes.COLUMNS_COURSES_ID);
		int noteTitlePos = cursor
				.getColumnIndex(NotesForLaterProviderContract.Notes.COLUMN_NOTE_TITLE);
		int noteTextPos = cursor
				.getColumnIndex(NotesForLaterProviderContract.Notes.COLUMN_NOTE_TEXT);


		Log.d(TAG, ">>>*** UPLOAD START - " + dataUri + " ***<<<");
		while (!mCanceled && cursor.moveToNext())
		{
			String courseId = cursor.getString(courseIdPos);
			String noteTitle = cursor.getString(noteTitlePos);
			String noteText = cursor.getString(noteTextPos);

			if (!noteTitle.equals(""))
			{
				Log.d(TAG, ">>>Uploading Note<<< " + courseId + "|" + noteTitle + "|" + noteText);
			}

		}
		if (mCanceled)
		{
			Log.d(TAG, ">>>*** UPLOAD CANCELLED - " + dataUri + " ***<<<");
		}
		else
		{
			Log.d(TAG, ">>>*** UPLOAD FINISHED - " + dataUri + " ***<<<");
		}
	}
}

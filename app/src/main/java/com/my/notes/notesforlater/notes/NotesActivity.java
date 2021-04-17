package com.my.notes.notesforlater.notes;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.my.notes.notesforlater.R;
import com.my.notes.notesforlater.broadcastreceivers.CourseEventBroadcastHelper;
import com.my.notes.notesforlater.broadcastreceivers.NoteReminderReceiver;
import com.my.notes.notesforlater.courses.CourseInfo;
import com.my.notes.notesforlater.data.DataManager;
import com.my.notes.notesforlater.data.NotesForLaterDBHelper;
import com.my.notes.notesforlater.data.NotesForLaterProviderContract;

import static com.my.notes.notesforlater.data.NotesForLaterDatabaseContract.CourseInfoEntry;
import static com.my.notes.notesforlater.data.NotesForLaterDatabaseContract.NoteInfoEntry;

public class NotesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
	public static final String NOTE_ID = "com.my.notes.notesforlater.NOTE_POSITION";
	public static final String ORIGINAL_NOTE_COURSE_ID = "com.my.notes.notesforlater.ORIGINAL_NOTE_COURSE_ID";
	public static final String ORIGINAL_NOTE_TITLE = "com.my.notes.notesforlater.ORIGINAL_NOTE_TITLE";
	public static final String ORIGINAL_NOTE_TEXT = "com.my.notes.notesforlater.ORIGINAL_NOTE_TEXT";
	public static final int ID_NOT_SET = -1;
	public static final int LOADER_NOTES = 0;
	public static final int LOADER_COURSES = 1;
	private final String TAG = getClass().getSimpleName();
	private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
	private boolean mIsNewNote;
	private Spinner mSpinnerCourses;
	private EditText mTextNoteTitle;
	private EditText mTextNoteText;
	private int mNoteId;
	private boolean mIsCancelling;
	private String mOriginalNoteCourseId;
	private String mOriginalNoteTitle;
	private String mOriginalNoteText;
	private NoteActivityViewModel mViewModel;
	private NotesForLaterDBHelper mNotesForLaterDBHelper;
	private Cursor mCursor;
	private int mCourseIdPos;
	private int mNoteTitlePos;
	private int mNoteTextPos;
	private SimpleCursorAdapter mAdapterCourses;
	private boolean mCoursesQueryFinished;
	private boolean mNotesQueryFinished;
	private Uri mNoteUri;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notes);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mNotesForLaterDBHelper = new NotesForLaterDBHelper(this);

		mSpinnerCourses = findViewById(R.id.spinner_courses);

		ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
				ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
		mViewModel = viewModelProvider.get(NoteActivityViewModel.class);
		if (mViewModel.mIsNewlyCreated && savedInstanceState != null)
			mViewModel.restoreState(savedInstanceState);
		mViewModel.mIsNewlyCreated = false;


		mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
				new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
				new int[]{android.R.id.text1}, 0);
		mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerCourses.setAdapter(mAdapterCourses);

		//loadCourseData();

		getLoaderManager().initLoader(LOADER_COURSES, null, this);

		readDisplayStateValues();
		if (savedInstanceState == null)
		{
			saveOriginalNoteValues();
		}
		else
		{
			restoreOriginalNoteValues(savedInstanceState);
		}

		mTextNoteTitle = findViewById(R.id.text_note_title);
		mTextNoteText = findViewById(R.id.text_note_text);

		if (!mIsNewNote)
			//loadNoteData();

			getLoaderManager().initLoader(LOADER_NOTES, null, this);

		Log.d(TAG, "onCreate");
	}

	private void loadCourseData()
	{
		SQLiteDatabase db = mNotesForLaterDBHelper.getReadableDatabase();

		String[] courseColumns = {
				CourseInfoEntry.COLUMN_COURSE_TITLE,
				CourseInfoEntry.COLUMN_COURSE_ID,
				CourseInfoEntry._ID
		};

		Cursor cursor = db
				.query(CourseInfoEntry.TABLE_NAME, courseColumns, null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);

		mAdapterCourses.changeCursor(cursor);
	}


	private void showReminderNotification()
	{
		String noteTitle = mTextNoteTitle.getText().toString();
		String noteText = mTextNoteText.getText().toString();
		int noteId = (int) ContentUris.parseId(mNoteUri);

		Intent intent = new Intent(this, NoteReminderReceiver.class);
		intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle);
		intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText);
		intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, noteId);

		PendingIntent pendingIntent = PendingIntent
				.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		long currentTimeInMillis = SystemClock.elapsedRealtime();
		long oneHour = 60 * 60 * 1000;

		long tenSeconds = 10 * 60;

		long alarmTime = currentTimeInMillis + tenSeconds;

		alarmManager.set(AlarmManager.ELAPSED_REALTIME, alarmTime, pendingIntent);

	}

	private void readDisplayStateValues()
	{
		Intent intent = getIntent();
		mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
		mIsNewNote = mNoteId == ID_NOT_SET;
		if (mIsNewNote)
		{
			createNewNote();
		}

		//Log.i(TAG, "mNoteId: " + mNoteId);
	}

	private void saveOriginalNoteValues()
	{
		if (mIsNewNote)
			return;
		mOriginalNoteCourseId = mNote.getCourse().getCourseId();
		mOriginalNoteTitle = mNote.getTitle();
		mOriginalNoteText = mNote.getText();
	}

	private void restoreOriginalNoteValues(Bundle savedInstanceState)
	{
		mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
		mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
		mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
	}

	private void loadNoteData()
	{
		SQLiteDatabase db = mNotesForLaterDBHelper.getReadableDatabase();

		String courseId = "android_intents";
		String titleStart = "dynamic";

		String selection = NoteInfoEntry._ID + " = ?";

		String[] selectionArgs = {Integer.toString(mNoteId)};

		String[] noteColumns = {
				NoteInfoEntry.COLUMN_COURSE_ID,
				NoteInfoEntry.COLUMN_NOTE_TITLE,
				NoteInfoEntry.COLUMN_NOTE_TEXT
		};

		mCursor = db.query(NoteInfoEntry.TABLE_NAME,
				noteColumns,
				selection,
				selectionArgs,
				null, null, null
		);

		mCourseIdPos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
		mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
		mNoteTextPos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

		mCursor.moveToNext();
		displayNote();
	}

	@SuppressLint("StaticFieldLeak")
	private void createNewNote()
	{
		AsyncTask<ContentValues, Integer, Uri> task = new AsyncTask<ContentValues, Integer, Uri>()
		{
			private ProgressBar mProgressBar;

			@Override
			protected void onPreExecute()
			{
				mProgressBar = findViewById(R.id.mProgressBar);
				mProgressBar.setVisibility(View.VISIBLE);
				mProgressBar.setProgress(1);
			}

			@Override
			protected Uri doInBackground(ContentValues... params)
			{
				Log.d(TAG, "Call to execute - thread: " + Thread.currentThread().getId());
				ContentValues insertValues = params[0];
				Uri rowUri = getContentResolver()
						.insert(NotesForLaterProviderContract.Notes.CONTENT_URI, insertValues);

				simulateLongRunningWork();
				publishProgress(2);
				simulateLongRunningWork();
				publishProgress(3);

				return rowUri;
			}

			@Override
			protected void onProgressUpdate(Integer... values)
			{
				int progressValue = values[0];
				mProgressBar.setProgress(progressValue);
			}


			@Override
			protected void onPostExecute(Uri uri)
			{
				Log.d(TAG, "Call to execute - thread: " + Thread.currentThread().getId());
				mNoteUri = uri;
				mProgressBar.setVisibility(View.GONE);
				displaySnackBar(mNoteUri.toString());
			}
		};

		ContentValues values = new ContentValues();

		values.put(NotesForLaterProviderContract.Notes.COLUMNS_COURSES_ID, "");
		values.put(NotesForLaterProviderContract.Notes.COLUMN_NOTE_TITLE, "");
		values.put(NotesForLaterProviderContract.Notes.COLUMN_NOTE_TEXT, "");

		Log.d(TAG, "Call to execute - thread: " + Thread.currentThread().getId());

		task.execute(values);

		/*final Handler handler = new Handler();
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				mNoteUri = NotesActivity.this.getContentResolver().
						insert(NotesForLaterProviderContract.Notes.CONTENT_URI, values);
			}
		});*/

	}

	private void simulateLongRunningWork()
	{
		try
		{
			Thread.sleep(2000);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private void displaySnackBar(String message)
	{
		View view = findViewById(R.id.spinner_courses);
		Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
	}

	private void displayNote()
	{
		String courseId = mCursor.getString(mCourseIdPos);
		String noteTitle = mCursor.getString(mNoteTitlePos);
		String noteText = mCursor.getString(mNoteTextPos);

		int courseIndex = getIndexOfCourseId(courseId);

		mSpinnerCourses.setSelection(courseIndex);
		mTextNoteTitle.setText(noteTitle);
		mTextNoteText.setText(noteText);

		CourseEventBroadcastHelper.sendEventBroadcast(this, courseId, "");
	}

	private int getIndexOfCourseId(String courseId)
	{
		Cursor cursor = mAdapterCourses.getCursor();

		int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);

		int courseRowIndex = 0;

		boolean more = cursor.moveToFirst();

		while (more)
		{
			String cursorCourseId = cursor.getString(courseIdPos);

			if (courseId.equals(cursorCourseId))
			{
				break;
			}

			courseRowIndex++;
			more = cursor.moveToNext();
		}
		return courseRowIndex;
	}

	@Override
	protected void onDestroy()
	{
		mNotesForLaterDBHelper.close();
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (mIsCancelling)
		{
			Log.i(TAG, "Cancelling note at position: " + mNoteId);
			if (mIsNewNote)
			{
				deleteNoteFromDatabase();
			}
			else
			{
				storePreviousNoteValues();
			}
		}
		else
		{
			saveNote();
		}
		Log.d(TAG, "onPause");
	}

	@SuppressLint("StaticFieldLeak")
	private void deleteNoteFromDatabase()
	{
		final String selection = NoteInfoEntry._ID + " = ?";
		final String[] selectionArgs = {Integer.toString(mNoteId)};


		AsyncTask task = new AsyncTask()
		{
			@Override
			protected Object doInBackground(Object[] objects)
			{
				SQLiteDatabase db = mNotesForLaterDBHelper.getReadableDatabase();
				db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
				return null;
			}
		};
		task.execute();

	}

	private void storePreviousNoteValues()
	{
		CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
		mNote.setCourse(course);
		mNote.setTitle(mOriginalNoteTitle);
		mNote.setText(mOriginalNoteText);
	}

	private void saveNote()
	{
		String courseId = selectCourseId();
		String noteTitle = mTextNoteTitle.getText().toString();
		String noteText = mTextNoteText.getText().toString();
		saveNoteToDatabase(courseId, noteTitle, noteText);
	}

	private String selectedCourseId()
	{
		int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
		Cursor cursor = mAdapterCourses.getCursor();
		cursor.moveToPosition(selectedPosition);
		int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
		String courseId = cursor.getString(courseIdPos);
		return courseId;
	}

	private String selectCourseId()
	{
		Cursor cursor = mAdapterCourses.getCursor();
		cursor.moveToPosition(mSpinnerCourses.getSelectedItemPosition());
		int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);

		return cursor.getString(courseIdPos);
	}

	private void saveNoteToDatabase(String courseId, String noteTitle, String noteText)
	{
		ContentValues values = new ContentValues();
		values.put(NotesForLaterProviderContract.Notes.COLUMNS_COURSES_ID, courseId);
		values.put(NotesForLaterProviderContract.Notes.COLUMN_NOTE_TITLE, noteTitle);
		values.put(NotesForLaterProviderContract.Notes.COLUMN_NOTE_TEXT, noteText);

		getContentResolver().update(mNoteUri, values, null, null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (outState != null)
			mViewModel.saveState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_note, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_send_mail)
		{
			sendEmail();
			return true;
		}
		else if (id == R.id.action_cancel)
		{
			mIsCancelling = true;
			finish();
		}
		else if (id == R.id.action_next)
		{
			moveNext();
			return true;
		}
		else if (id == R.id.action_set_reminder)
		{
			showReminderNotification();
		}

		return super.onOptionsItemSelected(item);
	}

	private void sendEmail()
	{
		CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
		String subject = mTextNoteTitle.getText().toString();
		String text = "Checkout what I learned in the Pluralsight course \"" +
				course.getTitle() + "\"\n" + mTextNoteText.getText().toString();
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc2822");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, text);
		startActivity(intent);
	}

	private void moveNext()
	{
		saveNote();

		++mNoteId;
		mNote = DataManager.getInstance().getNotes().get(mNoteId);

		saveOriginalNoteValues();
		displayNote();
		invalidateOptionsMenu();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem item = menu.findItem(R.id.action_next);
		int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;

		item.setEnabled(mNoteId < lastNoteIndex);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		CursorLoader loader = null;

		if (id == LOADER_NOTES)
		{
			loader = createLoaderNotes();
		}
		else if (id == LOADER_COURSES)
		{
			loader = createLoaderCourses();
		}

		return loader;
	}

	private CursorLoader createLoaderCourses()
	{
		mCoursesQueryFinished = false;

		//Uri uri = Uri.parse("content://com.my.notes.notesforlater.provider");
		Uri uri = NotesForLaterProviderContract.Courses.CONTENT_URI;

		String[] courseColumns = {
				NotesForLaterProviderContract.Courses.COLUMN_COURSE_TITLE,
				NotesForLaterProviderContract.Courses.COLUMNS_COURSES_ID,
				NotesForLaterProviderContract.Courses._ID
		};
		return new CursorLoader(this, uri, courseColumns, null, null, NotesForLaterProviderContract.Courses.COLUMN_COURSE_TITLE);
	}

	@SuppressLint("StaticFieldLeak")
	private CursorLoader createLoaderNotes()
	{
		mNotesQueryFinished = false;

		String[] noteColumns = {
				NotesForLaterProviderContract.Notes.COLUMNS_COURSES_ID,
				NotesForLaterProviderContract.Notes.COLUMN_NOTE_TITLE,
				NotesForLaterProviderContract.Notes.COLUMN_NOTE_TEXT
		};
		mNoteUri = ContentUris
				.withAppendedId(NotesForLaterProviderContract.Notes.CONTENT_URI, mNoteId);

		return new CursorLoader(this, mNoteUri, noteColumns, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		if (loader.getId() == LOADER_NOTES)
		{
			loadFinishNotes(data);
		}
		else if (loader.getId() == LOADER_COURSES)
		{
			mAdapterCourses.changeCursor(data);
			mCoursesQueryFinished = true;
			displayNoteWhenQueryFinished();
		}
	}

	private void loadFinishNotes(Cursor data)
	{
		mCursor = data;

		mCourseIdPos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
		mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
		mNoteTextPos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

		mCursor.moveToNext();

		mNotesQueryFinished = true;
		//displayNote();
		displayNoteWhenQueryFinished();
	}

	private void displayNoteWhenQueryFinished()
	{
		if (mCoursesQueryFinished && mNotesQueryFinished)
		{
			displayNote();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		if (loader.getId() == LOADER_NOTES)
		{
			if (mCursor != null)
			{
				mCursor.close();
			}
		}
		else if (loader.getId() == LOADER_COURSES)
		{
			if (mCursor != null)
			{
				mCursor.close();
			}
		}
	}

}
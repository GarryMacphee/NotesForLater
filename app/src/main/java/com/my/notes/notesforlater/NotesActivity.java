package com.my.notes.notesforlater;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import static com.my.notes.notesforlater.NotesForLaterDatabaseContract.NoteInfoEntry;

public class NotesActivity extends AppCompatActivity
{
	public static final String NOTE_POSITION = "com.my.notes.notesforlater.NOTE_POSITION";
	public static final String ORIGINAL_NOTE_COURSE_ID = "com.my.notes.notesforlater.ORIGINAL_NOTE_COURSE_ID";
	public static final String ORIGINAL_NOTE_TITLE = "com.my.notes.notesforlater.ORIGINAL_NOTE_TITLE";
	public static final String ORIGINAL_NOTE_TEXT = "com.my.notes.notesforlater.ORIGINAL_NOTE_TEXT";
	public static final int POSITION_NOT_SET = -1;
	private final String TAG = getClass().getSimpleName();
	private NoteInfo mNote;
	private boolean mIsNewNote;
	private Spinner mSpinnerCourses;
	private EditText mTextNoteTitle;
	private EditText mTextNoteText;
	private int mNotePosition;
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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notes);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mNotesForLaterDBHelper = new NotesForLaterDBHelper(this);

		ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
				ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
		mViewModel = viewModelProvider.get(NoteActivityViewModel.class);
		if (mViewModel.mIsNewlyCreated && savedInstanceState != null)
			mViewModel.restoreState(savedInstanceState);
		mViewModel.mIsNewlyCreated = false;

		mSpinnerCourses = findViewById(R.id.spinner_courses);

		List<CourseInfo> courses = DataManager.getInstance().getCourses();
		ArrayAdapter<CourseInfo> adapterCourses =
				new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
		adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerCourses.setAdapter(adapterCourses);

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
			loadNoteData();

		Log.d(TAG, "onCreate");
	}

	private void readDisplayStateValues()
	{
		Intent intent = getIntent();
		mNotePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
		mIsNewNote = mNotePosition == POSITION_NOT_SET;
		if (mIsNewNote)
		{
			createNewNote();
		}

		Log.i(TAG, "mNotePosition: " + mNotePosition);
		mNote = DataManager.getInstance().getNotes().get(mNotePosition);

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

		String selection = NoteInfoEntry.COLUMN_COURSE_ID + " = ? AND "
				+ NoteInfoEntry.COLUMN_NOTE_TITLE + " LIKE ?";

		String[] selectionArgs = {courseId, titleStart + "%"};

		String[] noteColumns = {
				NoteInfoEntry.COLUMN_COURSE_ID,
				NoteInfoEntry.COLUMN_NOTE_TITLE,
				NoteInfoEntry.COLUMN_NOTE_TEXT
		};

		mCursor = db.query(
				NoteInfoEntry.TABLE_NAME,
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

	private void createNewNote()
	{
		DataManager dm = DataManager.getInstance();
		mNotePosition = dm.createNewNote();
//        mNote = dm.getNotes().get(mNotePosition);
	}

	private void displayNote()
	{
		String courseId = mCursor.getString(mCourseIdPos);
		String noteTitle = mCursor.getString(mNoteTitlePos);
		String noteText = mCursor.getString(mNoteTextPos);

		List<CourseInfo> courses = DataManager.getInstance().getCourses();

		CourseInfo CourseInfo = DataManager.getInstance().getCourse(courseId);

		int courseIndex = courses.indexOf(CourseInfo);
		mSpinnerCourses.setSelection(courseIndex);
		mTextNoteTitle.setText(noteTitle);
		mTextNoteText.setText(noteText);
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
			Log.i(TAG, "Cancelling note at position: " + mNotePosition);
			if (mIsNewNote)
			{
				DataManager.getInstance().removeNote(mNotePosition);
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

	private void storePreviousNoteValues()
	{
		CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
		mNote.setCourse(course);
		mNote.setTitle(mOriginalNoteTitle);
		mNote.setText(mOriginalNoteText);
	}

	private void saveNote()
	{
		mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
		mNote.setTitle(mTextNoteTitle.getText().toString());
		mNote.setText(mTextNoteText.getText().toString());
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

		++mNotePosition;
		mNote = DataManager.getInstance().getNotes().get(mNotePosition);

		saveOriginalNoteValues();
		displayNote();

		invalidateOptionsMenu();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem item = menu.findItem(R.id.action_next);
		int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;

		item.setEnabled(mNotePosition < lastNoteIndex);
		return super.onPrepareOptionsMenu(menu);
	}
}
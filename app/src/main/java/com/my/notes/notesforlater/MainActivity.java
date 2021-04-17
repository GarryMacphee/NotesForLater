package com.my.notes.notesforlater;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import static com.my.notes.notesforlater.NotesForLaterDatabaseContract.NoteInfoEntry;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor>, CourseEventDisplayCallback
{
	public static final int NOTE_UPLOADER_JOB_ID = 1;
	private NoteRecyclerAdapter mNoteRecyclerAdapter;
	private AppBarConfiguration mAppBarConfiguration;
	private LinearLayoutManager mLinearLayoutManager;
	private RecyclerView mRecyclerView;
	private CourseRecyclerAdapter mMCourseRecyclerAdapter;
	private NotesForLaterDBHelper mDbOpenHelper;
	public static final int LOADER_NOTES = 0;
	private GridLayoutManager mMGridLayoutManager;
	private CourseEventsReceiver mCourseEventsReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		enableStrictMode();

		mDbOpenHelper = new NotesForLaterDBHelper(this);

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(view ->
		{
			startActivity(new Intent(MainActivity.this, NotesActivity.class));
		});

		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
		PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);
		PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		initializeDisplayContent();

		setupCourseEventsReceiver();
	}

	private void setupCourseEventsReceiver()
	{
		mCourseEventsReceiver = new CourseEventsReceiver();
		mCourseEventsReceiver.setCourseEventDisplayCallback(this);

		IntentFilter intentFilter = new IntentFilter(CourseEventsReceiver.ACTION_COURSE_EVENT);
		registerReceiver(mCourseEventsReceiver, intentFilter);
	}


	private void enableStrictMode()
	{
		/*if (BuildConfig.DEBUG)
		{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.detectAll()
					.penaltyLog()
					.build();
			StrictMode.setThreadPolicy(policy);
		}*/
	}


	private void initializeDisplayContent()
	{
		DataManager.loadFromDatabase(mDbOpenHelper);

		mRecyclerView = findViewById(R.id.list_items_recycler);
		mLinearLayoutManager = new LinearLayoutManager(this);
		mMGridLayoutManager = new GridLayoutManager(this, getResources()
				.getInteger(R.integer.course_grid_span));

		GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(GridLayout
				.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f));

		mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);

		List<CourseInfo> courses = DataManager.getInstance().getCourses();
		mMCourseRecyclerAdapter = new CourseRecyclerAdapter(this, courses);

		displayNotes();
	}

	private void displayNotes()
	{
		mRecyclerView.setLayoutManager(mLinearLayoutManager);
		mRecyclerView.setAdapter(mNoteRecyclerAdapter);

		selectNavigationItem(R.id.nav_notes);
	}

	private void selectNavigationItem(int id)
	{
		NavigationView navigationView = findViewById(R.id.nav_view);
		Menu menu = navigationView.getMenu();
		menu.findItem(id).setChecked(true);
	}

	@Override
	protected void onDestroy()
	{
		mDbOpenHelper.close();
		unregisterReceiver(mCourseEventsReceiver);
		super.onDestroy();
	}

	@Override
	public void onBackPressed()
	{
		DrawerLayout drawer = findViewById(R.id.drawer_layout);

		if (drawer.isDrawerOpen(GravityCompat.START))
		{
			drawer.closeDrawer(GravityCompat.START);
		}
		else
		{
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		getLoaderManager().restartLoader(LOADER_NOTES, null, this);

		updateNavHeader();

		//openDrawer();
	}

	@SuppressLint("WrongConstant")
	private void openDrawer()
	{
		Handler handler = new Handler(Looper.getMainLooper());

		handler.postDelayed(() ->
		{
			DrawerLayout drawer = findViewById(R.id.drawer_layout);
			drawer.openDrawer(Gravity.START);
		}, 1000);
	}

	private void loadNotes()
	{
		SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

		final String[] noteColumns = {
				NoteInfoEntry.COLUMN_NOTE_TITLE,
				NoteInfoEntry.COLUMN_COURSE_ID,
				NoteInfoEntry._ID
		};

		String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;

		final Cursor noteCursor = db
				.query(NoteInfoEntry.TABLE_NAME, noteColumns, null, null, null, null, noteOrderBy);

		mNoteRecyclerAdapter.changeCursor(noteCursor);
	}


	private void updateNavHeader()
	{
		NavigationView navigationView = findViewById(R.id.nav_view);
		View headerView = navigationView.getHeaderView(0);
		TextView textUserName = headerView.findViewById(R.id.text_user_name);
		TextView textEmailAddress = headerView.findViewById(R.id.text_email_address);

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = pref.getString("user_display_name", "");
		String emailAddress = pref.getString("user_email_address", "");

		textUserName.setText(userName);
		textEmailAddress.setText(emailAddress);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
		if (id == R.id.action_settings)
		{
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}

		if (id == R.id.action_backup_notes)
		{
			backupNotes();
		}

		if (id == R.id.action_upload_notes)
		{
			scheduleNotesUpload();
		}

		return super.onOptionsItemSelected(item);
	}


	private void scheduleNotesUpload()
	{
		PersistableBundle persistableBundle = new PersistableBundle();
		persistableBundle
				.putString(NoteUploaderJobService.EXTRA_DATA_URI, NotesForLaterProviderContract.Notes.CONTENT_URI
						.toString());

		ComponentName componentName = new ComponentName(this, NoteUploaderJobService.class);

		JobInfo jobInfo = new JobInfo.Builder(NOTE_UPLOADER_JOB_ID, componentName)
				.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
				.setExtras(persistableBundle)
				.build();

		JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
		jobScheduler.schedule(jobInfo);

	}

	private void backupNotes()
	{
		Intent intent = new Intent(this, NotesBackupService.class);
		intent.putExtra(NotesBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
		startService(intent);
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item)
	{
		int id = item.getItemId();

		if (id == R.id.nav_notes)
		{
			displayNotes();
		}
		else if (id == R.id.nav_courses)
		{
			displayCourses();
		}
		else if (id == R.id.nav_share)
		{
			handleShare();
		}
		else if (id == R.id.nav_send)
		{
			handleSelection("Send");
		}

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public boolean onSupportNavigateUp()
	{
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration)
				|| super.onSupportNavigateUp();
	}

	private void handleShare()
	{
		View view = findViewById(R.id.list_items_recycler);
		Snackbar.make(view, "Share to - " + PreferenceManager.getDefaultSharedPreferences(this)
															 .getString("user_favorite_social", ""),
				Snackbar.LENGTH_LONG).show();
	}

	private void displayCourses()
	{
		mRecyclerView.setLayoutManager(mMGridLayoutManager);
		mRecyclerView.setAdapter(mMCourseRecyclerAdapter);
		selectNavigationItem(R.id.nav_courses);
	}

	private void handleSelection(String message)
	{
		View view = findViewById(R.id.list_items_recycler);
		Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
	}

	@SuppressLint("StaticFieldLeak")
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		CursorLoader loader = null;
		if (id == LOADER_NOTES)
		{
			final String[] noteColumns = {
					NotesForLaterProviderContract.Notes._ID,
					NotesForLaterProviderContract.Notes.COLUMN_NOTE_TITLE,
					NotesForLaterProviderContract.Notes.COLUMN_COURSE_TITLE
			};

			final String noteOrderBy = NotesForLaterProviderContract.Notes.COLUMN_COURSE_TITLE + "," + NotesForLaterProviderContract.Notes.COLUMN_NOTE_TITLE;

			loader = new CursorLoader(this, NotesForLaterProviderContract.Notes.CONTENT_EXPANDED_URI,
					noteColumns, null, null, noteOrderBy);

		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		if (loader.getId() == LOADER_NOTES)
		{
			mNoteRecyclerAdapter.changeCursor(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		if (loader.getId() == LOADER_NOTES)
		{
			mNoteRecyclerAdapter.changeCursor(null);
		}
	}

	@Override
	public void onEventReceived(String courseId, String courseMessage)
	{
		Log.d(getClass()
				.getSimpleName(), ">>>Received courses from broadcast receiver<<< \n courseId: " + courseId + "| courseMessage: " + courseMessage);
	}
}
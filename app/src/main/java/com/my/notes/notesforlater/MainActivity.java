package com.my.notes.notesforlater;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
	private NoteRecyclerAdapter mNoteRecyclerAdapter;
	private AppBarConfiguration mAppBarConfiguration;
	private LinearLayoutManager mLinearLayoutManager;
	private RecyclerView mRecyclerView;
	private CourseRecyclerAdapter mMCourseRecyclerAdapter;
	private GridLayoutManager mGridLayoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(view ->
		{
			startActivity(new Intent(MainActivity.this, NotesActivity.class));
		});

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		initializeDisplayContent();
	}


	private void selectNavigationMenuItem(int id)
	{
		NavigationView navigationView = findViewById(R.id.nav_view);
		Menu menu = navigationView.getMenu();
		menu.findItem(id).setChecked(true);
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
		mNoteRecyclerAdapter.notifyDataSetChanged();
	}


	private void initializeDisplayContent()
	{
		mRecyclerView = findViewById(R.id.list_items_recycler);
		mLinearLayoutManager = new LinearLayoutManager(this);
		mGridLayoutManager = new GridLayoutManager(this, 2);

		List<NoteInfo> notes = DataManager.getInstance().getNotes();
		mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, notes);

		List<CourseInfo> courses = DataManager.getInstance().getCourses();
		mMCourseRecyclerAdapter = new CourseRecyclerAdapter(this, courses);

		displayNotes();
	}

	private void displayCourses()
	{
		mRecyclerView.setLayoutManager(mGridLayoutManager);
		mRecyclerView.setAdapter(mMCourseRecyclerAdapter);
		selectNavigationItem(R.id.nav_courses);
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
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/*@Override
	public boolean onSupportNavigateUp()
	{
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration)
				|| super.onSupportNavigateUp();
	}*/

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
			handleSelection("Share");
		}
		else if (id == R.id.nav_send)
		{
			handleSelection("Send");
		}

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}


	private void handleSelection(String message)
	{
		View view = findViewById(R.id.list_items_recycler);
		Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
	}
}
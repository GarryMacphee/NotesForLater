package com.my.notes.notesforlater;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class NoteListActivity extends AppCompatActivity
{
	private NoteRecyclerAdapter mNoteRecyclerAdapter;

	//private ArrayAdapter<NoteInfo> mAdapterNotes;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_list);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(view -> startActivity(
				new Intent(NoteListActivity.this, NotesActivity.class)));

		initializeDisplayContent();
	}

	private void initializeDisplayContent()
	{
		final RecyclerView recyclerView = findViewById(R.id.list_notes);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(linearLayoutManager);

		List<NoteInfo> notes = DataManager.getInstance().getNotes();
		mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, notes);
		recyclerView.setAdapter(mNoteRecyclerAdapter);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mNoteRecyclerAdapter.notifyDataSetChanged();
	}

}

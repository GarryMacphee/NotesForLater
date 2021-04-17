package com.my.notes.notesforlater.notes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.my.notes.notesforlater.R;
import com.my.notes.notesforlater.data.DataManager;

import java.util.List;

public class NoteListActivity extends AppCompatActivity
{
	private NoteRecyclerAdapter mNoteRecyclerAdapter;
	private RecyclerView mRecyclerView;
	private LinearLayoutManager mLinearLayoutManager;

	//private ArrayAdapter<NoteInfo> mAdapterNotes;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_list);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(view -> startActivity(new Intent(NoteListActivity.this, NotesActivity.class)));

		initializeDisplayContent();
	}

	private void initializeDisplayContent()
	{
		mRecyclerView = findViewById(R.id.list_notes);
		mLinearLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(mLinearLayoutManager);

		List<NoteInfo> notes = DataManager.getInstance().getNotes();
		mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);
		mRecyclerView.setAdapter(mNoteRecyclerAdapter);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mNoteRecyclerAdapter.notifyDataSetChanged();
	}

}

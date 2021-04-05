package com.my.notes.notesforlater;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.my.notes.notesforlater.NotesForLaterDatabaseContract.NoteInfoEntry;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.NoteRecyclerViewHolder>
{
	private final Context mContext;
	private Cursor mCursor;
	private final LayoutInflater mLayoutInflater;
	private int mNoteTitlePos;
	private int mCoursePos;
	private int mIdPos;

	public NoteRecyclerAdapter(Context context, Cursor cursor)
	{
		mContext = context;
		mCursor = cursor;
		mLayoutInflater = LayoutInflater.from(mContext);
		populateColumnPositions();
	}


	private void populateColumnPositions()
	{
		if (mCursor == null)
			return;

		mCoursePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);

		mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);

		mIdPos = mCursor.getColumnIndex(NoteInfoEntry._ID);

	}


	public void changeCursor(Cursor cursor)
	{
		if (mCursor != null)
		{
			mCursor.close();
		}
		mCursor = cursor;
		populateColumnPositions();
		notifyDataSetChanged();
	}


	@NonNull
	@Override
	public NoteRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		View itemView = mLayoutInflater.inflate(R.layout.note_list_item, parent, false);
		return new NoteRecyclerViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(@NonNull NoteRecyclerViewHolder holder, int position)
	{
		mCursor.moveToPosition(position);
		String course = mCursor.getString(mCoursePos);
		String noteTitle = mCursor.getString(mNoteTitlePos);
		int id = mCursor.getInt(mIdPos);

		holder.mTextCourse.setText(course);
		holder.mTextTitle.setText(noteTitle);
		holder.mId = id;
	}

	@Override
	public int getItemCount()
	{
		return mCursor == null ? 0 : mCursor.getCount();
	}

	public class NoteRecyclerViewHolder extends RecyclerView.ViewHolder
	{
		public final TextView mTextCourse;
		public final TextView mTextTitle;
		public int mId;

		public NoteRecyclerViewHolder(@NonNull View itemView)
		{
			super(itemView);
			mTextCourse = itemView.findViewById(R.id.text_title);
			mTextTitle = itemView.findViewById(R.id.text_course);
			itemView.setOnClickListener(v ->
			{
				Intent intent = new Intent(mContext, NotesActivity.class);
				intent.putExtra(NotesActivity.NOTE_ID, mId);
				mContext.startActivity(intent);
			});
		}
	}
}

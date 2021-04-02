package com.my.notes.notesforlater;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.NoteRecyclerViewHolder>
{
	private final Context mContext;
	private final List<NoteInfo> mNotes;
	private final LayoutInflater mLayoutInflater;

	public NoteRecyclerAdapter(Context context, List<NoteInfo> notes)
	{
		mContext = context;
		mNotes = notes;
		mLayoutInflater = LayoutInflater.from(mContext);
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
		NoteInfo note = mNotes.get(position);
		holder.mTextCourse.setText(note.getCourse().getTitle());
		holder.mTextTitle.setText(note.getTitle());
		holder.mCurrentPosition = position;
	}

	@Override
	public int getItemCount()
	{
		return mNotes.size();
	}

	public class NoteRecyclerViewHolder extends ViewHolder
	{
		public final TextView mTextCourse;
		public final TextView mTextTitle;
		public int mCurrentPosition;

		public NoteRecyclerViewHolder(@NonNull View itemView)
		{
			super(itemView);
			mTextCourse = itemView.findViewById(R.id.text_course);
			mTextTitle = itemView.findViewById(R.id.text_title);
			itemView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(mContext, NotesActivity.class);
					intent.putExtra(NotesActivity.NOTE_POSITION, mCurrentPosition);
					mContext.startActivity(intent);
				}
			});
		}
	}
}

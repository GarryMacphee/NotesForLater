package com.my.notes.notesforlater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.NoteRecyclerViewHolder>
{
	private final Context mContext;
	private final LayoutInflater mLayoutInflater;

	public NoteRecyclerAdapter(Context context)
	{
		mContext = context;
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

	}

	@Override
	public int getItemCount()
	{
		return 0;
	}

	public class NoteRecyclerViewHolder extends ViewHolder
	{

		public NoteRecyclerViewHolder(@NonNull View itemView)
		{
			super(itemView);
		}
	}
}

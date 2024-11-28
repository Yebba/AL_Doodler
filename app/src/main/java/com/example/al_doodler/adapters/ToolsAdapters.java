package com.example.al_doodler.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.al_doodler.Interface.ToolsListener;
import com.example.al_doodler.Interface.ViewOnClick;
import com.example.al_doodler.R;
import com.example.al_doodler.model.ToolsItem;
import com.example.al_doodler.viewHolder.ToolsViewHolder;

import java.util.List;

// This file has override functions for the tool bar. They change the typeface of the tool selected, and link the tool icons to the actual tools.
public class ToolsAdapters extends RecyclerView.Adapter<ToolsViewHolder> {

    private List<ToolsItem> toolsItemList;
    private int selected = -1;
    private ToolsListener listener;

    public ToolsAdapters(List<ToolsItem> toolsItemList, ToolsListener listener) {
        this.toolsItemList = toolsItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ToolsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tools_item, parent, false);

        return new ToolsViewHolder(view);
    }

//  This function affects the way the elements in the tool bar look. When a tool is selected, then change the typeface so the user knows it is selected.
    @Override
    public void onBindViewHolder(@NonNull ToolsViewHolder holder, int position) {

        holder.name.setText(toolsItemList.get(position).getName());
        holder.icon.setImageResource(toolsItemList.get(position).getIcon());

        holder.setViewOnClick(new ViewOnClick() {
            @Override
            public void onClick(int pos) {
                selected = pos;
                listener.onSelected(toolsItemList.get(pos).getName());
                notifyDataSetChanged();
            }
        });

        if(selected == position){

            holder.name.setTypeface(holder.name.getTypeface(), Typeface.BOLD_ITALIC);
        } else {
            holder.name.setTypeface(Typeface.DEFAULT);
        }

    }

    @Override
    public int getItemCount() {
        return toolsItemList.size();
    }
}

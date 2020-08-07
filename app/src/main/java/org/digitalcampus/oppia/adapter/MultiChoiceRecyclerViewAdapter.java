package org.digitalcampus.oppia.adapter;


import android.view.View;

import org.digitalcampus.oppia.utils.MultiChoiceHelper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class MultiChoiceRecyclerViewAdapter<H extends MultiChoiceRecyclerViewAdapter.ViewHolder> extends RecyclerView.Adapter<H> {


    private MultiChoiceHelper multiChoiceHelper;
    protected boolean isMultiChoiceMode;

    public void updateViewHolder(@NonNull final MultiChoiceRecyclerViewAdapter.ViewHolder viewHolder, final int position){
        viewHolder.updateCheckedState(position);
    }

    public void setEnterOnMultiChoiceMode(boolean multiChoiceModeActive) {
        this.isMultiChoiceMode = multiChoiceModeActive;

    }

    public MultiChoiceHelper getMultiChoiceHelper() {
        return multiChoiceHelper;
    }

    public void setMultiChoiceHelper(MultiChoiceHelper helper){
        multiChoiceHelper = helper;
    }

    public class ViewHolder extends MultiChoiceHelper.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            bind(multiChoiceHelper, getAdapterPosition());
        }

    }
}



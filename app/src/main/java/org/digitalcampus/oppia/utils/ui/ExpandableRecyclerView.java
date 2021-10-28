package org.digitalcampus.oppia.utils.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import org.digitalcampus.mobile.learning.R;

public class ExpandableRecyclerView extends RecyclerView {

    private static final String TAG = ExpandableRecyclerView.class.getSimpleName();

    public ExpandableRecyclerView(Context context) {
        super(context, null);
        initRecycler();
    }

    public ExpandableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initRecycler();
    }

    public ExpandableRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initRecycler();
    }

    private void initRecycler() {
        setClipToPadding(false);
        setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    public Parcelable onSaveInstanceState() {
        //begin boilerplate code that allows parent classes to save state
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        //end

        if (getAdapter() != null)
            ss.stateToSave = ((Adapter) this.getAdapter()).getExpandedGroups();

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        //begin boilerplate code so parent classes can restore state
        if (!(state instanceof SavedState))  // if state is not instance of out SaveState just restore in reg way
        {
            super.onRestoreInstanceState(state);
            return;
        }
        // else if cast him to SavedState

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        //end

        if (getAdapter() != null)
            ((Adapter) getAdapter()).setExpandedGroups(ss.stateToSave);
    }

    static class SavedState implements Parcelable {
        public static final SavedState EMPTY_STATE = new SavedState() {
        };

        SparseBooleanArray stateToSave;
        Parcelable superState;

        SavedState() {
            superState = null;
        }

        SavedState(Parcelable superState) {
            this.superState = superState != EMPTY_STATE ? superState : null;
        }

        private SavedState(Parcel in) {
            Parcelable superState = in.readParcelable(ExpandableRecyclerView.class.getClassLoader());
            this.superState = superState != null ? superState : EMPTY_STATE;
            this.stateToSave = in.readSparseBooleanArray();
        }

        @Override
        public int describeContents() {
            return 0;
        }


        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            out.writeParcelable(superState, flags);
            out.writeSparseBooleanArray(this.stateToSave);
        }

        public Parcelable getSuperState() {
            return superState;
        }

        //required field that makes Parcelables from a Parcel
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }


    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (!(adapter instanceof Adapter))
            throw new IllegalArgumentException("adapter has to be of type ExpandableRecyclerView.Adapter");
        super.setAdapter(adapter);
    }


    public abstract static class Adapter<CVH extends ViewHolder, GVH extends ViewHolder, HVH extends ViewHolder, C, G> extends RecyclerView.Adapter<ViewHolder> {

        private OnChildItemClickedListener onChildItemClickedListener;
        private boolean headerVisible = false;
        protected boolean startExpanded = false;

        private static final int TYPE_GROUP_HEADER = 0;
        private static final int TYPE_HEADER = 1;
        private static final int TYPE_CHILD = 2;

        SparseBooleanArray expanded = new SparseBooleanArray();

        public Adapter() {
        }

        boolean isExpanded(int group) {
            return expanded.get(group, startExpanded);
        }

        SparseBooleanArray getExpandedGroups() {
            return expanded;
        }

        public void setHeaderVisible(boolean visible) {
            headerVisible = visible;
        }

        public void setExpandedGroups(SparseBooleanArray expanded) {
            this.expanded = expanded;
        }

        public void expand(int group) {
            if (isExpanded(group))
                return;

            // this lines of code calculate number of shown item in recycler view. also group is counting .
            int position = 0;
            for (int i = 0; i < group; i++) {
                position++;
                if (isExpanded(i))
                    position += getChildItemCount(i);
            }
            position++; // this for percent group
            if (headerVisible) {
                position++;
            }
            notifyItemRangeInserted(position, getChildItemCount(group)); // notify recycler view for expanding
            expanded.put(group, true); // save expanding in sparce array
        }

        public void collapse(int group) {
            if (!isExpanded(group)) // if is not expanded . so nothing to collapse.
                return;

            int position = 0;
            for (int i = position; i < group; i++) {
                position++;
                if (isExpanded(i))
                    position += getChildItemCount(i); // item
            }
            position++;
            if (headerVisible) {
                position++;
            }
            notifyItemRangeRemoved(position, getChildItemCount(group));
            expanded.put(group, false);
        }

        public abstract int getGroupItemCount();

        public abstract int getChildItemCount(int group);

        @Override
        public int getItemCount() {
            int count = 0;
            for (int i = 0; i < getGroupItemCount(); i++) {
                count += isExpanded(i) ? getChildItemCount(i) + 1 : 1;
            }

            //If the header is visible, we add an additional item
            if (headerVisible) {
                count++;
            }
            return count;
        }

        public abstract G getGroupItem(int position);

        public abstract C getChildItem(int group, int position);

        public Object getItem(int i) {
            if (headerVisible) {
                if (i == 0) {
                    return null;
                } else {
                    i--;
                }
            }
            int group = 0;
            while (group < getGroupItemCount()) {
                if (i > 0 && !isExpanded(group)) {
                    i--;
                    group++;
                    continue;
                }
                if (i > 0 && isExpanded(group)) {
                    i--;
                    if (i < getChildItemCount(group))
                        return getChildItem(group, i);
                    i -= getChildItemCount(group);
                    group++;
                    continue;
                }
                if (i == 0)
                    return getGroupItem(group);
            }
            throw new IndexOutOfBoundsException();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
            if (headerVisible) {
                if (i == 0) {
                    onBindHeaderViewHolder((HVH) holder);
                    return;
                } else {
                    i--;
                }
            }
            int group = 0;
            while (group < getGroupItemCount()) {
                if (i > 0 && !isExpanded(group)) {
                    i--;
                    group++;
                    continue;
                }
                if (i > 0 && isExpanded(group)) {
                    i--;
                    if (i < getChildItemCount(group)) {
                        onBindChildViewHolder((CVH) holder, group, i);
                        return;
                    }
                    i -= getChildItemCount(group);
                    group++;
                    continue;
                }
                if (i == 0) {
                    onBindGroupViewHolder((GVH) holder, group);
                    return;
                }
            }
            throw new IndexOutOfBoundsException();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_HEADER:
                    return onCreateHeaderViewHolder(parent);
                case TYPE_GROUP_HEADER:
                    return onCreateGroupViewHolder(parent);
                default:
                    return onCreateChildViewHolder(parent, viewType);
            }
        }

        protected abstract HVH onCreateHeaderViewHolder(ViewGroup parent);

        protected abstract GVH onCreateGroupViewHolder(ViewGroup parent);

        protected abstract CVH onCreateChildViewHolder(ViewGroup parent, int viewType);

        public int getChildItemViewType(int group, int position) {
            return TYPE_CHILD;
        }

        @Override
        public int getItemViewType(int i) {
            if (headerVisible) {
                if (i == 0) {
                    return TYPE_HEADER;
                } else {
                    i--;
                }
            }
            int group = 0;
            while (group < getGroupItemCount()) {
                if (i > 0 && !isExpanded(group)) {
                    i--;
                    group++;
                    continue;
                }
                if (i > 0 && isExpanded(group)) {
                    i--;
                    if (i < getChildItemCount(group))
                        return getChildItemViewType(group, i);
                    i -= getChildItemCount(group);
                    group++;
                    continue;
                }

                if (i == 0)
                    return TYPE_GROUP_HEADER;
            }
            throw new IndexOutOfBoundsException();
        }

        public void setOnChildItemClickedListener(OnChildItemClickedListener onItemClickedListener) {
            this.onChildItemClickedListener = onItemClickedListener;
        }

        public abstract void onBindHeaderViewHolder(HVH holder);

        public void onBindChildViewHolder(CVH holder, final int group, final int position) {
            holder.itemView.setOnClickListener(v -> {
                if (Adapter.this.onChildItemClickedListener != null) {
                    Adapter.this.onChildItemClickedListener.onChildItemClicked(group, position);
                }

            });
        }

        public void onBindGroupViewHolder(final GVH holder, final int group) {
            if (holder instanceof GroupViewHolder)
                ((GroupViewHolder) holder).setExpanded(isExpanded(group));

            holder.itemView.setOnClickListener(v -> {
                if (isExpanded(group)) {
                    collapse(group);
                    if (holder instanceof GroupViewHolder)
                        ((GroupViewHolder) holder).collapse();
                } else {
                    expand(group);
                    if (holder instanceof GroupViewHolder)
                        ((GroupViewHolder) holder).expand();
                }
            });
        }
    }

    public abstract static class GroupViewHolder extends RecyclerView.ViewHolder {

        ImageView expandedIndicator;
        private boolean expanded;

        public GroupViewHolder(View itemView) {
            super(itemView);
            expandedIndicator = itemView.findViewById(R.id.expanded_indicator);
        }


        public void expand() {
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(200);
            animator.addUpdateListener(animation -> {
                expandedIndicator.setRotation(180 * (float) (animation.getAnimatedValue()));
                expandedIndicator.postInvalidate();
            });
            animator.start();
            expanded = true;
        }

        public void collapse() {
            ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(200);
            animator.addUpdateListener(animation -> {
                expandedIndicator.setRotation(180 * (float) (animation.getAnimatedValue()));
                expandedIndicator.postInvalidate();
            });
            animator.start();
            expanded = false;
        }

        public void setExpanded(boolean expanded) {
            expandedIndicator.setRotation(expanded ? 180 : 0);
            this.expanded = expanded;
        }

        public boolean isExpanded() {
            return expanded;
        }
    }


    public interface OnChildItemClickedListener {
        void onChildItemClicked(int group, int position);
    }

}

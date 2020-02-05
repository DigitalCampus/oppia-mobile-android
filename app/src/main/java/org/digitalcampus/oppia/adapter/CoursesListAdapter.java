package org.digitalcampus.oppia.adapter;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.squareup.picasso.Picasso;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.CircleTransform;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class CoursesListAdapter extends RecyclerView.Adapter<CoursesListAdapter.ViewHolder> {


    private final SharedPreferences prefs;
    private List<Course> courses;
    private Context context;
    private OnItemClickListener itemClickListener;

    private Dialog contextMenuDialog;
    private int currentSelectedItem;
    private int selectedOption;


    public CoursesListAdapter(Context context, List<Course> courses) {
        this.context = context;
        this.courses = courses;
        createDialog();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_course_list, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        final Course c = getItemAtPosition(position);

        String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        viewHolder.courseTitle.setText(c.getTitle(lang));
        String description = c.getDescription(lang);
        if (!TextUtils.isEmpty(description) && prefs.getBoolean(PrefsActivity.PREF_SHOW_COURSE_DESC, BuildConfig.SHOW_COURSE_DESCRIPTION)) {
            viewHolder.courseDescription.setText(description);
            viewHolder.courseDescription.setVisibility(View.VISIBLE);
        } else {
            viewHolder.courseDescription.setVisibility(View.GONE);
        }

        if (prefs.getBoolean(PrefsActivity.PREF_SHOW_PROGRESS_BAR, App.DEFAULT_DISPLAY_PROGRESS_BAR)) {
            int courseProgress = (int) c.getProgressPercent();
            viewHolder.circularProgressBar.setVisibility(View.VISIBLE);
            viewHolder.circularProgressBar.setProgressWithAnimation(courseProgress, 1000l);
        } else {
            viewHolder.circularProgressBar.setVisibility(View.GONE);
        }

        // set image
        if (c.getImageFile() != null) {
            String image = c.getImageFileFromRoot();
            Picasso.get().load(new File(image))
                    .placeholder(R.drawable.default_course)
                    .transform(new CircleTransform())
                    .into(viewHolder.courseImage);
        } else {
            viewHolder.courseImage.setImageResource(R.drawable.default_course);
        }

    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public Course getItemAtPosition(int position) {
        return courses.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private View rootView;
        private CircularProgressBar circularProgressBar;
        private TextView courseTitle;
        private TextView courseDescription;
        private ImageView courseImage;


        public ViewHolder(View itemView) {

            super(itemView);

            courseTitle = itemView.findViewById(R.id.course_title);
            courseDescription = itemView.findViewById(R.id.course_description);
            courseImage = itemView.findViewById(R.id.course_image);
            circularProgressBar = itemView.findViewById(R.id.circularProgressBar);

            rootView = itemView;

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(getAdapterPosition());
                    }
                }
            });

            rootView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    currentSelectedItem = getAdapterPosition();
                    selectedOption = -1;
                    contextMenuDialog.show();
                    return true;
                }
            });


        }

    }

    private void createDialog() {
        contextMenuDialog = new Dialog(context);
        contextMenuDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        contextMenuDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        contextMenuDialog.setContentView(R.layout.dialog_course_contextmenu);
        registerMenuClick(R.id.course_context_reset);
        registerMenuClick(R.id.course_context_delete);
        registerMenuClick(R.id.course_context_update_activity);

        contextMenuDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            //@Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (itemClickListener != null) {
                    itemClickListener.onContextMenuItemSelected(currentSelectedItem, selectedOption);
                }
            }
        });
    }

    private void registerMenuClick(final int id) {
        contextMenuDialog.findViewById(id).setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View view) {
                selectedOption = id;
                contextMenuDialog.dismiss();
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onContextMenuItemSelected(int position, int itemId);
    }
}



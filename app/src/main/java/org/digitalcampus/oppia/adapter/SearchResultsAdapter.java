package org.digitalcampus.oppia.adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.SearchResult;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {


    private static final String TAG = "SearchResultsAdapter";
    private final String prefLang;
    private List<SearchResult> searchResults;
    private Context context;
    private OnItemClickListener itemClickListener;


    public SearchResultsAdapter(Context context, List<SearchResult> searchResults) {
        this.context = context;
        this.searchResults = searchResults;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(context).inflate(R.layout.row_search_results, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        final SearchResult sr = getItemAtPosition(position);

        Activity activity = sr.getActivity();
        Course course = sr.getCourse();

        String cTitle = course.getTitle(prefLang);
        String sTitle = sr.getSection().getTitle(prefLang);
        String aTitle = activity.getTitle(prefLang);

        viewHolder.activityTitle.setText(aTitle);
        viewHolder.sectionTitle.setText(sTitle);
        viewHolder.courseTitle.setText(cTitle);
        Log.d(TAG, course.getLocation());
        viewHolder.rootView.setTag(R.id.TAG_COURSE, course);
        viewHolder.rootView.setTag(R.id.TAG_ACTIVITY_DIGEST, activity.getDigest());

        Log.d(TAG, activity.getImageFilePath(""));
        if (activity.hasCustomImage()){

            String image = activity.getImageFilePath(course.getLocation());
            Log.d(TAG, new File(image).exists() ? "Exists" : "Noooo");
            Picasso.get().load(new File(image)).into(viewHolder.activityImage);
        }
        else {
            int defaultActivityDrawable = activity.getDefaultResourceImage();
            viewHolder.activityImage.setImageResource(defaultActivityDrawable);
        }
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public SearchResult getItemAtPosition(int position) {
        return searchResults.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        private TextView activityTitle;
        private TextView sectionTitle;
        private TextView courseTitle;
        private ImageView activityImage;

        public ViewHolder(View itemView) {

            super(itemView);

            activityTitle = itemView.findViewById(R.id.activity_title);
            sectionTitle = itemView.findViewById(R.id.section_title);
            courseTitle = itemView.findViewById(R.id.course_title);
            activityImage = itemView.findViewById(R.id.activity_icon);

            rootView = itemView;

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(v, getAdapterPosition());
                    }
                }
            });
        }

    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
 


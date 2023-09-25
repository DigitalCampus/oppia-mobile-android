package org.digitalcampus.oppia.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowMediaDownloadBinding
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.adapter.DownloadMediaAdapter.DownloadMediaViewHolder
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener
import org.digitalcampus.oppia.model.Media
import java.util.Locale

class DownloadMediaAdapter(private val context: Context, private var mediaList: MutableList<Media>) :
    MultiChoiceRecyclerViewAdapter<DownloadMediaViewHolder>() {

    inner class DownloadMediaViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding: RowMediaDownloadBinding

        init {
            binding = RowMediaDownloadBinding.bind(itemView)
            binding.actionBtn.setOnClickListener {
                itemClickListener?.onClick(getAdapterPosition())
            }
        }
    }

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var itemClickListener: ListInnerBtnOnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadMediaViewHolder {
        val contactView = LayoutInflater.from(context).inflate(R.layout.row_media_download, parent, false)
        return DownloadMediaViewHolder(contactView)
    }

    override fun onBindViewHolder(viewHolder: DownloadMediaViewHolder, position: Int) {
        updateViewHolder(viewHolder, position)
        val m = getItemAtPosition(position)
        val courses = StringBuilder()
        for (i in m.getCourses()!!.indices) {
            val c = m.getCourses()!![i]
            val title = c.getTitle(prefs)
            courses.append(if (i != 0) ", $title" else title)
        }
        viewHolder.binding.mediaCourses.text = courses.toString()
        viewHolder.binding.mediaTitle.text = m.filename
        viewHolder.binding.mediaPath.text = m.downloadUrl
        if (m.fileSize != 0.0) {
            viewHolder.binding.mediaFileSize.text = context.getString(R.string.media_file_size, m.fileSize / (1024 * 1024))
            viewHolder.binding.mediaFileSize.visibility = View.VISIBLE
        } else {
            viewHolder.binding.mediaFileSize.visibility = View.INVISIBLE
        }
        viewHolder.binding.actionBtn.visibility = if (isMultiChoiceMode) View.INVISIBLE else View.VISIBLE
        val actionBtnimagRes: Int
        if (m.isDownloading) {
            actionBtnimagRes = R.drawable.ic_action_cancel
            viewHolder.binding.downloadProgress.visibility = View.VISIBLE
            viewHolder.binding.mediaPath.visibility = View.GONE
            viewHolder.binding.downloadError.visibility = View.GONE
            if (m.progress > 0) {
                viewHolder.binding.downloadProgress.isIndeterminate = false
                viewHolder.binding.downloadProgress.progress = m.progress
            } else {
                viewHolder.binding.downloadProgress.isIndeterminate = true
            }
        } else {
            actionBtnimagRes = if (m.hasFailed()) R.drawable.dialog_ic_action_update else R.drawable.ic_action_download
            viewHolder.binding.downloadError.visibility = if (m.hasFailed()) View.VISIBLE else View.GONE
            viewHolder.binding.downloadProgress.visibility = View.GONE
            viewHolder.binding.mediaPath.visibility = View.VISIBLE
        }
        viewHolder.binding.actionBtn.setImageResource(actionBtnimagRes)
        viewHolder.binding.actionBtn.tag = actionBtnimagRes
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    fun getItemAtPosition(position: Int): Media {
        return mediaList[position]
    }

    fun sortByCourse() {
        //Sort the media list by filename
        val prefsCourse = PreferenceManager.getDefaultSharedPreferences(context)
        val lang = prefsCourse.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().language)
        mediaList.sortedWith { o1, o2 ->
            val titleCourse1 = o1.getCourses()!![0].getTitle(lang)
            val titleCourse2 = o2.getCourses()!![0].getTitle(lang)
            titleCourse1.compareTo(titleCourse2)
        }

        notifyDataSetChanged()
    }

    fun sortByFilename() {
        //Sort the media list by filename
        mediaList.sortedBy { it.filename }
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: ListInnerBtnOnClickListener?) {
        itemClickListener = listener
    }
}
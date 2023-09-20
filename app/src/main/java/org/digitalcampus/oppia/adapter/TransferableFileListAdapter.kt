package org.digitalcampus.oppia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowTransferableFileBinding
import org.digitalcampus.oppia.adapter.TransferableFileListAdapter.TclaViewHolder
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener
import org.digitalcampus.oppia.model.CourseTransferableFile

class TransferableFileListAdapter @JvmOverloads constructor(
    private val transferableFiles: MutableList<CourseTransferableFile>,
    private val listener: ListInnerBtnOnClickListener,
    filterCourses: Boolean = false
) : RecyclerView.Adapter<TclaViewHolder>() {

    val TAG = TransferableFileListAdapter::class    .simpleName
    private var courseFiles: MutableList<CourseTransferableFile> = ArrayList()

    init {
        if (filterCourses) {
            filterCourses()
            registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onChanged() {
                    filterCourses()
                    super.onChanged()
                }
            })
        } else {
            courseFiles = transferableFiles
        }
    }

    inner class TclaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowTransferableFileBinding = RowTransferableFileBinding.bind(itemView)

        init {
            binding.downloadCourseBtn.setOnClickListener {
                listener.onClick(adapterPosition)
            }
        }
    }

    private fun filterCourses() {
        courseFiles.clear()
        for (file in transferableFiles) {
            if (CourseTransferableFile.TYPE_COURSE_BACKUP == file.type) {
                courseFiles.add(file)
            }
        }
        for (course in courseFiles) {
            var relatedSize: Long = 0
            for (file in transferableFiles) {
                if (course.relatedMedia.contains(file.filename)) {
                    relatedSize += file.fileSize
                }
            }
            course.relatedFilesize = relatedSize
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TclaViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_transferable_file, parent, false)
        return TclaViewHolder(v)
    }

    override fun onBindViewHolder(holder: TclaViewHolder, position: Int) {
        val current = courseFiles[position]
        holder.binding.fileTitle.visibility = if (current.title != null) View.VISIBLE else View.GONE
        holder.binding.fileTitle.text = current.title

        if (current.type == CourseTransferableFile.TYPE_ACTIVITY_LOG) {
            holder.binding.fileSubtitle.text = current.displayDateTimeFromFilename
            holder.binding.fileAside.visibility = View.VISIBLE
            holder.binding.fileAside.text = current.displayFileSize
            holder.binding.elemIcon.setImageResource(R.drawable.ic_file_account)
        } else {
            val iconResId = if (current.type == CourseTransferableFile.TYPE_COURSE_BACKUP) {
                R.drawable.ic_notification
            } else {
                R.drawable.default_icon_video
            }
            holder.binding.elemIcon.setImageResource(iconResId)
            holder.binding.fileSubtitle.text = current.displayFileSize
            holder.binding.fileAside.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return courseFiles.size
    }
}
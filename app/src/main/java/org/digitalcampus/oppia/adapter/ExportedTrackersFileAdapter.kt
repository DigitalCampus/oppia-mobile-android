package org.digitalcampus.oppia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.apache.commons.io.FileUtils
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowActivitylogBinding
import org.digitalcampus.oppia.adapter.ExportedTrackersFileAdapter
import org.digitalcampus.oppia.task.ExportActivityTask
import org.digitalcampus.oppia.utils.DateUtils
import org.joda.time.format.DateTimeFormat
import java.io.File

class ExportedTrackersFileAdapter @JvmOverloads constructor(
    private val fileList: List<File>,
    private val listener: OnItemClickListener?,
    private val showDeleteButton: Boolean = false
) : RecyclerView.Adapter<ExportedTrackersFileAdapter.ViewHolder>() {

    companion object {
        val TAG = ExportedTrackersFileAdapter::class.simpleName
    }

    interface OnItemClickListener {
        fun onItemShareClick(fileToShare: File?)
        fun onItemToDelete(fileToDelete: File?)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowActivitylogBinding = RowActivitylogBinding.bind(itemView)

        init {
            binding.shareBtn.setOnClickListener {
                listener?.onItemShareClick(fileList[adapterPosition])
            }
            if (showDeleteButton) {
                binding.deleteBtn.setOnClickListener {
                    listener?.onItemToDelete(fileList[adapterPosition])
                }
            } else {
                binding.deleteBtn.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_activitylog, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = fileList[position]
        val filename = current.name
        var username = filename.substring(0, filename.indexOf('_'))
        if (username == "activity") {
            username = "Multiple users"
        }
        val f = DateTimeFormat.forPattern(ExportActivityTask.activityTimestampFormat)
        val dateTime = f.parseDateTime(
            filename.substring(
                filename.lastIndexOf('_') + 1,
                filename.lastIndexOf('.')
            )
        )
        val date = DateUtils.DISPLAY_DATETIME_FORMAT.print(dateTime)

        holder.binding.fileName.text = username
        holder.binding.fileSize.text = FileUtils.byteCountToDisplaySize(current.length())
        holder.binding.fileDate.text = date
    }

    override fun getItemCount(): Int {
        return fileList.size
    }
}
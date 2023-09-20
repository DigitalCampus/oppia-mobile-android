package org.digitalcampus.oppia.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.listener.OnRemoveButtonClickListener
import org.digitalcampus.oppia.model.OfflineCourseFile

class OfflineCourseImportAdapter(
    private val context: Context,
    private val fileList: ArrayList<OfflineCourseFile>,
    private val listener: OnRemoveButtonClickListener?
) : RecyclerView.Adapter<OfflineCourseImportAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileTitle: TextView = itemView.findViewById(R.id.file_title)
        val fileType: TextView = itemView.findViewById(R.id.type_badge)
        val progressBar: ProgressBar = itemView.findViewById(R.id.import_progress)
        val rowBtn: ImageButton = itemView.findViewById(R.id.row_btn)
        private val importing = false

        init {
            rowBtn.setOnClickListener {
                if (!importing) {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener?.onRemoveButtonClick(position)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_offline_course_import_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileItem = fileList[position]
        holder.fileTitle.text = fileItem.file.name
        holder.fileType.text = fileItem.type.toString()
        val badgeColor = getColorByFileType(fileItem.type)
        val badgeDrawable = holder.fileType.background as GradientDrawable
        badgeDrawable.setTint(badgeColor)
        holder.fileType.background = badgeDrawable
        val iconResource = getIconByFileStatus(fileItem.status)
        holder.rowBtn.setImageResource(iconResource)
        if (fileItem.status === OfflineCourseFile.Status.IMPORTING) {
            holder.progressBar.visibility = View.VISIBLE
            holder.rowBtn.visibility = View.GONE
        } else {
            holder.progressBar.visibility = View.GONE
            holder.rowBtn.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    private fun getColorByFileType(fileType: OfflineCourseFile.FileType): Int {
        val colorResId = when (fileType) {
            OfflineCourseFile.FileType.COURSE -> R.color.bg_badge_status_read_only
            OfflineCourseFile.FileType.MEDIA -> R.color.bg_badge_status_draft
            else -> R.color.grey_dark
        }
        return ContextCompat.getColor(context, colorResId)
    }

    private fun getIconByFileStatus(status: OfflineCourseFile.Status): Int {
        return if (status === OfflineCourseFile.Status.IMPORTED) {
            R.drawable.ic_action_accept
        } else {
            R.drawable.ic_action_cancel
        }
    }
}
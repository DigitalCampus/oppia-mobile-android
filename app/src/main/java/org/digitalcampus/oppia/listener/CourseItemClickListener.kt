package org.digitalcampus.oppia.listener

interface CourseItemClickListener {
    fun onItemClick(position: Int)
    fun onContextMenuItemSelected(position: Int, itemId: Int)
}
package org.digitalcampus.oppia.model

class ActivityType(var name: String, var type: String, var color: Int, var isEnabled: Boolean) {

    companion object {
        const val ALL = "all"
    }

    var values = ArrayList<Int>()

    override fun toString(): String {
        return name
    }
}
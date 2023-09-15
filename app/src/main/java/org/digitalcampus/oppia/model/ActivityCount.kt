package org.digitalcampus.oppia.model

class ActivityCount {
    var typeCount: MutableMap<String, Int> = LinkedHashMap()

    companion object {
        @JvmStatic
        fun initialize(activityTypes: List<ActivityType>): ActivityCount {
            val activityCount = ActivityCount()
            for (activityType in activityTypes) {
                activityCount.typeCount[activityType.type] = 0
            }
            return activityCount
        }
    }


    fun incrementNumberActivityType(type: String) {
        typeCount[type] = typeCount.getOrDefault(type, 0) + 1
    }

    fun getValueForType(type: String): Int {
        return typeCount[type] ?: 0
    }

    fun hasValidEvent(event: String): Boolean {
        return typeCount.containsKey(event)
    }

}
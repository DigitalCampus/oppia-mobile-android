package org.digitalcampus.oppia.model.db_model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.digitalcampus.oppia.database.converters.TimestampConverter
import org.joda.time.DateTime

@Entity(tableName = "leaderboard")
class Leaderboard : Comparable<Leaderboard> {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0
    var username: String? = null
    var fullname: String? = null
    var points = 0
    var position = 0

    @TypeConverters(TimestampConverter::class)
    var lastupdate = DateTime()

    @Ignore
    var isUser = false

    @Ignore
    constructor(username: String, fullname: String?, points: Int, lastupdate: DateTime, position: Int) {
        this.username = username
        this.fullname = fullname
        this.points = points
        this.position = position
        this.lastupdate = lastupdate
    }

    @Ignore
    constructor(username: String, fullname: String?, points: Int, lastupdateStr: String?, position: Int) {
        this.username = username
        this.fullname = fullname
        this.points = points
        this.position = position
        setLastupdateStr(lastupdateStr)
    }

    constructor() {}

    override operator fun compareTo(other: Leaderboard): Int {
        return points.compareTo(other.points)
    }

    fun setLastupdateStr(string: String?) {
        lastupdate = TimestampConverter.fromTimestamp(string)
    }

}
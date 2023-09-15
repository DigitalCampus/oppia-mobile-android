package org.digitalcampus.oppia.model.db_model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_preference",
    indices = [Index(name = "idx", value = ["username", "preference"], unique = true)]
)
class UserPreference(var username: String, var preference: String, var value: String) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0

}
package org.digitalcampus.oppia.model.db_model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_custom_field")
class UserCustomField {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0
    var username: String = ""

    @ColumnInfo(name = "field_key")
    var fieldKey: String = ""

    @ColumnInfo(name = "value_str")
    var valueStr: String? = null

    @ColumnInfo(name = "value_int")
    var valueInt: Int? = null

    @ColumnInfo(name = "value_bool")
    var valueBool: Boolean? = null

    @ColumnInfo(name = "value_float")
    var valueFloat: Float? = null
}
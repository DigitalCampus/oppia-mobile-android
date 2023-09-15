package org.digitalcampus.oppia.model

sealed class CustomValueType
data class StringValue(val value: String) : CustomValueType()
data class BooleanValue(val value: Boolean) : CustomValueType()
data class IntValue(val value: Int) : CustomValueType()
data class FloatValue(val value: Float) : CustomValueType()

class CustomValue(private val value: CustomValueType) {
    fun getValue(): Any {
        return value
    }

    override fun toString(): String {
        return value.toString()
    }
}
package org.digitalcampus.oppia.utils.multichoice

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.SparseBooleanArray
import androidx.collection.LongSparseArray

class SavedState internal constructor(input: Parcel) : Parcelable {
    private var checkedItemCount: Int
    private var checkStates: SparseBooleanArray?
    var checkedIdStates: LongSparseArray<Int>? = null
    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(checkedItemCount)
        out.writeSparseBooleanArray(checkStates)

        checkedIdStates?.let {checkedIdStates ->
            out.writeInt(checkedIdStates.size())
            for (i in 0 until checkedIdStates.size()) {
                out.writeLong(checkedIdStates.keyAt(i))
                out.writeInt(checkedIdStates.valueAt(i))
            }
        } ?: out.writeInt(-1)
    }

    override fun describeContents(): Int {
        return 0
    }

    init {
        checkedItemCount = input.readInt()
        checkStates = input.readSparseBooleanArray()
        val n = input.readInt()
        if (n >= 0) {
            checkedIdStates = LongSparseArray(n)
            for (i in 0 until n) {
                val key = input.readLong()
                val value = input.readInt()
                checkedIdStates?.append(key, value)
            }
        }
    }

    companion object {
        @JvmField
        val CREATOR: Creator<SavedState?> = object : Creator<SavedState?> {
            override fun createFromParcel(input: Parcel): SavedState {
                return SavedState(input)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
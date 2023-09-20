package org.digitalcampus.oppia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.RowDeviceBinding
import org.digitalcampus.oppia.adapter.DevicesBTAdapter.DevicesBTViewHolder

class DevicesBTAdapter(private val context: Context, private val devices: List<String>) : RecyclerViewClickableAdapter<DevicesBTViewHolder>() {

    inner class DevicesBTViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding: RowDeviceBinding

        init {
            binding = RowDeviceBinding.bind(itemView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesBTViewHolder {
        val contactView = LayoutInflater.from(context).inflate(R.layout.row_device, parent, false)
        return DevicesBTViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: DevicesBTViewHolder, position: Int) {
        val device = getItemAtPosition(position)
        holder.binding.root.text = device
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    fun getItemAtPosition(position: Int): String {
        return devices[position]
    }
}
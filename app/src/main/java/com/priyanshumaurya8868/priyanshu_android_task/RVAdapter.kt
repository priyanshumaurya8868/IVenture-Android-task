package com.priyanshumaurya8868.priyanshu_android_task

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.priyanshumaurya8868.priyanshu_android_task.databinding.RvItemBinding
import com.priyanshumaurya8868.priyanshu_android_task.models.Appointed
import com.priyanshumaurya8868.priyanshu_android_task.models.Appointment
import com.priyanshumaurya8868.priyanshu_android_task.util.Constant

class RVAdapter : ListAdapter<Appointment, RVAdapter.MyVH>(
    object : DiffUtil.ItemCallback<Appointment>() {
        override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment) =
            oldItem.toString() == newItem.toString()

    }
) {


    inner class MyVH(val binding: RvItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyVH {
        val inflater = parent.context.getSystemService(LayoutInflater::class.java)
        val binding = RvItemBinding.inflate(inflater, parent, false)
        return MyVH(binding)
    }

    override fun onBindViewHolder(holder: MyVH, position: Int) {
        val obj = getItem(position)
        holder.binding.apply {
            time.text = obj.time
            when (obj.state) {
                Constant.ENABLE -> enableState(this)
                Constant.APPOINTED -> appointedState(this)
                Constant.BUSY -> busyState(this)
                Constant.DISABLE -> disableState(this)

            }
            this.root.setOnClickListener {
                if (onItemClickListener != null && mRv_id != null) {
                    onItemClickListener!!(Appointed(mRv_id!!, position))
                }
            }
        }

    }

    private fun busyState(binding: RvItemBinding) {
        binding.apply {
            container.setBackgroundResource(R.drawable.busy_rv_bc)
            stretchLine.visibility = View.VISIBLE
        }
    }

    private fun appointedState(binding: RvItemBinding) {
        binding.apply {
            container.setBackgroundResource(R.drawable.appointed_bc)
            time.setTextColor(ColorStateList.valueOf(root.context.getColor(R.color.white)))
        }
    }

    private fun disableState(binding: RvItemBinding) {
        binding.apply {
            container.setBackgroundResource(R.drawable.disable_rv_item_bc)
            time.setTextColor(ColorStateList.valueOf(root.context.getColor(R.color.grey)))
        }
    }

    private fun enableState(binding: RvItemBinding) {
        binding.container.setBackgroundResource(R.drawable.enable_rv_bc)
    }

    private var onItemClickListener: ((Appointed) -> Unit)? = null

    private var mRv_id: Int? = null

    fun setOnClickListener(listener: (Appointed) -> Unit, rv_id: Int) {
        mRv_id = rv_id
        onItemClickListener = listener
    }
}